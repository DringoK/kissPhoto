package dringo.kissPhoto.view;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.view.mediaViewers.*;
import dringo.kissPhoto.view.viewerHelpers.PlayerControlPanel;
import dringo.kissPhoto.view.viewerHelpers.RotatablePaneLayouter;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.MessageFormat;

/**
 * MIT License
 * Copyright (c)2023 kissPhoto
 * <p/>
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * This is the Pane where visible Media (photo, video) is shown<br>
 * It is a JavaFX-Pane which contains the links between the primary contentView and a secondary contentView (=fullScreenWindow)<br>
 * and all mediaViewers (Photo, Player, Other/None)<br>
 * The secondary contentView exists as soon as fullScreen has been activated for the fist time and is made visible/invisible from that on<br>
 * <br>
 * Here the strategy is implemented for selecting the compatible viewer (see setMedia() )
 * <p/>
 *
 * @author Dringo
 * @version 2023-10-03 Keep it simple: Description usage was too complicated: "Show Description" toggles now between description, show all and off.
 * @version 2023-01-05 del/ctrl-del, Shift-Ctrl-del and ctrl-z (=delete/undelete) support added while focus on MediaContentView . Moving to next/previous file cleaned up and moved to FileTableView
 * @version 2022-09-08 Fixed Full-Screen with TV-sets, parameter 'Stage' is not necessary (see getStage())
 * @version 2022-09-01 Touch Rotation Support added
 * @version 2021-01-09 isMediaPlayerActive() neu
 * @version 2020-11-02 change viewer strategy: every viewer decides by its own if it is compatible with the media. FullScreen Stage is now a singleton to save memory
 */
public class MediaContentView extends StackPane {
  public static final String SHOW_ON_NEXT_SCREEN_FULLSCREEN = "show.on.next.screen.fullscreen";

  private int enteredLineNumber = -1; //negative means: nothing entered

  private FileTableView fileTableView = null; //optional link to the fileTableView connected with this MediaContentView (for selecting pictures while focus is in MediaContentView
  private MetaInfoView metaInfoView = null;

  private FullScreenStage fullScreenStage = null; //in primary content view: link to secondary mediaContentView (fullscreen), in FullScreen Stage==null (no self-link) indicating that it has no Full Screen but is the Full Screen
  private Screen currentFullScreen = null; //in FullScreen-Mode changes in Scaling need to sync Stage and Current Full Screen sizes
  private MediaContentView primaryMediaContentView = null; //in full screen content view only: link to the mediaContentView of the primary stage

  private final StackPane mediaStackPane = new StackPane(); //the viewers lie one above the other, so fading will (in future) be possible even from photo to video clip ...
  private PhotoViewer photoViewer;
  private PlayerViewer playerViewer;
  private OtherViewer otherViewer;
  private final AttributesViewer attrViewer = new AttributesViewer(this);

  private MediaFile currentMediaFile = null; //the according mediaFile for attributes

  private MediaFile lastMediaFileBoundtoProgress = null;
  private ChangeListener<Number> lastListener = null;

  //Main-Menu connects "enabled" of the Player-/Image Menu with these Properties
  private final SimpleBooleanProperty isPlayerActive = new SimpleBooleanProperty(false);
  private final SimpleBooleanProperty isImageActive = new SimpleBooleanProperty(false);
  private final SimpleBooleanProperty isFullScreenActiveProperty = new SimpleBooleanProperty(false); //in primary MediaContentView redundant to hasActiveFullScreenMediaContentView(), but here menusItems can be bound to


  private double rotation; //original rotation angle during touch rotation

  /**
   * constructor
   */
  public MediaContentView() {
    super();
    init();
  }

  /**
   * @param primaryMediaContentView link to the primaryMediaContentView (i.e. from full screen to "normal view"
   * this constructor is only used internally for linking back the full-Screen version to the primary MediaContentView (in primary Stage)
   */
  protected MediaContentView(MediaContentView primaryMediaContentView) {
    super();
    this.primaryMediaContentView = primaryMediaContentView;
    init();
  }

