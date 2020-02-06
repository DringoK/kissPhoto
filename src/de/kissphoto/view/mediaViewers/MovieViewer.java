package de.kissphoto.view.mediaViewers;

import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.helper.PlayerViewer;
import javafx.util.Duration;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class is the abstract viewer class for movie clips.
 * - The internal implementation is MovieViewerFX
 * - If VLC installation is found then MovieViewerVLC can be used
 * - if no video playing is possible MovieViewerDummy has to be used
 * By kissPhoto they are treated like photos that move ("like in harry potter newspaper")
 *
 * @author Dr. Ingo Kreuz
 * @date 2011-11-02
 * @modified:
 */
public abstract class MovieViewer extends PlayerViewer implements ZoomableViewer {

  public MovieViewer(MediaContentView mediaContentView) {
    super(mediaContentView);
  }

  public Duration getCurrentTime() {
    if (mediaPlayer != null)
      return mediaPlayer.getCurrentTime();
    else
      return Duration.UNKNOWN;    //e.g. MovieViewerDummy does not have a mediaPlayer so there is no duration in this case
  }
}
