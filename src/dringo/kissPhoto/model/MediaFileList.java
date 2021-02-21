package dringo.kissPhoto.model;

import dringo.kissPhoto.ctrl.CounterPositionHeuristic;
import dringo.kissPhoto.helper.PathHelpers;
import dringo.kissPhoto.view.FileTableView;
import dringo.kissPhoto.view.MediaContentView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.MessageFormat;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br>
 * This list reflects a directory of the hard disc interpreted as a list of MediaFiles
 * <ul>
 * <li>It contains a list of MediaFile objects.
 * <li>It uses a heuristic to guess which number of the file names is the counting one ("the nth")
 * <li>It supports auto numbering of the mediaFiles
 * <li>and with that also changing the order in the folder (when viewed sorted for filename later on with any tool)
 * <li>It supports renaming, search&replace
 * <li>It supports moving of files (one up/down) or via an "clipboard", deleting, undeleting
 * <li>exporting to CSV
 * </ul>
 * All changes are not directly written to disk (=files renamed) but collected in memory until saving is triggered manually
 *
 * @author Dringo
 * @since 2012-09-01
 * @version 2020-12-20 Media Cache moved to MediaFile
 * @version 2020-11-30 clean up code
 * @version 2017-10-30 support lossless rotation and flipping of ImageFiles, saving moved to mediaFileListSavingTask to enable ProgressBar (with rotation it can take long!)
 * @version 2014-06-07 getContent interface to cache simplified
 * @version 2014-06-05 java.io operations changed into java.nio, onFolderChanged optimized (events for no longer existing tmp-files are ignored)
 * @version 2014-06-04 open with invalid file but valid path will open the path (directory)
 * @version 2014-05-24 find/replace works now
 */
public class MediaFileList { //should extend ObservableList, but JavaFx only provides FactoryClasses. Therefore it contains an Observable List only
  private static final String NO_SUCH_FILE_OR_DIRECTORY = "no.such.file.or.directory";

  private Path folder;    //without a trailing File.separator!
  private ObservableList<MediaFile> fileList;         //the list of files to be shown and edited
  private ObservableList<MediaFile> deletedFileList;  //the files to be deleted on next saving (File-Save ctrl-s)
  private ObservableList<MediaFile> clipboardFileList;//the deleted files that can be inserted by paste menu (=move in file list)
  private final CounterPositionHeuristic heuristic = new CounterPositionHeuristic();
  private int counterPosition = 0; //effectively used position (nth number in filenames)entered by user or guessed by heuristic

  private final SearchRec searchRec = new SearchRec();

  /**
   * constructor
   */
  public MediaFileList() {
    resetMediaFileList();
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
   * open a folder by reading the specified directory into MediaFile objects
   * organized as an array list
   * If the file is a file the mediaFileList directory is opened
   * If the file/directory is null or does not exist at all an error-string is returned
   *
   * @param fileOrFolder the file or folder to be loaded
   * @return "" (if everything ok) or Error-Message
   */
  public String openFolder(Path fileOrFolder) {
    //parameter valid?
    if (fileOrFolder == null) {
      return language.getString(NO_SUCH_FILE_OR_DIRECTORY);  //------>preliminary exit
    }
    folder = PathHelpers.extractFolder(fileOrFolder);

    if (folder == null || !Files.exists(folder))
      return MessageFormat.format(language.getString(NO_SUCH_FILE_OR_DIRECTORY), fileOrFolder.toAbsolutePath());  //------>preliminary exit

    //folder successfully determined, now open file list:
    //prepare
    MediaFile.flushAllMediaFromCache();
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
          modifiedFile.flushFromCache();  //mark an existing file as invalid because it has changed
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
      if (mediaFile.isChanged()) {
        changes++;
      }
    }

    //plus deletions
    changes += deletedFileList.size();

    return changes;
  }