  private void init() {
    //width/height-binding of the pane is not necessary because it lies in a splitPane which does binding automatically
    setStyle("-fx-background-color: black;");

    attrViewer.setVisible(false); //initially attributes are not visible

    //--all MediaViewers are collected in a StackPane
    photoViewer = new PhotoViewer(this);
    photoViewer.prefWidthProperty().bind(mediaStackPane.widthProperty());
    photoViewer.prefHeightProperty().bind(mediaStackPane.heightProperty());


    //find the best movie viewer for the system: 1:VLC, 2:JavaFX, 3:Dummy
    //try 1:VLC
    if (!KissPhoto.optionNoVLC) {
      try {
        playerViewer = new PlayerViewerVLCJ(this);
      } catch (Exception e) {
        playerViewer = null;
      }
    }

    if (playerViewer == null || !PlayerViewerVLCJ.isVlcAvailable()) {
      //try 2: JavaFX
      try {
       if (!KissPhoto.optionNoFX)
          playerViewer = new PlayerViewerFX(this);
        else //3: Dummy
          playerViewer = new PlayerViewerDummy(this);

      } catch (Exception e) {
        playerViewer = new PlayerViewerDummy(this);
        //noinspection CallToPrintStackTrace
        e.printStackTrace();
      }
    }

    playerViewer.prefWidthProperty().bind(mediaStackPane.widthProperty());
    playerViewer.prefHeightProperty().bind(mediaStackPane.heightProperty());

    otherViewer = new OtherViewer(this);
    mediaStackPane.getChildren().addAll(photoViewer, playerViewer, otherViewer);

    //----------------initialize all the rest -------------------------

    //the layouter can perform mediaStackPane.resize() even if it is rotated
    //centering/resizing is performed without further binding of its content
    RotatablePaneLayouter rotatablePaneLayouter = new RotatablePaneLayouter(mediaStackPane);
    //but the layouter has to be bound to its father (this)
    rotatablePaneLayouter.prefHeightProperty().bind(heightProperty());
    rotatablePaneLayouter.prefWidthProperty().bind(widthProperty());

    //add contents to mediaContentView
    StackPane.setAlignment(attrViewer, Pos.BOTTOM_CENTER);
    getChildren().addAll(rotatablePaneLayouter, attrViewer); //the attr. Viewer lies over all other viewers, but not in stack Pane (because it would fill the whole screen :-(

    setOnScroll(scrollEvent -> {
      if (!scrollEvent.isControlDown() && !scrollEvent.isAltDown() && !scrollEvent.isShiftDown()) {

        if (scrollEvent.getDeltaY()==0)
          return; //ignore "zero" scrolls which occur in Kubuntu-Linux
        else if (scrollEvent.getDeltaY() > 0)
          showPreviousMedia();
        else
          showNextMedia();
        scrollEvent.consume();
      }
    });

    //Touch rotate
    //default is "no rotation"

    setOnRotationStarted(event -> rotation =mediaStackPane.getRotate());
    setOnRotate(event -> mediaStackPane.setRotate(rotation+event.getTotalAngle()));
    setOnRotationFinished(event -> {
      //round to 90°
      if (event.getTotalAngle()>135 || event.getTotalAngle()<-135) currentMediaFile.rotate(MediaFile.RotateOperation.ROTATE180);
      else if (event.getTotalAngle()>45) currentMediaFile.rotate(MediaFile.RotateOperation.ROTATE90);
      else if (event.getTotalAngle()<-45) currentMediaFile.rotate(MediaFile.RotateOperation.ROTATE270);
      showRotationAndFlippingPreview();
    });

    setOnMouseClicked(event -> {
      if (event.getClickCount() > 1) { //if double-clicked
        toggleFullScreenAndNormal();
      }
    });

    setOnKeyPressed(keyEvent -> {
      boolean eventHandled = true;

      switch (keyEvent.getCode()) {
        //moving in fileTableView
        case HOME:
          showMediaInLineNumber(1);
          break;
        case END:
          showMediaInLineNumber(Integer.MAX_VALUE);
          break;
        case UP:  //only used if the current zoomable viewer does not consume it for moving
        case PAGE_UP: //works always
          showPreviousMedia();
          break;
        case DOWN: //only used if the current zoomable viewer does not consume it for moving
        case PAGE_DOWN: //works always
          showNextMedia();
          break;

        case ENTER:
          if (enteredLineNumber > 0)
            showMediaForEnteredLineNumber();
          else
            showNextMedia();
          break;

        //full screen support
        case F5:
          createFullScreenStage();
          break;
        case TAB:
          //tab = next screen
          showFullScreenStageOnNextScreen(!keyEvent.isShiftDown()); //shift-Tab = previous screen
          break;
        case ESCAPE:
          endFullScreen();
          break;

        //main menu short-cuts that should fire also when focus on mediaContentView
        case DELETE:
          if (keyEvent.isShiftDown() && keyEvent.isControlDown())  //shift-ctrl-del = undelete with dialog
            fileTableView.unDeleteWithDialog();
          else
            fileTableView.deleteSelectedFiles(false);  //ctrl-del or del = delete file
          break;
        case Z:
          if (keyEvent.isControlDown())
            fileTableView.undeleteLastDeletedFile();  //ctrl-z = undelete
          break;
        //attributes viewer control (toggle) (note: shortcuts are described in context menu --> don't forget to keep consistent!!!)
        case D:
          if (keyEvent.isControlDown()) {
            attrViewer.toggleDiplayMode();
            break;
          }

        default:
          eventHandled = false;
      }
      if (!eventHandled) {
        eventHandled = true;
        //collecting/selecting line number
        switch (keyEvent.getText()) {
          case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> collectDigit(Integer.parseInt(keyEvent.getText()));
          default -> eventHandled = false;
        }
      }

      if (eventHandled) keyEvent.consume();
    });
  }

