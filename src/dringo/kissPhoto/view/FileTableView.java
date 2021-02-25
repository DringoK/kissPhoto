package dringo.kissPhoto.view;


import com.drew.lang.annotations.NotNull;
import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.ctrl.FileChangeWatcher;
import dringo.kissPhoto.ctrl.FileChangeWatcherEventListener;
import dringo.kissPhoto.helper.PathHelpers;
import dringo.kissPhoto.model.ImageFileRotater;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileList;
import dringo.kissPhoto.model.MediaFileListSavingTask;
import dringo.kissPhoto.view.dialogs.*;
import dringo.kissPhoto.view.fileTableHelpers.FileHistory;
import dringo.kissPhoto.view.fileTableHelpers.TextFieldCellFactory;
import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.text.MessageFormat;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * JavaFX Table View that is the GUI for MediaFileList
 * <p/>
 *
 * @author Ingo
 * @version 2021-01-16 eliminating the slow and error-prone reflection: column.setCellValueFactory(new PropertyValueFactory<>("propertyname") replaced with call back. ToolTipText added
 * @version 2021-01-09 improve scrolling, save also currently edited value, improve findNext (F3-support)
 * @version 2014-05-02 I18Support, Cursor in Edit-Mode over lines, reopen added etc
 * @since 2012-09-06
 */

public class FileTableView extends TableView<MediaFile> implements FileChangeWatcherEventListener {
  //string constants (i18alized) for table columns' headlines
  public static final String PREFIX = "prefix";
  public static final String COUNTER = "counter";
  public static final String SEPARATOR = "separator";
  public static final String DESCRIPTION = "description";
  public static final String EXTENSION = "extension";
  public static final String MODIFIED = "modified";
  public static final String NOTHING_FOUND = "nothing.found";
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
  //----- Table Columns
  protected final TableColumn<MediaFile, String> statusColumn;
  protected final TableColumn<MediaFile, String> prefixColumn;
  protected final TableColumn<MediaFile, String> counterColumn;
  protected final TableColumn<MediaFile, String> separatorColumn;
  protected final TableColumn<MediaFile, String> descriptionColumn;
  protected final TableColumn<MediaFile, String> extensionColumn;
  protected final TableColumn<MediaFile, String> fileDateColumn;
  //---- views linking
  private final Stage primaryStage;  //main window
  private final StatusBar statusBar;  //messages
  //---- the content to be displayed
  private final MediaFileList mediaFileList = new MediaFileList();
  //---- listen if an external program changes the currently loaded folder
  private final FileChangeWatcher fileChangeWatcher = new FileChangeWatcher();  //check for external changes to an opened folder
  private final FileHistory fileHistory;
  protected MediaContentView mediaContentView; //mediaContentView to show media if selection changes
  protected VirtualFlow<?> flow;  //viewport for scrolling
  //----- Define Cell Factory and EditEventHandler
  //will be the identical for all columns (except statusColumn, see FileTableView constructor)
  TextFieldCellFactory textFieldCellFactory = new TextFieldCellFactory();
  CellEditCommitEventHandler cellEditCommitEventHandler = new CellEditCommitEventHandler();
  //every mediaFileList has one searchRec that is built once.
  // For convenience a pointer to that is searchRec is stored in Filetable while search is active (see findFirst/replaceAll)
  // if no search is active searchRec == null!
  MediaFileList.SearchRec searchRec = null;
  private MediaFile lastSelection = null;
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
  //----- Enable keeping Cursor-Position while editing when changing line
  private boolean selectSearchResultOnNextStartEdit = false;  //during SearchNext searchRec Cursor needs to be set on StartEdit
  private boolean renameDialogActive = false;

  private final Tooltip tooltip = new Tooltip();

  private boolean isMovingFiles = false; //ignore selection refresh events while movingUp/Dn because selection has to be rebuilt with every step there, but effectively it does not change
  /**
   * constructor will try to open the passed file or foldername
   * if there is no such file or folder an empty list is shown
   *
   * @param primaryStage     link back to main window
   * @param mediaContentView link to the view where media is displayed if focus changes
   * @param statusBar        link to statusBar for showing information/errors
   */
  public FileTableView(Stage primaryStage, final MediaContentView mediaContentView, StatusBar statusBar) {
    //remember connections to main window etc
    this.primaryStage = primaryStage;
    this.mediaContentView = mediaContentView;
    this.statusBar = statusBar;
    fileHistory = new FileHistory(this);

    this.setMinSize(100.0, 100.0);

    //set properties of the table
    setEditable(false); //Edit Event shall not be handled by TableView's default, but by the main menu bar
    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    //create Table
    //status: readonly Column to show status flags
    statusColumn = new TableColumn<>(language.getString("status"));
    statusColumn.setPrefWidth(STATUS_COL_DEFAULT_WIDTH);
    statusColumn.setEditable(false);
    //statusColumn.setCellValueFactory(new PropertyValueFactory<MediaFile, String>("status"));
    statusColumn.setCellValueFactory(p -> p.getValue().statusProperty());
    getColumns().add(statusColumn);

    //prefix
    prefixColumn = new TableColumn<>(language.getString(PREFIX));
    prefixColumn.setPrefWidth(PREFIX_COL_DEFAULT_WIDTH);
    prefixColumn.setEditable(true);
    prefixColumn.setOnEditCommit(cellEditCommitEventHandler);
    prefixColumn.setCellFactory(textFieldCellFactory);
    //prefixColumn.setCellValueFactory(new PropertyValueFactory<>(PREFIX));
    prefixColumn.setCellValueFactory(p -> p.getValue().prefixProperty());
    getColumns().add(prefixColumn);


    //counter
    counterColumn = new TableColumn<>(language.getString(COUNTER));
    counterColumn.setPrefWidth(COUNTER_COL_DEFAULT_WIDTH);
    counterColumn.setEditable(true); //as a default counter is editable until renumbering is set to auto-mode
    counterColumn.setCellFactory(textFieldCellFactory);
    counterColumn.setOnEditCommit(cellEditCommitEventHandler);
    counterColumn.setComparator((o1, o2) -> {    //should sort numeric even without leading zeros in the filename
      try {
        int i1 = Integer.parseInt(o1);
        int i2 = Integer.parseInt(o2);
        return Integer.compare(i1, i2);
      } catch (Exception e) {   //e.g. for empty strings: treat them as equal
        return 0;
      }
    });
    //counterColumn.setCellValueFactory(new PropertyValueFactory<>(COUNTER));
    counterColumn.setCellValueFactory(p -> p.getValue().counterProperty());
    getColumns().add(counterColumn);

    //separator
    separatorColumn = new TableColumn<>(language.getString(SEPARATOR));
    separatorColumn.setEditable(true);
    separatorColumn.setPrefWidth(SEPARATOR_COL_DEFAULT_WIDTH);
    separatorColumn.setCellFactory(textFieldCellFactory);
    separatorColumn.setOnEditCommit(cellEditCommitEventHandler);
    //separatorColumn.setCellValueFactory(new PropertyValueFactory<>(SEPARATOR));
    separatorColumn.setCellValueFactory(p -> p.getValue().separatorProperty());
    getColumns().add(separatorColumn);

    //description
    descriptionColumn = new TableColumn<>(language.getString(DESCRIPTION));
    descriptionColumn.setEditable(true);
    descriptionColumn.setPrefWidth(DESCRIPTION_COL_DEFAULT_WIDTH);
    descriptionColumn.setCellFactory(textFieldCellFactory);
    descriptionColumn.setOnEditCommit(cellEditCommitEventHandler);
    //descriptionColumn.setCellValueFactory(new PropertyValueFactory<>(DESCRIPTION));
    descriptionColumn.setCellValueFactory(p -> p.getValue().descriptionProperty());
    getColumns().add(descriptionColumn);

    //file type (extension)
    extensionColumn = new TableColumn<>(language.getString(EXTENSION));
    extensionColumn.setPrefWidth(EXTENSION_COL_DEFAULT_WIDTH);
    extensionColumn.setEditable(true);
    extensionColumn.setCellFactory(textFieldCellFactory);
    extensionColumn.setOnEditCommit(cellEditCommitEventHandler);
    //extensionColumn.setCellValueFactory(new PropertyValueFactory<>(EXTENSION));
    extensionColumn.setCellValueFactory(p -> p.getValue().extensionProperty());
    getColumns().add(extensionColumn);

    //file date
    fileDateColumn = new TableColumn<>(language.getString(MODIFIED));
    fileDateColumn.setPrefWidth(FILEDATE_COL_DEFAULT_WIDTH);
    fileDateColumn.setEditable(true);
    fileDateColumn.setCellFactory(textFieldCellFactory);
    fileDateColumn.setOnEditCommit(cellEditCommitEventHandler);
    //fileDateColumn.setCellValueFactory(new PropertyValueFactory<>(FILE_DATE));
    fileDateColumn.setCellValueFactory(p -> p.getValue().fileDateProperty());
    getColumns().add(fileDateColumn);


    //install SortOrder-ChangeListener to keep Selection
    this.getSortOrder().addListener((ListChangeListener<TableColumn<MediaFile, ?>>) change -> restoreLastSelection());

    //---------- install event handlers --------------
    installKeyHandlers(mediaContentView);
    installMouseHandlers();
    getViewPort();
    installDragDropHandlers(primaryStage);
    installSelectionHandler(mediaContentView);

    //-----install bubble help ------------
    tooltip.setShowDelay(ViewerControlPanel.TOOLTIP_DELAY); //same delay like in viewer control panel
    setTooltip(tooltip); //the text will be set whenever selection is changed (focus change listener)
  }

  private void installSelectionHandler(MediaContentView mediaContentView) {
    this.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
      if (isMovingFiles) return; //ignore refresh events while movingUp/Dn because selection has to be rebuilt with every step there, but effectively it does not change

      //set selected media in mediaContentView (or null if not valid)
      if (newValue.intValue() >= 0) { //only if selection is valid
        lastSelection = mediaFileList.getFileList().get(newValue.intValue());
        mediaContentView.setMedia(lastSelection, null);
        setTooltipText(lastSelection);
        mediaFileList.preLoadMedia(newValue.intValue(), mediaContentView);
      } else {
        if (mediaFileList.getFileList().size() <= 0) {
          //this happens e.g. if sort order is changed (by clicking the headlines) in an empty list (nothing loaded)
          //keep lastSelection, so the sortOrderChange-Listener can restore selection

          //invalid selection
          mediaContentView.setMedia(null, null);
          lastSelection = null;
        }
      }
    });
  }

  /**
   * reflect all changes of the given media file in the tooltip of the filetable
   * @param focusedMediaFile the mediafile which changes are reported in the tooltip
   */
  private void setTooltipText(MediaFile focusedMediaFile){
    String s= focusedMediaFile.getChangesText();
    if (s.length()>0) {
      setTooltip(tooltip);
      tooltip.setText(focusedMediaFile.getChangesText());
    }else{
      setTooltip(null);
    }
  }

  private void installDragDropHandlers(@NotNull Stage primaryStage) {
    primaryStage.getScene().setOnDragOver(dragEvent -> {
      Dragboard db = dragEvent.getDragboard();
      if (db.hasFiles()) {
        dragEvent.acceptTransferModes(TransferMode.COPY);
      } else {
        dragEvent.consume();
      }
    });

    primaryStage.getScene().setOnDragDropped(dragEvent -> {
      Dragboard db = dragEvent.getDragboard();
      boolean success = false;
      if (db.hasFiles()) {
        success = true;
        //load first file only
        openFolder(db.getFiles().get(0).getAbsolutePath(), true);
      }
      dragEvent.setDropCompleted(success);
      dragEvent.consume();
    });
  }

  private void getViewPort() {
    //this is a solution for getting the viewport (flow) seen on http://stackoverflow.com/questions/17268529/javafx-tableview-keep-selected-row-in-current-view
    skinProperty().addListener((ov, t, t1) -> {
      if (t1 == null) {
        return;
      }

      TableViewSkin<?> tvs = (TableViewSkin<?>) t1;
      ObservableList<Node> kids = tvs.getChildren();

      if (kids != null && !kids.isEmpty()) {
        flow = (VirtualFlow<?>) kids.get(1);
      }
    });
  }

  private void installMouseHandlers() {
    setOnMouseClicked(event -> {
      if (event.getClickCount() > 1) { //if double clicked
        rename();
      }
    });
  }

  private void installKeyHandlers(MediaContentView mediaContentView) {
    setOnKeyPressed(keyEvent -> {
      switch (keyEvent.getCode()) {
        //Edit
        case F2: //F2 (from menu) does not work if multiple lines are selected so here a key listener ist installed for F2
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
            keyEvent.consume();
            rename();
          }
          break;

        //Player
        case SPACE:
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !isEditMode() && mediaContentView.getPlayerViewer().isVisible()) {
            keyEvent.consume();
            mediaContentView.getPlayerViewer().getPlayerControls().togglePlayPause();
          }
          break;
        case LEFT:
          if (keyEvent.isControlDown() && !keyEvent.isShiftDown() && !isEditMode() && mediaContentView.getPlayerViewer().isVisible()) {
            keyEvent.consume();
            mediaContentView.getPlayerViewer().getPlayerControls().rewind();
          }
          break;

        //overwrite standard functionality (selection) view ctrl-shift up/down with file moving
        case DOWN:
          if (keyEvent.isAltDown() && keyEvent.isShiftDown()) {
            keyEvent.consume();
            moveSelectedFilesDown();
          }
          break;
        case UP:
          if (keyEvent.isAltDown() && keyEvent.isShiftDown()) {
            keyEvent.consume();
            moveSelectedFilesUp();
          }
      }
    });
  }

  /**
   * Strategy for opening initial File or Folder:
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
   * @param mediaFile the the focusedMediaFile where to set the values
   * @param column    the column indicating the property  to be set
   * @param newValue  the value to be set
   */
  public synchronized void saveEditedValue(MediaFile mediaFile, TableColumn<MediaFile, String> column, String newValue) {
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
      ((FileTableView)column.getTableView()).setTooltipText(mediaFile);
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
        scrollViewportToIndex(i, Alignment.CENTER);
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
   *
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
   * @param fileOrFolderName     string path to the file or folder
   * @param askForUnsavedChanges if true and ansaved changes are pending then a dialog opens, if false the caller must care itself for it
   */
  public void openFolder(String fileOrFolderName, boolean askForUnsavedChanges) {
    openFolder(Paths.get(fileOrFolderName), askForUnsavedChanges);
  }

  /**
   * if unsaved changes are pending a dialog opens to ask if the should be saved
   * <p>
   * open a file or folder (from File)
   * If the file-object is a file then the containing folder is opened and the file is focused
   * if this file is invalid then then containing folder is opened and the first file is focused
   * if the file-object is a folder then the folder is opened and the first file is focused
   *
   * @param fileOrFolder         file object representing a file or folder
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
          newFileOrFolder = getFocusModel().getFocusedItem().getFileOnDisk(); //--> reopen current selection
      } else
        newFileOrFolder = fileOrFolder;  //normal open

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
          if (newFileOrFolder != null) {
            selectRowByPath(newFileOrFolder);
            fileHistory.putOpenedFileToHistory(newFileOrFolder);
          }

        }
        registerStatisticsPanel();


        //register a file watcher for watching out for changes to this folder from external applications
        try {
          //register stops the old thread and starts an new for the new folder to register
          fileChangeWatcher.registerFolderToWatch(mediaFileList.getCurrentFolderName(), this);   //openFolder (above) already has set the currentFolderName
        } catch (Exception e) {
          //in Case of error the function does not exist to update the folder in background..so what...
        }
        //set default sorting
        Platform.runLater(this::resetSortOrder);

      } finally {
        //empty directory?
        if (mediaFileList.getFileList().size() < 1) {
          //clear any visible image shown before
          mediaContentView.setMedia(null, null);
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
    mediaFileList.getFileList().addListener((ListChangeListener<MediaFile>) c -> {
      while (c.next()) {
        if (c.wasAdded() || c.wasRemoved()) {
          statusBar.showFilesNumber(mediaFileList.getFileList().size());
        }
      }
    });
    getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>) c -> {
      while (c.next()) {
        if (c.wasAdded() || c.wasRemoved()) {
          statusBar.showSelectedNumber(getSelectionModel().getSelectedIndices().size());
        }
      }
    });
    mediaFileList.getDeletedFileList().addListener((ListChangeListener<MediaFile>) c -> {
      while (c.next()) {
        if (c.wasAdded() || c.wasRemoved()) {
          statusBar.showDeletedNumber(mediaFileList.getDeletedFileList().size());
        }
      }
    });

    //note: modifications are not yet registered, because they are not tracked in a observable list, but es attributes of MediaFile (decentral)
    //it's not worth to search for them every time a change has been made only to show the number
    //therefore the modificationNumber remains invisible as initialized in StatisticsPanel
  }

  /**
   * save all changes which the user has applied to the file table to the disk
   * (rename, time stamp, rotate, flip, ...)
   */
  public synchronized void saveFolder() {

    fileChangeWatcher.pauseWatching();  //ignore own changes in filesystem
    MediaFile currentFile = getFocusModel().getFocusedItem();
    int currentIndex = getFocusModel().getFocusedIndex();

    //if currentFile isChanged then stop players to enable renaming (keep it simple: do it always while saving)
    if (mediaContentView.isMediaPlayerActive()) {
      mediaContentView.getPlayerViewer().resetPlayer(); //stop players and disconnect from file to enable renaming
    }

    //use a savingTask for saving so that the UI can update in between (e.g. showing the progressBar)
    MediaFileListSavingTask savingTask = mediaFileList.getNewSavingTask();
    statusBar.getProgressProperty().bind(savingTask.progressProperty());
    statusBar.showProgressBar();
    statusBar.showMessage(MessageFormat.format(language.getString("saving.0.changes"), getUnsavedChanges()));

    //define what happens when task has finished
    savingTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, t -> {
      //MediaFile.flushAllMediaFromCache(); //not necessary because flush is executed if necessary while saving in MediaFile.saveChanges...

      mediaContentView.setMedia(null, null); //provoke a change in next line
      mediaContentView.setMedia(currentFile, null); //reload media, continue playing
      mediaFileList.preLoadMedia(currentIndex, mediaContentView);

      fileChangeWatcher.continueWatching();

      statusBar.clearProgress();
      int errorCount = savingTask.getValue();

      if (errorCount > 0) {
        statusBar.showError(MessageFormat.format(language.getString("errors.occurred.during.saving.check.status.column.for.details"), getUnsavedChanges(), MediaFile.STATUSFLAGS_HELPTEXT));
      } else {
        statusBar.showMessage(language.getString("changes.successfully.written.to.disk"));
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

      //copy and sort the selection
      ObservableList<Integer> selectedIndicesSorted = getCopyOfSelectedIndicesSortedAndUnique();

      isMovingFiles = true; //ignore selection events
      //check if selection already contains first file (=index 0)
      if (selectedIndicesSorted.get(0) > 0) { //not the first row is selected = there is a row above
        int focusIndex = getFocusModel().getFocusedIndex();

        //move
        int current;
        for (int i = 0; i < selectedIndicesSorted.size(); i++) { //iterator not usable because IntValue not writeable (see selectedIndicesSorted.set)
          current = selectedIndicesSorted.get(i); //convert Integer to int
          swapMediaFiles(current - 1, current); //move content
          selectedIndicesSorted.set(i, current - 1); //move selection -1=one up
        }

        //renew selection, which has been lost during swapMediaFiles()
        for (Integer anIndex : selectedIndicesSorted) {
          (getSelectionModel()).select(anIndex);
        }

        isMovingFiles = false; //handle selection events again
        //set new focus (has been changed by select() calls)
        scrollViewportToIndex(focusIndex - 1, Alignment.TOP);
        getFocusModel().focus(focusIndex - 1);
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
      //copy and sort the selection
      ObservableList<Integer> selectedIndicesSorted = getCopyOfSelectedIndicesSortedAndUnique();

      isMovingFiles = true; //ignore selection events
      //check if selection already contains last file (=index fileList.size-1)
      if (selectedIndicesSorted.get(selectedIndicesSorted.size()-1) < (mediaFileList.getFileList().size() - 1)){ //not the last row is selected = one row below
        int focusIndex = getFocusModel().getFocusedIndex();

        //move
        int current;
        for (int i = selectedIndicesSorted.size()-1; i>=0; i--) {
          current = selectedIndicesSorted.get(i); //convert Integer to int
          swapMediaFiles(current, current + 1); //move content
          selectedIndicesSorted.set(i, current + 1); //move selection +1=one down
        }

        //renew selection, which has been lost during swapMediaFiles()
        for (Integer anIndex : selectedIndicesSorted) {
          (getSelectionModel()).select(anIndex);
        }

        isMovingFiles = false; //handle selection events again

        //set new focus (has been changed by select() calls)
        scrollViewportToIndex(getFocusModel().getFocusedIndex(), Alignment.BOTTOM);
        getFocusModel().focus(focusIndex + 1);
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

  public ReadOnlyBooleanProperty getFindReplaceDialogShowingProperty(){
    if (findReplaceDialog == null) findReplaceDialog = new FindReplaceDialog(primaryStage, this);
    return findReplaceDialog.showingProperty();
  }

  public synchronized  void findNext(){
    if (findReplaceDialog.isShowing()){
      findReplaceDialog.handleFindFirst_FindNext();
    }
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

      //editable will be reset in TextFieldCell on CommitEdit
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
   * @return the focusedMediaFile which is one line above the selected focusedMediaFile or null if current media file is the first
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
   * <p>
   * if multiple lines are selected
   * - Prefix
   * - separator
   * - and description
   * are copied from the first of the selected lines to all other selected lines (like autofill in excel)
   * <p>
   * if just one line is selected copying will be performed from the above line (if there is any. if not nothing will happen)
   * if in edit-mode nothing happens (this will be performed in TextFieldCell)
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
    //copy and sort the selection
    ObservableList<Integer> selectedIndicesSorted = getCopyOfSelectedIndicesSortedAndUnique();

    int newSelectionIndex = selectedIndicesSorted.get(selectedIndicesSorted.size() - 1) + 1 //last selected row + 1
      - selectedIndicesSorted.size(); //this index will be lowered by the number of deleted items

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
   * @param rotateOperation operation to be performed
   */
  public synchronized void rotateSelectedFiles(ImageFileRotater.RotateOperation rotateOperation) {
    int filesCount = getSelectionModel().getSelectedItems().size();
    int notRotatable = mediaFileList.rotateSelectedFiles(getSelectionModel().getSelectedItems(), rotateOperation);
    mediaContentView.showRotationAndFlippingPreview();
    if (notRotatable == 0)
      statusBar.showMessage(MessageFormat.format(language.getString("0.images.rotated"), filesCount));
    else
      statusBar.showMessage(MessageFormat.format(language.getString("0.files.cannot.save.the.rotation"), notRotatable)
        + " " + MessageFormat.format(language.getString("0.images.rotated"), filesCount));

    setTooltipText(getFocusModel().getFocusedItem());
  }

  /**
   * perform flipping (mirroring) of all selected Images
   * Only Images are affected - all other selected files are ignored
   *
   * @param horizontally = true: mirror horizontally, false: mirror vertically
   */
  public synchronized void flipSelectedFiles(boolean horizontally) {
    int filesCount = getSelectionModel().getSelectedItems().size();
    int notRotatable = mediaFileList.flipSelectedFiles(getSelectionModel().getSelectedItems(), horizontally);
    mediaContentView.showRotationAndFlippingPreview();
    if (notRotatable == 0)
      statusBar.showMessage(MessageFormat.format(language.getString("0.images.flipped"), filesCount));
    else
      statusBar.showMessage(MessageFormat.format(language.getString("0.files.cannot.save.the.flipping"), notRotatable)
        + " " + MessageFormat.format(language.getString("0.images.flipped"), filesCount));
    setTooltipText(getFocusModel().getFocusedItem());
  }

  public synchronized void setOrientationAccordingExif() {
    int filesCount = getSelectionModel().getSelectedItems().size();
    int notRotatable = mediaFileList.setOrientationAccordingExif(getSelectionModel().getSelectedItems());
    mediaContentView.showRotationAndFlippingPreview();
    if (notRotatable == 0)
      statusBar.showMessage(MessageFormat.format(language.getString("0.images.oriented.according.exif.information"), filesCount));
    else
      statusBar.showMessage(MessageFormat.format(language.getString("0.could.not.be.orientated.according.to.exif"), notRotatable)
        + " " + MessageFormat.format(language.getString("0.images.oriented.according.exif.information"), filesCount - notRotatable));

    setTooltipText(getFocusModel().getFocusedItem());
  }

  private ObservableList<Integer> getCopyOfSelectedIndicesSortedAndUnique() {
    ObservableList<Integer> selectedIndicesSorted = FXCollections.observableArrayList(((MultipleSelectionModel<MediaFile>) getSelectionModel()).getSelectedIndices()); //copy
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

      mediaContentView.getPlayerViewer().pause();  //stop all currently played media because in undeleteDialog there could also be media played
      //show dialog
      if (unDeleteDialog == null) unDeleteDialog = new UnDeleteDialog(primaryStage);
      int result = unDeleteDialog.showModal(mediaFileList.getDeletedFileList());

      unDeleteDialog.stopPlayers(); //now stop media from the undeleteDialog so that main window can playback again...

      //execute unDeletion
      if (result == UnDeleteDialog.UNDELETE_SELECTION_BTN && unDeleteDialog.getFilesToUndelete() != null) {
        //make copy of the list selection of the dialog to prevent events in the (closed) dialog while unDeletion
        ObservableList<MediaFile> unDeletionList = FXCollections.observableArrayList(unDeleteDialog.getFilesToUndelete());
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
    MediaFile currentFile = getSelectionModel().getSelectedItem();
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
   * make index visible
   * The scrollTo()-Method of TableView is similar but "jumps" to the middle of the visible region time by time.
   * Especially while in edit mode this leads to losing the focus, i.e. the cell in edit mode is different to the focussed cell :-(
   * The viewPort trick has been found on StackOverflow :-)
   * <p/>
   * Always try to show two extra lines before/after the line 'index'
   * if index is already visible nothing happens
   *
   * @param indexToShow line to scroll to become visible in viewport
   * @param alignment   should the line -index- be aligned top, centered or bottom
   */
  public void scrollViewportToIndex(int indexToShow, Alignment alignment) {
    try {
      if (flow != null) {  //the flow is not valid before it is drawn for the first time. Will be the case every time after loading a directory ;-)

        //calculate how many extra lines make sense
        int height = flow.getLastVisibleCell().getIndex() - flow.getFirstVisibleCell().getIndex() + 1;

        int extraLines;
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

        if (indexToShow < firstLineWithExtraLines || indexToShow > lastLineWithExtraLines) {
          int index = switch (alignment) {
            case TOP -> indexToShow - extraLines;     //calculates the first line to be in the viewport
            case CENTER -> indexToShow - extraLines;  //calculates the first line to be in the viewport
            default -> indexToShow + extraLines;      //BOTTOM: calculates the last line to be in the viewport
          }; //the resulting index where to jump to
          //calculate index to be shown as first line according alignment
          if (index > mediaFileList.getFileList().size() - 1) index = mediaFileList.getFileList().size() - 1;
          if (index < 0) index = 0;

          flow.scrollTo(index);     //view the index fully visible inside the viewport (VirtualFlow)
        }
      } else { //flow==null
        //retry until there is a Viewport available
        Platform.runLater(() -> scrollViewportToIndex(indexToShow, alignment));
      }
    } catch (Exception e) {
      //if scrolling is not possible then don't do it (e.g. during Viewport is built newly because of opening an new folder
    }
  }

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

  //--------------------------- Find & Replace -------------------------------

  /**
   * store selection in mediaFileList
   * connect local searchRec to mediaFileList's searchRec (=current search)
   * and also connect textFieldFactory's searchRec for highlighting finding results
   */
  private void initSearch() {
    //pass a copy of selection list to initSearch because selection will change during search
    searchRec = mediaFileList.initSearch(FXCollections.observableArrayList(((MultipleSelectionModel<MediaFile>) getSelectionModel()).getSelectedItems()));
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
    final TableColumn<MediaFile, String> foundCol;

    if (searchRec != null && mediaFileList.searchNext(searchText) && searchRec.foundMediaFile != null) {
      //mark the current occurrence found in the fileTableView

      //set the focus to it
      foundCol = switch (searchRec.tableColumn) {
        case MediaFile.COL_NO_PREFIX -> prefixColumn;
        case MediaFile.COL_NO_COUNTER -> counterColumn;
        case MediaFile.COL_NO_SEPARATOR -> separatorColumn;
        //case MediaFile.COL_NO_DESCRIPTION -> descriptionColumn;  //default
        case MediaFile.COL_NO_EXTENSION -> extensionColumn;
        case MediaFile.COL_NO_FILEDATE -> fileDateColumn;
        default -> descriptionColumn;
      };

      final int foundIndex;
      if (searchRec.searchInSelection)
        foundIndex = mediaFileList.getFileList().indexOf(searchRec.foundMediaFile);  //calculate selection index --> file index
      else
        foundIndex = searchRec.tableRow;

      getFocusModel().focus(foundIndex);
      getSelectionModel().clearAndSelect(foundIndex, foundCol);

      //make it visible
      if (searchRec.found){
        scrollViewportToIndex(foundIndex, Alignment.CENTER);
      }

      selectSearchResultOnNextStartEdit = true;
      setEditable(true);
      edit(foundIndex, foundCol);

      return searchRec.found; //only if searchRec not null
    }
    return false; //default return value if searchRec or foundMediaFile is null
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

  //------- getters for columns are used to determine the currently changed column --------------
  public TableColumn<MediaFile, String> getStatusColumn() {
    return statusColumn;
  }

  public TableColumn<MediaFile, String> getPrefixColumn() {
    return prefixColumn;
  }

  public TableColumn<MediaFile, String> getCounterColumn() {
    return counterColumn;
  }

  public TableColumn<MediaFile, String> getSeparatorColumn() {
    return separatorColumn;
  }

  public TableColumn<MediaFile, String> getDescriptionColumn() {
    return descriptionColumn;
  }

  public TableColumn<MediaFile, String> getExtensionColumn() {
    return extensionColumn;
  }

  public TableColumn<MediaFile, String> getFileDateColumn() {
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

  public enum Alignment {TOP, CENTER, BOTTOM}

  /**
   * EventHandler to handle CommitEdit() events from FieldCell in context of FileTableView
   * this handler is registered when the columns are built in the constructor
   * same handler for all columns
   * it is fired in TextFieldCell.commitEdit
   */
  private class CellEditCommitEventHandler implements EventHandler<TableColumn.CellEditEvent<MediaFile, String>> {
    @Override
    public void handle(TableColumn.CellEditEvent<MediaFile, String> t) {
      saveEditedValue(t.getRowValue(), t.getTableColumn(), t.getNewValue());
    }
  }

}