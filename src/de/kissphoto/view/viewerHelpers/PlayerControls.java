package de.kissphoto.view.viewerHelpers;

import de.kissphoto.KissPhoto;
import de.kissphoto.view.viewerHelpers.viewerButtons.BurgerMenuButton;
import de.kissphoto.view.viewerHelpers.viewerButtons.PlayListButton;
import de.kissphoto.view.viewerHelpers.viewerButtons.PlayPauseButton;
import de.kissphoto.view.viewerHelpers.viewerButtons.RepeatButton;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static de.kissphoto.KissPhoto.language;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a MediaPlayer's controls area
 * <ul>
 * <li>a play/pause button
 * <li>a progress bar for jumping to a certain point of the media
 * <li>text indicating time/total time
 * <li>option buttons: Repeat and Playlist mode
 * </ul>
 * <br>
 * <ul>
 * <li>to be connected to a JavaFX MediaPlayer
 * <li>fades in if mouse moves over Player Area
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
 * @author Dr. Ingo Kreuz
 * @since  2014-07-18
 * @version 2020-11-22 bug fixing event handling + Keep it visible while mouse is in control area, added burger menu + repeat icon + Playlist icon
 * @version 2020-10-25 using new interface of PlayerViewer avoiding direct access to MediaPlayer (because FX and VLC implementation are different)
 * @version 2017-10-21 play/pause-symbol synchronized with player state
 * @version 2017-10-08 longer showing the Player, higher player
 */

public class PlayerControls extends VBox {
  private boolean userHasPaused=false;  //reflects the intend of the user, PlayButton reflects the status of the player

  private HBox controlArea;
  private HBox optionArea;
  private PlayerViewer playerViewer;

  private PlayPauseButton playPauseButton;
  private Slider progressSlider;
  private Text progressText;
  private Duration totalDuration;   //of the current media in playerViewer, used for scrollbar scaling and progressText
  private BurgerMenuButton burgerMenuButton;

  private PlayListButton playListButton;
  private RepeatButton repeatButton;

  private PlayerControlsHiderThread playerControlsHiderThread;

  //------------- IDs for GlobalSettings-File
  private static final String PAUSED_ID = "playerWasPaused";
  private static final String REPEAT_MODE_ID = "playerRepeatMode";
  private static final String PLAYLIST_MODE_ID = "playerPlayListMode";

  //color and padding for all panels
  public static final Color ICON_COLOR = new Color(1, 1, 1, .90);
  public static final Color BACKGROUND_COLOR = new Color(0, 0, 0, .55);
  private static final double PADDING = 12.0; //borders

  //control Panel sizes
  private static final double CONTROL_AREA_HEIGHT = 35.0;
  private static final double BUTTON_SIZE = 30.0;  //width and height

  //option Panel sizes
  private static final double OPTION_AREA_HEIGHT = 20;
  private static final double OPTIONS_SIZE = 17;

  //-------------- Sync with Menues
  ObservableList<MenuItem> playPauseMenuItemList = FXCollections.observableArrayList();
  ObservableList<CheckMenuItem> repeatMenuItemList = FXCollections.observableArrayList();
  ObservableList<CheckMenuItem> playListModeMenuItemList = FXCollections.observableArrayList();

  /**
   * @param viewer to show in
   * @constructor create a player control and connect it to a MediaPlayer
   */
  PlayerControls(PlayerViewer viewer) {
    //setStyle("-fx-background-color: blue;");
    //setOpacity(0.5);

    playerViewer = viewer;

    createControlArea();
    createOptionArea();

    //default values (not using set...Mode(), because they overwrite the settings)
    playPauseButton.setPaused(false); //if playing then show pause
    repeatButton.setButtonValue(false, true);
    playListButton.setButtonValue(true);
    //then try to load the last values
    restoreLastPlayerStatus();

    this.getChildren().addAll(controlArea, optionArea);

    //------------------- handle events ---------------------------------

    //------- showing/hiding controlArea using
    setOnMouseMoved(mouseEvent -> {
      resetThreadAndShow(); //reset show time
      mouseEvent.consume();   //if no control in the playerControl has consumed the event yet, do not let go through, preventing playerViewer to interfere with play/pause there
    });

    controlArea.setOnMouseEntered(mouseEvent->{
      setVisible(true);
      playerControlsHiderThread.pause();

    });
    controlArea.setOnMouseExited(mouseEvent->{
      playerControlsHiderThread.resume();
    });

    optionArea.setOnMouseEntered(mouseEvent->{
      setVisible(true);
      playerControlsHiderThread.pause();

    });
    optionArea.setOnMouseExited(mouseEvent->{
      playerControlsHiderThread.resume();
    });


    //toggle play/pause to user's intend
    playPauseButton.setOnMousePressed(mouseEvent -> {
      togglePlayPause();
      mouseEvent.consume();
    });

    //------------------ finally start the thread which will auto-hide this PlayerControls instance
    playerControlsHiderThread = new PlayerControlsHiderThread(this);
    playerControlsHiderThread.setShowTimeInMillis(2500);
  }

