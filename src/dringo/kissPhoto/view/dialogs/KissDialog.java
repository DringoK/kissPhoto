package dringo.kissPhoto.view.dialogs;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is the Dialog ancestor for all dialogs in KissPhoto
 * It is not shown directly but concentrates all methods that are common for all dialogs.
 * <li>showing the About Window
 *
 * @author Dringo
 * @since 2014-06-16 multi monitor support
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 */
public class KissDialog extends Stage {
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
   * The
   */
  public void centerOnOwner() {
    setX(getOwner().getX() + ((getOwner().getWidth() - this.getWidth()) / 2));
    setY(getOwner().getY() + ((getOwner().getHeight() - this.getHeight()) / 2));
  }

  /**
   * do all necessary scaling of the dialog, so that all elements are visible (independent from the current platform scaling)
   * and center the dialog over the main window (i.e. bring to the correct screen and center)
   *
   * ..usually called just before showAndWait() in showModal() of child classes
   */
  public void centerAndScaleDialog() {
    centerOnOwner();
    toFront();
    //repaint was no longer necessary with JDK9 and scaling was solved also with that remove... fine :-)
  }
}
