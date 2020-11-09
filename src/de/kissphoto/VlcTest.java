package de.kissphoto;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

public class VlcTest extends Application {
  private final MediaPlayerFactory mediaPlayerFactory;

  private final EmbeddedMediaPlayer embeddedMediaPlayer;

  private ImageView videoImageView;   //ImageView = Videoanzeige = Ziel für die Pixel-Buffer-Kopieraktionen

  public VlcTest() {
    mediaPlayerFactory = new MediaPlayerFactory();
    embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
    embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      @Override
      public void playing(MediaPlayer mediaPlayer) {
      }

      @Override
      public void paused(MediaPlayer mediaPlayer) {
      }

      @Override
      public void stopped(MediaPlayer mediaPlayer) {
      }

      @Override
      public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
      }
    });
  }

  @Override
  public void init() {
    this.videoImageView = new ImageView();
    this.videoImageView.setPreserveRatio(true);

    embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(this.videoImageView));
  }

  @Override
  public final void start(Stage primaryStage) throws Exception {

    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: black;");

    videoImageView.fitWidthProperty().bind(root.widthProperty());
    videoImageView.fitHeightProperty().bind(root.heightProperty());

    root.setCenter(videoImageView);

    Scene scene = new Scene(root, 1200, 675, Color.BLACK);
    primaryStage.setTitle("vlcj Test using JavaFX");
    primaryStage.setScene(scene);
    primaryStage.show();

    embeddedMediaPlayer.controls().setPosition(0.4f);

//    embeddedMediaPlayer.media().play("test\\01-01 Nur noch kurz die Welt retten (Live Video).mp4");
    File f = new File("D:\\_Medien\\Video\\Video\\Tim Bendzko\\02-07 Wenn Worte meine Sprache wären (Musikvideo).mp4");
    System.out.println(f.toString());
    System.out.println(f.toURI().toString());
//    embeddedMediaPlayer.media().play(f.toURI().toString());
    embeddedMediaPlayer.media().play(f.toString());

  }

  @Override
  public final void stop() {
    embeddedMediaPlayer.controls().stop();
    embeddedMediaPlayer.release();
    mediaPlayerFactory.release();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
