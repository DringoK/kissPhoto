package de.kissphoto.model;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import de.kissphoto.helper.AppStarter;
import de.kissphoto.helper.I18Support;
import de.kissphoto.helper.StringHelper;
import de.kissphoto.view.inputFields.SeparatorInputField;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * <p>
 * MediaFile is a file-object displayed and edited in the FileTableView of kissPhoto
 * It is a wrapper around a file which is tried to be interpreted as a media file (photo, movie, ...)
 * It defines all functions for parsing the filename, renaming, changing timestamp, EXIF, ...
 * </p>
 * <p>Note:
 * Changes to MediaFiles are not directly written to the disk before "saving" is executed by calling the "perform..." methods.
 * </p>
 *
 * @Author: ikreuz
 * @Date: 2012-08-28
 * @modified: 2014-06-05 java.io operations changed into java.nio
 * @modified: 2014-06-07 getContent interface to cache simplified
 * @modified: 2014-06-21 BUG-Fix (important):  because of different behaviour of java.nio comp. to java.io PerformRename() has not performed SECONDRUN, but overwriting!!!
 * @modified: 2014-06-22 extra column for the counter's separator (the character after the counter)
 * @modified: 2014-07-05 bug found why separator column didn't reflect changes in the property over the model: separatorProperty() had wrong capitalization
 * @modified: 2016-06-12 performDelete will now no longer delete but move to subfolder "deleted" (localized, e.g. german "aussortiert")
 * @modified: 2018-10-21 support rotation in general (for subclasses which provide a MediaFileRotater sybling)
 * @modified: 2019-06-22 cache issue fixed: isMediaContentValid() and getMediaContentException() added
 */
