package dringo.kissPhoto.helper;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.text.DecimalFormatSymbols;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * <p/>
 * String utilities.
 * These simple routines avoid to import the complete apache StringUtils-jar
 *
 * @author Dringo
 * @since 2012-10-04
 * @version 2020-12-20 repeat removed, because already contained in String
 */
public class StringHelper {

  /**
   * get the locale separator character from Platform or Default character if not supported by platform
   * on many platforms (except windows) there is no list character defined
   * to be compatible with excel the following strategy is used:
   * if the decimal separator is , then use ; else use , (like comma separated values indicate)
   */
  public static char getLocaleCSVSeparator() {
    if (new DecimalFormatSymbols().getDecimalSeparator() == ',') {
      return ';';
    } else {
      return ',';
    }
  }

  /**
   * Convert String to QuotedString: a leading and a trailing quote (") and double all all previously existing quotes
   *
   * @param aString: the String to be converted:
   * @return the converted string
   */
  public static String quote(String aString) {
    return "\"" + aString.replaceAll("\"", "\"\"") + "\"";
  }


  //Text-Width from
  //https://stackoverflow.com/questions/12737829/javafx-textfield-resize-to-text-length

  static final Text helper;
  static final double DEFAULT_WRAPPING_WIDTH;
  static final double DEFAULT_LINE_SPACING;
  static final String DEFAULT_TEXT;
  static final TextBoundsType DEFAULT_BOUNDS_TYPE;

  static {
    helper = new Text();
    DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
    DEFAULT_LINE_SPACING = helper.getLineSpacing();
    DEFAULT_TEXT = helper.getText();
    DEFAULT_BOUNDS_TYPE = helper.getBoundsType();
  }

  public static double computeTextWidth(Font font, String text, double help0) {
    // Toolkit.getToolkit().getFontLoader().computeStringWidth(field.getText(),
    // field.getFont());

    helper.setText(text);
    helper.setFont(font);

    helper.setWrappingWidth(0.0D);
    helper.setLineSpacing(0.0D);
    double d = Math.min(helper.prefWidth(-1.0D), help0);
    helper.setWrappingWidth((int) Math.ceil(d));
    d = Math.ceil(helper.getLayoutBounds().getWidth());

    helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
    helper.setLineSpacing(DEFAULT_LINE_SPACING);
    helper.setText(DEFAULT_TEXT);
    return d;
  }
}
