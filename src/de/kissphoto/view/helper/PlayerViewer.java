package de.kissphoto.view.helper;

import de.kissphoto.helper.I18Support;
import de.kissphoto.view.MediaContentView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ResourceBundle;

import static javafx.scene.media.MediaPlayer.Status.PAUSED;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class is the base class for all viewer with MediaPlayers (i.e. MovieViewer (implemented) and AudioViewer (not yet implemented)
 * It basically consists of a MediaPlayer and PlayerControls.
 *
 * @author Dr. Ingo Kreuz
 * @date 2014-07-24
 * @modified: 2017-10-08 autoplay suspended while edit-mode/multi-edit-mode
 * @modified: 2017-10-15: handlers installed for mediaPlayer.StatusProperty and  autoPlayProperty to sync the player and the menus (main/context)
 * @modified: 2017-10-20: single click and space (if not zoomed) will toggle Play/Pause now additionally
 *
 */
public class PlayerViewer extends StackPane {
  protected static ResourceBundle language = I18Support.languageBundle;

  //language file keys (used also in main menu)
  public static final String PAUSE = "pause";
  public static final String PLAY = "play";
  public static final String AUTO_PLAY = "auto.play";
  public static final String STOP = "stop";

  private static final KeyCodeCombination PLAY_PAUSE_KEY_CODE_COMBINATION = new KeyCodeCombination(KeyCode.P);

  //link to mainMenu items for controlling checking and disabling. Use same link for all instances of the player (i.e. also for full screen)
  private static MenuItem mainMenuPlayPauseItem;
  private static MenuItem mainMenuStopItem;

  //contextMenu items for controlling checking and disabling
  protected ContextMenu contextMenu = new ContextMenu();
  private CheckMenuItem autoPlayItem;
  private MenuItem playPauseItem;
  private MenuItem stopItem;

  protected MediaContentView mediaContentView; //link to the underlying mediaContentView (e.g.for binding sizes and for next media after endOfMedia)

  protected MediaPlayer mediaPlayer;      //initialized in setMedia()
  protected PlayerControls playerControls;

  public SimpleBooleanProperty autoPlayProperty = new SimpleBooleanProperty(true); //play immediately and skip automatically to next media

  protected boolean lastMouseButtonWasPrimary = false;
  protected boolean lastMouseDownWasMouseDragged = false;


  /**
   * @param mediaContentView remember the view where this viewer resides in
   * @constructor initialize the play status
   */
  public PlayerViewer(MediaContentView mediaContentView) {
    super();

    this.mediaContentView = mediaContentView;
    playerControls = new PlayerControls(this);

    //visible only while hovering
    playerControls.setVisible(false);
  }

  /**
   * @param event
   * @return if event has been handled
   */
  public boolean handleMouseMoved(MouseEvent event) {
    playerControls.show();
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
    if (event.getClickCount() == 1 && lastMouseButtonWasPrimary && !lastMouseDownWasMouseDragged) {
      togglePlayPause();
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
      togglePlayPause();
      handled = true;
    }
    return handled;
  }


  /**
   * call this before setting PlayerViewer to null, e.g. to end internal thread
   */
  public void cleanUp() {
    playerControls.cleanUp();
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
        togglePlayPause();
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
   * put the media (movie) into the MovieViewer and play it
   *
   * @param media the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the playable media is loaded/visible
   */
  public void setMedia(Media media, Duration seekPosition) {
    System.out.println("PlayerViewer.setMedia: resetPlayer");
    resetPlayer();
    System.out.println("PlayerViewer.setMedia: new MediaPlayer");
    mediaPlayer = new MediaPlayer(media);

    System.out.println("PlayerViewer.setMedia: setOnReady");
    mediaPlayer.setOnReady(new Runnable() {
      @Override
      public void run() {
        if (seekPosition != null) mediaPlayer.seek(seekPosition);

        if (autoPlayProperty.get() && !mediaContentView.isFileTableViewInEditMode())
          play();
        else
          pause();

        // as the media is playing move the slider for progress
        playerControls.setSliderScaling();
        playerControls.showProgress(Duration.ZERO);

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
          public void invalidated(Observable ov) {
            if (mediaPlayer != null) playerControls.showProgress(mediaPlayer.getCurrentTime());
          }
        });
      }
    });
    System.out.println("PlayerViewer.setMedia: setOnEndOfMedia");
    mediaPlayer.setOnEndOfMedia(new Runnable() {
      @Override
      public void run() {
        if (autoPlayProperty.get() && !mediaContentView.isFileTableViewInEditMode()) {
          mediaContentView.showNextMedia();   //if already the last, showNextMedia() will do nothing and media will remain paused...
        }
      }
    });

    //install listener for player status to update play/pause/inactive, stop active/inactive
    System.out.println("PlayerViewer.setMedia: statusProperty");
    mediaPlayer.statusProperty().addListener(new ChangeListener<MediaPlayer.Status>() {
      @Override
      public void changed(ObservableValue<? extends MediaPlayer.Status> observable, MediaPlayer.Status oldValue, MediaPlayer.Status newValue) {
        PlayerViewer.setMenuItemsForPlayerStatus(newValue, mainMenuStopItem, mainMenuPlayPauseItem);
        PlayerViewer.setMenuItemsForPlayerStatus(newValue, stopItem, playPauseItem);
        playerControls.setPlayPausedButtonForPlayerStatus(newValue);
      }
    });

  }

  /**
   * reset the player: stop it and free all event Handlers
   */
  public void resetPlayer() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      //mediaPlayer.currentTimeProperty().removeListener(progressListener);
      mediaPlayer.setOnPaused(null);
      mediaPlayer.setOnPlaying(null);
      mediaPlayer.setOnReady(null);
      mediaPlayer.dispose();
      mediaPlayer = null;
    }
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

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void play() {
    if (mediaPlayer != null) {
      mediaPlayer.play();
    }
  }

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void pause() {
    if (mediaPlayer != null) {
      mediaPlayer.pause();
    }
  }

  /**
   * exactly execute what keyboard "P" does: if paused then play and vice versa
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void togglePlayPause() {
    if (mediaPlayer != null && mediaPlayer.getStatus() == PAUSED)
      play();
    else
      pause();
  }

  /**
   * stop, rewind
   * adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void stop() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
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

  //------------------------------- getters / setters ------------------------
  public boolean isPaused() {
    return (mediaPlayer.getStatus() == PAUSED);
  }

  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }
}
