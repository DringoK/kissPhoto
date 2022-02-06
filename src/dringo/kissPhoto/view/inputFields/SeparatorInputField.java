package dringo.kissPhoto.view.inputFields;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (Combo-Box) which presents all supported separator characters (after counter)
 *
 * @author Dringo
 * @since 2014-06-22
 * @version 2020-12-20 lambda expressions for event handlers
 * @version 2016-11-01 RestrictedTextfield stores connected MediaFile and Column no more locally
 */
public class SeparatorInputField extends ComboBox<String> implements RestrictedInputField {
  public static final String SEPARATOR_CHARS = " _-"; //space (must be first in this string, see constructor), underline or dash will be interpreted as separator characters
  public static final String SEPARATOR_NONE = "";
  public static final String SEPARATOR_SPACE = " ";
  public static final String SEP_NAME_NONE = "None";
  public static final String SEP_NAME_SPACE = "Space";

  /**
   * create a new comboBox, nothing selected, no initial value
   */
//@constructor
  public SeparatorInputField() {
    super();
    init();
  }

  /**
   * create new ComboBox
   *
   * @param initialText is the initial value
   */
//@constructor
  public SeparatorInputField(String initialText) {
    super();
    init();
    setValue(initialText);
  }

  /**
   * initialization common to all constructors
   */
  private void init() {
    //add standard separators
    getItems().addAll(SEP_NAME_NONE, SEP_NAME_SPACE);

    //add all the rest
    int i = 1;
    while (i < SEPARATOR_CHARS.length()) {
      getItems().add(SEPARATOR_CHARS.substring(i, i + 1));
      i++;
    }

    setOnMouseEntered(mouseEvent -> {
      show();  //immediately open, no second click necessary
    });

    setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.SPACE) {
        show();
      }
    });
    setEditable(false); //no input in editInputField allowed only by drop down
  }

  /**
   * convert
   *
   * @param o a string from the displayed string list
   * @return to a string that will be displayed and used in value-propertey
   */
  public static String toString(Object o) {
    String s = (String) o;
    if (s == null) {
      return SEP_NAME_NONE;
    } else {
      return switch (s) {
      //translate the non visible characters
        case SEPARATOR_NONE -> SEP_NAME_NONE;
        case SEPARATOR_SPACE -> SEP_NAME_SPACE;
        //use the others as is
        default -> s;
      };
    }
  }

  /**
   * convert
   *
   * @param s the string that is displayed in the list
   * @return into the (String) object that is displayed in the table cell
   */
  public static Object fromString(java.lang.String s) {
    return switch (s) {
      //translate the non visible characters back
      case SEP_NAME_NONE -> SEPARATOR_NONE;
      case SEP_NAME_SPACE -> SEPARATOR_SPACE;
      //use the others as is
      default -> s;
    };
  }

  /**
   * At the end of editing TextFieldCell calls this method to give the RestrictedTextField-Implementation a chance
   * to make the entered value valid. E.g. a valid Date/Time in DateTimeTextField
   * The default implementation of validate here just returns the unchanged input text
   * @param text to be validated
   * @param defaultText the text to be returend if text could not be "repaired" during validation
   * @return the text modified so that it is valid or empty String if not possible
   */
  public String validate(String text, String defaultText){
    return text;
  }

  //implement TextFieldComboBox interface to map the TextField-Interface to ComboBox
  @Override
  public void setText(String newValue) {
    setValue(toString(newValue));
  }

  @Override
  public String getText() {
    return (String) fromString(getValue());
  }

  @Override
  public void selectRange(int startPos, int endPos) {
    setStyle("-fx-background-color: blue;");
  }

  @Override
  public void selectAll() {
    setStyle("-fx-background-color: blue;");
  }

  @Override
  public void deselect() {
    setStyle("-fx-background-color: white;");
  }

  @Override
  public void positionCaret(int pos) {
    //nothing to do: it is only one character maximum
  }

  @Override
  public int getCaretPosition() {
    return 0;
  }

  @Override
  public int getLength() {
    return getValue().length(); //is 0 or 1
  }
}
