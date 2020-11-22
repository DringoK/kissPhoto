package de.kissphoto.model;

import de.kissphoto.KissPhoto;
import javafx.collections.ObservableList;

import java.nio.file.Path;

/**
 * MovieFiles are PlayableMediaFiles
 * Here just the External Editors are managed for this kind of files
 *
 * @Author ikreuz
 * @since 2012-08-28
 * @version 2020-11-19 globalSettings is now global (static in Kissphoto)
 * @version 2014-06-05 java.io operations changed into java.nio
 */
public class MovieFile extends MediaFilePlayable {

  public MovieFile(Path movieFile, MediaFileList parent) {
    super(movieFile, parent);
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
      MovieFile.externalMainEditorPath = KissPhoto.globalSettings.getProperty(MovieFile.class.getSimpleName() + MAIN_EDITOR);
      MovieFile.external2ndEditorPath = KissPhoto.globalSettings.getProperty(MovieFile.class.getSimpleName() + SECOND_EDITOR);
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
      MovieFile.externalMainEditorPath = newExternalMainEditorPath;
    else
      MovieFile.externalMainEditorPath = "";

    if (newExternal2ndEditorPath != null)
      MovieFile.external2ndEditorPath = newExternal2ndEditorPath;
    else
      MovieFile.external2ndEditorPath = "";

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
        MediaFile.executeExternalEditor(selection, MovieFile.externalMainEditorPath);
      else
        MediaFile.executeExternalEditor(selection, MovieFile.external2ndEditorPath);
    }
  }

  @Override
  public MediaFile.SaveResult saveChanges() {
    //Media files with players need to flush their cache because media becomes invalid if underlying filename changes
    if (isFilenameChanged()) flushMediaContent();
    return super.saveChanges();
  }

  /**
   * save the external editor's pathnames to GlobalSettings
   */
  protected static void saveExternalEditorPaths() {
    if (MovieFile.externalMainEditorPath != null)
      KissPhoto.globalSettings.setProperty(MovieFile.class.getSimpleName() + MAIN_EDITOR, MovieFile.externalMainEditorPath);
    if (MovieFile.external2ndEditorPath != null)
      KissPhoto.globalSettings.setProperty(MovieFile.class.getSimpleName() + SECOND_EDITOR, MovieFile.external2ndEditorPath);
  }

  public static String getExternalMainEditorPath() {
    return MovieFile.externalMainEditorPath;
  }

  public static String getExternal2ndEditorPath() {
    return MovieFile.external2ndEditorPath;
  }

}
