package dringo.kissPhoto.view.inputFields;

/**
 * Interface common to all Input Fields
 * either derived from restrictedTextField
 * or implementing TextFieldComboBox
 * Implementing classes must be derived from Node!!
 *
 * @author Ingo
 * @since 2016-11-01
 */
public interface RestrictedInputField {
  //as is in RestrictedTextField, mapped to setValue in ComboBox
  void setText(String newValue);

  String getText();

  void selectRange(int startPos, int endPos);

  void selectAll();

  void deselect();

  void positionCaret(int pos);

  int getCaretPosition();

  int getLength();

  void setMinWidth(double minWidth);

  void requestFocus();
}
