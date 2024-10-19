package dringo.kissPhoto.view.viewerHelpers;

import dringo.kissPhoto.view.viewerHelpers.viewerButtons.CancelButton;
import dringo.kissPhoto.view.viewerHelpers.viewerButtons.UpButton;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a semi transparent bar to indicate that the user can change the folder
 * It is shown in fileTableView and the mediaViewers whenever the user runs over the border of the current folder.
 *
 * <ul>
 * <li>takes over the width of the parent region (FileTable or MediaViewers)</li>
 * <li>can be place to top or bottom depending on direction prev or next</li>
 * <li>shows buttons to cancel or to continue</li>
 * <li>reacts on arrow up/down and page up/down to continue</li>
 * </ul>
 * <br>
 *
 * @author Dringo
 * @since 2024-10-17
 * @version 2024-xx
 */

public class NextFolderOverlay extends HBox {

  public enum Direction {PREV, NEXT}
  public static final Color ICON_COLOR = new Color(1, 1, 1, .90);
  public static final Color BACKGROUND_COLOR = new Color(0, 0, 0, .55);
  private static final double PADDING = 12.0; //borders

  //control Panel sizes
  private static final double CONTROL_AREA_HEIGHT = 25.0;
  protected static final double BUTTON_SIZE = 20.0;  //width and height


  CancelButton cancelButton = new CancelButton(BUTTON_SIZE, BACKGROUND_COLOR, ICON_COLOR);
  UpButton upButton = new UpButton(BUTTON_SIZE, BACKGROUND_COLOR, ICON_COLOR);
  Text explainText = new Text("  press key again to change to ");

  /**
   * create a NextFolderOverlay
   * @param parentRegion a FileTable or MediaViewer where it should be embedded
   */
  public NextFolderOverlay(Region parentRegion){
    setStyle("-fx-background-color: blue;");
    setOpacity(0.5);
    setVisible(false); //visible only while possible folder change
    setAlignment(Pos.TOP_CENTER);

    setHeight(CONTROL_AREA_HEIGHT);

    prefWidthProperty().bind(parentRegion.widthProperty());

    getChildren().addAll(cancelButton, upButton, explainText);
  }

  /**
   * show the overlay
   * @param direction determines if up or down arrow will be shown and if the overlay will be shown on top or bottom
   * @return
   */
  public boolean show(Direction direction){
    if (direction==Direction.PREV){
      setAlignment(Pos.TOP_CENTER);
    }else{
      setAlignment(Pos.BOTTOM_CENTER);
    }
    setVisible(true);
    return true;
  }
}
