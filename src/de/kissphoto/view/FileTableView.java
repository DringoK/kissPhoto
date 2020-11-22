package de.kissphoto.view;


import com.drew.lang.annotations.NotNull;
import de.kissphoto.KissPhoto;
import de.kissphoto.ctrl.FileChangeWatcher;
import de.kissphoto.ctrl.FileChangeWatcherEventListener;
import de.kissphoto.helper.I18Support;
import de.kissphoto.helper.PathHelpers;
import de.kissphoto.model.ImageFileRotater;
import de.kissphoto.model.MediaFile;
import de.kissphoto.model.MediaFileList;
import de.kissphoto.model.MediaFileListSavingTask;
import de.kissphoto.view.dialogs.*;
import de.kissphoto.view.fileTableHelpers.FileHistory;
import de.kissphoto.view.fileTableHelpers.TextFieldCellFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * JavaFX Table View that is the GUI for MediaFileList
 * <p/>
 *
 * @author Ingo
 * @since 2012-09-06
 * @version 2020-11-019 bug fixing: empty directories and reloading invalid directories, GlobalSettings made global (static)
 * @version 2019-11-01 moving viewport completely rewritten and Alignment is new parameter. Behavior is now as expected with extra lines :-), move/up down keys improved
 * @version 2019-07-07 chooseFileOrFolder sends extra refresh() for tableView to prevent from be shown as empty, cache problems fixed
 * @version 2018-11-17 OpenRecentFile(0) now keeps the selection (like ReopenCurrentFolder earlier), keep caret position when moving up/down (bugfix)
 * @version 2017-10-30 saving uses ProgressBar now because saving of rotated images can last long...
 * @version 2017-10-24 statistics in statusBar support
 * @version 2017-10-22 file-open history (Open recent) support
 * @version 2017-10-20 space-bar is now play/pause additionally if not in edit mode
 * @version 2017-10-14 bugfixing and new functionality copyDescriptionDown() + store Column-Widths
 * @version 2016-11-01 RestrictedTextField stores connection to FileTable locally, so that passing editing threads store the correct table cell
 * @version 2016-06-12 shift-ctrl up/down for moving files now also works in windows 10
 * @version 2014-06-22 extra column for the counter's separator (the character after the counter)
 * @version 2014-06-07 getContent interface to cache simplified
 * @version 2014-06-05 java.io operations changed into java.nio
 * @version 2014-06-02 WAIT-mouse cursor shown during loading a directory now
 * @version 2014-05-25 find/replace markup works now
 * @version 2014-05-02 I18Support, Cursor in Edit-Mode over lines, reopen added etc
 */

public class FileTableView extends TableView implements FileChangeWatcherEventListener {
  //---- views linking
  private final Stage primaryStage;  //main window
  private final StatusBar statusBar;  //messages
  protected MediaContentView mediaContentView; //mediaContentView to show media if selection changes

  //---- the content to be displayed
  private final MediaFileList mediaFileList = new MediaFileList();

  //---- listen if an external program changes the currently loaded folder
  private FileChangeWatcher fileChangeWatcher = new FileChangeWatcher();  //check for external changes to an opened folder
  private MediaFile lastSelection = null;

  //------------- IDs for GlobalSettings for FileTableView
  private static final String LAST_FILE_OPENED = "lastFileOpened";

  protected VirtualFlow flow;  //viewport vor scrolling

  //----- Table Columns
  protected final TableColumn statusColumn;
  protected final TableColumn prefixColumn;
  protected final TableColumn counterColumn;
  protected final TableColumn separatorColumn;
  protected final TableColumn descriptionColumn;
  protected final TableColumn extensionColumn;
  protected final TableColumn fileDateColumn;

  //string constants (i18alized) for table columns' headlines
  private static ResourceBundle language = I18Support.languageBundle;
  public static final String PREFIX = "prefix";
  public static final String COUNTER = "counter";
  public static final String SEPARATOR = "separator";
  public static final String DESCRIPTION = "description";
  public static final String EXTENSION = "extension";
  public static final String MODIFIED = "modified";
  public static final String NOTHING_FOUND = "nothing.found";
  private static final String FILE_DATE = "fileDate";

  //Default Column Widths
  private static final int STATUS_COL_DEFAULT_WIDTH = 20;
  private static final int PREFIX_COL_DEFAULT_WIDTH = 50;
  private static final int COUNTER_COL_DEFAULT_WIDTH = 40;
  private static final int SEPARATOR_COL_DEFAULT_WIDTH = 30;
  private static final int DESCRIPTION_COL_DEFAULT_WIDTH = 480;
  private static final int EXTENSION_COL_DEFAULT_WIDTH = 50;
  private static final int FILEDATE_COL_DEFAULT_WIDTH = 155;

  //Constants for writing Width to settings-File
  private static final String STATUS_COL_WIDTH = "StatusColWidth";
  private static final String PREFIX_COL_WIDTH = "PrefixColWidth";
  private static final String COUNTER_COL_WIDTH = "CounterColWidth";
  private static final String SEPARATOR_COL_WIDTH = "SeparatorColWidth";
  private static final String DESCRIPTION_COL_WIDTH = "DescriptionColWidth";
  private static final String EXTENSION_COL_WIDTH = "ExtensionColWidth";
  private static final String FILEDATE_COL_WIDTH = "FileDateColWidth";

  //renumbering values will be initialized on every openFolder() and can be changed by user using renumberDialog()
  private int numberingOffset = 1;  //determines with which number renumbering of the list starts.
  private int numberingStepSize = 1;
  private int numberingDigits = 0;   //zero is [auto]

  //----- Dialog Singletons
  private FindReplaceDialog findReplaceDialog = null; //will be created when used firstly (see findAndReplace() )
  private RenumberDialog renumberDialog = null; //will be create when used firstly (see renumberWithDialog() )
  private RenameDialog renameDialog = null; //will be created when used firstly (see renameWithDialog())
  private UnDeleteDialog unDeleteDialog = null; //will be created when used firstly (see unDeleteWithDialog())

  //----- link to MenuItems to enable/disable
  private MenuItem unDeleteMenuItem = null;  //Main menu will pass a link to it's unDeleteMenuItem, so deletion routines can control if disabled or not
  private MenuItem pasteMenuItem = null;     //Main menu will pass a link to it's pasteMenuItem, so copy/cut routines can control if disabled or not

  //----- Define Cell Factory and EditEventHandler
  //will be the identical for all columns (except statusColumn, see FileTableView constructor)
  TextFieldCellFactory textFieldCellFactory = new TextFieldCellFactory();
  CellEditCommitEventHandler cellEditCommitEventHandler = new CellEditCommitEventHandler();

  //----- Enable keeping Cursor-Postion while editing when chaning line
  private boolean selectSearchResultOnNextStartEdit = false;  //during SearchNext searchRec Cursor needs to be set on StartEdit

  private boolean renameDialogActive = false;
  private FileHistory fileHistory = null;

  static boolean isActivateMenu = false; //(see setOnKeyPressed/released) prevent collision of Menu Activation and File Moving


  /**
   * constructor will try to open the passed file or foldername
   * if there is no such file or folder an empty list is shown
   *
   * @param primaryStage     link back to main window
   * @param mediaContentView link to the view where media is displayed if focus changes
   * @param statusBar        link to statusBar for showing information/errors
   */
  public FileTableView(@NotNull Stage primaryStage, final MediaContentView mediaContentView, StatusBar statusBar) {
    //remember connections to main window etc
    this.primaryStage = primaryStage;
    this.mediaContentView = mediaContentView;
    this.statusBar = statusBar;
    fileHistory = new FileHistory(KissPhoto.globalSettings, this);

    this.setMinSize(100.0, 100.0);

    //set properties of the table
    setEditable(false); //Edit Event shall not be handled by TableView's default, but by the main menu bar
    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    //create Table
    //status: readonly Column to show status flags
    statusColumn = new TableColumn(language.getString("status"));
    statusColumn.setPrefWidth(STATUS_COL_DEFAULT_WIDTH);
    statusColumn.setEditable(false);
    statusColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>("status"));

