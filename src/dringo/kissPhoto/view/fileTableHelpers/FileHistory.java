package dringo.kissPhoto.view.fileTableHelpers;

import dringo.kissPhoto.view.FileTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dringo.kissPhoto.KissPhoto.globalSettings;
import static dringo.kissPhoto.KissPhoto.language;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The "recently opened files' list is managed here
 * <p/>
 *
 * @author Dr Ingo Kreuz
 * @since 2017-10-22
 * @version 2020-12-20 globalSettings and language are now static in KissPhoto, lambda expressions for event handlers
 * @version 2017-10-22
 */
public class FileHistory {
  private static final int MAX_ENTRIES = 9; //collect a maximum number of history entries
  private final ObservableList<Path> recentlyOpenedList = FXCollections.observableArrayList();
  FileTableView fileTableView;
  Menu mainMenuItem;

  private static final String LAST_FILE_OPENED = "lastFileOpened"; //constant for key in .setting file (a number 1..MAX_ENTRIES is added)

  //string constants (i18alized) for table columns' headlines


  public FileHistory(FileTableView fileTableView) {
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
   * @param openedFile the file to be put to file history (open recent)
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
   * @param openedFile the currently selected file to be updated in file history
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
      Path p = entry.getParent().getFileName(); //get the mediaFileList directory
      if (p == null)
        item = new MenuItem(entry.getParent().toString());  //complete path (=root) if there is no mediaFileList
      else
        item = new MenuItem("..." + File.separator + p.getFileName().toString() + File.separator); //only ... + containing directory

      if (i < 10)  //add accelerator (ctrl+0, ctrl+1, ...) only for the first 10 (because there are not more number keys ;-)
        item.setAccelerator(new KeyCodeCombination(KeyCode.getKeyCode(Integer.toString(i)), KeyCombination.CONTROL_DOWN));

      item.setOnAction(event -> fileTableView.openFolder(entry, true));

      mainMenuItem.getItems().add(item);
      i++;
    }
  }


}
