package dringo.kissPhoto.view;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileTagged;
import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoTreeItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableRootItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems.EditableTagItem;
import dringo.kissPhoto.view.MetaInfoEditableTagsViewHelper.EditableTagTextFieldCellFactory;
import dringo.kissPhoto.view.viewerHelpers.MetaInfoEditableTagsViewContextMenu;
import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * <p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This View resides as a Tab in MetaInfoView
 * All contained tags are editable standard exif entries
 * If the entry exist in the {@link MediaFileTagged} exif header its value is shown and can be edited. Its value is not consistent with the AllTagsView until MediaFile is saved
 * If not, the entry is shown with empty value. It is nevertheless editable. If the user adds a value the entry is added to the MediaFile when saved and becomes visible in MetaInfoAllTagsView
 * <p>
 * the Exif Entries are shown in a tree table
 * <ul>
 *   <li>every branch is a group of similar entries</li>
 *   <li>every leaf is an exif entry </li>
 * </ul>
 * <p>
 * <p/>
 *
 * @author Dringo
 * @version 2022-01-07 meta info writing supported
 * @since 2021-11-13
 */


public class MetaInfoEditableTagsView extends TreeTableView<EditableMetaInfoItem> {
  //default values if value not in global settings
  private static final double DEFAULT_TYPE_COLUMN_WIDTH = 50;
  private static final double DEFAULT_KEY_COLUMN_WIDTH = 250;

  //IDs for globalSettings
  private static final String KEY_COLUMN_WIDTH = "metaInfoEditableView_KeyColumnWidth";
  private static final String SELECTED_KEY_PATH = "metaInfoEditableView_SelectedKeyPath";

  //connect columns to data
  // param.getValue() returns the TreeItem<EditableMetaInfoItem> instance for a particular TreeTableView row,
  // param.getValue().getValue() returns the EditableMetaInfoItem instance inside the TreeItem<EditableMetaInfoItem>
  private final TreeTableColumn<EditableMetaInfoItem, String> tagColumn = new TreeTableColumn<>(language.getString("tag"));
  private final TreeTableColumn<EditableMetaInfoItem, String> tagIDColumn = new TreeTableColumn<>(language.getString("tag.id"));
  private final TreeTableColumn<EditableMetaInfoItem, String> valueColumn = new TreeTableColumn<>(language.getString("value"));

  EditableTagTextFieldCellFactory tableTextFieldCellFactory = new EditableTagTextFieldCellFactory();
  CellEditCommitEventHandler cellEditCommitEventHandler = new CellEditCommitEventHandler();



  private final MetaInfoEditableTagsViewContextMenu contextMenu;
  private FileTableView fileTableView;        //some key presses are led to fileTableView

  //try to keep selection when changing media
  private TreeItem<EditableMetaInfoItem> userSelection = null;  //always try to keep the current selection if the next mediaFile is selected. (SelectionListener in use). It is valid from the moment a user has selected something or the last value has been read out of globalSettings
  private ObservableStringList userSelectionPath = null; //same as above as a String-Path. This is used as a cached value for userSelection which is valid from last getSelectionPath until user changes selection
  private boolean freezeUserSelection; //do not change currentSelection while loading new media, so that the selection can be restored after loading

  private final MetaInfoView metaInfoView; //link to the view we are contained in
  private MediaFile currentMediaFile = null; //if invisible then setMedia only stores here what has to be loaded if MetaInfoView becomes visible (=active), null while visible

