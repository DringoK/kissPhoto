package dringo.kissPhoto.view.viewerHelpers.viewerButtons;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a Burger Menu Button (3 horizontal Lines)
 * it is a Group and consists of a rectangle (background) three lines
 *
 * @author Dringo
 * @since 2020-11-15
 * @version 2020-11-15 initial version
 */
public class BurgerMenuButton extends Group {

  private Rectangle buttonRect;
  private final Node icon;


  /**
   * @param size  the width and height of the button
   */
  public BurgerMenuButton(double size, Color backgroundColor, Color iconColor) {
    super();

    //this rect is used as the clicking area for mouse control. It will be invisible (background-color)
    buttonRect = new Rectangle(size, size, backgroundColor);

    //and three horizontal lines
    icon = createIcon(size *2/3, iconColor);
    getChildren().addAll(buttonRect, icon);
  }

  /**
   * the Burger Control is a group of three horizontal lines
   *
   * @param iconSize  the width of the two lines
   * @return the control as a Node
   */
  private Node createIcon(double iconSize, Color iconColor) {
    final Group icon = new Group();
    final double LINE_WIDTH = iconSize/6;

    final Line topLine = new Line(0, iconSize-LINE_WIDTH, iconSize, iconSize-LINE_WIDTH);
    topLine.setStrokeWidth(LINE_WIDTH);
    topLine.setStroke(iconColor);

    final Line middleLine = new Line(0, iconSize/2, iconSize, iconSize/2);
    middleLine.setStrokeWidth(LINE_WIDTH);
    middleLine.setStroke(iconColor);

    final Line bottomLine = new Line(0, LINE_WIDTH, iconSize, LINE_WIDTH);
    bottomLine.setStrokeWidth(LINE_WIDTH);
    bottomLine.setStroke(iconColor);


    icon.getChildren().addAll(topLine, middleLine, bottomLine);

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
