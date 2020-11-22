package de.kissphoto.view.viewerHelpers;

import de.kissphoto.helper.I18Support;
import de.kissphoto.model.MediaFile;
import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.mediaViewers.ZoomableViewer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ResourceBundle;
/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class is the base class for all viewer with MediaPlayers (i.e. MovieViewerFX (implemented) and MovieViewerVLC
 * It basically consists of a MediaPlayer (to be defined in implementing subclasses) and PlayerControls.
 *
 * @author Dr. Ingo Kreuz
 * @since 2014-07-24
 * @version 2020-11-02 media Viewers now determine itself if they can show/play a file (no longer the content view)
 * @version 2020-10-25 made abstract and moved JavaFx-Player into MovieViewerFX, so than MovieViewerVLC could be added
 * @version 2017-10-20 single click and space (if not zoomed) will toggle Play/Pause now additionally
 * @version 2017-10-15 handlers installed for mediaPlayer.StatusProperty and  autoPlayProperty to sync the player and the menus (main/context)
 * @version 2017-10-08 autoplay suspended while edit-mode/multi-edit-mode
 *
 */
abstract public class PlayerViewer extends StackPane implements ZoomableViewer {
  protected static ResourceBundle language = I18Support.languageBundle;

  //language file keys (used also in main menu)
  public static final String PAUSE = "pause";
  public static final String PLAY = "play";
  public static final String AUTO_PLAY = "auto.play";
  public static final String STOP = "stop";

  protected boolean finished; //true if the endOfMedia event had been detected, false if any other status has been detected

  private static final KeyCodeCombination PLAY_PAUSE_KEY_CODE_COMBINATION = new KeyCodeCombination(KeyCode.P);

  //link to mainMenu items for controlling checking and disabling. Use same link for all instances of the player (i.e. also for full screen)
  protected static MenuItem mainMenuPlayPauseItem;
  protected static MenuItem mainMenuStopItem;

  //contextMenu items for controlling checking and disabling
  protected ContextMenu contextMenu = new ContextMenu();
  protected CheckMenuItem autoPlayItem;
  protected MenuItem playPauseItem;
  protected MenuItem stopItem;

  protected MediaContentView mediaContentView; //link to the underlying mediaContentView (e.g.for binding sizes and for next media after endOfMedia)

  protected PlayerControls playerControls;

  public SimpleBooleanProperty autoPlayProperty = new SimpleBooleanProperty(true); //play immediately and skip automatically to next media

  protected boolean lastMouseButtonWasPrimary = false;
  protected boolean lastMouseDownWasMouseDragged = false;

  //


  /**
   * @param mediaContentView remember the view where this viewer resides in
   * @constructor initialize the play status
   */
  public PlayerViewer(MediaContentView mediaContentView) {
    super();

    this.mediaContentView = mediaContentView;
    playerControls = new PlayerControls(this);
    setAlignment(playerControls, Pos.TOP_CENTER);
    //will be added to this StackPane in the implementing subclasses on top of their mediaView

    //visible only while hovering
    playerControls.setVisible(false);
  }

  /**
   * @param event
   * @return if event has been handled
   */
  public boolean handleMouseMoved(MouseEvent event) {
    playerControls.resetThreadAndShow();
    return true;
  }

  ;

  /**
   * @param event
   * @return if event has been handled
   */
  public boolean handleMouseDragged(MouseEvent event) {
    lastMouseDownWasMouseDragged = true;
    return false; //nothing has happened for the user, so don't block further actions if any
  }

  ;

  /**
   * @param event
   * @return if event has been handled
   */
  public boolean handleMousePressed(MouseEvent event) {
    lastMouseButtonWasPrimary = event.isPrimaryButtonDown();
    return false; //nothing has happened for the user
  }

  /**
   * @param event
   * @return if event has been handled
   */
  public boolean handleMouseClicked(MouseEvent event) {
    boolean handled = false;
    //do not handle double-click i.e. this is full screen
    if (event.getClickCount() == 1 && lastMouseButtonWasPrimary && !lastMouseDownWasMouseDragged) {
      //togglePlayPause();// would interfere with the controlArea of playerControls
      handled = true;
      }
    lastMouseDownWasMouseDragged = false;
    return handled;
  }

  ;

  /**
   * @param event
   * @return if event has been handled
   */
  public boolean handleKeyPressed(KeyEvent event) {
    boolean handled = false;
    if (event.getCode() == KeyCode.SPACE && !(event.isShiftDown() || event.isControlDown())) { //space without shift or ctrl
      playerControls.togglePlayPause();
      handled = true;
    }
    return handled;
  }


  /**
   * call this before setting PlayerViewer to null, e.g. to end internal thread
   */
  public void cleanUp() {
    playerControls.cleanUp();
    releaseAll();
  }


  /**
   * Register the Player's main menu items, so that they can be enabled/disabled dependent on the players status
   * MainMenuBar will register its menu items with that
   *
   * @param playItem     main menu item to enable/disable/change Pause/Pla
   * @param stopItem     main menu to enable/disable
   */
  public void registerMainMenuItems(MenuItem playItem, MenuItem stopItem) {
    mainMenuPlayPauseItem = playItem;
    mainMenuStopItem = stopItem;
  }

  /**
   * add the player functions to the context-menu
   */
  protected void initPlayerContextMenu() {
    //---- Player support
    autoPlayItem = new CheckMenuItem(language.getString(AUTO_PLAY));
    autoPlayItem.setSelected(true);
    autoPlayItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    autoPlayItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        toggleAutoPlay();
      }
    });

    playPauseItem = new MenuItem(language.getString(PLAY));  //P = Pause/Play --> only one of the is enabled at one time (at maximum)
    playPauseItem.setAccelerator(PLAY_PAUSE_KEY_CODE_COMBINATION);
    playPauseItem.setDisable(true);
    playPauseItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        playerControls.togglePlayPause();
      }
    });

    stopItem = new MenuItem(language.getString(STOP)); //S=Stop/Rewind
    stopItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_ANY));
    stopItem.setDisable(true);
    stopItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        stop();
      }
    });
    contextMenu.getItems().addAll(autoPlayItem, playPauseItem, stopItem, new SeparatorMenuItem());

    //sync context menu's autoPlay check with property
    autoPlayProperty.addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        autoPlayItem.setSelected(newValue);
      }
    });
  }

  /**
   *
   * @return the status of the media player of this PlayerViewer
   */
  abstract public javafx.scene.media.MediaPlayer.Status getStatus();

