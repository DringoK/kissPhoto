package de.kissphoto.model;

import de.kissphoto.ctrl.CounterPositionHeuristic;
import de.kissphoto.helper.I18Support;
import de.kissphoto.helper.StringHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br>
 * This list reflects a directory of the hard disc interpreted as a list of MediaFiles
 * <ul>
 * <li>It is a list of MediaFile objects.
 * <li>It uses a heuristic to guess which number of the file names is the counting one ("the nth")
 * <li>It supports auto numbering of the mediaFiles
 * <li>and with that also changing the order in the folder (when viewed sorted for filename later on with any tool)
 * <li>It supports renaming, search&replace
 * <li>It supports moving of files (one up/down) or via an "clipboard", deleting, undeleting
 * <li>exporting to CSV
 * </ul>
 * All changes are not directly written to disk (=files renamed) but collected in memory until saving is triggered manually
 *
 * @author: Dr. Ingo Kreuz
 * @date: 2012-09-01
 * @modified: 2014-05-24 find/replace works now
 * @modified: 2014-06-04 open with invalid file but valid path will open the path (directory)
 * @modified: 2014-06-05 java.io operations changed into java.nio, onFolderChanged optimized (events for no longer existing tmp-files are ignored)
 * @modified: 2014-06-07 getContent interface to cache simplified
 */
public class MediaFileList {
  private static final String NO_SUCH_FILE_OR_DIRECTORY = "no.such.file.or.directory";  //should extend ObservableList, but JavaFx only provides FactoryClasses. Therefore it contains an Observable List only

  private static ResourceBundle language = I18Support.languageBundle;
  private Path folder;    //without a trailing File.separator!
  private ObservableList<MediaFile> fileList;         //the list of files to be shown and edited
  private ObservableList<MediaFile> deletedFileList;  //the files to be deleted on next saving (File-Save ctrl-s)
  private ObservableList<MediaFile> clipboardFileList;//the deleted files that can be inserted by paste menu (=move in file list)
  private CounterPositionHeuristic heuristic = new CounterPositionHeuristic();
  private int counterPosition = 0; //effectively used position (nth number in filenames)entered by user or guessed by heuristic
  private MediaCache mediaCache = new MediaCache(this); //implements a Cache strategy: which content should be preloaded, which can be flushed
  private boolean respectFolderChanges = true;         //false while changing the loaded folder (saveCurrentFolder)
  private SearchRec searchRec = new SearchRec();

  /**
   * @constructor
   */
  public MediaFileList() {
    resetMediaFileList();
  }

  public static String extractFilename(Path file) {
    return file.getFileName().toString();
  }

  private void resetMediaFileList() {
    fileList = FXCollections.observableArrayList();  //use factory
    deletedFileList = FXCollections.observableArrayList();
    clipboardFileList = null; //i.e. invalid until next cut (see deleteFiles(true))
  }

  /**
   * used by MediaFiles to parse the filenames: The nth number is interpreted as the counter
   *
   * @return the position
   */
  public int getCounterPosition() {
    return counterPosition;
  }

  /**
   * e.g. for correcting the previously guessed position manually (via GUI)
   *
   * @param counterPosition
   */
  public void setCounterPosition(int counterPosition) {
    this.counterPosition = counterPosition;
  }

  /**
   * open a folder by reading the specified directory into MediaFile objects
   * organized as an array list
   * If the passed name is a file the parent directory is opened
   * If the file/directory is null or does not exist at all an error-string is returned
   *
   * @param fileOrFolderName the folder to be opened
   * @returns "" (if everything ok) or Error-Message
   */
  public String openFolder(String fileOrFolderName) throws Exception {
    folder = Paths.get(fileOrFolderName);
    return openFolder(folder);
  }