  /**
   * call this before setting mediaContentView to null (e.g. when ending full screen mode)
   * e.g. to clean up all playerViewers
   */
  public void cleanUp() {
    if (photoViewer != null) photoViewer.cleanUp();
    if (playerViewer != null) playerViewer.cleanUp();
  }

  /**
   * perform unsaved/planned transformation as a preview by rotating/flipping the mediaStackPane
   * if the currentMediaFile == null then nothing happens
   */
  public void showRotationAndFlippingPreview() {
    if (currentMediaFile == null) return;

    switch (currentMediaFile.getRotateOperation()) {
      case ROTATE0 -> mediaStackPane.setRotate(0);
      case ROTATE90 -> mediaStackPane.setRotate(90);
      case ROTATE180 -> mediaStackPane.setRotate(180);
      case ROTATE270 -> mediaStackPane.setRotate(270);
    }

    //finally execute flipping
    if (currentMediaFile.isFlippedHorizontally()) mediaStackPane.setScaleX(-1);
    else mediaStackPane.setScaleX(1);

    if (currentMediaFile.isFlippedVertically()) mediaStackPane.setScaleY(-1);
    else mediaStackPane.setScaleY(1);

    //maintain the fullScreenStage's media also, if it is displayed currently
    if (hasActiveFullScreenMediaContentView()) {
      fullScreenStage.getMediaContentView().showRotationAndFlippingPreview();
    }
  }

  /**
   * @return true if this is the primaryContentView view with an active secondary fullScreen contentView
   */
  public boolean hasActiveFullScreenMediaContentView() {
    return (fullScreenStage != null) && fullScreenStage.isShowing();
  }

  /**
   * the main ContentView controls the decision which viewer to take and contains the active player<br>
   * the primary ContentView is main ContentView if there is no active FullScreen Window<br>
   * the secondaryContentView is the main ContentView if it is active<br>
   *
   * @return true, if this is a main ContentView which contains the active player and controls the decision about the viewer
   */
  public boolean isMainMediaContentView() {
    return (!hasActiveFullScreenMediaContentView());
  }

  /**
   * fullScreenStage has no fullscreen stage again but a primaryMediaContentView
   * fullScreenStage is the mainContentView if it is active
   * if fullScreenStage is not active the primaryMediaContentView is the main ContentView but not a fullScreen Stage
   *
   * @return true if this contentView is in a fullScreenStage
   */
  public boolean isFullScreenMediaContentView() {
    return (primaryMediaContentView != null);
  }

  /**
   * Report if the mediaPlayer is the currently active viewer.
   * Use: FileTableView.saveFolder() needs write access also to the currently active media. If this is the mediaPlayer
   * resetPlayer() needs to be called to free the media temporarily (esp. because of VLCJPlayer)
   * @return true if the mediaPlayer is the active (visible) player
   */
  public boolean isMediaPlayerActive(){
    return playerViewer.isVisible();
  }

  /**
   * When setMedia has found a compatible viewer for the main MediaContentView it calls this routine<br>
   * To activate the same kind of viewer in a secondary mediaContentViewer (i.e. if fullScreen is active, but this is not the fullScreen window)<br>
   * If this method must only be called from setMedia() in fullScreenStage's mediaContentView<br>
   * <br>
   *
   * @param mediaFile        the file to be shown in the secondary MediaContentView
   * @param compatibleViewer the viewer that is used in the main MediaContentView
   */
  public void syncMediaContentViews(MediaFile mediaFile, Object compatibleViewer) {
    currentMediaFile = mediaFile;
    attrViewer.setMedia(mediaFile);

    //----- empty directory
    switch (compatibleViewer) {
      case null -> activateEmptyMediaViewer();

      //----- Photo
      case PhotoViewer viewer -> {
        photoViewer.setMediaFileIfCompatible(mediaFile);
        activatePhotoViewer();
      }
      //----- Playable File (Video or Audio) only shown in main Window
      case PlayerViewer viewer -> activatePlayerOnOtherScreenHint();

      //----- Unsupported Media
      default -> activateOtherMediaViewer();
    }
  }

