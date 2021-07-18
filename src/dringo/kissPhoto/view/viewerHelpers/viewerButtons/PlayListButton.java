package dringo.kissPhoto.view.viewerHelpers.viewerButtons;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
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
 * @author Dringo
 * @since 2020-11-19
 * @version 2020-11-19 initial version
 */
public class PlayListButton extends Group {
  private final Rectangle buttonRect;
  private final Tooltip tooltip = new Tooltip();

  private final SimpleBooleanProperty playListMode = new SimpleBooleanProperty(true);

  /**
   * @param size  the width and height of the button
   */
  public PlayListButton(double size, Color backgroundColor, Color iconColor) {
    super();
    final double ICON_SIZE = size*4/5;

    buttonRect = new Rectangle(size, size, backgroundColor); //this rect is used as the clicking area for mouse control. It will be invisible (background-color)
    Node icon = createIcon(ICON_SIZE, iconColor);

    Line strikeThrough = new Line(0, 0, ICON_SIZE, ICON_SIZE);
    strikeThrough.setStrokeWidth(ICON_SIZE/13*2);
    strikeThrough.setStroke(iconColor);
    //center
    strikeThrough.setTranslateX((buttonRect.getWidth() - ICON_SIZE) / 2);
    strikeThrough.setTranslateY((buttonRect.getHeight() - ICON_SIZE) / 2);

    tooltip.setShowDelay(ViewerControlPanel.TOOLTIP_DELAY);
    Tooltip.install(buttonRect, tooltip);
    getChildren().addAll(buttonRect, icon, strikeThrough);

    strikeThrough.visibleProperty().bind(playListMode.not());  //strike through if not playListMode
    playListMode.addListener((observable, oldValue, newValue) -> setTooltipText());
    setPlayListMode(true);
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
    playIcon.getPoints().addAll(0.0, 0.0,
      0.0, 5*GRID,
      5*GRID, 5/2*GRID);
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
  public void setPlayListMode(boolean playListMode){
    this.playListMode.set(playListMode);
  }

  private void setTooltipText() {
    if (playListMode.get())
        tooltip.setText(KissPhoto.language.getString("playlist.mode.play.next.media.file.at.the.end.of.the.current.track"));
      else
        tooltip.setText(KissPhoto.language.getString("playlist.mode.off.play.only.current.media.file"));
  }

  public boolean isPlayListMode(){
    return playListMode.get();
  }

  //for binding with menues and fullscreen PlayerControl
  public SimpleBooleanProperty playListModeProperty(){return playListMode;}


  public double getWidth() {
    return buttonRect.getWidth();
  }

  public double getHeight() {
    return buttonRect.getHeight();
  }
}
