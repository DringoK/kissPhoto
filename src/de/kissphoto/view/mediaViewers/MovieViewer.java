package de.kissphoto.view.mediaViewers;

import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.mediaViewers.helper.PlayerViewer;
import javafx.geometry.Rectangle2D;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;

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
 */
public class MovieViewer extends PlayerViewer implements ZoomableViewer {
  protected MediaView mediaView;
  private ViewportZoomer viewportZoomer;

  /**
   * @constructor to initialize the viewer
   */
  public MovieViewer(final MediaContentView contentView) {
    super(contentView);   //mediaContentView of fatherclass is now = contentView
    mediaView = new MediaView();
    getChildren().addAll(mediaView, playerControls);

    mediaView.setPreserveRatio(true);
    mediaView.fitHeightProperty().bind(mediaContentView.heightProperty());
    mediaView.fitWidthProperty().bind(mediaContentView.widthProperty());

    setFocusTraversable(true);

    viewportZoomer = new ViewportZoomer(this);

    initPlayerContextMenu();
    viewportZoomer.addContextMenuItems(contextMenu);
    viewportZoomer.installContextMenu(mediaContentView, contextMenu);
  }

  /**
   * put the media (movie) into the MovieViewer and play it
   *
   * @param media the media to show
   */
  public void setMedia(Media media) {
    super.setMedia(media);
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
}
