package dringo.kissPhoto.model.Metadata.EditableItem;

import dringo.kissPhoto.model.Metadata.EditableTagItem.EditableTagItemFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import mediautil.image.jpeg.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This class wraps mediautils Exif (AbstractImageInfo<?>) to enable showing and edit it in a TreeTableView
 * A MetadataItem will be the root node for the MetaInfoEditableTagsView
 * It consists of Directories (EditableDirectoryItem) which again consist of Tags (EditableTagItem)
 *
 * Because AbstractImageInfo does not define Directories a Directory/Tag Tree is built here grouping editable tags
 * plus a dirctory "misc" that is empty in the beginning, but editable tags can be added
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-13 First implementation
 * @since 2021-11-13
 */

public class EditableMetadataItem extends EditableMetaInfoItem {
  Exif imageInfo; //link to the media util object to be wrapped
  ObservableList<EditableDirectoryItem> directories = FXCollections.observableArrayList(); //use Factory to generate empty list
  /**
   * Constructor to wrap an imageInfo object
   * @param imageInfo The object to be wrapped
   */
  public EditableMetadataItem(Exif imageInfo) {
    keyString = new SimpleStringProperty("EditableMetadata");
    this.imageInfo = imageInfo;

    //build Directory Structure
    EditableDirectoryItem directoryItem;

    directoryItem = new EditableDirectoryItem("Image Description");
    directoryItem.addTag(EditableTagItemFactory.getTag(Exif.DOCUMENTNAME, imageInfo));
    directoryItem.addTag(EditableTagItemFactory.getTag(Exif.IMAGEDESCRIPTION, imageInfo));
    directories.add(directoryItem);
    directoryItem = new EditableDirectoryItem("Copyright");
    directories.add(directoryItem);
    directoryItem = new EditableDirectoryItem("Date Time");
    directories.add(directoryItem);
    directoryItem = new EditableDirectoryItem("Misc");
    directories.add(directoryItem);
  }

  /**
   * ImageInfo has Directories as its children.
   * @return true if this MetadataItem has no children
   */
  @Override
  public boolean isLeaf() {
    return directories.size()==0;
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
      valueString = new SimpleStringProperty("(" + directories.size() + ")");
    }
  return valueString;
  }

  /**
   * read the children into the oberservable list to show them in the TreeTableView
   * not used with editable directory/tag structure, because this structure is constant
   *
   * @param children list of sub items
   */
  @Override
  public void cacheEditableChildren(ObservableList<TreeItem<EditableMetaInfoItem>> children) {
    if (directories!=null) {
      directories.forEach((dir)-> children.add(new EditableMetaInfoTreeItem(dir)));
    }
  }

  /**
   * @return the text that will be displayed in the type column
   */
  @Override
  public StringProperty getExifIDString() {
    return null;
  }
}
