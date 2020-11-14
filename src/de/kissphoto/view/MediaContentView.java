package de.kissphoto.view;

import de.kissphoto.KissPhoto;
import de.kissphoto.helper.I18Support;
import de.kissphoto.model.ImageFileRotater;
import de.kissphoto.model.MediaFile;
import de.kissphoto.model.MediaFileRotater;
import de.kissphoto.view.helper.PlayerViewer;
import de.kissphoto.view.helper.RotatablePaneLayouter;
import de.kissphoto.view.mediaViewers.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * This is the Pane where visible Media (photo, video) is shown<br>
 * It is a JavaFX-Pane which contains the links between the primaryWindow and fullScreenWindow<br>
 * and all mediaViewers (Photo, Player, Other/None)<br>
 * <br>
 * Here the strategy for selecting the right viewer is implemented (see setMedia() )
 * <p/>
 *
 * @author Dr. Ingo Kreuz
 * @version 2020-11-02 change viewer strategy: every viewer decides by it's own if it is compatible with the media
 */
public class MediaContentView extends Pane {
  public static final String SHOW_ON_NEXT_SCREEN_FULLSCREEN = "show.on.next.screen.fullscreen";
  private static ResourceBundle language = I18Support.languageBundle;

  //every viewer has these items. For enabling/disabling keep a reference to all of them
  static private ObservableList<MenuItem> showOnNextScreenItems = FXCollections.observableArrayList();
  static private ObservableList<MenuItem> fullScreenItems = FXCollections.observableArrayList();

  private int enteredLineNumber = -1; //negative means: nothing entered

  private FileTableView fileTableView = null; //optional link to the fileTableView connected with this MediaContentView (for selecting pictures while focus is in MediaContentView

  private Stage owner = null; //normal mediaContentView is in the primary stage, full-Screen MediaContentView is in a dialog-stage which is in fullScreenMode
  private FullScreenStage fullScreenStage = null; //this stage is used (!=null) if showFullScreen() has been called. It again has a mediaContentView
  private MediaContentView primaryMediaContentView = null; //in full screen mode only: link to the mediaContentView of the primary stage

  StackPane mediaStackPane = new StackPane(); //the viewers lie one above the other, so fading will (in future) be possible even from photo to video clip ...
  private PhotoViewer photoViewer;
  private PlayerViewer playerViewer;
  private OtherViewer otherViewer;
  private AttributesViewer attrViewer = new AttributesViewer(this);

  private Object currentMedia = null; //the content from the cache
  private MediaFile currentMediaFile = null; //the according mediaFile for attributes
  ImageFileRotater.RotateOperation currentRotation = MediaFileRotater.RotateOperation.ROTATE0;

  /**
   * put media/image into the appropriate viewer and set it visible (and all others invisible)
   * if null is passed all internal views are set to invisible (= nothing is shown)
   * <p>
   * if a lastPlayerPos!=null has been passed and a Player-Media becomes active then
   * the Media is seeked to this position as soon as it is loaded (see PlayerViewer.setMedia())
   *
   * @param lastPlayerPos null=no effect, not null=try to seek the position for player media
   * @param mediaFile     is the mediaFile of which it's content is to be shown
   */
  private MediaFile lastMediaFileBoundtoProgress = null;
  private ChangeListener lastListener = null;

  /**
   * @param owner                   link to the stage which contains this mediaContentView
   * @param primaryMediaContentView link to the primaryMediaContentView (i.e. from full screen to "normal view"
   * @constructor this constructor is only used internally for linking back the full-Screen version to the primary MediaContentView (in primary Stage)
   */
  protected MediaContentView(Stage owner, MediaContentView primaryMediaContentView) {
    super();
    this.primaryMediaContentView = primaryMediaContentView;
    init(owner);
  }

  /**
   * @constructor
   */
  public MediaContentView(Stage owner) {
    super();
    init(owner);
  }


