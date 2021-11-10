package dringo.kissPhoto.model.Metadata;

import com.drew.metadata.Directory;
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
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class wraps com.drew.metadata.Directory to enable showing it in a TreeTableView
 * A Directory consists of a list of Tags
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-07 type column supported
 * @version 2021-03-20 First implementation
 * @since 2021-03-14
 */

public class DirectoryItem extends MetaInfoItem {
  Directory directory = null; //link the wrapped Directory

  /**
   * Constructor to wrap a Directory object
   * @param directory The object to be wrapped
   */
  DirectoryItem(Directory directory){
    this.directory = directory;
  }

  /**
   * Directories have Tags possibly as their children.
   *
   * @return true if this DirectoryItem has no children
   */
  @Override
  public boolean isLeaf() {
    if (directory!=null){
      return directory.getTagCount()==0;
    }else {
      return true; //if the wrapped Directory object is not existent it has no children
    }
  }

  /**
   * children of a DirectoryItem are TagItems
   * read list of children to cache them
   * @param children
   */
  @Override
  public void cacheChildren(ObservableList<TreeItem<MetaInfoItem>> children) {
    if (directory!=null) {
      for (Tag tag : directory.getTags()) {
        children.add(new MetaInfoTreeItem(new TagItem(tag)));
      }
    }
  }

  /**
   * read the keyName once from the directory then use the cached value
   * @return the text that will be displayed in the tree column
   */
  @Override
  public StringProperty getKeyString() {
    if (keyString == null){
      if (directory!=null)
        keyString = new SimpleStringProperty(directory.getName());
      else
        keyString = new SimpleStringProperty("<dir>");
    }
    return keyString;
  }

  /**
   * read the value once from the directory then use the cached value
   * @return the text that can be displayed in the value column (if the node is not expanded)
   */
  @Override
  public StringProperty getValueString() {
    if (valueString==null){
      if (directory!=null)
        valueString = new SimpleStringProperty("("+directory.getTagCount()+")");
      else
        valueString = new SimpleStringProperty("");
    }
    return valueString;
  }

  /**
   * @return the text that will be displayed in the type column
   */
  @Override
  public StringProperty getTypeString() {
    return null;
    //return new SimpleStringProperty("");
  }
}
