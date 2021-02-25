package dringo.kissPhoto.view.viewerHelpers.viewerButtons;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a Button which can toggle between Pause (two lines) and Play (triangle)
 * it is a Group and consists of a rectangle and the Play or the Pause ICON (only one is visible at a time)
 *
 * @author Dringo
 * @since 2014-07-31
 * @version 2020-11-15 tidy up ;-)
 * @version 2017-10-21 PlayPauseButton shape now synchronized to playerStatus in PlayerControls
 */
public class PlayPauseButton extends Group {

  private SimpleBooleanProperty paused = new SimpleBooleanProperty(false);

  private Rectangle buttonRect;
  private final Node playIcon;
  private final Node pauseIcon;


  /**
   * a rectangle as play or pause button
   * @param size  the width and hight of the button
   */
  public PlayPauseButton(double size, Color backgroundColor, Color iconColor) {
    super();

    //this rect is used as the clicking area for mouse control. It will be invisible (background-color)
    buttonRect = new Rectangle(size, size, backgroundColor);

    //and two icons where only one is visible at a time
    playIcon = createPlayIcon(size*2/3, iconColor);
    pauseIcon = createPauseIcon(size*2/3, iconColor);
    getChildren().addAll(buttonRect, playIcon, pauseIcon);

    setPaused(false);
  }

  /**
   * the Pause Control is a group of two vertical lines
   *
   * @param iconSize  the width and height of the two lines
   * @return the control as a Node
   */
  private Node createPauseIcon(double iconSize, Color iconColor) {
    // pause control
    final Group pauseIcon = new Group();
    final double LINE_WIDTH = iconSize / 3;

    Line leftLine = new Line();
    leftLine.setStartX(0);
    leftLine.setStartY(0);
    leftLine.setEndX(0);
    leftLine.setEndY(iconSize);
    leftLine.setStrokeWidth(LINE_WIDTH);
    leftLine.setStroke(iconColor);

    Line rightLine = new Line();
    rightLine.setStartX(iconSize - LINE_WIDTH);
    rightLine.setStartY(0);
    rightLine.setEndX(iconSize - LINE_WIDTH);
    rightLine.setEndY(iconSize);
    rightLine.setStrokeWidth(LINE_WIDTH);
    rightLine.setStroke(iconColor);


    pauseIcon.getChildren().addAll(leftLine, rightLine);

    //center Icon
    pauseIcon.setTranslateX((buttonRect.getWidth() - iconSize) / 2);
    pauseIcon.setTranslateY((buttonRect.getHeight() - iconSize) / 2);

    //bind to pausedProperty
    playIcon.visibleProperty().bind(paused);     //if player is paused then show the playIcon
    pauseIcon.visibleProperty().bind(paused.not());

    return pauseIcon;
  }

  /**
   * the Play Icon is a triangle (Polygon) with its top pointing to the right
   *
   * @param iconSize  the width and height of the triangle (=the x - distance to the top pointing to the right
   * @return the control as a Node
   */
  private Node createPlayIcon(double iconSize, Color iconColor) {
    // play control
    Polygon playIcon = new Polygon();
    playIcon.getPoints().addAll(0.0, 0.0,
      0.0, iconSize,
      iconSize, (iconSize / 2));

    playIcon.setFill(iconColor);

    //center icon
    playIcon.setTranslateX((buttonRect.getWidth() - iconSize) / 2);
    playIcon.setTranslateY((buttonRect.getHeight() - iconSize) / 2);

    return playIcon;
  }

  public double getWidth() {
    return buttonRect.getWidth();
  }

  public double getHeight() {
    return buttonRect.getHeight();
  }

  /**
   * get the pause state of the button
   *
   * @return true if the state is "paused" and therefore the playIcon is visible
   * false if the state is "play" and therefore the pauseIcon is visible
   */
  public boolean isPaused() {
    return paused.get();
  }

  /**
   * get the pause state of the button
   *
   * @param paused true to set the state to "paused" and make the playIcon visible
   *               false to set the state to "play" and make the playIcon visible
   */
  public void setPaused(boolean paused) {
    this.paused.set(paused);
  }

  //for binding with menues and fullscreen PlayerControl
  public SimpleBooleanProperty pausedProperty(){return paused;}

}
