package dringo.kissPhoto.view.inputFields;

import javafx.stage.Stage;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is an input field (TextField) which accepts numbers (digits) with maximum one slash only
 * A tooltip is shown with explanation which characters are allowed if invalid characters are tried to enter
 * Invalid characters are ignored.
 *
 * @author Dringo
 * @since 2022-02-20
 * @version 2022-02-20 initial version
 */
public class FractionTextField extends NumberWithSeparatorTextField {
  private static final String MESSAGE = "a.fraction.numerator.denominator.may.contain.only.digits.and.one.slash";
  public static final char SEPARATOR = '/';

  //@constructor
  public FractionTextField(Stage stage) {
    super(SEPARATOR, MESSAGE, stage);
  }

  public FractionTextField(String initialText, Stage stage) {
    super(initialText, SEPARATOR, MESSAGE, stage);
  }

}