  /**
   * open a folder by reading the specified directory into MediaFile objects
   * organized as an array list
   * If the file is a file the parent directory is opened
   * If the file/directory is null or does not exist at all an error-string is returned
   *
   * @param fileOrFolder the file or folder to be loaded
   * @returns "" (if everything ok) or Error-Message
   */
  public String openFolder(Path fileOrFolder) {
    //parameter valid?
    if (fileOrFolder == null) {
      return language.getString(NO_SUCH_FILE_OR_DIRECTORY);  //------>preliminary exit
    }

    //determine folder
    if (Files.isDirectory(fileOrFolder)) {
      folder = fileOrFolder;
    } else {
      folder = fileOrFolder.getParent();
    }
    if (folder == null || !Files.exists(folder))
      return MessageFormat.format(language.getString(NO_SUCH_FILE_OR_DIRECTORY), fileOrFolder.toAbsolutePath());  //------>preliminary exit

    //folder successfully determined, now open file list:
    //prepare
    mediaCache.flushAll();
    resetMediaFileList();
    counterPosition = heuristic.guessCounterPosition(folder);

    //read directory
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
      for (Path file : stream) {
        if (Files.isRegularFile(file) && !Files.isHidden(file))
          fileList.add(MediaFile.createMediaFile(file, this));//wrap the file as a MediaFile and specialize it according its media type
      }
    } catch (IOException | DirectoryIteratorException x) {
      return MessageFormat.format(language.getString("error.while.reading.directory.0"), x.getMessage());
    }

