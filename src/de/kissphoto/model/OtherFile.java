package de.kissphoto.model;

import de.kissphoto.KissPhoto;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;

import java.nio.file.Path;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>This Class wrappes all files that cannot be displayed/played.
 * Nevertheless the filenames can be changed
 * <p/>
 *
 * @author Ingo
 * @since 2012-09-02
 * @version 2020-11-19 globalSettings is now global (static in Kissphoto)
 * @version 2014-06-05 java.io operations changed into java.nio
 * @version 2019-06-22 mediaCache corrections: getMediaContentException() added
 */
public class OtherFile extends MediaFile {
  public OtherFile(Path otherFile, MediaFileList parent) {
    super(otherFile, parent);
  }

  @Override
  public Object getSpecificMediaContent() {
    return null;
  }

  @Override
  public Exception getMediaContentException() {
    return null;
  }

  @Override
  public long getContentApproxMemSize() {
    return 0;  //content of other file is never loaded, freeing from cache has no effect on memory consumption
  }

  @Override
  public ReadOnlyDoubleProperty getContentProgressProperty() {
    return null;
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
   */
  public static void loadExternalEditorPaths() {
    try {
      OtherFile.externalMainEditorPath = KissPhoto.globalSettings.getProperty(OtherFile.class.getSimpleName() + MAIN_EDITOR);
      OtherFile.external2ndEditorPath = KissPhoto.globalSettings.getProperty(OtherFile.class.getSimpleName() + SECOND_EDITOR);
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
    if (newExternalMainEditorPath != null)
      OtherFile.externalMainEditorPath = newExternalMainEditorPath;
    else
      OtherFile.externalMainEditorPath = "";

    if (newExternal2ndEditorPath != null)
      OtherFile.external2ndEditorPath = newExternal2ndEditorPath;
    else
      OtherFile.external2ndEditorPath = "";

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
        MediaFile.executeExternalEditor(selection, OtherFile.externalMainEditorPath);
      else
        MediaFile.executeExternalEditor(selection, OtherFile.external2ndEditorPath);
    }
  }

  /**
   * save the external editor's pathnames to GlobalSettings
   */
  protected static void saveExternalEditorPaths() {
    if (OtherFile.externalMainEditorPath != null)
      KissPhoto.globalSettings.setProperty(OtherFile.class.getSimpleName() + MAIN_EDITOR, OtherFile.externalMainEditorPath);
    if (OtherFile.external2ndEditorPath != null)
      KissPhoto.globalSettings.setProperty(OtherFile.class.getSimpleName() + SECOND_EDITOR, OtherFile.external2ndEditorPath);
  }

  public static String getExternalMainEditorPath() {
    return OtherFile.externalMainEditorPath;
  }

  public static String getExternal2ndEditorPath() {
    return OtherFile.external2ndEditorPath;
  }

}
