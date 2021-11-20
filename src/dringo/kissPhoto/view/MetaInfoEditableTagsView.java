package dringo.kissPhoto.view;

import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileTagged;
import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoTreeItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetadataItem;
import dringo.kissPhoto.view.MetaInfoEditableTagsViewHelper.EditableTagTextFieldCellFactory;
import dringo.kissPhoto.view.viewerHelpers.MetaInfoEditableTagsViewContextMenu;
import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import mediautil.image.jpeg.Exif;

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
 * @version 2021-11-13
 * @since 2021-11-13
 */


public class MetaInfoEditableTagsView extends TreeTableView<EditableMetaInfoItem> {
  //default values if value not in global settings
  private static final double DEFAULT_TYPE_COLUMN_WIDTH = 80;
  private static final double DEFAULT_KEY_COLUMN_WIDTH = 250;

  //IDs for globalSettings
  private static final String KEY_COLUMN_WIDTH = "metaInfoEditableView_KeyColumnWidth";
  private static final String SELECTED_KEY_PATH = "metaInfoEditableView_SelectedKeyPath";

  //connect columns to data
  // param.getValue() returns the TreeItem<EditableMetaInfoItem> instance for a particular TreeTableView row,
  // param.getValue().getValue() returns the EditableMetaInfoItem instance inside the TreeItem<EditableMetaInfoItem>
  private final TreeTableColumn<EditableMetaInfoItem, String> keyColumn = new TreeTableColumn<>(language.getString("key"));
  private final TreeTableColumn<EditableMetaInfoItem, String> typeColumn = new TreeTableColumn<>("Exif ID"); //for testing purposes only
  private final TreeTableColumn<EditableMetaInfoItem, String> valueColumn = new TreeTableColumn<>(language.getString("value"));

  EditableTagTextFieldCellFactory tableTextFieldCellFactory = new EditableTagTextFieldCellFactory();
  CellEditCommitEventHandler cellEditCommitEventHandler = new CellEditCommitEventHandler();



  private final MetaInfoEditableTagsViewContextMenu contextMenu;
  private FileTableView fileTableView;        //some key presses are led to fileTableView

  //try to keep selection when changing media
  private TreeItem<EditableMetaInfoItem> userSelection = null;  //always try to keep the current selection if the next mediaFile is selected. (SelectionListener in use). It is valid from the moment a user has selected something or the last value has been read out of globalSettings
  private ObservableStringList userSelectionPath = null; //same as above as a String-Path. This is used as a cached value for userSelection which is valid from last getSelectionPath until user changes selection
  private boolean freezeUserSelection; //do not change currentSelection while loading new media, so that the selection can be restored after loading

  private MediaFile currentMediaFile = null; //if invisible then setMedia only stores here what has to be loaded if MetaInfoView becomes visible (=active), null while visible

  /**
   * Create an empty TreeTableView.
   * setMediaFile(mediaFile) will connect it later to the current mediaFile
   */
  public MetaInfoEditableTagsView() {

    keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyString());
    keyColumn.setPrefWidth(DEFAULT_KEY_COLUMN_WIDTH);
    getColumns().add(keyColumn);

    //for testing purpose only
    typeColumn.setCellValueFactory(param -> param.getValue().getValue().getExifIDString());
    typeColumn.setPrefWidth(DEFAULT_TYPE_COLUMN_WIDTH);
    getColumns().add(typeColumn);     ///////////////////uncomment this line for test purpose to show the column with tag type ("Exif ID of TAG")
    //note: if this column should be displayed in a release: don't forget to adjust the Column-With binding to be adapted  for valueColumn two lines below

    valueColumn.setCellValueFactory(param -> param.getValue().getValue().getValueString());
    valueColumn.prefWidthProperty().bind(widthProperty().subtract(keyColumn.widthProperty())); //the rest of the available space
    valueColumn.setEditable(true);
    valueColumn.setCellFactory(tableTextFieldCellFactory);
    valueColumn.setOnEditCommit(cellEditCommitEventHandler);
    getColumns().add(valueColumn);

    setShowRoot(false);

    installSelectionListener();

    //-----install bubble help ------------
    Tooltip tooltip = new Tooltip(language.getString("edit.the.meta.info.entries.here"));
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

  public void addCurrentTagToFileTable() {
    trySelectTagIfDirectoryIsSelected(); //ignore the invalid call
    fileTableView.defineMetaInfoColumn(getUserSelectionPath()); //will be ignored with directories, has only an effect for tags
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
      path.add(i.getValue().getKeyString().getValue());
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
  private void selectPath(ObservableStringList path) {
    TreeItem<EditableMetaInfoItem> item = getRoot();
    int pathIndex = path.size() - 2; //start at the end of the list (-1 because indices are 0-based), ignore the invisible root (-1)
    boolean found = true; //if element has been found
    while (pathIndex >= 0) {
      //search for element in children
      for (TreeItem<EditableMetaInfoItem> child : item.getChildren()) {
        found = (child.getValue().getKeyString().getValue().equalsIgnoreCase(path.get(pathIndex)));
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
    if (item != null && item != getRoot()) {     //something valid (at least a part of the path) has been found and will be selected now
      getSelectionModel().select(item);
      scrollTo(getRow(item));
    } else {                                                 //not even the first part was valid so clear selection
      getSelectionModel().clearSelection();
    }
  }


  /**
   * try to set the root of the tree to display the editable meta info
   * for speeding up reasons this is only performed if MetaInfoView is visible (=active)
   *
   * @param mediaFile for which meta info shall be displayed
   */
  public void setMediaFile(MediaFile mediaFile) {
    currentMediaFile = mediaFile;

    if (isVisible()) {
      if (mediaFile instanceof MediaFileTaggedEditable) {
        //status here: mediaFile is tagged, editable and not null
        EditableMetaInfoTreeItem metaInfoTreeItem = ((MediaFileTaggedEditable) mediaFile).getMetaInfoCached(this);

        freezeUserSelection = true;
        getUserSelectionPath();  //update the variable if necessary
        setRoot(metaInfoTreeItem); //metaInfoTreeItem might be null = empty if there is no metadata available
        selectPath(userSelectionPath); //try to the select the same element as before in the new tree
        freezeUserSelection = false;
      } else {
        freezeUserSelection = true; //do not change user selection until a tagged file is loaded again
        setRoot(null); //do not show anything
      }
    }
  }

  /**
   * Cache support: lazy load the meta info from media
   *
   * @param mediaFileTaggedEditable link back to the mediaFile which tries to fill its cache for this view
   * @return the MetadataTreeItem that should be cached
   */
  public EditableMetaInfoTreeItem getViewerSpecificMediaInfo(MediaFileTaggedEditable mediaFileTaggedEditable) {
    //lazy Load MetaData by calling getMetadata()
    //lazy load the MetaDataTreeItem if possible from cache (see MediaFile.getCachedMetaInfo())
    Exif imageInfo = mediaFileTaggedEditable.getEditableImageInfo(); //get the cached value from the model
    if (imageInfo != null) { //if Metadata could be loaded or had been loaded before
      return new EditableMetaInfoTreeItem(new EditableMetadataItem(imageInfo));  //EditableMetadataItem defines the tree structure
    } else {
      return null;
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
  private class CellEditCommitEventHandler implements EventHandler<TreeTableColumn.CellEditEvent<EditableMetaInfoItem, String>> {
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