    //prefix
    prefixColumn = new TableColumn(language.getString(PREFIX));
    prefixColumn.setPrefWidth(PREFIX_COL_DEFAULT_WIDTH);
    prefixColumn.setEditable(true);
    prefixColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>(PREFIX));
    prefixColumn.setCellFactory(textFieldCellFactory);
    prefixColumn.setOnEditCommit(cellEditCommitEventHandler);

    //counter
    counterColumn = new TableColumn(language.getString(COUNTER));
    counterColumn.setPrefWidth(COUNTER_COL_DEFAULT_WIDTH);
    counterColumn.setEditable(true); //as a default counter is editable until renumbering is set to auto-mode
    counterColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>(COUNTER));
    counterColumn.setCellFactory(textFieldCellFactory);
    counterColumn.setOnEditCommit(cellEditCommitEventHandler);
    counterColumn.setComparator(new Comparator() {
      @Override
      public int compare(Object o1, Object o2) {    //should sort numeric even without leading zeros in the filename
        try {
          int i1 = Integer.parseInt((String) o1);
          int i2 = Integer.parseInt((String) o2);
          return Integer.compare(i1, i2);
        } catch (Exception e) {   //e.g. for empty strings: treat them as equal
          return 0;
        }
      }
    });

    //separator
    separatorColumn = new TableColumn(language.getString(SEPARATOR));
    separatorColumn.setEditable(true);
    separatorColumn.setPrefWidth(SEPARATOR_COL_DEFAULT_WIDTH);
    separatorColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>(SEPARATOR));
    separatorColumn.setCellFactory(textFieldCellFactory);
    separatorColumn.setOnEditCommit(cellEditCommitEventHandler);

    //description
    descriptionColumn = new TableColumn(language.getString(DESCRIPTION));
    descriptionColumn.setEditable(true);
    descriptionColumn.setPrefWidth(DESCRIPTION_COL_DEFAULT_WIDTH);
    descriptionColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>(DESCRIPTION));
    descriptionColumn.setCellFactory(textFieldCellFactory);
    descriptionColumn.setOnEditCommit(cellEditCommitEventHandler);

    //file type (extension)
    extensionColumn = new TableColumn(language.getString(EXTENSION));
    extensionColumn.setPrefWidth(EXTENSION_COL_DEFAULT_WIDTH);
    extensionColumn.setEditable(true);
    extensionColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>(EXTENSION));
    extensionColumn.setCellFactory(textFieldCellFactory);
    extensionColumn.setOnEditCommit(cellEditCommitEventHandler);

    //file date
    fileDateColumn = new TableColumn(language.getString(MODIFIED));
    fileDateColumn.setPrefWidth(FILEDATE_COL_DEFAULT_WIDTH);
    fileDateColumn.setEditable(true);
    fileDateColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>(FILE_DATE));
    fileDateColumn.setCellFactory(textFieldCellFactory);
    fileDateColumn.setOnEditCommit(cellEditCommitEventHandler);

    this.getColumns().setAll(statusColumn, prefixColumn, counterColumn, separatorColumn,
        descriptionColumn, extensionColumn, fileDateColumn);

    //install SortOrder-ChangeListener to keep Selection
    this.getSortOrder().addListener(new ListChangeListener() {

      @Override
      public void onChanged(ListChangeListener.Change change) {
        restoreLastSelection();
      }
    });

    //---------- install event handlers --------------
    installKeyHandlers(mediaContentView);
    installMouseHandlers();
    getViewPort();
    installDragDropHandlers(primaryStage);
    installSelectionHandler(mediaContentView);
    installInvalidCacheHandler(mediaContentView);
  }

  private void installInvalidCacheHandler(MediaContentView mediaContentView) {
    mediaFileList.getMediaFileThatNeedsReloadProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        MediaFile mediaFileToReload = (MediaFile) newValue;
        //System.out.println("FileTableView: invalidation detected: " + mediaFileToReload.getFileOnDisk());

        int currentlySelectedLine = getSelectionModel().getFocusedIndex();
        //if this is the currently shown media then reload
        if ((currentlySelectedLine >= 0) && (currentlySelectedLine == mediaFileList.getFileList().indexOf(mediaFileToReload))) {
          //System.out.println("FileTableView: ----------> current media invalidated: Line: " + currentlySelectedLine);
          mediaContentView.setMedia(mediaFileList.getFileList().get(currentlySelectedLine), null);
        }
      }
    });
  }

  private void installSelectionHandler(MediaContentView mediaContentView) {
    this.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        //set selected media in mediaContentView (or null if not valid)
        if (newValue.intValue() >= 0) { //only if selection is valid
          lastSelection = mediaFileList.getFileList().get(newValue.intValue());
          mediaContentView.setMedia(mediaFileList.getFileList().get(newValue.intValue()), null);

        } else {
          if (mediaFileList.getFileList().size() <= 0) {
            //this happens e.g. if sort order is changed (by clicking the headlines) in an empty list (nothing loaded)
            //keep lastSelection, so the sortOrderChange-Listener can restore selection

            //invalid selection
            mediaContentView.setMedia(null, null);
            lastSelection = null;
          }
        }
      }
    });
  }

  private void installDragDropHandlers(@NotNull Stage primaryStage) {
    primaryStage.getScene().setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
          dragEvent.acceptTransferModes(TransferMode.COPY);
        } else {
          dragEvent.consume();
        }
      }
    });

    primaryStage.getScene().setOnDragDropped(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
          success = true;
          //load first file only
          openFolder(db.getFiles().get(0).getAbsolutePath(), true);
        }
        dragEvent.setDropCompleted(success);
        dragEvent.consume();
      }
    });
  }

  private void getViewPort() {
    //this is a solution for getting the viewport (flow) seen on http://stackoverflow.com/questions/17268529/javafx-tableview-keep-selected-row-in-current-view
    skinProperty().addListener(new ChangeListener<Skin>() {
      @Override
      public void changed(ObservableValue<? extends Skin> ov, Skin t, Skin t1) {
        if (t1 == null) {
          return;
        }

        TableViewSkin tvs = (TableViewSkin) t1;
        ObservableList<Node> kids = tvs.getChildren();

        if (kids == null || kids.isEmpty()) {
          return;
        }
        flow = (VirtualFlow) kids.get(1);
      }
    });
  }

  private void installMouseHandlers() {
    setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        if (event.getClickCount() > 1) { //if double clicked
          rename();
        }
      }
    });
  }

  private void installKeyHandlers(MediaContentView mediaContentView) {
    setOnKeyPressed(keyEvent -> {
      //F2 (from menu) does not work if multiple lines are selected so here a key listener ist installed for F2
      if ((keyEvent.getCode() == KeyCode.F2) && !keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
        keyEvent.consume();
        rename();
      } else if ((keyEvent.getCode() == KeyCode.SPACE) && !keyEvent.isControlDown() && !keyEvent.isShiftDown()
        && !isEditMode() && mediaContentView.getPlayerViewer().isVisible()) {
        keyEvent.consume();
        mediaContentView.getPlayerViewer().getPlayerControls().togglePlayPause();
      }
      //overwrite standard functionality (selection) view ctrl-shift up/down with file moving
      else if ((keyEvent.getCode() == KeyCode.DOWN) && keyEvent.isControlDown() && keyEvent.isShiftDown()) {
        moveSelectedFilesDown();
        keyEvent.consume();
      } else if ((keyEvent.getCode() == KeyCode.UP) && keyEvent.isControlDown() && keyEvent.isShiftDown()) {
        moveSelectedFilesUp();
        keyEvent.consume();
      }
    });
  }

  /**
   * Strategy for opening inital File or Folder:
   * - try to open the passed file (the parameter when starting kissPhoto)
   * - if failed then try to open the last open folder (read it from settings file if available)
   * - if failed then load nothing but show a message in Status bar
   *
   * @param initialFileOrFolderName the parameter that was provided when kissPhoto was started
   */
  public void openInitialFolder(String initialFileOrFolderName) {
    //----------------------------------------
    // try to open file or Folder

    if ((initialFileOrFolderName != null) && (initialFileOrFolderName.length() > 0)) {
      //first trial is to open the file passed as a parameter
      openFolder(initialFileOrFolderName, false); //no unsaved changes possible, because initial opening
    } else {
      //second trial is to open the last file opened
      String LastFileOrFolderName = null;
      if (fileHistory.getRecentlyOpenedList().size() > 0) {
        openFolder(fileHistory.getRecentlyOpenedList().get(0), false); //no unsaved changes possible, because initial opening
      } else {
        statusBar.showMessage(language.getString("use.ctrl.o.to.open.a.folder"));
      }
    }
  }

  public void setDefaultColumnWidths() {
    statusColumn.setPrefWidth(STATUS_COL_DEFAULT_WIDTH);
    prefixColumn.setPrefWidth(PREFIX_COL_DEFAULT_WIDTH);
    counterColumn.setPrefWidth(COUNTER_COL_DEFAULT_WIDTH);
    separatorColumn.setPrefWidth(SEPARATOR_COL_DEFAULT_WIDTH);
    descriptionColumn.setPrefWidth(DESCRIPTION_COL_DEFAULT_WIDTH);
    extensionColumn.setPrefWidth(EXTENSION_COL_DEFAULT_WIDTH);
    fileDateColumn.setPrefWidth(FILEDATE_COL_DEFAULT_WIDTH);
  }

  /**
   * Store last file table settings from Global-Settings properties file
   * i.e. column widths
   * assumes:
   * - globalSettings already loaded
   * - stage not null
   */
  public void storeLastSettings() {
    KissPhoto.globalSettings.setProperty(STATUS_COL_WIDTH, Double.toString(statusColumn.getWidth()));
    KissPhoto.globalSettings.setProperty(PREFIX_COL_WIDTH, Double.toString(prefixColumn.getWidth()));
    KissPhoto.globalSettings.setProperty(COUNTER_COL_WIDTH, Double.toString(counterColumn.getWidth()));
    KissPhoto.globalSettings.setProperty(SEPARATOR_COL_WIDTH, Double.toString(separatorColumn.getWidth()));
    KissPhoto.globalSettings.setProperty(DESCRIPTION_COL_WIDTH, Double.toString(descriptionColumn.getWidth()));
    KissPhoto.globalSettings.setProperty(EXTENSION_COL_WIDTH, Double.toString(extensionColumn.getWidth()));
    KissPhoto.globalSettings.setProperty(FILEDATE_COL_WIDTH, Double.toString(fileDateColumn.getWidth()));


    try {
      Path currentFile = mediaFileList.getFileList().get(getSelectionModel().getFocusedIndex()).getFileOnDisk();
      fileHistory.refreshOpenedFileInHistory(currentFile);
    } catch (Exception e) {
      //if there is no focused file, then do not update the selection but keep it as it was when the folder opened
    }
  }

  /**
   * Restore last file table settings from Global-Settings properties file
   * i.e. column widths
   * assumes:
   * - globalSettings already loaded
   * - stage not null
   */
  public void restoreLastSettings() {
    try {
      statusColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(STATUS_COL_WIDTH)));
    } catch (Exception e) {
      primaryStage.setX(STATUS_COL_DEFAULT_WIDTH);
    }
    try {
      prefixColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(PREFIX_COL_WIDTH)));
    } catch (Exception e) {
      primaryStage.setX(PREFIX_COL_DEFAULT_WIDTH);
    }
    try {
      counterColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(COUNTER_COL_WIDTH)));
    } catch (Exception e) {
      primaryStage.setX(COUNTER_COL_DEFAULT_WIDTH);
    }
    try {
      separatorColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(SEPARATOR_COL_WIDTH)));
    } catch (Exception e) {
      primaryStage.setX(SEPARATOR_COL_DEFAULT_WIDTH);
    }
    try {
      descriptionColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(DESCRIPTION_COL_WIDTH)));
    } catch (Exception e) {
      primaryStage.setX(DESCRIPTION_COL_DEFAULT_WIDTH);
    }
    try {
      extensionColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(EXTENSION_COL_WIDTH)));
    } catch (Exception e) {
      primaryStage.setX(EXTENSION_COL_DEFAULT_WIDTH);
    }
    try {
      fileDateColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(FILEDATE_COL_WIDTH)));
    } catch (Exception e) {
      primaryStage.setX(FILEDATE_COL_DEFAULT_WIDTH);
    }
  }

  /**
   * Save the string value that was edited using some TextField
   * depending from the current column into the correct property
   *
   * @param row      the table row indicating the the mediaFile where to set the values
   * @param column   the column indicating the property  to be set
   * @param newValue the value to be set
   */
  public synchronized void saveEditedValue(TableRow row, TableColumn column, String newValue) {
    MediaFile mediaFile = (MediaFile) row.getItem();
    saveEditedValue(mediaFile, column, newValue);
  }

  /**
   * Save the string value that was edited using some TextField
   * depending from the current column into the correct property
   *
   * @param mediaFile the the mediaFile where to set the values
   * @param column    the column indicating the property  to be set
   * @param newValue  the value to be set
   */
  public synchronized void saveEditedValue(MediaFile mediaFile, TableColumn column, String newValue) {
    if (mediaFile != null) {      //can be null e.g. if file has been changed externally while table in edit mode
      if (column == prefixColumn) {
        mediaFile.setPrefix(newValue);
      } else if (column == counterColumn) {
        mediaFile.setCounter(newValue);
      } else if (column == separatorColumn) {
        mediaFile.setSeparator(newValue);
      } else if (column == descriptionColumn) {
        mediaFile.setDescription(newValue);
      } else if (column == extensionColumn) {
        mediaFile.setExtension(newValue);
      } else if (column == fileDateColumn) {
        mediaFile.setModifiedDate(newValue);
      }
    }
  }

  /**
   * lookup the passed filename in the currently displayed list and look at the physical filenames on disk
   *
   * @param fileName will be compared with physical filename of MediaFiles
   * @return true if the selection was successful
   */
  public boolean selectRowByPath(String fileName) {
    //select the passed image/media file (initialFileOrFolder) or the first if this could not be found (e.g. if a folder was passed)
    boolean found = false;
    if (!mediaFileList.getFileList().isEmpty() && fileName != null) {
      File f = new File(fileName);      //file class used for parsing the name
      int i = mediaFileList.searchPhysicalFilename(f.getName());
      found = (i >= 0);
      if (found) {
        this.getSelectionModel().clearAndSelect(i);
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            scrollViewportToIndex(i, Alignment.CENTER);
          }
        });
      }
    }
    return found;
  }

  /**
   * goto first line in table
   */
  public void selectFirstLine() {
    if (!mediaFileList.getFileList().isEmpty()) {
      getSelectionModel().clearAndSelect(0);
    }
  }

  /**
   * continue search from beginning uses this in FindReplaceDialog
   * to find out if it makes sense: do not continue if
   * - it has already been continued or
   * - user already started in the beginning
   *
   * @return true if exactly one line is selected and this is the first one
   * false if other selection or empty table
   */
  public boolean isFirstLineSelectedOnly() {
    if (!mediaFileList.getFileList().isEmpty()) {
      return ((getSelectionModel().getSelectedItems().size() == 1) && getSelectionModel().isSelected(0));
    } else
      return false;
  }

  /**
   * show a message Dialog if there are any pending changes in the file list
   * if there are no changes pending: continue=true is returned
   * if the user selects to save first: saveFolder() is called and continue=true is returned
   * if the user selects No (continue without saving): continue=true is returned without saving
   * if the user closes the window or pushes cancel-button: continue=false is returned
   *
   * @return continue (true, if the user wants to continue)
   */
  public boolean askIfContinueUnsavedChanges() {
    int unsavedChanges = getUnsavedChanges();

    if (unsavedChanges > 0) {
      String MessageLabel;
      if (unsavedChanges == 1) {
        MessageLabel = language.getString("there.is.an.unsaved.change");
      } else {
        MessageLabel = MessageFormat.format(language.getString("there.are.0.unsaved.changes"), Integer.toString(unsavedChanges));
      }

      int result = new MessageBox(primaryStage, MessageLabel,
          MessageBox.USER_BTN + MessageBox.NO_BTN + MessageBox.CANCEL_BTN,
          language.getString("save")).showModal();
      if ((result == MessageBox.CANCEL_BTN) || (result == MessageBox.NONE_BTN)) {
        return false;
      } else if (result == MessageBox.USER_BTN) {
        saveFolder();
      }
    }
    return true; //if Save or No has been selected (i.e. not Cancel)
  }

  /**
   * OpenDialog to open a file or directory
   */
  public void chooseFileOrFolder() {
    if (askIfContinueUnsavedChanges()) {
      //DirectoryChooser dirChooserDialog = new DirectoryChooser();
      FileChooser dirChooserDialog = new FileChooser();  //DirectoryChooser doesn't provide preview into directories (no files are displayed under Windows)
      dirChooserDialog.setTitle(language.getString("kissphoto.select.a.folder"));
      if (mediaFileList.getCurrentFolder() != null && Files.exists(mediaFileList.getCurrentFolder()))  //only if something has been loaded before
        dirChooserDialog.setInitialDirectory(mediaFileList.getCurrentFolder().toFile());
      //File dir = dirChooserDialog.showDialog(primaryStage);
      File dir = dirChooserDialog.showOpenDialog(primaryStage);
      if (dir != null) {
        openFolder(dir.toPath(), false); //false, because already cared for unsaved changes
        resetSortOrder();
      }
    }
  }

  /**
   * try to select the row of the table by searching for the path of a physical filename
   * If not found then select first row of the table
   * @param file path to the physical Filename
   */
  public void selectRowByPath(final Path file) {
    if (!selectRowByPath(file.getFileName().toString())) { //try to open the selected file, if the selection was a folder it is not found
      getSelectionModel().selectFirst();    //then select the first element
      scrollViewportToIndex(0, Alignment.TOP);
    } else {
      scrollViewportToIndex(getSelectionModel().getSelectedIndex(), Alignment.CENTER); //if file found and selected then make the selection visible
    }
  }

  public void onFolderChanged(String filename, WatchEvent.Kind kind) {
    //--> ik: folder update was signalled to slowly: don't update list automatically
    /*mediaFileList.onFolderChanged(filename, kind);
    if modified file was currently selected then update mediaContentView
    if (getSelectionModel().getSelectedItem() == mediaFileList.getMediaFile(filename)) {
      mediaContentView.setMedia((MediaFile) getFocusModel().getFocusedItem());
    }
    */
    statusBar.showError(language.getString("underlying.directory.has.changed.new.file.s.have.been.added.to.the.end.of.the.list"));
  }

  public void stopWatcherThread() {
    fileChangeWatcher.stopWatcherThread();
  }

  /**
   * open a file or folder (from string=path)
   * If the string is a path to a file then the containing folder is opened and the file is focused
   * if this file is invalid then then containing folder is opened and the first file is focused
   * if the string is a path to a folder then the folder is opened and the first file is focused
   *
   * @param fileOrFolderName string path to the file or folder
   * @param askForUnsavedChanges if true and ansaved changes are pending then a dialog opens, if false the caller must care itself for it
   */
  public void openFolder(String fileOrFolderName, boolean askForUnsavedChanges) {
    openFolder(Paths.get(fileOrFolderName), askForUnsavedChanges);
  }

  /**
   * if unsaved changes are pending a dialog opens to ask if the should be saved
   * 
   * open a file or folder (from File)
   * If the file-object is a file then the containing folder is opened and the file is focused
   * if this file is invalid then then containing folder is opened and the first file is focused
   * if the file-object is a folder then the folder is opened and the first file is focused
   *
   * @param fileOrFolder file object representing a file or folder
   * @param askForUnsavedChanges if true and unsaved changes are pending then a dialog opens, if false the caller must care itself for it
   */
  public synchronized void openFolder(final Path fileOrFolder, boolean askForUnsavedChanges) {
    if (!askForUnsavedChanges || askIfContinueUnsavedChanges()) {

      //update history-entry of current selection
      if (lastSelection != null) {
        fileHistory.refreshOpenedFileInHistory(lastSelection.getFileOnDisk());
      }

      lastSelection = null;  //invalidate the remembering (which is e.g. for resort)

      Path newFileOrFolder = null;
      //detect a reopen
      //if current directory is same as fileOrFolder change it to the current selection

      boolean reopened = (mediaFileList.getCurrentFolder() != null) && mediaFileList.getCurrentFolder().equals(PathHelpers.extractFolder(fileOrFolder)); //same folder

      if (reopened) {
        if (getFocusModel().getFocusedItem() != null)
          newFileOrFolder = ((MediaFile) getFocusModel().getFocusedItem()).getFileOnDisk(); //--> reopen current selection
      } else
        newFileOrFolder = fileOrFolder;  //normal open

        final FileTableView fileTableView = this; //for handing over to the runLater event

        final Scene primaryScene = primaryStage.getScene();
        if (primaryScene != null)
          primaryScene.setCursor(Cursor.WAIT); //can be null during openInitialFolder() called from main()

        statusBar.showMessage(MessageFormat.format(language.getString("trying.to.open.0"), fileOrFolder.toString()));


        String errMsg = "";
        try {
          getSelectionModel().clearSelection();  //prevent the selection listener from doing nonsense while loading
          errMsg = mediaFileList.openFolder(newFileOrFolder);

          if (errMsg.length() == 0) {
            primaryStage.setTitle(KissPhoto.KISS_PHOTO + KissPhoto.KISS_PHOTO_VERSION + " - " + mediaFileList.getCurrentFolderName());
            if (reopened)
              statusBar.showMessage(MessageFormat.format(language.getString("0.reopend"), mediaFileList.getCurrentFolderName()));
            else
              statusBar.showMessage(MessageFormat.format(language.getString("0.files.opened"), Integer.toString(getMediaFileList().getFileList().size())));

            statusBar.showFilesNumber(mediaFileList.getFileList().size());
            numberingOffset = 1;  //determines with which number renumbering of the list starts.
            numberingStepSize = 1;
            numberingDigits = 0;   //zero is [auto]

            setItems(mediaFileList.getFileList());
            selectRowByPath(newFileOrFolder);

            fileHistory.putOpenedFileToHistory(newFileOrFolder);
          }
          registerStatisticsPanel();


          //register a file watcher for watching out for changes to this folder from external applications
          try {
            //register stops the old thread and starts an new for the new folder to register
            fileChangeWatcher.registerFolderToWatch(mediaFileList.getCurrentFolderName(), fileTableView);   //openFolder (above) already has set the currentFolderName
          } catch (Exception e) {
            //in Case of error the function does not exist to update the folder in background..so what...
          }
          //set default sorting
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              resetSortOrder();
            }
          });

        } finally {
          //empty directory?
          if (mediaFileList.getFileList().size() < 1) {
            //clear any visible image shown before
            fileTableView.mediaContentView.setMedia(null, null);
          }
          if (primaryScene != null) primaryScene.setCursor(Cursor.DEFAULT);
          if (errMsg.length() > 0) {
            statusBar.showError(MessageFormat.format(language.getString("could.not.open.0"), fileOrFolder.toString()));
          }
        }
      }
      refresh();
      requestFocus(); //if full-screen is active then after a dialog the main window should be active again
      primaryStage.requestFocus();


  }

  /**
   * Status Bar's statistics are registered with the currently loaded MediaFileList and Selection
   */
  private void registerStatisticsPanel() {
    //register statistics-changes
    mediaFileList.getFileList().addListener(new ListChangeListener<MediaFile>() {
      @Override
      public void onChanged(Change<? extends MediaFile> c) {
        while (c.next()) {
          if (c.wasAdded() || c.wasRemoved()) {
            statusBar.showFilesNumber(mediaFileList.getFileList().size());
          }
        }
      }
    });
    getSelectionModel().getSelectedIndices().addListener(new ListChangeListener() {
      @Override
      public void onChanged(Change c) {
        while (c.next()) {
          if (c.wasAdded() || c.wasRemoved()) {
            statusBar.showSelectedNumber(getSelectionModel().getSelectedIndices().size());
          }
        }
      }
    });
    mediaFileList.getDeletedFileList().addListener(new ListChangeListener<MediaFile>() {
      @Override
      public void onChanged(Change<? extends MediaFile> c) {
        while (c.next()) {
          if (c.wasAdded() || c.wasRemoved()) {
            statusBar.showDeletedNumber(mediaFileList.getDeletedFileList().size());
          }
        }
      }
    });

    //note: modifications are not yet registered, because they are not tracked in a observable list, but es attributes of MediaFile (decentral)
    //it's not worth to search for them every time a change has been made only to show the number
    //therefore the modificationNumber remains invisible as initialized in StatisticsPanel
  }

  /**
   * reopen the same folder again for discarding all changes and try to select the same file again
   * If there are unsaved changes a message box asks "if sure"
   */
  public void reOpenFolder() {
    if (getFocusModel().getFocusedItem() != null)//if currently something selected
      openFolder(((MediaFile) getFocusModel().getFocusedItem()).getFileOnDisk(), true); //the file will be selected again if possible
    else
      openFolder(mediaFileList.getCurrentFolder(), true); //the first file will be selected
  }

  /**
   * save all changes which the user has applied to the file table to the disk
   * (rename, time stamp, rotate, flip, ...)
   */
  public synchronized void saveFolder() {

    fileChangeWatcher.pauseWatching();  //ignore own changes in filesystem
    MediaFile currentFile = (MediaFile) getFocusModel().getFocusedItem();

    //if currentFile isChanged then stop players to enable renaming (keep it simple: do it always while saving)
    mediaContentView.getPlayerViewer().resetPlayer(); //stop players and disconnect from file to enable renaming

    //use a savingTask for saving so that the UI can update in between (e.g. showing the progressBar)
    MediaFileListSavingTask savingTask = mediaFileList.getNewSavingTask();
    statusBar.getProgressProperty().bind(savingTask.progressProperty());
    statusBar.showProgressBar();
    statusBar.showMessage(MessageFormat.format(language.getString("saving.0.changes"), getUnsavedChanges()));

    //define what happens when task has finished
    savingTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
      new EventHandler<WorkerStateEvent>() {

        @Override
        public void handle(WorkerStateEvent t) {
          mediaContentView.setMedia(currentFile, null); //continue playing

          fileChangeWatcher.continueWatching();

          statusBar.clearProgress();
          int errorCount = savingTask.getValue();

          if (errorCount > 0) {
            statusBar.showError(MessageFormat.format(language.getString("errors.occurred.during.saving.check.status.column.for.details"), getUnsavedChanges(), MediaFile.STATUSFLAGS_HELPTEXT));
          } else {
            statusBar.showMessage(language.getString("changes.successfully.written.to.disk"));
          }
        }
      });

    //and start the task in a new thread
    mediaFileList.startSavingTask(savingTask);

    //here is how a cancel button could be implemented (not yet used as saving should no be interrupted)
