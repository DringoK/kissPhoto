package dringo.kissPhoto.view.viewerHelpers;

import dringo.kissPhoto.view.MetaInfoView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This is the Context Menu for MetaInfoContextMenu
 * </p>
 *
 *
 * @author Ingo
 * @since 2021-11-06
 * @version 2021-01-06 initial version
 */
public class MetaInfoViewContextMenu extends ContextMenu {
  MetaInfoView metaInfoView; //link to corresponding view

  /**
   * Create a new ContextMenu
   */
  public MetaInfoViewContextMenu(MetaInfoView metaInfoView) {
    this.metaInfoView = metaInfoView;
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
      metaInfoView.addCurrentTagToFileTable();
    });
    getItems().add(addTagtoFileTableItem);

  }


}
