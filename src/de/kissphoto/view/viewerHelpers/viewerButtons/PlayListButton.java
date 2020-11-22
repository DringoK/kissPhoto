package de.kissphoto.view.viewerHelpers.viewerButtons;

import de.kissphoto.helper.I18Support;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements an Option Button for PlayList-Mode (3 horizontal Lines plus a triangle/Play-Icon)<br>
 * StrikeThrough line (diagonal line) can be switched on and off)<br>
 * all elements are in a Group<br>
 *<br>
 *   Note:
 *   <ul>
 *     <li>button content will be created in the size passed to constructor and not be changed when resizing the button</li>
 *     <li>for simplicity the icon is not implemented as the control's skin but simply drawn on the empty control area</li>
 *   </ul>
 *
 * @author Dr. Ingo Kreuz
 * @since 2020-11-19
 * @version 2020-11-19 initial version
 */
public class PlayListButton extends Group {
  private static ResourceBundle language = I18Support.languageBundle;

  private Rectangle buttonRect;
  private Node icon;
  private Line strikeThrough;
  private Tooltip tooltip = new Tooltip();

  private boolean buttonValue = true;

  /**
   * @param size  the width and height of the button
   */
  public PlayListButton(double size, Color backgroundColor, Color iconColor) {
    super();
    final double ICON_SIZE = size*4/5;

    buttonRect = new Rectangle(size, size, backgroundColor); //this rect is used as the clicking area for mouse control. It will be invisible (background-color)
    icon = createIcon(ICON_SIZE, iconColor);

    strikeThrough = new Line(0, 0, ICON_SIZE, ICON_SIZE);
    strikeThrough.setStrokeWidth(ICON_SIZE/13*2);
    strikeThrough.setStroke(iconColor);
    //center
    strikeThrough.setTranslateX((buttonRect.getWidth() - ICON_SIZE) / 2);
    strikeThrough.setTranslateY((buttonRect.getHeight() - ICON_SIZE) / 2);

    setButtonValue(true);

    Tooltip.install(buttonRect, tooltip);
    getChildren().addAll(buttonRect, icon, strikeThrough);
  }

  /**
   * the Burger Control is a group of three horizontal lines
   *
   * @param iconSize  the width of the two lines
   * @return the control as a Node
   */
  private Node createIcon(double iconSize, Color iconColor) {
    Group icon = new Group();
    final double GRID = iconSize/13;
    final double LINE_WIDTH = 2*GRID;

    Line topLine = new Line(0, 1*GRID, iconSize, 1*GRID);
    topLine.setStrokeWidth(LINE_WIDTH);
    topLine.setStroke(iconColor);

    Line middleLine = new Line(0, 5*GRID, iconSize, 5*GRID);
    middleLine.setStrokeWidth(LINE_WIDTH);
    middleLine.setStroke(iconColor);

    Line bottomLine = new Line(0, 9*GRID, iconSize/2, 9*GRID);
    bottomLine.setStrokeWidth(LINE_WIDTH);
    bottomLine.setStroke(iconColor);

    Polygon playIcon = new Polygon();
    //build it in the origin because it must start with 0.0, 0.0
    playIcon.getPoints().addAll(new Double[]{
      0.0, 0.0,
      0.0, 5*GRID,
      5*GRID, 5/2*GRID});
    playIcon.setTranslateX(8 * GRID);
    playIcon.setTranslateY(8 * GRID);

    playIcon.setFill(iconColor);

    icon.getChildren().addAll(topLine, middleLine, bottomLine, playIcon);

    //center icon
    icon.setTranslateX((buttonRect.getWidth() - iconSize) / 2);
    icon.setTranslateY((buttonRect.getHeight() - iconSize) / 2);

    return icon;
  }

  /**
   * control the strikeThrough being visible
   * and the tooltip text
   *
   * @param playListMode  true=play next title when finished a track, false=stop at the end of the track
   */
  public void setButtonValue(boolean playListMode){
    strikeThrough.setVisible(!playListMode);
    buttonValue = playListMode;
    if (playListMode)
        tooltip.setText(language.getString("playlist.mode.play.next.media.file.at.the.end.of.the.current.track"));
      else
        tooltip.setText(language.getString("playlist.mode.off.play.only.current.media.file"));

  }

  public boolean getButtonValue(){
    return buttonValue;
  }


  public double getWidth() {
    return buttonRect.getWidth();
  }

  public double getHeight() {
    return buttonRect.getHeight();
  }
}