  private void init(Stage owner) {
    this.owner = owner;
    //width/height-binding of the pane is not necessary because it lies in a splitPane which does binding automatically
    setStyle("-fx-background-color: black;");

    attrViewer.setVisible(false); //initially attributes are not visible

    //--all MediaViewers are collected in a StackPane
    photoViewer = new PhotoViewer(this);
    photoViewer.fitWidthProperty().bind(mediaStackPane.widthProperty());
    photoViewer.fitHeightProperty().bind(mediaStackPane.heightProperty());


    //find the best movie viewer for the system: 1:VLC, 2:JavaFX, 3:Dummy
    //try 1:VLC
    if (!KissPhoto.optionNoVLC) {
      try {
        playerViewer = new MovieViewerVLCJ(this);
      } catch (Exception e) {
        playerViewer = null;
      }
    }

    if (playerViewer == null || !((MovieViewerVLCJ) playerViewer).isVlcAvailable()) {
      //try 2: JavaFX
      try {
        if (!InetAddress.getLocalHost().getHostName().startsWith("CMTC"))  //videos cannot be played by JavaFX on Daimler-Installations
          playerViewer = new MovieViewerFX(this);
        else //3: Dummy
          playerViewer = new MovieViewerDummy(this);

      } catch (UnknownHostException e) {
        playerViewer = new MovieViewerDummy(this);
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
    getChildren().addAll(rotatablePaneLayouter, attrViewer); //the attr. Viewer lies over all other viewers, but not in stack Pane (because it would fill the whole screen :-(

    setOnScroll(new EventHandler<ScrollEvent>() {
      @Override
      public void handle(ScrollEvent scrollEvent) {
        if (!scrollEvent.isControlDown() && !scrollEvent.isAltDown() && !scrollEvent.isShiftDown()) {
          if (scrollEvent.getDeltaY() > 0)
            showPreviousMedia();
          else
            showNextMedia();
          scrollEvent.consume();
        }
      }
    });

    setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        if (event.getClickCount() > 1) { //if double clicked
          toggleFullScreenAndNormal();
        }
      }
    });

    setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        boolean eventHandled = true;

        switch (keyEvent.getCode()) {
          //moving in fileTableView
          case HOME:
            showMedia(1);
            break;
          case END:
            showMedia(Integer.MAX_VALUE);
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
            toggleFullScreenAndNormal();
            break;
          case TAB:
            if (keyEvent.isShiftDown())
              showFullScreenOnNextScreen(false); //shift-Tab = previous screen
            else
              showFullScreenOnNextScreen(true); //tab = next screen
            break;
          case ESCAPE:
            endFullScreen();
            break;
          case DELETE:    //also with ctrl-delete: close the window, not only end full screen (see "workaround" in this class)
            if (keyEvent.isControlDown()) endFullScreen();
            break;

          //attributes viewer control (toggle) (note: shortcuts are described in context menu --> don't forget to keep consistent!!!)
          case D:
            if (keyEvent.isControlDown()) {
              attrViewer.setVisible(!attrViewer.isVisible());
              break;
            }
          case P:
            if (keyEvent.isControlDown()) {
              attrViewer.setDisplayPrefix(!attrViewer.isDisplayPrefix());
              break;
            }
          case C:
            if (keyEvent.isControlDown()) {
              attrViewer.setDisplayCounter(!attrViewer.isDisplayCounter());
              break;
            }
          case E:
            if (keyEvent.isControlDown()) {
              attrViewer.setDisplayExtension(!attrViewer.isDisplayExtension());
              break;
            }
          case F:
            if (keyEvent.isControlDown()) {
              attrViewer.setDisplayFileDate(!attrViewer.isDisplayFileDate());
              break;
            }

          default:
            eventHandled = false;
        }
        if (!eventHandled) {
          eventHandled = true;
          switch (keyEvent.getText()) {
            //collecting/selecting line number
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
              collectDigit(Integer.parseInt(keyEvent.getText()));

              break;
            default:
              eventHandled = false;
          }
        }

