package de.kissphoto.view.inputFields;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


/**
 * This is an abstract input field (TextField) which supports verification of input
 * it installs event listener and calls abstract verification methods
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 * An implementing class just needs to implement the verification methods and to set the errorMessage
 *
 * @author Ingo
 * @since 2012-10-06
 * @version 2020-12-20 lambda expressions for event handlers
 * @version 2016-11-01 RestrictedTextfield stores connected MediaFile and Column no more locally
 */
public abstract class RestrictedTextField extends TextField implements RestrictedInputField {
  private Tooltip tooltip;
  private Stage containingStage;

  /**
   * @param stage is the containing Stage (for showing tooltips, when restricted character has been entered
   */
  RestrictedTextField(Stage stage) {
    super();
    init(stage);
  }

  /**
   * @param caption the caption can be passed for an inital value of the TextField
   * @param stage   is the containing Stage (for showing tooltips, when restricted character has been entered
   */
  RestrictedTextField(String caption, Stage stage) {
    super(caption);
    init(stage);
  }

  private void init(Stage stage) {
    containingStage = stage;
    installEventHandlers();
    tooltip = new Tooltip();
    tooltip.setAutoHide(true);
    tooltip.setAutoFix(true);
  }

  /**
   * Implement a check that is called directly after clipboard insertion
   * i.e. the insertion already has taken place and correction can be performed now.
   *
   * @return error message or empty string if no unallowed characters had been inserted
   */
  protected abstract String verifyClipboardInsertion();

  /**
   * Implement a check that is called with every key event
   * This verification is part of an EventFilter: If the event is consumed within the verification no char is inserted in textfield
   *
   * @param event the keyEvent that triggered the verification
   * @return error message or empty string if no unallowed characters had been inserted
   */
  protected abstract String verifyKeyTyped(KeyEvent event);

  /**
   * Event Handlers to prevent from illegal input
   */
  private void installEventHandlers() {
    //remove invalid characters after ctrl-V (insert from clipboard)
    setOnKeyReleased(event -> {
      //clipboard  or copy from line above
      if (event.isControlDown() && (event.getCode() == KeyCode.V) ||
        (event.isControlDown() && (event.getCode() == KeyCode.U))) {
        String err = verifyClipboardInsertion(); //abstract method to be overwritten by implementing classes
        if (!err.isEmpty()) {
          tooltip.setText(err);
          setTooltip(tooltip);
          tooltip.show(RestrictedTextField.this, tooltipScreenLocationX(), tooltipScreenLocationY());
        } else {
          tooltip.hide();
          Tooltip.uninstall(RestrictedTextField.this, tooltip);
        }
      }
    });

    //suppress invalid characters
    addEventFilter(KeyEvent.KEY_TYPED, event -> {
      if ((event.getCharacter().length() > 0) &&
          !(event.getCharacter().startsWith("\t")) && //do not check tab (will move the focus and not produce text)
          !(event.getCharacter().startsWith("\b"))   //do not check backspace (will not produce text(but remove;-))
          ) {
        String err = verifyKeyTyped(event);  //abstract method to be overwritten by implementing classes

        if (!err.isEmpty()) {
          tooltip.setText(err);
          setTooltip(tooltip);
          tooltip.show(RestrictedTextField.this, tooltipScreenLocationX(), tooltipScreenLocationY());
        } else {
          tooltip.hide();
          Tooltip.uninstall(RestrictedTextField.this, tooltip);
        }
      }
    });

    focusedProperty().addListener((arg0, oldVal, newVal) -> {
      if (!newVal) { //focus became 'false' --> focus lost event
        tooltip.hide();
        Tooltip.uninstall(RestrictedTextField.this, tooltip);
      }
    });
  }

  /**
   * calculate the x position (relative to screen) near the editInputField where to show the tooltip as error-Message
   *
   * @return x position to place tooltip on show
   */
  private double tooltipScreenLocationX() {
    final double sceneX = getScene().getX();
    final double localX = localToScene(0, 0).getX();
    final double stageX = containingStage.getX();
    return sceneX + localX + stageX;
  }

  /**
   * calculate the y position (relative to screen) near the textfield where to show the tooltip as error-Message
   *
   * @return y position to place tooltip on show
   */
  private double tooltipScreenLocationY() {
    return getScene().getY() + localToScene(0, getHeight()).getY() + containingStage.getY();
  }
}
