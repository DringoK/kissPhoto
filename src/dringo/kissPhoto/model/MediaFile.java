package dringo.kissPhoto.model;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.model.Metadata.MetaInfoProperty;
import dringo.kissPhoto.view.inputFields.SeparatorInputField;
import dringo.kissPhoto.view.mediaViewers.PhotoViewer;
import dringo.kissPhoto.helper.AppStarter;
import dringo.kissPhoto.helper.StringHelper;
import dringo.kissPhoto.view.mediaViewers.MediaViewer;
import dringo.kissPhoto.view.mediaViewers.PlayerViewerVLCJ;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * <p>
 * MediaFile is a file-object displayed and edited in the FileTableView of kissPhoto
 * It is a wrapper around a file which is tried to be interpreted as a media file (photo, movie, ...)
 * It defines all functions for parsing the filename, renaming, changing timestamp, EXIF, ...
 * </p>
 * <p>Note:
 * Changes to MediaFiles are not directly written to the disk before "saving" is executed by calling the "perform..." methods.
 * </p>
 *
 * @author ikreuz
 * @since 2012-08-28
 * @version 2022-10-15 retry strategy corrected: no more infinite retries (retries used currently for images in PhotoViewer only)
 * @version 2022-01-07 meta info writing supported. performDelete() and moveFileToDeleted() separated, so that backup files before transformations become possible
 * @version 2021-11-07 metainfo column support (="" if not MediaFileTagged), reflection for FileTableView eliminated
 * @version 2021-04-07 metadata stuff (cache!) now completely in subclass MediaFileTagged
 * @version 2020-12-20 cache now directly in this class. What to put into cache is asked from according viewer.
 * @version 2019-06-22 cache issue fixed: isMediaContentValid() and getMediaContentException() added
 * @version 2018-10-21 support rotation in general (for subclasses which provide a MediaFileRotater sybling)
 * @version 2016-06-12 performDelete will now no longer delete but move to subfolder "deleted" (localized, e.g. german "aussortiert")
 * @version 2014-07-05 bug found why separator column didn't reflect changes in the property over the model: separatorProperty() had wrong capitalization
 * @version 2014-06-22 extra column for the counter's separator (the character after the counter)
 * @version 2014-06-21 BUG-Fix (important):  because of different behaviour of java.nio comp. to java.io PerformRename() has not performed SECONDRUN, but overwriting!!!
 * @version 2014-06-07 getContent interface to cache simplified, java.io operations changed into java.nio
 */
public abstract class MediaFile implements Comparable<MediaFile> {
  protected final static MediaCache mediaCache = new MediaCache(); //implements the Cache strategy for all MediaFiles

  public static final String PLACEHOLDER_PREFIX = "%p";
  public static final String PLACEHOLDER_COUNTER = "%c";
  public static final String PLACEHOLDER_SEPARATOR = "%s";
  public static final String PLACEHOLDER_DESCRIPTION = "%d";
  public static final String PLACEHOLDER_EXTENSION = "%e";
  public static final String PLACEHOLDER_DATE = "%m"; //modified date
  public static final String PLACEHOLDER_TIME = "%t";
  //order of the column-wise search in searchNext() and for interpreting searchRec.tableColumn;
  public static final int COL_PREFIX = 0;
  public static final int COL_COUNTER = 1;
  public static final int COL_SEPARATOR = 2;
  //public static final int COL_DESCRIPTION = 3; //never used, because always treated as the default
  public static final int COL_EXTENSION = 4;
  public static final int COL_FILEDATE = 5;

  public final static int MAX_LOAD_RETRIES = 3;
  public static final int SUCCESSFUL = 0;
  public static final int SECOND_RUN = 1;
  public static final int RENAME_ERROR = 2;

