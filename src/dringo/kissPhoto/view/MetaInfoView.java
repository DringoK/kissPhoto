package dringo.kissPhoto.view;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.helper.AppStarter;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileTagged;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;

import java.util.Objects;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * <p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * MetaInfoView is the Window in the lower right corner which shows the Meta Tags (e.g. Exif) of a {@link MediaFileTagged}
 * setMedia() loads the meta info in the memory
 * It has two tabs: one to show all tags, but readonly, one to show editable tabs only
 * <p>
 * <p/>
 *
 *
 * @author Dringo
 * @version 2023-10-14 Fixed: 1.Restoring of divider position fixed 2.Resizing of the main window no longer activates metaInfoView if switched off: property SplitPane.ResizableWithParent is "false" now.
 * @version 2021-12-30 Fixed: "Show GPS in Google Maps" works now also if metaInfoView is not visible
 * @version 2021-11-10 tags introduced, MetaInfoAllTagsView is the old implementation which is now in one of the tabs
 * @since 2021-11-10
 */


public class MetaInfoView extends TabPane {
  //default values if value not in global settings
  private static final boolean DEFAULT_VISIBILITY = true;
  private static final double DETAILS_AREA_DEFAULT_DIVIDER_POS = 0.9;

  //IDs for globalSettings
  private static final String META_INFO_VIEW_VISIBLE = "metaInfoView_Visible";
  private static final String DETAILS_AREA_DIVIDER_POSITION = "detailsArea_DividerPosition";
  private static final String META_INFO_ACTIVE_TAB = "metaInfoView_ActiveTab";

  private final MetaInfoAllTagsView metaInfoAllTagsView = new MetaInfoAllTagsView(this);
  private final MetaInfoEditableTagsView metaInfoEditableTagsView = new MetaInfoEditableTagsView(this);

  private final SplitPane surroundingPane;    //surroundingPane the splitPane where the metaView lies in. When showing/hiding the dividerPos will be restored
  private final Tab metaInfoAllTagsViewTab;
  private final Tab metaInfoEditableTagsViewTab;
  private MediaContentView mediaContentView;  //some key presses are led to mediaContentView
  private StatusBar statusBar;
  private double rememberDividerPos = 0; //keep old DividerPos if MetaInfoView becomes visible again, i.e. valid while MetaInfoView is not visible. maintained in onShowHide

  private MediaFile currentMediaFile = null; //if invisible then setMedia only stores here what has to be loaded if MetaInfoView becomes visible (=active), null while visible

