package dringo.kissPhoto.model.Metadata.EditableItem;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.MetaInfoItem;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import mediautil.image.jpeg.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This class is the base for all wrapped metada classes to be shown in a TreeTableView that are editable
 * <li>Metadata object (subclass RootItem)</li>
 * <li>Directory (subclass EditableDirectoryItem)</li>
 * <li>Tag (subclass EditableTagItem)</li>
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-13 First implementation: on an abstract level same as non editable
 * @since 2021-11-13
 */

public abstract class EditableMetaInfoItem extends MetaInfoItem {

  protected Exif exifHeader; //link to the Exif-Header read by media util
  protected MediaFileTaggedEditable mediaFile; //link to media file is used for synchronizing the changedMetaInfoItems list and for saving the changes

  public EditableMetaInfoItem(MediaFileTaggedEditable mediaFile, Exif exifHeader) {
    this.exifHeader = exifHeader;
    this.mediaFile = mediaFile;
  }

  /**
   * read the children into the oberservable list to show them in the TreeTableView
   * not used with editable directory/tag structure, because this structure is constant
   *
   * @param children list of children items
   */
  public abstract void cacheEditableChildren(ObservableList<TreeItem<EditableMetaInfoItem>> children);

  /**
   * read the children into the oberservable list to show them in the TreeTableView
   *
   * @param children  list of children items
   */
  @Override
  public void cacheChildren(ObservableList<TreeItem<MetaInfoItem>> children) {
    Exception e = new Exception("EditableMetaInfoItem.cacheChildren() must not be used with MetaInfoItems. Use cacheEditabelChildren instead!");
    e.printStackTrace();
  }

  /**
   * As soon as the user has entered a new value in MetaInfoEditableTagsView this method is called to save the value
   * @param newValue the value when the user ended edit the field
   */
  public void saveEditedValue(String newValue){
    //as a default: nothing to do. Overwritten in writable items, i.e. EditableTagItem
    //don't forget to call mediaFile.updateStatusProperty(); after saving the changes
  }

}
