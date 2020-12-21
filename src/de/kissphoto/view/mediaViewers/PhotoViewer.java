package de.kissphoto.view.mediaViewers;

import de.kissphoto.model.ImageFile;
import de.kissphoto.model.MediaFile;
import de.kissphoto.view.MediaContentView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for photos
 * <ul>
 * <li>JavaFx fast high quality rendering</li>
 * <li>zooming in/out/100%/fit</li>
 * <li>full screen support</li>
 * <li>displaying filename/date in picture</li>
 * <li>keyboard support (next/prev, zoom, fullscreen)</li>
 * </ul>
 *
 * @author Dr. Ingo Kreuz
 * @since 2014-05-25
 * @version 2020-12-20: MediaFile-Type and cache content is now controlled by the viewers: only the know what they accept and what should be cached to speed up viewing
 * @version 2020-12-13: same structure like playerViewers now: "contains an imageView" not "is an imageView". Therefore common sybling: MediaViewer :)
 * @version 2020-11-02: Viewer now decides if it can show a media and returns true if so
 * @version 2019-07-07: Cache problems fixed
 * @version 2018-10-11: Support preview of rotation/mirroring
 * @version 2017-10-21: Event-Handling (mouse/keyboard) centralized, so that viewport events and player viewer events can be handled
 * @version 2016-11-06: ViewportZoomerMover extracted for all viewport zooming and moving operations (now identical to movieViewer's)
 * @version 2014-06-02: mouse shape improved and animated (closedHand/openHand)
 * @version 2014-06-01: mouse support added
 * @version 2014-05-25: zooming, moving (panning) (keyboard only)
 */
public class PhotoViewer extends MediaViewerZoomable{
  /**
   * Reports if a given file would be accepted by this viewer
   * Note: some Image Formats are accepted that currently cannot be displayed by JavaFX
   * but useful for loading external editor (nothing will be displayed (black))
   * @param file to be investigated
   * @return true if file is an image
   */
  public static boolean willAccept(Path file){
    String filename = file.getFileName().toString().toLowerCase();
    return filename.endsWith(".jpg") ||
          filename.endsWith(".jpeg") ||
          filename.endsWith(".jp2")  ||
          filename.endsWith(".png")  ||
          filename.endsWith(".bmp")  ||
          filename.endsWith(".gif")  ||
          filename.endsWith(".tiff") ||
          filename.endsWith(".tif")  ||
          filename.endsWith(".ico");
  }

  private ImageView imageView = new ImageView();

  /**
   * constructor to initialize the viewer
   */
  public PhotoViewer(final MediaContentView contentView) {
    super(contentView);
    //link back to the containing Pane
    imageView.setPreserveRatio(true);

    getChildren().addAll(imageView, viewerControlPanel);

    imageView.fitHeightProperty().bind(prefHeightProperty());
    imageView.fitWidthProperty().bind(prefWidthProperty());


    addContextMenuItems();  //inherited from MediaViewerZoomable
    installContextMenu(); //inherited from MediaViewerZoomable

    installKeyboardHandlers();
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
    if (!(mediaFile instanceof ImageFile)){ //includes test on null
      return false;
    }
    boolean compatible = true;

    //remember link to the file object to access current rotation
    Image image = (Image) mediaFile.getMediaContentCached(this);  //getMediaContent tries to put the content into an Image-Object and returns null if not possible
    if (image != null) {
      imageView.setImage(image);
    } else {
      compatible = false;
    }
    return compatible;
  }

  /**
   * load an image specified by "FileOnDisk" property
   *
   * @return Image if successful or null if not
   * note: if null is returned possibly MediaCache needs to be maintained to free memory..and retried again
   * which is recursively tried
   */
  @Override
  public Object getViewerSpecificMediaContent(MediaFile mediaFile) {
    if (!(mediaFile instanceof ImageFile)) return null;
    MediaViewer photoViewer = this; //to hand over into errorPropertyChange Listener

    Image image= null;
    if (mediaFile.isMediaContentInValid()) {  //if not already loaded i.e. image in cache is invalid
      try {
        //System.out.println("RetryCounter="+loadRetryCounter + "  getMediaContent loading " + fileOnDisk);
        image = new Image(mediaFile.getFileOnDisk().toUri().toString(), true);  //true=load in Background
        //install error-listener for background-loading
        image.errorProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            mediaFile.flushFromCache();
            if (mediaFile.shouldRetryLoad())
              mediaFile.getMediaContentCached(photoViewer); //i.e. retry is recursive
          }
        });
        image.progressProperty().addListener(new ChangeListener<Number>() {
          @Override
          public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (newValue.doubleValue() >= 1.0) {     //if image loaded completely then retryCounter can be reset
              mediaFile.resetLoadRetryCounter();
            }
          }
        });
      } catch (Exception e) {
        //will not occur with backgroundLoading: image.getException will get the exception
      }
    }

    return image;
  }


  //----------------------- Implement ZoomableViewer Interface ----------------------------


  @Override
  public Rectangle2D getViewport() {
    return imageView.getViewport();
  }

  @Override
  public void setViewport(Rectangle2D value) {
    imageView.setViewport(value);
  }

  /**
   * gets visible width of displayed image
   */

  @Override
  public double getMediaWidth() {
    return imageView.getImage().getWidth();
  }

  /**
   * gets visible height of displayed image
   */

  @Override
  public double getMediaHeight() {
    return imageView.getImage().getHeight();
  }

  @Override
  public double getFitWidth() {
    return imageView.getFitWidth();
  }

  @Override
  public double getFitHeight() {
    return imageView.getFitHeight();
  }
}