  /**
   * Create a Tabbed Pane with MetaInfoAllTagsView and MetaInfoEditableTagsView
   * setMediaFile(mediaFile) will connect them later to the current mediaFile
   *
   * @param surroundingPane the splitPane where the metaView lies in. When showing/hiding the dividerPos will be restored
   */
  public MetaInfoView(SplitPane surroundingPane) {
    this.surroundingPane = surroundingPane;
    this.setMinHeight(0);

    heightProperty().addListener((observable, oldValue, newValue) -> onHeightChanged(newValue.doubleValue()));
    visibleProperty().addListener((observable, oldValue, newValue) -> onShowHide(newValue));
    SplitPane.setResizableWithParent(this,false); //node-property is evaluated by the surrounding splitPane


    metaInfoAllTagsViewTab = new Tab(language.getString("contained.tags.Tab"), metaInfoAllTagsView);
    metaInfoAllTagsViewTab.setClosable(false);
    getTabs().add(metaInfoAllTagsViewTab);

    metaInfoEditableTagsViewTab = new Tab(language.getString("edit.tags.tab"), metaInfoEditableTagsView);
    metaInfoEditableTagsViewTab.setClosable(false);
    getTabs().add(metaInfoEditableTagsViewTab);

    installKeyHandlers();

    //whenever the active Tab is changed, setMedia has probably not  (lazy) loaded the file for the new tab, so set it on change:
    getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (currentMediaFile instanceof MediaFileTagged) {
        //status here: mediaFile is tagged and not null
        if (newValue == metaInfoAllTagsViewTab)
          metaInfoAllTagsView.setMediaFile(currentMediaFile, isVisible());
        if (newValue == metaInfoEditableTagsViewTab)
          metaInfoEditableTagsView.setMediaFile(currentMediaFile, isVisible());
      }
    });
  }

  public void setOtherViews(FileTableView fileTableView, MediaContentView mediaContentView, StatusBar statusBar) {
    this.mediaContentView = mediaContentView;
    this.statusBar = statusBar;

    //hand the links further to the contained Tabs:
    metaInfoAllTagsView.setOtherViews(fileTableView);
    metaInfoEditableTagsView.setOtherViews(fileTableView);
  }

    private void installKeyHandlers() {
    setOnKeyReleased(keyEvent -> {   //KeyPressed and KeyTyped are not fired for F2
      //Player
      if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.SPACE) {
        if (!keyEvent.isControlDown() && !keyEvent.isShiftDown() && mediaContentView.getPlayerViewer().isVisible()) {
          keyEvent.consume();
          mediaContentView.getPlayerViewer().getPlayerControls().togglePlayPause();
        }
      }
    });
  }

  /**
   * pass the media file to all MetaInfoViews contained in the Tabs
   * for speeding up reasons this is only performed if MetaInfoView is visible (=active)
   *
   * @param mediaFile for which meta info shall be displayed
   */
  public void setMediaFile(MediaFile mediaFile) {
    currentMediaFile = mediaFile;
    //lazy load: if not visible just currentMedia is taken over but nothing is loaded
    metaInfoAllTagsView.setMediaFile(mediaFile, isVisible() && metaInfoAllTagsViewTab.isSelected());
    metaInfoEditableTagsView.setMediaFile(mediaFile, isVisible() && metaInfoEditableTagsViewTab.isSelected());
  }

  /**
   * @return the AllTagsView e.g. for FileTableView to identify it as the source of a mouse drag event
   */
  public  MetaInfoAllTagsView getMetaInfoAllTagsView(){
    return metaInfoAllTagsView;
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
   * Maintain the Surrounding SplitPane if MetaInfo is shown/activated or hidden/disabled
   * while it is inactive no metadata is read or cached to speed up the application
   * when hiding (visible=false) the current position of the divider is stored and restored when showing again
   */
  public void onShowHide(boolean visible) {
    if (visible) {       //i.e. just became visible
      surroundingPane.setDividerPosition(0, rememberDividerPos);
      if (currentMediaFile != null) setMediaFile(currentMediaFile);
      guaranteeMinimumHeight(); //after manual showing guarantee a minimum height to ensure it really became visible
    } else {
      double divPos = surroundingPane.getDividerPositions()[0];
      surroundingPane.setDividerPosition(0, 1.0); //100%=only media is shown
      rememberDividerPos = divPos;
    }
  }

  /**
   * if the dividerPosition is above DETAILS_AREA_DEFAULT_DIVIDER_POS it is set to this value
   * This is e.g. used if the user has manually set the visibility to true (e.g. by main menu item)
   * to make the view really become visible.
   * It is not(!) used if the user moves the divider with the mouse.
   */
  public void guaranteeMinimumHeight() {
    //note: 1.0=100%= MetaInfoView is invisible
    if (surroundingPane.getDividerPositions()[0] > DETAILS_AREA_DEFAULT_DIVIDER_POS) {
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
    KissPhoto.globalSettings.setProperty(META_INFO_VIEW_VISIBLE, Boolean.toString(isVisible()));

    if (isVisible()) {
      KissPhoto.globalSettings.setProperty(DETAILS_AREA_DIVIDER_POSITION, Double.toString(surroundingPane.getDividerPositions()[0]));
    } else {
      KissPhoto.globalSettings.setProperty(DETAILS_AREA_DIVIDER_POSITION, Double.toString(rememberDividerPos));
    }

    //store the visibility settings of the tabs
    metaInfoAllTagsView.storeVisibilityInGlobalSettings();
    metaInfoEditableTagsView.storeVisibilityInGlobalSettings();

    //and finally, store which tab was active

    KissPhoto.globalSettings.setProperty(META_INFO_ACTIVE_TAB, Integer.toString(getSelectionModel().getSelectedIndex()));
  }

  /**
   * read visibility and dividerPos from settings file and restore the view accordingly
   */
  public void restoreVisibilityFromGlobalSettings() {
    //restore the visibility settings of the tabs
    metaInfoAllTagsView.restoreVisibilityFromGlobalSettings();
    metaInfoEditableTagsView.restoreVisibilityFromGlobalSettings();

    //restore which tab was active
    try {
      getSelectionModel().select(Integer.parseInt(KissPhoto.globalSettings.getProperty(META_INFO_ACTIVE_TAB)));
    } catch (Exception e) {
      //getSelectionModel().select(0); //default tab (0 is the default therefore nothing to do)
    }

    boolean wasVisible = DEFAULT_VISIBILITY;
    try{
      wasVisible = Boolean.parseBoolean(KissPhoto.globalSettings.getProperty(META_INFO_VIEW_VISIBLE));
    } catch (Exception e) {
      //is already default --> nothing to do
    }

    try {
      rememberDividerPos = Double.parseDouble(KissPhoto.globalSettings.getProperty(DETAILS_AREA_DIVIDER_POSITION));
    } catch (Exception e) {
      rememberDividerPos = DETAILS_AREA_DEFAULT_DIVIDER_POS;
    }

    double divPos = rememberDividerPos;

    if (wasVisible)
      onShowHide(true);  //now chang event, therefore trigger onShowHide manually
    else
      setVisible(false);  //change in visibility fires OnShowHide() which might change remember divPos to 1.0 (hide)

    //set the restored dividerPos if visible:
    rememberDividerPos = divPos;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isValidGpsAvailable(){
    metaInfoAllTagsView.setMediaFile(currentMediaFile, true); //just for the case that metadata not yet loaded e.g. because allTagsView is not visible
    return metaInfoAllTagsView.getGpsCoordinates() != null;
  }
  /**
   * Try to open maps.google.com in the default browser
   * at the coordinates given in the GPS directory of the current media file
   * e.g.
   * <a href="https://www.google.com/maps/place/47">...</a>°05'29.0"N+8°27'52.0"E
   */
  public void showGPSPositionInGoogleMaps() {
    metaInfoAllTagsView.setMediaFile(currentMediaFile, true); //just for the case that metadata not yet loaded e.g. because allTagsView is currently not visible

    boolean successful = false;
    String gpsCoordinates = metaInfoAllTagsView.getGpsCoordinates();
    if (gpsCoordinates != null){
      String url = "https://www.google.com/maps/place/" + gpsCoordinates;
      successful = AppStarter.tryToBrowse(url);
    }
    if (successful)
      statusBar.showMessage(gpsCoordinates + " " + language.getString("opened.in.google.maps"));
    else
      statusBar.showError(language.getString("no.valid.gps.data.available.for.the.current.media.file"));
  }

  /**
   * try to find the tagId in
   * @param tagId  id of the tag to bei edited
   * @return true if tag found
   */
  public boolean editTag(int tagId){
    boolean successful = metaInfoEditableTagsView.selectTag(tagId);
    if (successful){
      getSelectionModel().select(metaInfoEditableTagsViewTab);
      metaInfoEditableTagsView.editCurrentTag();
    }else {
      statusBar.showError(language.getString("current.tag.not.editable.only.exif.tags.can.be.edited"));
    }
    return successful;
  }

  public boolean selectTagInAllTagsView(int tagId){
    boolean successful = metaInfoAllTagsView.selectTag(tagId);
    if (successful){
      getSelectionModel().select(metaInfoAllTagsViewTab);
    }else {
      statusBar.showError(language.getString("current.tag.cannot.be.selected.in.alltagsview.it.is.not.yet.contained.in.the.file.change.tag.and.save.file.to.add.it"));
    }
    return successful;
  }

}



