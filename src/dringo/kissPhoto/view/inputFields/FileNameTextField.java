package dringo.kissPhoto.view.inputFields;

import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static dringo.kissPhoto.KissPhoto.language;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (TextField) which does not accepts characters that are not allowed in filenames (e.g. :,+,?...)
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 *
 * @author Dringo
 * @since 2012-10-06
 * @version 2020-12-20 language now static in KissPhoto
 * @version 2016-11-01 RestrictedTextfield stores connected MediaFile and Column no more locally
 * @version 2014-05-02 I18Support
 */
public class FileNameTextField extends RestrictedTextField {
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
