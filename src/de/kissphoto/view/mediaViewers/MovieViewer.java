package de.kissphoto.view.mediaViewers;

import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.mediaViewers.helper.PlayerViewer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for movie clips.
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
 * @date 2014-06-09
 * @modified: 2016-11-06: ViewportZoomerMover extracted for all viewport zooming and moving operations (now identical to PhotoViewer's)
 * @modified: 2017-10-21: Event-Handling (mouse/keyboard) centralized, so that viewport events and player viewer events can be handled
 */
public class MovieViewer extends PlayerViewer implements ZoomableViewer {
  protected MediaView mediaView;
  private ViewportZoomer viewportZoomer;

  /**
   * @constructor to initialize the viewer
   */
  public MovieViewer(final MediaContentView contentView) {
    super(contentView);   //mediaContentView of father class is now = contentView
    mediaView = new MediaView();
    //note: playerControls defined and initialized in PlayerViewer (fatherclass)
    getChildren().addAll(mediaView, playerControls);

    mediaView.setPreserveRatio(true);
    mediaView.fitHeightProperty().bind(mediaContentView.heightProperty());
    mediaView.fitWidthProperty().bind(mediaContentView.widthProperty());

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
   * @param media the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the movie is loaded/visible
   */
  public void setMedia(Media media, Duration seekPosition) {
    super.setMedia(media, seekPosition);
    mediaView.setMediaPlayer(mediaPlayer);
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
