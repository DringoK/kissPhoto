package de.kissphoto.model;

import de.kissphoto.helper.GlobalSettings;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.nio.file.Path;

/**
 * This is a special MediaFile, namely an image.
 * As a content in getCachedMediaContent() it delivers an Image-Object which will be loaded in background
 * It allows access on the Metadata (Exif, ITPC-Tags, ...)
 * <p/>
 *
 * @Author: Ingo
 * @Date: 28.08.12
 * @modified: 2014-06-05 java.io operations changed into java.nio
 */
public class ImageFile extends MediaFileTagged {

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
    return 0; //as default, ie. if error occured or PixelFormat is unknown
  }

  @Override
  public long getContentApproxMemSize() {
    Image currentImage = (Image) content;

    //the "guess"
    return new Double(currentImage.getHeight() * currentImage.getWidth() * getBytesPerPixel()).longValue();
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

}
