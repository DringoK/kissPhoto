package dringo.kissPhoto.view.inputFields;

import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static dringo.kissPhoto.KissPhoto.language;


/**
 * This is an input field (TextField) which accepts numbers (digits) only
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 * Illegal characters are ignored.
 *
 * @author Dringo
 * @since 2012-10-05
 * @version 2020-12-20 language now static in KissPhoto
 * @version 2014-05-02 I18Support
 * @version 2016-11-01 RestrictedTextfield stores connected MediaFile and Column no more locally
 */
public class NumberTextField extends RestrictedTextField {
  private static final String SORRY_ONLY_DIGITS_ALLOWED_HERE = "sorry.only.digits.allowed.here";

  //@constructor
  public NumberTextField(Stage stage) {
    super(stage);
  }

  public NumberTextField(String caption, Stage stage) {
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
      if (!Character.isDigit(getText().charAt(i))) {
        setText(getText().replace(getText().substring(i, i + 1), ""));
        i--; //again from the deleted position
        invalidCharFound = true;
      }
      i++;
    }
    if (invalidCharFound) {
      return language.getString(SORRY_ONLY_DIGITS_ALLOWED_HERE);
    } else {
      return ""; //no error :-)
    }
  }

  /**
   * allow digits only, ignore all other characters
   *
   * @param event the keyEvent that triggered the verification
   * @return error Message or "" if no error
   */
  @Override
  protected String verifyKeyTyped(KeyEvent event) {
    if ((event.getCharacter().length() > 0) &&                  //it is an error if
        (!Character.isDigit(event.getCharacter().charAt(0)) &&  //no digit
            (event.getCharacter().charAt(0) != 13) &&               //and not [ENTER]
            (event.getCharacter().charAt(0) != 127)                 //and not [DEL]
            //and not [BACKSPACE] (this is no event)
        )) {
      System.out.println("charAt(0)=" + (int) event.getCharacter().charAt(0));
      event.consume();     //ignore these keys!!
      return language.getString(SORRY_ONLY_DIGITS_ALLOWED_HERE);
    } else {
      System.out.println("no error");
      return ""; //no error :-)
    }
  }
}
