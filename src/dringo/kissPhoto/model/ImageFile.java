package dringo.kissPhoto.model;

import com.drew.metadata.exif.ExifIFD0Directory;
import dringo.kissPhoto.KissPhoto;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.Objects;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is a special MediaFile, namely an image.
 * As a content in getCachedMediaContent() it delivers an Image-Object which will be loaded in background
 * It allows access to the Metadata (Exif, IPTC-Tags, ...)
 * ImageFile supports lossless rotation (90 degrees wise) and flipping of jpg-Images
 * <p/>
 *
 * @author Dringo
 * @since 2012-08-28
 * @version 2020-12-20 the according viewer determines now what to put in the cache (i.e. helps the viewer to show quickly)
 * @version 2020-11-19 globalSettings is now global (static in Kissphoto)
 * @version 2019-07-07: Cache problems fixed
 * @version 2019-06-22 mediaCache corrections: getMediaContentException() added
 * @version 2017-10-28 support of rotation for jpg files
 * @version 2014-06-05 java.io operations changed into java.nio
 */
public class ImageFile extends MediaFileTaggedEditable {
  // Orientation as described in Exif-Standard V.2.31
  private final static int TOP_LEFT = 1;      //         0°:The 0th row is at the visual top of the image, and the 0th column is the visual left-hand side.  --> nothing to do
  private final static int TOP_RIGHT = 2;     //     H-Flip:The 0th row is at the visual top of the image, and the 0th column is the visual right-hand side. --> flip horizontally
  private final static int BOTTOM_RIGHT = 3;  //       180°:The 0th row is at the visual bottom of the image, and the 0th column is the visual right-hand side. --> rotate 180
  private final static int BOTTOM_LEFT = 4;   //     V-Flip:The 0th row is at the visual bottom of the image, and the 0th column is the visual left-hand  side. --> flip vertically
  private final static int LEFT_TOP = 5;      //270°+V-Flip:The 0th row is the visual left-hand side of the image, and the 0th column is the visual top.     --> rotate90 then flip horizontally
  private final static int RIGHT_TOP = 6;     //       270°:The 0th row is the visual right-hand side of the image, and the 0th column is the visual top.    --> rotate90
  private final static int RIGHT_BOTTOM = 7;  // 90°+V-Flip:The 0th row is the visual right-hand side of the image, and the 0th column is the visual bottom. --> rotate270 then flip horizontally
  private final static int LEFT_BOTTOM = 8;   //        90°:The 0th row is the visual left-hand side of the image, and the 0th column is the visual bottom.  --> rotate270


  public ImageFile(Path imageFile, MediaFileList parent) {
    super(imageFile, parent);
  }


  @Override
  public void cancelBackgroundLoading() {
    super.cancelBackgroundLoading();
    if (content != null) ((Image) content).cancel();
  }

  /**
   * @return if content != null return the images progressProperty else null
   */
  @Override
  public ReadOnlyDoubleProperty getContentProgressProperty() {
    if (content != null)
      return ((Image) content).progressProperty();
    else
      return null;
  }


  /**
   * implement getMediaContentException for ImageFiles
   * to get any exception that occurred while loading.
   * A content is valid if:  (content != null) && (getMediaContentException() == null)
   *
   * @return null if no exception has occurred or content empty, anException if error occurred while loading
   */
  @Override
  public Exception getMediaContentException() {
    Image image = (Image) content;
    if (image != null) {
      if (image.getException() != null)
        return image.getException();   //image but not valid
      else if (image.isError())
        return new Exception("image.IsError"); //image but not valid
      else
        return null; //valid image --> no exception
    } else
      return null; //no image --> no exception
  }

