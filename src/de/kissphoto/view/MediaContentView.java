package de.kissphoto.view;

import de.kissphoto.helper.I18Support;
import de.kissphoto.model.ImageFile;
import de.kissphoto.model.ImageFileRotater;
import de.kissphoto.model.MediaFile;
import de.kissphoto.model.MediaFileRotater;
import de.kissphoto.view.helper.RotatablePaneLayouter;
import de.kissphoto.view.mediaViewers.AttributesViewer;
import de.kissphoto.view.mediaViewers.MovieViewer;
import de.kissphoto.view.mediaViewers.OtherViewer;
import de.kissphoto.view.mediaViewers.PhotoViewer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * This is the Pane where visible Media (photo, video) is shown
 * It is a JavaFX-Pane which contains an ImageView and a MediaView
 * <p/>
 *
 * @Author: Dr. Ingo Kreuz
 * @Date: 2012-09-10
 * @modified: 2014-05-02 (I18Support)
 * @modified: 2014-05-29 Full-Screen-Mode, and moving in fileTableView (up/down/jump to digit) added (keyboard only)
 * @modified: 2014-06-01 Mouse-Support and Context Menu-Support here and in PhotoViewer
 * @modified: 2014-06-02 attributesViewer: show description (etc) as overlay added, full screen mode an context menu made compatible
 * moving around now keeps the focused line in FileView visible
 * @modified: 2014-06-09 moviePlayer support added
 * @modified: 2016-11-06 FullScreenStage moved to separate class (was inner class before)
 * @modified: 2017-10-08 all viewers' size is bound now to MediaContentView's size, so that zooming works correctly.
 * zooming is resetted now whenever the media changes (handing over zooming is "too clever" i.e. not "KISS"
 * Double Click switches Full-Screen Mode
 * @modified: 2018-10-21 Support rotation of media >in principle< i.e. if currentMediaFile.canRotate() by rotating mediaStackPane
 */
public class MediaContentView extends Pane {
  public static final String SHOW_ON_NEXT_SCREEN_FULLSCREEN = "show.on.next.screen.fullscreen";
  private static ResourceBundle language = I18Support.languageBundle;
  private int enteredLineNumber = -1; //negative means: nothing entered

  private FileTableView fileTableView = null; //optional link to the fileTableView connected with this MediaContentView (for selecting pictures while focus is in MediaContentView
  private FullScreenStage fullScreenStage = null; //this stage is used (!=null) if showFullScreen() has been called
  private Stage owner = null; //normal mediaContentView is in the primary stage, full-Screen MediaContentView is in a dialog-stage which is in fullScreenMode
  private MediaContentView primaryMediaContentView = null; //in full screen mode only: link to the mediaContentView of the primary stage

  private Object currentMedia = null; //the content from the cache
  private MediaFile currentMediaFile = null; //the according mediaFile for attributes

  private PhotoViewer photoViewer;
  private MovieViewer movieViewer;
  private OtherViewer otherViewer;
  //private AudioViewer...      //todo
  private AttributesViewer attrViewer = new AttributesViewer(this);

  StackPane mediaStackPane = new StackPane(); //the viewers lie one above the other, so fading will (in future) be possible even from photo to video clip ...

  //every viewer has these items. For enabling/disabling keep a reference to all of them
  static private ObservableList<MenuItem> showOnNextScreenItems = FXCollections.observableArrayList();
  static private ObservableList<MenuItem> fullScreenItems = FXCollections.observableArrayList();

  ImageFileRotater.RotateOperation currentRotation = MediaFileRotater.RotateOperation.ROTATE0;


  /**
   * @constructor
   */
  public MediaContentView(Stage owner) {
    super();
    init(owner);
  }

  /**
   * @param owner                   link to the stage which contains this mediaContentView
   * @param primaryMediaContentView link to the primaryMediaContentView (i.e. from full screen to "normal view"
   * @constructor this constructor is only used internally for linking back the full-Screen version to the primary MediaContentView (in primary Stage)
   */
  protected MediaContentView(Stage owner, MediaContentView primaryMediaContentView) {
    super();
    init(owner);
    this.primaryMediaContentView = primaryMediaContentView;
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
    movieViewer = new MovieViewer(this);
    movieViewer.prefWidthProperty().bind(mediaStackPane.widthProperty());
    movieViewer.prefHeightProperty().bind(mediaStackPane.heightProperty());
    otherViewer = new OtherViewer(this);
    mediaStackPane.getChildren().addAll(photoViewer, movieViewer, otherViewer);

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

    movieViewer.cleanUp();
  }

  public void setFileTableView(FileTableView fileTableView) {

    this.fileTableView = fileTableView;
  }


  /**
   * perform unsaved/planned transformation as a preview by rotating/flipping the mediaStackPane
   */
  public void showRotationAndFlippingPreview() {
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
  }

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
  public void setMedia(MediaFile mediaFile, Duration lastPlayerPos) {
    //reset all probably playing players
    movieViewer.resetPlayer();

    if (mediaFile != null) {  //if nothing is handed over then display standard view ("sorry cannot display"...) e.g. from init of undelete dialog
      currentMedia = mediaFile.getCachedMediaContent();
      if (currentMedia == null) currentMedia = mediaFile.getMediaContent();  //in undelete dialog don't use cache!

      currentMediaFile = mediaFile;
      attrViewer.setMedia(mediaFile);

      if (currentMedia != null) {
        if (currentMedia.getClass() == Image.class) {
          //-------- if photo ---------
          photoViewer.setImageFile((ImageFile) mediaFile);
          photoViewer.setVisible(true);
          movieViewer.setVisible(false);
          otherViewer.setVisible(false);


          photoViewer.zoomToFit();
        } else if (currentMedia.getClass() == Media.class) {
          //--------if movie -----------
          if (fullScreenStage == null) //only show film in foreground window (here if there is no fullscreen stage or in fullscreen stage)
            movieViewer.setMedia((Media) currentMedia, lastPlayerPos);
          photoViewer.setVisible(false);
          movieViewer.setVisible(true);
          otherViewer.setVisible(false);

          movieViewer.zoomToFit();
        } else {
          //------- if unsupported Media type -------
          photoViewer.setVisible(false);
          movieViewer.setVisible(false);
          otherViewer.setVisible(true);
        }
      } else {
        //current Media is null
        photoViewer.setVisible(false);
        movieViewer.setVisible(false);
        otherViewer.setVisible(true);
      }
    } else {
      //mediaFile is null
      photoViewer.setVisible(false);
      movieViewer.setVisible(false);
      otherViewer.setVisible(true);
    }

    showRotationAndFlippingPreview();

    //maintain the fullScreenStage's media also, if it is displayed currently
    if (fullScreenStage != null && fullScreenStage.isShowing()) {
      fullScreenStage.getMediaContentView().setMedia(mediaFile, lastPlayerPos);
    }
  }

  /**
   * select previous line in fileTableView
   * the selectionChangeListener there will load the previous media then
   * (if there is no current selection (e.g. empty filelist) or no connection to the fileTableView (e.g. in undeleteDialog) nothing will happen)
   */
  public void showPreviousMedia() {
    boolean fileTableViewHadFocus = fileTableView.isFocused();

    if (fileTableView == null || fileTableView.getSelectionModel() == null) return;

    int currentSelection = fileTableView.getSelectionModel().getSelectedIndex();
    if (currentSelection > 0) {
      fileTableView.getSelectionModel().clearAndSelect(currentSelection - 1);
      fileTableView.scrollViewportToIndex(currentSelection - 1);
    }

    //above selection might steel focus from mediaContentView if changed from movie to image
    if (!fileTableViewHadFocus) Platform.runLater(new Runnable() {
      @Override
      public void run() {
        movieViewer.requestFocus();   //whatever is visible gets the focus back
        otherViewer.requestFocus();
        photoViewer.requestFocus();
      }
    });
  }

  /**
   * select next line in fileTableView
   * the selectionChangeListener there will load the next media then
   * (if there is no current selection (e.g. empty filelist) or no connection to the fileTableView (e.g. in undeleteDialog) nothing will happen)
   */
  public void showNextMedia() {
    boolean fileTableViewHadFocus = fileTableView.isFocused();
    if (fileTableView == null || fileTableView.getSelectionModel() == null) return;

    int currentSelection = fileTableView.getSelectionModel().getSelectedIndex();
    if (currentSelection < fileTableView.getMediaFileList().getFileList().size() - 1) {
      fileTableView.getSelectionModel().clearAndSelect(currentSelection + 1);
      fileTableView.scrollViewportToIndex(currentSelection + 1);
    }

    //above selection might steel focus from mediaContentView if changed from movie to image
    if (!fileTableViewHadFocus) Platform.runLater(new Runnable() {
      @Override
      public void run() {
        movieViewer.requestFocus();   //whatever is visible gets the focus back
        otherViewer.requestFocus();
        photoViewer.requestFocus();
      }
    });
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
      fileTableView.scrollViewportToIndex(0);
    } else if (lineNumber < fileTableView.getMediaFileList().getFileList().size()) {
      fileTableView.getSelectionModel().clearAndSelect(lineNumber - 1); //-1 because list is zero-based human entry is 1-based
      fileTableView.scrollViewportToIndex(lineNumber - 1);
    } else {
      fileTableView.getSelectionModel().clearAndSelect(fileTableView.getMediaFileList().getFileList().size() - 1); //last element
      fileTableView.scrollViewportToIndex(fileTableView.getMediaFileList().getFileList().size() - 1);
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
    Duration currentPlayerPosition = null; //only used if player was active to hand over the position to full-screen stage

    if (fullScreenStage == null && !owner.isFullScreen()) { //only if not already in fullScreen-Mode
      if (movieViewer.isVisible()) {
        currentPlayerPosition = movieViewer.getMediaPlayer().getCurrentTime();
        movieViewer.resetPlayer(); //stop playback if running, because it will be started full screen
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
    Duration currentPlayerPosition = null; //only used if player was active to hand over the position to full-screen stage

    //only if in fullScreen-Mode (seen from the main view)
    if (fullScreenStage != null) {
      attrViewer.copyState(fullScreenStage.mediaContentView.getAttrViewer()); //synchronize normal and full screen attributesViewers
      if (fullScreenStage.mediaContentView.getMovieViewer().isVisible()) {
        currentPlayerPosition = fullScreenStage.getMediaContentView().getMovieViewer().getMediaPlayer().getCurrentTime();
        fullScreenStage.mediaContentView.getMovieViewer().resetPlayer();
      }
      fullScreenStage.close();
      fullScreenStage.getMediaContentView().cleanUp();
      fullScreenStage = null;
      setMedia(currentMediaFile, currentPlayerPosition); //sync normal view with previous full screen view

      //enabling/disabling of fullScreen items in all context menus (of all viewers)
      for (MenuItem item : fullScreenItems) {
        item.setText(language.getString("full.screen"));
        //item.setAccelerator(new KeyCodeCombination(KeyCode.F5)); //old accelerator (ESC) cannot be overwritten, therefore F5 always remains to be the visible accelerator (and also works additionally to ESC)
      }
      for (MenuItem item : showOnNextScreenItems) item.setDisable(true);

      fileTableView.requestFocus();
    }
    //if in fullScreenMode (seen from the full-Screen-window itself)
    if (primaryMediaContentView != null) {
      primaryMediaContentView.endFullScreen();
    }
  }

  public void toggleFullScreenAndNormal() {
    if (fullScreenStage == null && !owner.isFullScreen()) { //only if not already in fullScreen-Mode
      showFullScreen();
    } else {
      endFullScreen();
    }
  }


  /**
   * force repaint by reseting the scene
   * This solves a repainting bug in JavaFx 1.8.05
   */
  private void repaint() {
    final Scene oldScene = owner.getScene();
    owner.setScene(null);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        owner.setScene(oldScene);
      }
    });
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
    return fileTableView.isEditMode();
  }


  public AttributesViewer getAttrViewer() {
    return attrViewer;
  }

  public MovieViewer getMovieViewer() {
    return movieViewer;
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

  public MediaFile getCurrentMediaFile() {
    return currentMediaFile;
  }

}
