package dringo.kissPhoto.view.viewerHelpers.viewerButtons;

import dringo.kissPhoto.view.viewerHelpers.ViewerControlPanel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a FullScreen Button (rectangle)<br>
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
 * @since 2020-12-13
 * @version 2020-12-13 initial version
 */
public class FullScreenButton extends Group {
  private final Rectangle buttonRect;
  private final Tooltip tooltip = new Tooltip();

  private final SimpleBooleanProperty fullScreenMode = new SimpleBooleanProperty(false);

  /**
   * @param size  the width and height of the button
   */
  public FullScreenButton(double size, Color backgroundColor, Color iconColor) {
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

    setTooltipText();
    tooltip.setShowDelay(ViewerControlPanel.TOOLTIP_DELAY);
    Tooltip.install(buttonRect, tooltip);

    getChildren().addAll(buttonRect, icon, strikeThrough);

    strikeThrough.visibleProperty().bind(fullScreenMode); //Strike Through if in FullScreen-Mode (clicking will end fullScreenMode)
    fullScreenMode.addListener((observable, oldValue, newValue) -> setTooltipText());

    setFullScreenMode(false);
  }

  /**
   * the FullScreen Button icon is a rectangle
   *
   * @param iconSize  the width of the icon
   * @return the control as a Node
   */
  private Node createIcon(double iconSize, Color iconColor) {
     final double GRID = iconSize / 13;
     final double LINE_WIDTH = 2 * GRID;

     Rectangle icon = new Rectangle(iconSize, iconSize*2/3);
     icon.setStrokeWidth(LINE_WIDTH);
     icon.setStroke(iconColor);
     icon.setFill(Color.TRANSPARENT);

    //center icon
    icon.setTranslateX((buttonRect.getWidth() - icon.getWidth()) / 2);
    icon.setTranslateY((buttonRect.getHeight() - icon.getHeight()) / 2);
   return icon;
  }

  /**
   * control the strikeThrough being visible
   * and the tooltip text
   *
   * @param fullScreenMode    false = strikeThrough
   */
  public void setFullScreenMode(boolean fullScreenMode){
    this.fullScreenMode.set(fullScreenMode);
  }

  private void setTooltipText(){
    if (fullScreenMode.get()) {
      tooltip.setText(language.getString("end.fullscreen.mode"));
    }else{
      tooltip.setText(language.getString("start.fullscreen.mode"));
    }
  }

  public boolean isFullScreenMode(){
    return fullScreenMode.get();
  }

  //for binding with menues and fullscreen PlayerControl
  public SimpleBooleanProperty fullScreenModeProperty(){return fullScreenMode;}

  public double getWidth() {
    return buttonRect.getWidth();
  }

  public double getHeight() {
    return buttonRect.getHeight();
  }
}