  @Override
  public boolean canTransformInFile(){
    String filename = fileOnDisk.getFileName().toString().toLowerCase();
    return filename.endsWith(".jpg") ||
           filename.endsWith(".jpeg") ||
           filename.endsWith(".jp2");
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
      if (content != null) {
        return switch (currentContent.getPixelReader().getPixelFormat().getType()) {
          case BYTE_RGB -> 3;
          case BYTE_BGRA, BYTE_BGRA_PRE, BYTE_INDEXED -> 4;
          case INT_ARGB, INT_ARGB_PRE -> 8;
        };
      }
    } catch (Exception e) {
      //during complete flush (e.g. reload) it might happen that getPixelReader() returns null
    }
    return 8; //as default, ie. if error occurred or PixelFormat is unknown then calculate with the maximum possible
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
   * load the external editor's pathNames from globalSettings
   */
  public static void loadExternalEditorPaths() {
    try {
      ImageFile.externalMainEditorPath = KissPhoto.globalSettings.getProperty(ImageFile.class.getSimpleName() + MAIN_EDITOR);
      ImageFile.external2ndEditorPath = KissPhoto.globalSettings.getProperty(ImageFile.class.getSimpleName() + SECOND_EDITOR);
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
   */
  public static void setExternalEditorPaths(String newExternalMainEditorPath, String newExternal2ndEditorPath) {
    ImageFile.externalMainEditorPath = Objects.requireNonNullElse(newExternalMainEditorPath, "");
    ImageFile.external2ndEditorPath = Objects.requireNonNullElse(newExternal2ndEditorPath, "");

    saveExternalEditorPaths();
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
        executeExternalEditor(selection, ImageFile.externalMainEditorPath);
      else
        executeExternalEditor(selection, ImageFile.external2ndEditorPath);
    }
  }

  /**
   * save the external editor's pathnames to GlobalSettings
   */
  protected static void saveExternalEditorPaths() {
    if (ImageFile.externalMainEditorPath != null)
      KissPhoto.globalSettings.setProperty(ImageFile.class.getSimpleName() + MAIN_EDITOR, ImageFile.externalMainEditorPath);
    if (ImageFile.external2ndEditorPath != null)
      KissPhoto.globalSettings.setProperty(ImageFile.class.getSimpleName() + SECOND_EDITOR, ImageFile.external2ndEditorPath);
  }

  public static String getExternalMainEditorPath() {
    return ImageFile.externalMainEditorPath;
  }

  public static String getExternal2ndEditorPath() {
    return ImageFile.external2ndEditorPath;
  }

  /**
   * the orientation read out of the jpegs exif directory
   * if the file has no metadata (or is no jpeg) then return -1
   *
   * @return orientation or -1 if not readable
   */
  public int getEXIFOrientation() {
    getMetadata();

    ExifIFD0Directory jpegDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class); //JpegDirectory.class);

    int orientation=TOP_LEFT; //unchanged
    try {
      if (jpegDirectory!=null)
        orientation = jpegDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
      //System.out.println("Orientation:" + orientation);
    } catch (Exception e) {
      //System.out.println("Orientation Exception: " + e);
      orientation = -1;
    }
    return orientation;
  }


  /**
   * read the current orientation from exif and set the planned transformations accordingly
   */
  public void setOrientationAccordingExif() {
    int orientation = getEXIFOrientation();

    switch (orientation) {
      case TOP_LEFT:
        break;
      case TOP_RIGHT:
        rotateOperation = RotateOperation.ROTATE0;
        flipHorizontally = true;
        flipVertically = false;
        break;
      case BOTTOM_RIGHT:
        rotateOperation = RotateOperation.ROTATE180;
        flipHorizontally = false;
        break;
      case BOTTOM_LEFT:
        rotateOperation = RotateOperation.ROTATE0;
        flipHorizontally = false;
        flipVertically = true;
        break;
      case LEFT_TOP:
        rotateOperation = RotateOperation.ROTATE90;
        flipHorizontally = true;
        flipVertically = false;
        break;
      case RIGHT_TOP:
        rotateOperation = RotateOperation.ROTATE90;
        flipHorizontally = false;
        flipVertically = false;
        break;
      case RIGHT_BOTTOM:
        rotateOperation = RotateOperation.ROTATE270;
        flipHorizontally = true;
        flipVertically = false;
        break;
      case LEFT_BOTTOM:
        rotateOperation = RotateOperation.ROTATE270;
        flipHorizontally = false;
        flipVertically = false;
        break;
    }
    updateStatusProperty();
  }
}
