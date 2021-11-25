package dringo.kissPhoto.model.Metadata.EditableItem;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems.EditableTagItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class is represents a directory in MetaInfoEditableTabsView to be shown in a TreeTableView
 * An EditableDirectory consists of a list of EditableTags
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-10
 * @since 2021-11-10
 */

public class EditableDirectoryItem extends EditableMetaInfoItem {
  ObservableList<EditableTagItem> tags; //children list is set

  /**
   * Constructor to wrap a Directory object
   * @param directoryName The Name that will be displayed in the treeTable's branch
   */
  public EditableDirectoryItem(String directoryName){
    keyString = new SimpleStringProperty(directoryName);
    tags = FXCollections.observableArrayList();
  }

  /**
   * media util does not support a directory or group structure
   * with the help of addTag tags can be added to the directory to build up a structure manually
   * @param editableTagItem the tag to be added to the directory manually
   */
  public void addTag(EditableTagItem editableTagItem){
    tags.add(editableTagItem);
  }

  /**
   * Directories have Tags possibly as their children.
   *
   * @return true if this DirectoryItem has no children
   */
  @Override
  public boolean isLeaf() {
    return tags.size()==0;
  }

  /**
   * @return the text that will be displayed in the tree
   */
  @Override
  public StringProperty getKeyString() {
    return keyString;
  }

  /**
   * read the value once from the directory then use the cached value
   * @return the text that can be displayed in the value column (if the node is not expanded)
   */
  @Override
  public StringProperty getValueString() {
    if (valueString==null){
      valueString = new SimpleStringProperty("("+tags.size()+")");
    }
    return valueString;
  }

  /**
   * @return the text that will be displayed in the Exif-ID column
   */
  @Override
  public StringProperty getExifIDString() {
    return null;
    //return new SimpleStringProperty("");
  }

  /**
   * read the children into the oberservable list to show them in the TreeTableView
   * not used with editable directory/tag structure, because this structure is constant
   *
   * @param children list of tag items contained in this directory
   */
  @Override
  public void cacheEditableChildren(ObservableList<TreeItem<EditableMetaInfoItem>> children) {
    if (tags!=null) {
      tags.forEach((tag)-> children.add(new EditableMetaInfoTreeItem(tag)));
    }
  }
}
