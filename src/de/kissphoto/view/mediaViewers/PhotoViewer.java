package de.kissphoto.view.mediaViewers;

import de.kissphoto.model.ImageFile;
import de.kissphoto.model.MediaFile;
import de.kissphoto.view.MediaContentView;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
 * @version 2014-05-25
 * @version 2014-05-25: zooming, moving (panning) (keyboard only)
 * @version 2014-06-01: mouse support added
 * @version 2014-06-02: mouse shape improved and animated (closedHand/openHand)
 * @version 2016-11-06: ViewportZoomerMover extracted for all viewport zooming and moving operations (now identical to movieViewer's)
 * @version 2017-10-21: Event-Handling (mouse/keyboard) centralized, so that viewport events and player viewer events can be handled
 * @version 2018-10-11: Support preview of rotation/mirroring
 * @version 2019-07-07: Cache problems fixed
 * @version 2020-11-02: Viewer now decides if it can show a media and returns true if so
 */
public class PhotoViewer extends ImageView implements ZoomableViewer {
  private final ViewportZoomer viewportZoomer;

  /**
   * constructor to initialize the viewer
   */
  public PhotoViewer(final MediaContentView contentView) {
    super();
    //link back to the containing Pane
    setPreserveRatio(true);

    //binding is performed in MediaContentView
    setFocusTraversable(true);

    viewportZoomer = new ViewportZoomer(this); //installs also the eventHandlers and contextMenu
    ContextMenu contextMenu = viewportZoomer.addContextMenuItems(null);  //null=no existing contextMenu but create new one
    viewportZoomer.installContextMenu(contentView, contextMenu);

    installMouseHandlers();
    installKeyboardHandlers();
  }

  //----------------------- Implement ZoomableViewer Interface ----------------------------

  /**
   * gets visible width of displayed image
   */

  @Override
  public double getMediaWidth() {
    return getImage().getWidth();
  }

  /**
   * gets visible height of displayed image
   */

  @Override
  public double getMediaHeight() {
    return getImage().getHeight();
  }
  @Override
  public void zoomToFit() {
    viewportZoomer.zoomToFit();
  }

  @Override
  public void installResizeHandler() {
    fitWidthProperty().addListener((observable, oldValue, newValue) -> viewportZoomer.handleResize());
    fitHeightProperty().addListener((observable, oldValue, newValue) -> viewportZoomer.handleResize());
  }

  private void installMouseHandlers() {
    setOnScroll(event -> {
      boolean handled = viewportZoomer.handleMouseScroll(event);
      if (handled) event.consume();
    });
    setOnMousePressed(event -> {
      boolean handled = viewportZoomer.handleMousePressed(event);
      if (handled) event.consume();
    });
    setOnMouseDragged(event -> {
      boolean handled = viewportZoomer.handleMouseDragged(event);
      if (handled) event.consume();
    });
    setOnMouseReleased(event -> {
      boolean handled = viewportZoomer.handleMouseReleased(event);
      if (handled) event.consume();
    });
    setOnMouseClicked(event -> {
      boolean handled = viewportZoomer.handleMouseClicked(event);
      if (handled) event.consume();
    });

  }

  private void installKeyboardHandlers() {
    setOnKeyPressed(event -> {
      boolean handled = viewportZoomer.handleKeyPressed(event);
      if (handled) event.consume();
    });
  }

  /**
   * determine if the mediaFile is an ImageFile that can be displayed by this viewer
   * compatible if
   * <ul>
   *   <li>MediaContent is an Image File and</li>
   *   <li>MediaContent can be loaded as an Image</li>
   * </ul>
   * <li>
   *
   * </li>
   *
   * set the internal property imageFile and show the imageFile.image
   * If imageFile==null nothing happens
   *
   * @param mediaFile mediaFile to be displayed
   * @return true if displaying was successful, false if mediaFile could not be displayed
   */
  public boolean setMediaFileIfCompatible(MediaFile mediaFile) {
    boolean compatible = (mediaFile != null) && (mediaFile.getClass() == ImageFile.class);

    if (compatible){
      //remember link to the file object to access current rotation
      Image image = (Image) mediaFile.getMediaContent();  //getMediaContent tries to put the content into an Image-Object and returns null if not possible
      if (image != null) {
        setImage(image);
      } else {
        compatible = false;
      }
    }
    return compatible;
  }


}
