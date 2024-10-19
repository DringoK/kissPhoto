package dringo.kissPhoto.view.mediaViewers;

import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.PlayableFile;
import dringo.kissPhoto.view.MediaContentView;
import dringo.kissPhoto.view.viewerHelpers.PlayerControlPanel;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.util.Objects;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 * <p>
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for movie clips using JavaFX's media player component
 * By kissPhoto they are treated like photos that move ("like in harry potter newspaper")
 * <ul>
 * <li>JavaFx fast high quality rendering
 * <li>zooming in/out/100%/fit
 * <li>full screen support
 * <li>displaying filename/date in picture
 * <li>keyboard support (next/prev, zoom, fullscreen)
 * <li>player control over main menu, contextMenu und keyboard
 * <li>volume control not yet implemented
 * </ul>
 *
 * @author Dringo
 * @since 2014-06-09
 * @version 2024-10-06: Retry support to fix MediaPlayer issue described e.g. in https://stackoverflow.com/questions/74247333/flaky-javafx-error-media-invalid-mediaexception
 * @version 2020-12-20: MediaFile-Type and cache content is now controlled by the viewers: only the know what they accept and what should be cached to speed up viewing
 * @version 2020-10-25: JavaFX MediaPlayer renamed from MovieViewer to MovieViewerFX, to enable MovieViewerVLCJ. Pause/End of File behavior improved
 * @version 2017-10-21: Event-Handling (mouse/keyboard) centralized, so that viewport events and player viewer events can be handled
 * @version 2016-11-06: ViewportZoomerMover extracted for all viewport zooming and moving operations (now identical to PhotoViewer's)
 */
public class PlayerViewerFX extends PlayerViewer {
  protected final MediaView mediaView;
  private Duration seekPosition;


  /**
   * constructor to initialize the viewer
   */
  public PlayerViewerFX(final MediaContentView contentView) {
    super(contentView);   //mediaContentView of father class is now = contentView

    mediaView = new MediaView();
    mediaView.setPreserveRatio(true);

    //note: playerControlPanel defined and initialized in PlayerViewer (father class)
    getChildren().addAll(mediaView, viewerControlPanel);

    mediaView.fitHeightProperty().bind(prefHeightProperty());
    mediaView.fitWidthProperty().bind(prefWidthProperty());

    initPlayerContextMenu();
    addContextMenuItems();
    installContextMenu();

  }

  public javafx.scene.media.MediaPlayer.Status getStatus(){
    MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
    if (mediaPlayer!= null)
      return mediaPlayer.getStatus();
    else
      return MediaPlayer.Status.UNKNOWN;
  }

    /**
     * put the media (movie) into the MovieViewer and play it if "playing" was active before or pause it if not
     *
     * @param mediaFile     mediaFile containing the media to show
     * @param seekPosition if not null it is tried to seek this position as soon as the movie is loaded/visible
     * @return true if the file could be played, false if not
     */
  @Override
  public boolean setMediaFileIfCompatible(MediaFile mediaFile, Duration seekPosition) {
    super.setMediaFileIfCompatible(mediaFile);   //maintains currentlyShowedMediaFile
    this.seekPosition = seekPosition;
    if (!(mediaFile instanceof PlayableFile playableFile)){  //includes test on null
      return false;
    }
    boolean compatible = true;

    resetPlayer();

    try {
      //if it cannot be put into a Media object it can not be played --> catch
      MediaPlayer mediaPlayer = (MediaPlayer) playableFile.getCachedOrLoadMediaContent(this, false);
      refreshViewIfCurrentMediaFile(playableFile, mediaPlayer);
    } catch (Exception e) {
      compatible = false;
    }
    return compatible;
  }

  /**
   * show video in viewer, if the mediaFile is the currently shown mediaFile (therefore preloaded files are not directly shown)
   * @param mediaFile
   * @param media  needs to be a MediaPlayer object
   * @return true if it was the current mediaFile
   */
  @Override
  public boolean refreshViewIfCurrentMediaFile(MediaFile mediaFile, Object media) {
    if (super.refreshViewIfCurrentMediaFile(mediaFile, media)) {
      if (media instanceof MediaPlayer mediaPlayer) {
        PlayerControlPanel viewerControlPanel = (PlayerControlPanel) this.viewerControlPanel; //one central cast cause in MediaViewer it's a ViewerControlPanel only

        mediaPlayer.setOnEndOfMedia(() -> {
          finished = true;
          mediaContentView.showNextOrRepeatMedia();
        });

        //install listener for player status to update play/pause/inactive, stop active/inactive
        mediaPlayer.statusProperty().addListener((observable, oldValue, newValue) -> {
          finished = false; //setEndOfMedia will set it to true
        });

        mediaPlayer.setOnReady(() -> {
          if (viewerControlPanel.isUserHasPaused())
            pause();
          else
            play();

          // as the media is playing move the slider for progress
          viewerControlPanel.setSliderScaling(mediaPlayer.getTotalDuration());
          viewerControlPanel.showProgress(Duration.ZERO);

          mediaPlayer.currentTimeProperty().addListener(ov -> {
            viewerControlPanel.showProgress(mediaPlayer.getCurrentTime());
          });

          if (seekPosition != null) seek(seekPosition);
        });

        mediaView.setMediaPlayer(mediaPlayer);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * reset the player: stop it and free all event Handlers
   */
  public void resetPlayer() {
    if (currentlyShowedMediaFile != null) {
      currentlyShowedMediaFile.flushMediaContent(); //do not use cache for PlayerViewerFX files
    }

    MediaPlayer mediaPlayer = this.mediaView.getMediaPlayer();
    if (mediaPlayer == null) return; //nothing to clean up, if there is none
    if (mediaPlayer.getStatus() == MediaPlayer.Status.DISPOSED) return;

    if (mediaPlayer.getStatus() != MediaPlayer.Status.STOPPED) mediaPlayer.stop();
    mediaPlayer.setOnPaused(null);
    mediaPlayer.setOnPlaying(null);
    mediaPlayer.setOnReady(null);
    mediaPlayer.dispose();
    mediaView.setMediaPlayer(null);

  }

  /**
   * start player from the beginning to implement repeat track
   */
  @Override
  public void rewindAndPlayWhenFinished() {
    seek(Duration.ZERO);//rewind
  }

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void play() {
    MediaPlayer mediaPlayer = this.mediaView.getMediaPlayer();
    if (mediaPlayer != null) {
      mediaPlayer.play();
    }
  }

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void pause() {
    MediaPlayer mediaPlayer = this.mediaView.getMediaPlayer();
    if (mediaPlayer != null) {
      if (finished){
        seek(Duration.ZERO); //rewind
        finished = false;
      }
      mediaPlayer.pause();
    }
  }

  /**
   * stop, rewind
   * adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void stop() {
    MediaPlayer mediaPlayer = this.mediaView.getMediaPlayer();
    if (mediaPlayer != null) {
      mediaPlayer.stop();
    }
  }

  /**
   * seek Position (Duration)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   * @param newPos position to jump to. null is treated like Duration.Zero (rewind)
   */
  public void seek(Duration newPos){
    MediaPlayer mediaPlayer = this.mediaView.getMediaPlayer();
    if (mediaPlayer != null){
      mediaPlayer.seek(Objects.requireNonNullElse(newPos, Duration.ZERO));

      if (finished){
        finished=false; //seeking results in not being at the end of the media anymore
      }
      if (((PlayerControlPanel)viewerControlPanel).isUserHasPaused()) mediaPlayer.pause();     //FX player keeps PLAYING status even if finished
    }
  }
  /**
   * get the current position of the media currently playing
   * @return current position
   */
  public Duration getCurrentTime() {
    MediaPlayer mediaPlayer = this.mediaView.getMediaPlayer();
    if (mediaPlayer != null)
      return mediaPlayer.getCurrentTime();
    else
      return Duration.UNKNOWN;
  }

  /**
   * if mediaPlayer is null (currently no media file displayed) Duration(0) is returned
   * @return the total length of the currently loaded media
   */
  public Duration getTotalDuration(){
    MediaPlayer mediaPlayer = this.mediaView.getMediaPlayer();
    if (mediaPlayer != null)
      return mediaPlayer.getTotalDuration();
    else
      return Duration.ZERO;
  }

  /**
   * nothing to release as FX media Player uses no external DLLs
   */
  public void releaseAll(){
    //nothing to do here
  }

  /**
   * load a Media specified by "FileOnDisk" property
   * note: if null is returned possibly MediaCache needs to be maintained to free memory and retried again
   * retry not implemented because never needed
   *
   * @return MediaPlayer if successful or null if not
   */

  @Override
  public Object getViewerSpecificMediaContent(MediaFile mediaFile) {
    if (!(mediaFile instanceof PlayableFile)) return null;

    MediaPlayer mediaPlayer = null;
    try {
      Media media = null;
      media = new Media(mediaFile.getFileOnDisk().toFile().toURI().toString());
      mediaPlayer = new MediaPlayer(media);

      //install error-listener for background start
      mediaPlayer.setOnError(() -> {
        resetPlayer();

        Platform.runLater(()->{    //
          if (mediaFile.retryCounterNotExceeded()){
            MediaPlayer reloadedPlayer = (MediaPlayer) mediaFile.getCachedOrLoadMediaContent(this, true);
            refreshViewIfCurrentMediaFile(mediaFile, reloadedPlayer);
          }
        });
      });

    } catch (Exception e) {
      //media = null;  //not supported --> media remains null
    }
    return mediaPlayer;
  }

  //----------------------- Implement ZoomableViewer Interface ----------------------------

  @Override
  public void setViewport(Rectangle2D value) {
    mediaView.setViewport(value);
  }

  @Override
  public Rectangle2D getViewport() {
    return mediaView.getViewport();
  }

  @Override
  public double getMediaWidth() {
    return mediaView.getMediaPlayer().getMedia().getWidth();
  }

  @Override
  public double getMediaHeight() {
    return mediaView.getMediaPlayer().getMedia().getHeight();
  }

  @Override
  public double getFitWidth() {
    return mediaView.getFitWidth();
  }

  @Override
  public double getFitHeight() {
    return mediaView.getFitHeight();
  }

}