        if (eventHandled) keyEvent.consume();
      }
    });
  }

  /**
   * call this before setting mediaContentView to null (e.g. when ending full screen mode)
   * e.g. to cleanUp all playerViewers
   */
  public void cleanUp() {
    if (playerViewer != null) playerViewer.cleanUp();
  }

  /**
   * perform unsaved/planned transformation as a preview by rotating/flipping the mediaStackPane
   * if the currentMediaFile == null then nothing happens
   */
  public void showRotationAndFlippingPreview() {
    if (currentMediaFile == null) return;

    switch (currentMediaFile.getRotateOperation()) {
      case ROTATE0:
        mediaStackPane.setRotate(0);
        break;

      case ROTATE90:
        mediaStackPane.setRotate(90);
        break;

      case ROTATE180:
        mediaStackPane.setRotate(180);
        break;

      case ROTATE270:
        mediaStackPane.setRotate(270);
        break;
    }

    //finally execute flipping
    if (currentMediaFile.isFlippedHorizontally()) mediaStackPane.setScaleX(-1);
    else mediaStackPane.setScaleX(1);

    if (currentMediaFile.isFlippedVertically()) mediaStackPane.setScaleY(-1);
    else mediaStackPane.setScaleY(1);

    //maintain the fullScreenStage's media also, if it is displayed currently
    if (fullScreenStage != null && fullScreenStage.isShowing()) {
      fullScreenStage.getMediaContentView().showRotationAndFlippingPreview();
    }

  }

  /**
   * mainContentView: there is no FullScreen Window or this is the FullScreen Window
   * secondaryView: there is a FullScreen Window but this is not the FullScreen Window
   *
   * @return true, if this is a main ContentView, false if secondary
   */
  public boolean isMainMediaContentView() {
    return (fullScreenStage == null);
  }

  /**
   * fullScreenStage has no fullscreen stage again but a primaryMediaContentView
   * fullScreenStage is the mainContentView if it exists
   * if no fullScreenStage exists the primaryMediaContentView is the main ContentView but not a fullScreen Stage
   *
   * @return true if this contentView is in a fullScreenStage
   */
  public boolean isFullScreenMediaContentView() {
    return (primaryMediaContentView != null);
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
    if (compatibleViewer == null) {
      activateEmptyMediaViewer();
    }
    //----- Photo
    else if (compatibleViewer instanceof PhotoViewer) {
      photoViewer.setMediaFileIfCompatible(mediaFile);
      activatePhotoViewer();
    }
    //----- Playable File (Video or Audio) only shown in main Window
    else if (compatibleViewer instanceof PlayerViewer) {
      activatePlayerOnOtherScreenHint();
      //----- Unsupported Media
    } else {
      activateOtherMediaViewer();
    }
  }

  /**
   * set MediaFile to be shown<br>
   * <br>
   * If the mediaFile is already showing nothing happens to suppress multiple calls due to events while building the GUI during start-up<br>
   * <br>
   * If this is the main mediaContentView (i.e. the only one or the fullscreen) then
   * find the suitable viewer by trying one after the other until a suitable viewer is found.
   * Note: at least the "OtherViewer" will fits in the end
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
          if (this.isFullScreenMediaContentView()) primaryMediaContentView.syncMediaContentViews(mediaFile, null);

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
        e.printStackTrace();
      }
    } else {

      //-----if this was not the main ContentView forward the call to the main ContentView (fullScreenStage exists)
      fullScreenStage.getMediaContentView().setMedia(mediaFile, lastPlayerPos);
    }

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
    photoViewer.setVisible(false);
    playerViewer.setVisible(true);
    otherViewer.setVisible(false);

    playerViewer.zoomToFit();
    showRotationAndFlippingPreview();
  }

  private void activatePhotoViewer() {
    photoViewer.setVisible(true);
    playerViewer.setVisible(false);
    otherViewer.setVisible(false);

    photoViewer.zoomToFit();
    showRotationAndFlippingPreview();
    stopAllActivePlayers();
  }

  private void activateEmptyMediaViewer() {
    activateOtherMediaViewer(false, language.getString("nothing.to.show"));
  }

  /**
   * If VLC is not currently used a hint is added, that VLC should be installed to support more media file formats
   */
  public void activateOtherMediaViewer() {
    if (playerViewer instanceof MovieViewerVLCJ)
      activateOtherMediaViewer(true, "");   //no vlc installtion hint
    else
      //if vlc is not installed than add a hint
      activateOtherMediaViewer(true, language.getString("to.support.more.file.formats.install.the.free.vlc.player.from.videolan.on.your.system.kissphoto.will.detect.and.use.it"));
  }

  /**
   * show otherViewer, hide all others
   *
   * @param mainMessageVisible true, if the main message (not supported media) should be shown
   * @param additionalMessage  extra information in a second line, or nothing if "" or null is provided
   */
  public void activateOtherMediaViewer(boolean mainMessageVisible, String additionalMessage) {
    otherViewer.setMainMessageVisable(mainMessageVisible);
    otherViewer.setAdditionalMessage(additionalMessage);

    photoViewer.setVisible(false);
    playerViewer.setVisible(false);
    otherViewer.setVisible(true);
    stopAllActivePlayers();
  }

  public void activatePlayerOnOtherScreenHint() {
    otherViewer.setMainMessageVisable(false);
    otherViewer.setAdditionalMessage(language.getString("media.file.is.being.played.in.fullscreen.window"));

    photoViewer.setVisible(false);
    playerViewer.setVisible(false);
    otherViewer.setVisible(true);

  }

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
   * @param mediaFile
   */
  private void showProgressBarForMediaFile(MediaFile mediaFile) {
    if (mediaFile != null) {
      if (fileTableView != null && mediaFile.getContentProgressProperty() != null) {  //fileTableView=null for UndeleteDialog
        //show Progressbar only if media not already completely loaded
        if (mediaFile.getContentProgressProperty().doubleValue() < 1.0) {
          StatusBar statusBar = fileTableView.getStatusBar();

          statusBar.showMessage(MessageFormat.format(language.getString("loading.0"), mediaFile.getResultingFilename()));
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
    if (fileTableView == null || fileTableView.getSelectionModel() == null) return;

    boolean fileTableViewHadFocus = fileTableView.isFocused();

    int currentSelection = fileTableView.getSelectionModel().getSelectedIndex();
    if (currentSelection > 0) {
      fileTableView.getSelectionModel().clearAndSelect(currentSelection - 1);
      fileTableView.scrollViewportToIndex(currentSelection - 1, FileTableView.Alignment.TOP);
    }

    //above selection might steel focus from mediaContentView if changed from movie to image
    if (!fileTableViewHadFocus) Platform.runLater(new Runnable() {
      @Override
      public void run() {
        playerViewer.requestFocus();   //whatever is visible gets the focus back
        otherViewer.requestFocus();
        photoViewer.requestFocus();
      }
    });
  }

  /**
   * select next line in fileTableView
   * the selectionChangeListener there will load the next media then
   * (if there is no current selection (e.g. empty filelist) or no connection to the fileTableView (e.g. in undeleteDialog) nothing will happen)
   *
   * @return true if skipped to next Media, false if already at the end of the list
   */
  public boolean showNextMedia() {
    boolean skipped = false;

    if (fileTableView != null && fileTableView.getSelectionModel() != null) {

      boolean fileTableViewHadFocus = fileTableView.isFocused();

      int currentSelection = fileTableView.getSelectionModel().getSelectedIndex();
      if (currentSelection < fileTableView.getMediaFileList().getFileList().size() - 1) { //if not already at the end of the fileTableView's list
        fileTableView.getSelectionModel().clearAndSelect(currentSelection + 1);
        fileTableView.scrollViewportToIndex(currentSelection + 1, FileTableView.Alignment.BOTTOM);
        skipped = true;
      }

      //above selection might steel focus from mediaContentView if changed from movie to image
      if (!fileTableViewHadFocus) Platform.runLater(() -> {
        //whatever is visible --> get the focus back
        requestFocus();
//        if (playerViewer.isVisible()) playerViewer.requestFocus();
//        else if (otherViewer.isVisible()) otherViewer.requestFocus();
//        else if (photoViewer.isVisible()) photoViewer.requestFocus();
      });
    }
    return skipped;
  }

  /**
   * if content is in Fullscreen mode
   * then the underlying stage is moved to the next screen
   * if only one screen is available the window is moved to the primary screen
   * (this might be useful if multiscreen support was switched of while the content was displayed on a secondary screen)
   *
   * @param next if true next, if false previous screen is selected
   */
  public void showFullScreenOnNextScreen(boolean next) {
    if (owner.isFullScreen()) {
      ObservableList<Screen> screens = Screen.getScreens();

      //only if multiple screens are available
      if (screens != null && screens.size() > 1) {
        ObservableList<Screen> currentScreens = Screen.getScreensForRectangle(owner.getX(), owner.getY(), owner.getWidth(), owner.getHeight());

        Screen current;
        if (currentScreens != null && currentScreens.size() > 0)
          current = currentScreens.get(0);
        else
          current = Screen.getPrimary();

        int currentIndex = screens.indexOf(current);
        if (currentIndex >= 0) {
          if (next) {
            currentIndex++; //select next
            if (currentIndex >= screens.size()) currentIndex = 0; //make it a loop
          } else {
            currentIndex--; //select previous
            if (currentIndex < 0) currentIndex = screens.size() - 1; //make it a loop
          }

          //move to new Screen
          current = screens.get(currentIndex);  //select new currentIndex ;-)
          //owner.setFullScreen(false); //so that the bounds can be changed
          owner.setX(current.getBounds().getMinX());
          owner.setY(current.getBounds().getMinY());
          owner.setWidth(current.getBounds().getWidth());
          owner.setHeight(current.getBounds().getHeight());
          //owner.setFullScreen(true);
        }
      }
    } else if (fullScreenStage != null) {      //if this method was called in the main mediaContentView then try to forward it to the fullScreen-stage
      fullScreenStage.mediaContentView.showFullScreenOnNextScreen(next);
    }
  }

  /**
   * jump to a line number in fileTableView
   * if the line number is smaller than 0 the first line is selected
   * if the line number is greater than the length of fileTableView the last element is selected
   *
   * @param lineNumber the line to jump to (one based, i.e. 1 jumps to first line (which has internal index 0))
   */
  public void showMedia(int lineNumber) {
    if (fileTableView == null || fileTableView.getSelectionModel() == null) return;

    if (lineNumber <= 0) {
      fileTableView.getSelectionModel().clearAndSelect(0); //first element
      fileTableView.scrollViewportToIndex(0, FileTableView.Alignment.TOP);
    } else if (lineNumber < fileTableView.getMediaFileList().getFileList().size()) {
      fileTableView.getSelectionModel().clearAndSelect(lineNumber - 1); //-1 because list is zero-based human entry is 1-based
      fileTableView.scrollViewportToIndex(lineNumber - 1, FileTableView.Alignment.CENTER);
    } else {
      fileTableView.getSelectionModel().clearAndSelect(fileTableView.getMediaFileList().getFileList().size() - 1); //last element
      fileTableView.scrollViewportToIndex(fileTableView.getMediaFileList().getFileList().size() - 1, FileTableView.Alignment.BOTTOM);
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
    else //new digit for a existing entry
      enteredLineNumber = enteredLineNumber * 10 + digit;
  }

  /**
   * show the media in the line number of fileTableView that has been entered previously by the user
   * and that has been collected using collectDigit()
   * if no number has been entered before (i.e. enteredLineNumber<0) then nothing will happen
   */
  private void showMediaForEnteredLineNumber() {
    if (enteredLineNumber > 0) { //only if a valid number has been entered before
      showMedia(enteredLineNumber);
      enteredLineNumber = -1;  //consume the number
    }
  }

  /**
   * open an additional MediaContentView in a full screen stage (window).
   * Any Players will be stopped in main window, but the position is handed over into full screen
   */
  public void showFullScreen() {
    if (!isFullScreenMediaContentView() && fullScreenStage == null) { //only if not already in fullScreen-Mode

      Duration currentPlayerPosition = null; //only used if player was active to hand over the position to full-screen stage

      if (playerViewer.isVisible()) {
        currentPlayerPosition = playerViewer.getCurrentTime();
        playerViewer.resetPlayer(); //stop playback if running, because it will be started full screen
      }

      fullScreenStage = new FullScreenStage(this, currentPlayerPosition); //use a new mediaContentView for fullScreen and link to "this" =normal view
      fullScreenStage.mediaContentView.getAttrViewer().copyState(attrViewer); //synchronize normal and full screen attributesViewers

      //enabling/disabling of fullScreen items in all context menus (of all viewers)
      for (MenuItem item : fullScreenItems) {
        item.setText(language.getString("end.full.screen.mode"));
        //item.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));  //old accelerator (F5) cannot be overwritten, therefore F5 always remains to be the visible accelerator (and also works additionally to ESC)
      }
      for (MenuItem item : showOnNextScreenItems) item.setDisable(false);

      fullScreenStage.show();
    }
  }

  public void endFullScreen() {
    Duration fullScreenPlayerPosition = null; //only used if player was active to hand over the position to full-screen stage

    //only if in fullScreen-Mode (seen from the main window's view)
    if (fullScreenStage != null) {
      attrViewer.copyState(fullScreenStage.mediaContentView.getAttrViewer()); //synchronize normal and full screen attributesViewers
      if (fullScreenStage.mediaContentView.getPlayerViewer() != null && fullScreenStage.mediaContentView.getPlayerViewer().isVisible()) {
        fullScreenPlayerPosition = fullScreenStage.getMediaContentView().getPlayerViewer().getCurrentTime();
        fullScreenStage.mediaContentView.getPlayerViewer().resetPlayer();
      }
      fullScreenStage.close();
      fullScreenStage.getMediaContentView().cleanUp();
      fullScreenStage = null;

      MediaFile mediaFile = currentMediaFile; //save it
      currentMediaFile = null; //reset it, so that setMedia will have an effect, otherwise re-setting it would be prevented
      setMedia(mediaFile, fullScreenPlayerPosition); //sync normal view with previous full screen view, restore currentMediaFile

      //enabling/disabling of fullScreen items in all context menus (of all viewers)
      for (MenuItem item : fullScreenItems) {
        item.setText(language.getString("full.screen"));
        //item.setAccelerator(new KeyCodeCombination(KeyCode.F5)); //old accelerator (ESC) cannot be overwritten, therefore F5 always remains to be the visible accelerator (and also works additionally to ESC)
      }
      for (MenuItem item : showOnNextScreenItems) item.setDisable(true);

      if (fileTableView != null) fileTableView.requestFocus();    //null if in UnDeleteDialog
    }
    //if in fullScreenMode (seen from the full-Screen-window itself)
    if (primaryMediaContentView != null) {
      primaryMediaContentView.endFullScreen();
    }
  }

  public void toggleFullScreenAndNormal() {
    if (fullScreenStage == null && !owner.isFullScreen()) { //only if not already in fullScreen-Mode
      showFullScreen();
      showFullScreenOnNextScreen(true); //if multiple screens are available use the "next" initially
      //note: fileTableView = null if in UnDeleteDialog (i.e. no progressBar)
      if (fileTableView != null)
        fileTableView.getStatusBar().showMessage(language.getString("esc.to.end.full.screen.tab.to.shift.full.screen.panel.between.screens"));
    } else {
      endFullScreen();
      if (fileTableView != null) fileTableView.getStatusBar().clearMessage();
    }
  }

  /**
   * (main menu) add a fullScreenItem to the list of MenuItems that need to be
   * enabled/disabled when fullscreen mode is activated/deactivated
   *
   * @param item to be added to the list
   */
  public void addToFullScreenItems(MenuItem item) {
    fullScreenItems.add(item);
  }

  /**
   * (main menu) add a showOnNextScreenItem to the list of MenuItems that need to be
   * enabled/disabled when fullscreen mode is activated/deactivated
   *
   * @param item to be added to the list
   */
  public void addToShowOnNextScreenItems(MenuItem item) {
    showOnNextScreenItems.add(item);
  }

  /**
   * the viewers can add the navigation menu items of mediaContentView to the end of their context-menu
   * by calling this method
   *
   * @param contextMenu the menu where the menuitems shall be added to
   */
  public void addContextMenuItems(final ContextMenu contextMenu) {

    //----- select media file from list
    MenuItem nextItem = new MenuItem(language.getString("next.scroll.mouse.wheel.down")); //PgDn  or ENTER
    nextItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_DOWN));
    nextItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showNextMedia();
        actionEvent.consume();
      }
    });

    MenuItem previousItem = new MenuItem(language.getString("previous.scroll.mouse.wheel.up")); //PgUp
    previousItem.setAccelerator(new KeyCodeCombination(KeyCode.PAGE_UP));
    previousItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showPreviousMedia();
        actionEvent.consume();
      }
    });

    MenuItem homeItem = new MenuItem(language.getString("first")); //home
    homeItem.setAccelerator(new KeyCodeCombination(KeyCode.HOME));
    homeItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showMedia(1);
        actionEvent.consume();
      }
    });

    MenuItem endItem = new MenuItem(language.getString("last")); //end
    endItem.setAccelerator(new KeyCodeCombination(KeyCode.END));
    endItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showMedia(Integer.MAX_VALUE);
        actionEvent.consume();
      }
    });

    MenuItem gotoItem = new MenuItem(language.getString("goto.number.type.digits.then.enter")); //ENTER
    gotoItem.setAccelerator(new KeyCodeCombination(KeyCode.ENTER));
    gotoItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showMediaForEnteredLineNumber();
        actionEvent.consume();
      }
    });
    contextMenu.getItems().addAll(nextItem, previousItem, homeItem, endItem, gotoItem);

    //-------- View
    MenuItem fullScreenItem = new MenuItem(language.getString("full.screen"));
    fullScreenItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
    fullScreenItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        toggleFullScreenAndNormal();
        actionEvent.consume();
      }
    });
    fullScreenItems.add(fullScreenItem);

    MenuItem showOnNextScreenItem = new MenuItem(language.getString(SHOW_ON_NEXT_SCREEN_FULLSCREEN));
    showOnNextScreenItem.setAccelerator((new KeyCodeCombination(KeyCode.TAB))); //TAB, previous shift-Tab is not shown in menu
    showOnNextScreenItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFullScreenOnNextScreen(true);
      }
    });
    showOnNextScreenItem.setDisable(true); //enable only in Full screen mode
    showOnNextScreenItems.add(showOnNextScreenItem);

    contextMenu.getItems().addAll(new SeparatorMenuItem(), fullScreenItem, showOnNextScreenItem);