//    Button cancelButton = new Button("Cancel");
//    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
//      @Override
//      public void handle(ActionEvent event) {
//        cancelButton.setDisable(true);
//        savingTask.cancel(true);  //mayInterruptIfRunning really true? think over...
//      }
//    });
  }

  /**
   * show up file chooser dialog and export the current file list to CSV
   */
  public void chooseFilenameAndExportToCSV() {
    FileChooser fileChooserDialog = new FileChooser();
    fileChooserDialog.setTitle(language.getString("kissphoto.export.current.file.list.into.a.csv.file"));
    if (mediaFileList.getCurrentFolder() != null)  //only if something has been loaded before
      fileChooserDialog.setInitialDirectory(new File(mediaFileList.getCurrentFolder().toAbsolutePath().toString()));
    fileChooserDialog.setInitialFileName("kissPhotoFileList.csv");
    fileChooserDialog.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(language.getString(WriteFolderStructureCSVDialog.COMMA_SEPARATED_VALUES_FILE_SPREADSHEET), "*.CSV"),
        new FileChooser.ExtensionFilter(language.getString(WriteFolderStructureCSVDialog.ALL_FILES), "*.*")
    );

    File file = fileChooserDialog.showSaveDialog(primaryStage);   //already asks if existing file should be replaced ;-)
    if (file != null) {
      try {
        mediaFileList.exportToCSV(file);
        statusBar.showMessage(language.getString("csv.file.successfully.written.to.disk"));
      } catch (Exception e) {
        statusBar.showError(language.getString("csv.file.could.not.be.written.to.disk"));
      }
    }
  }

  /**
   * Cut selected lines:  save the selection internally and delete the files from the list (move to undeletion list)
   * todo Cutting to system clipboard as csv should also be supported
   */
  public void cutToClipboard() {
    deleteSelectedFiles(true);
    pasteMenuItem.setDisable(false); //pasting is now possible
    statusBar.showMessage(language.getString("use.edit.paste.ctrl.v.to.paste.into.a.new.location.of.the.list"));
  }

  /**
   * paste previously cutted lines before current line --> move files using clipboard
   * note: pasting from system clipboard is not planned (what should it do...move/copy files on disk?...does not fit the UI concept...)
   */
  public void pasteFromClipboard() {
    ObservableList<MediaFile> pastedFiles = mediaFileList.paste(getSelectionModel().getFocusedIndex());

    //select the pasted files
    if (pastedFiles != null && pastedFiles.size() > 0) {
      getSelectionModel().clearSelection();
      for (MediaFile mediaFile : pastedFiles) {
        getSelectionModel().select(mediaFile);
      }
    }
    pasteMenuItem.setDisable(true); //pasting has finished and is no longer possible
    statusBar.showMessage(language.getString("use.edit.renumber.to.fix.the.new.location.or.view.reset.sorting.for.restoring.old.locations"));
  }

  /**
   * move up all selected Media Files in the Table View(and the selection and the focus):
   * for every selected Media File
   * --> exchanging the prefix and the numbering with the file above
   * <p/>
   * if the selection contains the first file already: nothing will happen
   */
  public synchronized void moveSelectedFilesUp() {
    if ((getSelectionModel()).getSelectedIndices().size() > 0) {  //only if a selection exists to be moved

      mediaFileList.disablePreLoad();  //no preload into cache while moving multiple files to prevent from access violations
      try {
        //copy and sort the selection
        ObservableList<Integer> selectedIndicesSorted = getCopyOfSelectedIndicesSortedAndUnique();

        //check if selection already contains first file (=index 0)
        boolean firstFileSelected = (selectedIndicesSorted.get(0) == 0);

        if (!firstFileSelected) { //not the first (but something) is selected
          int focusIndex = getFocusModel().getFocusedIndex();

          //move
          int current;
          for (int i = 0; i < selectedIndicesSorted.size(); i++) {
            current = selectedIndicesSorted.get(i); //convert Integer to int
            swapMediaFiles(current - 1, current); //move content
            selectedIndicesSorted.set(i, current - 1); //move selection -1=one up
          }

          //renew selection, which has been lost during swapMediaFiles()
          for (Integer anIndex : selectedIndicesSorted) {
            (getSelectionModel()).select(anIndex.intValue());
          }
          //set new focus (has been changed by select() calls)
          getFocusModel().focus(focusIndex - 1);
          scrollViewportToIndex(getFocusModel().getFocusedIndex(), Alignment.TOP);
        }
      } finally {
        mediaFileList.enablePreLoad();
      }
    }
  }

  /**
   * move down all selected Media Files in the Table View(and the selection and the focus):
   * for every selected Media File
   * --> exchanging the prefix and the numbering with the file below
   * <p/>
   * if the selection contains the last file already or no file: nothing will happen
   */
  public synchronized void moveSelectedFilesDown() {
    if ((getSelectionModel()).getSelectedIndices().size() > 0) {  //only if a selection exists to be moved
      mediaFileList.disablePreLoad();  //no preload into cache while moving multiple files to prevent from access violations
      try {
        //copy and sort the selection
        ObservableList<Integer> selectedIndicesSorted = getCopyOfSelectedIndicesSortedAndUnique();

        //check if selection already contains last file (=index fileList.size-1)
        boolean lastFileSelected = (selectedIndicesSorted.get(selectedIndicesSorted.size() - 1) == (mediaFileList.getFileList().size() - 1));

        if (!lastFileSelected) { //not the first (but something) is selected
          int focusIndex = getFocusModel().getFocusedIndex();

          //move
          int current;
          for (int i = 0; i < selectedIndicesSorted.size(); i++) {
            current = selectedIndicesSorted.get(i); //convert Integer to int
            swapMediaFiles(current, current + 1); //move content
            selectedIndicesSorted.set(i, current + 1); //move selection +1=one down
          }

          //renew selection, which has been lost during swapMediaFiles()
          for (Integer anIndex : selectedIndicesSorted) {
            (getSelectionModel()).select(anIndex.intValue());
          }

          //set new focus (has been changed by select() calls)
          getFocusModel().focus(focusIndex + 1);
          scrollViewportToIndex(getFocusModel().getFocusedIndex(), Alignment.BOTTOM);
        }
      } finally {
        mediaFileList.enablePreLoad();
      }
    }
  }

  /**
   * Helper for moving up/down files
   * swap contents and swap the position in the fileList of [oneIndex] and the [otherIndex]
   * !!!! side effect !!!!!!: the selection of this FileTableView is lost!!!!!!!!!!!
   * --> save it before calling this method and restore it later if necessary
   *
   * @param oneIndex   in fileList to swap
   * @param otherIndex in fileList to swap
   */
  private void swapMediaFiles(int oneIndex, int otherIndex) {
    MediaFile currentMediaFile = mediaFileList.getFileList().get(oneIndex);
    MediaFile otherMediaFile = mediaFileList.getFileList().get(otherIndex);

    //swap contents:
    String prefix = currentMediaFile.getPrefix();
    String counter = currentMediaFile.getCounter();
    currentMediaFile.setPrefix(otherMediaFile.getPrefix());
    currentMediaFile.setCounter(otherMediaFile.getCounter());
    otherMediaFile.setPrefix(prefix);
    otherMediaFile.setCounter(counter);

    //move: exchange the elements in fileList  //side effect: selection is lost
    mediaFileList.getFileList().set(otherIndex, currentMediaFile);
    mediaFileList.getFileList().set(oneIndex, otherMediaFile);
  }

  /**
   * renumber selection or all
   * according the user settings in RenumberDialog
   * note: if globalNumbering is used (param is initialization for dialog) then the start- step- and digit-values
   * are stored for the currently loaded list in FileTableView as numberingOffset, numberingStepSize and numberingDigits
   * The values stepSize and Digits are stored for next renumbering
   * if renumbering is initialized with globalNumbering the dialog is initialized with global numberingOffset (start) values and the new value is stored
   * if renumbering is initialized with local Numbering (global=false) the dialog start offset is initialized with smallest number found in selection
   * if renum_all was chosen in the dialog the start value will also be stored as numberingOffset
   *
   * @param globalNumbering true:ie. numbering is calculated relative to the index of each file to be renumbered, false: local numbering scheme
   */
  public synchronized void renumberWithDialog(boolean globalNumbering) {
    //initialize start index
    int start = numberingOffset; //initialize with global numbering

    if (!globalNumbering) { //then lookup the minimum value in the selection
      start = Integer.MAX_VALUE;
      ObservableList<MediaFile> selection = getSelectionModel().getSelectedItems();
      if (selection == null) {
        start = 1;
      } else {
        for (MediaFile mediaFile : selection) {
          if (mediaFile.getCounterValue() < start) start = mediaFile.getCounterValue();
        }
      }
    }

    //show dialog
    if (renumberDialog == null) renumberDialog = new RenumberDialog(primaryStage);
    int result = renumberDialog.showModal(start, numberingStepSize, numberingDigits, globalNumbering);

    //execute numbering and store dialog values for next renumbering
    if (result != RenumberDialog.CANCEL_BTN && result != RenumberDialog.NONE_BTN) {
      start = renumberDialog.getStart();
      numberingStepSize = renumberDialog.getStep();
      numberingDigits = renumberDialog.getDigits();
      boolean global = renumberDialog.getGlobal();

      if (result == RenumberDialog.RENUM_ALL_BTN) getSelectionModel().selectAll();
      if (global || (result == RenumberDialog.RENUM_ALL_BTN))
        numberingOffset = start; //remember start as global numbering offset for this list

      if (global) {
        mediaFileList.renumberRelativeToIndices(start, numberingStepSize, numberingDigits, getSelectionModel().getSelectedIndices());
      } else {
        mediaFileList.renumber(start, numberingStepSize, numberingDigits, getSelectionModel().getSelectedIndices());
      }
    }
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    primaryStage.requestFocus();
  }

  /**
   * show up Find&Replace Dialog
   */

  public synchronized void findAndReplace() {
    //initialize with first of selected files
    if (findReplaceDialog == null) findReplaceDialog = new FindReplaceDialog(primaryStage, this);

    //---show dialog
    findReplaceDialog.showModal();
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    primaryStage.requestFocus();
  }

  /**
   * rename selected line...or if multiple lines are selected call renameWithDialog()
   * if no column is currently selected, then select descriptionColumn
   */

  public synchronized void rename() {
    if (getSelectionModel().getSelectedItems().size() > 1) {
      renameWithDialog();
    } else {
      if (getFocusModel().getFocusedCell().getTableColumn() == null) {
        getFocusModel().focus(getFocusModel().getFocusedIndex(), descriptionColumn);
      }
      setEditable(true);
      edit(getFocusModel().getFocusedIndex(), getFocusModel().getFocusedCell().getTableColumn());

      //editable will reset in TextFieldCell on CommitEdit
    }

  }

  /**
   * Rename all selected files showing up a dialog first
   * The initialization of the dialog is based on the first selected file
   */
  public synchronized void renameWithDialog() {
    int result;
    MediaFile firstSelectedFile;

    //initialize with first of selected files
    if (renameDialog == null) renameDialog = new RenameDialog(primaryStage);

    //copy and sort the selection for determining the "fist selected"
    ObservableList<Integer> selectedIndicesSorted = FXCollections.observableArrayList(getSelectionModel().getSelectedIndices()); //copy
    FXCollections.sort(selectedIndicesSorted);
    try {
      firstSelectedFile = mediaFileList.getFileList().get(selectedIndicesSorted.get(0));
    } catch (Exception e) {
      firstSelectedFile = null;
    }

    //---show dialog
    renameDialogActive = true;
    try {
      if (firstSelectedFile != null) {
        result = renameDialog.showModal(firstSelectedFile.getPrefix(), firstSelectedFile.getSeparator(), firstSelectedFile.getDescription(), firstSelectedFile.getExtension());
      } else {
        result = renameDialog.showModal("", "", "", "");
      }
      //---execute renaming
      if (result == RenameDialog.RENAME_BTN) {
        ObservableList<MediaFile> selectedFiles = getSelectionModel().getSelectedItems();
        for (MediaFile m : selectedFiles) {
          m.rename(renameDialog.isPrefixChecked(), renameDialog.getPrefix(),
              renameDialog.isSeparatorChecked(), renameDialog.getSeparator(),
              renameDialog.isDescriptionChecked(), renameDialog.getDescription(),
              renameDialog.isExtensionChecked(), renameDialog.getExtension());
        }
      }
    } finally {
      renameDialogActive = false;
    }
    refresh();
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    primaryStage.requestFocus();
  }

  /**
   * @return the mediaFile which is one line above the selected mediaFile or null if current media file is the first
   */
  public MediaFile getAboveMediaFile() {
    int selection = getSelectionModel().getSelectedIndex();

    if (selection == 0)
      return null;
    else
      return mediaFileList.getFileList().get(selection - 1);
  }

  /**
   * auto fill from first selected lines to the other selected lines
   *
   * if multiple lines are selected
   * - Prefix
   * - separator
   * - and description
   * are copied from the first of the selected lines to all other selected lines (like autofill in excel)
   *
   * if just one line is selected copying will be performed from the above line (if there is any. if not nothing will happen)
   * if in edit-mode nothing happens (this would have to make TextFieldCell
   */
  public void copyDescriptionDown() {
    MediaFile firstSelectedFile = null;
    MediaFile otherSelectedFile = null;

    if (isEditMode()) return;

    //multiple selected lines -> copy first to all others
    if (getSelectionModel().getSelectedIndices().size() > 1) {
      //copy and sort the selection for determining the "fist selected"
      ObservableList<Integer> selectedIndicesSorted = FXCollections.observableArrayList(getSelectionModel().getSelectedIndices()); //copy
      FXCollections.sort(selectedIndicesSorted);
      try {
        firstSelectedFile = mediaFileList.getFileList().get(selectedIndicesSorted.get(0));
      } catch (Exception e) {
        //firstSelectedFile remains null in case of exception
      }
      if (firstSelectedFile != null) {
        for (Integer indexObject : selectedIndicesSorted) {
          otherSelectedFile = mediaFileList.getFileList().get(indexObject);
          copyFileDescriptions(firstSelectedFile, otherSelectedFile);
        }
      }

    } else if (getSelectionModel().getSelectedIndices().size() == 1) {
      int selectedIndex = getSelectionModel().getSelectedIndex();
      if (selectedIndex > 0) { //not the first file because there is none above to copy from
        try {
          firstSelectedFile = mediaFileList.getFileList().get(selectedIndex - 1); //the file above the selected is the source
          otherSelectedFile = mediaFileList.getFileList().get(selectedIndex); //the selected file is the target
        } catch (Exception e) {
          //first/otherSelectedFile remain null in case of exception
        }
        if (firstSelectedFile != null && otherSelectedFile != null) {
          copyFileDescriptions(firstSelectedFile, otherSelectedFile);
        }
      }

    } //else size()==0 == nothing selected and nothing will happen
    refresh();

  }

  /**
   * copy the descriptive fields from sourceFile to copyToFile
   *
   * @param sourceFile copy from
   * @param copyToFile copy to
   */
  public void copyFileDescriptions(MediaFile sourceFile, MediaFile copyToFile) {
    copyToFile.setPrefix(sourceFile.getPrefix());
    copyToFile.setSeparator(sourceFile.getSeparator());
    copyToFile.setDescription(sourceFile.getDescription());
  }

  /**
   * renumbers all files of the fileList by writing column "counter"  (independent of the current selection)
   * (all files are selected in a first step)
   * Leading zeros are used to get at least the number of digits determined by param digits
   * the numbering begins with start and uses step as step size<br><br>
   * This variation will calculate the numbering for every file from its position (index) in fileList
   * i.e. a file at index idx gets the number: counter = idx*step + start
   * <p/>
   * starting number, step and digits are used as set for the currently loaded list
   * (see members numberingOffset, numberingStepSize, numberingDigits)
   */
  public synchronized void renumberSelectionRelativeToIndices() {
    if (mediaFileList.getFileList().isEmpty()) return; //if nothing is opened

    getSelectionModel().selectAll();
    mediaFileList.renumberRelativeToIndices(numberingOffset, numberingStepSize, numberingDigits, getSelectionModel().getSelectedIndices());
  }

  /**
   * move all files to be deleted to deletion list and remove them from the FileTableView
   * when saving (File-Save) they are deleted physically from disk
   * The Deletion List can be used for undeletion until saving
   * <p/>
   * The selection is moved after the last deleted row (or last row of the table if the last row has been deleted)
   *
   * @param cutToClipboard: true means pasting is enabled, ie. the deleted files are additionally stored in the pasting list (for moving)
   *                        false means the selection is only moved to the deletion list
   */
  public synchronized void deleteSelectedFiles(boolean cutToClipboard) {

    if (mediaFileList.getFileList().isEmpty())
      return;  //if nothing is opened or nothing is selected for any other reason

    //-----------Calculate new selection after deletion
    int newSelectionIndex = -1; //negative = no selection
    //copy and sort the selection
    ObservableList<Integer> selectedIndicesSorted = getCopyOfSelectedIndicesSortedAndUnique();

    newSelectionIndex = selectedIndicesSorted.get(selectedIndicesSorted.size() - 1) + 1; //last selected row + 1
    newSelectionIndex = newSelectionIndex - selectedIndicesSorted.size(); //this index will be lowered by the number of deleted items

    //--------delete
    //copy selection list (otherwise the selection will change while deletion - which leads to random results)
    ObservableList<MediaFile> deletionList = FXCollections.observableArrayList(getSelectionModel().getSelectedItems());
    getSelectionModel().clearSelection();  //all previously marked files will deleted, so remove the selection and prevent from events while deletion on the selection

    mediaFileList.deleteFiles(deletionList, cutToClipboard);

    //----- select row after last selected deleted files
    if (newSelectionIndex > (mediaFileList.getFileList().size() - 1)) { //if last file has been deleted
      newSelectionIndex = mediaFileList.getFileList().size() - 1;
    }
    if (newSelectionIndex >= 0) {
      getSelectionModel().select(newSelectionIndex);
      if (mediaFileList.getFileList().size() > 0) getFocusModel().focus(newSelectionIndex);
      scrollViewportToIndex(newSelectionIndex, Alignment.CENTER);
    }
    statusBar.showMessage(MessageFormat.format(language.getString("0.file.s.marked.for.deletion.files.can.be.recovered.using.edit.undelete.until.next.save"), deletionList.size()));
    if (unDeleteMenuItem != null)
      unDeleteMenuItem.setDisable(mediaFileList.getDeletedFileList().size() < 1); //enable Menu for undeletion if applicable
  }

  /**
   * perform the handed rotateOperation to all selected Images
   * Only Images are affected - all other selected files are ignored
   *
   * @param rotateOperation
   */
  public synchronized void rotateSelectedFiles(ImageFileRotater.RotateOperation rotateOperation) {
    int imageFilesCount = mediaFileList.getImageFileCount(getSelectionModel().getSelectedItems());
    mediaFileList.rotateSelectedFiles(getSelectionModel().getSelectedItems(), rotateOperation);
    mediaContentView.showRotationAndFlippingPreview();
    statusBar.showMessage(MessageFormat.format(language.getString("0.images.rotated"), imageFilesCount));
  }

  /**
   * perform flipping (mirroring) of all selected Images
   * Only Images are affected - all other selected files are ignored
   *
   * @param horizontally = true: mirror horizontally, false: mirror vertically
   */
  public synchronized void flipSelectedFiles(boolean horizontally) {
    int imageFilesCount = mediaFileList.getImageFileCount(getSelectionModel().getSelectedItems());
    mediaFileList.flipSelectedFiles(getSelectionModel().getSelectedItems(), horizontally);
    mediaContentView.showRotationAndFlippingPreview();
    statusBar.showMessage(MessageFormat.format(language.getString("0.images.flipped"), imageFilesCount));
  }

  public synchronized void setOrientationAccordingExif() {
    int imageFilesCount = mediaFileList.getImageFileCount(getSelectionModel().getSelectedItems());
    mediaFileList.setOrientationAccordingExif(getSelectionModel().getSelectedItems());
    mediaContentView.showRotationAndFlippingPreview();
    statusBar.showMessage(MessageFormat.format(language.getString("0.images.oriented.according.exif.information"), imageFilesCount));
  }

  private ObservableList<Integer> getCopyOfSelectedIndicesSortedAndUnique() {
    ObservableList<Integer> selectedIndicesSorted = FXCollections.observableArrayList(((MultipleSelectionModel) getSelectionModel()).getSelectedIndices()); //copy
    FXCollections.sort(selectedIndicesSorted);
    //remove doubles from sorted list (for what ever reason the index of the focused line is multiple times in the selection list???)
    int currentValue = -1;
    int i = 0;
    while (i < selectedIndicesSorted.size()) {
      if (selectedIndicesSorted.get(i) != currentValue) {//new value found
        currentValue = selectedIndicesSorted.get(i);
        i++;
      } else { //remove double
        selectedIndicesSorted.remove(i);
      }
    }
    return selectedIndicesSorted;
  }

  /**
   * call this method to execute the editor specified for the current selection
   * The type of the first selected file determines the editor to call
   * All selected media files will be passed to that editor as parameters
   * if nothing is selected, nothing will happen
   *
   * @param mainEditor true to call the mainEditor, false to call the 2nd Editor
   */
  public void executeExternalEditorForSelection(boolean mainEditor) {
    if (askIfContinueUnsavedChanges())
      ExternalEditorsDialog.executeExternalEditor(getSelectionModel().getSelectedItems(), mainEditor);
  }

  public synchronized void unDeleteWithDialog() {
    if (mediaFileList.getDeletedFileList().size() > 0) {
      int result = 0;
      ObservableList<MediaFile> unDeletionList = null;

      mediaContentView.getPlayerViewer().pause();  //stop all currently played media because in undeleteDialog there could also be media played
      //show dialog
      if (unDeleteDialog == null) unDeleteDialog = new UnDeleteDialog(primaryStage);
      result = unDeleteDialog.showModal(mediaFileList.getDeletedFileList());

      unDeleteDialog.stopPlayers(); //now stop media from the undeleteDialog so that main window can playback again...

      //execute unDeletion
      if (result == UnDeleteDialog.UNDELETE_SELECTION_BTN && unDeleteDialog.getFilesToUndelete() != null) {
        //make copy of the list selection of the dialog to prevent events in the (closed) dialog while unDeletion
        unDeletionList = FXCollections.observableArrayList(unDeleteDialog.getFilesToUndelete());
        mediaFileList.unDeleteFiles(getFocusModel().getFocusedIndex(), unDeletionList);
        statusBar.showMessage(MessageFormat.format(language.getString("0.file.s.recovered.before.the.previously.selected.row.you.may.want.to.use.view.reset.sorting.columns.from.main.menu"), unDeletionList.size()));
      } else {
        statusBar.clearMessage();
      }

      unDeleteDialog.cleanUp(); //clearMessage internal list of the dialog to enable garbage collection and prevent interference with linked media files
    } else {
      statusBar.showError(language.getString("no.files.marked.for.deletion.therefore.nothing.to.un.delete"));
    }
    if (unDeleteMenuItem != null) unDeleteMenuItem.setDisable(mediaFileList.getDeletedFileList().size() < 1);
    requestLayout();
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    primaryStage.requestFocus();
  }

  /**
   * Search for changes in the filelist
   *
   * @return the number of unsaved changes. 0 means: no changes are unsaved.
   */
  public int getUnsavedChanges() {
    return mediaFileList.getUnsavedChanges();
  }

  /**
   * the columns "prefix", "counter", "separator" and "descriptions" are set to be sorted.
   * This results in a sorting just like the filenames earlier in the standard-ordering of the file explorer
   */
  public void resetSortOrder() {
    MediaFile currentFile = (MediaFile) getSelectionModel().getSelectedItem();
    getSortOrder().clear();
    getSortOrder().add(prefixColumn);
    getSortOrder().add(counterColumn);
    getSortOrder().add(separatorColumn);
    getSortOrder().add(descriptionColumn);
    prefixColumn.setSortType(TableColumn.SortType.ASCENDING);
    counterColumn.setSortType(TableColumn.SortType.ASCENDING);
    separatorColumn.setSortType(TableColumn.SortType.ASCENDING);
    descriptionColumn.setSortType(TableColumn.SortType.ASCENDING);
    prefixColumn.setSortable(true);
    counterColumn.setSortable(true);
    separatorColumn.setSortable(true);
    descriptionColumn.setSortable(true);

    getSelectionModel().clearSelection();
    getSelectionModel().select(currentFile);

    int index = getSelectionModel().getSelectedIndex();
    if (index >= 0) scrollViewportToIndex(index, Alignment.CENTER);
  }

  /**
   * (e.g. when sorting is performed)
   * the latest selected row
   * is selected again, the focus is set and the table is scrolled to make the item visible
   */
  protected void restoreLastSelection() {
    if (lastSelection != null) {
      getSelectionModel().clearSelection();
      getSelectionModel().select(lastSelection);
      getFocusModel().focus(getSelectionModel().getSelectedIndex());
      scrollViewportToIndex(getFocusModel().getFocusedIndex(), Alignment.CENTER);
    }
  }

  /**
   * @return true if currently in Cell-EditMode or Multi-EditMode
   */
  public boolean isEditMode() {
    return (getEditingCell() != null) ||  //cell-Edit-Mode? ...or
        renameDialogActive;           //multi-edit
  }


  /**
   * EventHandler to handle CommitEdit() events from FieldCell in context of FileTableView
   */
  private class CellEditCommitEventHandler implements EventHandler<TableColumn.CellEditEvent<MediaFile, String>> {
    @Override
    public void handle(TableColumn.CellEditEvent<MediaFile, String> t) {
      saveEditedValue(t.getRowValue(), t.getTableColumn(), t.getNewValue());
    }
  }


  public enum Alignment {TOP, CENTER, BOTTOM}

  ;
  /**
   * make index visible
   * The scrollTo()-Method of TableView is similar but "jumps" to the middle of the visible region time by time.
   * Especially while in edit mode this leads to losing the focus, i.e. the cell in edit mode is different to the focussed cell :-(
   * The viewPort trick has been found on StackOverflow :-)
   * <p/>
   * Always try to show two extra lines before/after the line 'index'
   * if index is already visible nothing happens
   *
   * @param indexToShow line to scroll to become visible in viewport
   * @param alignment should the line -index- be aligned top, centered or bottom
   */
  public void scrollViewportToIndex(int indexToShow, Alignment alignment) {
    try {
      if (flow != null) {  //the flow is not valid before it is drawn for the first time. Will be the case every time after loading a directory ;-)

        //calculate how many extra lines make sense
        int height = flow.getLastVisibleCell().getIndex() - flow.getFirstVisibleCell().getIndex() + 1;

        int extraLines = 2;
        if (height < 3) extraLines = 0;
        else if (height < 5) extraLines = 1;
        else extraLines = 2;

        //do nothing if already visible (including extra lines)
        int firstLineWithExtraLines = flow.getFirstVisibleCell().getIndex() + extraLines;
        if (firstLineWithExtraLines > mediaFileList.getFileList().size() - 1)
          firstLineWithExtraLines = mediaFileList.getFileList().size() - 1;
        if (firstLineWithExtraLines < 0) firstLineWithExtraLines = 0;

        int lastLineWithExtraLines = flow.getLastVisibleCell().getIndex() - extraLines;
        if (lastLineWithExtraLines < 0) lastLineWithExtraLines = 0;

        if (indexToShow >= firstLineWithExtraLines && indexToShow <= lastLineWithExtraLines) {
          //nothing needs to be done
        } else {
          int index = 0; //the resulting index where to jump to
          //calculate index to be shown as first line according alignment
          switch (alignment) {
            case TOP:
              index = indexToShow - extraLines;
              break;
            case CENTER:
              index = indexToShow - (height / 2);
              break;
            case BOTTOM:
              index = indexToShow - height + extraLines + 1;
              break;
          }
          if (index > mediaFileList.getFileList().size() - 1) index = mediaFileList.getFileList().size() - 1;
          if (index < 0) index = 0;

          flow.scrollTo(index);     //the new first line
        }
      } else {
        //retry until there is a Viewport available
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            System.out.println("retry " + indexToShow);
            scrollViewportToIndex(indexToShow, alignment);
          }
        });
      }
    } catch (Exception e) {
      //if scrolling is not possible then don't do it (e.g. during Viewport is built newly because of opening an new folder
    }
  }

  //--------------------------- Find & Replace -------------------------------

  //every mediaFileList has one searchRec that is built once.
  // For convenience a pointer to that is searchRec is stored in Filetable while search is active (see findFirst/replaceAll)
  // if no search is active searchRec == null!
  MediaFileList.SearchRec searchRec = null;

  /**
   * find the first occurrence of searchText in the file table (all columns)
   * this is done by initializing all search cursors and calling findNext then
   * <p/>
   * The search starts from the first selected line of current selection in the first column
   *
   * @param searchText the text to be found in the file table
   * @return true if found, false if not found
   */
  public boolean findFirst(String searchText) {
    initSearch();
    return findNext(searchText);
  }

  /**
   * store selection in mediaFileList
   * connect local searchRec to mediaFileList's searchRec (=current search)
   * and also connect textFieldFactory's searchRec for highlighting finding results
   */
  private void initSearch() {
    //pass a copy of selection list to initSearch because selection will change during search
    searchRec = mediaFileList.initSearch(FXCollections.observableArrayList(((MultipleSelectionModel) getSelectionModel()).getSelectedItems()));
  }

  /**
   * find the next occurrence of searchText (that might have been changed since last findFirst/findNext)
   * starting from the last finding position
   * If this method is called without previous findFirst search start from any last finding position or from the start
   * <p/>
   * if found go to edit mode and select the found text, message on status bar
   * if not found: error message on status bar
   *
   * @param searchText the text to be found in the file table
   * @return true if found, false if not found
   */
  public boolean findNext(String searchText) {
    final TableColumn foundCol;

    if (searchRec != null && mediaFileList.searchNext(searchText) && searchRec.foundMediaFile != null) {
      //mark the current occurrence found in the fileTableView

      //set the focus to it
      switch (searchRec.tableColumn) {
        case MediaFile.COL_NO_PREFIX:
          foundCol = prefixColumn;
          break;
        case MediaFile.COL_NO_COUNTER:
          foundCol = counterColumn;
          break;
        case MediaFile.COL_NO_SEPARATOR:
          foundCol = separatorColumn;
          break;
        case MediaFile.COL_NO_DESCRIPTION:
          foundCol = descriptionColumn;
          break;
        case MediaFile.COL_NO_EXTENSION:
          foundCol = extensionColumn;
          break;
        case MediaFile.COL_NO_FILEDATE:
          foundCol = fileDateColumn;
          break;
        default:
          foundCol = descriptionColumn;
      }

      final int foundIndex;
      if (searchRec.searchInSelection)
        foundIndex = mediaFileList.getFileList().indexOf(searchRec.foundMediaFile);  //calculate selection index --> file index
      else
        foundIndex = searchRec.tableRow;

      getFocusModel().focus(foundIndex);
      getSelectionModel().clearAndSelect(foundIndex, foundCol);

      //make it visible
      scrollViewportToIndex(foundIndex, Alignment.CENTER);

      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          selectSearchResultOnNextStartEdit = true;
          setEditable(true);
          edit(foundIndex, foundCol);
        }
      });

      //for status bar add 1 to all values as humans usually count 1, 2, 3, ... not 0, 1, 2,...
      statusBar.showMessage(language.getString("found") + " " +
          language.getString("line") + " " + Integer.toString(searchRec.tableRow + 1) + ", " + language.getString("column") + " " + Integer.toString(searchRec.tableColumn + 1)
          + ", " + language.getString("character.position") + " [" + Integer.toString(searchRec.startPos + 1) + "-" + Integer.toString(searchRec.endPos + 1) + "]"
      );
    } else {
      statusBar.showError(language.getString(NOTHING_FOUND));
    }

    return searchRec.found;
  }

  /**
   * if in field edit mode the current selection is replaced by replaceText then findNext is called
   *
   * @param searchText  the text to be found after replacing
   * @param replaceText the text with which the selection is replaced with (if in field edit mode)
   * @return true if a next occurrence found
   */
  public boolean replaceAndFindNext(String searchText, String replaceText) {
    if (searchRec != null && searchRec.foundMediaFile != null)
      searchRec.foundMediaFile.replaceAccordingSearchRec(replaceText, searchRec);

    return findNext(searchText);
  }

  /**
   * findFirst and replaceAndFindNext is called until nothing more could be found
   * use closeSearchAndRestoreSelection to restore selection after the search after calling replaceAll()
   *
   * @param searchText   the text to be found after replacing
   * @param replaceText  the text with which the selection is replaced with (if in field edit mode)
   * @param doInitSearch true=no prior search can be continued: you want to start searching from the beginning prior calling this method
   * @return the number of replacements performed
   */
  public int replaceAll(String searchText, String replaceText, boolean doInitSearch) {
    if (doInitSearch)
      initSearch();
    else if (searchRec != null && searchRec.foundMediaFile != null)
      searchRec.foundMediaFile.replaceAccordingSearchRec(replaceText, searchRec);

    int counter = mediaFileList.replaceAll(searchText, replaceText);
    if (!doInitSearch)
      counter++; //if this is a continued search on occurance was replace before mediaFileList.replaceAll

    if (counter == 0)
      statusBar.showError(language.getString(NOTHING_FOUND));
    else if (counter == 1)
      statusBar.showMessage(language.getString("one.occurrence.has.been.replaced"));
    else
      statusBar.showMessage(MessageFormat.format(language.getString("0.occurrences.have.been.replaced"), Integer.toString(counter)));

    return counter;
  }

  /**
   * clearMessage the status bar and                     (always)
   * restore the old selection from before search (only if it was searchInSelection mode)
   * that was stored in searchRec
   */
  public void closeSearchAndRestoreSelection() {
    if (searchRec != null && searchRec.searchInSelection && searchRec.selection != null && searchRec.selection.size() > 0) {
      getSelectionModel().clearSelection();
      for (MediaFile mediaFile : searchRec.selection) getSelectionModel().select(mediaFile);

      scrollViewportToIndex(getMediaFileList().getFileList().indexOf(searchRec.selection.get(0)), Alignment.CENTER);
      getFocusModel().focus(getMediaFileList().getFileList().indexOf(searchRec.selection.get(0)));
    }
    statusBar.clearMessage();
    searchRec = null; //pointer to MediaFile's searchRec is deleted to indicate "no search active i.e. call findFirst or replaceAll first)
  }

  /**
   * show a dialog to select a master extension
   * for all files with that extension:
   * Copy the date from the file to all files with the same name (but different file extension)
   */
  public void copyFileDatesByExtension() {
    CopyFileDatesExtDialog dialog = new CopyFileDatesExtDialog(primaryStage, mediaFileList);

    if (dialog.showModal()) {
      //copy dates from master extension to all others with same name
      mediaFileList.copyFileDates(dialog.getResult());
    }
  }

  //--------------------------- getters and setters -------------------------------
  public MediaFileList getMediaFileList() {
    return mediaFileList;
  }

  public void setUnDeleteMenuItem(MenuItem unDeleteMenuItem) {
    this.unDeleteMenuItem = unDeleteMenuItem;
  }

  public void setPasteMenuItem(MenuItem pasteMenuItem) {
    this.pasteMenuItem = pasteMenuItem;
  }
  public FileHistory getFileHistory() {
    return fileHistory;
  }

  public StatusBar getStatusBar() {
    return statusBar;
  }

  //Column-Getters are necessary for Cell in Cellfactory:
