package dringo.kissPhoto.view.viewerHelpers.viewerButtons;

import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
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
 * This Class implements an Arrow Up Button<br>
 * all elements are in a Group<br>
 *<br>
 *   Note:
 *   <ul>
 *     <li>button content will be created in the size passed to constructor and not be changed when resizing the button</li>
 *     <li>for simplicity the icon is not implemented as the control's skin but simply drawn on the empty control area</li>
 *   </ul>
 *
 * @author Dringo
 * @since 2024-10-17
 * @version 2024-10-17 initial version
 */
public class UpButton extends Group {
  private final Rectangle buttonRect;
  private final Tooltip tooltip = new Tooltip();

  /**
   * @param size  the width and height of the button
   */
  public UpButton(double size, Color backgroundColor, Color iconColor) {
    super();
    final double ICON_SIZE = size*4/5;

    buttonRect = new Rectangle(size, size, backgroundColor); //this rect is used as the clicking area for mouse control. It will be invisible (background-color)
    Node icon = createIcon(ICON_SIZE, iconColor);

    tooltip.setText("proceed to previous folder");
    tooltip.setShowDelay(ViewerControlPanel.TOOLTIP_DELAY);
    Tooltip.install(buttonRect, tooltip);
    getChildren().addAll(buttonRect, icon);
  }

  /**
   * the Burger Control is a group of three horizontal lines
   *
   * @param iconSize  the width of the two lines
   * @return the control as a Node
   */
  private Node createIcon(double iconSize, Color iconColor) {
    Group icon = new Group();
    final double LINE_WIDTH = iconSize/8;
    final double ARROW_SIZE = iconSize/4;

    Line verticalLine = new Line(iconSize/2, 0, iconSize/2, iconSize);
    verticalLine.setStrokeWidth(LINE_WIDTH);
    verticalLine.setStroke(iconColor);

    Polygon arrowHead = new Polygon();
    //build it in the origin because it must start with 0.0, 0.0
    arrowHead.getPoints().addAll(0.0, 0.0,
        ARROW_SIZE, 0.0,
        ARROW_SIZE/2, ARROW_SIZE);
    arrowHead.setTranslateX((iconSize-ARROW_SIZE)/2);
    arrowHead.setTranslateY(iconSize-ARROW_SIZE);

    arrowHead.setFill(iconColor);

    icon.getChildren().addAll(verticalLine, arrowHead);

    //center icon
    icon.setTranslateX((buttonRect.getWidth() - iconSize) / 2);
    icon.setTranslateY((buttonRect.getHeight() - iconSize) / 2);

    return icon;
  }


  public double getWidth() {
    return buttonRect.getWidth();
  }

  public double getHeight() {
    return buttonRect.getHeight();
  }
}
