package dringo.kissPhoto.view;

import com.drew.metadata.Metadata;
import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileTagged;
import dringo.kissPhoto.model.Metadata.MetaInfoItem;
import dringo.kissPhoto.model.Metadata.MetaInfoTreeItem;
import dringo.kissPhoto.model.Metadata.MetadataItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.StackPane;

import static dringo.kissPhoto.KissPhoto.globalSettings;
import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * View for showing all metadata info of media files. Eg. Exif or IPTC
 * It is the viewer for MetaInfoTreeItems i.e. MetaData, Directory or Tag
 * <p/>
 * <p>
 * This is a Pane with an integrated TreeTableView.
 * The divider position of the surrounding SplitPane is managed in this class: it is seen as the height of this metaInfoView ;-)
 * </p>
 *
 * @author Dringo
 * @version 2021-03-20 First implementation
 * @since 2021-03-14
 */


public class MetaInfoView extends StackPane {
  //default values if value not in global settings
  private static final boolean DEFAULT_VISIBILITY = true;
  private static final double DETAILS_AREA_DEFAULT_DIVIDER_POS = 0.9;
  private static final double DEFAULT_KEYCOLUMN_WIDTH=250;
  //IDs for globalSettings
  private static final String METAINFO_VIEW_VISIBLE = "metaInfoView_Visible";
  private static final String DETAILS_AREA_DIVIDER_POSITION = "detailsArea_DividerPosition";
  private static final String KEYCOLUMN_WIDTH = "metaInfoView_KeyColumnWidth";
  private static final String SELECTED_KEYPATH = "metaInfoView_SelectedKeyPath";


  private final SplitPane surroundingPane;    //surroundingPane the splitPane where the metaView lies in. When showing/hiding the dividerPos will be restored
  private FileTableView fileTableView;        //some key presses are led to fileTableView
  private MediaContentView mediaContentView;  //some key presses are led to mediaContentView
  private final TreeTableView<MetaInfoItem> treeTableView = new TreeTableView<>();

  //connect columns to data
// param.getValue() returns the TreeItem<MetaInfoItem> instance for a particular TreeTableView row,
// param.getValue().getValue() returns the MetaInfoItem instance inside the TreeItem<MetaInfoItem>
  private final TreeTableColumn<MetaInfoItem, String> keyColumn = new TreeTableColumn<>(language.getString("key"));
  private final TreeTableColumn<MetaInfoItem, String> valueColumn = new TreeTableColumn<>(language.getString("value"));

  private double rememberDividerPos = 0; //keep old DividerPos if MetaInfoView becomes visible again, i.e. valid while MetaInfoView is not visible. maintained in onShowHide

  private MediaFile mediaFileWhileHidden = null; //if invisible then setMedia only stores here what has to be loaded if MetaInfoView becomes visible (=active), null while visible

  //try to keep selection when changing media
  private TreeItem<MetaInfoItem> userSelection = null;  //always try to keep the current selection if the next mediaFile is selected. It is valid from the moment a user has selected something or the last value has been read out of globalSettings
  private ObservableStringList userSelectionPath = null; //same as above as a String-Path. This is used as a cached value for userSelection which is valid from last getSelectionPath until user changes selection
  private boolean freezeUserSelection; //do not change currentSelection while loading new media, so that the selection can be restored after loading

  /**
   * Create an empty TreeTableView.
   * setMediaFile(mediaFile) will connect it later to the current mediaFile
   * @param surroundingPane the splitPane where the metaView lies in. When showing/hiding the dividerPos will be restored
   */
  public MetaInfoView(SplitPane surroundingPane) {
    this.surroundingPane = surroundingPane;
    this.setMinHeight(0);

    keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyString());
    keyColumn.setPrefWidth(DEFAULT_KEYCOLUMN_WIDTH);
    treeTableView.getColumns().add(keyColumn);

    valueColumn.setCellValueFactory(param -> param.getValue().getValue().getValueString());
    valueColumn.prefWidthProperty().bind(widthProperty().subtract(keyColumn.widthProperty())); //the rest of the available space
//    valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
//    valueColumn.setEditable(true);
//    treeTableView.setEditable(true);

    treeTableView.getColumns().add(valueColumn);

    treeTableView.setShowRoot(false);

    heightProperty().addListener((observable, oldValue, newValue) -> onHeightChanged(newValue.doubleValue()));
    visibleProperty().addListener((observable, oldValue, newValue) -> onShowHide());

