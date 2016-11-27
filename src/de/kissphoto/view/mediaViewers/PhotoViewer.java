package de.kissphoto.view.mediaViewers;

import de.kissphoto.helper.I18Support;
import de.kissphoto.view.MediaContentView;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.ImageView;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for photos
 * <ul>
 * <li>JavaFx fast high quality rendering
 * <li>zooming in/out/100%/fit
 * <li>full screen support
 * <li>displaying filename/date in picture
 * <li>fading button Controls + keyboard support (next/prev, zoom, fullscreen)
 * </ul>
 *
 * @author Dr. Ingo Kreuz
 * @date 2014-05-25
 * @modified: 2014-05-25: zooming, moving (panning) (keyboard only)
 * @modified: 2014-06-01: mouse support added
 * @modified: 2014-06-02: mouse shape improved and animated (closedHand/openHand)
 * @modified: 2016-11-06: ViewportZoomerMover extracted for all viewport zooming and moving operations (now identical to movieViewer's)
 */
public class PhotoViewer extends ImageView implements ZoomableViewer {
  private static ResourceBundle language = I18Support.languageBundle;

  private MediaContentView mediaContentView;
  private ViewportZoomer viewportZoomer;

  /**
   * constructor to initialize the viewer
   */
  public PhotoViewer(final MediaContentView contentView) {
    super();
    this.mediaContentView = contentView;
    setPreserveRatio(true);

    fitHeightProperty().bind(mediaContentView.heightProperty());
    fitWidthProperty().bind(mediaContentView.widthProperty());

    setFocusTraversable(true);

    viewportZoomer = new ViewportZoomer(this); //installs also the eventHandlers and contextMenu
    ContextMenu contextMenu = viewportZoomer.addContextMenuItems(null);  //null=no existing contextMenu but create new one
    viewportZoomer.installContextMenu(mediaContentView, contextMenu);

  }

  //----------------------- Implement ZoomableViewer Interface ----------------------------

  @Override
  public double getMediaWidth() {
    return getImage().getWidth();
  }

  @Override
  public double getMediaHeight() {
    return getImage().getHeight();
  }
}
