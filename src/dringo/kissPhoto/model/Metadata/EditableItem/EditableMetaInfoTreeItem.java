package dringo.kissPhoto.model.Metadata.EditableItem;

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
 * This class wraps an EditableMetaInfoItem (i.e. an EditableTagItem (leaf), DirectoryItem or the MetaDataItem (root))
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-07 type column supported
 * @version 2021-03-20 First implementation
 * @since 2021-03-14
 */
public class EditableMetaInfoTreeItem extends TreeItem<EditableMetaInfoItem> {
  boolean childrenCached = false;

  public EditableMetaInfoTreeItem(EditableMetaInfoItem metaInfoItem)
  {
    super(metaInfoItem);
  }

  /**
   * A TreeItem is a leaf if it has no children. The isLeaf method may of
   * course be overridden by subclasses to support alternate means of defining
   * how a TreeItem may be a leaf, but the general premise is the same: a
   * leaf can not be expanded by the user, and as such will not show a
   * disclosure node or respond to expansion requests.
   *
   * @return true if this TreeItem has no children
   */
  @Override
  public boolean isLeaf() {
    return (getValue().isLeaf());
  }

  /**
   * cache the wrapped children list only on first access
   * see TreeItem docu: This method is called frequently, and
   * it is therefore recommended that the returned list be cached by any TreeItem implementations.
   * @return the (cached) list of children
   */
  @Override
  public ObservableList<TreeItem<EditableMetaInfoItem>> getChildren() {
    ObservableList<TreeItem<EditableMetaInfoItem>> children = super.getChildren();

    if (!childrenCached){
      //System.out.println("MetaInfoTreeItem.cacheChildren: " + getValue().getKeyString().getValue());
      childrenCached = true;   //fix: if this line is put after the next line a stack overflow occurs if branch expanded after focus lost...
      getValue().cacheEditableChildren(children);
    }

    return children;
  }

}
