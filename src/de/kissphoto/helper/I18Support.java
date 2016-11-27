package de.kissphoto.helper;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Ingo on 02.05.2014.
 * <p/>
 * provide the language resource bundle for all classes of kissphoto
 */
public class I18Support {
  public static final String GERMAN = "de";
  public static final String ENGLISH = "en";

  public static final String LANGUAGE = "language";

  private static final String LANGUANGE_BUNDLE_NAME = "de.kissphoto.languages.kissphoto";

  public static ResourceBundle languageBundle = ResourceBundle.getBundle(LANGUANGE_BUNDLE_NAME, new Locale(ENGLISH));  //default is english
  public static String currentLanguage = ENGLISH;

  /**
   * change the language setting from the default ENGLISH
   *
   * @param countryCodeISO639_1 use one of the constants provided with this class (e.g. GERMAN, ENGLISH)
   */
  public static void setLanguage(String countryCodeISO639_1) {
    languageBundle = ResourceBundle.getBundle(LANGUANGE_BUNDLE_NAME, new Locale(countryCodeISO639_1));
    currentLanguage = countryCodeISO639_1;
  }

}

