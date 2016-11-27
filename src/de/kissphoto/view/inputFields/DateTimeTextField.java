package de.kissphoto.view.inputFields;

import de.kissphoto.helper.I18Support;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ResourceBundle;


/**
 * This is an input field (TextField) which accepts numbers (digits), space, : and - only (characters allowed in date and time)
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 *
 * @User: Ingo
 * @Date: 06.10.12
 * @modified: 02.05.14: I18Support
 * @modified: 01.11.16: RestrictedTextfield stores connected MediaFile and Column no more locally
 */
public class DateTimeTextField extends RestrictedTextField {
  private static ResourceBundle language = I18Support.languageBundle;
  private static final String PLEASE_ENTER_A_DATE_TIME_IN_FORMAT_YYYY_MM_DD_HH_MM_SS = "please.enter.a.date.time.in.format.yyyy.mm.dd.hh.mm.ss";

  //@constructor
  public DateTimeTextField(Stage stage) {
    super(stage);
  }

  public DateTimeTextField(String caption, Stage stage) {
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
    int i = 0;
    while (i < getText().length()) {
      if (!(Character.isDigit(getText().charAt(i))
          || getText().charAt(i) == '-'
          || getText().charAt(i) == ' '
          || getText().charAt(i) == ':')) {
        setText(getText().replace(getText().substring(i, i + 1), ""));
        i--; //again from the deleted position
        invalidCharFound = true;
      }
      i++;
    }
    setText(getText().trim());

    if (invalidCharFound) {
      return language.getString(PLEASE_ENTER_A_DATE_TIME_IN_FORMAT_YYYY_MM_DD_HH_MM_SS);
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
    if ((event.getCharacter().length() > 0) &&
        !((Character.isDigit(event.getCharacter().charAt(0))) ||
            (event.getCharacter().startsWith("-")) ||
            (event.getCharacter().startsWith(":")))
        ) {
      event.consume();
      return language.getString(PLEASE_ENTER_A_DATE_TIME_IN_FORMAT_YYYY_MM_DD_HH_MM_SS);
    } else {
      return "";
    }
  }
}
