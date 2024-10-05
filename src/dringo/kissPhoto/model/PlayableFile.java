package dringo.kissPhoto.model;

import dringo.kissPhoto.KissPhoto;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;

import java.nio.file.Path;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * MovieFiles are PlayableMediaFiles
 * Here just the External Editors are managed for this kind of files
 *
 * @author ikreuz
 * @since 2012-08-28
 * @version 2020-12-20 The current playerViewer decides now if a file is a movieFile and what to put into the cache
 * @version 2020-11-19 globalSettings is now global (static in Kissphoto)
 * @version 2014-06-05 java.io operations changed into java.nio
 */
public class PlayableFile extends MediaFileTagged {

  public PlayableFile(Path movieFile, MediaFileList parent) {
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
      PlayableFile.externalMainEditorPath = KissPhoto.globalSettings.getProperty(PlayableFile.class.getSimpleName() + MAIN_EDITOR);
      PlayableFile.external2ndEditorPath = KissPhoto.globalSettings.getProperty(PlayableFile.class.getSimpleName() + SECOND_EDITOR);
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
      PlayableFile.externalMainEditorPath = newExternalMainEditorPath;
    else
      PlayableFile.externalMainEditorPath = "";

    if (newExternal2ndEditorPath != null)
      PlayableFile.external2ndEditorPath = newExternal2ndEditorPath;
    else
      PlayableFile.external2ndEditorPath = "";

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
        executeExternalEditor(selection, PlayableFile.externalMainEditorPath);
      else
        executeExternalEditor(selection, PlayableFile.external2ndEditorPath);
    }
  }

  @Override
  public MediaFile.SaveResult saveChanges() {
    //Media files with players need to flush their cache because media becomes invalid if underlying filename changes
    if (isFilenameChanged()) flushFromCache();
    return super.saveChanges();
  }

  /**
   * save the external editor's pathnames to GlobalSettings
   */
  protected static void saveExternalEditorPaths() {
    if (PlayableFile.externalMainEditorPath != null)
      KissPhoto.globalSettings.setProperty(PlayableFile.class.getSimpleName() + MAIN_EDITOR, PlayableFile.externalMainEditorPath);
    if (PlayableFile.external2ndEditorPath != null)
      KissPhoto.globalSettings.setProperty(PlayableFile.class.getSimpleName() + SECOND_EDITOR, PlayableFile.external2ndEditorPath);
  }

  public static String getExternalMainEditorPath() {
    return PlayableFile.externalMainEditorPath;
  }

  public static String getExternal2ndEditorPath() {
    return PlayableFile.external2ndEditorPath;
  }


  @Override
  public long getContentApproxMemSize() {
    return 40000000; //40MB for a player (tried out)
  }

  @Override
  public ReadOnlyDoubleProperty getContentProgressProperty() {
    return null; //no progress available for playable media
  }

}
