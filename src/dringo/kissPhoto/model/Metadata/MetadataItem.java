package dringo.kissPhoto.model.Metadata;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
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
 * <p>
 * This class wraps com.drew.metadata.Metadata to enable showing it in a TreeTableView
 * A MetadataItem will be the root node for the MetaDataViewer
 * <p/>
 *
 * @author Dringo
 * @version 2021-03-20 First implementation
 * @since 2021-03-14
 */

public class MetadataItem extends MetaInfoItem{
  Metadata metadata = null; //link to the object to be wrapped
  /**
   * Constructor to wrap a Metadata object
   * @param metadata The object to be wrapped
   */
  public MetadataItem(Metadata metadata) {
    keyString = new SimpleStringProperty("Metadata");
    this.metadata = metadata;
  }

  /**
   * Metadata has Directories as its children.
   *
   * @return true if this MetadataItem has no children
   */
  @Override
  public boolean isLeaf() {
    if (metadata!=null){
      return metadata.getDirectoryCount()==0;
    }else {
      return true; //if the wrapped Metadata object is not existent it has no children
    }

  }

  /**
   * children of a MetadataItem are DirectoryItems
   * read list of children to cache them
   * @param children
   */
  @Override
  public void cacheChildren(ObservableList<TreeItem<MetaInfoItem>> children) {
    if (metadata!=null )
      for (Directory directory : metadata.getDirectories()){
        children.add(new MetaInfoTreeItem(new DirectoryItem(directory)));
      }
  }

  /**
   * @return the text that will be displayed in the tree column
   */
  @Override
  public StringProperty getKeyString() {
    return keyString;
  }

  /**
   * read the value once from metadata, then use the cached value
   * @return the text that can be displayed in the value column (if the node is not expanded)
   */
  @Override
  public StringProperty getValueString() {
    if (valueString == null) {
      if (metadata != null)
        valueString = new SimpleStringProperty("(" + metadata.getDirectoryCount() + ")");
      else
        valueString = new SimpleStringProperty("");
    }
  return valueString;
  }
}
