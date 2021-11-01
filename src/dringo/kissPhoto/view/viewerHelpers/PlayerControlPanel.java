package dringo.kissPhoto.view.viewerHelpers;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.MediaContentView;
import dringo.kissPhoto.view.viewerHelpers.viewerButtons.PlayListButton;
import dringo.kissPhoto.view.viewerHelpers.viewerButtons.PlayPauseButton;
import dringo.kissPhoto.view.viewerHelpers.viewerButtons.RepeatButton;
import dringo.kissPhoto.view.mediaViewers.PlayerViewer;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a MediaPlayer's controls area extending the basic ViewerControls
 * <ul>
 * <li>a play/pause button
 * <li>a progress bar for jumping to a certain point of the media
 * <li>text indicating time/total time
 * <li>option buttons: Repeat and Playlist mode
 * </ul>
 * <br>
 * <ul>
 * <li>to be connected to a JavaFX MediaPlayer
 * </ul>
 * <p/>
 * It is a transparent VBox which's size is bound to a playerViewer<br>
 * with controlArea (HBox) and optionArea (HBox) in it<br>
 * <br>
 * The current status is not reported anywhere but read by MediaContentView whenever needed<br>
 * The current status is stored in/restored from globalSettings, so that it is the same status after restart (Paused, PlayListMode, RepeatMode)<br>
 * The current status reflects the intend of the user not of the player.
 * i.e. if the media is paused because end has reached, playing is still shown and skipping or changing media would restart playing.
 * Pushing the pause button in this case wil set userHasPaused()=true and show the play button.
 * When the play button is pushed then media is rewind if necessary and player is restarted.<br>
 * <br>
 * Note: it will be added into PlayerViewers(StackPane) and therefore will be bound to the PlayerViewers size
 *
 * @author Dringo
 * @since 2014-07-18
 * @version 2021-01-16 bugfix tooltip help of repeat/playListMode buttons
 * @version 2020-12-13 PlayerControls now one of the ViewerControls
 * @version 2020-11-30 option pane added (repeat, playListMode)
 * @version 2017-10-08 show the Player a longer time, higher player area
 */

public class PlayerControlPanel extends ViewerControlPanel {
  private final PlayerViewer playerViewer;

  private PlayPauseButton playPauseButton;
  private Slider progressSlider;
  private Text progressText;
  private Duration totalDuration;   //of the current media in playerViewer, used for scrollbar scaling and progressText

  private PlayListButton playlistButton;
  private RepeatButton repeatButton;

  //------------- IDs for GlobalSettings-File
  private static final String PAUSED_ID = "playerWasPaused";
  private static final String REPEAT_MODE_ID = "playerRepeatMode";
  private static final String PLAYLIST_MODE_ID = "playerPlayListMode";

  /**
   * create a player control and connect it to a MediaPlayer
   * @param viewer to be conntected to
   */
  public PlayerControlPanel(PlayerViewer viewer) {
    super(viewer);   //builds control and option area and installs hiding thread

    playerViewer = viewer;

    //default values using direct set method to avoid changing the settings file
    playlistButton.setPlayListMode(false); //initalize with "not default value" to force a change event to initialize the tooltip
    playlistButton.setPlayListMode(true);  //this is the default value
    repeatButton.setRepeatMode(true);      //initalize with "not default value" to force a change event to initialize the tooltip
    repeatButton.setRepeatMode(false);     //this is the default value

    playPauseButton.setPaused(false); //if playing then show pause
    //then try to load the last values
    restoreLastPlayerStatus();

    //toggle play/pause to user's intend
    playPauseButton.setOnMousePressed(mouseEvent -> {
      togglePlayPause();
      mouseEvent.consume();
    });
  }

