package dringo.kissPhoto.helper;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * <p/>
 * provide the language resource bundle for all classes of kissphoto
 * @author Dringo
 * @version 2021-11-06 language resource bundle moved to resources
 * @since 2014-05-02
 */
public class I18Support {
  public static final String GERMAN = "de";
  public static final String ENGLISH = "en";

  public static final String LANGUAGE = "language";

  private static final String LANGUAGE_BUNDLE_NAME = "languages.kissphoto";

  public static ResourceBundle languageBundle = ResourceBundle.getBundle(LANGUAGE_BUNDLE_NAME, new Locale(ENGLISH));  //default is english
  public static String currentLanguage = ENGLISH;

  /**
   * change the language setting from the default ENGLISH
   *
   * @param countryCodeISO639_1 use one of the constants provided with this class (e.g. GERMAN, ENGLISH)
   */
  public static void setLanguage(String countryCodeISO639_1) {
    languageBundle = ResourceBundle.getBundle(LANGUAGE_BUNDLE_NAME, new Locale(countryCodeISO639_1));
    currentLanguage = countryCodeISO639_1;
  }

}

