package dringo.kissPhoto.view;

import dringo.kissPhoto.KissPhoto;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
  public FullScreenStage(MediaContentView primaryMediaContentView) {
    super();
    initOwner(primaryMediaContentView.getOwner()); //link to main Application, so that it will be closed together

    //build GUI
    Group root = new Group();
    Scene scene = new Scene(root, 1, 1, Color.BLACK);  //1,1 --> use min Size during build. Will be fullScreen.
    setScene(scene);

    //build new MediaContentView for fullScreen Stage and link it to main window / primaryMediaContentView
    mediaContentView = new MediaContentView(this, primaryMediaContentView); //link to primaryMediaContentView
    mediaContentView.setFileTableView(primaryMediaContentView.getFileTableView());

    mediaContentView.prefHeightProperty().bind(scene.heightProperty());
    mediaContentView.prefWidthProperty().bind(scene.widthProperty());
    root.getChildren().add(mediaContentView);

    setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); //dont show hint
    //because: otherwise the ESC for the context menu would close the full screen at the same time
    //ESC is - instead - handled by a KeyEvent-Listener which gets no event if context-menu is closed with ESC
    setFullScreenExitHint(KissPhoto.language.getString("press.esc.to.exit.full.screen.mode"));
    setFullScreen(true);
  }

  /**
   * @return the link to to primary MediaContentView (if in full screen stage)
   */
  public MediaContentView getMediaContentView() {
    return mediaContentView;
  }
}
