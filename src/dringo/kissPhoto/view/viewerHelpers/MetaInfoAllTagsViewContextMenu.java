package dringo.kissPhoto.view.viewerHelpers;

import dringo.kissPhoto.view.MetaInfoAllTagsView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This is the Context Menu for MetaInfoContextMenu for the tab with all already contained tag entries
 * </p>
 *
 *
 * @author Ingo
 * @since 2021-11-06
 * @version 2021-11-06 initial version
 */
public class MetaInfoAllTagsViewContextMenu extends ContextMenu {
  MetaInfoAllTagsView metaInfoAllTagsView; //link to corresponding view

  /**
   * Create a new ContextMenu
   */
  public MetaInfoAllTagsViewContextMenu(MetaInfoAllTagsView metaInfoAllTagsView) {
    this.metaInfoAllTagsView = metaInfoAllTagsView;
    addContextMenuItems();
    setAutoHide(true);
  }


  /**
   * Build all ContextMenu items for FileTable
   */
  public void addContextMenuItems() {
    final MenuItem addTagtoFileTableItem = new MenuItem(language.getString("show.current.tag.in.file.table.s.column"));
    //addTagtoFileTableItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    addTagtoFileTableItem.setOnAction(event -> {
      event.consume();
      metaInfoAllTagsView.addCurrentTagToFileTable();
    });
    getItems().add(addTagtoFileTableItem);

    final MenuItem editItemInEditableView = new MenuItem(language.getString("edit.tag"));
    editItemInEditableView.setAccelerator(new KeyCodeCombination(KeyCode.F2));
    editItemInEditableView.setOnAction(event -> {
      event.consume();
      metaInfoAllTagsView.startEditInEditableTab();
    });
    getItems().add(editItemInEditableView);
  }


}
