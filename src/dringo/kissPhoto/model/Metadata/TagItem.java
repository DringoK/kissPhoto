package dringo.kissPhoto.model.Metadata;

import com.drew.metadata.Tag;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class wraps com.drew.metadata.Tag to enable showing it in a TreeTableView
 * A Tag resides in a Directory and has no children
 * <p/>
 *
 * @author Dringo
 * @version 2021-03-20 First implementation
 * @since 2021-03-14
 */

public class TagItem extends MetaInfoItem {
  Tag tag = null; //link to the object to be wrapped
  /**
   * Constructor to wrap a Tag object
   * @param tag The object to be wrapped
   */
  public TagItem(Tag tag) {
    this.tag = tag;
  }

  /**
   * Metadata has Directories, Directories have Tags possibly as their children.
   * Tags have no children and are always leafs
   *
   * @return true if this MetainfoItem has no children
   */
  @Override
  public boolean isLeaf() {
    return true;
  }

  /**
   * a tag has no children
   * @param children
   */
  @Override
  public void cacheChildren(ObservableList<TreeItem<MetaInfoItem>> children) {
    //nothing to do as a tag has no children
  }

  /**
   * read the keyName once from the tag then use the cached value
   * @return the text that will be displayed in the tree column
   */
  @Override
  public StringProperty getKeyString() {
    if (keyString==null){
      if (tag != null)
        keyString = new SimpleStringProperty(tag.getTagName());
      else
        keyString = new SimpleStringProperty("");
    }
    return keyString;
  }

  /**
   *  read the value once from the tag then use the cached value
   *  @return the text that will be displayed in the value column
   */
  @Override
  public StringProperty getValueString() {
    if (valueString == null){
      if (tag != null)
        valueString = new SimpleStringProperty(tag.getDescription());
      else
        valueString = new SimpleStringProperty("");
    }
    return valueString;
  }
}