  /**
   * try to preload the media content / put it into the cache
   * by asking the appropriate viewer to get an object that will help to show the mediaFile quickly
   */
  public void preloadMediaContent(MediaFile mediaFile){
    if (!photoViewer.preloadMediaContent(mediaFile))  //first try the photoViewer
      playerViewer.preloadMediaContent(mediaFile);    //then try the active playerViewer
    //otherViewer has no Cache support
  }
  /**
   * set MediaFile to be shown<br>
   * <br>
   * If the mediaFile is already showing nothing happens to suppress multiple calls due to events while building the GUI during start-up<br>
   * <br>
   * If this is the main mediaContentView (i.e. the only one or the fullscreen = the controlling window which also contains the active player) then
   * find the suitable viewer by trying one after the other until a suitable viewer is found.
   * Note: at least the "OtherViewer" will fit in the end
   * when the compatible viewer has been found it is activated on the main mediaContentView and synchronized with the other views by activating the same viewer there<br>
   * <br>
   * if this is not the main view and a fullScreenStage exists then forward the call to the main mediaContentView<br>
   * <br>
   *
   * @param mediaFile     set the mediaFile to be displayed/played. Might be null, if empty directory is loaded
   * @param lastPlayerPos used for synchronizing position between main window and fullscreen MediaContentView. If null start at the beginning
   */
  public void setMedia(MediaFile mediaFile, Duration lastPlayerPos) {
    if (mediaFile == currentMediaFile) return; //suppress multiple calls

    if (this.isMainMediaContentView()) {
      try {
        currentMediaFile = mediaFile;
        attrViewer.setMedia(mediaFile);

        //try one viewer after the other to display the media

        //----- empty directory
        if (mediaFile == null) {  //e.g. if current directory is empty
          activateEmptyMediaViewer();
          if (this.isFullScreenMediaContentView()) primaryMediaContentView.syncMediaContentViews(null, null);

          //----- Photo
        } else if (photoViewer.setMediaFileIfCompatible(mediaFile)) {

          activatePhotoViewer();

          if (this.isFullScreenMediaContentView()) {
            primaryMediaContentView.showProgressBarForMediaFile(mediaFile); //progress still in main window :-)
            primaryMediaContentView.syncMediaContentViews(mediaFile, photoViewer);
          } else {
            showProgressBarForMediaFile(mediaFile);
          }

          //---- Playable File (Video or Audio)
        } else if (playerViewer.setMediaFileIfCompatible(mediaFile, lastPlayerPos)) {
          activatePlayerViewer();
          if (this.isFullScreenMediaContentView())
            primaryMediaContentView.syncMediaContentViews(mediaFile, playerViewer);

        } else {
          //------- if unsupported Media type -------
          activateOtherMediaViewer();
          if (this.isFullScreenMediaContentView())
            primaryMediaContentView.syncMediaContentViews(mediaFile, otherViewer);
        }

      } catch (
        Exception e) {
        //noinspection CallToPrintStackTrace
        e.printStackTrace();
      }
    } else {

      //-----if this was not the main ContentView forward the call to the main ContentView (fullScreenStage exists)
      fullScreenStage.getMediaContentView().setMedia(mediaFile, lastPlayerPos);
    }

    //test-code


  }

  /**
   * if playerViewer started to play and found out late that it is not playable it can call this method to show
   * this on all mediaContentViews (normal+fullScreen).<br>
   * E.g. MovieViewerVLCJ always reports compatible and reports error() later asynchronously<br>
   * <br>
   * otherViewer is activated
   */
  public void showPlayerError() {
    activateOtherMediaViewer();
    if (this.isFullScreenMediaContentView())
      primaryMediaContentView.syncMediaContentViews(currentMediaFile, otherViewer);
  }

  private void activatePlayerViewer() {
    isImageActive.set(false);
    isPlayerActive.set(true);

    photoViewer.setVisible(false);
    playerViewer.setVisible(true);
    otherViewer.setVisible(false);

    playerViewer.zoomToFit();
    showRotationAndFlippingPreview();
  }

  private void activatePhotoViewer() {
    isImageActive.set(true);
    isPlayerActive.set(false);

    photoViewer.setVisible(true);
    playerViewer.setVisible(false);
    otherViewer.setVisible(false);

    photoViewer.zoomToFit();
    showRotationAndFlippingPreview();
    stopAllActivePlayers();
  }

  private void activateEmptyMediaViewer() {
    activateOtherMediaViewer(false, KissPhoto.language.getString("nothing.to.show"));
  }

  /**
   * If VLC is not currently used a hint is added, that VLC should be installed to support more media file formats
   */
  public void activateOtherMediaViewer() {
    isImageActive.set(false);
    isPlayerActive.set(false);

    if (playerViewer instanceof PlayerViewerVLCJ)
      activateOtherMediaViewer(true, "");   //no vlc installation hint
    else
      //if vlc is not installed than add a hint
      activateOtherMediaViewer(true, KissPhoto.language.getString("to.support.more.file.formats.install.the.free.vlc.player.from.videolan.on.your.system.kissphoto.will.detect.and.use.it"));
  }