//As all Cells are from same type/class it is necessary to choose the correct type of input field dependent from the column
//With these getters the Cell can compare in which column editing is started.
//Alternatively every column would need a differt CellFactory...
  public TableColumn getStatusColumn() {
    return statusColumn;
  }

  public TableColumn getPrefixColumn() {
    return prefixColumn;
  }

  public TableColumn getCounterColumn() {
    return counterColumn;
  }

  public TableColumn getSeparatorColumn() {
    return separatorColumn;
  }

  public TableColumn getDescriptionColumn() {
    return descriptionColumn;
  }

  public TableColumn getExtensionColumn() {
    return extensionColumn;
  }

  public TableColumn getFileDateColumn() {
    return fileDateColumn;
  }

  //Cell needs primary stage for enabling hints in editable textFields and to get focus back to mainWindow from SearchDialog
  public Stage getPrimaryStage() {
    return primaryStage;
  }

  //how many lines are currently in the table
  public int getRowCount() {
    return mediaFileList.getFileList().size();
  }

  //used by Cell to determine if startEdit was called by Searching-Routing and therefore Searchresult should be highlighted
  public boolean isSelectSearchResultOnNextStartEdit() {
    return selectSearchResultOnNextStartEdit;
  }

  public void resetSelectSearchResultOnNextStartEdit() {
    selectSearchResultOnNextStartEdit = false;
  }

  //searchRec is != null only while find/replace is activ
  public MediaFileList.SearchRec getSearchRec() {
    return searchRec;
  }

}
