package de.kissphoto.view.mediaViewers;

import javafx.geometry.Rectangle2D;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This interface has to be implemented by all views that want to use ViewportZoomerMover to enable
 * zoom and panning support for its viewport
 *
 * @author Dr. Ingo Kreuz
 * @date 2016-11-06
 * @modified:
 */

public interface ZoomableViewer {
  public void setViewport(Rectangle2D value);

  Rectangle2D getViewport();

  //proportions of Image/Media
  double getMediaWidth();

  double getMediaHeight();

  //proportions of viewer
  double getFitWidth();

  double getFitHeight();
}