  /**
   * Preload-Strategy: try to put one file before and one after the current position into the cache
   * What to put in cache is determined by the viewer for the media. What is the appropriate viewer selects ContentView
   * @param index the current position in mediaFileList
   * @param contentView selects the appropriate viewer which again knows what to put in cache
   */
  public void preLoadMedia(int index, MediaContentView contentView){
    MediaFile mediaFile;
      //Cancel any background loadings except next/previous
      if (index > 1) {//if there is a previous/previous
        mediaFile = fileList.get(index - 2);
        if (mediaFile != null) mediaFile.cancelBackgroundLoading();
      }
      if (index < fileList.size() - 2) { //if there exists a 'next next'
        mediaFile = fileList.get(index + 2);
        if (mediaFile != null) mediaFile.cancelBackgroundLoading();
      }

      //preload previous media if necessary async in background
      if (index > 0) { //if there exists a 'previous'
        mediaFile = fileList.get(index - 1);
        contentView.preloadMediaContent(mediaFile);
      }
      //preload next media if necessary async. in background
      if (index < fileList.size() - 1) { //if there exists a 'next'
        mediaFile = fileList.get(index + 1);
        contentView.preloadMediaContent(mediaFile);
      }

  }

  /**
   * perform the handed rotateOperation to all selected Images
   * Only Images are affected - all other selected files are ignored
   *
   * @param selectedFiles list of currently selected files
   * @param rotateOperation to perform
   * @return the number of mediaFiles that cannot save the rotation (only jpgs are supported so far)
   */
  public synchronized int rotateSelectedFiles(ObservableList<MediaFile> selectedFiles, ImageFileRotater.RotateOperation rotateOperation) {
    int countNotRotatable = 0;
    for (MediaFile mediaFile : selectedFiles) {
      if (!(mediaFile instanceof OtherFile)) mediaFile.rotate(rotateOperation);
      if (!mediaFile.canRotate()) countNotRotatable++;
    }
    return countNotRotatable;
  }

  /**
   * perform flipping (mirroring) of all selected Images
   * Only Images are affected - all other selected files are ignored
   *
   * @param selectedFiles list of currently selected files
   * @param horizontally  = true: mirror horizontally, false: mirror vertically
   * @return the number of mediaFiles that cannot save the flipping (only jpgs are supported so far)
   */
  public synchronized int flipSelectedFiles(ObservableList<MediaFile> selectedFiles, boolean horizontally) {
    int countNotRotatable = 0;
    for (MediaFile mediaFile : selectedFiles) {
//      if (mediaFile instanceof ImageFile) //rotate all despite only jpg images' rotation can be saved
      if (!(mediaFile instanceof OtherFile))
        if (horizontally)
        mediaFile.flipHorizontally();
      else
        mediaFile.flipVertically();

      if (!mediaFile.canRotate()) countNotRotatable++;
    }
    return countNotRotatable;
  }

  /**
   * set orientation of the image according EXIF orientation (set rotation and flipping) of all selected JPEG-Images
   * Only JPEG-Images are affected - all other selected files are ignored
   *
   * @param selectedFiles list of currently selected files
   * @return the number of mediaFiles that cannot save the flipping (only jpgs are supported so far)
   */
  public synchronized int setOrientationAccordingExif(ObservableList<MediaFile> selectedFiles) {
    int countNotRotatable = 0;
    for (MediaFile mediaFile : selectedFiles) {
      if (mediaFile.canRotate()) //rotate only jpgs, because only here the Exif orientation can be determined
        ((ImageFile)mediaFile).setOrientationAccordingExif();
      else
        countNotRotatable++;
    }
    return countNotRotatable;
  }

  /**
   * Build a Task for saving all changes of mediaFileList
   * and hand it to the GUI. Now the GUI has the chance to connect a progressBar with the progressProperty of the task
   * before it starts it using startSavingTask();
   *
   * @return a task that will save all changes
   */
  public MediaFileListSavingTask getNewSavingTask() {
    return new MediaFileListSavingTask(deletedFileList, fileList, getUnsavedChanges());
  }

  public void startSavingTask(MediaFileListSavingTask mediaFileListSavingTask) {
    Thread th = new Thread(mediaFileListSavingTask);
    th.setDaemon(true); //cancel with end of kissPhoto (latest)
    th.start();
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
   * @see FileTableView chooseFilenameAndExportToCSV()
   */
  public synchronized void exportToCSV(File file) throws IOException {
    Writer writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), StandardCharsets.ISO_8859_1);  //(UTF-8 does not work with Excel), US-ASCII, ISO-8859-1, UTF-8, UTF-16-BE, UTF-16LE, UTF-16
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
    ObservableList<MediaFile> listToSearchIn;

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

    return new DecimalFormat("0".repeat(numberWidth));
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
        fileList.get(i).setCounter(decimalFormat.format((long) i * step + start));
      }
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
  public static class SearchRec {
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