  private void createControlArea() {
    controlArea = new HBox();

    controlArea.setStyle("-fx-background-color: black;");
    controlArea.setOpacity(0.5);
    controlArea.setPrefHeight(CONTROL_AREA_HEIGHT);
    controlArea.prefWidthProperty().bind(widthProperty());
    controlArea.setAlignment(Pos.CENTER_RIGHT);
    controlArea.setPadding(new Insets(0, PADDING, 0,PADDING)); //only left/right, because top/bottom regulated by Pos.CENTER

    //----------- build controls ---------------------
    playPauseButton = new PlayPauseButton(BUTTON_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    burgerMenuButton = new BurgerMenuButton(BUTTON_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    burgerMenuButton.setOnMouseClicked((mouseEvent)->{
      playerViewer.contextMenu.show(playerViewer, mouseEvent.getScreenX(),mouseEvent.getScreenY());
      mouseEvent.consume();
    });


    progressText = new Text();
    progressText.setFill(Color.WHITE);

    Rectangle spaceBeforeBurgerMenu = new Rectangle(BUTTON_SIZE/2, BUTTON_SIZE, BACKGROUND_COLOR);

    progressSlider = createSlider();
    progressSlider.prefWidthProperty().bind(controlArea.prefWidthProperty());  //try to use as much space as possible of the controlArea for the progress Slider

    controlArea.getChildren().addAll(playPauseButton, progressSlider, progressText, spaceBeforeBurgerMenu, burgerMenuButton);

    controlArea.setCursor(Cursor.DEFAULT);
  }
  private void createOptionArea() {
    optionArea = new HBox();

    optionArea.setStyle("-fx-background-color: black;");
    optionArea.setOpacity(0.5);
    optionArea.setPrefHeight(OPTION_AREA_HEIGHT);
    optionArea.prefWidthProperty().bind(widthProperty());
    optionArea.setAlignment(Pos.CENTER);
    optionArea.setPadding(new Insets(0, PADDING, 0,PADDING)); //only left/right, because top/bottom regulated by Pos.CENTER
    optionArea.setSpacing(PADDING/2);

    //----------- build controls ---------------------
    playListButton = new PlayListButton(OPTIONS_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    playListButton.setOnMouseClicked((mouseEvent)->{
      setPlayListMode(!isPlayListMode());
      mouseEvent.consume();
    });

    repeatButton = new RepeatButton(OPTIONS_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    repeatButton.setOnMouseClicked((mouseEvent)->{
      setRepeatMode(!isRepeatMode());
      mouseEvent.consume();
    });

    optionArea.getChildren().addAll(playListButton, repeatButton);

    optionArea.setCursor(Cursor.DEFAULT);
  }

  /**
   * Restore last Option-Settings and Play-Status from Global-Settings properties file
   * assumes:
   * - globalSettings already loaded
   */
  private void restoreLastPlayerStatus() {
    try {
      setPlayListMode(Boolean.parseBoolean(KissPhoto.globalSettings.getProperty(PLAYLIST_MODE_ID)) );
    } catch (Exception ignored) {}//if not loadable keep default value)
    syncPlayListModeMenuItems();

    try {
      setRepeatMode(Boolean.parseBoolean(KissPhoto.globalSettings.getProperty(REPEAT_MODE_ID)) );
    } catch (Exception ignored) {}//if not loadable keep default value)
    syncRepeatMenuItems();

    try {
      userHasPaused = Boolean.parseBoolean(KissPhoto.globalSettings.getProperty(PAUSED_ID)) ;
    } catch (Exception ignored) {}//if not loadable keep default value)
    syncPlayPauseMenuItems();
  }
    /**
     * report button status
     * @return true if playList Mode is active
     */
  public boolean isPlayListMode(){
    return playListButton.getButtonValue();
  }
  /**
   * report button status
   * @return true if Repeat Mode is active
   */
  public boolean isRepeatMode(){
    return repeatButton.getButtonValue();
  }

  /**
   * user's intend
   * @return true if user intended to pause media
   */
  public boolean isUserHasPaused(){
    return userHasPaused;
  }

  /**
   * exactly execute what keyboard "P" does: if paused then play and vice versa..to user's intend
   */
  public void togglePlayPause(){
    if (userHasPaused) //start from the controls status and synchronize the users intend in play/pause if necessary
      play();
    else
      pause();
    //note: the button synchronizes with the status of the player when the new status is reported as an event
  }

  /**
   * user intends to start media
   */
  public void play(){
    playerViewer.play();

    userHasPaused=false;
    syncPlayPauseMenuItems();
    KissPhoto.globalSettings.setProperty(PAUSED_ID,Boolean.toString(userHasPaused));

    //note: the button synchronizes with the status of the player when the new status is reported as an event
  }

  /**
   * user intends to pause media
   */
  public void pause(){
    playerViewer.pause();
    userHasPaused=true;
    syncPlayPauseMenuItems();
    KissPhoto.globalSettings.setProperty(PAUSED_ID,Boolean.toString(userHasPaused));

    //if at the end of media the player might still report PLAYING (e.g.FX' media player)
    //so force to show pause state for button
    playPauseButton.setPaused(true);

    //note: the button synchronizes with the status of the player when the new status is reported as an event
  }

  /**
   * restart media by seeking position 0.0
   * don't change paused state
   */
  public void rewind(){
    playerViewer.seek(Duration.ZERO);
  }
  /**
   * set button status
   * @param newValue  true if playList Mode shall be active
   */
  public void setPlayListMode(boolean newValue){
    playListButton.setButtonValue(newValue);
    repeatButton.setButtonValue(isRepeatMode(),newValue);//adjust the tooltip repeat track/list

    syncPlayListModeMenuItems();
    KissPhoto.globalSettings.setProperty(PLAYLIST_MODE_ID, Boolean.toString(newValue));
  }
  /**
   * set button status
   * @return true if playList Mode is active
   */
  public void setRepeatMode(boolean newValue){
    repeatButton.setButtonValue(newValue,isPlayListMode()); //don't change playlist mode

    syncRepeatMenuItems();
    KissPhoto.globalSettings.setProperty(REPEAT_MODE_ID, Boolean.toString(newValue));
  }

  public void cleanUp() {
    playerControlsHiderThread.endThread();
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
    slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        //max -> width
        //pos -> x
        double pos = slider.getMax() / slider.getWidth() * mouseEvent.getX();
        playerViewer.seek(new Duration(pos));
        mouseEvent.consume();
      }
    });

    //Slider has been dragged with mouse
    slider.valueProperty().addListener(new InvalidationListener() {
      public void invalidated(Observable ov) {
        if (slider.isValueChanging()) {
          playerViewer.seek(new Duration(slider.getValue()));
        }
      }
    });

    return slider;
  }

  /**
   * if new Media was set to the player then this method should be called "onReady" of the player
   * to update the scale of the slider
   */
  public void setSliderScaling(Duration totalDuration) {
    if ((totalDuration != null) && (totalDuration.toMillis()>0)) {
      progressSlider.setMax(totalDuration.toMillis());
      progressSlider.setBlockIncrement(totalDuration.toMillis() / 10);
      this.totalDuration = totalDuration;
    }else{
      this.totalDuration = Duration.ZERO;
    }
  }

  // update slider as video is progressing if totalDuration and currentPos are valid
  public void showProgress(Duration currentPos) {
    if (currentPos!=null && totalDuration!=null) {
      progressSlider.setValue(currentPos.toMillis());
      progressText.setText(String.format("%s/%s", formatTime(currentPos), formatTime(totalDuration)));
    }
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
   * @param playPauseMenuItem
   */
  public void registerPlayPauseMenuItem(MenuItem playPauseMenuItem){
    playPauseMenuItemList.add(playPauseMenuItem);
  }
  /**
   * sync state of playPauseButton (or more exact the userHasPaused intend) with all registered menuItems
   */
  private void syncPlayPauseMenuItems(){
    for (MenuItem item: playPauseMenuItemList){ //sync all registered menuItems
      if (userHasPaused)
        item.setText(language.getString("play")); //if paused menuItem can start play again
      else
        item.setText((language.getString("pause")));  //if playing then the menuItem can pause

    }
    playPauseButton.setPaused(userHasPaused);
  }

  /**
   * register a menuItem to reflect the repeatButton's state also in that menuItem
   * @param repeatMenuItem
   */
  public void registerRepeatMenuItem(CheckMenuItem repeatMenuItem){
    repeatMenuItemList.add(repeatMenuItem);
  }
  /**
   * sync state of repeatButton with all registered menuItems
   */
  private void syncRepeatMenuItems(){
    for (CheckMenuItem item: repeatMenuItemList){ //sync all registered menuItems
      item.setSelected(isRepeatMode());
    }
  }
  /**
   * register a menuItem to reflect the playListModeButton's state also in that menuItem
   * @param playListModeMenuItem
   */
  public void registerPlayListModeMenuItem(CheckMenuItem playListModeMenuItem){
    playListModeMenuItemList.add(playListModeMenuItem);
  }
  /**
   * sync state of playListModeButton with all registered menuItems
   */
  private void syncPlayListModeMenuItems(){
    for (CheckMenuItem item: playListModeMenuItemList){ //sync all registered menuItems
      item.setSelected(isPlayListMode());
    }
  }

  /**
   * show the PlayerControls for some time using the playerControlsHiderThread to hide it automatically after some time
   */
  void resetThreadAndShow() {
    playerControlsHiderThread.showPlayerControls();
  }

  void hide() {
    playerControlsHiderThread.hidePlayerControlsImmediately();
  }
}
