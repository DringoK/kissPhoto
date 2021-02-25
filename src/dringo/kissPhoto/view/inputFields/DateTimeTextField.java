package dringo.kissPhoto.view.inputFields;

import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import static dringo.kissPhoto.KissPhoto.language;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (TextField) which accepts numbers (digits), space, : and - only (characters allowed in date and time)
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 *
 * @author Ingo
 * @since 2012-10-06
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers
 * @version 2016-11-01 RestrictedTextfield stores connected MediaFile and Column no more locally
 * @version 2014-05-02 I18Support
 */
public class DateTimeTextField extends RestrictedTextField {
  private static final String PLEASE_ENTER_A_DATE_TIME_IN_FORMAT_YYYY_MM_DD_HH_MM_SS = "please.enter.a.date.time.in.format.yyyy.mm.dd.hh.mm.ss";

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