  /**
   * show otherViewer, hide all others
   *
   * @param mainMessageVisible true, if the main message (not supported media) should be shown
   * @param additionalMessage  extra information in a second line, or nothing if "" or null is provided
   */
  public void activateOtherMediaViewer(boolean mainMessageVisible, String additionalMessage) {
    isImageActive.set(false);
    isPlayerActive.set(false);

    otherViewer.setMainMessageVisible(mainMessageVisible);
    otherViewer.setAdditionalMessage(additionalMessage);

    photoViewer.setVisible(false);
    playerViewer.setVisible(false);
    otherViewer.setVisible(true);
    stopAllActivePlayers();
  }

  public void activatePlayerOnOtherScreenHint() {
    isImageActive.set(false);
    isPlayerActive.set(true);

    otherViewer.setMainMessageVisible(false);
    otherViewer.setAdditionalMessage(KissPhoto.language.getString("media.file.is.being.played.in.fullscreen.window"));

    photoViewer.setVisible(false);
    playerViewer.setVisible(false);
    otherViewer.setVisible(true);

  }
  //make Properties bindable e.g. in Main-Menu
  public SimpleBooleanProperty getIsPlayerActive(){return isPlayerActive;}
  public SimpleBooleanProperty getIsImageActive(){return isImageActive;}
  public SimpleBooleanProperty getIsFullScreenActiveProperty(){return isFullScreenActiveProperty;}

  private void stopAllActivePlayers() {
    //reset all probably playing players
    playerViewer.resetPlayer();
    clearProgress();
  }

  protected void clearProgress() {
    //if the mediaContentView is part of undeleteDialog there is no progress to be shown
    if (fileTableView != null) {
      if (lastListener != null && lastMediaFileBoundtoProgress != null && lastMediaFileBoundtoProgress.getContentProgressProperty() != null)
        lastMediaFileBoundtoProgress.getContentProgressProperty().removeListener(lastListener);
      lastListener = null;

      fileTableView.getStatusBar().clearProgress();
      fileTableView.getStatusBar().clearMessage();
      lastMediaFileBoundtoProgress = null;
    }
  }

  /**
   * bind progress of loading media to the status bar's progress bar
   *
   * @param mediaFile to be bound to the progress bar
   */
  private void showProgressBarForMediaFile(MediaFile mediaFile) {
    if (mediaFile != null) {
      if (fileTableView != null && mediaFile.getContentProgressProperty() != null) {  //fileTableView=null for UndeleteDialog
        //show Progressbar only if media not already completely loaded
        if (mediaFile.getContentProgressProperty().doubleValue() < 1.0) {
          StatusBar statusBar = fileTableView.getStatusBar();

          statusBar.showMessage(MessageFormat.format(KissPhoto.language.getString("loading.0"), mediaFile.getResultingFilename()));
          statusBar.getProgressProperty().bind(mediaFile.getContentProgressProperty());
          statusBar.showProgressBar();
          lastMediaFileBoundtoProgress = mediaFile;
          lastListener = new ProgressChangeListener(this);
          mediaFile.getContentProgressProperty().addListener(lastListener);
        }
      }
    }
  }

  /**
   * select previous line in fileTableView
   * the selectionChangeListener there will load the previous media then
   * (if there is no current selection (e.g. empty filelist) or no connection to the fileTableView (e.g. in undeleteDialog) nothing will happen)
   */
  public void showPreviousMedia() {
    if (fileTableView != null) {
      fileTableView.showPreviousMedia();
    }
    //this might steel the focus (set it to filetable)
    requestFocus();
  }
  /**
   * select next line in fileTableView
   * the selectionChangeListener there will load the previous media then
   * (if there is no current selection (e.g. empty filelist) or no connection to the fileTableView (e.g. in undeleteDialog) nothing will happen)
   * @return true if successfully skipped to next Media, false if already at the end of the list
   */
  public boolean showNextMedia() {
    boolean skipped = false;

    if (fileTableView != null) {
      skipped = fileTableView.showNextMedia();
    }
    //this might steel the focus (set it to filetable)
    requestFocus();

    return skipped;
  }

  /**
   * jump to a line number in fileTableView
   * if the line number is smaller than 0 the first line is selected
   * if the line number is greater than the length of fileTableView the last element is selected
   *
   * @param lineNumber the line to jump to (zero based! i.e. first line is zero, last line is getFileList().size()-1)
   */
  public void showMediaInLineNumber(int lineNumber) {
    fileTableView.showMediaInLineNumber(lineNumber);    //above selection might steel focus from mediaContentView if changed from movie to image
    requestFocus();
  }

