package dringo.kissPhoto.view.inputFields;

import dringo.kissPhoto.model.MediaFile;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.text.ParseException;
import java.util.Date;

import static dringo.kissPhoto.KissPhoto.language;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 * <p>
 * This is an input field (TextField) which accepts numbers (digits), space, : and - only (characters allowed in date and time)
 * A tooltip is shown with explanation which characters are allowed if illegal characters are tried to enter
 *
 * @author Ingo
 * @version 2014-05-02 I18Support
 * @since 2012-10-06
 */
public class DateTimeTextField extends RestrictedTextField {
  private static final String PLEASE_ENTER_A_DATE_TIME_IN_FORMAT_YYYY_MM_DD_HH_MM_SS = "please.enter.a.date.time.in.format.yyyy.mm.dd.hh.mm.ss";
  private static final String MASK = "____-__-__ __:__:__";  //keep compatible to MediaFile.DATE_PATTERN
  private static final int DATE_LENGTH = MASK.length();

  boolean wasDelete = false;
  boolean wasBackspace = false;

  /**
   * @param initialText is valid date. If not then we start with an empty dateTime
   * @param stage       connection to MainStage for bubble help error message
   */
  public DateTimeTextField(String initialText, Stage stage) {
    super(validateDate(initialText, MASK), stage);

    //track Selection, Delete and Backspace for verifyKeyTyped()
    addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      switch (event.getCode()) {
        case DELETE -> {
          wasDelete = true;
          wasBackspace = false;
        }
        case BACK_SPACE -> {
          wasDelete = false;
          wasBackspace = true;
        }
        default -> {
          wasDelete = false;
          wasBackspace = false;
        }
      }
    });
  }

  /**
   * allow inputs according MASK
   * when this method is called JavaFX has already inserted the content of the clipboard
   * strategy:
   * extract the inserted text: assume that nothing was selected before insertion, then the text is now longer by the length of the insertion and the caret is at the end
   * remove the inserted text
   * insert and verify it char by char as if it had been typed in again
   * <p>
   * but if there was a selection replaced by the insertion then it might be possible that invalid characters remain (e.g. select YYYY and insert "hall")
   * therefore: finally replace all invalid characters with MASK
   *
   * @return error Message if invalid characters have been detected or "" if no error
   */
  @Override
  protected String verifyClipboardInsertion() {
    boolean invalidCharFound = false;
    int oldCaretPos = getCaretPosition();

    //--- assume it was no selection --> overwrite
    String input;
    int insertionLength = getText().length() - MASK.length();
    if (insertionLength > 0) {
      input = getText().substring(getCaretPosition() - insertionLength, getCaretPosition());
      replaceText(getCaretPosition() - insertionLength, getCaretPosition(), ""); //delete the inserted text
      positionCaret(getCaretPosition() - insertionLength);
      int i = 0;
      while (getCaretPosition() < MASK.length() && i < input.length()) { //either input or Mask
        if (!insertCharIfValid(input.charAt(i))) {
          invalidCharFound = true;
        }
        i++;
      }

      //--- but maybe there was a selection replaced so eliminate all invalid characters
      oldCaretPos = getCaretPosition(); //the position is now "as if the clipboard content had been typed"
      if (oldCaretPos > MASK.length()) //just to ensure that it is valid
        oldCaretPos = MASK.length();
    }


    input = getText();
    setText(MASK); //start from scratch
    positionCaret(0);
    int i = 0;
    while (getCaretPosition() < MASK.length() && i < input.length()) { //either input or Mask
      if (!insertCharIfValid(input.charAt(i))) {
        invalidCharFound = true;
      }
      i++;
    }
    positionCaret(oldCaretPos);

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
    if (event.getCharacter().length() < 1)
      return language.getString(PLEASE_ENTER_A_DATE_TIME_IN_FORMAT_YYYY_MM_DD_HH_MM_SS);

    int pos = getCaretPosition();

    //if a DELETE or BACKSPACE has been detected TextField usually has already executed it before firing the event for the application
    //so just restore the MASK here
    if (wasDelete || wasBackspace) {
      //too short after systems' deletion --> fill up with characters from the mask
      int missingChars = MASK.length() - getText().length();
      if (missingChars > 0)
        replaceText(pos, pos, MASK.substring(pos, pos + missingChars));

      if (pos < MASK.length()) {
        positionCaret(pos);

        if (wasDelete)
          extendSelection(pos + missingChars);
      }

      //reset
      wasBackspace = false;
      wasDelete = false;
      return "";
    }

    event.consume(); //insertion valid->do replacement, if not->ignore the key --> always end the event

    //-------- otherwise handle normal input
    if (insertCharIfValid(event.getCharacter().charAt(0))) {
      return "";
    } else {
      return language.getString(PLEASE_ENTER_A_DATE_TIME_IN_FORMAT_YYYY_MM_DD_HH_MM_SS);
    }
  }

  private boolean insertCharIfValid(char key) {
    int pos = getCaretPosition();
    if (pos > getSelection().getStart())
      pos = getSelection().getStart(); //if selection exists use the beginning of the selection

    boolean isValid = false;
    if (pos < MASK.length()) { //editing only in the area defined by the mask
      char maskChar = MASK.charAt(pos);

      if (Character.isDigit(key)) {  //it's always allowed to type a digit
        isValid = true;
        //jump over delimiters if digit entered where delimiter is expected
        if (maskChar != '_') {
          pos++;
        }
      } else {
        isValid = (key == maskChar);  //it allowed (but no necessary) to type a delimiter of the mask
      }
    }

    if (isValid) {
      //if something is selected delete it first
      replaceSelection(MASK.substring(getSelection().getStart(), getSelection().getEnd()));
      //replace and move caret = always overtype
      replaceText(pos, pos + 1, Character.toString(key));
      positionCaret(pos + 1);
    }
    return isValid;
  }

  /**
   * At the end of editing FileTableTextFieldCell calls this method to give the RestrictedTextField-Implementation a chance
   * to make the entered value valid.
   * Here a valid date/time String is produced out of the input:
   * SimpleDateFormat is used to parse() the string and produce the string again using format()
   *
   * @param text        to be validated
   * @param defaultText return this text if validation and repair fail
   * @return the text modified so that it is a valid Date/Time or defaultText if not possible
   */
  @Override
  public String validate(String text, String defaultText) {
    return validateDate(text, defaultText);
  }

  private static String validateDate(String text, String defaultText) {
    String validated;

    //truncate
    if (text.length() > DATE_LENGTH)
      text = text.substring(0, DATE_LENGTH - 1); //-1 --> Zero Based

    try {
      Date date = MediaFile.dateFormatter.parse(text);
      validated = MediaFile.dateFormatter.format(date);
    } catch (ParseException e) {
      validated = defaultText; //it was not possible to interpret it as date/time
    }
    if (validated.isEmpty()) //an empty text will not cause ParseException but is still not a valid date
      return defaultText;
    else
      return validated;
  }
}

