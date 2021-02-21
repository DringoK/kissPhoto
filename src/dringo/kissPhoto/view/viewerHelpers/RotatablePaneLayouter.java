package dringo.kissPhoto.view.viewerHelpers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This solves a problem with layouting rotated Panes:
 * The layouters (such as Pane, StackPane and so on) interchange height and width of childPanes if the childPane
 * is rotated by 90° or 270°. The solution is to overwrite layoutChildren() which I do in this helper
 * <br>
 * RotatablePaneLayouter gets exactly one Child-Pane
 * <ul>
 * <li>that may be rotated using childPane.setRotate(...)</li>
 * <li>it will always be resized to the height and width of the layouter</li>
 * <li>it is relocated to be completely visible compensating the JavaFX bug in resize()</li>
 * </ul>
 *
 * @author Dringo
 * @date 2018-11-17
 * @modified:
 */

public class RotatablePaneLayouter extends Region {
  private Pane child;

  /**
   * The layouter manages exactly one childPane pane that may be rotated later using childPane.setRotate()
   *
   * @param childPane the Pane to be managed once
   */
  public RotatablePaneLayouter(Pane childPane) {
    getChildren().add(childPane);
    this.child = childPane;

    // make sure layout gets invalidated when the childPane orientation changes
    childPane.rotateProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        requestLayout();
      }
    });
  }

  @Override
  protected void layoutChildren() {
    // set fit sizes:
    //resize child to fit into RotatablePane and correct movement caused by resizing if necessary
    if ((child.getRotate() == 90) || (child.getRotate() == 270)) {
      //vertical
      child.resize(getHeight(), getWidth()); //exchange width and height
      // and relocate to correct movement caused by resizing
      double delta = (getWidth() - getHeight()) / 2;
      child.relocate(delta, -delta);
    } else {
      //horizontal
      child.resize(getWidth(), getHeight()); //keep width and height
      //with 0° or 180° resize does no movement to be corrected
      child.relocate(0, 0);
    }
  }
}