  /**
   * Dependent on repeatMode and playListMode in PlayerControls
   * repeat Media, select next media
   * repeat Playlist, or halt at the end
   */
  public void showNextOrRepeatMedia() {
    PlayerControlPanel playerControls = playerViewer.getPlayerControls();
    if (playerControls.isPlayListMode() && !fileTableView.isEditMode()) {   //while editing a line in filetable changing the line would steel the cursor
      boolean skipped = showNextMedia();
      if (!skipped) {  //end of list reached
        if (playerControls.isRepeatMode()) {
          showMediaInLineNumber(0);
        }
        //else do nothing, media remains halted
      }

    } else {
      //not isPlayListMode()  or in FileTableView's EditMode
      if (playerControls.isRepeatMode()) {
        Platform.runLater(()-> playerViewer.rewindAndPlayWhenFinished());
      }
      //else do nothing, media remains to be finished
    }
  }


  /**
   * if digits are typed they are collected by this method
   *
   * @param digit a single digit (0..9) as an int
   */
  private void collectDigit(int digit) {
    if (enteredLineNumber < 0) //new number
      enteredLineNumber = digit;
    else //new digit for an existing entry
      enteredLineNumber = enteredLineNumber * 10 + digit;
  }

  /**
   * show the media in the line number of fileTableView that has been entered previously by the user
   * and that has been collected using collectDigit()
   * if no number has been entered before (i.e. enteredLineNumber<0) then nothing will happen
   */
  private void showMediaForEnteredLineNumber() {
    if (enteredLineNumber > 0) { //only if a valid number has been entered before
      showMediaInLineNumber(enteredLineNumber - 1); //-1 because list is zero-based human entry is 1-based
      enteredLineNumber = -1;  //consume the number
    }
  }

  /**
   * if content is in Fullscreen mode
   * then the underlying stage is moved to the next screen
   * if only one screen is available the window is moved to the primary screen
   * (this might be useful if multiscreen support was switched off while the content was displayed on a secondary screen)
   *
   * @param next if true next, if false previous screen is selected
   */
  public void showFullScreenStageOnNextScreen(boolean next) {
    if (getStage() instanceof FullScreenStage currentStage) {
      ObservableList<Screen> screens = Screen.getScreens();

      if (screens != null && screens.size() > 1) {
        //only if multiple screens are available --> select next screen
        ObservableList<Screen> currentScreens = Screen.getScreensForRectangle(getStage().getX(), getStage().getY(), 1,1); //relevant for "currentScreen" is the left upper corner of its fullScreenStage only


        if (!currentScreens.isEmpty())
          currentFullScreen = currentScreens.get(0);
        else
          currentFullScreen = Screen.getPrimary();

        int currentIndex = screens.indexOf(currentFullScreen);
        if (currentIndex >= 0) {
          if (next) {         //next or previous screen to be selected
            currentIndex++; //select next
            if (currentIndex >= screens.size()) currentIndex = 0; //make it a loop
          } else {
            currentIndex--; //select previous
            if (currentIndex < 0) currentIndex = screens.size() - 1; //make it a loop
          }

          //move to new Screen
          currentStage.moveToFullScreen(screens.get(currentIndex));  //select new currentIndex ;-)
        }
      }else{
        //if only one screen exists select primary screen
        currentStage.moveToFullScreen(Screen.getPrimary());
      }

    } else if (fullScreenStage != null) {      //if this method was called in the main mediaContentView then try to forward it to the fullScreen-stage
      fullScreenStage.mediaContentView.showFullScreenStageOnNextScreen(next);
    }
  }


  /**
   * open an additional MediaContentView in a full screen stage (window).
   * Any Players will be stopped in main window, but the position is handed over into full screen
   */
  public void createFullScreenStage() {
    if (!isFullScreenMediaContentView() && !hasActiveFullScreenMediaContentView()) { //only the primary window and not already fullScreen-Mode active
      Duration currentPlayerPosition = null; //only used if player was active to hand over the position to full-screen stage

      if (playerViewer.isVisible()) {
        currentPlayerPosition = playerViewer.getCurrentTime();
        playerViewer.resetPlayer(); //stop playback if running, because it will be started full screen
      }

      //Singleton: only build it once and only when needed for the first time otherwise reuse
      if (fullScreenStage==null){
        fullScreenStage = new FullScreenStage(this, metaInfoView); //use a new mediaContentView for fullScreen and link to "this" =normal view
        //bind controls together
        playerViewer.getPlayerControls().bindBidirectionalPlayPauseButtons(fullScreenStage.mediaContentView.getPlayerViewer().getPlayerControls().getPlayPauseButton());
        playerViewer.getPlayerControls().bindBidirectionalPlaylistModeButtons(fullScreenStage.mediaContentView.getPlayerViewer().getPlayerControls().getPlaylistButton());
        playerViewer.getPlayerControls().bindBidirectionalRepeatButtons(fullScreenStage.mediaContentView.getPlayerViewer().getPlayerControls().getRepeatButton());
        //getAttrViewer().copyState(primaryMediaContentView.getAttrViewer());
      }else{
        fullScreenStage.getMediaContentView().currentMediaFile = null; //reset it, so that setMedia detects a change and will set/start it again
      }
      fullScreenStage.mediaContentView.getAttrViewer().copyState(attrViewer); //synchronize normal and full screen attributesViewers

      isFullScreenActiveProperty.set(true); //setting the property to update all bound menuItems

      fullScreenStage.show();
      fullScreenStage.getMediaContentView().setMedia(getCurrentMediaFile(), currentPlayerPosition);       //and start playing, if player was active

      showFullScreenStageOnNextScreen(true); //if multiple screens are available use the "next" initially
    }
  }