  //this constants are used by sibling classes for ids in globalSettings (together with their class.getSimpleName())
  protected final static String MAIN_EDITOR = "_mainEditor";
  protected final static String SECOND_EDITOR = "_2ndEditor";
  //helpers
  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN);

  /**
   * status is a single character representing the most important boolean error flag of this File
   * e.g. "C" =conflicting name
   * "" = no error
   */
  public static final String STATUSFLAGS_HELPTEXT = KissPhoto.language.getString("statusFlags.Helptext");
  protected Path fileOnDisk;   //including physical filename on Disk...to be renamed
  protected MediaFileList mediaFileList; //every list element knows about its list: Access counterPosition and for future use (e.g. support dirTree)
  protected Object content = null;            //cached content

  /**
   * Constants for rotating (lossless if possible) media.
   * At least the rotation can be performed on screen.
   * A subtype of MediaFile can save the rotation.
   * E.g. MediaFileTaggedEditable saves the rotation lossless using MediaUtil library
   */
  public enum RotateOperation{
    ROTATE0, ROTATE90, ROTATE180, ROTATE270
  } //clockwise

  //every file is rotatable but currently only ImageFiles provide an implementation for saving the rotation
  //planned operation when saved next time: first rotate then flip vertical then horizontal!!!
  protected RotateOperation rotateOperation = RotateOperation.ROTATE0;
  protected boolean flipHorizontally = false;
  protected boolean flipVertically = false;

  //prevent from infinite loop if (background) loading fails permanently (currently supported by ImageFile Background loading
  protected int loadRetryCounter = 0;
  public final StringProperty status = new SimpleStringProperty();

  //parsed Filename, editable by user
  public final StringProperty prefix = new SimpleStringProperty();
  public final StringProperty counter = new SimpleStringProperty();
  public final StringProperty separator = new SimpleStringProperty();
  public final StringProperty description = new SimpleStringProperty();
  public final StringProperty extension = new SimpleStringProperty();
  public final StringProperty modifiedDate = new SimpleStringProperty();

  //errors regarding the filename (to be shown on GUI)
  private boolean renameError = false;   //indicate that the last rename was not successful
  private boolean timeStampWriteError = false; //indicates that the last try to write the timestamp was not successful
  private boolean filenameChanged = false; //if true the physical name of the file needs to be written to disk
  private boolean timeStampChanged = false; //if true the time stamp needs to be written to disk

  /**
   * @param file          the file that will be wrapped by this class or its subclasses
   *                      constructor generates an internal File object
   *                      and stores a parsed editable copy of the filename internally
   * @param mediaFileList double linked: link to the mediaFileList where the mediaFile resides in
   */
  public MediaFile(Path file, MediaFileList mediaFileList) {
    this.mediaFileList = mediaFileList;
    this.fileOnDisk = file;

    parseFilename(fileOnDisk.getFileName().toString());

    try {
      this.modifiedDate.set(dateFormatter.format(new Date(Files.getLastModifiedTime(fileOnDisk).toMillis())));
    } catch (IOException e) {
      //if date cannot be accessed nothing will displayed as the date
      this.modifiedDate.set("");
    }
  }

  /**
   * Factory method for creating a matching subclass of MediaFile
   * supported MediaPlayer FileTypes are documented in JavaFX: Package javafx.scene.media
   * http://docs.oracle.com/javafx/2/api/javafx/scene/media/package-summary.html#SupportedMediaTypes
   *
   * @param file       to be investigated and wrapped by MediaFile
   * @param parentList every MediaFile knows about the list it is contained in
   * @return subclass of MediaFile
   */
  public static MediaFile createMediaFile(Path file, MediaFileList parentList) {
    //create specialized MediaFile-object based on extension
    if (PhotoViewer.willAccept(file)){
      return new ImageFile(file, parentList);
    } else if (PlayerViewerVLCJ.willAccept(file)){
        return new PlayableFile(file, parentList);
    } else {
      return new OtherFile(file, parentList);
    }
  }

  /**
   * return all field names of a media file as a CSV (comma separated) string</br>
   * the string is terminated with \n</br>
   * this can be used as the headline of a csv-export where the lines are written using the toCSVString method</br>
   *
   * @return CSV strings with all fields of MediaFile
   */
  public static String getCSVHeadline() {
    final char sep = StringHelper.getLocaleCSVSeparator();  //just a shortcut

    return MessageFormat.format(KissPhoto.language.getString("csv_Headline"), sep, sep, sep, sep, sep, sep, sep);
  }

  /**
   * execute the external editor specified for the subclass of MediaFile
   * If the specified external editor is invalid nothing will happen
   * If the selection is empty or null nothing will happen
   *
   * @param selection  the list of MediaFiles currently selected will be passed as parameters
   * @param editorPath string holding the path to the external editor. If invalid (null, empty, nofile..),
   *                   in windows cmd /c is used to start the standard app for the file type otherwise nothing will happen
   */
  public static void executeExternalEditor(ObservableList<MediaFile> selection, String editorPath) {
    if (selection != null && selection.size() > 0) {
      AppStarter.exec(editorPath, selection);
    }
  }

  /**
   * parse filename and store it into the 4 parsed Filename Properties (prefix, counter, description, extension)
   *
   * @param filename: the string to be parsed
   */
  private void parseFilename(String filename) {
    //parse extension: search for last dot in the filename
    int extPos = filename.lastIndexOf(".");

    String pureFilename;  //i.e. without extension
    if (extPos > 0) {
      extension.set(filename.substring(extPos));  //including the dot!!!
      pureFilename = filename.substring(0, extPos);
    } else {
      extension.set("");
      pureFilename = filename;
    }

    //parse rest of "pure" filename
    //find nth number as a separator
    int i = 0; //run through filename string
    int found = 0; //count numbers found
    int numberPosStart = 0;
    int numberPosEnd = 0;
    while ((i < pureFilename.length()) && (found < mediaFileList.getCounterPosition())) {
      //find begin of next number
      while ((i < pureFilename.length()) && (!Character.isDigit(filename.charAt(i)))) {
        i++;
      }
      if (i < pureFilename.length()) { //i.e. if finding was the reason of the end of the above while loop
        found++;
        numberPosStart = i;

        //find end of number
        while ((i < pureFilename.length()) && (Character.isDigit(pureFilename.charAt(i)))) {
          i++;
        }
        numberPosEnd = i; //same for end of string or non-digit found: this is the position after the end digit of the number (substring() expects this)
      }
    }
    if (found >= mediaFileList.getCounterPosition()) {
      prefix.set(pureFilename.substring(0, numberPosStart));
      counter.set(pureFilename.substring(numberPosStart, numberPosEnd));
      description.set(pureFilename.substring(numberPosEnd));
    } else {
      //if not enough numbers have been found: the complete filename is treated as the description
      prefix.set("");
      counter.set("");
      description.set(pureFilename);
    }

    //if there is only a prefix (no counter or description), than show the filename as description
    if (description.get().isEmpty() && counter.get().isEmpty()) {
      description.set(prefix.get());
      prefix.set("");
    }

    //put separator character in extra column
    if (!description.get().isEmpty()) {
      String startChar = description.get().substring(0, 1);
      if (SeparatorInputField.SEPARATOR_CHARS.contains(startChar)) {
        separator.set(startChar);
        description.set(description.get().substring(1));
      } else {
        separator.set("");
      }
    } else {
      separator.set("");
    }
  }

  /**
   * metaInfoColumn.setCellValueFactory calls this method everytime it tries to update cell-content in this column
   * default implementation for MediaFiles which are not implementations of MediaFileTagged i.e. nothing to be shown in that column
   * MediaFileTagged will override this method
   * @param metaInfoColumnPath the path to the exif-tag as a string-list that should be shown (no effect when MediaFile is not tagged)
   * @return null
   */
  public MetaInfoProperty getMetaInfo(ObservableStringList metaInfoColumnPath){
    return null;
  }

  /**
   * Convert to a comma separated value string with a line break in the end
   * the
   */
  public String toCSVString() {
    final char sep = StringHelper.getLocaleCSVSeparator();  //just a shortcut

    return "\"" + getFileOnDiskPath() + "\"" + sep + "\"" + getFileOnDisk().getFileName() + "\"" + sep +
      "\"" + getPrefix() + "\"" + sep +
      "\"" + getCounter() + "\"" + sep +
      "\"" + getSeparator() + "\"" + sep +
      "\"" + getDescription() + "\"" + sep +
      "\"" + getExtension() + "\"" + sep +
      "\"" + getModifiedDate() + "\"\n";
  }

  /**
   * Change the filename (prefix, separator, description, extension) just in memory
   * i.e. the physical file is left unchanged (until next save = performRename())
   * The String-parameters may contain the PLACEHOLDER constants which are replaced by the old values of the file
   * prior to replacing prefix, description and extension
   *
   * @param changePrefix      true if the prefix shall be changed (checkbox in GUI)
   * @param newPrefix         new value
   * @param changeSeparator   true if separator shall be changed (checkbox in GUI)
   * @param newSeparator      new value
   * @param changeDescription true if description shall be changed (checkbox in GUI)
   * @param newDescription    new value
   * @param changeExtension   true if extension shall be changed (checkbox in GUI)
   * @param newExtension      new value
   */
  public void rename(boolean changePrefix, String newPrefix,
                     boolean changeSeparator, String newSeparator,
                     boolean changeDescription, String newDescription,
                     boolean changeExtension, String newExtension) {

    //replace in memory (members)
    if (changePrefix) setPrefix(replaceRenamePlaceholders(newPrefix));
    if (changeSeparator)
      setSeparator(newSeparator);  //note: single character does not support replacement (in GUI combo box) and might be null!
    if (changeDescription) setDescription(replaceRenamePlaceholders(newDescription));
    if (changeExtension) setExtension(replaceRenamePlaceholders(newExtension));
  }

  /**
   * helper for renaming: a copy of the string is returned where the rename placeholders (%p, %c, %d ...)
   * are replaced by the current values (before renaming) of prefix, separator, counter, description, ...
   *
   * @param withPlaceholders the rename string with placeholders
   * @return the rename string with placeholders replaced
   */
  private String replaceRenamePlaceholders(String withPlaceholders) {
    String replaced = withPlaceholders;
    replaced = replaced.replace(PLACEHOLDER_PREFIX, getPrefix());
    replaced = replaced.replace(PLACEHOLDER_COUNTER, getCounter());
    replaced = replaced.replace(PLACEHOLDER_SEPARATOR, getSeparator());
    replaced = replaced.replace(PLACEHOLDER_DESCRIPTION, getDescription());
    replaced = replaced.replace(PLACEHOLDER_EXTENSION, getExtension());

    replaced = replaced.replace(PLACEHOLDER_DATE, getModifiedDateOnly());
    replaced = replaced.replace(PLACEHOLDER_TIME, getModifiedTimeOnly());

    return replaced;
  }

  /**
   * helper function for MediaFileList.searchNext:
   * search the next occurrence of searchString in the current MediaFile fields
   * starting from the cursor position of the searchRec structure
   * the modalResult is stored in the searchRec structure and additionally true is returned if found
   *
   * @param searchRec cursors and modalResult are used as staring point and will be updated here: startPos, endPos, found
   * @return true if found, false if not (same as searchRec.found)
   */
  public boolean searchNext(String searchText, MediaFileList.SearchRec searchRec) {
    String textToSearchIn;
    boolean found = false;
    int foundPos = 0;

    while (!found && searchRec.tableColumn <= COL_FILEDATE) {

      textToSearchIn = getStringPropertyForColNumber(searchRec.tableColumn).get();

      //search is not case sensitive (toUpperCase is compared)
      foundPos = textToSearchIn.toLowerCase().indexOf(searchText.toLowerCase(), searchRec.endPos);
      found = (foundPos >= 0);

      if (!found) searchRec.tableColumn++;  //ggf. in nächster Spalte weitersuchen
      //always start the search in the next column from the beginning
      searchRec.startPos = 0;
      searchRec.endPos = 0;
    }

    if (found) {
      searchRec.startPos = foundPos;
      searchRec.endPos = foundPos + searchText.length();
      searchRec.foundMediaFile = this;
    }

    searchRec.found = found;
    return found;
  }

  /**
   * helper for selecting the correct media file's field according to the table info in searchRec
   *
   * @param colNumber the column number
   * @return the StringProperty that
   */
  public StringProperty getStringPropertyForColNumber(int colNumber) {
    return switch (colNumber) {
      case COL_PREFIX -> prefix;
      case COL_COUNTER -> counter;
      case COL_SEPARATOR -> separator;
      //case COL_NO_DESCRIPTION -> description; //same as default
      case COL_EXTENSION -> extension;
      case COL_FILEDATE -> modifiedDate;
      default -> description;
    };
  }

  /**
   * the part of the string that is described by searchRec (column, startPos, endPos)
   * is replaced by 'replaceText'
   *
   * @param replaceText the text that is used for replacing
   * @param searchRec   the position that is to be replaced
   */
  public void replaceAccordingSearchRec(String replaceText, MediaFileList.SearchRec searchRec) {
    if (searchRec.found) {  //replace only, if Cursors are valid
      StringProperty currentField = getStringPropertyForColNumber(searchRec.tableColumn);
      String currentText = currentField.get();

      currentField.setValue(currentText.substring(0, searchRec.startPos) +
        replaceText +
        currentText.substring(searchRec.endPos));

      searchRec.endPos = searchRec.startPos + replaceText.length();  //correct the new endPos for further searching

      if (searchRec.tableColumn == COL_FILEDATE)
        setTimeStampChanged(true);
      else
        setFilenameChanged(true);
    }

  }

  /**
   * Extract filepath from getAbsolutePath.
   *
   * @return The filepath of the wrapped file in Media file (i.e. without name and extension)
   * Note: the path includes no ending File.separator (i.e. / or \)
   */
  public String getFileOnDiskPath() {
    try {
      return fileOnDisk.toRealPath().getParent().toString();
    } catch (IOException e) {
      return fileOnDisk.getParent().toString(); //if this is not a real path the return the syntactic version only of the filepath
    }
  }

  /**
   * Extract filename from getAbsolutePath.
   *
   * @return The filename of the wrapped file in Media file (i.e. only name and extension)
   */
  public String getFileOnDiskName() {
    return fileOnDisk.getFileName().toString();
  }

  /**
   * concatenate the parsed Filename again
   * This method is used be 'performRename'
   * and can be used for previewing the name
   * Note: this is the name only without the path
   *
   * @return the string of (edited) concatenated filename parts
   */

  public String getResultingFilename() {
    return prefix.get() + counter.get() + separator.get() + description.get() + extension.get();
  }

  /**
   * if a rename has failed the reason can be a renumbering:
   * if all file names are same except for the number the filename conflicts temporarily
   * For this use-case an intermediate filename is tried before the final rename is completed in a second run
   * <p/>
   * The returned filename is not yet existing in the current directory. This is performed by
   * adding a number. The number is increased until the filename is new, i.e. does not exist yet
   *
   * @param aFile Path to a file or directory that should be unique in its directory
   * @return a non-existing filename similar to the given filename but additionally with ".kiss" (+number if necessary) before the extension
   */
  private String generateUniqueFilename(Path aFile) {
    String filename = prefix.get() + counter.get() + separator.get() + description.get() + extension.get(); //first try without number
    int i = 1;
    while (Files.exists((aFile.resolveSibling(filename)))) {
      filename = prefix.get() + counter.get() + separator.get() + description.get() + "-" + i + extension.get();
      i++;
    }
    return filename;
  }

  private int performRename() {
    int result;

    try {
      Path newFile = getFileOnDisk().resolveSibling(getResultingFilename());
      if (Files.exists(newFile)) {
        newFile = getFileOnDisk().resolveSibling(generateUniqueFilename(getFileOnDisk()));
        setRenameError(true); //this is temporarily a rename error
        result = SECOND_RUN;
      } else {
        setFilenameChanged(false);
        setRenameError(false);
        result = SUCCESSFUL;
      }

      Files.move(fileOnDisk, newFile);
      fileOnDisk = newFile;

    } catch (Exception e) {    //can be file access denied or write protect or IO Error or...
      setFilenameChanged(true);
      setRenameError(true);    //mark that an error has occurred
      result = RENAME_ERROR;
    }
    return result;
  }

  /**
   * the changes made in the time stamp property are applied to the disk
   * for both: creation time and modified time
   *
   * @return true if successful
   */
  private boolean performSetTimeStamp() {
    try {
      FileTime newTimeStamp = FileTime.fromMillis(dateFormatter.parse(modifiedDate.get()).getTime());
      Files.setLastModifiedTime(fileOnDisk, newTimeStamp);
      Files.setAttribute(fileOnDisk, "basic:creationTime", newTimeStamp, LinkOption.NOFOLLOW_LINKS);

      //successful
      setTimeStampChanged(false);
      setTimeStampWriteError(false);
      return true;
    } catch (Exception e) {
      setTimeStampWriteError(true);
      return false;
    }
  }

  /**
   * "Delete" the file
   * The files are not really deleted but moved into a subfolder "deleted"  (internationalized i.e. in german "aussortiert")
   * If a file with a given name is existing already in "deleted" the name is made unique by generateUniqueFilename()
   *
   * @return true if successful
   */
  public boolean performDeleteFile() {
    boolean successful = moveFileToDeleted() != null;
    if (!successful) setRenameError(true);
    return successful;
  }

  /**
   * move the file to the deleted folder (internationalized i.e. in german "aussortiert")
   * If a file with a given name is existing already in "deleted" the name is made unique by generateUniqueFilename()
   *
   * @return the path to the file in the new location (and possibly with new name) or null if an error occurred
   */
  public Path moveFileToDeleted(){
    Path deletePath = fileOnDisk.resolveSibling(KissPhoto.language.getString("deletedSubDir")); //delete subfolder is sibling to file
    Path deletedFile = deletePath.resolve(fileOnDisk.getFileName()); //append Filename to get target filename for deleted file

    try {
      //create deleted subfolder if not existing
      if (Files.notExists(deletePath))
        Files.createDirectory(deletePath);

      //generate unique target filename
      Path uniqueDeletedFile = deletePath.resolve(generateUniqueFilename(deletedFile));

      //move the file to deleted subfolder
      Files.move(fileOnDisk, uniqueDeletedFile);


      //successful
      return uniqueDeletedFile;

    } catch (IOException e) {
      return null;
    }
  }

  /**
   * the changes made in the parsed filename properties are applied to the disk
   * <p/>
   * if the rename fails the rename is tried again with a temp name and the flag "Physical Rename Error"
   * As soon as all other files have been renamed it can be tried again to rename the file
   * reason: e.g. when changing the order of two files with the same name (but different number) none of the files
   * can be renamed first. The intermediate filename for the first file enables renaming of the second. In a second run
   * the intermediate name can now be renamed into the wanted name.
   * (MediaFileList implements a strategy in save-method with "two runs")
   *
   * @return <ul>
   *   <li>SaveResult.SUCCESSFUL if successful</li>
   *   <li>SaveResult.NEEDS_2ND_TRY if an intermediate filename has been given and a second run is necessary</li>
   *   <li>SaveResult.ERROR if another error has occurred (e.g. write protect/access denied etc)</li>
   * </ul>
   */
  public SaveResult saveChanges() {
    boolean secondTryNecessary = false;
    boolean successful = true;

    //note transformations and meta tags are saved in MediaFileTaggedEditable.saveChanges()

    //rename
    if (isFilenameChanged()) {
      int renameResult = performRename();
      if (renameResult == MediaFile.SECOND_RUN) { //if renaming was not successful it has been renamed into an intermediate name
        secondTryNecessary = true;                //in a second loop this can be resolved
      } else if (renameResult == MediaFile.RENAME_ERROR) {
        successful = false;
      }
    }
    //time-stamp
    if (isTimeStampChanged()) {
      if (!performSetTimeStamp()) {
        successful = false;
      }
    }

    if (secondTryNecessary) //second trial has priority to keep consistence
      return SaveResult.NEEDS_2ND_TRY;
    else if (!successful)
      return SaveResult.ERROR;
    else
      return SaveResult.SUCCESSFUL;
  }

  public String statusFlagsToString() {
    //status in first line has highest priority
    if (renameError) {
      return "R";
    }
    if (timeStampWriteError) {
      return "T";
    }
    if (isChanged()) {
      return "*";
    }
    return ""; //= nothing to report
  }

  /**
   * keep property synchronous with the boolean flags
   * to display it in the table
   */
  public void updateStatusProperty() {
    statusProperty().set(statusFlagsToString());
  }

  //Default implementation for cancelling any background loading
  public void cancelBackgroundLoading() {
    //do nothing as default
    //overwritten e.g. in ImageFile
  }

  /**
   * @return true if Media Content is valid, false if not loaded or Exception occurred while loading
   */
  public boolean isMediaContentInValid() {
    return ((content == null) || (getMediaContentException() != null));
  }

  /**
   * Non abstract Media-Types overwrite this method
   * to get any exception that occured while loading.
   * A content is valid if:  (content != null) && (getMediaContentException() == null)
   *
   * @return null if no exception has occurred or content empty, anException if error occurred while loading
   */
  public abstract Exception getMediaContentException();

  //-------------------- Cache support -------------
   /**
   * try to load MediaContent from Cache. If not possible load it with specific load-routine and put it into cache
   * @param mediaViewer the mediaView which is asked to (pre) load the file in its specific format
   * @return MediaContent or null if this was not possible
   */
  public Object getMediaContentCached(MediaViewer mediaViewer) {
    return tryOrRetryMediaContentCached(mediaViewer, false);
  }

  /**
   * try or retry to load MediaContent from Cache. If not possible load it with specific load-routine and put it into cache
   * @param mediaViewer the mediaView which is asked to (pre) load the file in its specific format
   * @param isNotRetry call it with true if you try first. recursive retries will call it with false to count up retry-Counter
   * @return MediaContent or null if this was not possible
   */
  public Object tryOrRetryMediaContentCached(MediaViewer mediaViewer, boolean isNotRetry) {
    if (isNotRetry)
      resetLoadRetryCounter();
    //if Retry then counter still needed (will be counted in shouldRetryLoad() that must be called before trying to retry)

    if (isMediaContentInValid()){
      //if not in cache then ask the viewer to load it
      mediaCache.maintainCacheSizeByFlushingOldest(); //housekeeping before load for having enough memory to laod
      content = mediaViewer.getViewerSpecificMediaContent(this);
    }

    if (content != null)
      mediaCache.addAsLatest(this);//and remember that it is now in memory and the youngest entry of the cache

    return content;
  }

  public int getLoadRetryCounter() {
    return loadRetryCounter;
  }

  public boolean shouldRetryLoad(){
    loadRetryCounter++;
    System.out.println("MediaFile.shouldRetryLoad: retryCounter="+loadRetryCounter + " for " + fileOnDisk.getFileName());
    return loadRetryCounter < MAX_LOAD_RETRIES;
  }

  public void resetLoadRetryCounter(){
    //System.out.println("MediaFile.resetLoadRetryCounter");
    loadRetryCounter = 0;
  }

  /**
   * invalidate content cache for this mediaFile and free memory
   * remove entry from cache list
   */
  public void flushFromCache(){
    flushMediaContent();
    mediaCache.flush(this);
  }

  /**
   * Flush the media content to free memory
   * don't forget to clear it in the cache also (or use flushFromCache instead)
   */
  public void flushMediaContent() {
    content = null;
  }

  public static void flushAllMediaFromCache(){
    mediaCache.flushAll();
  }
  /*
   * --------------------- flag getters and setters---------------------
   * write flags with these setters/getters to keep the status property up to date
   * internally reading can be done directly
   * write access is private, read access is public
   */

  public abstract ReadOnlyDoubleProperty getContentProgressProperty();

  /**
   * For maintenance of the MediaCache a guess is necessary how much memory becomes available if the MediaFile is removed from cache and memory
   *
   * @return approx size in bytes of the mediaContent
   */
  public abstract long getContentApproxMemSize();

  /*
   * --------------------- getters and setters---------------------
   */

  @Override
  public String toString() {
    return getResultingFilename();
  }

  private void setRenameError(boolean value) {
    renameError = value;
    updateStatusProperty();
  }

  private void setTimeStampWriteError(boolean value) {
    timeStampWriteError = value;
    updateStatusProperty();
  }

  public boolean isChanged() {
    return (filenameChanged || timeStampChanged || isTransformed());
  }

  public boolean isFilenameChanged() {
    return filenameChanged;
  }


  private void setFilenameChanged(boolean value) {
    filenameChanged = value;
    updateStatusProperty();
  }

  /**
   *
   * @return a text that describes the user, what has been changed
   */
  public String getChangesText(){
    String s = "";
    if (filenameChanged) s += MessageFormat.format(KissPhoto.language.getString("rename.0.1"), getFileOnDiskName(),getResultingFilename());


    if (timeStampChanged){
      if (s.length()>0) s+= "\n";
      s += KissPhoto.language.getString("time.stamp.changed");
    }

    if (isTransformed()){
      if (s.length()>0) s+= "\n";
      if (isRotated()) {
        if (isFlippedHorizontally() || isFlippedVertically())
          s+= KissPhoto.language.getString("image.rotated.and.flipped");
        else
          s+= KissPhoto.language.getString("image.rotated");
      }else{
        if (isFlippedHorizontally() || isFlippedVertically())
          s+= KissPhoto.language.getString("image.flipped");
      }
    }
    return s;
  }

  /*
   * --------------------- TableView getters and setters---------------------
   */

  public boolean isTimeStampChanged() {
    return timeStampChanged;
  }

  protected void setTimeStampChanged(boolean value) {
    timeStampChanged = value;
    updateStatusProperty();
  }

  public Path getFileOnDisk() {
    return fileOnDisk;
  }

  /**
   * status is the one char indicator that encodes the status flags (see statusFlagsToString() )
   *
   * @return status
   */
  public StringProperty statusProperty() { //TableView binds the property over it's name during runtime with that method
    return status;
  }

  public String getStatus() {
    return status.get();
  }

  public String getPrefix() {
    return prefix.get();
  }

  /**
   * prefix is the constant part of the files in the directory before the counter
   *
   * @param prefix new value
   */
  public void setPrefix(String prefix) {
    if (!prefix.equals(this.prefix.get())) {
      this.prefix.set(prefix);
      setFilenameChanged(true);
    }
  }

  public String getCounter() {
    return counter.get();
  }

  /**
   * The counter as a string
   *
   * @param counter new value
   */
  public void setCounter(String counter) {
    if (!counter.equals(this.counter.get())) {
      this.counter.set(counter);
      setFilenameChanged(true);
    }
  }

  public int getCounterValue() {
    try {
      return Integer.parseInt(getCounter());
    } catch (Exception e) { //if numbering is empty an exception has to be caught
      return 0;
    }
  }

  public String getSeparator() {
    return separator.get();
  }

  /**
   * The character of the filename after the counter is interpreted as the separator if it is one of the chars in SEPARATOR_CHARS
   *
   * @param newSeparator new value
   */
  public void setSeparator(String newSeparator) {
    if (!newSeparator.equals(this.separator.get())) {
      this.separator.set(newSeparator);
      setFilenameChanged(true);
    }
  }
  public String getDescription() {
    return description.get();
  }

  /**
   * The part of the filename after the counter and separator can be used to describe the media
   *
   * @param description new value
   */
  public void setDescription(String description) {
    if (!description.equals(this.description.get())) {
      this.description.set(description);
      setFilenameChanged(true);
    }
  }

  public String getExtension() {
    return extension.get();
  }

  /**
   * file extension includes the dot (.)
   *
   * @param extension new value
   */
  public void setExtension(String extension) {
    if (!extension.equals(this.extension.get())) {
      this.extension.set(extension);
      setFilenameChanged(true);
    }
  }

  public String getModifiedDate() {
    return modifiedDate.get();
  }

  public void setModifiedDate(String modifiedDate) {
    if(!modifiedDate.equals(this.modifiedDate.get())) {
      this.modifiedDate.set(modifiedDate);
      setTimeStampChanged(true);
    }
  }
  /*
   * --------------------- Comparable Interface ---------------------
   * ..sorts for resulting filename (e.g. in undelete dialog)
   */

  public String getModifiedDateOnly() {
    int i = modifiedDate.get().indexOf(" "); //all before space is date
    if (i >= 0) {
      return modifiedDate.get().substring(0, i);
    } else {
      return modifiedDate.get();  //if date is malformed then return it completely
    }
  }

  /*
   * --------------------- Factories ---------------------
   */

  public String getModifiedTimeOnly() {
    int i = modifiedDate.get().indexOf(" "); //all after space is time
    if (i >= 0) {
      return modifiedDate.get().substring(i + 1);
    } else {
      return modifiedDate.get();  //if date is malformed then return it completely
    }
  }

  /*
   *--------------------------------------------- Rotation stuff --------------------------------
   */

  @Override
  public int compareTo(MediaFile o) {
    return getResultingFilename().compareTo(o.getResultingFilename());
  }

  /**
   * is the resulting Filename the same when ignoring the extension (ignore Case)
   *
   * @param otherFile the file to compare with
   * @return true if the resulting filname is the same when ignoring the extension
   */
  public boolean isSameResultingNameButExtension(MediaFile otherFile) {
    return getPrefix().equalsIgnoreCase(otherFile.getPrefix()) &&
      getCounter().equalsIgnoreCase(otherFile.getCounter()) &&
      getSeparator().equalsIgnoreCase(otherFile.getSeparator()) &&
      getDescription().equalsIgnoreCase(otherFile.getDescription());
  }

  /**
   * if the subclass of MediaFile can save the rotation or flipping changes to disk during specialized saveChanges() then
   *
   * @return true else false
   */
  public boolean canTransformInFile() {
    return false;  //standard is false. Can be overwritten in subclass is rotating is supported
  }

  /**
   * orientation is changed if the resulting rotation since last save() is not 0°
   *
   * @return if orientation has change since last save()
   */
  public boolean isOrientationChanged() {
    return rotateOperation != RotateOperation.ROTATE0;
  }


  /**
   * plan a rotation
   * the handed operation is added on previously planned rotation operation and then optimized
   * if canRotate==false then the rotation will not be saved later and a warning should be shown on GUI
   *
   * @param operation 90 degree-wise clockwise
   */
  public void rotate(RotateOperation operation) {
    boolean wasOrientationChanged = isOrientationChanged(); //remember state before transformation

    int rotation = (rotateOperation.ordinal() + operation.ordinal()) % 4; //modulo 4 because 360=90*4
    rotateOperation = RotateOperation.values()[rotation];

    //as rotation is performed first in saveChanges() when rotation changes orientation flipping V/H must be exchanged
    if (wasOrientationChanged != isOrientationChanged()) {
      boolean temp = flipHorizontally;
      flipHorizontally = flipVertically;
      flipVertically = temp;
    }
    if (canTransformInFile()) updateStatusProperty();

  }

  /**
   * plan a flip operation
   * the handed operation is added on previously planned flip operations
   * if canRotate==false then the flipping will not be saved later and a warning should be shown on GUI
   */
  public void flipHorizontally() {
    flipHorizontally = !flipHorizontally;
    if (canTransformInFile()) updateStatusProperty();
  }

  /**
   * plan a flip operation
   * the handed operation is added on previously planned flip operations
   * if canRotate==false then the flipping will not be saved later and a warning should be shown on GUI
   */
  public void flipVertically() {
    if (canTransformInFile()) { //only if the specific MediaFile type supports rotation
      flipVertically = !flipVertically;
      updateStatusProperty();
    }
  }

  /**
   * reset all planned transformations in the model
   * e.g. after they have been written to disk
   */
  public void resetTransformations(){
    rotateOperation = RotateOperation.ROTATE0;
    flipHorizontally = false;
    flipVertically = false;

    if (canTransformInFile()) updateStatusProperty();
  }

  public RotateOperation getRotateOperation() {
    return rotateOperation;
  }

  public boolean isRotated() {
    return rotateOperation != RotateOperation.ROTATE0;
  }

  public boolean isFlippedHorizontally() {
    return flipHorizontally;
  }

  /*
   *------------------------------------------------- Logic for External Editors --------------------------------
   */
  //----- this part is same for all subclasses of MediaFile

  public boolean isFlippedVertically() {
    return flipVertically;
  }

  public boolean isTransformed() {
    return isRotated() || isFlippedHorizontally() || isFlippedVertically();
  }


  public enum SaveResult {ERROR, NEEDS_2ND_TRY, SUCCESSFUL}

}

