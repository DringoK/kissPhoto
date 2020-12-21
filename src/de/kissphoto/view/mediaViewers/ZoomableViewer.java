package de.kissphoto.view.mediaViewers;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This interface has to be implemented by all views that want to use ViewportZoomerMover to enable
 * zoom and panning support for its viewport
 *
 * @author Dr. Ingo Kreuz
 * @since 2016-11-06
 * @version 2018-10-11  bugfixing: zoom rotated images
 */

public interface ZoomableViewer {

  /**
   * install Handler for resizing the viewer
   * In common a listener on width/height has to be implemented which calls ViewportZoomer.handleResize()
   */
  void installResizeHandler();

  //--------- Zooming in MediaContent View: hand over zoom factor between the viewers
  //zoom so that the media fits directly into the viewer (delete viewport)
  void zoomToFit();
}