    return ""; //no error
  }

  /**
   * This method is called by the FileWatcher.onFolderChanged
   * If the folder change was external (by another program) the cache will be flushed
   * to reflect the external changes when displaying media next time
   */
  public void onFolderChanged(String filename, WatchEvent.Kind kind) {
    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
      onFolderChangedAddFile(filename);

    } else {
      final MediaFile modifiedFile = getMediaFile(filename);

      if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
        if (modifiedFile != null)
          fileList.remove(modifiedFile); //not from cache: it will be replaced sooner or later ;-)

      } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
        if (modifiedFile != null)
          mediaCache.flush(modifiedFile);  //mark an existing file as invalid because it has changed
        else
          onFolderChangedAddFile(filename);   //generate an non existing file (sometimes CREATE-events were not generated during testing...)
      }
    }
  }

  private void onFolderChangedAddFile(String filename) {
    Path file = getCurrentFolder().resolve(filename);
    //ignore all directories and hidden files
    try {
      if (Files.exists(file) && Files.isRegularFile(file) && !Files.isHidden(file)) {
        fileList.add(MediaFile.createMediaFile(file, this));//wrap the file as a MediaFile and specialize it according its media type
      } else {
      }
    } catch (Exception e) {
      //nothing to do if file could not be handled
    }

  }

  /**
   * Return the currently open folder, e.g. to start navigation from there in the file open dialog (see MainMenuBar)
   * note: to set the current folder use openFolder( folderName )
   *
   * @return currently open folder
   */
  public Path getCurrentFolder() {
    return folder;
  }

  public String getCurrentFolderName() {
    try {
      return folder.toRealPath().toString();
    } catch (Exception e) {
      return folder.toAbsolutePath().toString(); //if real Path was not possible then return the syntactic conversion only
    }
  }

  /**
   * Count changes in the fileList
   *
   * @return the number of unsaved changes. 0 means: no changes are unsaved.
   */
  public int getUnsavedChanges() {
    int changes = 0;

    //renamings and timestamp changes
    for (MediaFile mediaFile : fileList) {
      if (mediaFile.isFilenameChanged()) {
        changes++;
      }
    }

    //plus deletions
    changes += deletedFileList.size();

    return changes;
  }

  /**
   * All changes to the media files contained in currently loaded file list (folder)
   * are written to disk: Renaming, TimeStamp-Changes or changes to EXIF-info (todo)
   * <p/>
   * Strategy for renaming:
   * temporarily filename can occur, if filnames are only different in their counter an renumbering was performed
   * Therefore 2 steps are made:
   * <ul>
   * <li>first loop over files: try to rename, if fail apply an intermediate name which is unlikely to be double
   * <li>second loop over files: try to rename again from the intermediate name
   * </ul>
   * If the second loop also is not successful (e.g. the fail was because of invalid name or write protect) the
   * file remains unchanged and it's status is marked as "rename error" (see MediaFile.performRename())
   *
   * @return if successful (true) or if errors occurred (false)
   */
  public boolean saveCurrentFolder() {
    boolean successful = true;
    boolean secondLoopNecessary = false;

    try {
      //first delete all files from disk which are in deletedFileList
      //(do it first to avoid renaming problems, if another file has got the name of a deleted file in between)
      ObservableList<MediaFile> deletedListCopy = FXCollections.observableArrayList(deletedFileList); //copy list for iteration
      for (MediaFile mediaFile : deletedListCopy) {
        mediaCache.flush(mediaFile);
        try {
          //perform deletion on disk
          if (mediaFile.performDeleteFile())
            //if successful
            deletedFileList.remove(mediaFile);    //delete from list with files to delete (the undelete list ;-)
            //it already has been immediately deleted from the view when moving to deletion list, so not necessary to delete from fileList
          else
            successful = false;

        } catch (Exception e) {
          successful = false;
        }
      }

      //first loop for rename and the only loop for all other changes
      for (MediaFile mediaFile : fileList) {
        //rename
        if (mediaFile.isFilenameChanged()) {
          //Media files with players need to flush their cache because media becomes invalid if underlying filename changes
          if (mediaFile.getClass() == MovieFile.class) {
            mediaFile.flushMediaContent();
          }
          if (mediaFile.performRename() == MediaFile.SECOND_RUN) { //if renaming was not successful it has been renamed into an intermediate name
            secondLoopNecessary = true;        //in a second loop this can be resolved
          }
        }
        //time-stamp
        if (mediaFile.isTimeStampChanged()) {
          if (!mediaFile.performSetTimeStamp()) {
            successful = false;
          }
        }
      }

      if (secondLoopNecessary) {
        for (MediaFile mediaFile : fileList) {
          if (mediaFile.isFilenameChanged()) {
            if (mediaFile.performRename() != MediaFile.SUCCESSFUL) {
              successful = false;
            }
          }
        }
      }

    } catch (Exception e) {
      successful = false; //especially when security exceptions occur
    }
    return successful;
  }

  /**
   * export the table of mediaFileList to a CSV-File
   * <p/>
   * 1 first column in CSV is the path with an \ at the end<br>
   * 2 second column in CSV is the filename                <br>
   * 3 the rest of the columns are the editable columns of the file table (status column is omitted)<br>
   * <br>
   * if there are unsaved changes pending columns 2 and 3 are inconsistent ;-)<br>
   *
   * @param file the file where to save to. If it already exists it will be overwritten
   * @throws java.io.IOException from FileWriter class
   * @see de.kissphoto.view.FileTableView chooseFilenameAndExportToCSV()
   */
  public synchronized void exportToCSV(File file) throws IOException {
    Writer writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), "ISO-8859-1");  //(UTF-8 does not work with Excel), US-ASCII, ISO-8859-1, UTF-8, UTF-16-BE, UTF-16LE, UTF-16
      //-------- write headline
      writer.write(MediaFile.getCSVHeadline());

      //-------- write data
      for (MediaFile mediaFile : fileList) {
        writer.write(mediaFile.toCSVString());
      }
    } finally {
      if (writer != null) writer.close();
    }
  }

  /**
   * lookup the filename in the file names on disk ("the original names")
   *
   * @param filename the filename to search for
   * @return the index or -1 if not found
   */
  public int searchPhysicalFilename(String filename) {
    int i = 0;
    boolean found = false;
    while (i < fileList.size() && !found) {
      found = (filename.compareToIgnoreCase(fileList.get(i).getFileOnDisk().getFileName().toString()) == 0);
      i++;
    }
    if (found) {
      return i - 1;  //in the while loop there was one extra i++ after it has been found
    } else {
      return -1;
    }
  }

  /**
   * initialize the internal search variables (cursors, modes)
   * dependent on the current selection:
   * - if there is only one line selected: the complete table is search with searchNext
   * - if there are more the one line selected: searchNext will only search in the selected lines
   *
   * @return the freshly initialized
   */
  public SearchRec initSearch(ObservableList<MediaFile> currentSelection) {
    searchRec.selection = currentSelection;
    searchRec.searchInSelection = searchRec.selection != null && ((searchRec.selection.size()) > 1);

    if (searchRec.searchInSelection || currentSelection == null || currentSelection.size() < 1)
      searchRec.tableRow = 0;  //relative to selection (searchInSelection)
    else
      searchRec.tableRow = fileList.indexOf(currentSelection.get(0));// , relative to complete list (!searchInSelection)

    searchRec.tableColumn = 0; //see MediaFile COL_NO-constants
    searchRec.startPos = 0;    //see MediaFile.searchNext
    searchRec.endPos = 0;      //see MediaFile.searchNext

    searchRec.foundMediaFile = null; //see MediaFile.searchNext
    searchRec.found = false;         //see MediaFile.searchNext

    return searchRec;
  }

  /**
   * find the next occurrence of searchText (that might have been changed since last search
   * starting from the last finding position
   * If this method is called without previous initSearch the search starts from any last finding position or from the start of the table
   * <p/>
   * this method just sets the search Cursors and is used by findNext and replaceAll
   *
   * @param searchText the text to be found in the file table
   * @return true if found, false if not found
   */
  public boolean searchNext(String searchText) {
    ObservableList<MediaFile> listToSearchIn = null;

    if (searchRec.searchInSelection && searchRec.selection != null) {
      listToSearchIn = searchRec.selection;
    } else {
      listToSearchIn = fileList;
    }

    searchRec.found = false; //init a new search loop
    //search the complete list until next occurrence or end of list
    while (!searchRec.found && searchRec.tableRow < listToSearchIn.size()) {
      if (!listToSearchIn.get(searchRec.tableRow).searchNext(searchText, searchRec)) {
        searchRec.tableRow++;                            //try next line
        searchRec.tableColumn = MediaFile.COL_NO_PREFIX; //start in every line with the first column
        searchRec.startPos = 0;                          //and with the beginning of the text
        searchRec.endPos = 0;
      }
    }

    return searchRec.found;
  }

  /**
   * initSearch() needs to be called before a call to replaceAll()!! Otherwise not all occurrences will be replaced
   * <p/>
   * searchNext() and mediaFile.replace() is called until nothing more could be found
   *
   * @param searchText  the text to be found after replacing
   * @param replaceText the text with which the selection is replaced with (if in field edit mode)
   * @return the number of replacements performed
   */
  public int replaceAll(String searchText, String replaceText) {
    int counter = 0;
    while (searchNext(searchText)) {
      if (searchRec.foundMediaFile != null)
        searchRec.foundMediaFile.replaceAccordingSearchRec(replaceText, searchRec);
      counter++;
    }
    return counter;
  }

  /**
   * A grouped list is processed:
   * every root item is taken as the master
   * for each master: its fileDate is copied to its children's fileDate
   *
   * @param changeList clustered list with mediaFiles to change
   */
  public void copyFileDates(TreeItem<MediaFile> changeList) {
    for (TreeItem<MediaFile> master : changeList.getChildren()) {
      for (TreeItem<MediaFile> child : master.getChildren()) {
        child.getValue().setModifiedDate(master.getValue().getModifiedDate());
      }
    }
  }

  public ObservableList<MediaFile> getFileList() {
    return fileList;
  }

  /**
   * get the content (e.g. the image), if possible from the cache
   *
   * @param index index of the mediaFile in mediaFileList
   */
  public Object getCachedMediaContent(int index) {
    return mediaCache.getCachedMediaContent(index);
  }

  /**
   * get the content (e.g. the image), if possible from the cache
   *
   * @param mediaFile in mediaFileList
   */
  public Object getCachedMediaContent(MediaFile mediaFile) {
    return mediaCache.getCachedMediaContent(mediaFile);
  }

  /**
   * @param filename Look up the filename in the fileList's mediaFiles
   * @return and return the mediaFile if found, null if not
   */
  public MediaFile getMediaFile(String filename) {
    MediaFile foundMediaFile = null;

    for (MediaFile mediaFile : fileList) {
      if (mediaFile.getFileOnDiskName().equals(filename)) {
        foundMediaFile = mediaFile;
        break;
      }
    }
    return foundMediaFile;
  }

  /**
   * Enable Pre-load in Cache: Enable loading the neighbour files (one before and one after) of the file loaded with getCachedMediaContent(index)
   * in the Cache.
   * By default pre-loading is on
   */
  public void enablePreLoad() {
    mediaCache.enablePreLoad();
  }

  /**
   * Disable Pre-load: Disable loading the neighbour files (one before and one after) of the file loaded with getCachedMediaContent(index)
   * in the Cache.
   * By default pre-loading is on
   */
  public void disablePreLoad() {
    mediaCache.disablePreLoad();
  }

  /**
   * Calculate the Decimal Format, i.e. how many leading zeros shall be used when renumbering
   *
   * @param start          starting number for renumbering
   * @param step           step size for renumbering
   * @param digits         how many digits shall the DecimalFormat define. 0=automatic (Use the smallest possible fixed number), 1=no leading zeros
   * @param numberElements how many elements shall be renumbered using the DecimalFormat
   * @return decimalFormat that can be used by decimalFormat.format(int) to get correct number of digits (leading zeros)
   */
  private DecimalFormat determineFormat(int start, int step, int digits, int numberElements) {
    //determine the width of the numbers: 0=auto
    int numberWidth = digits;
    if (numberWidth == 0) {
      int finalNumber = start + (numberElements - 1) * step;
      numberWidth = (int) (Math.log10(Math.max(start, finalNumber))) + 1;
    }

    DecimalFormat decimalFormat = new DecimalFormat(StringHelper.repeat("0", numberWidth));

    return decimalFormat;
  }

  /**
   * renumbers all files of the fileList by writing column "counter" which are in the indices list ("selection")
   * if nothing is in the indices list nothing will happen
   * Leading zeros are used to get at least the number of digits determined by param digits
   * the numbering begins with start and uses step as step size
   *
   * @param start   starting number
   * @param step    repeatingly add this number to the start, can be negative, 0 leads to constant numbering (using start)
   * @param digits  Use leading zeros for to fill up digits for the number . 0=automatic(Use the smallest possible fixed number), 1=no leading zeros
   * @param indices The indices of fileList to be renumbered. null or empty list will prevent from any renumbering
   */
  public void renumber(int start, int step, int digits, ObservableList<Integer> indices) {
    //if null or empty list: nothing will be renumbered
    if (indices != null) {
      //copy and sort the indices
      ObservableList<Integer> indicesSorted = FXCollections.observableArrayList(indices); //copy
      FXCollections.sort(indicesSorted);

      DecimalFormat decimalFormat = determineFormat(start, step, digits, indices.size());

      //renumber
      int newNumber = start;

      for (Integer i : indicesSorted) {
        fileList.get(i).setCounter(decimalFormat.format(newNumber));
        newNumber = newNumber + step;
      }
    }
  }

  /**
   * renumbers all files of the fileList by writing column "counter" which are in the indices list ("selection")
   * if nothing is in the indices list nothing will happen
   * Leading zeros are used to get at least the number of digits determined by param digits
   * the numbering begins with start and uses step as step size<br><br>
   * In contrast to renumber() this variation will calculate the numbering for every file from its position (index) in fileList
   * i.e. a file at index idx gets the number: counter = idx*step + start
   *
   * @param start   starting number for fileList
   * @param step    stepSize, can be negative, 0 leads to constant numbering (using start)
   * @param digits  Use leading zeros for to fill up digits for the number . 0=automatic(Use the smallest possible fixed number), 1=no leading zeros
   * @param indices The indices of fileList to be renumbered. null or empty list will prevent from any renumbering
   */
  public void renumberRelativeToIndices(int start, int step, int digits, ObservableList<Integer> indices) {
    //if null or empty list: nothing will be renumbered
    if (indices != null) {
      //copy and sort the indices
      ObservableList<Integer> indicesSorted = FXCollections.observableArrayList(indices); //copy
      //sorting is not necessary

      DecimalFormat decimalFormat = determineFormat(start, step, digits, fileList.size()); //fileList because renumbering is virtually for whole fileList

      //renumber
      for (Integer i : indicesSorted) {
        fileList.get(i).setCounter(decimalFormat.format(i * step + start));
      }
    }
  }

  /**
   * renumbers all files of the fileList by writing column "counter"
   * Leading zeros are used to get at least the number of digits determined by param digits
   * the numbering begins with start and uses step as step size
   *
   * @param start  starting number
   * @param step   repeatingly add this number to the start, can be negative, 0 leads to constant numbering (using start)
   * @param digits Use leading zeros for to fill up digits for the number . 0=automatic(Use the smallest possible fixed number), 1=no leading zeros
   */
  public void renumberAll(int start, int step, int digits) {
    DecimalFormat decimalFormat = determineFormat(start, step, digits, fileList.size());

    //renumber
    int newNumber = start;

    for (MediaFile mediaFile : fileList) {
      mediaFile.setCounter(decimalFormat.format(newNumber));
      newNumber = newNumber + step;
    }
  }

  /**
   * mark all files passed in the parameter to be deleted:
   * remove them from filelist (=hide them)
   * add them to the deletedFileList
   * caution: the list in the parameter must not be a subset of the list to be deleted
   * make a copy first.
   * E.g. if the selection of mediafilelist is passed you have to make a copy of the selection-list first.
   *
   * @param filesToBeDeleted the files from filelist which shall be moved dot deletedFileList
   * @param cutToClipboard:  true means pasting is enabled, ie. the deleted files are additionally stored in the pasting list (for moving)
   *                         false means the selection is only moved to the deletion list
   */
  public void deleteFiles(ObservableList<MediaFile> filesToBeDeleted, boolean cutToClipboard) {
    deletedFileList.addAll(filesToBeDeleted);
    if (cutToClipboard) {
      clipboardFileList = FXCollections.observableArrayList(); //use factory: always use a new list
      clipboardFileList.addAll(filesToBeDeleted);
    }
    fileList.removeAll(filesToBeDeleted);
  }

  /**
   * undelete the files of the clipBoardFileList
   *
   * @param insertionIndex the inserting will be before the line with this index (e.g. the focused line)
   * @return the list of the pasted files (so they can be selected) or null if there were no cutted files
   */
  public ObservableList<MediaFile> paste(int insertionIndex) {
    ObservableList<MediaFile> retVal = clipboardFileList;

    if (clipboardFileList != null) {
      unDeleteFiles(insertionIndex, clipboardFileList);
      clipboardFileList = null; //mark it as invalid
    }

    return retVal;  //return the old list
  }

  /**
   * unmark all files passed in the parameter to be unDeleted:
   * remove them from deletedFileList
   * add them to fileList (recover them)
   * caution: the list in the parameter must not be a subset of the list to be deleted
   * make a copy first.
   * E.g. if the selection of mediaFileList is passed you have to make a copy of the selection-list first.
   * This routine can also be used for pasting: The deletion list and the fileList are still maintained and the
   * insertion is always before the focused line
   *
   * @param filesToBeUnDeleted the files from fileList which shall be moved dot deletedFileList
   * @param insertionIndex     the inserting will be before the line with this index (e.g. the focused line)
   */
  public void unDeleteFiles(int insertionIndex, ObservableList<MediaFile> filesToBeUnDeleted) {
    fileList.addAll(insertionIndex, filesToBeUnDeleted);
    deletedFileList.removeAll(filesToBeUnDeleted);
    //if there were any of them on the "clipboard" restoring the files removes these also from the (ctrl-x) "Cut"-Clipboard
    if (clipboardFileList != null) clipboardFileList.removeAll(filesToBeUnDeleted);
  }

  public ObservableList<MediaFile> getDeletedFileList() {
    return deletedFileList;
  }

  /**
   * a structure with all cursors and modes of a text search
   * it is initialized with initSearch(), so this call should be made for a new search using searchNext()
   * searchNext() always start from the cursor position stored in searchRec
   */
  public class SearchRec {
    //mode
    public ObservableList<MediaFile> selection = null; //null means invalid
    public boolean searchInSelection = false;

    //cursors: the position of the last occurrence
    public int tableRow = 0;
    public int tableColumn = 0;
    public int startPos = 0;
    public int endPos = 0;

    //the core modalResult ;-)
    public MediaFile foundMediaFile = null;
    public boolean found = false;
  }
}