    installSelectionListener();
    installKeyHandlers();

    getChildren().add(treeTableView);
  }

  public void setOtherViews(FileTableView fileTableView, MediaContentView mediaContentView) {
    this.fileTableView = fileTableView;
    this.mediaContentView = mediaContentView;
  }

  private void installSelectionListener() {
    treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      //do not change currentSelection while loading new media, so that the selection can be restored after loading
      if (!freezeUserSelection) {
        userSelection = newValue;
        userSelectionPath = null; //invalidate the cached path for the selection (getUserSelectionPath() will update it)
      }
    });
  }

  private void installKeyHandlers() {
    setOnKeyReleased(keyEvent -> {   //KeyPressed and KeyTyped are not fired for F2
      switch (keyEvent.getCode()) {
        //Edit
        case F2: //F2 (from menu) does not work if multiple lines are selected so here a key listener ist installed for F2
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isMetaDown()) {
            keyEvent.consume();
            fileTableView.rename();
          }
          break;

        //Player
        case SPACE:
          if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && mediaContentView.getPlayerViewer().isVisible()) {
            keyEvent.consume();
            mediaContentView.getPlayerViewer().getPlayerControls().togglePlayPause();
          }
          break;
      }
    });
  }


  /**
   * update the userSelectionPath variable if necessary
   * @return the cached or updated userSelectionPath variable
   */
  private ObservableStringList getUserSelectionPath(){
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
  private ObservableStringList getPath(TreeItem<MetaInfoItem> item){
    ObservableStringList path = new ObservableStringList();

    //walk up to the root. item will be the first in the list (index 0)
    TreeItem<MetaInfoItem> i = item;
    while (i != null){
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
   * @param path the tree path to be expanded and selected
   */
  private void selectPath(ObservableStringList path){
    TreeItem<MetaInfoItem> item = treeTableView.getRoot();
    int pathIndex=path.size()-2; //start at the end of the list (-1 because indices are 0-based), ignore the invisible root (-1)
    boolean found = true; //if element has been found
    while (pathIndex>=0){
      //search for element in children
      for (TreeItem<MetaInfoItem> child:item.getChildren()) {
        found = (child.getValue().getKeyString().getValue().equalsIgnoreCase(path.get(pathIndex)));
        if (found){
          item = child; //take over as current item
          item.setExpanded(true);
          break;   //for
        }
      }
      if (!found)
        break;    //while
      pathIndex--;
    }
    if (item !=null && item != treeTableView.getRoot()) {     //something valid (at least a part of the path) has been found and will be selected now
      treeTableView.getSelectionModel().select(item);
      treeTableView.scrollTo(treeTableView.getRow(item));
    } else {                                                 //not even the first part was valid so clear selection
      treeTableView.getSelectionModel().clearSelection();
    }
  }


  /**
   * try to set the root on metadata of the mediaFile to display its meta info
   * for speeding up reasons this is only performed if MetaInfoView is visible (=active)
   * @param mediaFile for which meta info shall be displayed
   * @return true if the media file is compatible with metadataViewer (i.e. mediaFile is a MediaFileTagged)
   */
  public boolean setMediaFile(MediaFile mediaFile){
    if (isVisible()) {
      mediaFileWhileHidden = null; //not used while visible

      if (mediaFile instanceof MediaFileTagged) {
        //status here: mediaFile is tagged and not null
        MetaInfoTreeItem metaInfoTreeItem = ((MediaFileTagged)mediaFile).getMetaInfoCached(this);

        freezeUserSelection = true;
        getUserSelectionPath();  //update the variable if necessary
        treeTableView.setRoot(metaInfoTreeItem); //metaInfoTreeItem might be null = empty if there is no metadata available
        selectPath(userSelectionPath); //try to the select the same element as before in the new tree
        freezeUserSelection = false;

        return true;
      } else {
        freezeUserSelection = true; //do not change user selection until a tagged file is loaded again
        treeTableView.setRoot(null); //do not show anything
        return false;
      }
    } else
      mediaFileWhileHidden = mediaFile; //used only in this case to load Metadata in onShowHide()
      return (mediaFile instanceof MediaFileTagged);
  }

  /**
   * Cache support: lazy load the meta infos from media
   * @param mediaFileTagged link back to the mediaFile which tries to fill it's cache for this view
   * @return the MetadataTreeItem that should be cached
   */
  public MetaInfoTreeItem getViewerSpecificMediaInfo(MediaFileTagged mediaFileTagged){
    //lazy Load MetaData by calling getMetadata()
    //lazy load the MetaDataTreeItem if possible from cache (see MediaFile.getCachedMetaInfo())
    Metadata metadata = mediaFileTagged.getMetadata(); //get the cached value from the model
    if (metadata!=null) { //if Metadata could be loaded or had been loaded before
      return new MetaInfoTreeItem(new MetadataItem(metadata));
    }else {
      return null;
    }
  }


  /**
   * AutoHide (=Disable) is performed if SurroundingSplitPane's Divider makes this Pane's height =0
   * AutoShow (=Enable) if the Slider is moved to make the Pane's Height >0
   * @param newHeight the new window height is handed over by the event
   */
  public void onHeightChanged(double newHeight){
    setVisible(newHeight > 0.001);
  }
  /**
   * Maintain the Surrounding SplitPane if MetaInfo is shown/activated or hidden/disabled
   * while it is inactive no metadata is read or cached to speed up the application
   * when hiding (visible=false) the current position of the divider is stored and restored when showing again
   */
  public void onShowHide(){
    if (isVisible()) {       //i.e. just became visible
      surroundingPane.setDividerPosition(0, rememberDividerPos);
      if (mediaFileWhileHidden !=null)
        setMediaFile(mediaFileWhileHidden);
    }else{
      rememberDividerPos = surroundingPane.getDividerPositions()[0];
      surroundingPane.setDividerPosition(0, 1); //100%=only media is shown
    }
  }

  /**
   * if the dividerPosition is above DETAILS_AREA_DEFAULT_DIVIDER_POS it is set to this value
   * This is e.g. used if the user has manually set the visibility to true (e.g. by main menu item)
   * to make the view really become visible.
   * It is not(!) used if the user moves the divider with the mouse.
   */
  public void guaranteeMinimumHeight(){
    //note: 1.0=100%= MetaInfoView is invisible
    if (surroundingPane.getDividerPositions()[0]> DETAILS_AREA_DEFAULT_DIVIDER_POS) {
      surroundingPane.setDividerPosition(0, DETAILS_AREA_DEFAULT_DIVIDER_POS);
      rememberDividerPos = DETAILS_AREA_DEFAULT_DIVIDER_POS;
    }
  }

  /**
   * Store visibility (true/false) and dividerPos (in %) (i.e. "Pane size")
   * the current dividerPos is stored (if visible)
   * or rememberDividerPos (if currently invisible)
   */
  public void storeVisibilityInGlobalSettings() {
    globalSettings.setProperty(SELECTED_KEYPATH, getUserSelectionPath().toCSVString());

    globalSettings.setProperty(METAINFO_VIEW_VISIBLE, Boolean.toString(isVisible()));
    globalSettings.setProperty(KEYCOLUMN_WIDTH, Double.toString(keyColumn.getWidth()));

    if (isVisible()){
      globalSettings.setProperty(DETAILS_AREA_DIVIDER_POSITION, Double.toString(surroundingPane.getDividerPositions()[0]));
    }else{
      globalSettings.setProperty(DETAILS_AREA_DIVIDER_POSITION, Double.toString(rememberDividerPos));
    }
  }

  /**
   * read visibility and dividerPos from settings file and restore the view accordingly
   */
  public void restoreVisibilityFromGlobalSettings() {
    try {
      userSelectionPath = new ObservableStringList();
      userSelectionPath.appendFromCSVString(globalSettings.getProperty(SELECTED_KEYPATH));
    } catch (Exception e){
      userSelectionPath = null; //nothing selected
    }

    try {
      setVisible(Boolean.parseBoolean(globalSettings.getProperty(METAINFO_VIEW_VISIBLE)));
    } catch (Exception e) {
      setVisible(DEFAULT_VISIBILITY);
    }

    try {
      rememberDividerPos= Double.parseDouble(globalSettings.getProperty(DETAILS_AREA_DIVIDER_POSITION));
    } catch (Exception e) {
      rememberDividerPos=DETAILS_AREA_DEFAULT_DIVIDER_POS;
    }
    if (isVisible()) surroundingPane.setDividerPosition(0,rememberDividerPos);

    try {
      keyColumn.setPrefWidth(Double.parseDouble(globalSettings.getProperty(KEYCOLUMN_WIDTH)));
    } catch (Exception e) {
      keyColumn.setPrefWidth(DEFAULT_KEYCOLUMN_WIDTH);
    }
  }
}
