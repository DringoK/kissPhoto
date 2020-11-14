package de.kissphoto.view.mediaViewers;

import de.kissphoto.model.MediaFile;
import de.kissphoto.model.MovieFile;
import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.helper.PlayerViewer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
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
 * <li>autoPlay feature: when on: if media is selected playback starts immediately, if end of media next media is shown
 * </ul>
 *
 * @author Dr. Ingo Kreuz
 * @since 2014-06-09
 * @version 2020-10-25: JavaFX MediaPlayer renamed from MovieViewer to MovieViewerFX, to enable MovieViewerVLCJ. Pause/End of File behavior improved
 * @version 2017-10-21: Event-Handling (mouse/keyboard) centralized, so that viewport events and player viewer events can be handled
 * @version 2016-11-06: ViewportZoomerMover extracted for all viewport zooming and moving operations (now identical to PhotoViewer's)
 */
public class MovieViewerFX extends PlayerViewer {
  protected MediaView mediaView;
  private ViewportZoomer viewportZoomer;

  protected MediaPlayer mediaPlayer;      //initialized in setMedia()
  private boolean finished; //true if the endOfMedia event had been detected, false if any other status has been detected


  /**
   * @constructor to initialize the viewer
   */
  public MovieViewerFX(final MediaContentView contentView) {
    super(contentView);   //mediaContentView of father class is now = contentView
    //binding is automatically when placed in a StackPane (mediaStackPane is a StackPane)
    //prefHeightProperty().bind(mediaContentView.getMediaStackPaneHeightProperty());
    //prefWidthProperty().bind(mediaContentView.getMediaStackPaneWidthProperty());

    mediaView = new MediaView();
    mediaView.setPreserveRatio(true);

    //note: playerControls defined and initialized in PlayerViewer (fatherclass)
    getChildren().addAll(mediaView, playerControls);

    mediaView.fitHeightProperty().bind(prefHeightProperty());
    mediaView.fitWidthProperty().bind(prefWidthProperty());

    setFocusTraversable(true);

    viewportZoomer = new ViewportZoomer(this);

    initPlayerContextMenu();
    viewportZoomer.addContextMenuItems(contextMenu);
    viewportZoomer.installContextMenu(mediaContentView, contextMenu);

    installMouseHandlers();
    installKeyboardHandlers();
  }

  /**
   * put the media (movie) into the MovieViewer and play it
   *
   * @param mediaFile     mediaFile containing the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the movie is loaded/visible
   * @return true if the file could be played, false if not
   */
  public boolean setMediaFileIfCompatible(MediaFile mediaFile, Duration seekPosition) {
    resetPlayer();

    boolean compatible = (mediaFile != null) && (mediaFile.getClass() == MovieFile.class);
    if (compatible) try {
      Media media = (Media)mediaFile.getMediaContent();  //if it cannot be put into a Media object it can not be played --> catch
      mediaPlayer = new MediaPlayer(media);

      mediaPlayer.setOnReady(new Runnable() {
        @Override
        public void run() {
          if (autoPlayProperty.get() && !mediaContentView.isFileTableViewInEditMode())
            play();
          else
            pause();

          // as the media is playing move the slider for progress
          playerControls.setSliderScaling(mediaPlayer.getTotalDuration());
          playerControls.showProgress(Duration.ZERO);

          mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
              if (mediaPlayer != null) playerControls.showProgress(mediaPlayer.getCurrentTime());
            }
          });

          if (seekPosition != null) seek(seekPosition);
        }
      });
      mediaPlayer.setOnEndOfMedia(new Runnable() {
        @Override
        public void run() {
          boolean skipped = skipToNextOnAutoPlay();
          if (!skipped) {
            //already at the end of the file list --> FXPlayer remains to be paused,but does not fire a StatusProperty Change event
            setPlayerStatusInAllMenues(MediaPlayer.Status.PAUSED); //therefore sync the menues here likein change Event of Status
            finished = true;
          }
        }
      });

      //install listener for player status to update play/pause/inactive, stop active/inactive
      mediaPlayer.statusProperty().addListener(new ChangeListener<MediaPlayer.Status>() {
        @Override
        public void changed(ObservableValue<? extends MediaPlayer.Status> observable, MediaPlayer.Status oldValue, MediaPlayer.Status newValue) {
          setPlayerStatusInAllMenues(newValue);
          finished = false; //setEndOfMedia will set it to true, any other status indicates that it
        }
      });

      mediaView.setMediaPlayer(mediaPlayer);
    } catch (Exception e) {
      compatible = false;
    }
    return compatible;
  }
  /**
   * reset the player: stop it and free all event Handlers
   */
  public void resetPlayer(){
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.setOnPaused(null);
      mediaPlayer.setOnPlaying(null);
      mediaPlayer.setOnReady(null);
      mediaPlayer.dispose();
      mediaPlayer = null;
    }
  }
  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void play() {
    if (mediaPlayer != null) {
      if (finished) seek(Duration.ZERO); //rewind
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
   * stop, rewind
   * adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  public void stop() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
    }
  }

  /**
   * seek Position (Duration)
   * if mediaPlayer is null (currently no media file displayed) nothin happens
   * @param newPos position to jump to
   */
  public void seek(Duration newPos){
    if (mediaPlayer != null){
      mediaPlayer.seek(newPos);
      if (finished){
        setPlayerStatusInAllMenues(mediaPlayer.getStatus()); //sync menues with status
        finished=false; //seeking results in not being at the end of the media any more
      }
    }
  }
  /**
   * get the current position of the media currently playing
   * @return current position
   */
  public Duration getCurrentTime() {
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
    return mediaPlayer.getMedia().getWidth();
  }

  @Override
  public double getMediaHeight() {
    return mediaPlayer.getMedia().getHeight();
  }

  @Override
  public double getFitWidth() {
    return mediaView.getFitWidth();
  }

  @Override
  public double getFitHeight() {
    return mediaView.getFitHeight();
  }

  @Override
  public void installResizeHandler() {
    prefWidthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        viewportZoomer.handleResize();
      }
    });
    prefHeightProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        viewportZoomer.handleResize();
      }
    });
  }

  @Override
  public void zoomToFit() {
    viewportZoomer.zoomToFit();
  }

  private void installMouseHandlers() {
    setOnScroll(new EventHandler<ScrollEvent>() {
      @Override
      public void handle(ScrollEvent event) {
        boolean handled = viewportZoomer.handleMouseScroll(event);
        if (handled) event.consume();
      }
    });
    setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        boolean handled = viewportZoomer.handleMousePressed(event);
        handled = handleMousePressed(event) || handled; //inherited from PlayerViewer
        if (handled) event.consume();
      }
    });

    setOnMouseMoved(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        boolean handled = handleMouseMoved(event); //inherited from PlayerViewer
        if (handled) event.consume();
      }
    });
    setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        boolean handled = viewportZoomer.handleMouseDragged(event);
        handled = handleMouseDragged(event) || handled; //inherited from PlayerViewer
        if (handled) event.consume();
      }
    });
    setOnMouseReleased(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        boolean handled = viewportZoomer.handleMouseReleased(event);
        if (handled) event.consume();
      }
    });
    setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        //clicks must only be handled by one class to perform only one action at a time
        boolean handled = viewportZoomer.handleMouseClicked(event);
        if (!handled) {
          handled = handleMouseClicked(event);
        } //inherited from PlayerViewer
        if (handled) event.consume();
      }
    });

  }

  private void installKeyboardHandlers() {
    setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        //keyboard clicks must only be handled by one class to perform only one action at a time
        boolean handled = viewportZoomer.handleKeyPressed(event);
        if (!handled) {
          handleKeyPressed(event);
        }     //inherited from PlayerViewer
        if (handled) event.consume();
      }
    });
  }

}
