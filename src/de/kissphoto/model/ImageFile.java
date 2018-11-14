package de.kissphoto.model;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import de.kissphoto.helper.GlobalSettings;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.nio.file.Path;

/**
 * This is a special MediaFile, namely an image.
 * As a content in getCachedMediaContent() it delivers an Image-Object which will be loaded in background
 * It allows access to the Metadata (Exif, ITPC-Tags, ...)
 * ImageFile supports lossless rotation (90 degrees wise) and flipping of jpg-Images
 * <p/>
 *
 * @author Dr. Ingo Kreuz
 * date: 28.08.12
 * modified:
 * 2014-06-05 java.io operations changed into java.nio
 * 2017-10-28 support of rotation for jpg files
 */
public class ImageFile extends MediaFileTagged {
  //Exif orientation constants
  private final static int TOP_LEFT = 1;
  private final static int TOP_RIGHT = 2;
  private final static int BOTTOM_RIGHT = 3;
  private final static int BOTTOM_LEFT = 4;
  private final static int LEFT_TOP = 5;
  private final static int RIGHT_TOP = 6;
  private final static int RIGHT_BOTTOM = 7;
  private final static int LEFT_BOTTOM = 8;

  protected static ImageFileRotater imageFileRotater = new ImageFileRotater();


  public ImageFile(Path imageFile, MediaFileList parent) {
    super(imageFile, parent);
  }

  @Override
  public Object getMediaContent() {
    if (content == null) {  //if not already loaded
      content = new Image(fileOnDisk.toUri().toString(), true);  //true=load in Background
    }
    return content;
  }

  @Override
  public MediaFileRotater getMediaFileRotater() {
    return imageFileRotater;
  }

  /**
   * if the content is loaded (not null)
   * this functions reads out the images color format and returns the number of bytes needed per pixel
   *
   * @return number of bytes per Pixel
   */
  public int getBytesPerPixel() {
    try {
      Image currentContent = (Image) content;
      if (content != null)
        switch (currentContent.getPixelReader().getPixelFormat().getType()) {
          case BYTE_RGB:
            return 3;
          case BYTE_BGRA:
            return 4;
          case BYTE_BGRA_PRE:
            return 4;
          case BYTE_INDEXED:
            return 4;
          case INT_ARGB:
            return 8;
          case INT_ARGB_PRE:
            return 8;
          default:
            return 1;
        }
    } catch (Exception e) {
      //during complete flush (e.g. reload) it might happen that getPixelReader() returns null
    }
    return 8; //as default, ie. if error occured or PixelFormat is unknown then calculate with the maximum possible
  }

  @Override
  public long getContentApproxMemSize() {
    Image currentImage = (Image) content;

    //the "guess"
    if (currentImage != null)
      return (long) ((currentImage.getHeight() * currentImage.getWidth() * getBytesPerPixel()) * 1.1);
    else
      return 0;
  }

  /*
   *------------------------------------------------- Logic for External Editors --------------------------------
   * re-implement these methods for all siblings of MediaFile, because every child of MediaFile needs different editors and constants
   * ...and static methods have no late binding!
   */

  private static String externalMainEditorPath;  //every sub class needs it's own value
  private static String external2ndEditorPath;

  /**
   * load the external editor's pathnames from globalSettings
   *
   * @param globalSettings link to globalSettings
   */
  public static void loadExternalEditorPaths(GlobalSettings globalSettings) {
    try {
      ImageFile.externalMainEditorPath = globalSettings.getProperty(ImageFile.class.getSimpleName() + MAIN_EDITOR);
      ImageFile.external2ndEditorPath = globalSettings.getProperty(ImageFile.class.getSimpleName() + SECOND_EDITOR);
    } catch (Exception e) {
      //nothing to do in case of exception --> editors will remain inactive
      //until a value is added via ExternalEditorsDialog
    }
  }

  /**
   * set the paths for executing external editors for the class className and save the paths in global settings
   *
   * @param newExternalMainEditorPath the path to the external main editor
   * @param newExternal2ndEditorPath  the path to the external second editor
   * @param globalSettings            link to globalSettings
   */
  public static void setExternalEditorPaths(String newExternalMainEditorPath, String newExternal2ndEditorPath, GlobalSettings globalSettings) {
    if (newExternalMainEditorPath != null)
      ImageFile.externalMainEditorPath = newExternalMainEditorPath;
    else
      ImageFile.externalMainEditorPath = "";

    if (newExternal2ndEditorPath != null)
      ImageFile.external2ndEditorPath = newExternal2ndEditorPath;
    else
      ImageFile.external2ndEditorPath = "";

    saveExternalEditorPaths(globalSettings);
  }

