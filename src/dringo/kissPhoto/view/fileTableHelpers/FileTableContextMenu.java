package dringo.kissPhoto.view.fileTableHelpers;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.FileTableView;
import dringo.kissPhoto.view.MainMenuBar;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This is the Context Menu for File Table
 * <p>
 * some menu items are "copied" from Main Menu to the context menu. Therefore string constants from MainMenu are used
 * <p/>
 *
 * @author Ingo
 * @since 2021-11-01
 * @version 2021-01-01 initial version
 */
public class FileTableContextMenu extends ContextMenu {
  FileTableView fileTableView; //link to corresponding FileTable

  /**
   * Create a new ContextMenu
   */
  public FileTableContextMenu(FileTableView fileTableView) {
    this.fileTableView = fileTableView;
    addContextMenuItems();
  }


  /**
   * Build all ContextMenu items for FileTable
   */
  public void addContextMenuItems() {
    final MenuItem autoNumberItem = new MenuItem(KissPhoto.language.getString(MainMenuBar.RENUMBER_STANDARD_MENU));
    autoNumberItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    autoNumberItem.setOnAction(event -> {
      event.consume();
      fileTableView.renumberSelectionStandard();
    });
    getItems().add(autoNumberItem);

    final MenuItem cleanCountersItem = new MenuItem(KissPhoto.language.getString(MainMenuBar.CLEAN_COUNTERS));
    cleanCountersItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    cleanCountersItem.setOnAction(event -> {
      event.consume();
      fileTableView.cleanCounter();
    });
    getItems().add(cleanCountersItem);



    getItems().add(new SeparatorMenuItem());

    final MenuItem renameItem = new MenuItem(KissPhoto.language.getString(MainMenuBar.RENAME_MENU));
    renameItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
    renameItem.setOnAction(event -> {
      event.consume();
      fileTableView.rename();
    });
    getItems().add(renameItem);

    final MenuItem cleanPrefixItem = new MenuItem(KissPhoto.language.getString(MainMenuBar.CLEAN_PREFIXES));
    cleanPrefixItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    cleanPrefixItem.setOnAction(event -> {
      event.consume();
      fileTableView.cleanPrefix();
    });
    getItems().add(cleanPrefixItem);


  }


}