public abstract class MediaFile implements Comparable<MediaFile> {
  public static final String PLACEHOLDER_PREFIX = "%p";
  public static final String PLACEHOLDER_COUNTER = "%c";
  public static final String PLACEHOLDER_SEPARATOR = "%s";
  public static final String PLACEHOLDER_DESCRIPTION = "%d";
  public static final String PLACEHOLDER_EXTENSION = "%e";
  public static final String PLACEHOLDER_DATE = "%m"; //modified date
  public static final String PLACEHOLDER_TIME = "%t";
  //order of the column-wise search in searchNext() and for interpreting searchRec.tableColumn;
  public static final int COL_NO_PREFIX = 0;
  public static final int COL_NO_COUNTER = 1;
  public static final int COL_NO_SEPARATOR = 2;
  public static final int COL_NO_DESCRIPTION = 3;
  public static final int COL_NO_EXTENSION = 4;
  public static final int COL_NO_FILEDATE = 5;
  public final static int MAX_LOAD_RETRIES = 3;
  /**
   * the changes made in the parsed filename properties are applied to the disk
   * <p/>
   * if the rename fails the rename is tried again with a temp name and the flag "Physical Rename Error"
   * As soon as all other files have been renamed it can be tried again to rename the file
   * because: e.g. when changing the order of two files with the same name (but different number) none of the files
   * can be renamed first. The intermediate filename for the first file enables renaming of the second. In a second run
   * the intermediate name can now be renamed into the wanted name.
   * (MediaFileList implements a strategy in save-method with "two runs")
   *
   * @return SUCCESSFUL if successful
   * SECOND_RUN if an intermediate filename has been given and a second run is necessary
   * RENAME_ERROR if another error has occurred (e.g. write protect/access denied etc)
   */
  public static final int SUCCESSFUL = 0;
  public static final int SECOND_RUN = 1;
  public static final int RENAME_ERROR = 2;
  //this constants are used by sibling classes for ids in globalSettings (together with their class.getSimpleName())
  protected final static String MAIN_EDITOR = "_mainEditor";
  protected final static String SECOND_EDITOR = "_2ndEditor";
  //helpers
  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static ResourceBundle language = I18Support.languageBundle;
  /**
   * status is a single character representing the most important boolean error flag of this File
   * e.g. "C" =conflicting name
   * "" = no error
   */
  public static final String STATUSFLAGS_HELPTEXT = language.getString("statusFlags.Helptext");
  protected Path fileOnDisk;   //including physical filename on Disk...to be renamed
  protected MediaFileList mediaFileList; //every list element knows about its list: Access counterPosition and for future use (e.g. support dirTree)
  protected Object content = null;
  //planned operation when saved next time: first rotate then flip vertical then horizontal!!!
  protected MediaFileRotater.RotateOperation rotateOperation = MediaFileRotater.RotateOperation.ROTATE0;
  protected boolean flipHorizontally = false;
  protected boolean flipVertically = false;
  //prevent from infinite loop if (background) loading fails permanently (currently supported by ImageFile Background loading
  protected int loadRetryCounter = 0;
  private StringProperty status = new SimpleStringProperty();
  //parsed Filename, editable by user
  private StringProperty prefix = new SimpleStringProperty("");
  private StringProperty counter = new SimpleStringProperty("");
  private StringProperty separator = new SimpleStringProperty("");
  private StringProperty description = new SimpleStringProperty("");
  private StringProperty extension = new SimpleStringProperty("");
  private StringProperty modifiedDate = new SimpleStringProperty("");
  //errors regarding the filename (to be shown on GUI)
  private boolean conflictingName = false; //if true resulting name conflicts with other file in directory
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
   * return all field names of a media file as a CSV (comma separated) string</br>
   * the string is terminated with \n</br>
   * this can be used as the headline of a csv-export where the lines are written using the toCSVString method</br>
   *
   * @return CSV strings with all fields of MediaFile
   */
  public static String getCSVHeadline() {
    final char sep = StringHelper.getLocaleCSVSeparator();  //just a shortcut

    return MessageFormat.format(language.getString("csv_Headline"), sep, sep, sep, sep, sep, sep, sep);
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
    String filename = file.getFileName().toString().toLowerCase();
    if ((filename.endsWith(".jpg")) ||
      (filename.endsWith(".jpeg")) ||
      (filename.endsWith(".jp2")) ||
      (filename.endsWith(".png")) ||
      (filename.endsWith(".bmp")) ||
      (filename.endsWith(".gif")) ||
      (filename.endsWith(".tiff")) ||   //not yet supported by Java-Fx ImageView but useful for loading external editor (nothing will be displayed (black))
      (filename.endsWith(".tif")) ||    //not yet supported by Java-Fx ImageView but useful for loading external editor (nothing will be displayed (black))
      (filename.endsWith(".ico"))) {     //not yet supported by Java-Fx ImageView but useful for loading external editor (nothing will be displayed (black))

      return new ImageFile(file, parentList);
    } else {
      if ((filename.endsWith(".mp4")) ||  //MPEG-4 Part 14
        (filename.endsWith(".mts")) ||  //mmpg-4 Part 14 from Digi-Cam
        (filename.endsWith(".hls")) ||  //http live stream
        (filename.endsWith(".flv")) ||   //flash video
        (filename.endsWith(".fxm")) ||   //fx media
        //not yet supported by Java-Fx, but with VLC and useful for loading external editor (will not be played)
        (filename.endsWith(".mp2")) ||   //mpeg2
        (filename.endsWith(".vob")) ||   //video object = mpeg2 file on a dvd
        (filename.endsWith(".mov")) ||  //Apple's movie format

        //in this version audio is treated like video (black area is displayed, but sound is played)
        (filename.endsWith(".wav")) ||   //Waveform Audio Format
        (filename.endsWith(".aiff")) ||  //Audio Interchange File Format
        (filename.endsWith(".aif")) ||   //Audio Interchange File Format
        (filename.endsWith(".mp3")) ||   //MPEG-1, 2, 2.5 raw audio stream possibly with ID3 metadata v2.3 or v2.4
        (filename.endsWith(".m4a"))) {   //mp-4 audio only
        return new MovieFile(file, parentList);
      } else {
        return new OtherFile(file, parentList);
      }
    }
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
    String pureFilename = null;  //i.e. without extension

    //parse extension: search for last dot in the filename
    int extPos = filename.lastIndexOf(".");
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
   * Filetype
   *
   * @return
   */
  public FileType getMediaType() {
    try {
      FileInputStream fileInputStream = new FileInputStream(fileOnDisk.toFile());
      BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
      FileType fileType = FileTypeDetector.detectFileType(bufferedInputStream);
      return fileType;
    } catch (Exception e) {  //FileNotFoundException or IOException
      return FileType.Unknown;
    }

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

    while (!found && searchRec.tableColumn <= COL_NO_FILEDATE) {

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
    StringProperty stringProperty;
    switch (colNumber) {
      case COL_NO_PREFIX:
        stringProperty = prefix;
        break;
      case COL_NO_COUNTER:
        stringProperty = counter;
        break;
      case COL_NO_SEPARATOR:
        stringProperty = separator;
        break;
      case COL_NO_DESCRIPTION:
        stringProperty = description;
        break;
      case COL_NO_EXTENSION:
        stringProperty = extension;
        break;
      case COL_NO_FILEDATE:
        stringProperty = modifiedDate;
        break;
      default:
        stringProperty = description;
    }
    return stringProperty;
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

      if (searchRec.tableColumn == COL_NO_FILEDATE)
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
      filename = prefix.get() + counter.get() + separator.get() + description.get() + "-" + Integer.toString(i) + extension.get();
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
   * @return: true if successful
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
   * @return: true if successful
   */
  public boolean performDeleteFile() {
    Path deletePath = fileOnDisk.resolveSibling(language.getString("deletedSubDir")); //delete subfolder is sibling to file
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
      return true;

    } catch (IOException e) {
      setRenameError(true);
      return false;
    }
  }

  /**
   * tries to write all changes to disk
   *
   * @return SaveResult.ERROR if an error occured or SaveResult.NEEDS_2ND_TRY if an intermediate name has been used while renaming
   */
  public SaveResult saveChanges() {
    boolean secondTryNecessary = false;
    boolean successful = true;

    //transform
    if (isTransformed()) {
      successful = performTransformation();

      //above transformation has changed timestamp in filesystem
      setTimeStampChanged(true); //to reset to the previous timestamp if it was not already timeStampChanged
    }


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
      return SaveResult.ERROR;    //error has priority
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
    if (conflictingName) {
      return "C";
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
  protected void updateStatusProperty() {
    statusProperty().set(statusFlagsToString());
  }

  /**
   * Non abstract Media-Types overwrite this method to deliver their media content
   * At the same time they should keep the content in memory until flushContent is called;
   *
   * @return the media content which is wrapped by the media File, e.g. an Image if MediaFile is an ImageFile
   */
  public abstract Object getSpecificMediaContent();

  //Default implementation for cancelling any background loading
  public void cancelBackgroundLoading() {
    //do nothing as default
    //overwritten e.g. in ImageFile
  }

  /**
   * @return true if Media Content is valid, false if not loaded or Exception occurred while loading
   */
  public boolean isMediaContentValid() {
    return ((content != null) && (getMediaContentException() == null));
  }

  /**
   * Non abstract Media-Types overwrite this method
   * to get any exception that occured while loading.
   * A content is valid if:  (content != null) && (getMediaContentException() == null)
   *
   * @return null if no exception has occurred or content empty, anException if error occurred while loading
   */
  public abstract Exception getMediaContentException();

  public int getLoadRetryCounter() {
    return loadRetryCounter;
  }

  /**
   * try to load MediaContent from Cache. If not possible load it with specific load-routine
   *
   * @return MediaContent or null if this was not possible
   */
  public Object getMediaContent() {
    Object media = mediaFileList.getCachedMediaContent(this);
    if (media == null)
      getSpecificMediaContent();
    return media;
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

  /**
   * Flush the media content to free memory
   * This method is called from MediaCache which implements the cache strategy
   * and whenever the file changes(e.g. after rotating an Jpeg on disk)
   *
   * @return the memory size in bytes that will be free now (approximately)
   */
  public long flushMediaContent() {
    long memSize = getContentApproxMemSize();
    content = null; //delete  a f t e r  the memsize has been determined ;-)
    return memSize;
  }

  @Override
  public String toString() {
    return getResultingFilename();
  }

  public boolean isConflictingName() {
    return conflictingName;
  }

  private void setConflictingName(boolean value) {
    conflictingName = value;
    updateStatusProperty();
  }

  public boolean isRenameError() {
    return renameError;
  }

  private void setRenameError(boolean value) {
    renameError = value;
    updateStatusProperty();
  }

  public boolean isTimeStampWriteError() {
    return timeStampWriteError;
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

  /*
   * --------------------- getters and setters---------------------
   */

  private void setFilenameChanged(boolean value) {
    filenameChanged = value;
    updateStatusProperty();
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

  public StringProperty prefixProperty() { //TableView binds the property over it's name during runtime with that method
    return prefix;
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

  public StringProperty counterProperty() { //TableView binds the property over it's name during runtime with that method
    return counter;
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

  public StringProperty separatorProperty() { //TableView binds the property over it's name during runtime with that method
    return separator;
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

  public StringProperty descriptionProperty() {//TableView binds the property over it's name during runtime with that method
    return description;
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

  public StringProperty extensionProperty() { //TableView binds the property over it's name during runtime with that method
    return extension;
  }

  public String getModifiedDate() {
    return modifiedDate.get();
  }

  public void setModifiedDate(String modifiedDate) {
    if (!modifiedDate.equals(this.modifiedDate.get())) {
      this.modifiedDate.set(modifiedDate);
      setTimeStampChanged(true);
    }
  }
  /*
   * --------------------- Comparable Interface ---------------------
   * ..sorts for resulting filename (e.g. in undelete dialog)
   */

  public StringProperty fileDateProperty() {  //TableView binds the property over it's name during runtime with that method
    return modifiedDate;
  }

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
   * if the subclass of MediaFile provides a specific MediaFileRotater then
   *
   * @return true else false
   */
  public boolean canRotate() {
    return false;  //standard is false. Can be overwritten in subclass is rotating is supported
  }

  /**
   * should be overwritten in the subclass if a specific mediaFileRotater ist available
   *
   * @return
   */
  public MediaFileRotater getMediaFileRotater() {
    return null;
  }

  /**
   * orientation is changed if the resulting rotation since last save() is
   * 90 or 270 degree
   *
   * @return if orientation has change since last save()
   */
  public boolean isOrientationChanged() {
    return rotateOperation == ImageFileRotater.RotateOperation.ROTATE90
      || rotateOperation == ImageFileRotater.RotateOperation.ROTATE270;
  }

  /**
   * plan a rotation
   * the handed operation is added on previously planned rotation operation and then optimized
   * if canRotate==false then the rotation will not be saved later and a warning should be shown on GUI
   *
   * @param operation 90 degree-wise clockwise
   */
  public void rotate(MediaFileRotater.RotateOperation operation) {
    boolean wasOrientationChanged = isOrientationChanged(); //remember state before transformation

    int rotation = (rotateOperation.ordinal() + operation.ordinal()) % 4; //modulo 4 because 360=90*4
    rotateOperation = MediaFileRotater.RotateOperation.values()[rotation];

    //as rotation is performed first in saveChanges() when rotation changes orientation flipping V/H must be exchanged
    if (wasOrientationChanged != isOrientationChanged()) {
      boolean temp = flipHorizontally;
      flipHorizontally = flipVertically;
      flipVertically = temp;
    }
    if (canRotate()) updateStatusProperty();

  }

  /**
   * plan a flip operation
   * the handed operation is added on previously planned flip operations
   * if canRotate==false then the flipping will not be saved later and a warning should be shown on GUI
   */
  public void flipHorizontally() {
    flipHorizontally = !flipHorizontally;
    if (canRotate()) updateStatusProperty();
  }

  /**
   * plan a flip operation
   * the handed operation is added on previously planned flip operations
   * if canRotate==false then the flipping will not be saved later and a warning should be shown on GUI
   */
  public void flipVertically() {
    if (canRotate()) { //only if the specific MediaFile type supports rotation
      flipVertically = !flipVertically;
      updateStatusProperty();
    }
  }

  /**
   * the default operation is empty
   * so this method needs to be overwritten when a specific MediaFileRotater is provided
   * if canRotate==false then the transformation will not be saved later and a warning should be shown on GUI
   *
   * @return successful
   */
  private boolean performTransformation() {
    //reset planned operations
    boolean successful = true;

    if (canRotate()) {
      successful = getMediaFileRotater().transform(this, rotateOperation, flipHorizontally, flipVertically);

      //reset planned operations
      rotateOperation = ImageFileRotater.RotateOperation.ROTATE0;
      flipHorizontally = false;
      flipVertically = false;

      updateStatusProperty();
      flushMediaContent(); //the imagefile needs to be read again
    }

    return successful;
  }

  public ImageFileRotater.RotateOperation getRotateOperation() {
    return rotateOperation;
  }

  public boolean isRotated() {
    return rotateOperation != ImageFileRotater.RotateOperation.ROTATE0;
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

