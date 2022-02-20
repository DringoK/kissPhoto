package dringo.kissPhoto.view.inputFields;

import javafx.stage.Stage;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (TextField) which accepts numbers (digits) with maximum one decimal point only
 * A tooltip is shown with explanation which characters are allowed if invalid characters are tried to enter
 * Invalid characters are ignored.
 *
 * @author Dringo
 * @since 2022-01-14
 * @version 2022-02-20 common logic with FractionTextField moved to NumberWithSeparatorTextField
 */
public class RationalTextField extends NumberWithSeparatorTextField {
  private static final String MESSAGE = "a.number.may.contain.only.digits.and.one.decimal.point";
  private static final char SEPARATOR = '.';

  //@constructor
  public RationalTextField(Stage stage) {
    super(SEPARATOR, MESSAGE, stage);
  }

  public RationalTextField(String initialText, Stage stage) {
    super(initialText, SEPARATOR, MESSAGE, stage);
  }

}
