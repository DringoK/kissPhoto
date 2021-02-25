package dringo.kissPhoto.view.viewerHelpers;

import dringo.kissPhoto.view.mediaViewers.MediaViewer;
import dringo.kissPhoto.view.viewerHelpers.viewerButtons.BurgerMenuButton;
import dringo.kissPhoto.view.viewerHelpers.viewerButtons.FullScreenButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements the basics of the controls area of viewers
 * <ul>
 * <li>control area and option area</li>
 * <li>auto show/hide mechanism using ViewerControlsHiderThread: fades in if mouse moves over viewer Area</li>
 * <li>burger menu (identical to right click = opens context menu)</li>
 * <li>full screen button (identical to context menu item or F5)</li>
 * </ul>
 * <p/>
 * It is a transparent VBox which's size is bound to the viewer handed in the constructor<br>
 * <br>
 * <br>
 * Note: it will be added into PlayerViewers(StackPane) and therefore will be bound to the PlayerViewers size
 *
 * @author Dringo
 * @since 2020-12-13
 * @version 2020-12-13 Common Parts taken over from PlayerControls. Mouse is now also hidden if controlPanel is hidden
 */

public class ViewerControlPanel extends VBox {
  private BorderPane controlPane; //contains the BurgerMenuButton and the controlArea for all other control buttons
  protected HBox controlArea;

  private BorderPane optionPane; //contains the FullScreenButton and the optionArea for all other option buttons
  protected HBox optionArea;

  MediaViewer mediaViewer;

  private ViewerControlsHiderThread viewerControlsHiderThread;

  //color and padding for all panels
  public static final Color ICON_COLOR = new Color(1, 1, 1, .90);
  public static final Color BACKGROUND_COLOR = new Color(0, 0, 0, .55);
  private static final double PADDING = 12.0; //borders

  //control Panel sizes
  private static final double CONTROL_AREA_HEIGHT = 35.0;
  protected static final double BUTTON_SIZE = 30.0;  //width and height

  //option Panel sizes
  private static final double OPTION_AREA_HEIGHT = 20;
  protected static final double OPTIONS_SIZE = 17;

  //tooltip
  public static final Duration TOOLTIP_DELAY = Duration.millis(300);

  /**
   * create a viewer control
   * @param mediaViewer the viewer with the context menu to be enabled by the Burger Button (additional assumption: it is derived from Node)
   */
  public ViewerControlPanel(MediaViewer mediaViewer) {
    //setStyle("-fx-background-color: blue;");
    //setOpacity(0.5);
    this.mediaViewer = mediaViewer;
    setVisible(false); //visible only while hovering
    setAlignment(Pos.TOP_CENTER);

    createControlArea();
    createOptionArea();

    this.getChildren().addAll(controlPane, optionPane);

    //------------------- handle events ---------------------------------
    mediaViewer.setOnMouseMoved(mouseEvent -> {
      handleShowEvent();
      mouseEvent.consume();
    });
    //------- showing/hiding controlArea using

    setOnMouseMoved(mouseEvent -> {
      handleShowEvent();
      mouseEvent.consume();
    });

    //-- keep mouse cursor and controls on if over controlPane or optionPane, but not for the rest of the viewerControlPanel (which's size is same as the viewers size)
    controlPane.setOnMouseEntered(mouseEvent -> {
      setVisible(true);
      viewerControlsHiderThread.pause();

    });
    controlPane.setOnMouseExited(mouseEvent -> viewerControlsHiderThread.resume());

    optionPane.setOnMouseEntered(mouseEvent -> {
      setVisible(true);
      viewerControlsHiderThread.pause();

    });
    optionPane.setOnMouseExited(mouseEvent -> viewerControlsHiderThread.resume());



    //------------------ finally start the thread which will auto-hide this PlayerControls instance
    viewerControlsHiderThread = new ViewerControlsHiderThread(this);
    viewerControlsHiderThread.setShowTimeInMillis(500); //can be short because when hovering over controls hiding is supressed
  }

  //---------------- install Mouse-Move-Activation also in mediaViewer
  public void handleShowEvent() {
    resetThreadAndShow();
    mediaViewer.setCursor(Cursor.OPEN_HAND);
  }
  public void handleHideEvent(){
    setVisible(false);
    mediaViewer.setCursor(Cursor.NONE);
  }

  /**
   * build the "first" buttons line containing basic control buttons (e.g. in PlayerControls here the Play-Button is located.)
   * This basic version just implements the Burger-Button.
   * Extend this method in specialized Controls classes and add all additional specialized buttons into controlArea
   */
  void createControlArea() {
    controlPane = new BorderPane();
    controlPane.setStyle("-fx-background-color: black;");
    controlPane.setOpacity(0.5);
    controlPane.setPrefHeight(CONTROL_AREA_HEIGHT);
    controlPane.prefWidthProperty().bind(widthProperty());
    controlPane.setPadding(new Insets(0, PADDING, 0, PADDING)); //only left/right, because top/bottom regulated by Pos.CENTER

    BurgerMenuButton burgerMenuButton = new BurgerMenuButton(BUTTON_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    burgerMenuButton.setOnMouseClicked((mouseEvent) -> {
      mediaViewer.getContextMenu().show(mediaViewer, mouseEvent.getScreenX(), mouseEvent.getScreenY());
      mouseEvent.consume();
    });
    controlPane.setRight(burgerMenuButton);

    controlArea = new HBox();
    controlArea.setAlignment(Pos.CENTER_RIGHT);
    controlArea.prefWidthProperty().bind(widthProperty());
    controlPane.setCenter(controlArea);

    controlPane.setCursor(Cursor.DEFAULT);
  }

  /**
   * build the "second" buttons line containing option buttons (e.g. in PlayerControls here the repeatMode-Button is located.)
   * This basic version just implements the Fullscreen Button.
   * Extend this method in specialized Controls classes
   */
  protected void createOptionArea() {
    optionPane = new BorderPane();
    optionPane.setStyle("-fx-background-color: black;");
    optionPane.setOpacity(0.5);
    optionPane.setPrefHeight(OPTION_AREA_HEIGHT);
    optionPane.prefWidthProperty().bind(widthProperty());
    optionPane.setPadding(new Insets(0, PADDING, 0, PADDING)); //only left/right, because top/bottom regulated by Pos.CENTER

    FullScreenButton fullScreenButton = new FullScreenButton(OPTIONS_SIZE, BACKGROUND_COLOR, ICON_COLOR);
    fullScreenButton.fullScreenModeProperty().bind(mediaViewer.getMediaContentView().getIsFullScreenActiveProperty());
    fullScreenButton.setOnMouseClicked((mouseEvent) -> {
      mediaViewer.getMediaContentView().toggleFullScreenAndNormal();
      mouseEvent.consume();
    });
    optionPane.setRight(fullScreenButton);


    optionArea = new HBox();
    optionArea.setAlignment(Pos.CENTER);
    optionArea.prefWidthProperty().bind(widthProperty());
    optionArea.setSpacing(PADDING / 2);
    optionPane.setCenter(optionArea);

    optionPane.setCursor(Cursor.DEFAULT);
  }

  public void cleanUp() {
    viewerControlsHiderThread.endThread();
  }

  /**
   * show the PlayerControls for some time using the playerControlsHiderThread to hide it automatically after some time
   */
  public void resetThreadAndShow() {
    viewerControlsHiderThread.showPlayerControls();
  }

  /*
  public void hide() {
    playerControlsHiderThread.hidePlayerControlsImmediately();
  }
  */
}