//-------- Attributes
    final CheckMenuItem showAttrItem = new CheckMenuItem(language.getString("display.description"));
    showAttrItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
    showAttrItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        attrViewer.setVisible(showAttrItem.isSelected());
      }
    });
//--------
    final CheckMenuItem showPrefixItem = new CheckMenuItem(language.getString("show.prefix"));
    showPrefixItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
    showPrefixItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        attrViewer.setDisplayPrefix(showPrefixItem.isSelected());
      }
    });

    final CheckMenuItem showCounterItem = new CheckMenuItem(language.getString("show.counter"));
    showCounterItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
    showCounterItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        attrViewer.setDisplayCounter(showCounterItem.isSelected());
      }
    });
    final CheckMenuItem showExtensionItem = new CheckMenuItem(language.getString("show.extension"));
    showExtensionItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
    showExtensionItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        attrViewer.setDisplayExtension(showExtensionItem.isSelected());
      }
    });
    final CheckMenuItem showFileDateItem = new CheckMenuItem(language.getString("show.file.date"));
    showFileDateItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
    showFileDateItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        attrViewer.setDisplayFileDate(showFileDateItem.isSelected());
      }
    });

    contextMenu.getItems().addAll(new SeparatorMenuItem(), showAttrItem, showPrefixItem, showCounterItem,
      showExtensionItem, showFileDateItem);

    contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent windowEvent) {
        showAttrItem.setSelected(attrViewer.isVisible());

        showPrefixItem.setSelected(attrViewer.isDisplayPrefix());
        showPrefixItem.setDisable(!attrViewer.isVisible());

        showCounterItem.setSelected(attrViewer.isDisplayCounter());
        showCounterItem.setDisable(!attrViewer.isVisible());

        showExtensionItem.setSelected(attrViewer.isDisplayExtension());
        showExtensionItem.setDisable(!attrViewer.isVisible());

        showFileDateItem.setSelected(attrViewer.isDisplayFileDate());
        showFileDateItem.setDisable(!attrViewer.isVisible());
      }
    });
  }

  /**
   * short for accessing fileTableView.isEditMode e.g. from playerViewer
   *
   * @return true if fileTableView is currently in CellEdit-Mode or Multi-edit-Mode
   */
  public boolean isFileTableViewInEditMode() {
    if (fileTableView != null) //is null in UndeleteDialog
      return fileTableView.isEditMode();
    else
      return false;
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

  public Stage getOwner() {
    return owner;
  }

  public FileTableView getFileTableView() {
    return fileTableView;
  }

  public void setFileTableView(FileTableView fileTableView) {

    this.fileTableView = fileTableView;
  }

  public MediaFile getCurrentMediaFile() {
    return currentMediaFile;
  }

  /**
   * ProgressChangeListner cannot be an anonymous class because it has to be rememered where it has been activated
   * so that it can be stopped before the next media is loaded an connected to the progress bar
   * see showProgressBar() and clearProgress()
   */
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
}
