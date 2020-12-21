package de.kissphoto.view.inputFields;

import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * @author Ingo
 * @since 2016-11-01
 * <p>
 * Restricted Textfield that accepts all characters but supports all interfaces
 */
public class UnrestrictedTextField extends RestrictedTextField {
  /**
   * @param stage is the containing Stage (for showing tooltips, when restricted character has been entered
   */
  public UnrestrictedTextField(Stage stage) {
    super(stage);
  }

  /**
   * @param caption the caption can be passed for an inital value of the TextField
   * @param stage   is the containing Stage (for showing tooltips, when restricted character has been entered
   */
  public UnrestrictedTextField(String caption, Stage stage) {
    super(caption, stage);
  }

  /**
   * Implement a check that is called directly after clipboard insertion
   * i.e. the insertion already has taken place and correction can be performed now.
   *
   * @return empty string to indicate that no unallowed characters had been inserted
   */
  @Override
  protected String verifyClipboardInsertion() {
    return "";
  }

  /**
   * Implement a check that is called with every key event
   * This verification is part of an EventFilter: If the event is consumed within the verification no char is inserted in textfield
   *
   * @param event the keyEvent that triggered the verification
   * @return empty string to indicate that no unallowed characters had been inserted
   */
  @Override
  protected String verifyKeyTyped(KeyEvent event) {
    return "";
  }

}
