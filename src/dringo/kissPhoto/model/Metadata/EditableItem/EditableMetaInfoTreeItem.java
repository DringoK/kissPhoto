package dringo.kissPhoto.model.Metadata.EditableItem;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems.EditableTagItem;
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
 * This class wraps an EditableMetaInfoItem (i.e. an EditableTagItem (leaf), DirectoryItem or the RootItem)
 * <p/>
 *
 * @author Dringo
 * @since 2021-11-15
 * @version 2022-01-07 first working version
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

  /**
   * recursive search for tagID
   * @param tagID the wanted tagID
   * @return the found treeItem or null if not found
   */
  public EditableMetaInfoTreeItem searchForTag(int tagID){
    EditableMetaInfoItem item = getValue();
    //found: I am the wanted TreeItem
    if (item instanceof EditableTagItem && ((EditableTagItem)item).getTagID() == tagID)
      return this;

    //not found: search in subcomponents
    ObservableList<TreeItem<EditableMetaInfoItem>> children = getChildren();

    //no children-->stop searching here
    if (children == null) return null;

    //has children: continue search in children
    EditableMetaInfoTreeItem result = null;
    for (TreeItem<EditableMetaInfoItem>child:children){
      result=((EditableMetaInfoTreeItem) child).searchForTag(tagID);
      if (result!=null)
        break;
    }
    return result;
  }

}
