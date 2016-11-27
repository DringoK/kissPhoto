package de.kissphoto.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * store and restore global settings such as the last opened file or folder
 * <p/>
 * <p/>
 * Every class can set Properties at every time.
 * Every class can get Properties in a try statement and in catch set the default value
 * <p/>
 * <p/>
 * load is invoked at startup in KissPhoto start method
 * store is invoked in KissPhoto in the setOnCloseRequest Event Handle
 * <p/>
 * User: Ingo
 * Date: 09.10.13
 */
public class GlobalSettings extends Properties {
  private static final String PROPERTIES_FILENAME = "KissPhoto.settings";


  public GlobalSettings() {
  }

  /**
   * restore settings if possible
   */
  public void load() {
    try {
      load(new FileInputStream(PROPERTIES_FILENAME));
    } catch (Exception e) {
      //the default values will be set by the classes who use globalSettings for storing their Settings
    }
  }

  /**
   * remember settings if possible
   */
  public void store() {
    try {
      store(new FileOutputStream(PROPERTIES_FILENAME), null);
    } catch (Exception e) {
      //ignore if writing was not possible
    }
  }
}
