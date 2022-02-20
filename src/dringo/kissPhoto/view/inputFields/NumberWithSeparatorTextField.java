package dringo.kissPhoto.view.inputFields;

import dringo.kissPhoto.KissPhoto;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (TextField) which accepts numbers (digits) with maximum one separator character only
 * A tooltip is shown with explanation which characters are allowed if invalid characters are tried to enter
 * Invalid characters are ignored.
 *
 * @author Dringo
 * @since 2022-02-20
 * @version 2022-02-20 initial version
 */
public class NumberWithSeparatorTextField extends RestrictedTextField {
  private String errorMessage;
  private char separator;


  /**
   * @param separator the character that may be in the number (e.g. decimal point or slash or...)
   * @param errorMessage the constant in kissphoto_ll.properties that holds the error message if a invalid character is detected
   * @param stage the stage of kissphoto for showing the errorMessage as a bubble help
   */
  public NumberWithSeparatorTextField(char separator, String errorMessage, Stage stage) {
    super(stage);
    this.separator = separator;
    this.errorMessage = errorMessage;
  }

  public NumberWithSeparatorTextField(String initialText, char separator, String errorMessage, Stage stage) {
    super(initialText, stage);
    this.separator = separator;
    this.errorMessage = errorMessage;
  }

  /**
   * allow digits only and maximum one separator char, delete all other characters
   *
   * @return error Message or "" if no error
   */
  @Override
  protected String verifyClipboardInsertion() {
    boolean invalidCharFound = false;
    boolean alreadyHasFirstSeparator = false; //if first point has been detected a second one is not allowed and will be deleted
    StringBuilder str = new StringBuilder(getText());
    int i = 0;
    while (i < str.length()) {
      if (!(Character.isDigit(str.charAt(i)) ||                        //delete character if not (a digit
          ((str.charAt(i) == separator) && !alreadyHasFirstSeparator))) {    //or a first separator)
        str.deleteCharAt(i);
        invalidCharFound = true;
      }else{
        if (str.charAt(i) == '.') alreadyHasFirstSeparator = true;  //a first decimal point has been accepted above, remember this so that no more are accepted
        i++;                                                    //if no deletion was necessary then next character
      }
    }

    if (invalidCharFound) {
      setText(str.toString()); //take over the corrected string
      return KissPhoto.language.getString(errorMessage); //and return the error message
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
            !((event.getCharacter().charAt(0) == separator) && !getText().contains(Character.toString(separator))) //and not a first separator char (e.g. decimal point and not decimal point until now)
            //and not [BACKSPACE] (this is no event)
        )) {
      event.consume();     //ignore these keys!!
      return KissPhoto.language.getString(errorMessage);
    } else {
      return ""; //no error :-)
    }
  }
}
