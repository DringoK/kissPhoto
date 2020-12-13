package de.kissphoto.view.mediaViewers;

import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.viewerHelpers.ViewerControlPanel;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.StackPane;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This is the base class for all MediaViewers.
 * <ul>
 *   <li>viewerControls can be installed</li>
 *   <li>Context Menu can be activated</li>
 *   <li>ViewerControls can be installed</li>
 * </ul>
 * Assumption: All Viewers that implement MediaViewer must be derived from Node
 *
 * @author Dr. Ingo Kreuz
 * @since 2020-12-13
 * @version 2020-12-13
*/

public class MediaViewer extends StackPane {
  protected MediaContentView mediaContentView;  //backward link to the contentView that uses this mediaViewer
  protected ContextMenu contextMenu = new ContextMenu();   //the contextMenu of the viewer
  protected ViewerControlPanel viewerControlPanel; //the controls (BurgerMenu, FullScreen, ...)  To be initialized in implementing subclass!

  public MediaViewer(final MediaContentView mediaContentView){
    this.mediaContentView = mediaContentView;
    setViewerControlPanel();

    setFocusTraversable(true);
  }

  /**
   * needs to be overwritten if a special viewerControl is used in the implementing subclass (e.g. in PlayerViewer: PlayerControls is used)
   */
  void setViewerControlPanel(){
    viewerControlPanel = new ViewerControlPanel(this);
  }

  public ContextMenu getContextMenu() {
    return contextMenu;
  }

  public MediaContentView getMediaContentView(){
    return mediaContentView;
  }

  /**
   * call this before setting PlayerViewer to null, e.g. to end internal thread
   */
  public void cleanUp() {
    viewerControlPanel.cleanUp();
  }

}