  /**
   * overwrite the behavior of ViewerControlPanel
   * and build the control area additionally with player controls
   * called from ViewerControl constructor
   */
  protected void createControlArea() {
    super.createControlArea();

    //----------- build controls ---------------------
    playPauseButton = new PlayPauseButton(BUTTON_SIZE, BACKGROUND_COLOR, ICON_COLOR);

    progressText = new Text();
    progressText.setFill(Color.WHITE);

    Rectangle spaceBeforeBurgerMenu = new Rectangle(BUTTON_SIZE / 2, BUTTON_SIZE, BACKGROUND_COLOR);

    progressSlider = createSlider();
    progressSlider.prefWidthProperty().bind(controlArea.prefWidthProperty());  //try to use as much space as possible of the controlArea for the progress Slider

    controlArea.getChildren().addAll(playPauseButton, progressSlider, progressText, spaceBeforeBurgerMenu);
  }

  /**
   * overwrite the behavior of ViewerControlPanel
   * and build the options area additionally with repeat and playList Option
   * called from ViewerControl constructor
   */
  protected void createOptionArea() {
    super.createOptionArea();

    //----------- build controls ---------------------
    playlistButton = new PlayListButton(OPTIONS_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    playlistButton.setOnMouseClicked((mouseEvent) -> {
      setPlayListMode(!isPlayListMode());
      mouseEvent.consume();
    });

    repeatButton = new RepeatButton(OPTIONS_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    repeatButton.setOnMouseClicked((mouseEvent) -> {
      setRepeatMode(!isRepeatMode());
      mouseEvent.consume();
    });

    repeatButton.playListModeProperty().bind(playlistButton.playListModeProperty()); //keep playListMode synced for repeat and playlistbutton for better Tooltips (see RepeatButton.setTooltipText())

    optionArea.getChildren().addAll(playlistButton, repeatButton);
  }

  /**
   * Restore last Option-Settings and Play-Status from Global-Settings properties file
   * assumes:
   * - globalSettings already loaded
   */
  private void restoreLastPlayerStatus() {
    try {
      setPlayListMode(Boolean.parseBoolean(KissPhoto.globalSettings.getProperty(PLAYLIST_MODE_ID)));
    } catch (Exception ignored) {
    }//if not loadable keep default value)

    try {
      setRepeatMode(Boolean.parseBoolean(KissPhoto.globalSettings.getProperty(REPEAT_MODE_ID)));
    } catch (Exception ignored) {
    }//if not loadable keep default value)

    try {
      playPauseButton.pausedProperty().set(Boolean.parseBoolean(KissPhoto.globalSettings.getProperty(PAUSED_ID)));
    } catch (Exception ignored) {
    }//if not loadable keep default value)
  }

  /**
   * report button status
   *
   * @return true if playList Mode is active
   */
  public boolean isPlayListMode() {
    return playlistButton.isPlayListMode();
  }

  /**
   * report button status
   *
   * @return true if Repeat Mode is active
   */
  public boolean isRepeatMode() {
    return repeatButton.isRepeatMode();
  }

  /**
   * user's intend
   *
   * @return true if user intended to pause media
   */
  public boolean isUserHasPaused() {
    return playPauseButton.pausedProperty().get();
  }

  /**
   * exactly execute what keyboard "P" does: if paused then play and vice versa..to user's intend
   * if this is the primary ContentView's Player and a secondary (FullScreen) Content View is active then forward this call to the FullScreen's player
   */
  public void togglePlayPause() {
    MediaContentView myMediaContentView = playerViewer.getMediaContentView();

    if (myMediaContentView.hasActiveFullScreenMediaContentView()){
      myMediaContentView.getFullScreenMediaContentView().getPlayerViewer().getPlayerControls().togglePlayPause();
    }else {
      if (playPauseButton.pausedProperty().get()) //start from the controls status and synchronize the users intend in play/pause if necessary
        play();
      else
        pause();
      //note: the button synchronizes with the status of the player when the new status is reported as an event
    }
  }

  /**
   * user intends to start media
   */
  public void play() {
    playerViewer.play();

    playPauseButton.pausedProperty().set(false);
    KissPhoto.globalSettings.setProperty(PAUSED_ID, playPauseButton.pausedProperty().toString());

    //note: the button synchronizes with the status of the player when the new status is reported as an event
  }

  /**
   * user intends to pause media
   */
  public void pause() {
    playerViewer.pause();
    playPauseButton.pausedProperty().set(true);
    KissPhoto.globalSettings.setProperty(PAUSED_ID, playPauseButton.pausedProperty().toString());

    //if at the end of media the player might still report PLAYING (e.g.FX' media player)
    //so force to show pause state for button
    playPauseButton.setPaused(true);

    //note: the button synchronizes with the status of the player when the new status is reported as an event
  }

  /**
   * restart media by seeking position 0.0
   * don't change paused state
   */
  public void rewind() {
    playerViewer.seek(Duration.ZERO);
  }

  /**
   * set button status
   *
   * @param newValue true if playList Mode shall be active
   */
  public void setPlayListMode(boolean newValue) {
    playlistButton.setPlayListMode(newValue);
    KissPhoto.globalSettings.setProperty(PLAYLIST_MODE_ID, Boolean.toString(newValue));
  }

  /**
   * set button status
   */
  public void setRepeatMode(boolean newValue) {
    repeatButton.setRepeatMode(newValue); //don't change playlist mode
    KissPhoto.globalSettings.setProperty(REPEAT_MODE_ID, Boolean.toString(newValue));
  }

  /**
   * the progress bar is a slider to show the position in the playable media
   * a change listener for the value is installed
   * The width of the slider is the "rest" of the
   *
   * @return the Slider
   */
  private Slider createSlider() {
    Slider slider = new Slider();
    slider.setSnapToTicks(false);

    //click needs initiate player.seek which again moves the slider to the clicked position
    slider.setOnMouseClicked(mouseEvent -> {
      //max -> width
      //pos -> x
      double pos = slider.getMax() / slider.getWidth() * mouseEvent.getX();
      playerViewer.seek(new Duration(pos));
      mouseEvent.consume();
    });

    //Slider has been dragged with mouse
    slider.valueProperty().addListener(ov -> {
      if (slider.isValueChanging()) {
        playerViewer.seek(new Duration(slider.getValue()));
      }
    });

    return slider;
  }

  /**
   * if new Media was set to the player then this method should be called "onReady" of the player
   * to update the scale of the slider
   */
  public void setSliderScaling(Duration totalDuration) {
    if ((totalDuration != null) && (totalDuration.toMillis() > 0)) {
      progressSlider.setMax(totalDuration.toMillis());
      progressSlider.setBlockIncrement(totalDuration.toMillis() / 10);
      this.totalDuration = totalDuration;
    } else {
      this.totalDuration = Duration.ZERO;
    }
  }

  // update slider as video is progressing if totalDuration and currentPos are valid
  public void showProgress(Duration currentPos) {
    if (currentPos != null && totalDuration != null) {
      progressSlider.setValue(currentPos.toMillis());
      progressText.setText(String.format("%s/%s", formatTime(currentPos), formatTime(totalDuration)));
    }
  }

  /**
   * show end position of progress bar
   * this is necessary if media ends (halted) and no more time events will follow which would call showProgress
   */
  public void showProgressEndPosition(){
    progressSlider.setValue(progressSlider.getMax());
  }

  /**
   * (idea originally found on http://www.java2s.com/Code/Java/JavaFX/MediaPlayerforflvfile.htm)
   * convert
   *
   * @param duration of the media clip into the format hh:mm:ss or mm:ss if hh is zero
   * @return return ot as a String
   */
  private static String formatTime(Duration duration) {
    int i = (int) Math.floor(duration.toSeconds());

    int hours = i / (60 * 60);
    if (hours > 0) i = i - (hours * 60 * 60);

    int minutes = i / 60;
    if (minutes > 0) i = i - (minutes * 60);

    int seconds = i;

    if (hours > 0) {
      return String.format("%d:%02d:%02d", hours, minutes, seconds);
    } else {
      return String.format("%02d:%02d", minutes, seconds);
    }
  }

  //------------- Sync menuItems with PlayerControls

  /**
   * register a menuItem to reflect the playPauseButton's state also in that menuItem
   *
   * @param playPauseMenuItem the menu to be registered
   */
  public void bindPlayPauseMenuItem(MenuItem playPauseMenuItem) {
    playPauseButton.pausedProperty().addListener((observable, oldValue, newValue) -> syncPlayPauseMenuItemText(playPauseMenuItem));
  }
  /**
   * bind/unbind another otherPlayPauseButton to reflect the otherPlayPauseButton's state also in that other playerControl
   * used to Sync FullScreen-Mode
   *
   * @param otherPlayPauseButton the button to be snyced
   */
  public void bindBidirectionalPlayPauseButtons(PlayPauseButton otherPlayPauseButton) {
    playPauseButton.pausedProperty().bindBidirectional(otherPlayPauseButton.pausedProperty());
  }
  public void unbindBidirectionalPlayPauseButtons(PlayPauseButton otherPlayPauseButton){
    playPauseButton.pausedProperty().unbindBidirectional(otherPlayPauseButton.pausedProperty());
  }
  public PlayPauseButton getPlayPauseButton() {return playPauseButton;}

  /**
   * sync state of playPauseButton (or more exact the userHasPaused intend) with all registered menuItems
   * if this is the fullScreenContentViews PlayerControl send sync to primaryPlayerControl
   */
  protected void syncPlayPauseMenuItemText(MenuItem item) {
        if (playPauseButton.pausedProperty().get())
          item.setText(KissPhoto.language.getString("play")); //if paused menuItem can start play again
        else
          item.setText((KissPhoto.language.getString("pause")));  //if playing then the menuItem can pause
  }

  /**
   * register a menuItem to reflect the repeatButton's state also in that menuItem
   *
   * @param repeatMenuItem the menu item to be synced
   */
  public void bindBidirectionalRepeatMenuItem(CheckMenuItem repeatMenuItem) {
    repeatMenuItem.selectedProperty().bindBidirectional(repeatButton.repeatModeProperty());
  }
  /**
   * bind another otherRepeatButton to reflect the otherRepeatButton's state also in that other playerControl
   * used to Sync FullScreen-Mode
   *
   * @param otherRepeatButton the button to be synced
   */
  public void bindBidirectionalRepeatButtons(RepeatButton otherRepeatButton) {
    repeatButton.repeatModeProperty().bindBidirectional(otherRepeatButton.repeatModeProperty());
  }
  public void unbindBidirectionalRepeatButtons(RepeatButton otherRepeatButton){
    repeatButton.repeatModeProperty().unbindBidirectional(otherRepeatButton.repeatModeProperty());
  }
  public RepeatButton getRepeatButton() {return repeatButton;}

  /**
   * register a menuItem to reflect the playListModeButton's state also in that menuItem
   *
   * @param playlistModeMenuItem the menu Item to by synced
   */
  public void bindBidirectionalPlaylistModeMenuItem(CheckMenuItem playlistModeMenuItem) {
    playlistModeMenuItem.selectedProperty().bindBidirectional(playlistButton.playListModeProperty());
  }
  /**
   * bind another otherPlaylistButton to reflect the otherPlaylistButton's state also in that other playerControl
   * used to Sync FullScreen-Mode
   *
   * @param otherPlaylistButton the button to be synced
   */
  public void bindBidirectionalPlaylistModeButtons(PlayListButton otherPlaylistButton) {
    playlistButton.playListModeProperty().bindBidirectional(otherPlaylistButton.playListModeProperty());
  }
  public void unbindBidirectionalPlaylistModeButtons(PlayListButton otherPlaylistButton) {
    playlistButton.playListModeProperty().unbindBidirectional(otherPlaylistButton.playListModeProperty());
  }
  public PlayListButton getPlaylistButton() {return playlistButton;}

}
