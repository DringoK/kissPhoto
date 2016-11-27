package de.kissphoto.view.mediaViewers.helper;

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
 * @date 2014-07-18
 * @modified:
 */

public class PlayerControls extends Pane {

  private BorderPane controlArea;
  private PlayerViewer playerViewer;
  private PlayPauseButton playPauseButton;
  private Slider progressSlider;
  private Text progressText;
  private Duration duration;   //of the current media in playerViewer, used for scrollbar scaling and progressText
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
    controlArea.setPrefHeight(40);
    controlArea.prefWidthProperty().bind(widthProperty());

    Insets insets = new Insets(12, 12, 12, 12); //margin of the control elements
    //----------- build controls ---------------------
    playPauseButton = new PlayPauseButton(this, 30, 30);
    BorderPane.setAlignment(playPauseButton, Pos.CENTER_LEFT);
    BorderPane.setMargin(playPauseButton, insets);
    progressSlider = createSlider();
    BorderPane.setAlignment(progressSlider, Pos.CENTER);
    progressText = createProgressText();
    BorderPane.setAlignment(progressText, Pos.CENTER_RIGHT);
    BorderPane.setMargin(progressText, insets);

    controlArea.setLeft(playPauseButton);
    controlArea.setRight(progressText);
    controlArea.setCenter(progressSlider);

    controlArea.setCursor(Cursor.DEFAULT);
    getChildren().add(controlArea);

    //------------------- process events ---------------------------------

    // pause media and swap button with play button
    playPauseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent me) {
        playPauseButton.togglePaused();
        if (playPauseButton.isPaused())
          playerViewer.pause();
        else
          playerViewer.play();
      }
    });

    //------------------ finally start the thread which will auto-hide this PlayerControls instance
    playerControlsHiderThread = new PlayerControlsHiderThread(this);
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


    slider.setOnMousePressed(new EventHandler<MouseEvent>() {
      // pause temporarily so that the slider's value listener can react on the mouse click. OnMouseReleased will resume
      @Override
      public void handle(MouseEvent mouseEvent) {
        wasPausedBeforeMousePressed = playerViewer.isPaused();
        playerViewer.pause();

        //do not consume, so the value listener of the slider is firing next
      }
    });

    slider.setOnMouseReleased(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (!wasPausedBeforeMousePressed) playerViewer.play();
        wasPausedBeforeMousePressed = false;
      }
    });

    slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        //max -> width
        //pos -> x
        double pos = slider.getMax() / slider.getWidth() * mouseEvent.getX();
        playerViewer.getMediaPlayer().seek(new Duration(pos));
      }
    });

    slider.valueProperty().addListener(new InvalidationListener() {
      public void invalidated(Observable ov) {
        if (slider.isValueChanging()) {
          playerViewer.getMediaPlayer().seek(new Duration(slider.getValue()));
        }
      }
    });

    return slider;
  }

  private Text createProgressText() {
    Text text = new Text();
    text.setFill(Color.WHITE);
    return text;
  }

  /**
   * if new Media was set to the player then this method should be called "onReady" of the player
   * to update the scale of the slider
   */
  public void setSliderScaling() {
    duration = playerViewer.getMediaPlayer().getTotalDuration();
    progressSlider.setMax(duration.toMillis());
    progressSlider.setBlockIncrement(duration.toMillis() / 10);
  }

  // update slider as video is progressing
  public void showProgress(Duration currentPos) {
    progressSlider.setValue(currentPos.toMillis());
    progressText.setText(String.format("%s/%s", formatTime(currentPos), formatTime(duration)));
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
   * show the PlayerControls for some time using the playerControlsHiderThread to hide it automatically after some time
   */
  void show() {
    playerControlsHiderThread.showPlayerControls();
  }

  void hide() {
    playerControlsHiderThread.hidePlayerControlsImmediately();
  }
}