  /**
   * execute the external editor specified for the subclass of MediaFile
   * If the specified external editor is invalid nothing will happen
   * If the selection is empty or null nothing will happen
   *
   * @param selection  the list of MediaFiles currently selected will be passed as parameters
   * @param mainEditor true to execute MainEditor, false to execute 2ndEditor
   */
  public static void executeExternalEditor(ObservableList<MediaFile> selection, boolean mainEditor) {
    if (selection != null && selection.size() > 0) {
      if (mainEditor)
        MediaFile.executeExternalEditor(selection, ImageFile.externalMainEditorPath);
      else
        MediaFile.executeExternalEditor(selection, ImageFile.external2ndEditorPath);
    }
  }

  /**
   * save the external editor's pathnames to GlobalSettings
   *
   * @param globalSettings link to globalSettings
   */
  protected static void saveExternalEditorPaths(GlobalSettings globalSettings) {
    if (ImageFile.externalMainEditorPath != null)
      globalSettings.setProperty(ImageFile.class.getSimpleName() + MAIN_EDITOR, ImageFile.externalMainEditorPath);
    if (ImageFile.external2ndEditorPath != null)
      globalSettings.setProperty(ImageFile.class.getSimpleName() + SECOND_EDITOR, ImageFile.external2ndEditorPath);
  }

  public static String getExternalMainEditorPath() {
    return ImageFile.externalMainEditorPath;
  }

  public static String getExternal2ndEditorPath() {
    return ImageFile.external2ndEditorPath;
  }

  /**
   * the orientation read out of the jpegs exif directory
   *
   * @return orientation or -1 if not readable
   */
  public int getEXIFOrientation() {
    getMetadata();

    ExifIFD0Directory jpegDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class); //JpegDirectory.class);

    int orientation = 1;
    try {
      orientation = jpegDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
      System.out.println("Orientation:" + orientation);
    } catch (MetadataException me) {
      System.out.println("Orientation Exception: " + me);
      orientation = -1;
    }
    return orientation;
  }

  /* Orientation as described in Exif-Standard V.2.31
   *
   * 1 = TOP_LEFT:             0°:  The 0th row is at the visual top of the image, and the 0th column is the visual left-hand side.  --> nothing to do
   * 2 = TOP_RIGHT:        H-Flip:  The 0th row is at the visual top of the image, and the 0th column is the visual right-hand side. --> flip horizontally
   * 3 = BOTTOM_RIGHT:       180°:  The 0th row is at the visual bottom of the image, and the 0th column is the visual right-hand side. --> rotate 180
   * 4 = BOTTOM_LEFT:      V-Flip:  The 0th row is at the visual bottom of the image, and the 0th column is the visual left-hand  side. --> flip vertically
   * 5 = LEFT_TOP:    270°+V-Flip:  The 0th row is the visual left-hand side of the image, and the 0th column is the visual top.     --> rotate90 then flip horizontally
   * 6 = RIGHT_TOP:          270°:  The 0th row is the visual right-hand side of the image, and the 0th column is the visual top.    --> rotate90
   * 7 = RIGHT_BOTTOM: 90°+V-Flip:  The 0th row is the visual right-hand side of the image, and the 0th column is the visual bottom. --> rotate270 then flip horizontally
   * 8 = LEFT_BOTTOM:         90°:  The 0th row is the visual left-hand side of the image, and the 0th column is the visual bottom.  --> rotate270
   ' Other = reserved
   */

  /**
   * read the current orientation from exif and set the planned transformations accordingly
   */
  public void setOrientationAccordingExif() {
    int orientation = getEXIFOrientation();

    switch (orientation) {
      case TOP_LEFT:
        break;
      case TOP_RIGHT:
        rotateOperation = ImageFileRotater.RotateOperation.ROTATE0;
        flipHorizontally = true;
        flipVertically = false;
        break;
      case BOTTOM_RIGHT:
        rotateOperation = ImageFileRotater.RotateOperation.ROTATE180;
        flipHorizontally = false;
        break;
      case BOTTOM_LEFT:
        rotateOperation = ImageFileRotater.RotateOperation.ROTATE0;
        flipHorizontally = false;
        flipVertically = true;
        break;
      case LEFT_TOP:
        rotateOperation = ImageFileRotater.RotateOperation.ROTATE90;
        flipHorizontally = true;
        flipVertically = false;
        break;
      case RIGHT_TOP:
        rotateOperation = ImageFileRotater.RotateOperation.ROTATE90;
        flipHorizontally = false;
        flipVertically = false;
        break;
      case RIGHT_BOTTOM:
        rotateOperation = ImageFileRotater.RotateOperation.ROTATE270;
        flipHorizontally = true;
        flipVertically = false;
        break;
      case LEFT_BOTTOM:
        rotateOperation = ImageFileRotater.RotateOperation.ROTATE270;
        flipHorizontally = false;
        flipVertically = false;
        break;
    }
  }
}
