package dringo.kissPhoto.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p>
 * This is the full screen version of MediaContentView<br>
 * It is a full screen stage and contains a separate mediaContentView<br>
 * The state of the mediaContentView of the main-Window (primaryMediaContentView) is copied into the full-screen mediaContentView
 * in the constructor<br>
 * if ESC is pressed this stage is closed and destroyed<br>
 *<br>
 * @author Dringo
 * @since 2016-11-06 moved from inner class of MediaContentView to separate class
 * @version 2022-09-04 when output scaling changes, width/height will be recalculated to solve problem with TV-sets under Win10
 * @version 2020-12-20 language now static in KissPhoto
 * @version 2017-10-08 currentPlayerPosition is handed over from main window
 * @version 2016-11-06
 */
class FullScreenStage extends Stage {
  MediaContentView mediaContentView;


  /**
   * @param primaryMediaContentView link back to main window for taking over all settings
   * Create new MediaContentView link it to the primary one and take over currentPlayerPosition if possible
   */
  public FullScreenStage(MediaContentView primaryMediaContentView, MetaInfoView metaInfoView) {
    super();
    initOwner(primaryMediaContentView.getStage()); //link to main Application, so that it will be closed together
    initStyle(StageStyle.UNDECORATED);
    setResizable(false);

    //build GUI
    Group root = new Group();
    Scene scene = new Scene(root, 1, 1, Color.BLACK);  //1,1 --> use min Size during build. Will be fullScreen.
    setScene(scene);

    //build new MediaContentView for fullScreen Stage and link it to main window / primaryMediaContentView
    mediaContentView = new MediaContentView( primaryMediaContentView); //link to primaryMediaContentView
    mediaContentView.setOtherViews(primaryMediaContentView.getFileTableView(), metaInfoView);

    mediaContentView.prefHeightProperty().bind(scene.heightProperty());
    mediaContentView.prefWidthProperty().bind(scene.widthProperty());
    root.getChildren().add(mediaContentView);

    //setFullScreen does not work with Win10, JDK16,17&18, and TVsets
    //setFullScreen(true);
  }

  /**
   * to make the contentView's Stage fullScreen:
   * move its Stage to the given screen
   * and adapt width/height to the screen's width/height
   * @param screen the screen where to move
   */
  public void moveToFullScreen(Screen screen) {
    setX(screen.getBounds().getMinX());
    setY(screen.getBounds().getMinY());
    setWidth(screen.getBounds().getWidth());
    setHeight(screen.getBounds().getHeight());
    toFront();
  }


  /**
   * @return the link to to primary MediaContentView (if in full screen stage)
   */
  public MediaContentView getMediaContentView() {
    return mediaContentView;
  }
}
