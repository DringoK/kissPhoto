package dringo.kissPhoto.view.mediaViewers;

import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.PlayableFile;
import dringo.kissPhoto.view.MediaContentView;
import dringo.kissPhoto.view.viewerHelpers.PlayerControlPanel;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.filefilters.AudioFileFilter;
import uk.co.caprica.vlcj.filefilters.VideoFileFilter;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;

import static javafx.scene.media.MediaPlayer.Status;
import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for movie clips using VLCJ from Mark Lee/Caprica Software Limited which again uses a previously installed VLC-Player's dll .
 * By kissPhoto movie clips are treated like photos that move ("like newspaper photos in harry potter")
 * <ul>
 * <li>VLCJ fast high quality rendering
 * <li>zooming in/out/100%/fit
 * <li>full screen support
 * <li>displaying filename/date in picture
 * <li>keyboard support (next/prev, zoom, fullscreen)
 * <li>player control over main menu, contextMenu, control Panel und keyboard
 * </ul>
 *
 * @author Dringo
 * @version 2021-09-05 finished event updates progress to reflect end of media
 * @version 2021-01-16 workaround for changing media if "paused" (vlc will crash)
 * @version 2021-01-08 resetPlayer() reworked to avoid blocking during saveFolder()
 * @version 2020-11-08 bugfixing
 * @since 2020-10-25
 */
public class PlayerViewerVLCJ extends PlayerViewer {
  //treat the VLCJ-mediaPlayer's status compatible to FX-mediaPlayer: use the FX-constants
  protected EmbeddedMediaPlayer mediaPlayer = null;
  protected ImageView mediaImageView;

  javafx.scene.media.MediaPlayer.Status playerStatus = javafx.scene.media.MediaPlayer.Status.UNKNOWN;
  boolean wasReset = false; //after calling resetPlayer() e.g. skipToNextOnAutoPlay() should not be used
  boolean repeatTrackWhenStopped = false; //flag set in rewindAndPlayWhenFinished to trigger restart in STOPPED-Event

  private static NativeDiscovery nativeDiscovery; //only search the path to libvlc.dll once for all instances
  private static MediaPlayerFactory mediaPlayerFactory = null; //prevent garbage collection by making also the factory a member (see vlcj docu)

  private static boolean vlcAvailable;
  private static final String requiredVLCVersion = LibVlcVersion.requiredVersion.version();
  private static String currentVLCVersion = null;
  private final PlayerControlPanel playerControlPanel;

  /**
   * Determine if a given file could be played with this PlayerViewer
   *
   * @param file the file to be investigated
   * @return true if playable
   */
  public static boolean willAccept(Path file) {
    File f = file.toFile();
    return VideoFileFilter.INSTANCE.accept(f) || AudioFileFilter.INSTANCE.accept(f);
  }

  /**
   * constructor to initialize the viewer
   */
  public PlayerViewerVLCJ(final MediaContentView contentView) {

    super(contentView);   //mediaContentView of father class is now = contentView
    //binding is not necessary when placed in a StackPane (mediaStackPane is a StackPane)
    //prefHeightProperty().bind(mediaContentView.getMediaStackPaneHeightProperty());
    //prefWidthProperty().bind(mediaContentView.getMediaStackPaneWidthProperty());

    //temp handle
    playerControlPanel = (PlayerControlPanel) viewerControlPanel;

    //try to initialize VLCJ...find VLC..but only once
    if (nativeDiscovery == null) {
      vlcAvailable = false;
      nativeDiscovery = new NativeDiscovery();
      nativeDiscovery.discover();
    }

    if (nativeDiscovery.discoveredPath() != null)  //if successful
      try {
        if (currentVLCVersion == null) {  //only read this once (static)
          LibVlcVersion version = new LibVlcVersion();
          currentVLCVersion = version.getVersion().version();
        }

        if (mediaPlayerFactory == null) mediaPlayerFactory = new MediaPlayerFactory();  //only build it once (static)

        //initialize all the rest for kissPhoto
        mediaImageView = new ImageView(); //with VLCJ an ImageView is used for rendering ie. to copy Pixels to
        mediaImageView.setPreserveRatio(true);

        buildMediaPlayer();

        vlcAvailable = true;  //only when the mediaPlayer was initialized successfully vlc is available

        //note: playerControlPanel defined and initialized in PlayerViewer (fatherclass)
        getChildren().addAll(mediaImageView, playerControlPanel);

        mediaImageView.fitHeightProperty().bind(prefHeightProperty());
        mediaImageView.fitWidthProperty().bind(prefWidthProperty());

        setFocusTraversable(true);

        initPlayerContextMenu();
        addContextMenuItems();
        installContextMenu();

      } catch (Exception e) {
        vlcAvailable = false;   //MovieViewerVLCJ is not usable e.g. if vlc is not installed on the system :-(
        if (mediaPlayerFactory != null) mediaPlayerFactory.release();
        mediaPlayerFactory = null;
      }
    //System.out.println("vlcAvailable=" + vlcAvailable);
    //System.out.println("PlayerViewerVLCJ.constructor requiredVLCVersion =" +requiredVLCVersion );
    //System.out.println("PlayerViewerVLCJ.constructor currentVLCVersion =" +currentVLCVersion );

  }

