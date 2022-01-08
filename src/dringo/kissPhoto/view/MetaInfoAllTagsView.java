package dringo.kissPhoto.view;

import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileTagged;
import dringo.kissPhoto.model.Metadata.MetaInfoItem;
import dringo.kissPhoto.model.Metadata.MetaInfoTreeItem;
import dringo.kissPhoto.model.Metadata.RootItem;
import dringo.kissPhoto.model.Metadata.TagItem;
import dringo.kissPhoto.view.viewerHelpers.MetaInfoAllTagsViewContextMenu;
import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * <p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This View resides as a Tab in MetaInfoView
 * All contained tags are collected using metadata-extractor library.
 * This view is read only
 *
 * the tag entries are shown in a tree table
 * <ul>
 *   <li>every branch is a directory (IFD=Image File Directory)</li>
 *   <li>every leaf is an exif entry </li>
 * </ul>
 *
 * <p/>
 *
 *
 * @author Dringo
 * @version 2022-01-07 the view is now a TreeView instead of containing one. Added cooperation with Editable TagsView
 * @version 2021-11-10 allTagsView now in a tab, so window handling functionality moved to the surrounding MetaInfoView
 * @version 2021-11-07 support for showing a tag in FileTableView added
 * @since 2021-11-07
 */


public class MetaInfoAllTagsView extends TreeTableView<MetaInfoItem> {
  //default values if value not in global settings
  private static final double DEFAULT_TYPE_COLUMN_WIDTH = 50;
  private static final double DEFAULT_KEY_COLUMN_WIDTH = 250;

  //IDs for globalSettings
  private static final String KEY_COLUMN_WIDTH = "metaInfoView_KeyColumnWidth";
  private static final String SELECTED_KEY_PATH = "metaInfoView_SelectedKeyPath";

  //connect columns to data
  // param.getValue() returns the TreeItem<MetaInfoItem> instance for a particular TreeTableView row,
  // param.getValue().getValue() returns the MetaInfoItem instance inside the TreeItem<MetaInfoItem>
  private final TreeTableColumn<MetaInfoItem, String> tagColumn = new TreeTableColumn<>(language.getString("tag"));
  private final TreeTableColumn<MetaInfoItem, String> tagIDColumn =  new TreeTableColumn<>(language.getString("tag.id"));
  private final TreeTableColumn<MetaInfoItem, String> valueColumn = new TreeTableColumn<>(language.getString("value"));
  private final MetaInfoAllTagsViewContextMenu contextMenu;
  private FileTableView fileTableView;        //some key presses are led to fileTableView

  //try to keep selection when changing media
  private TreeItem<MetaInfoItem> userSelection = null;  //always try to keep the current selection if the next mediaFile is selected. (SelectionListener in use). It is valid from the moment a user has selected something or the last value has been read out of globalSettings
  private ObservableStringList userSelectionPath = null; //same as above as a String-Path. This is used as a cached value for userSelection which is valid from last getSelectionPath until user changes selection
  private boolean freezeUserSelection; //do not change currentSelection while loading new media, so that the selection can be restored after loading

  MetaInfoView metaInfoView; //link to the view we are contained in
  private MediaFile currentMediaFile = null; //if invisible then setMedia only stores here what has to be loaded if MetaInfoView becomes visible (=active), null while visible

