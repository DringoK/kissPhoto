package dringo.kissPhoto.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
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
 * @author Dringo
 * @since 2013-10-09
 * @version 2020-11-06 changed to XML format to support UTF-8 encoding
 *
 */
public class GlobalSettings extends Properties {
  private static final String PROPERTIES_FOLDERNAME = ".kissPhoto";
  private static final String PROPERTIES_FILENAME = "kissPhoto.settings";

  private final String propertiesFilename;


  public GlobalSettings() {

    //determine the settings-path on this OS
    Path folderPath = getOSUserSettingsDirectory(); //is never null
    Path propertiesFilePath = folderPath.resolve(PROPERTIES_FOLDERNAME);

    try { //ignore IO-Exceptions

      //make a new settings dir if not already existing
      if (Files.notExists(propertiesFilePath))
        Files.createDirectory(propertiesFilePath);



    }catch (IOException e){
      //ignore if writing was not possible
    }

    propertiesFilename = propertiesFilePath.resolve(PROPERTIES_FILENAME).toString();
    System.out.println("Settings-Path=" + propertiesFilename);     //leave this information, so that anyone can find the setting-file

  }

  /**
   * restore settings if possible
   */
  public void load() {
    try {
      loadFromXML(new FileInputStream(propertiesFilename));
    } catch (Exception e) {
      //the default values will be set by the classes who use globalSettings for storing their Settings
    }
  }

  /**
   * remember settings if possible
   */
  public void store() {
    try {
      storeToXML(new FileOutputStream(propertiesFilename), null);
    } catch (Exception e) {
      //ignore if writing was not possible
    }
  }

  public static Path getOSUserSettingsDirectory()
  {
    String appdataPath;

    String OS = System.getProperty("os.name").toUpperCase();
    if (OS.contains("WIN"))
      appdataPath =  System.getenv("APPDATA");
    else if (OS.contains("MAC"))
      appdataPath = System.getProperty("user.home") + "/Library/Application Support";
    else if (OS.contains("NUX"))
      appdataPath = System.getProperty("user.home");
    else appdataPath= System.getProperty("user.dir");

    return Paths.get(appdataPath);
  }
}