  private void buildMediaPlayer() {
    mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
    registerEventsForMediaPlayer();

    //connect vlcj callback to the mediaView
    mediaPlayer.videoSurface().set(videoSurfaceForImageView(this.mediaImageView));
  }

  /**
   * as soon as the vlcj's mediaPlayer is built register the media event listeners
   */
  private void registerEventsForMediaPlayer() {
    mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      //do not call vlcj methods within vlcj events (see docu in vlcj)
      //use Platform.runLater except for stop(), play(), pause() which are asynchronous
      //use Platform.runLater also for GUI access

      @Override
      public void mediaPlayerReady(MediaPlayer mediaPlayer) {
        Platform.runLater(() -> {
          //show progress as soon as totalDuration is available
          playerControlPanel.setSliderScaling(getTotalDuration());
          playerControlPanel.showProgress(Duration.ZERO);
        });
      }

      @Override
      public void playing(MediaPlayer mediaPlayer) {
        Platform.runLater(() -> {
          playerStatus = Status.PLAYING;
          finished = false;
        });
      }

      @Override
      public void paused(MediaPlayer mediaPlayer) {
        Platform.runLater(() -> playerStatus = Status.PAUSED);
      }

      @Override
      public void stopped(MediaPlayer mediaPlayer) {
        Platform.runLater(() -> {
          if (wasReset)
            playerStatus = Status.STALLED;
          else {
            playerStatus = Status.STOPPED;

            if (repeatTrackWhenStopped) {     //see rewindAndPlayWhenFinished()
              play();
              repeatTrackWhenStopped = false;
            }
          }
        });
      }

      @Override
      public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
        Platform.runLater(() -> playerControlPanel.showProgress(new Duration(newTime)));
      }

      @Override
      public void finished(MediaPlayer mediaPlayer) {
        Platform.runLater(() -> {
          finished = true;
          playerControlPanel.showProgressEndPosition(); //indicate "completely played" because no more timeChaned-Events will follow
          if (!wasReset) mediaContentView.showNextOrRepeatMedia();
        });
      }

