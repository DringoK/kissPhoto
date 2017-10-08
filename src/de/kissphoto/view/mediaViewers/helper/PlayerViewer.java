package de.kissphoto.view.mediaViewers.helper;

import de.kissphoto.helper.I18Support;
import de.kissphoto.view.MediaContentView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class is the base class for all viewer with MediaPlayers (i.e. MovieViewer (implemented) and AudioViewer (not yet implemented)
 * It basically consists of a MediaPlayer and PlayerControls.
 *
 * @author Dr. Ingo Kreuz
 * @date 2014-07-24
 * @modified: 2017-10-08 autoplay suspended while edit-mode/multi-edit-mode
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
  private static CheckMenuItem mainMenuAutoPlayItem;
  private static MenuItem mainMenuPlayPauseItem;
  private static MenuItem mainMenuStopItem;

  //contextMenu items for controlling checking and disabling
  protected ContextMenu contextMenu = new ContextMenu();
  private CheckMenuItem autoPlayItem;
  private MenuItem playItem;
  private MenuItem pauseItem;
  private MenuItem stopItem;

  protected MediaContentView mediaContentView; //link to the underlying mediaContentView (e.g.for binding sizes and for next media after endOfMedia)

  protected MediaPlayer mediaPlayer;      //initialized in setMedia()
  protected PlayerControls playerControls;

  private boolean paused = false;      //currently paused? (otherwise autoPlay is active)

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

    setOnMouseMoved(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        playerControls.show();
      }
    });
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
   * @param autoPlayItem main menu item to toggle autoPlay
   * @param playItem     main menu item to enable/disable/change Pause/Pla
   * @param stopItem     main menu to enable/disable
   */
  public void registerMainMenuItems(CheckMenuItem autoPlayItem, MenuItem playItem, MenuItem stopItem) {
    mainMenuAutoPlayItem = autoPlayItem;
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
        toggleAutoPlay(false);
      }
    });

    playItem = new MenuItem(language.getString(PLAY));  //P = Pause/Play --> only one of the is enabled at one time (at maximum)
    playItem.setAccelerator(PLAY_PAUSE_KEY_CODE_COMBINATION);
    playItem.setDisable(true);
    playItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        play();
      }
    });
    pauseItem = new MenuItem(language.getString(PAUSE));  //P = Pause/Play --> only one of the is enabled at one time (at maximum)
    pauseItem.setAccelerator(PLAY_PAUSE_KEY_CODE_COMBINATION);
    pauseItem.setDisable(true);
    pauseItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        pause();
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
    contextMenu.getItems().addAll(autoPlayItem, playItem, pauseItem, stopItem, new SeparatorMenuItem());
  }

  /**
   * put the media (movie) into the MovieViewer and play it
   *
   * @param media the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the playable media is loaded/visible
   */
  public void setMedia(Media media, Duration seekPosition) {
    resetPlayer();
    mediaPlayer = new MediaPlayer(media);

    mediaPlayer.setOnReady(new Runnable() {
      @Override
      public void run() {
        if (seekPosition != null) mediaPlayer.seek(seekPosition);

        if (mainMenuAutoPlayItem.isSelected() && !mediaContentView.isFileTableViewInEditMode())
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
    mediaPlayer.setOnEndOfMedia(new Runnable() {
      @Override
      public void run() {
        if (mainMenuAutoPlayItem.isSelected() && !mediaContentView.isFileTableViewInEditMode()) {
          mediaContentView.showNextMedia();   //if already the last, showNextMedia() will do nothing and media will remain paused...
        }
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
    //adjust the menus
    mainMenuPlayPauseItem.setDisable(true);
    mainMenuPlayPauseItem.setText(language.getString(PLAY));
    playItem.setDisable(true);
    pauseItem.setDisable(true);
    mainMenuStopItem.setDisable(true);
    stopItem.setDisable(true);
  }

  /**
   * toggle auto play. if movie viewer is currently visible then pause/play accordingly
   * if MainMenu calls this it has already toggled it's state
   * if keyboard/context menu calls this method it needs to toggle main menu first
   *
   * @param mainMenuTriggered mainMenu calls this with true all others (context/keyboard) with false
   */
  public void toggleAutoPlay(boolean mainMenuTriggered) {
    if (!mainMenuTriggered) mainMenuAutoPlayItem.setSelected(!mainMenuAutoPlayItem.isSelected());

    if (mainMenuAutoPlayItem.isSelected()) {
      autoPlayItem.setSelected(true);
      if (isVisible()) play();
    } else {
      autoPlayItem.setSelected(false);
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
      //adjust the menus
      mainMenuPlayPauseItem.setDisable(false);
      mainMenuPlayPauseItem.setText(language.getString(PAUSE));
      playItem.setDisable(true);
      pauseItem.setDisable(false);
      mainMenuStopItem.setDisable(false);
      stopItem.setDisable(false);

      paused = false;
    }
  }

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void pause() {
    if (mediaPlayer != null) {
      mediaPlayer.pause();
      //adjust the menus
      mainMenuPlayPauseItem.setDisable(false);
      mainMenuPlayPauseItem.setText(language.getString(PLAY));
      playItem.setDisable(false);
      pauseItem.setDisable(true);
      mainMenuStopItem.setDisable(false);  //stop is still possible when paused!
      stopItem.setDisable(false);

      paused = true;
    }
  }

  /**
   * exactly execute what keyboard "P" does: if paused then play and vice versa
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void togglePlayPause() {
    //checking on null is done in play() and pause()
    if (paused)
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
      //adjust the menus
      mainMenuPlayPauseItem.setDisable(false);
      mainMenuPlayPauseItem.setText(language.getString(PLAY));
      playItem.setDisable(false);
      pauseItem.setDisable(true);
      mainMenuStopItem.setDisable(true);
      stopItem.setDisable(true);
      paused = true;
    }
  }

  //------------------------------- getters / setters ------------------------
  public boolean isPaused() {
    return paused;
  }

  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }
}
