package dringo.kissPhoto.view.inputFields;

import dringo.kissPhoto.KissPhoto;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (TextField) which accepts numbers (digits) with maximum one decimal point only
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 * Illegal characters are ignored.
 *
 * @author Dringo
 * @since 2022-01-14
 * @version 2022-01-14 initial version
 */
public class RationalTextField extends RestrictedTextField {
  private static final String MESSAGE = "a.number.may.contain.only.digits.and.one.decimal.point";

  //@constructor
  public RationalTextField(Stage stage) {
    super(stage);
  }

  public RationalTextField(String initialText, Stage stage) {
    super(initialText, stage);
  }

  /**
   * allow digits only, delete all other characters
   *
   * @return error Message or "" if no error
   */
  @Override
  protected String verifyClipboardInsertion() {
    boolean invalidCharFound = false;
    boolean alreadyHasFirstPoint = false; //if first point has been detected a second one is not allowed and will be deleted
    StringBuilder str = new StringBuilder(getText());
    int i = 0;
    while (i < str.length()) {
      if (!(Character.isDigit(str.charAt(i)) ||       //delete character if not (a digit
          ((str.charAt(i) == '.') && !alreadyHasFirstPoint))) {    //or a first decimal point)
        str.deleteCharAt(i);
        invalidCharFound = true;
      }else{
        if (str.charAt(i) == '.') alreadyHasFirstPoint = true;  //a first decimal point has been accepted above, remember this so that no more are accepted
        i++;                                                    //if no deletion was necessary then next character
      }
    }

    if (invalidCharFound) {
      setText(str.toString()); //take over the corrected string
      return KissPhoto.language.getString(MESSAGE); //and return the error message
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
        (!Character.isDigit(event.getCharacter().charAt(0)) &&  //not a digit
            (event.getCharacter().charAt(0) != 13) &&               //and not [ENTER]
            (event.getCharacter().charAt(0) != 127) &&              //and not [DEL]
            !((event.getCharacter().charAt(0) == '.' ) && !getText().contains(".")) //and not a first decimal point (i.e point and not point until now)
            //and not [BACKSPACE] (this is no event)
        )) {
      event.consume();     //ignore these keys!!
      return KissPhoto.language.getString(MESSAGE);
    } else {
      return ""; //no error :-)
    }
  }
}