      @Override
      public void error(MediaPlayer mediaPlayer) {
        //vlcj finds out that a media is not playable asynchronously. That's why we have to switch to the according viewer as soon as we find out that it is not compatible
        Platform.runLater(() -> {
          wasReset = true;
          mediaContentView.showPlayerError();
        });
      }
    });
  }

  public javafx.scene.media.MediaPlayer.Status getStatus() {
    return playerStatus;
  }


  /**
   * put the media (movie) into the MovieViewer and play it if "playing" was active before or pause it if not
   *
   * @param mediaFile    the mediaFile containing the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the movie is loaded/visible
   */
  @Override
  public boolean setMediaFileIfCompatible(MediaFile mediaFile, Duration seekPosition) {
    if (!(mediaFile instanceof PlayableFile)) { //instanceof includes test on null
      return false;
    }
    boolean compatible = true;

    try {
      if (((PlayerControlPanel) viewerControlPanel).isUserHasPaused()) {
        mediaPlayer.media().startPaused(mediaFile.getFileOnDisk().toFile().toString());
      }else {
        mediaPlayer.media().start(mediaFile.getFileOnDisk().toFile().toString()); //start() blocks until playing in contrast to play()
      }
      wasReset = false;
    } catch (Exception e) {
      e.printStackTrace();
      compatible = false;
    }

    if (seekPosition != null) seek(seekPosition);
    return compatible;
  }

  /**
   * reset the player: release all and build a new player (e.g. to avoid media file blocking during FileTableView.saveFolder())
   */
  public void resetPlayer() {
    if (!wasReset) { //prevent from double reset
      mediaPlayer.release(); //release the old one
      buildMediaPlayer();    //build a new one
      playerStatus = Status.STALLED;
      wasReset = true;
    }
  }

  /**
   * start player from the beginning to implement repeat track
   */
  @Override
  public void rewindAndPlayWhenFinished() {
    repeatTrackWhenStopped = true;  //after finished wait for being stopped and trigger restart there
    //bug in vlc(j): sometimes (seldom) "finished" is not followed by "stopped". manual/additional start() here does not change anything :-(
  }

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void play() {
    if (isMediaValid()) {
      wasReset = false;
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
        // No event is fired to update GUI because playerStatus=stopped when finished
        ((PlayerControlPanel) viewerControlPanel).showProgress(Duration.ZERO);
      }
      wasReset = false;
      finished = false;
      mediaPlayer.controls().pause(); //implementation in vlcj is asynchronous
    }
  }

  /**
   * stop, rewind
   * adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void stop() {
    if (isMediaValid()) {
      wasReset = false;
      mediaPlayer.controls().stop(); //implementation in vlcj is asynchronous
    }
  }

  /**
   * seek Position (Duration)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   *
   * @param newPos position to jump to. null is treated like Duration.ZERO (rewind)
   */
  public void seek(Duration newPos) {
    if (finished) {
      play(); // finished=false; //seeking results in not being at the end of the media any more
    }

    if (isMediaValid()) {
      if (newPos == null)
        mediaPlayer.controls().setTime(0, false);
      else
        mediaPlayer.controls().setTime((long) newPos.toMillis(), false); //true=fast positioning
    }
  }

  /**
   * get the current position of the media currently playing
   *
   * @return current position
   */
  public Duration getCurrentTime() {
    if (isMediaValid()) {
      return new Duration(mediaPlayer.status().time());
    } else
      return Duration.UNKNOWN;    //e.g. MovieViewerDummy does not have a mediaPlayer so there is no duration in this case
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

  protected boolean isMediaValid() {
    return mediaPlayer != null && mediaPlayer.media().isValid();
  }


  /**
   * release resources of VLC.dll
   */
  public void releaseAll() {
    if (vlcAvailable && isMediaValid()) {
      //stop();  //do not stop the player, because vlcj implemented it to be asynchronous. Therefore stop() would be executed after release()!
      mediaPlayer.release();
      mediaPlayerFactory.release();
    }
  }
  /**
   * could VLC be found and loaded?
   * @return true if vlc could be found and loaded in the minimum version on the system
  */

  public static boolean isVlcAvailable() {
    return vlcAvailable;
  }
  /**
   * required VLC Version
   * @return the minimum required VLC version or "" if it was not possible to determine it
   */
  public static String getRequiredVLCVersion() {
    if (requiredVLCVersion != null)
      return requiredVLCVersion;
    else
      return "";
  }


  /**
   * current VLC Version
   * @return the current VLC version or "" if vlc not available*/
  public static String getCurrentVLCVersion() {
    if (currentVLCVersion!=null)
      return currentVLCVersion;
    else
      return "";
  }

  //----------------------- Implement specific ZoomableViewer Interface ----------------------------

  @Override
  public Rectangle2D getViewport() {
    return mediaImageView.getViewport();
  }

  @Override
  public void setViewport(Rectangle2D value) {
    mediaImageView.setViewport(value);
  }

  @Override
  public double getMediaWidth() {
    if (mediaPlayer != null) {
      Dimension vd = mediaPlayer.video().videoDimension();
      if (vd != null)
        return vd.getWidth(); //<--------- this is the regular return value
    }

    return 0;  //default if something is null
  }

  @Override
  public double getMediaHeight() {
    if (mediaPlayer != null) {
      Dimension vd = mediaPlayer.video().videoDimension();
      if (vd != null)
        return vd.getHeight(); //<--------- this is the regular return value
    }

    return 0;  //default if something is null
  }

  @Override
  public double getFitWidth() {
    return mediaImageView.getFitWidth();
  }

  @Override
  public double getFitHeight() {
    return mediaImageView.getFitHeight();
  }
}
