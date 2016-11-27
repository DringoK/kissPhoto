package de.kissphoto.view.inputFields;

import de.kissphoto.helper.I18Support;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ResourceBundle;


/**
 * This is an input field (TextField) which does not accepts characters that are not allowed in filenames (e.g. :,+,?...)
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 *
 * @User: Ingo
 * @Date: 06.10.12
 * @modified: 02.05.14: I18Support
 * @modified: 01.11.16: RestrictedTextfield stores connected MediaFile and Column no more locally
 */
public class FileNameTextField extends RestrictedTextField {
  private static ResourceBundle language = I18Support.languageBundle;
  private static final String ARE_NOT_ALLOWED_IN_FILENAMES = "are.not.allowed.in.filenames";

  //@constructor
  public FileNameTextField(Stage stage) {
    super(stage);
  }

  public FileNameTextField(String caption, Stage stage) {
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
    if (getText().contains("\\")) {
      setText(getText().replace("\\", ""));
      invalidCharFound = true;
    }
    if (getText().contains("/")) {
      setText(getText().replace("/", ""));
      invalidCharFound = true;
    }
    if (getText().contains(":")) {
      setText(getText().replace(":", ""));
      invalidCharFound = true;
    }
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
      return language.getString(ARE_NOT_ALLOWED_IN_FILENAMES);
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
    if ("\\/:*?\"<>|".contains(event.getCharacter())) {
      event.consume();     //ignore these keys!!
      return language.getString(ARE_NOT_ALLOWED_IN_FILENAMES);
    } else {
      return "";
    }
  }
}
