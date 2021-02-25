package dringo.kissPhoto.helper;

import dringo.kissPhoto.model.MediaFile;
import javafx.collections.ObservableList;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto: keep it simple stupid when organizing your photos
 * <p/>
 * This helper class is for launching external applications
 * It uses cmd /c on windows plattforms if no program is specified and with this the standard program for the mime type
 * is used for the media files
 * <p/>
 * This solution for savely starting external programs is based on the article of Michael C. Daconta:
 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
 * <p/>
 * @author Dringo
 * @since 2014-05-17
 * @version 2014-06-05 java.io operations changed into java.nio
 */
public class AppStarter {
  public static void exec(String pathToProgram, ObservableList<MediaFile> selection) {
    String[] cmd;
    try {
      int paramIndex;
      if (pathToProgram != null && !pathToProgram.isEmpty()) {
        //build normal call as everything is provided
        cmd = new String[selection.size() + 1]; //
        cmd[0] = pathToProgram;
        paramIndex = 1; //start copying the selection after the command
      } else {
        //no program specified --> in windows use cmd.exe /c in other OSs just call the selection (whatever will happen then
        if (isWindowsSystem()) {
          cmd = new String[selection.size() + 2];
          cmd[0] = "cmd.exe";
          cmd[1] = "/C";
          paramIndex = 2;
        } else {
          cmd = new String[selection.size()];
          paramIndex = 0;
        }
      }
      for (MediaFile mediaFile : selection) {
        cmd[paramIndex] = "\"" + mediaFile.getFileOnDisk().toAbsolutePath().toString() + "\"";
        paramIndex++;
      }


      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec(cmd);

      new StreamGobbler(proc.getErrorStream()).start(); //any error message to consume?
      new StreamGobbler(proc.getInputStream()).start(); //any output to consume?

      // any error???
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }


  /**
   * @return true if the current os.name contains "windows"
   */
  private static boolean isWindowsSystem() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("windows");
  }

  /**
   * @return true if the current os.name contains "linux"
   */
  private static boolean isLinuxSystem() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("linux");
  }


  /**
   * this internal stream consumes (gobbles) all output from the external program
   * this prevents the external program to hang (waiting for a consumer receiving any output)
   */

  static class StreamGobbler extends Thread {
    InputStream is;

    StreamGobbler(InputStream is) {
      this.is = is;
      setDaemon(true);  //close when main process is closed
    }

    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        while ((br.readLine()) != null) {
          //consume all input  }
        }
      } catch (IOException ioe) {
        //ignore failures as we are not interested in the output of the program ;-)
      }
    }
  }

  /**
   * start the external default browser if possible
   * seen on stackoverflow:
   * https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java
   * @param url the address to be loaded from the browser
   */
  public static void tryToBrowse(String url){
    if(Desktop.isDesktopSupported()){
      Desktop desktop = Desktop.getDesktop();
      try {
        desktop.browse(new URI(url));
      } catch (IOException | URISyntaxException e) {
        //e.printStackTrace();
      }
    }else{
      Runtime runtime = Runtime.getRuntime();
      try {
        runtime.exec("xdg-open " + url);
      } catch (IOException e) {
        //e.printStackTrace();
      }
    }
  }


}
