package dringo.kissPhoto.view.viewerHelpers;

import dringo.kissPhoto.view.MetaInfoEditableTagsView;
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
    final MenuItem editItem = new MenuItem(language.getString("edit.tag"));
    editItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
    editItem.setOnAction(event -> {
      event.consume();
      metaInfoEditableTagsView.editCurrentTag();
    });
    getItems().add(editItem);

    final MenuItem editItemInEditableView = new MenuItem(language.getString("show.tag.in.the.contained.tags.tab"));
    editItemInEditableView.setAccelerator(new KeyCodeCombination(KeyCode.F3));
    editItemInEditableView.setOnAction(event -> {
      event.consume();
      metaInfoEditableTagsView.showTagInAllTagsView();
    });
    getItems().add(editItemInEditableView);

  }


}
