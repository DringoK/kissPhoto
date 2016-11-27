package de.kissphoto.view;

import de.kissphoto.helper.I18Support;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * **
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p>
 * This is the full screen version of MediaContentView
 * it is a full screen stage and contains a separate mediaContent View
 * The state of the mediaContentView of the main-Window (primaryMediaContentView) is copied into the full-screen mediaContentView
 * in the constructor
 * if ESC is pressed this stage is closed and destroyed
 *
 * @Author: Dr. Ingo Kreuz
 * @Date: 2016-011-06 moved from inner class of MediaContentView to separate class
 * @modified: 2016-11-06
 */
class FullScreenStage extends Stage {
  MediaContentView mediaContentView;
  private static ResourceBundle language = I18Support.languageBundle;

  //@constructor
  public FullScreenStage(MediaContentView primaryMediaContentView) {
    super();
    initOwner(primaryMediaContentView.getOwner());
    //initModality(Modality.APPLICATION_MODAL);

    Group root = new Group();
    Scene scene = new Scene(root, 1, 1, Color.BLACK);  //1,1 --> use min Size as set just before
    setScene(scene);
    mediaContentView = new MediaContentView(this, primaryMediaContentView);
    root.getChildren().add(mediaContentView);
    mediaContentView.setFileTableView(primaryMediaContentView.getFileTableView());
    mediaContentView.setMedia(primaryMediaContentView.getCurrentMediaFile());
    mediaContentView.prefHeightProperty().bind(scene.heightProperty());
    mediaContentView.prefWidthProperty().bind(scene.widthProperty());
    mediaContentView.getAttrViewer().copyState(primaryMediaContentView.getAttrViewer());
    //setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN)); //workaround: something but not ESC
    setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); //dont show hint
    //because: otherwise the ESC for the context menu would close the full screen at the same time
    //ESC is - instead - handled by a KeyEvent-Listener which get no event if context-menu is closed with ESC
    setFullScreenExitHint(language.getString("press.esc.to.exit.full.screen.mode"));
    setFullScreen(true);
  }

  /**
   * @return the link to to primary MediaContentView (if in full screen stage)
   */
  public MediaContentView getMediaContentView() {
    return mediaContentView;
  }
}
