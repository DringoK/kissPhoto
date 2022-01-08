package dringo.kissPhoto.model.Metadata;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This class is the base for all wrapped metada classes to be shown in a TreeTableView
 * <li>Metadata object (subclass RootItem)</li>
 * <li>Directory (subclass DirectoryItem)</li>
 * <li>Tag (subclass TagItem)</li>
 * <p/>
 *
 * @author Dringo
 * @since 2021-03-14
 * @version 2021-11-07 id column supported
 * @version 2021-03-20 First implementation
 */

public abstract class MetaInfoItem {
  protected SimpleStringProperty keyString = null;
  protected SimpleStringProperty exifIDString = null;
  protected SimpleStringProperty valueString = null;

  /**
   * Metadata has Directories, Directories have Tags possibly as their children.
   * Tags have no children and are always leafs
   * @return true if this MetainfoItem has no children
   */
  public abstract boolean isLeaf();

  /**
   * read the children into the observable list to show them in the TreeTableView
   * @param children list of sub items
   */
  public abstract void cacheChildren(ObservableList<TreeItem<MetaInfoItem>> children);

  /**
   * @return the text that will be displayed in the tag column
   */
  public abstract StringProperty getTagString();

  /**
   * @return the text that will be displayed in the value column
   */
  public abstract StringProperty getValueString();

  /**
   * @return the text that will be displayed in the Tag-ID column
   */
  public abstract StringProperty getTagIDString();
}