  public void endFullScreen() {
    Duration fullScreenPlayerPosition = null; //only used if player was active to hand over the position to full-screen stage

    //only if in fullScreen-Mode (seen from the main window's view)
    if (hasActiveFullScreenMediaContentView()) {
      attrViewer.copyState(fullScreenStage.mediaContentView.getAttrViewer()); //synchronize normal and full screen attributesViewers
      if (fullScreenStage.mediaContentView.getPlayerViewer() != null && fullScreenStage.mediaContentView.getPlayerViewer().isVisible()) {
        fullScreenPlayerPosition = fullScreenStage.getMediaContentView().getPlayerViewer().getCurrentTime();
        fullScreenStage.mediaContentView.getPlayerViewer().resetPlayer();
      }
      fullScreenStage.close();
      fullScreenStage=null;
      currentFullScreen=null;

      MediaFile mediaFile = currentMediaFile; //save it
      currentMediaFile = null; //reset it, so that setMedia will have an effect, otherwise re-setting would be prevented
      setMedia(mediaFile, fullScreenPlayerPosition); //sync normal view with previous full screen view, restore currentMediaFile

      isFullScreenActiveProperty.set(false);  //setting the property to update all bound menuItems

      if (fileTableView != null) fileTableView.requestFocus();    //null if in UnDeleteDialog
    }
    //if in fullScreenMode (seen from the full-Screen-window itself)
    if (primaryMediaContentView != null) {
      primaryMediaContentView.endFullScreen();
    }
  }

  public void toggleFullScreenAndNormal() {
    if (isFullScreenMediaContentView() || hasActiveFullScreenMediaContentView()) {
      //if fullscreen active --> end it
      endFullScreen();
      if (fileTableView != null) fileTableView.getStatusBar().clearMessage();
    } else {
      //start it
      createFullScreenStage();
      //note: fileTableView = null if in UnDeleteDialog (i.e. no progressBar)
      if (fileTableView != null)
        fileTableView.getStatusBar().showMessage(KissPhoto.language.getString("esc.to.end.full.screen.tab.to.shift.full.screen.panel.between.screens"));
    }
  }

  public void setFullScreenMenuItemText(MenuItem showFullScreenItem){
    if (isFullScreenActiveProperty.get()) {
      showFullScreenItem.setText(KissPhoto.language.getString("end.full.screen.mode"));
      showFullScreenItem.setAccelerator(MainMenuBar.fullScreenKeyCombinationEnd);
    }else {
      showFullScreenItem.setText(KissPhoto.language.getString("full.screen"));
      showFullScreenItem.setAccelerator(MainMenuBar.fullScreenKeyCombinationStart);
    }
  }

