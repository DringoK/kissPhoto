package de.kissphoto.view.inputFields;

import de.kissphoto.helper.I18Support;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ResourceBundle;


/**
 * This is an input field (Combo-Box) which presents all supported separator characters (after counter)
 *
 * @author: Dr. Ingo Kreuz
 * @Date: 2014-06-22
 * @modified: 01.11.16: RestrictedTextfield stores connected MediaFile and Column no more locally
 */
public class SeparatorInputField extends ComboBox implements RestrictedInputField {
  private static ResourceBundle language = I18Support.languageBundle;

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
   * @param caption is the initial value
   */
//@constructor
  public SeparatorInputField(String caption) {
    super();
    init();
    setValue(caption);
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

    setOnMouseEntered(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        show();  //immediately open, no second click necessary
      }
    });

    setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SPACE) {
          show();
        }
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
      switch (s) {
        //translate the non visible characters
        case SEPARATOR_NONE:
          return SEP_NAME_NONE;     //"break;" not necessary because "return" is used...
        case SEPARATOR_SPACE:
          return SEP_NAME_SPACE;
        //use the others as is
        default:
          return s;
      }
    }
  }

  /**
   * convert
   *
   * @param s the string that is displayed in the list
   * @return into the (String) object that is displayed in the table cell
   */
  public static Object fromString(java.lang.String s) {
    switch (s) {
      //translate the non visible characters
      //back
      case SEP_NAME_NONE:
        return SEPARATOR_NONE;     //"break;" not necessary because "return" is used...
      case SEP_NAME_SPACE:
        return SEPARATOR_SPACE;
      //use the others as is
      default:
        return s;
    }
  }

  //implement TextFieldComboBox interface to map the TextField-Interface to ComboBox
  @Override
  public void setText(String newValue) {
    setValue(toString(newValue));
  }

  @Override
  public String getText() {
    return (String) fromString((String) getValue());
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
    return ((String) getValue()).length(); //is 0 or 1
  }
}
