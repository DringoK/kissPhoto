package de.kissphoto.view.helper;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a MediaPlayer's controls area
 * <ul>
 * <li>a play/pause button
 * <li>a Fader for jumping to a certain point of the media
 * <li>text indicating time/total time
 * </ul>
 * <ul>
 * <li>to be connected to a JavaFX MediaPlayer
 * <li>fades in if mouse moves over Player Area
 * <li>supports play/pause/progress bar for browsing the media
 * </ul>
 * <p/>
 * It is a transparent Pane which's size is bound to a playerViewer
 * with all controls at the bottom of it
 *
 * @author Dr. Ingo Kreuz
 * @version  2014-07-18
 * @version 2020-11-11 bugfixing event handling + Keep it visible while mouse is in control area
 * @version 2020-10-25 using new interface of PlayerViewer avoiding direct access to MediaPlayer (because FX and VLC implementation are different)
 * @version 2017-10-21 play/pause-symbol synchronized with player state
 * @version 2017-10-08 longer showing the Player, higher player
 */

public class PlayerControls extends Pane {

  private BorderPane controlArea;
  private PlayerViewer playerViewer;
  private PlayPauseButton playPauseButton;
  private Slider progressSlider;
  private Text progressText;
  private Duration totalDuration;   //of the current media in playerViewer, used for scrollbar scaling and progressText
  private boolean wasPausedBeforeMousePressed = false;

  private PlayerControlsHiderThread playerControlsHiderThread;


  /**
   * @param viewer to show in
   * @constructor create a player control and connect it to a MediaPlayer
   */
  PlayerControls(PlayerViewer viewer) {
    playerViewer = viewer;

    controlArea = new BorderPane();

    controlArea.setStyle("-fx-background-color: black;");
    controlArea.setOpacity(0.5);
    controlArea.setPrefHeight(60);
    controlArea.prefWidthProperty().bind(widthProperty());

    Insets insets = new Insets(12, 12, 12, 12); //margin of the control elements
    //----------- build controls ---------------------
    playPauseButton = new PlayPauseButton(this, 30, 30);
    BorderPane.setAlignment(playPauseButton, Pos.CENTER_LEFT);
    BorderPane.setMargin(playPauseButton, insets);
    progressSlider = createSlider();
    BorderPane.setAlignment(progressSlider, Pos.CENTER);
    progressText = new Text();
    progressText.setFill(Color.WHITE);
    BorderPane.setAlignment(progressText, Pos.CENTER_RIGHT);
    BorderPane.setMargin(progressText, insets);

    controlArea.setLeft(playPauseButton);
    controlArea.setRight(progressText);
    controlArea.setCenter(progressSlider);

    controlArea.setCursor(Cursor.DEFAULT);
    getChildren().add(controlArea);

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


    // pause media and swap button with play button
    playPauseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent mouseEvent) {
        if (playPauseButton.isPaused()) {
          playerViewer.play();
        }else {
          playerViewer.pause();
        }
        mouseEvent.consume();
      }
    });

    //------------------ finally start the thread which will auto-hide this PlayerControls instance
    playerControlsHiderThread = new PlayerControlsHiderThread(this);
    playerControlsHiderThread.setShowTimeInMillis(2500);
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
    final Slider slider = new Slider();
    slider.setSnapToTicks(false);

    //click needs initiate player.seek which again moves the slider to the clicked position
    slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        mouseEvent.consume();
        //max -> width
        //pos -> x
        double pos = slider.getMax() / slider.getWidth() * mouseEvent.getX();
        playerViewer.seek(new Duration(pos));
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

  /**
   * set the visible play pause button in Player controls to the according shape
   *
   * @param newValue status of the player
   */
  public void setPlayPausedButtonForPlayerStatus(MediaPlayer.Status newValue) {
    switch (newValue) {
      case READY:
      case HALTED:
      case STOPPED:
      case PAUSED:
        playPauseButton.setVisible(true);
        playPauseButton.setPaused(true); //if paused then show play
        break;
      case PLAYING:
        playPauseButton.setVisible(true);
        playPauseButton.setPaused(false); //if playing then show pause
        break;
      case STALLED:
      case UNKNOWN:
      case DISPOSED:
      default:
        playPauseButton.setVisible(false);
        break;
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