  /**
   * the viewers can add the navigation menu items of mediaContentView to the end of their context-menu
   * by calling this method
   *
   * @param contextMenu the menu where the menuitems shall be added to
   */
  public void addContextMenuItems(final ContextMenu contextMenu) {

    //----- select media file from list
    MenuItem nextItem = new MenuItem(KissPhoto.language.getString("next.scroll.mouse.wheel.down")); //PgDn  or ENTER
    nextItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_DOWN));
    nextItem.setOnAction(actionEvent -> {
      showNextMedia();
      actionEvent.consume();
    });

    MenuItem previousItem = new MenuItem(KissPhoto.language.getString("previous.scroll.mouse.wheel.up")); //PgUp
    previousItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_UP));
    previousItem.setOnAction(actionEvent -> {
      showPreviousMedia();
      actionEvent.consume();
    });

    MenuItem homeItem = new MenuItem(KissPhoto.language.getString("first")); //home
    homeItem.setAccelerator(new KeyCodeCombination(KeyCode.HOME));
    homeItem.setOnAction(actionEvent -> {
      showMediaInLineNumber(1);
      actionEvent.consume();
    });

    MenuItem endItem = new MenuItem(KissPhoto.language.getString("last")); //end
    endItem.setAccelerator(new KeyCodeCombination(KeyCode.END));
    endItem.setOnAction(actionEvent -> {
      showMediaInLineNumber(Integer.MAX_VALUE);
      actionEvent.consume();
    });

    MenuItem gotoItem = new MenuItem(KissPhoto.language.getString("goto.number.type.digits.then.enter")); //ENTER
    gotoItem.setAccelerator(new KeyCodeCombination(KeyCode.ENTER));
    gotoItem.setOnAction(actionEvent -> {
      showMediaForEnteredLineNumber();
      actionEvent.consume();
    });
    contextMenu.getItems().addAll(nextItem, previousItem, homeItem, endItem, gotoItem);

    //-------- View
    MenuItem fullScreenItem = new MenuItem();
    setFullScreenMenuItemText(fullScreenItem);
    fullScreenItem.setOnAction(actionEvent -> {
      toggleFullScreenAndNormal();
      actionEvent.consume();
    });

    MenuItem showOnNextScreenItem = new MenuItem(KissPhoto.language.getString(SHOW_ON_NEXT_SCREEN_FULLSCREEN));
    showOnNextScreenItem.setAccelerator((new KeyCodeCombination(KeyCode.TAB))); //TAB, previous shift-Tab is not shown in menu
    showOnNextScreenItem.setOnAction(actionEvent -> showFullScreenStageOnNextScreen(true));
    showOnNextScreenItem.setDisable(true); //enable only in Full screen mode
    if (isFullScreenMediaContentView()) {
      //in FullScreenStage isFullScreen is always true why the text can be set to be constant
      isFullScreenActiveProperty.set(true);              //final
      setFullScreenMenuItemText(fullScreenItem); //final
      showOnNextScreenItem.setDisable(false);    //final
    }else {
      //bind to primaryContentView's isFullScreenActive property
      isFullScreenActiveProperty.addListener((observable, oldValue, newValue) -> setFullScreenMenuItemText(fullScreenItem));
      showOnNextScreenItem.disableProperty().bind(isFullScreenActiveProperty.not());
    }

    contextMenu.getItems().addAll(new SeparatorMenuItem(), fullScreenItem, showOnNextScreenItem);
  //---------- Metadata
    final MenuItem showGPSLocationItem = new MenuItem(KissPhoto.language.getString("show.gps.location.in.google.maps"));
    showGPSLocationItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
    showGPSLocationItem.setOnAction(event -> {
      event.consume();
    metaInfoView.showGPSPositionInGoogleMaps();
    });
    contextMenu.getItems().add(showGPSLocationItem);
    //-----> see below: setOnShowing/Hiding for enabling/disabling this menu item


//-------- Attributes
    final MenuItem showAttrItem = new MenuItem();
    showAttrItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
    showAttrItem.setOnAction(actionEvent -> attrViewer.toggleDiplayMode());
//--------
    contextMenu.getItems().addAll(new SeparatorMenuItem(), showAttrItem);

    contextMenu.setOnShowing(windowEvent -> {
      if (attrViewer.isVisible()) {  //if already visible then "all Attributes" can be activated
          if (attrViewer.isDisplayAll()){
            showAttrItem.setText(KissPhoto.language.getString("hide.attributes"));
          }else {
            showAttrItem.setText(KissPhoto.language.getString("display.all.attributes"));
          }
      }else{ //if it is not visible then Display of description can be activated
          showAttrItem.setText(KissPhoto.language.getString("display.description"));
      }

      //enable the menu item only if gps data is available
      showGPSLocationItem.setDisable(!metaInfoView.isValidGpsAvailable());
    });

    contextMenu.setOnHiding(event -> {
      //while hidden enable the menu item to enable the accelerator-key
      showGPSLocationItem.setDisable(false);
    });


  }

  public AttributesViewer getAttrViewer() {
    return attrViewer;
  }

  public PlayerViewer getPlayerViewer() {
    return playerViewer;
  }

  public PhotoViewer getPhotoViewer() {
    return photoViewer;
  }

  public Stage getStage() {
    return (Stage)this.getScene().getWindow();
  }

  public FileTableView getFileTableView() {
    return fileTableView;
  }

  public void setOtherViews(FileTableView fileTableView, MetaInfoView metaInfoView) {

    this.fileTableView = fileTableView;
    this.metaInfoView = metaInfoView;
  }

  public MediaFile getCurrentMediaFile() {
    return currentMediaFile;
  }

  /**
   * ProgressChangeListener cannot be an anonymous class because it has to be remembered where it has been activated
   * so that it can be stopped before the next media is loaded and connected to the progress bar
   * see showProgressBar() and clearProgress()
   */
  @SuppressWarnings("ClassCanBeRecord")
  private static class ProgressChangeListener implements ChangeListener<Number> {
    private final MediaContentView mediaContentView; //link back to the calling object

    public ProgressChangeListener(MediaContentView mediaContentView) {
      this.mediaContentView = mediaContentView;
    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      if (newValue.doubleValue() >= 0.99)
        mediaContentView.clearProgress();
    }

  }
  public MediaContentView getFullScreenMediaContentView() {
    if (fullScreenStage!=null)
      return fullScreenStage.getMediaContentView();
    else
      return null;
  }
}
