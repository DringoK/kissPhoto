package de.kissphoto.view.mediaViewers.helper;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a Button which can toggle between Pause (two lines) and Play (triangle)
 * it is a Group and consists of a rectangle and either the Play or the Pause caption
 *
 * @author Dr. Ingo Kreuz
 * @date 2014-07-31
 * @modified:
 */
public class PlayPauseButton extends Group {
  public static final Color CAPTION_COLOR = new Color(1, 1, 1, .90);
  public static final Color BACKGROUND_COLOR = new Color(0, 0, 0, .55);

  private boolean paused;

  private Rectangle playPauseButtonRect;
  private final Node playCaption;
  private final Node pauseCaption;


  /**
   * @param height the height of the button
   * @param width  the width of the button
   * @constructor a rounded rectangle as play or pause button
   */
  PlayPauseButton(PlayerControls playerControls, double width, double height) {
    super();

    // with rounded rect in it
    playPauseButtonRect = new Rectangle();
    playPauseButtonRect.setArcHeight(height / 4);     //rounded rectangle
    playPauseButtonRect.setArcWidth(height / 4);

    playPauseButtonRect.setWidth(width);
    playPauseButtonRect.setHeight(height);
    playPauseButtonRect.setFill(BACKGROUND_COLOR);

    //and two captions where only one is visible at a time
    playCaption = createPlayCaption(width * 0.66, height * 0.66);
    pauseCaption = createPauseCaption(width * 0.66, height * 0.66);
    getChildren().addAll(playPauseButtonRect, playCaption, pauseCaption);

    setPaused(false);
  }

  /**
   * the Pause Control is a group of two vertical lines
   *
   * @param height the height of the triangle (=the length of the left vertical line)
   * @param width  the width of the triangle (=the x - distance to the top pointing to the right
   * @return the control as a Node
   */
  private Node createPauseCaption(double width, double height) {
    // pause control
    final Group pauseCaption = new Group();

    final Line leftLine = new Line();
    final double LINE_WIDTH = width / 3;

    leftLine.setStartX(0);
    leftLine.setStartY(0);
    leftLine.setEndX(0);
    leftLine.setEndY(height);
    leftLine.setStrokeWidth(LINE_WIDTH);
    leftLine.setStroke(CAPTION_COLOR);

    final Line rightLine = new Line();
    rightLine.setStartX(width - LINE_WIDTH);
    rightLine.setStartY(0);
    rightLine.setEndX(width - LINE_WIDTH);
    rightLine.setEndY(height);
    rightLine.setStrokeWidth(LINE_WIDTH);
    rightLine.setStroke(CAPTION_COLOR);


    pauseCaption.getChildren().addAll(leftLine, rightLine);

    //center caption
    pauseCaption.setTranslateX((playPauseButtonRect.getWidth() - width) / 2);
    pauseCaption.setTranslateY((playPauseButtonRect.getHeight() - height) / 2);

    return pauseCaption;
  }

  /**
   * the Play Control is a triangle with its top pointing to the right
   *
   * @param height the height of the triangle (=the length of the left vertical line)
   * @param width  the width of the triangle (=the x - distance to the top pointing to the right
   * @return the control as a Node
   */
  private Node createPlayCaption(double width, double height) {
    // play control
    final Polygon playCaption = new Polygon();
    playCaption.getPoints().addAll(new Double[]{
        0.0, 0.0,
        0.0, height,
        width, (height / 2)});

    playCaption.setFill(CAPTION_COLOR);

    //center caption
    playCaption.setTranslateX((playPauseButtonRect.getWidth() - width) / 2);
    playCaption.setTranslateY((playPauseButtonRect.getHeight() - height) / 2);

    return playCaption;
  }

  public double getWidth() {
    return playPauseButtonRect.getWidth();
  }

  public double getHeight() {
    return playPauseButtonRect.getHeight();
  }

  /**
   * get the pause state of the button
   *
   * @return true if the state is "paused" and therefore the playCaption is visible
   * false if the state is "play" and therefore the pauseCaption is visible
   */
  public boolean isPaused() {
    return paused;
  }

  /**
   * get the pause state of the button
   *
   * @param paused true to set the state to "paused" and make the playCaption visible
   *               false to set the state to "play" and make the playCaption visible
   */
  public void setPaused(boolean paused) {
    this.paused = paused;
    playCaption.setVisible(paused);     //if player is paused then show the playCaption
    pauseCaption.setVisible(!paused);
  }

  public void togglePaused() {
    setPaused(!paused);
  }

}
