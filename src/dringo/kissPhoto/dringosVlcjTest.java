package dringo.kissPhoto;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

/**
 * Dringos minimum Test for VLCJ with JavaFX (Bell-Soft's Full version 15.0.1)
 * VLC4.0.0 throws assertion error when
 * - changing file position while paused
 * - changing file while paused
 */

public class dringosVlcjTest extends Application {
  //Window
  Stage primaryStage;
  Group root;
  Scene scene;

  //Player
  EmbeddedMediaPlayer mediaPlayer;
  MediaPlayerFactory mediaPlayerFactory;
  ImageView imageView = new ImageView(); //Canvas to copy the vlc's output to
  Slider progressSlider;
  
  Duration totalDuration;
  boolean finished = false; //media has finished?
  boolean paused = false;
  
  

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) {
    primaryStage = stage;  //make this parameter value available in event Handler (see setOnClosedEvent)
    primaryStage.setTitle("Dringo's VLCJ5 + VLC4.0.0 Test: Drag Movie into Window to Load. Click to Pause. Slider to Move Position");
    root = new Group();
    scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);


    BorderPane pane = new BorderPane();
    pane.prefWidthProperty().bind(stage.widthProperty());
    pane.prefHeightProperty().bind(stage.heightProperty());
    //pane.setStyle("-fx-background-color: black;");

    progressSlider = createSlider();
    progressSlider.prefWidthProperty().bind(pane.prefWidthProperty());  //try to use as much space as possible of the controlArea for the progress Slider

    imageView.setPreserveRatio(true);
    imageView.fitWidthProperty().bind(pane.prefWidthProperty());
    imageView.fitHeightProperty().bind(pane.prefHeightProperty());

    pane.setCenter(imageView);
    pane.setTop(progressSlider);
    root.getChildren().add(pane);

    initVLCJPlayer();
    installDragDropHandlers(primaryStage);

    pane.setOnMouseClicked((mouseEvent)->{
      if (paused)
        play();
      else
        pause();
    });

    primaryStage.show();

  }

  @Override
  public final void stop() {              //JavaFX.Application.stop() not the player's ;-)
    //release all external resources e.g. VLC.dll
    if (mediaPlayer!=null) mediaPlayer.release();
    if (mediaPlayerFactory !=null) mediaPlayerFactory.release();
  }

  private void initVLCJPlayer() {
    NativeDiscovery nativeDiscovery = new NativeDiscovery();
    nativeDiscovery.discover();

    if (nativeDiscovery.discoveredPath() != null)  //if successful
      try {
        LibVlcVersion version = new LibVlcVersion();
        System.out.println("VLCJ-Event: VLC version found: " + version.getVersion().version());

        mediaPlayerFactory = new MediaPlayerFactory();  //only build it once (static)

        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        registerEventsForMediaPlayer();

        //connect vlcj callback to the mediaView
        mediaPlayer.videoSurface().set(videoSurfaceForImageView(this.imageView));

      } catch (Exception e) {
        if (mediaPlayerFactory != null) mediaPlayerFactory.release();
        mediaPlayerFactory = null;
      }
  }

  private void registerEventsForMediaPlayer() {
    mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      //do not call vlcj methods within vlcj events (see docu in vlcj)
      //use Platform.runLater except for stop(), play(), pause() which are asynchronous
      //use Platform.runLater also for GUI access

      @Override
      public void mediaPlayerReady(MediaPlayer mediaPlayer) {
        System.out.println("VLCJ-Event: mediaPlayerReady");

        Platform.runLater(()->{
          //show progress as soon as totalDuration is available
          setSliderScaling(getTotalDuration());
          showProgress(Duration.ZERO);
        });
    }

      @Override
      public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
        System.out.println("VLCJ-Event: PositionChanged");
      }

      @Override
      public void playing(MediaPlayer mediaPlayer) {
        System.out.println("VLCJ-Event: playing");
        paused = false;
      }

      @Override
      public void paused(MediaPlayer mediaPlayer) {
        System.out.println("VLCJ-Event: paused");
        paused = true;
      }

      @Override
      public void stopped(MediaPlayer mediaPlayer) {
        System.out.println("VLCJ-Event: stopped");
      }

      @Override
      public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
        System.out.println("VLCJ-Event: time Changed");
        Platform.runLater(() -> showProgress(new Duration(newTime)));
      }

      @Override
      public void finished(MediaPlayer mediaPlayer) {
        System.out.println("VLCJ-Event: finished");
        finished = true;
      }

      @Override
      public void error(MediaPlayer mediaPlayer) {
        System.out.println("VLCJ-Event: error");
      }
    });
  }

  /**
   * if mediaPlayer is null (currently no media file displayed) Duration(0) is returned
   *
   * @return the total length of the currently loaded media
   */
  public Duration getTotalDuration() {
    if (isMediaValid()) {
      assert mediaPlayer.media().info() != null;
      return new Duration(mediaPlayer.media().info().duration());
    } else
      return Duration.ZERO;
  }

  public void setSliderScaling(Duration totalDuration) {
    if ((totalDuration != null) && (totalDuration.toMillis() > 0)) {
      progressSlider.setMax(totalDuration.toMillis());
      progressSlider.setBlockIncrement(totalDuration.toMillis() / 10);
      this.totalDuration = totalDuration;
    } else {
      this.totalDuration = Duration.ZERO;
    }
  }
  
  protected boolean isMediaValid() {
    return mediaPlayer != null && mediaPlayer.media().isValid();
  }

  // update slider as video is progressing if totalDuration and currentPos are valid
  public void showProgress(Duration currentPos) {
    System.out.println("show Progress: " + currentPos + "/" + totalDuration);
    if (currentPos != null && totalDuration != null) {
      progressSlider.setValue(currentPos.toMillis());
    }
  }

  private void openFile(String filename){
    try {
      if (paused) {
        mediaPlayer.media().startPaused(filename);
      }else {
        mediaPlayer.media().start(filename); //start() blocks until playing in contrast to play()
      }
      System.out.println("File opened: " + filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * @param newPos position to jump to. null is treated like Duration.ZERO (rewind)
   */
  public void seek(Duration newPos) {
    if (finished) {
      play(); // finished=false; //seeking results in not being at the end of the media any more
    }
    if (isMediaValid()) {
      if (newPos == null)
        mediaPlayer.controls().setTime(0);
      else
        mediaPlayer.controls().setTime((long) newPos.toMillis());
    }
  }

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void play() {
    if (isMediaValid()) {
      finished = false;
      mediaPlayer.controls().play(); //implementation in vlcj is asynchronous
    }
  }

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void pause() {
    if (isMediaValid()) {
      if (finished) {
        //seek(new Duration(0.4)); //rewind not necessary because VLC is in status STOPPED.
        showProgress(Duration.ZERO);
      }
      finished = false;
      mediaPlayer.controls().pause(); //implementation in vlcj is asynchronous
    }
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
      seek(new Duration(pos));
      mouseEvent.consume();
    });

    //Slider has been dragged with mouse
    slider.valueProperty().addListener(ov -> {
      if (slider.isValueChanging()) {
        seek(new Duration(slider.getValue()));
      }
    });

    return slider;
  }

  private void installDragDropHandlers(Stage primaryStage) {
    primaryStage.getScene().setOnDragOver(dragEvent -> {
      Dragboard db = dragEvent.getDragboard();
      if (db.hasFiles()) {
        dragEvent.acceptTransferModes(TransferMode.COPY);
      } else {
        dragEvent.consume();
      }
    });

    primaryStage.getScene().setOnDragDropped(dragEvent -> {
      Dragboard db = dragEvent.getDragboard();
      boolean success = false;
      if (db.hasFiles()) {
        success = true;
        //load first file only
        openFile(db.getFiles().get(0).getAbsolutePath());
      }
      dragEvent.setDropCompleted(success);
      dragEvent.consume();
    });
  }

}
