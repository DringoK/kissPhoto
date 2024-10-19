package dringo.kissPhoto.view.viewerHelpers.viewerButtons;

import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a Cancel Button (an X)<br>
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
public class CancelButton extends Group {
  private final Rectangle buttonRect;
  private final Tooltip tooltip = new Tooltip();

  /**
   * @param size  the width and height of the button
   */
  public CancelButton(double size, Color backgroundColor, Color iconColor) {
    super();
    final double ICON_SIZE = size*4/5;

    buttonRect = new Rectangle(size, size, backgroundColor); //this rect is used as the clicking area for mouse control. It will be invisible (background-color)
    Node icon = createIcon(ICON_SIZE, iconColor);

    tooltip.setText("stay in current folder");
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

    Line leftLine = new Line(0, 0, iconSize, iconSize);
    leftLine.setStrokeWidth(LINE_WIDTH);
    leftLine.setStroke(iconColor);

    Line rightLine = new Line(0, iconSize, iconSize,0);
    rightLine.setStrokeWidth(LINE_WIDTH);
    rightLine.setStroke(iconColor);

    icon.getChildren().addAll(leftLine, rightLine);

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
