package de.kissphoto.view.inputFields;

/**
 * Interface common to all Input Fields
 * either derived from restrictedTextField
 * or implementing TextFieldComboBox
 * Implementing classes must be derived from Node!!
 *
 * @date 1.11.2016
 * @user Ingo
 */
public interface RestrictedInputField {
  //as is in RestrictedTextField, mapped to setValue in ComboBox
  public void setText(String newValue);

  public String getText();

  public void selectRange(int startPos, int endPos);

  public void selectAll();

  public void deselect();

  public void positionCaret(int pos);

  public int getCaretPosition();

  public int getLength();

  public void setMinWidth(double minWidth);

  public void requestFocus();
}
