package de.kissphoto.view.inputFields;

import de.kissphoto.helper.I18Support;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ResourceBundle;


/**
 * This is an input field (TextField) which does not accepts characters that are not allowed in pathnames (e.g. :,+,?...)
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 *
 * @Author: Dr. Ingo Kreuz
 * @Date: 2014-05-30
 * @modified: 01.11.16: RestrictedTextfield stores connected MediaFile and Column no longer locally
 */
public class PathNameTextField extends RestrictedTextField {
  private static final String ARE_NOT_ALLOWED_IN_PATH_NAMES = "are.not.allowed.in.path.names";
  private static ResourceBundle language = I18Support.languageBundle;

  //@constructor
  public PathNameTextField(Stage stage) {
    super(stage);
  }

  public PathNameTextField(String caption, Stage stage) {
    super(caption, stage);
  }

  /**
   * allow digits only, delete all other characters
   *
   * @return error Message or "" if no error
   */
  @Override
  protected String verifyClipboardInsertion() {
    boolean invalidCharFound = false;
    if (getText().contains("*")) {
      setText(getText().replace("*", ""));
      invalidCharFound = true;
    }
    if (getText().contains("?")) {
      setText(getText().replace("?", ""));
      invalidCharFound = true;
    }
    if (getText().contains("\"")) {
      setText(getText().replace("\"", ""));
      invalidCharFound = true;
    }
    if (getText().contains("<")) {
      setText(getText().replace("<", ""));
      invalidCharFound = true;
    }
    if (getText().contains(">")) {
      setText(getText().replace(">", ""));
      invalidCharFound = true;
    }
    if (getText().contains("|")) {
      setText(getText().replace("|", ""));
      invalidCharFound = true;
    }

    if (invalidCharFound) {
      return language.getString(ARE_NOT_ALLOWED_IN_PATH_NAMES);
    } else {
      return ""; //no error :-)
    }
  }

  /**
   * allow digits only, ignore all other characters
   * exception: tab (#9) is passed through: this will not generate an input but move focus to next node
   *
   * @param event the keyEvent that triggered the verification
   * @return error Message or "" if no error
   */
  @Override
  protected String verifyKeyTyped(KeyEvent event) {
    if ("*?\"<>|".contains(event.getCharacter())) {
      event.consume();     //ignore these keys!!
      return language.getString(ARE_NOT_ALLOWED_IN_PATH_NAMES);
    } else {
      return "";
    }
  }
}
