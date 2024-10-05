package dringo.kissPhoto.view.mediaViewers;

import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.view.MediaContentView;
import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.StackPane;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This is the base class for all MediaViewers.
 * <ul>
 *   <li>viewerControls can be installed</li>
 *   <li>Context Menu can be activated</li>
 *   <li>ViewerControls can be installed</li>
 * </ul>
 *
 * @author Dringo
 * @since 2020-12-13  common father class introduced to all MediaViewers
 * @version 2020-12-20 now the viewer has to provide the object to be cached with MediaFile (because it's a view's implementation how to laod the file)
*/

public class MediaViewer extends StackPane {
  protected MediaContentView mediaContentView;  //backward link to the contentView that uses this mediaViewer
  protected ContextMenu contextMenu = new ContextMenu();   //the contextMenu of the viewer
  protected ViewerControlPanel viewerControlPanel; //the controls (BurgerMenu, FullScreen, ...)  To be initialized in the implementing subclass!
  protected MediaFile currentlyShowedMediaFile; //the file currently displayed by viewer

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

  public MediaFile getCurrentlyShowedMediaFile() {
    return currentlyShowedMediaFile;
  }

  /**
   * Only the viewer knows what would help it to display the media faster next time
   * standard is: do not use cache, i.e. return null as implemented here. Should be overwritten in subclasses
   * @param mediaFile the file to be loaded by the viewer
   * @return the object to be put in cache or null if no object can be put in cache
   */
  public Object getViewerSpecificMediaContent(MediaFile mediaFile){
    return null;
  }

  /**
   * try to preload the media content / put it into the cache
   * @return true if this is the viewer which can open the file
   */
  public boolean preloadMediaContent(MediaFile mediaFile){
    mediaFile.getCachedOrLoadMediaContent(this, false);
    return true;
  }
  /**
   * call this before setting PlayerViewer to null, e.g. to end internal thread
   */
  public void cleanUp() {
    viewerControlPanel.cleanUp();
  }

  /**
   * determine if the mediaFile can be displayed by this viewer
   * set the internal property currentlyShowedMediaFile and show it
   *
   * @param mediaFile mediaFile to be displayed
   * @return true if displaying was successful, false if mediaFile could not be displayed
   */
  public boolean setMediaFileIfCompatible(MediaFile mediaFile) {
    currentlyShowedMediaFile = mediaFile;
    return true;
  }


  /**
   * show media in viewer, if the mediaFile is the currently shown mediaFile (therefore preloaded files are not directly shown)
   * @param mediaFile
   * @param media
   * @return true if it was the current mediaFile
   */
  public boolean refreshViewIfCurrentMediaFile(MediaFile mediaFile, Object media){
    return currentlyShowedMediaFile==mediaFile;
  }


}