  /**
   * Create an empty TreeTableView.
   * setMediaFile(mediaFile) will connect it later to the current mediaFile
   */
  public MetaInfoAllTagsView(MetaInfoView metaInfoView) {

    this.metaInfoView = metaInfoView;
    tagColumn.setCellValueFactory(param -> param.getValue().getValue().getTagString());
    tagColumn.setPrefWidth(DEFAULT_KEY_COLUMN_WIDTH);
    getColumns().add(tagColumn);

    tagIDColumn.setCellValueFactory(param -> param.getValue().getValue().getTagIDString());
    tagIDColumn.setPrefWidth(DEFAULT_TYPE_COLUMN_WIDTH);
    getColumns().add(tagIDColumn);

    valueColumn.setCellValueFactory(param -> param.getValue().getValue().getValueString()); //for displaying
    valueColumn.prefWidthProperty().bind(widthProperty().subtract(tagColumn.widthProperty()).subtract(tagIDColumn.widthProperty())); //the rest of the available space
    getColumns().add(valueColumn);

    setShowRoot(false);

    installSelectionListener();
    installKeyHandlers();

    //drag drop support to draw a tag to the fileTableColumn
    setOnDragDetected(event -> {
      trySelectTagIfDirectoryIsSelected();
      if (getUserSelectionPath().getSize() == 3){ //only if valid selection
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(getUserSelectionPath().toCSVString());
        Dragboard dragboard = startDragAndDrop(TransferMode.ANY);
        dragboard.setContent(clipboardContent);
      }
      event.consume();
    });

    //-----install bubble help ------------
    Tooltip tooltip = new Tooltip(language.getString("note.you.can.drag.a.meta.tag.to.the.file.table.to.use.it.there"));
    tooltip.setShowDelay(ViewerControlPanel.TOOLTIP_DELAY); //same delay like in viewer control panel
    setTooltip(tooltip); //the text will be set whenever selection is changed (focus change listener)


    //------------ install Context menu
    contextMenu = new MetaInfoAllTagsViewContextMenu(this);

    //hide context menu if clicked "somewhere else" or request focus on mouse click
    setOnMouseClicked(mouseEvent -> {
      if (contextMenu.isShowing()) {
        contextMenu.hide(); //this closes the context Menu
        mouseEvent.consume();
      } else {
        requestFocus();
      }

      //double-click tries to edit the current tag in MetaInfoEditableTagsView
      if (mouseEvent.getClickCount()>1){
        startEditInEditableTab();
        mouseEvent.consume();
      }
    });

    setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));
  }

  /**
   * try to edit the current tag in MetaInfoEditableTagsView
   * if the current item is not a tag or the tag cannot be found in MetaInfoEditableTagsView then nothing happens
   */
  public void startEditInEditableTab(){
    if (userSelection != null ){
      MetaInfoItem item = userSelection.getValue();
      if (item instanceof TagItem){
        metaInfoView.editTag(((TagItem) item).getTagID());
      }

    }
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
        case F2,F3: //F2 (from context menu) does not work so here a key listener ist installed for F2
          //accept also F3, because this was the way back to MetaInfoAllTagsView from MetaInfoEditableTagsView
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
            startEditInEditableTab();
            keyEvent.consume();
          }
          break;
      }
    });

    //also consume the mouse up events to prevent the main menu react on the same event handled already by key down
    setOnKeyReleased(keyEvent -> {
      switch (keyEvent.getCode()) {
        //Edit
        case F2, F3: //F2 (from menu) does not work if multiple lines are selected so here a key listener ist installed additionally
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
            keyEvent.consume();
          }
          break;
      }
    });
  }

  public void addCurrentTagToFileTable(){
    trySelectTagIfDirectoryIsSelected(); //ignore the invalid call
    fileTableView.defineMetaInfoColumn(getUserSelectionPath()); //will be ignored with directories, has only an effect for tags
  }

  public boolean selectTag(int tagID){
    MetaInfoTreeItem item = (MetaInfoTreeItem) getRoot();
    if (item==null){
      //maybe only because of lazy laod --> force load and retry
      setMediaFile(currentMediaFile, true);
      item = (MetaInfoTreeItem) getRoot();
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

    Platform.runLater(()->requestFocus() );
    return item != null;
  }

  private void trySelectTagIfDirectoryIsSelected() {
    if (userSelection==null) return;  //can only happen in the very beginning, when user has not clicked anywhere

    if (!userSelection.isLeaf()){ //if a directory is currently selected than select the first tag out of it
      userSelection.setExpanded(true);
      if (userSelection.getChildren().size()>0)
        getSelectionModel().select(userSelection.getChildren().get(0));
      //if not then the directory remains selected
    }
  }

  /**
   * the path to the root will be converted to a String
   * - where the root is not included
   * - the order is from root to leaf (tag)
   * @return  the Path (StringList) as a single String where dot (".") separates the list elements
   */
  public String ConvertVisiblePathToDotString(ObservableStringList path){
    String s = "";
    for (int i = path.size()-2; i>=0; i--) {
      s += path.get(i) + ".";
    }
    if (s.length()>1)
      s = s.substring(0, s.length()-1); //remove last sep (-1)
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
  private ObservableStringList getPath(TreeItem<MetaInfoItem> item) {
    ObservableStringList path = new ObservableStringList();

    //walk up to the root. item will be the first in the list (index 0)
    TreeItem<MetaInfoItem> i = item;
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
   * @return the item, that has been selected or null if nothing could be selected
   */
  private TreeItem<MetaInfoItem> selectPath(ObservableStringList path) {
    TreeItem<MetaInfoItem> item = getRoot();
    int pathIndex = path.size() - 2; //start at the end of the list (-1 because indices are 0-based), ignore the invisible root (-1)
    boolean found = true; //if element has been found
    while (pathIndex >= 0) {
      //search for element in children
      for (TreeItem<MetaInfoItem> child : item.getChildren()) {
        found = (child.getValue().getTagString().getValue().equalsIgnoreCase(path.get(pathIndex)));
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
    return item;
  }


  /**
   * try to set the root of metadata for the mediaFile to display its meta info
   * for speeding up reasons this is only performed if MetaInfoView is visible/selected Tab or necessary for searching etc (load now=true)
   * if loadNow = false internal currentMedia is set, but nothing is loaded. Therefore the first access (e.g. seachring) will be able to load the structure (tree) later
   *
   * @param mediaFile for which meta info shall be displayed
   * @param loadNow false=lazy load=load later=only if shown for the first time, true: load now e.g. because now became visible or for searching a tag
   */
  public void setMediaFile(MediaFile mediaFile, boolean loadNow) {
    currentMediaFile = mediaFile;

    if (loadNow) {
      if (mediaFile instanceof MediaFileTagged) {
        //status here: mediaFile is tagged and not null
        MetaInfoTreeItem metaInfoTreeItem = ((MediaFileTagged) mediaFile).getMetaInfoCached(this);

        freezeUserSelection = true;
        getUserSelectionPath();  //update the variable if necessary
        setRoot(metaInfoTreeItem); //metaInfoTreeItem might be null = empty if there is no metadata available
        TreeItem<MetaInfoItem> selection = selectPath(userSelectionPath); //try to the select the same element as before in the new tree
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
   *
   * @param mediaFileTagged link back to the mediaFile which tries to fill its cache for this view
   * @return the MetadataTreeItem that should be cached
   */
  public MetaInfoTreeItem getViewerSpecificMediaInfo(MediaFileTagged mediaFileTagged) {
    //lazy Load MetaData by calling getMetadata()
    //lazy load the MetaDataTreeItem if possible from cache (see MediaFile.getCachedMetaInfo())
    Metadata metadata = mediaFileTagged.getMetadata(); //get the cached value from the model
    if (metadata != null) { //if Metadata could be loaded or had been loaded before
      return new MetaInfoTreeItem(new RootItem(metadata));
    } else {
      return null;
    }
  }


  /**
   * AutoHide (=Disable) is performed if SurroundingSplitPane's Divider makes this Pane's height =0
   * AutoShow (=Enable) if the Slider is moved to make the Pane's Height >0
   *
   * @param newHeight the new window height is handed over by the event
   */
  public void onHeightChanged(double newHeight) {
    setVisible(newHeight > 0.001);
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
   * try to build a string with GPS coordinates like 47°05'29.0"N+8°27'52.0"E
   * from the metadata directory "GPS"
   *
   * @return a string with GPS coordinates or null if not available
   */
  public String getGpsCoordinates() {
    String gpsCoordinates = null;
    String latitudeRef = null;
    String latitude = null;
    String longitudeRef = null;
    String longitude = null;

    //assumption: currentMediaFile is not null. Therefore the caller (MetaInfoView) has to set currentMediaFile first if it was null
    if (currentMediaFile instanceof MediaFileTagged) {
      Metadata metadata = ((MediaFileTagged) currentMediaFile).getMetadata();
      GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
      if (gpsDirectory != null) {//if found
        //look up the necessary tags
        for (Tag tag : gpsDirectory.getTags()) {
          if (tag.getTagType() == GpsDirectory.TAG_LATITUDE_REF) latitudeRef = tag.getDescription();
          else if (tag.getTagType() == GpsDirectory.TAG_LATITUDE) latitude = tag.getDescription();
          else if (tag.getTagType() == GpsDirectory.TAG_LONGITUDE_REF) longitudeRef = tag.getDescription();
          else if (tag.getTagType() == GpsDirectory.TAG_LONGITUDE) longitude = tag.getDescription();
        }
      }
      //only return a string if all components of the GPS coordinate are available
      if (latitudeRef != null && latitude != null && longitudeRef != null && longitude != null){
        gpsCoordinates = latitude + latitudeRef + "+" + longitude + longitudeRef;
        gpsCoordinates = gpsCoordinates.replaceAll(",","."); //on some computers decimal is represented as comma which is not accepted by Google Maps

      }
    }
    return gpsCoordinates;
  }
}