/**
   * put the media (movie) into the MovieViewer and play it if "playing" was active before or pause it if not
   *
   * @param mediaFile containing the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the playable media is loaded/visible
   * @return true if the file could be played, false if not
   */
  abstract public boolean setMediaFileIfCompatible(MediaFile mediaFile, Duration seekPosition);

  /**
   * reset the player: stop it and free all event Handlers
   */
  abstract public void resetPlayer();

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  abstract public void play();

  /**
   * start player from the beginning to implement repeat track
   */
  abstract public void rewindAndPlayWhenFinished();

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  abstract public void pause();


  /**
   * stop, rewind
   * adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  abstract public void stop();

  /**
   * seek Position (Duration)
   * if mediaPlayer is null (currently no media file displayed) nothin happens
   * @param newPos position to jump to
   */
  abstract public void seek(Duration newPos);

  /**
   * get the current position of the media currently playing
   * @return current position
   */
  abstract public Duration getCurrentTime();

  /**
   * if mediaPlayer is null (currently no media file displayed) Duration(0) is returned
   * @return the total length of the currently loaded media
   */
  abstract public Duration getTotalDuration();

  /**
   * Call this method when closing the main program to release all external resources
   */
  abstract public void releaseAll();

  public PlayerControls getPlayerControls(){
    return playerControls;
  }

  protected void setPlayerStatusInAllMenues(MediaPlayer.Status newPlayerStatus) {
    PlayerViewer.setMenuItemsForPlayerStatus(newPlayerStatus, mainMenuStopItem, mainMenuPlayPauseItem);
    PlayerViewer.setMenuItemsForPlayerStatus(newPlayerStatus, stopItem, playPauseItem);
    playerControls.setPlayPausedButtonForPlayerStatus(newPlayerStatus);
  }


  /**
   * toggle auto play. if movie viewer is currently visible then pause/play accordingly
   */
  public void toggleAutoPlay() {
    autoPlayProperty.set(!autoPlayProperty.get());


    if (autoPlayProperty.get()) {
      if (isVisible()) play();
    } else {
      if (isVisible()) pause();
    }
  }


  //------------------------------- static helper for menu items ---------------------
  public static void setMenuItemsForPlayerStatus(MediaPlayer.Status newValue, MenuItem stopItem, MenuItem playPauseItem) {
    switch (newValue) {
      case READY:
      case HALTED:
      case PAUSED:
        stopItem.setDisable(false);
        playPauseItem.setDisable(false);
        playPauseItem.setText(language.getString(PlayerViewer.PLAY));
        break;
      case PLAYING:
        stopItem.setDisable(false);
        playPauseItem.setDisable(false);
        playPauseItem.setText(language.getString(PlayerViewer.PAUSE));
        break;
      case STOPPED:
        stopItem.setDisable(true);
        playPauseItem.setDisable(false);
        playPauseItem.setText(language.getString(PlayerViewer.PLAY));
        break;
      case STALLED:
      case UNKNOWN:
      case DISPOSED:
      default:
        stopItem.setDisable(true);
        playPauseItem.setDisable(false);
        break;
    }
  }

}
