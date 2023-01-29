package dringo.kissPhoto.view;


import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.ctrl.FileChangeWatcher;
import dringo.kissPhoto.ctrl.FileChangeWatcherEventListener;
import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.helper.PathHelpers;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileList;
import dringo.kissPhoto.model.MediaFileListSavingTask;
import dringo.kissPhoto.view.dialogs.*;
import dringo.kissPhoto.view.fileTableHelpers.FileHistory;
import dringo.kissPhoto.view.fileTableHelpers.FileTableContextMenu;
import dringo.kissPhoto.view.fileTableHelpers.FileTableTextFieldCell;
import dringo.kissPhoto.view.fileTableHelpers.FileTableTextFieldCellFactory;
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
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.text.MessageFormat;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2023 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * JavaFX Table View that is the GUI for MediaFileList
 * <p/>
 *
 * @author Ingo

 * @version 2023-01 29 support file deletion and moving while in edit mode (see FileTableTextFieldCell)
 * @version 2023-01-05 ctrl-del to delete while inplace editing  and undelete last implemented. Moving to next/previous file cleaned up and moved to FileTableView from ContentView
 * @version 2022-09-04 clean up primaryStage parameter
 * @version 2022-01-08 improvement getting focus after start edit if double-clicked on empty part of table (without text)
 * @version 2021-11-07 Meta-Info Column added. Only visible while meta-InfoView is visible
 * @version 2021-11-01 context menu added, renumbering simplified
 * @version 2021-01-16 eliminating the slow and error-prone reflection: column.setCellValueFactory(new PropertyValueFactory<>("propertyName") replaced with call back. ToolTipText added
 * @version 2021-01-09 improve scrolling, save also currently edited value, improve findNext (F3-support)
 * @version 2014-05-02 I18Support, Cursor in Edit-Mode over lines, reopen added etc
 * @since 2012-09-06
 */

public class FileTableView extends TableView<MediaFile> implements FileChangeWatcherEventListener {
  //string constants (i18alized) for table columns' headlines used here and in RenameDialog
  public static final String PREFIX = "prefix";
  public static final String COUNTER = "counter";
  public static final String SEPARATOR = "separator";
  public static final String DESCRIPTION = "description";
  public static final String EXTENSION = "extension";
  public static final String MODIFIED = "modified";
  public static final String METAINFO = "metainfo";
  public static final String NOTHING_FOUND = "nothing.found";
  //Default Column Widths
  private static final int STATUS_COL_DEFAULT_WIDTH = 20;
  private static final int PREFIX_COL_DEFAULT_WIDTH = 50;
  private static final int COUNTER_COL_DEFAULT_WIDTH = 40;
  private static final int SEPARATOR_COL_DEFAULT_WIDTH = 30;
  private static final int DESCRIPTION_COL_DEFAULT_WIDTH = 480;
  private static final int EXTENSION_COL_DEFAULT_WIDTH = 50;
  private static final int FILEDATE_COL_DEFAULT_WIDTH = 155;
  private static final int METAINFO_COL_DEFAULT_WIDTH = 150;
  //Constants for writing Width to settings-File
  private static final String STATUS_COL_WIDTH = "StatusColWidth";
  private static final String PREFIX_COL_WIDTH = "PrefixColWidth";
  private static final String COUNTER_COL_WIDTH = "CounterColWidth";
  private static final String SEPARATOR_COL_WIDTH = "SeparatorColWidth";
  private static final String DESCRIPTION_COL_WIDTH = "DescriptionColWidth";
  private static final String EXTENSION_COL_WIDTH = "ExtensionColWidth";
  private static final String FILEDATE_COL_WIDTH = "FileDateColWidth";
  private static final String METAINFO_COL_WIDTH = "MetaInfoColWidth";
  private static final String METAINFO_COL_PATH = "MetaInfoColPath";
  //----- Table Columns
  protected final TableColumn<MediaFile, String> statusColumn;
  protected final TableColumn<MediaFile, String> prefixColumn;
  protected final TableColumn<MediaFile, String> counterColumn;
  protected final TableColumn<MediaFile, String> separatorColumn;
  protected final TableColumn<MediaFile, String> descriptionColumn;
  protected final TableColumn<MediaFile, String> extensionColumn;
  protected final TableColumn<MediaFile, String> fileDateColumn;
  protected final TableColumn<MediaFile, String> metaInfoColumn;
  private ObservableStringList metaInfoColumnPath = null; //currently shown tag in this column

  //---- views linking
  protected final MediaContentView mediaContentView; //mediaContentView to show media if selection changes
  private final MetaInfoView metaInfoView;
  private final StatusBar statusBar;  //messages
  //---- the content to be displayed
  private final MediaFileList mediaFileList = new MediaFileList();
  //---- listen if an external program changes the currently loaded folder
  private final FileChangeWatcher fileChangeWatcher = new FileChangeWatcher();  //check for external changes to an opened folder
  private final FileHistory fileHistory;

  protected VirtualFlow<?> flow;  //viewport for scrolling
  //----- Define Cell Factory and EditEventHandler
  //will be the identical for all columns (except statusColumn, see FileTableView constructor)
  final FileTableTextFieldCellFactory fileTableTextFieldCellFactory = new FileTableTextFieldCellFactory();
  final CellEditCommitEventHandler cellEditCommitEventHandler = new CellEditCommitEventHandler();
  //every mediaFileList has one searchRec that is built once.
  // For convenience a pointer to that is searchRec is stored in FileTable while search is active (see findFirst/replaceAll)
  // if no search is active searchRec == null!
  private MediaFileList.SearchRec searchRec = null;
  private MediaFile lastSelection = null;
  //renumbering values will be initialized on every openFolder() and can be changed by user using renumberDialog()
  private int numberingOffset = 1;  //determines with which number renumbering of the list starts.
  private int numberingStepSize = 1;
  private int numberingDigits = 0;   //zero is [auto]
  //----- Dialog Singletons
  private FindReplaceDialog findReplaceDialog = null; //will be created when used firstly (see findAndReplace() )
  private RenumberDialog renumberDialog = null; //will be created when used firstly (see renumberWithDialog() )
  private RenameDialog renameDialog = null; //will be created when used firstly (see renameWithDialog())
  private UnDeleteDialog unDeleteDialog = null; //will be created when used firstly (see unDeleteWithDialog())
  //----- link to MenuItems to enable/disable
  private MenuItem pasteMenuItem = null;     //Main menu will pass a link to it's pasteMenuItem, so copy/cut routines can control if disabled or not
  //----- Enable keeping Cursor-Position while editing when changing line
  private boolean selectSearchResultOnNextStartEdit = false;  //during SearchNext searchRec Cursor needs to be set on StartEdit
  private boolean renameDialogActive = false;

  private final Tooltip tooltip = new Tooltip();

  private boolean isMovingFiles = false; //ignore selection refresh events while movingUp/Dn because selection has to be rebuilt with every step there, but effectively it does not change
  private boolean enableSelectionListener = true; //will be disabled while loading to prevent from many unnecessary change events each would cause setMedia...
  private FileTableTextFieldCell editingFileTableTextFieldCell;  //will be updated in FileTableTextFieldCell in startEdit and reset to null on cancelEdit or commitEdit

  /**
   * constructor will try to open the passed file or folderName
   * if there is no such file or folder an empty list is shown
   *
   * @param mediaContentView link to the view where media is displayed if focus changes
   * @param metaInfoView     link to metaInfoView for showing Exif and other Metadata
   * @param statusBar        link to statusBar for showing information/errors
   * @param primaryStage     link to main window (getScene().getWindow() is null during start up)
   */
  public FileTableView(final MediaContentView mediaContentView, final MetaInfoView metaInfoView, StatusBar statusBar, Stage primaryStage) {
    //remember connections to main window etc
    this.mediaContentView = mediaContentView;
    this.metaInfoView = metaInfoView;
    this.statusBar = statusBar;
    fileHistory = new FileHistory(this);

    this.setMinSize(100.0, 100.0);

    //set properties of the table
    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    //create Table
    //status: readonly Column to show status flags
    statusColumn = new TableColumn<>(language.getString("status"));
    statusColumn.setPrefWidth(STATUS_COL_DEFAULT_WIDTH);
    statusColumn.setEditable(false);
    statusColumn.setCellValueFactory(p -> p.getValue().status);
    getColumns().add(statusColumn);

    //prefix
    prefixColumn = new TableColumn<>(language.getString(PREFIX));
    prefixColumn.setPrefWidth(PREFIX_COL_DEFAULT_WIDTH);
    prefixColumn.setEditable(true);
    prefixColumn.setOnEditCommit(cellEditCommitEventHandler);
    prefixColumn.setCellFactory(fileTableTextFieldCellFactory);
    prefixColumn.setCellValueFactory(p -> p.getValue().prefix);
    getColumns().add(prefixColumn);


    //counter
    counterColumn = new TableColumn<>(language.getString(COUNTER));
    counterColumn.setPrefWidth(COUNTER_COL_DEFAULT_WIDTH);
    counterColumn.setEditable(true); //as a default counter is editable until renumbering is set to auto-mode
    counterColumn.setCellFactory(fileTableTextFieldCellFactory);
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
    counterColumn.setCellValueFactory(mediaFile -> mediaFile.getValue().counter);
    getColumns().add(counterColumn);

    //separator
    separatorColumn = new TableColumn<>(language.getString(SEPARATOR));
    separatorColumn.setEditable(true);
    separatorColumn.setPrefWidth(SEPARATOR_COL_DEFAULT_WIDTH);
    separatorColumn.setCellFactory(fileTableTextFieldCellFactory);
    separatorColumn.setOnEditCommit(cellEditCommitEventHandler);
    separatorColumn.setCellValueFactory(mediaFile -> mediaFile.getValue().separator);
    getColumns().add(separatorColumn);

    //description
    descriptionColumn = new TableColumn<>(language.getString(DESCRIPTION));
    descriptionColumn.setEditable(true);
    descriptionColumn.setPrefWidth(DESCRIPTION_COL_DEFAULT_WIDTH);
    descriptionColumn.setCellFactory(fileTableTextFieldCellFactory);
    descriptionColumn.setOnEditCommit(cellEditCommitEventHandler);
    descriptionColumn.setCellValueFactory(mediaFile -> mediaFile.getValue().description);
    getColumns().add(descriptionColumn);

    //file type (extension)
    extensionColumn = new TableColumn<>(language.getString(EXTENSION));
    extensionColumn.setPrefWidth(EXTENSION_COL_DEFAULT_WIDTH);
    extensionColumn.setEditable(true);
    extensionColumn.setCellFactory(fileTableTextFieldCellFactory);
    extensionColumn.setOnEditCommit(cellEditCommitEventHandler);
    extensionColumn.setCellValueFactory(mediaFile -> mediaFile.getValue().extension);
    getColumns().add(extensionColumn);

    //file date
    fileDateColumn = new TableColumn<>(language.getString(MODIFIED));
    fileDateColumn.setPrefWidth(FILEDATE_COL_DEFAULT_WIDTH);
    fileDateColumn.setEditable(true);
    fileDateColumn.setCellFactory(fileTableTextFieldCellFactory);
    fileDateColumn.setOnEditCommit(cellEditCommitEventHandler);
    fileDateColumn.setCellValueFactory(mediaFile -> mediaFile.getValue().modifiedDate);
    getColumns().add(fileDateColumn);

    //Meta-Info Column
    metaInfoColumn = new TableColumn<>(language.getString("metadata")); //this title will be replaced by name of selected Tag in defineMetaInfoColumn()
    metaInfoColumn.visibleProperty().bind(metaInfoView.visibleProperty()); //the metadata column is visible if metadata is visible
    metaInfoColumn.setPrefWidth(METAINFO_COL_DEFAULT_WIDTH);
    metaInfoColumn.setEditable(true);
    metaInfoColumn.setCellFactory(fileTableTextFieldCellFactory);
    metaInfoColumn.setOnEditCommit(cellEditCommitEventHandler);
    metaInfoColumn.setCellValueFactory(mediaFile -> mediaFile.getValue().getMetaInfo(metaInfoColumnPath));
    getColumns().add(metaInfoColumn);

    setEditable(true);
    setTableMenuButtonVisible(true); //enable user to show/hide columns

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

    //------------ install Context menu
    FileTableContextMenu contextMenu = new FileTableContextMenu(this);
    contextMenu.setAutoHide(true);
    //hide context menu if clicked "somewhere else" or request focus on mouse click
    setOnMouseClicked(mouseEvent -> {
      if (contextMenu.isShowing()) {
        contextMenu.hide(); //this closes the context Menu
        mouseEvent.consume();
      } else {
        requestFocus();
      }
    });


    setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));

  }

  private void installSelectionHandler(MediaContentView mediaContentView) {
    getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
      if (enableSelectionListener && !isMovingFiles) {
        //set selected media in mediaContentView (or null if not valid)
        if (newValue.intValue() >= 0) { //only if selection is valid
          lastSelection = mediaFileList.getFileList().get(newValue.intValue());
          showMedia(lastSelection, null);
          setTooltipText(lastSelection);
          mediaFileList.preLoadMedia(newValue.intValue(), mediaContentView);
        } else {
          if (mediaFileList.getFileList().size() == 0) {
            //this happens e.g. if sort order is changed (by clicking the headlines) in an empty list (nothing loaded)
            //keep lastSelection, so the sortOrderChange-Listener can restore selection

            //invalidate selection
            showMedia(null, null);
            lastSelection = null;
          }
        }
      }
    });

    //guarantee that always a valid column is selected to avoid NPE during startEdit
    getFocusModel().focusedCellProperty().addListener((observable, oldValue, newValue) -> {
      if (enableSelectionListener && newValue.getTableColumn() == null){
        getSelectionModel().select(newValue.getRow(), descriptionColumn);
      }
    });
  }

  /**
   * reflect all changes of the given media file in the tooltip of the fileTable
   * @param focusedMediaFile the mediaFile which changes are reported in the tooltip
   */
  public void setTooltipText(MediaFile focusedMediaFile){
    String s= focusedMediaFile.getChangesText();
    if (s.length()>0) {
      setTooltip(tooltip);
      tooltip.setText(focusedMediaFile.getChangesText());
    }else{
      setTooltip(null);
    }
  }

  /**
   *
   * @param primaryStage link to primary window needed because this.getScene() is null during start up
   */
  private void installDragDropHandlers(Stage primaryStage) {
    //--------------FileTable accepts dragging of tags from metaInfoView
    setOnDragOver(dragEvent -> {
      if (dragEvent.getGestureSource() == metaInfoView.getMetaInfoAllTagsView()) {
        dragEvent.acceptTransferModes(TransferMode.COPY);
        dragEvent.consume();
      }
    });

    setOnDragDropped(dragEvent ->{
      boolean success= false;
      if (dragEvent.getGestureSource() == metaInfoView.getMetaInfoAllTagsView()) {
        defineMetaInfoColumn(metaInfoView.getMetaInfoAllTagsView().getUserSelectionPath());
        //stop dragging also if it could not be handled
        dragEvent.setDropCompleted(success);
        dragEvent.consume();
      }
    });

    //------------- The complete application accept files from other Applications (for opening via FileTableView)
    primaryStage.getScene().setOnDragOver(dragEvent -> {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
          dragEvent.acceptTransferModes(TransferMode.COPY);
          dragEvent.consume();
        }
    });

    primaryStage.getScene().setOnDragDropped(dragEvent -> {
      Dragboard db = dragEvent.getDragboard();
      boolean success = false;
      if (db.hasFiles()) {
        //load first file only
        openFolder(db.getFiles().get(0).getAbsolutePath(), true);
        success = true;
      }
      //stop dragging also if it could not be handled
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
    setOnMousePressed(event -> {
      if (event.getClickCount() > 1) { //if double-clicked
        Platform.runLater(this::rename);
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

        case DELETE: //menu shows "ctrl-Delete" to delete files. But while list is in selection model delete is more natural to use
          if (!keyEvent.isShiftDown()) { //delete and ctrl-delete are same but shift-ctrl-delete is undelete
            keyEvent.consume();
            deleteSelectedFiles(false);
          }

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

        //overwrite standard functionality (selection) view Alt-shift up/down with file moving
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

  public void defineMetaInfoColumn(ObservableStringList tagPath){
    metaInfoColumn.setText(metaInfoView.getMetaInfoAllTagsView().ConvertVisiblePathToDotString(tagPath));
    metaInfoColumnPath = tagPath;

    metaInfoColumn.setEditable(false); //isWritingSupported(tagPath) = if it is a tag from MetaInfoEditableTagsView

    KissPhoto.globalSettings.setProperty(METAINFO_COL_PATH, tagPath.toCSVString());


    refresh(); //refreshMetaInfoColumn for all visible lines
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

    KissPhoto.globalSettings.setProperty(METAINFO_COL_WIDTH, Double.toString(metaInfoColumn.getWidth()));
    //note: the metInfoColumn tagPath is store when defineMetaInfoColumn is called

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
      statusColumn.setPrefWidth(STATUS_COL_DEFAULT_WIDTH);
    }
    try {
      prefixColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(PREFIX_COL_WIDTH)));
    } catch (Exception e) {
      prefixColumn.setPrefWidth(PREFIX_COL_DEFAULT_WIDTH);
    }
    try {
      counterColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(COUNTER_COL_WIDTH)));
    } catch (Exception e) {
      counterColumn.setPrefWidth(COUNTER_COL_DEFAULT_WIDTH);
    }
    try {
      separatorColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(SEPARATOR_COL_WIDTH)));
    } catch (Exception e) {
      separatorColumn.setPrefWidth(SEPARATOR_COL_DEFAULT_WIDTH);
    }
    try {
      descriptionColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(DESCRIPTION_COL_WIDTH)));
    } catch (Exception e) {
      descriptionColumn.setPrefWidth(DESCRIPTION_COL_DEFAULT_WIDTH);
    }
    try {
      extensionColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(EXTENSION_COL_WIDTH)));
    } catch (Exception e) {
      extensionColumn.setPrefWidth(EXTENSION_COL_DEFAULT_WIDTH);
    }
    try {
      fileDateColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(FILEDATE_COL_WIDTH)));
    } catch (Exception e) {
      fileDateColumn.setPrefWidth(FILEDATE_COL_DEFAULT_WIDTH);
    }
    try {
      fileDateColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(FILEDATE_COL_WIDTH)));
    } catch (Exception e) {
      fileDateColumn.setPrefWidth(FILEDATE_COL_DEFAULT_WIDTH);
    }
    try {
      metaInfoColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(METAINFO_COL_WIDTH)));
    } catch (Exception e) {
      metaInfoColumn.setPrefWidth(METAINFO_COL_DEFAULT_WIDTH);
    }

    ObservableStringList tagPath = new ObservableStringList();
    try {
      tagPath.appendFromCSVString(KissPhoto.globalSettings.getProperty(METAINFO_COL_PATH));
      defineMetaInfoColumn(tagPath);
    } catch (Exception e) {
      //nothing was selected, nothing to restore
    }
  }

  /**
   * Save the string value that was edited using some TextField
   * depending on the current column into the correct property
   *
   * @param mediaFile the focusedMediaFile where to set the values
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

      int result = new MessageBox(getPrimaryStage(), MessageLabel,
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
      File dir = dirChooserDialog.showOpenDialog(getPrimaryStage());
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
      showMedia((MediaFile) getFocusModel().getFocusedItem());
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
   * if this file is invalid then the containing folder is opened and the first file is focused
   * if the string is a path to a folder then the folder is opened and the first file is focused
   *
   * @param fileOrFolderName     string path to the file or folder
   * @param askForUnsavedChanges if true and unsaved changes are pending then a dialog opens, if false the caller must care itself for it
   */
  public void openFolder(String fileOrFolderName, boolean askForUnsavedChanges) {
    openFolder(Paths.get(fileOrFolderName), askForUnsavedChanges);
  }

  /**
   * if unsaved changes are pending a dialog opens to ask if they should be saved
   * <p>
   * open a file or folder (from File)
   * If the file-object is a file then the containing folder is opened and the file is focused
   * if this file is invalid then the containing folder is opened and the first file is focused
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
      //detect a reopen event
      //if current directory is same as fileOrFolder change it to the current selection

      boolean reopened = (mediaFileList.getCurrentFolder() != null) && mediaFileList.getCurrentFolder().equals(PathHelpers.extractFolder(fileOrFolder)); //same folder

      if (reopened) {
        if (getFocusModel().getFocusedItem() != null)
          newFileOrFolder = getFocusModel().getFocusedItem().getFileOnDisk(); //--> reopen current selection
      } else
        newFileOrFolder = fileOrFolder;  //normal open

      final Scene primaryScene = this.getScene();
      if (primaryScene != null)
        primaryScene.setCursor(Cursor.WAIT); //can be null during openInitialFolder() called from main()

      statusBar.showMessage(MessageFormat.format(language.getString("trying.to.open.0"), fileOrFolder.toString()));


      String errMsg = "";
      try {
        getSelectionModel().clearSelection();  //prevent the selection listener from doing nonsense while loading
        enableSelectionListener = false;

        errMsg = mediaFileList.openFolder(newFileOrFolder);

        if (errMsg.length() == 0) {
          getPrimaryStage().setTitle(KissPhoto.KISS_PHOTO + KissPhoto.KISS_PHOTO_VERSION + " - " + mediaFileList.getCurrentFolderName());
          if (reopened)
            statusBar.showMessage(MessageFormat.format(language.getString("0.reopened"), mediaFileList.getCurrentFolderName()));
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
          //register stops of the old thread and start a new for the new folder to register
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
          showMedia(null,null);
        }
        if (primaryScene != null) primaryScene.setCursor(Cursor.DEFAULT);
        if (errMsg.length() > 0) {
          statusBar.showError(MessageFormat.format(language.getString("could.not.open.0"), fileOrFolder.toString()));
        }
      }
    }
    refresh();
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    getPrimaryStage().requestFocus();
    enableSelectionListener = true;

  }

  /**
   * Status Bar's statistics are registered with the currently loaded MediaFileList and Selection/deleted File List
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

    //note: modifications are not yet registered, because they are not tracked in an observable list, but es attributes of MediaFile (decentralized)
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
      showMedia(currentFile, null);
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

    //here is how a cancel button could be implemented (not yet used as saving should not be interrupted)
//    Button cancelButton = new Button("Cancel");
//    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
//      @Override
//      public void handle(ActionEvent event) {
//        cancelButton.setDisable(true);
//        savingTask.cancel(true);  //mayInterruptIfRunning really true? think over...
//      }
//    });
  }

  private void showMedia(MediaFile mediaFile, Duration lastPlayerPos) {
    mediaContentView.setMedia(mediaFile, lastPlayerPos); //reload media, continue playing
    metaInfoView.setMediaFile(mediaFile);
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

    File file = fileChooserDialog.showSaveDialog(getPrimaryStage());   //already asks if existing file should be replaced ;-)
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
   * Cut selected lines:  save the selection internally and delete the files from the list (move to un-deletion list)
   * todo Cutting to system clipboard as csv should also be supported
   */
  public void cutToClipboard() {
    deleteSelectedFiles(true);
    pasteMenuItem.setDisable(false); //pasting is now possible
    statusBar.showMessage(language.getString("use.edit.paste.ctrl.v.to.paste.into.a.new.location.of.the.list"));
  }

  /**
   * paste previously cut lines before current line --> move files using clipboard
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
   * renumbers selected files of the fileList by writing column "counter" in the standard way:
   * numbering is the position (index) of the file in the list, step size 1 is used<br><br>
   * Leading zeros are used to get at least the number of digits determined by param digits
   * <p/>
   */
  public synchronized void renumberSelectionStandard() {
    if (mediaFileList.getFileList().isEmpty()) return; //if nothing is opened

    mediaFileList.renumberRelativeToIndices(1, 1, 0, getSelectionModel().getSelectedIndices());
  }



  /**
   * renumber selection or all
   * according the user settings in RenumberDialog
   * note: if globalNumbering is used (param is initialization for dialog) then the start-, step- and digit-values
   * are stored for the currently loaded list in FileTableView as numberingOffset, numberingStepSize and numberingDigits
   * The values stepSize and Digits are stored for next renumbering
   * if renumbering is initialized with globalNumbering the dialog is initialized with global numberingOffset (start) values and the new value is stored
   * if renumbering is initialized with local Numbering (global=false) the dialog start offset is initialized with the smallest number found in selection
   * if renumber_all was chosen in the dialog the start value will also be stored as numberingOffset
   */
  public synchronized void renumberWithDialog() {
    //initialize start index
    int start = numberingOffset; //initialize

      ObservableList<MediaFile> selection = getSelectionModel().getSelectedItems();
      if (selection == null) {
        start = 1;
      } else {
        for (MediaFile mediaFile : selection) {
          if (mediaFile.getCounterValue() < start) start = mediaFile.getCounterValue();
        }
    }

    //show dialog
    if (renumberDialog == null) renumberDialog = new RenumberDialog(getPrimaryStage());
    int result = renumberDialog.showModal(start, numberingStepSize, numberingDigits);

    //execute numbering and store dialog values for next renumbering
    if (result != RenumberDialog.CANCEL_BTN && result != RenumberDialog.NONE_BTN) {
      start = renumberDialog.getStart();
      numberingStepSize = renumberDialog.getStep();
      numberingDigits = renumberDialog.getDigits();
      numberingOffset = start; //remember start as global numbering offset for this list

      mediaFileList.renumber(start, numberingStepSize, numberingDigits, getSelectionModel().getSelectedIndices());
    }
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    getPrimaryStage().requestFocus();
  }

  /**
   * show up Find&Replace Dialog
   */

  public synchronized void findAndReplace() {
    //initialize with first of selected files
    if (findReplaceDialog == null) findReplaceDialog = new FindReplaceDialog( getPrimaryStage(), this);

    //---show dialog
    findReplaceDialog.showModal();
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    getPrimaryStage().requestFocus();
  }

  /**
   * for binding Showing property in MainMenuBar during startup
   * @param primaryStage necessary because getPrimaryStage() does not work during startup
   * @return BooleanProperty indicating if FindReplaceDialog is visible
   */
  public ReadOnlyBooleanProperty getFindReplaceDialogShowingProperty(Stage primaryStage){
    if (findReplaceDialog == null) findReplaceDialog = new FindReplaceDialog(primaryStage,this);
    return findReplaceDialog.showingProperty();
  }

  public synchronized  void findNext(){
    if (findReplaceDialog.isShowing()){
      findReplaceDialog.handleFindFirst_FindNext();
    }
  }

  /**
   * FileTableTextFieldCell calls this method
   * on startEdit: set the editing cell
   * on commitEdit/cancelEdit: set editing cell to null
   * see cancelEdit
   *
   * @param cell the currently active editing cell or null if edit is not active
   */
  public void setEditingCell(FileTableTextFieldCell cell){
    editingFileTableTextFieldCell = cell;
  }
  /**
   * rename selected line...or if multiple lines are selected call renameWithDialog()
   * if no column is currently selected, then select descriptionColumn
   */

  public synchronized void rename() {
    if (getSelectionModel().getSelectedItems().size() > 1) {
      if (isEditMode()) {
        cancelEdit(); //F2 might have activated Cell-Edit-Mode already
      }
      renameWithDialog();
    } else {
      TablePosition<MediaFile,String> focusedCell = getFocusModel().getFocusedCell();
      if (focusedCell != null) {
        edit(getFocusModel().getFocusedIndex(), getFocusModel().getFocusedCell().getTableColumn());
      }
    }
  }

  /**
   * stop editing and restore last values
   * if edit mode was not active nothing happens
   */
  public void cancelEdit(){
    if (editingFileTableTextFieldCell!=null)
      editingFileTableTextFieldCell.cancelEdit();
  }

  /**
   * Rename all selected files showing up a dialog first
   * The initialization of the dialog is based on the first selected file
   */
  public synchronized void renameWithDialog() {
    int result;
    MediaFile firstSelectedFile;

    //initialize with first of selected files
    if (renameDialog == null) renameDialog = new RenameDialog(getPrimaryStage());

    //copy and sort the selection for determining the "fist selected"
    ObservableList<Integer> selectedIndicesSorted = FXCollections.observableArrayList(getSelectionModel().getSelectedIndices()); //copy
    FXCollections.sort(selectedIndicesSorted);
    try {
      firstSelectedFile = mediaFileList.getFileList().get(selectedIndicesSorted.get(0));
    } catch (Exception e) {
      firstSelectedFile = null;
    }

    //---show dialog
    renameDialogActive = true;    //indicate active edit mode for keyEvents, see isEditMode()
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
    getPrimaryStage().requestFocus();
  }

  /**
   * set prefixes to "" (empty string) in all marked files
   */
  public synchronized void cleanPrefix(){
    ObservableList<MediaFile> selectedFiles = getSelectionModel().getSelectedItems();
    for (MediaFile m : selectedFiles) {
      m.setPrefix("");
    }
  }

  /**
   * set counter (numbers) to "" (empty string) in all marked files
   */
  public synchronized void cleanCounter(){
    ObservableList<MediaFile> selectedFiles = getSelectionModel().getSelectedItems();
    for (MediaFile m : selectedFiles) {
      m.setCounter("");
    }
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
   * autofill from first selected lines to the other selected lines
   * <p>
   * if multiple lines are selected
   * - Prefix
   * - separator
   * - and description
   * are copied from the first of the selected lines to all other selected lines (like autofill in Excel)
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
   * move all files to be deleted to deletion list and remove them from the FileTableView
   * when saving (File-Save) they are deleted physically from disk
   * The Deletion List can be used for un-deletion until saving
   * <p/>
   * The selection is moved after the last deleted row (or last row of the table if the last row has been deleted)
   *
   * @param cutToClipboard true means pasting is enabled, ie. the deleted files are additionally stored in the pasting list (for moving)
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
    getSelectionModel().clearSelection();  //all previously marked files will be deleted, so remove the selection and prevent from events while deletion on the selection

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
  }

  /**
   * perform the handed rotateOperation to all selected Images
   * Only Images are affected - all others selected files are ignored
   *
   * @param rotateOperation operation to be performed
   */
  public synchronized void rotateSelectedFiles(MediaFile.RotateOperation rotateOperation) {
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
   * Only Images are affected - all others selected files are ignored
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

  public synchronized void setOrientationAccordingExif() { //todo: in Worker-Thread auslagern und Fortschrittsbalken anzeigen!
    int filesCount = getSelectionModel().getSelectedItems().size();
    int notRotatable = mediaFileList.setOrientationAccordingExif(getSelectionModel().getSelectedItems());
    mediaContentView.showRotationAndFlippingPreview();
    if (notRotatable == 0)
      statusBar.showMessage(MessageFormat.format(language.getString("0.images.oriented.according.exif.information"), filesCount));
    else
      statusBar.showMessage(
          MessageFormat.format(language.getString("0.images.oriented.according.exif.information"), filesCount - notRotatable)
          + " "
          + MessageFormat.format(language.getString("0.could.not.be.orientated.according.to.exif"), notRotatable)
      );

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

  /**
   * the last element of deletedFileList ist put back into the fileList
   * i.e. the file which have been deleted at last is undeleted
   * if no file has been deleted nothing happens
   * note: the deletedFileList is empty after saving has been performed and undelete therefore is then no longer possible with current implementation
   */
  public void undeleteLastDeletedFile(){
    if (mediaFileList.getDeletedFileList().size() > 0) {
      mediaFileList.undeleteLastFile(getSelectionModel().getSelectedIndex());
      showPreviousMedia();  //select the newly restored file

      statusBar.showMessage(MessageFormat.format(language.getString("0.file.s.recovered.before.the.previously.selected.row.you.may.want.to.use.view.reset.sorting.columns.from.main.menu"), 1));
    } else {
      statusBar.showError(language.getString("no.files.marked.for.deletion.therefore.nothing.to.un.delete"));
    }
  }

  public synchronized void unDeleteWithDialog() {
    if (mediaFileList.getDeletedFileList().size() > 0) {

      mediaContentView.getPlayerViewer().pause();  //stop all currently played media because in undeleteDialog there could also be media played
      //show dialog
      if (unDeleteDialog == null) unDeleteDialog = new UnDeleteDialog(getPrimaryStage());
      int result = unDeleteDialog.showModal(mediaFileList.getDeletedFileList());

      unDeleteDialog.stopPlayers(); //now stop media from the undeleteDialog so that main window can play back again...

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
    requestLayout();
    requestFocus(); //if full-screen is active then after a dialog the main window should be active again
    getPrimaryStage().requestFocus();
  }

  /**
   * Search for changes in the fileList
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
      //if scrolling is not possible then don't do it (e.g. during Viewport is built newly because of opening a new folder
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
        case MediaFile.COL_PREFIX -> prefixColumn;
        case MediaFile.COL_COUNTER -> counterColumn;
        case MediaFile.COL_SEPARATOR -> separatorColumn;
        //case MediaFile.COL_NO_DESCRIPTION -> descriptionColumn;  //default
        case MediaFile.COL_EXTENSION -> extensionColumn;
        case MediaFile.COL_FILEDATE -> fileDateColumn;
        default -> descriptionColumn;
      };

      final int foundIndex;
      if (searchRec.searchInSelection)
        foundIndex = mediaFileList.getFileList().indexOf(searchRec.foundMediaFile);  //calculate selection index --> file index
      else
        foundIndex = searchRec.tableRow;

      showMediaInLineNumber(foundIndex);       //ViewPort is not maintained sometimes. todo: find out why this is so

      selectSearchResultOnNextStartEdit = true;
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
      counter++; //if this is a continued search on occurrence was replaced before mediaFileList.replaceAll

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
    searchRec = null; //pointer to MediaFile's searchRec is deleted to indicate "no search active i.e. call findFirst or replaceAll first"
  }

  /**
   * show a dialog to select a master extension
   * for all files with that extension:
   * Copy the date from the file to all files with the same name (but different file extension)
   */
  public void copyFileDatesByExtension() {
    CopyFileDatesExtDialog dialog = new CopyFileDatesExtDialog(getPrimaryStage(), mediaFileList);

    if (dialog.showModal()) {
      //copy dates from master extension to all others with same name
      mediaFileList.copyFileDates(dialog.getResult());
    }
  }

  //--------------------------- getters and setters -------------------------------
  public MediaFileList getMediaFileList() {
    return mediaFileList;
  }

  public void registerGrayingDeleteMenuItems(MenuItem unDeleteLastMenuItem, MenuItem unDeleteMenuItem) {
    unDeleteMenuItem.disableProperty().bind(getMediaFileList().getDeletedFileListSizeProperty().isEqualTo(0));
    unDeleteLastMenuItem.disableProperty().bind(getMediaFileList().getDeletedFileListSizeProperty().isEqualTo(0));
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

  /**
   * determine primary window starting from "this" (fileTableView)
   * for getting focus, and binding sub windows (dialogs)
   * e.g. Cell needs primary stage for enabling hints in editable textFields and to get focus back to mainWindow from SearchDialog
   *
   * caution: during startup there will be a null pointer exception: getScene() reports null until fileTable is added to the main window
   * @return the main window
   */
  public Stage getPrimaryStage() {
    return (Stage)this.getScene().getWindow();
  }

  //how many lines are currently in the table
  public int getRowCount() {
    return mediaFileList.getFileList().size();
  }

  //used by Cell to determine if startEdit was called by Searching-Routing and therefore SearchResult should be highlighted
  public boolean isSelectSearchResultOnNextStartEdit() {
    return selectSearchResultOnNextStartEdit;
  }

  public void resetSelectSearchResultOnNextStartEdit() {
    selectSearchResultOnNextStartEdit = false;
  }

  //searchRec is != null only while find/replace is active
  public MediaFileList.SearchRec getSearchRec() {
    return searchRec;
  }

  /**
   * select previous line in fileTableView
   * the selectionChangeListener there will load the previous media then
   * (if there is no current selection (e.g. empty fileList) or no connection to the fileTableView (e.g. in undeleteDialog) nothing will happen)
   * @return true if successfully skipped to previous Media, false if already at the beginning of the list
   */
  public boolean showPreviousMedia() {
    boolean successful = false;
    if (getSelectionModel() != null) {

      int currentSelection = getSelectionModel().getSelectedIndex();
      if (currentSelection > 0) {
        getSelectionModel().clearAndSelect(currentSelection - 1);
        scrollViewportToIndex(currentSelection - 1, FileTableView.Alignment.TOP);
        successful = true;
      }
    }
    return successful;
  }

  /**
   * select next line in fileTableView
   * the selectionChangeListener there will load the next media then
   * (if there is no current selection (e.g. empty fileList) or no connection to the fileTableView (e.g. in undeleteDialog) nothing will happen)
   *
   * @return true if successfully skipped to next Media, false if already at the end of the list
   */
  public boolean showNextMedia() {
    boolean successful = false;

    if (getSelectionModel() != null) {

      int currentSelection = getSelectionModel().getSelectedIndex();
      if (currentSelection < getMediaFileList().getFileList().size() - 1) { //if not already at the end of the fileTableView's list
        getSelectionModel().clearAndSelect(currentSelection + 1);
        scrollViewportToIndex(currentSelection + 1, Alignment.BOTTOM);
        successful = true;
      }
    }
    return successful;
  }
  /**
   * jump to a line number in fileTableView
   * if the line number is smaller than 0 the first line is selected
   * if the line number is greater than the length of fileTableView the last element is selected
   *
   * @param lineNumber the line to jump to (zero based! i.e. first line is zero, last line is getFileList().size()-1)
   */
  public void showMediaInLineNumber(int lineNumber) {
    if (getSelectionModel() != null) {
      if (lineNumber <= 0) {
        getSelectionModel().clearAndSelect(0); //first element
        scrollViewportToIndex(0, FileTableView.Alignment.TOP);
      } else if (lineNumber < getMediaFileList().getFileList().size()) {
        getSelectionModel().clearAndSelect(lineNumber);
        scrollViewportToIndex(lineNumber, FileTableView.Alignment.CENTER);
      } else {
        getSelectionModel().clearAndSelect(getMediaFileList().getFileList().size() - 1); //last element
        scrollViewportToIndex(getMediaFileList().getFileList().size() - 1, FileTableView.Alignment.BOTTOM);
      }
    }
  }

  /**
   * currently selected line is deleted (moved to deletedFiles)
   * @return true if a file could be deleted (=something selected and not an empty list)
   */
  public boolean deleteCurrentLineAndMoveToNext(){
    boolean successful = false;

    if (mediaFileList.getFileList().size()>0 && getSelectionModel() != null) {
      int currentSelection = getSelectionModel().getSelectedIndex();
      //try to select next, if not possible try previous
      if (!showNextMedia())
        showPreviousMedia();

      mediaFileList.deleteFile(currentSelection);
    }
    return successful;
  }

  public enum Alignment {TOP, CENTER, BOTTOM}

  /**
   * EventHandler to handle CommitEdit() events from FieldCell in context of FileTableView
   * this handler is registered when the columns are built in the constructor
   * same handler for all columns
   * it is fired in TextFieldCell.commitEdit
   *
   * note: this class is implemented as a private subclass
   * - because only used here
   * - access to fileTableView.saveEditedValue would be unnecessarily complicated otherwise
   */
  private class CellEditCommitEventHandler implements EventHandler<TableColumn.CellEditEvent<MediaFile, String>> {
    @Override
    public void handle(TableColumn.CellEditEvent<MediaFile, String> t) {
      saveEditedValue(t.getRowValue(), t.getTableColumn(), t.getNewValue());
    }
  }

}
