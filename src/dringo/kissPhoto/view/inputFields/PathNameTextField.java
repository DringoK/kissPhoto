package dringo.kissPhoto.view.inputFields;

import dringo.kissPhoto.KissPhoto;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (TextField) which does not accepts characters that are not allowed in pathnames (e.g. :,+,?...)
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 *
 * @author Dringo
 * @since 2014-05-30
 * @version 2020-12-20 language now static in KissPhoto
 * @version 2016-11-01 RestrictedTextfield stores connected MediaFile and Column no longer locally
 */
public class PathNameTextField extends RestrictedTextField {
  private static final String ARE_NOT_ALLOWED_IN_PATH_NAMES = "are.not.allowed.in.path.names";

  //@constructor
  public PathNameTextField(Stage stage) {
    super(stage);
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
      return KissPhoto.language.getString(ARE_NOT_ALLOWED_IN_PATH_NAMES);
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
      return KissPhoto.language.getString(ARE_NOT_ALLOWED_IN_PATH_NAMES);
    } else {
      return "";
    }
  }
}
