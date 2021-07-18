package dringo.kissPhoto.view.viewerHelpers.viewerButtons;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements an Option Button for Repeat-Mode (two arrows in opposite direction)<br>
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
public class RepeatButton extends Group {
  private final Rectangle buttonRect;
  private final Tooltip tooltip = new Tooltip();

  private final SimpleBooleanProperty repeatMode = new SimpleBooleanProperty(true);
  private final SimpleBooleanProperty playListMode = new SimpleBooleanProperty(true); //to be bound to the PlayListBtn of the same PlayerControls for better ToolTip-Texts

  /**
   * @param size  the width and height of the button
   */
  public RepeatButton(double size, Color backgroundColor, Color iconColor) {
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

    strikeThrough.visibleProperty().bind(repeatMode.not()); //Strike Through if not repeatMode
    repeatMode.addListener((observable, oldValue, newValue) -> setTooltipText());
    playListMode.addListener((observable, oldValue, newValue) -> setTooltipText());

    setRepeatMode(true);

  }

  /**
   * the repeat Button icon is Control is a group of three horizontal lines
   *
   * @param iconSize  the width of the two lines
   * @return the control as a Node
   */
  private Node createIcon(double iconSize, Color iconColor) {
    Group icon = new Group();
     final double GRID = iconSize / 13;
     final double LINE_WIDTH = 2 * GRID;


     //top arrow
     Path topPath = new Path(
       new MoveTo(1 * GRID, 6 * GRID),
       new ArcTo(3 * GRID, 3 * GRID, 0.0, 4 * GRID, 3 * GRID, false, true),
       new LineTo(9 * GRID - (LINE_WIDTH / 2), 3 * GRID) //-LINE_WIDTH/2 to prevent overlapping with array: line will be longer than endpoint because of LINE_WIDTH
     );
     topPath.setStrokeWidth(LINE_WIDTH);
     topPath.setStroke(iconColor);

     Polygon topHead = new Polygon();  //arrowhead
     //build it in the origin because it must start with 0.0, 0.0
     topHead.getPoints().addAll(0.0, 0.0,        //top (left) corner of the triangle
       0.0, 6 * GRID,
       4 * GRID, 3 * GRID);
     topHead.setTranslateX(9 * GRID);
     topHead.setTranslateY(0 * GRID);
     topHead.setFill(iconColor);

     //bottom arrow
     Path bottomPath = new Path(
       new MoveTo(12 * GRID, 7 * GRID),
       new ArcTo(3 * GRID, 3 * GRID, 0.0, 9 * GRID, 10 * GRID, false, true),
       new LineTo(4 * GRID + (LINE_WIDTH / 2), 10 * GRID) //-LINE_WIDTH/2 to prevent overlapping with array: line will be longer than endpoint because of LINE_WIDTH
     );
     bottomPath.setStrokeWidth(LINE_WIDTH);
     bottomPath.setStroke(iconColor);

     Polygon bottomHead = new Polygon();  //arrowhead
     //build it in the origin because it must start with 0.0, 0.0
     bottomHead.getPoints().addAll(0.0, 0.0,        //top (right) corner of the triangle
       0.0, 6 * GRID,
       -4 * GRID, 3 * GRID);
     bottomHead.setTranslateX(4 * GRID);
     bottomHead.setTranslateY(7 * GRID);
     bottomHead.setFill(iconColor);


     //put all together
     icon.getChildren().addAll(topPath, topHead, bottomPath, bottomHead);

     //center icon
     icon.setTranslateX((buttonRect.getWidth() - iconSize) / 2);
     icon.setTranslateY((buttonRect.getHeight() - iconSize) / 2);
   return icon;
  }

  /**
   * control the strikeThrough being visible
   * and the tooltip text
   *
   * @param repeatMode    false = strikeThrough
   */
  public void setRepeatMode(boolean repeatMode){
    this.repeatMode.set(repeatMode);
  }

  private void setTooltipText(){
    if (repeatMode.get()) {
      if (playListMode.get())
        tooltip.setText(KissPhoto.language.getString("repeat.list"));
      else
        tooltip.setText(KissPhoto.language.getString("repeat.current.media.file"));
    }else{
      if (playListMode.get())
        tooltip.setText(KissPhoto.language.getString("don.t.repeat.play.list.once"));
      else
        tooltip.setText(KissPhoto.language.getString("don.t.repeat.play.current.media.once"));
    }
  }

  public boolean isRepeatMode(){
    return repeatMode.get();
  }

  //for binding with menues and fullscreen PlayerControl
  public SimpleBooleanProperty repeatModeProperty(){return repeatMode;}
  //for binding with the PlayListButton of the same PlayerControl for better toolTip-Texts
  public SimpleBooleanProperty playListModeProperty(){return playListMode;}

  public double getWidth() {
    return buttonRect.getWidth();
  }

  public double getHeight() {
    return buttonRect.getHeight();
  }
}