  /**
   * Create an empty TreeTableView.
   * setMediaFile(mediaFile) will connect it later to the current mediaFile
   */
  public MetaInfoEditableTagsView(MetaInfoView metaInfoView) {
    this.metaInfoView = metaInfoView;

    tagColumn.setCellValueFactory(param -> param.getValue().getValue().getTagString());
    tagColumn.setPrefWidth(DEFAULT_KEY_COLUMN_WIDTH);
    getColumns().add(tagColumn);

    tagIDColumn.setCellValueFactory(param -> param.getValue().getValue().getTagIDString());
    tagIDColumn.setPrefWidth(DEFAULT_TYPE_COLUMN_WIDTH);
    getColumns().add(tagIDColumn);

    valueColumn.setCellValueFactory(param -> param.getValue().getValue().getValueString()); //for displaying
    valueColumn.prefWidthProperty().bind(widthProperty().subtract(tagColumn.widthProperty()).subtract(tagIDColumn.widthProperty())); //the rest of the available space
    valueColumn.setCellFactory(tableTextFieldCellFactory);  //for editing
    valueColumn.setOnEditCommit(cellEditCommitEventHandler);
    valueColumn.setEditable(true);
    getColumns().add(valueColumn);

    setShowRoot(false);
    setEditable(true);

    installSelectionListener();
    installKeyHandlers();

    //-----install bubble help ------------
    Tooltip tooltip = new Tooltip(language.getString("edit.the.tags.here"));
    tooltip.setShowDelay(ViewerControlPanel.TOOLTIP_DELAY); //same delay like in viewer control panel
    setTooltip(tooltip); //the text will be set whenever selection is changed (focus change listener)

    //------------ install Context menu
    contextMenu = new MetaInfoEditableTagsViewContextMenu(this);

    //hide context menu if clicked "somewhere else" or request focus on mouse click
    setOnMouseClicked(mouseEvent -> {
      if (contextMenu.isShowing()) {
        contextMenu.hide(); //this closes the context Menu
        mouseEvent.consume();
      } else {
        requestFocus();
      }

      //double-click will start edit even for empty cells
      if (mouseEvent.getClickCount()>1){
        editCurrentTag();
      }
    });

    setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));
  }

  public void setOtherViews(FileTableView fileTableView) {
    this.fileTableView = fileTableView;
  }

  private void installSelectionListener() {
    getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      //do not change currentSelection while loading new media, so that the selection can be restored after loading
      if (!freezeUserSelection) {
        userSelection = newValue;
        userSelectionPath = null; //invalidate the cached path for the selection (getUserSelectionPath() will update it)
      }
    });
  }

  private void installKeyHandlers() {
    setOnKeyPressed(keyEvent -> {
      switch (keyEvent.getCode()) {
        //Edit
        case F2: //F2 (from context menu) does not work, so a key listener ist installed
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
            editCurrentTag();
            keyEvent.consume();
          }
          break;
        case F3: //F3 (from context menu) does not work, so a key listener ist installed
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
            showTagInAllTagsView();
            keyEvent.consume();
          }
          break;
      }
    });

    //also consume the key up events to prevent the main menu react on the same event handled already by key down
    setOnKeyReleased(keyEvent -> {
      switch (keyEvent.getCode()) {
        //Edit
        case F2, F3:
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
            keyEvent.consume();
          }
          break;
      }
    });

  }

  /**
   * Store selected Path and Column Width in Global Settings
   */
  public void storeVisibilityInGlobalSettings() {
    KissPhoto.globalSettings.setProperty(SELECTED_KEY_PATH, getUserSelectionPath().toCSVString());
    KissPhoto.globalSettings.setProperty(KEY_COLUMN_WIDTH, Double.toString(tagColumn.getWidth()));
  }

  /**
   * read settings from settings file and restore the view accordingly
   */
  public void restoreVisibilityFromGlobalSettings() {
    try {
      userSelectionPath = new ObservableStringList();
      userSelectionPath.appendFromCSVString(KissPhoto.globalSettings.getProperty(SELECTED_KEY_PATH));
    } catch (Exception e) {
      userSelectionPath = null; //nothing selected
    }

    try {
      tagColumn.setPrefWidth(Double.parseDouble(KissPhoto.globalSettings.getProperty(KEY_COLUMN_WIDTH)));
    } catch (Exception e) {
      tagColumn.setPrefWidth(DEFAULT_KEY_COLUMN_WIDTH);
    }
  }


  /**
   * start edit in the value column
   */
  public void editCurrentTag(){
    edit(getFocusModel().getFocusedIndex(), valueColumn);
    //note: startEdit in EditableTagTextFieldCell only accepts this for tags not for directories
    //so this check does not need to be made here
  }

  public void addCurrentTagToFileTable() {
    trySelectTagIfDirectoryIsSelected(); //ignore the invalid call
    fileTableView.defineMetaInfoColumn(getUserSelectionPath()); //will be ignored with directories, has only an effect for tags
  }

  public boolean selectTag(int tagID){
    EditableMetaInfoTreeItem item = (EditableMetaInfoTreeItem) getRoot();
    if (item==null){
      //maybe only because of lazy laod --> force load and retry
      setMediaFile(currentMediaFile, true);
      item = (EditableMetaInfoTreeItem) getRoot();
    }
    //search in all directories
    if (item !=null){  //only if valid root
      item = item.searchForTag(tagID);
    }
    //item is now null (not found) or the tag to be selected (found)

    if (item != null) { //if found
      getSelectionModel().select(item);
      scrollTo(getRow(item));
    }

    return item != null;
  }

  private void trySelectTagIfDirectoryIsSelected() {
    if (userSelection == null) return;  //can only happen in the very beginning, when user has not clicked anywhere

    if (!userSelection.isLeaf()) { //if a directory is currently selected than select the first tag out of it
      userSelection.setExpanded(true);
      if (userSelection.getChildren().size() > 0)
        getSelectionModel().select(userSelection.getChildren().get(0));
      //if not then the directory remains selected
    }
  }

  /**
   * the path to the root will be converted to a String
   * - where the root is not included
   * - the order is from root to leaf (tag)
   *
   * @return the Path (StringList) as a single String where dot (".") separates the list elements
   */
  public String ConvertVisiblePathToDotString(ObservableStringList path) {
    String s = "";
    for (int i = path.size() - 2; i >= 0; i--) {
      s += path.get(i) + ".";
    }
    if (s.length() > 1)
      s = s.substring(0, s.length() - 1); //remove last sep (-1)
    return s;

  }


  /**
   * update the userSelectionPath variable if necessary
   *
   * @return the cached or updated userSelectionPath variable
   */
  public ObservableStringList getUserSelectionPath() {
    if (userSelectionPath == null)  //update only if selection has been changed since last time
      userSelectionPath = getPath(userSelection);

    return userSelectionPath;
  }

  /**
   * The path to the root is returned as a string list
   * item will be the first element in the list (index 0)
   * if item is null the returned list is empty
   *
   * @param item for this tree item the path to the root is extracted
   * @return the Path (=StringList) for the TreeItem so that it can be restored for another MediaFileTagged using selectPath
   */
  private ObservableStringList getPath(TreeItem<EditableMetaInfoItem> item) {
    ObservableStringList path = new ObservableStringList();

    //walk up to the root. item will be the first in the list (index 0)
    TreeItem<EditableMetaInfoItem> i = item;
    while (i != null) {
      path.add(i.getValue().getTagString().getValue());
      i = i.getParent();
    }
    return path;
  }

  /**
   * try to expand and select the path in the tree
   * the last element in the path (StringList) is interpreted as the root element and the leaf is index 0
   * if a sub element is not found then stop expanding and select the last found element
   * if the complete path is found, then select the leaf (index 0)
   * if not even the root is found then the selection is cleared
   *
   * @param path the tree path to be expanded and selected
   */
  private TreeItem<EditableMetaInfoItem> selectPath(ObservableStringList path) {
    TreeItem<EditableMetaInfoItem> item = getRoot();
    if (item != null) {
      int pathIndex = path.size() - 2; //start at the end of the list (-1 because indices are 0-based), ignore the invisible root (-1)
      boolean found = false;
      while (pathIndex >= 0) {
        //search for element in children
        for (TreeItem<EditableMetaInfoItem> child : item.getChildren()) {
          if (child.getValue()!=null)
            found = (child.getValue().getTagString().getValue().equalsIgnoreCase(path.get(pathIndex)));
          else
            found = false;
          if (found) {
            item = child; //take over as current item
            item.setExpanded(true);
            break;   //for
          }
        }
        if (!found)
          break;    //while
        pathIndex--;
      }
    }
    if (item != null && item != getRoot()) {     //something valid (at least a part of the path) has been found and will be selected now
      getSelectionModel().select(item);
      scrollTo(getRow(item));
      return item;
    } else {                                                 //not even the first part was valid so clear selection
      getSelectionModel().clearSelection();
      return null;
    }
  }

  /**
   * try to set the root of the tree to display the editable meta info
   * for speeding up reasons this is only performed if MetaInfoView is visible/selected Tab or necessary for searching etc (load now=true)
   * if loadNow = false internal currentMedia is set, but nothing is loaded. Therefore the first access (e.g. seachring) will be able to load the structure (tree) later
   *
   * @param mediaFile for which meta info shall be displayed
   * @param loadNow false=lazy load=load later=only if shown for the first time, true: load now e.g. because now became visible or for searching a tag
   */
  public void setMediaFile(MediaFile mediaFile, boolean loadNow) {
    currentMediaFile = mediaFile;

    if (loadNow) {
      if (mediaFile instanceof MediaFileTaggedEditable) {
        //status here: mediaFile is tagged, editable and not null
        EditableMetaInfoTreeItem metaInfoTreeItem = ((MediaFileTaggedEditable) mediaFile).getMetaInfoCached(this);

        freezeUserSelection = true;
        getUserSelectionPath();  //update the variable if necessary
        setRoot(metaInfoTreeItem); //metaInfoTreeItem might be null = empty if there is no metadata available
        TreeItem<EditableMetaInfoItem> selection = selectPath(userSelectionPath); //try to the select the same element as before in the new tree
        if (selection != null) userSelection = selection; //if successfully selected then init userSelection with it
        freezeUserSelection = false;
      } else {
        freezeUserSelection = true; //do not change user selection until a tagged file is loaded again
        setRoot(null); //do not show anything
      }
    }
  }

  /**
   * Cache support: lazy load the meta info from media
   * {MediaFileTaggedEditable.getMetaInfoCached uses this method to get the rootItem}
   *
   * @param mediaFile link back to the mediaFile which tries to fill its cache for this view
   * @return the MetaInfoTreeItem that should be cached
   */
  public EditableMetaInfoTreeItem getViewerSpecificMediaInfo(MediaFileTaggedEditable mediaFile) {
    //lazy load the MetaDataTreeItem if possible from cache (see MediaFile.getCachedMetaInfo())
    //or build it
    EditableRootItem rootItem = mediaFile.readExifHeader();
    if (rootItem != null)
      return new EditableMetaInfoTreeItem(rootItem);  //EditableRootItem defines the tree structure
    else
      return null;
  }

  /**
   * try to select the current tag in MetaInfoAllTagsView
   * if the current item is not a tag or the tag cannot be found in MetaInfoAllTagsView then nothing happens
   */
  public void showTagInAllTagsView(){
    if (userSelection != null){
      EditableMetaInfoItem item = userSelection.getValue();
      if (item instanceof EditableTagItem){
        metaInfoView.selectTagInAllTagsView(((EditableTagItem) item).getTagID());
      }
    }
  }

  public FileTableView getFileTableView() {
    return fileTableView;
  }

  /**
   * EventHandler to handle CommitEdit() events from FieldCell in context of FileTableView
   * this handler is registered when the columns are built in the constructor
   * same handler for all columns
   * it is fired in TextFieldCell.commitEdit
   */
  private static class CellEditCommitEventHandler implements EventHandler<TreeTableColumn.CellEditEvent<EditableMetaInfoItem, String>> {
    /**
     * Invoked when a specific event of the type for which this handler is
     * registered happens.
     *
     * @param event the event which occurred
     */
    @Override
    public void handle(TreeTableColumn.CellEditEvent<EditableMetaInfoItem, String> event) {
      event.getRowValue().getValue().saveEditedValue(event.getNewValue());
    }

  }

}
