package de.kissphoto.view.fileTableHelpers;

import de.kissphoto.helper.GlobalSettings;
import de.kissphoto.helper.I18Support;
import de.kissphoto.view.FileTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The "recently opened files' list is managed here
 * <p/>
 *
 * @author: Dr Ingo Kreuz
 * @date: 2017-10-22
 * @modified:
 */
public class FileHistory {
  private static final int MAX_ENTRIES = 9; //collect a maximum number of history entries
  private ObservableList<Path> recentlyOpenedList = FXCollections.observableArrayList();
  GlobalSettings globalSettings = null;
  FileTableView fileTableView = null;

  Menu mainMenuItem = null;
  private static final String LAST_FILE_OPENED = "lastFileOpened"; //constant for key in .setting file (a number 1..MAX_ENTRIES is added)

  //string constants (i18alized) for table columns' headlines
  private static ResourceBundle language = I18Support.languageBundle;


  public FileHistory(GlobalSettings globalSettings, FileTableView fileTableView) {
    this.globalSettings = globalSettings;
    this.fileTableView = fileTableView;

    mainMenuItem = new Menu(language.getString("open.recent"));

    loadRecentlyOpenedListFromSettings();
  }

  public Menu getRecentlyFilesMenu() {
    return mainMenuItem;
  }

  public ObservableList<Path> getRecentlyOpenedList() {
    return recentlyOpenedList;
  }

  /**
   * put a file to history just after opening
   * remove duplicate if the path of the file is already in the list
   *
   * @param openedFile
   */
  public void putOpenedFileToHistory(Path openedFile) {
    recentlyOpenedList.add(0, openedFile); //the latest is the first

    //remove duplicate path (not file)
    Path pathOnly = openedFile.getParent();
    int i = 1; //start looking for duplicates from the second on (because index 0 has been just added)
    while (i < recentlyOpenedList.size()) {
      if (recentlyOpenedList.get(i).getParent().equals(pathOnly)) {
        recentlyOpenedList.remove(i);
      } else {
        i++;
      }
    }
    //restrict to MAX_ENTRIES
    while (recentlyOpenedList.size() > MAX_ENTRIES) {
      recentlyOpenedList.remove(MAX_ENTRIES - 1);
    }
    saveRecentlyOpenedListToSettings();
    putRecentlyOpenedListIntoMenu();
  }

  /**
   * refresh the latest entry when closing kissPhoto, so that current selection is updated
   *
   * @param openedFile
   */
  public void refreshOpenedFileInHistory(Path openedFile) {
    recentlyOpenedList.set(0, openedFile); //the latest is the first
    saveRecentlyOpenedListToSettings();
    putRecentlyOpenedListIntoMenu();
  }

  private void loadRecentlyOpenedListFromSettings() {
    try { //try to load as many entries as possible
      for (int i = 1; i <= MAX_ENTRIES; i++) {
        recentlyOpenedList.add(Paths.get(globalSettings.getProperty(LAST_FILE_OPENED + i)));
      }
    } catch (Exception e) {
      //consume the exception
    } finally {
      putRecentlyOpenedListIntoMenu();
    }
  }

  private void saveRecentlyOpenedListToSettings() {
    int i = 1;
    for (Path entry : recentlyOpenedList) {
      globalSettings.setProperty(LAST_FILE_OPENED + i, entry.toAbsolutePath().toString());
      i++;
    }
  }

  private void putRecentlyOpenedListIntoMenu() {
    MenuItem item;
    mainMenuItem.getItems().clear();
    int i = 0;
    for (Path entry : recentlyOpenedList) {
      Path p = entry.getParent().getFileName(); //get the parent directory
      if (p == null)
        item = new MenuItem(entry.getParent().toString());  //complete path (=root) if there is no parent
      else
        item = new MenuItem("..." + File.separator + p.getFileName().toString() + File.separator); //only ... + containing directory

      if (i < 10)  //add accelerator (ctrl+0, ctrl+1, ...) only for the first 10 (because there are not more number keys ;-)
        item.setAccelerator(new KeyCodeCombination(KeyCode.getKeyCode(Integer.toString(i)), KeyCombination.CONTROL_DOWN));

      item.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          fileTableView.openFolder(entry, true);
        }
      });

      mainMenuItem.getItems().add(item);
      i++;
    }
  }


}
