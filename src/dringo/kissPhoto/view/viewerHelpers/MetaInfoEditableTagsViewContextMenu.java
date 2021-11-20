package dringo.kissPhoto.view.viewerHelpers;

import dringo.kissPhoto.view.MetaInfoEditableTagsView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This is the Context Menu for MetaInfoContextMenu for the tab with the editable tag entries
 * </p>
 *
 *
 * @author Ingo
 * @version 2021-11-06 initial version
 * @since 2021-11-06
 */
public class MetaInfoEditableTagsViewContextMenu extends ContextMenu {
  MetaInfoEditableTagsView metaInfoEditableTagsView; //link to corresponding view

  /**
   * Create a new ContextMenu
   */
  public MetaInfoEditableTagsViewContextMenu(MetaInfoEditableTagsView metaInfoEditableTagsView) {
    this.metaInfoEditableTagsView = metaInfoEditableTagsView;
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
      metaInfoEditableTagsView.addCurrentTagToFileTable();
    });
    getItems().add(addTagtoFileTableItem);

  }


}
