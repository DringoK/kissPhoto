package de.kissphoto.view.dialogs;

import de.kissphoto.helper.I18Support;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ResourceBundle;

/**
 * This is the Dialog ancestor for all dialogs in KissPhoto
 * It is not shown directly but concentrates all methods that are common for all dialogs.
 * <li>showing the About Window
 *
 * @author: Ingo
 * @date: 2014-06-16 multi monitor support
 * @modified:
 */
public class KissDialog extends Stage {
  protected static ResourceBundle language = I18Support.languageBundle;
  //constants for setting which button you need
  //and reporting which button was used to close the message dialog
  public static final boolean OK_BOOL = true;
  public final boolean CANCEL_BOOL = false;
  boolean modalResult_bool = true;

  protected int modalResult;

  protected static final String OK_LABEL = language.getString("ok");
  protected static final String CANCEL_LABEL = language.getString("cancel");
  protected static final String YES_LABEL = language.getString("yes");
  protected static final String NO_LABEL = language.getString("no");
  Scene scene;

  public KissDialog(StageStyle stageStyle, Stage owner) {
    super(stageStyle);
    init(owner);
  }

  public KissDialog(Stage owner) {
    init(owner);
  }

  private void init(Stage owner) {
    initModality(Modality.APPLICATION_MODAL);
    initOwner(owner);
  }

  /**
   * For MultiScreen-Support the Dialog should appear always on the same screen as the owner (usually the main stage)
   */
  public void centerOnOwner() {
    setX(getOwner().getX() + ((getOwner().getWidth() - this.getWidth()) / 2));
    setY(getOwner().getY() + ((getOwner().getHeight() - this.getHeight()) / 2));
  }

  /**
   * force repaint by reseting the scene
   * This solves a repainting bug in JavaFx 1.8.05
   */
  protected void repaint() {
    setScene(null);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        setScene(scene);
      }
    });
  }
}
