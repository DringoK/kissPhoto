package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import dringo.kissPhoto.model.Metadata.Exif.ExifTagInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class represents an editable Tag Entry in MetaInfoEditableTagsView
 * An EditableTag resides in an EditableDirectory and has no children
 * It wraps an Entry-object of mediautil (which would be called "Tag" in meta-data-extractor, which is used in the read-only version of TagItem)
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-10 First implementation
 * @since 2021-11-10
 */

public abstract class EditableTagItem extends EditableMetaInfoItem {
  private final ExifTagInfo exifTagInfo;  //link to the entry in the list of editable exif tags, to find out, how it can be edited etc.
  private String originalValueString;     //the value, of the item as it is currently saved, to find out, if the value has changed. Null, if the exif tag/value didn't exist until now

  /**
   * Constructor to wrap an Entry object
   * if the entry exists in exifHeader then the value is loaded
   * if it is new then an empty entry is generated and newEntry=true
   *
   * @param exifTagInfo The object to be wrapped
   */
  public EditableTagItem(MediaFileTaggedEditable mediaFile, Exif exifHeader, ExifTagInfo exifTagInfo) {
    super(mediaFile, exifHeader);
    this.exifTagInfo = exifTagInfo;

    //try to load the tag entry if it already exists
    Entry entry = exifHeader.getTagValue(exifTagInfo.getId(), true);

    //generate a new entry
    if (entry == null) {
      originalValueString = null;   //indicates a new value
      //entry and exifHeader remain being null and will be generated on save only
    } else {
      //remember the original value to find out, if the value has been changed and needs to be written to the file
      initValueFromEntry(entry);
      originalValueString = getValueString().get();
    }
  }

  public boolean isNewEntry() {
    return originalValueString == null;
  }

  public boolean isChanged() {
    if (valueString == null) return false; //not yet edited
    else if (valueString.get().isEmpty() && originalValueString == null) return false; //empty==null
    else
      return !valueString.get().equals(originalValueString); //note: if originalStringValue==null 'equals' will return false
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
   * read the keyName once from the tagInfo then use the cached value
   *
   * @return the text that will be displayed in the tree column
   */
  @Override
  public StringProperty getKeyString() {
    if (keyString == null) {
      keyString = new SimpleStringProperty(exifTagInfo.getName());
    }
    return keyString;
  }

  /**
   * maintains the originalStringValue and the hasChanged property
   *
   * @param value that has be edited as a StringProperty
   */
  public void setValueFromString(SimpleStringProperty value) {
    boolean wasChanged = isChanged();
    valueString = value; //save the edited value
    boolean changed = isChanged();

    //sync mediaFile.changedMetaTags list if changedStatus changed ;-)
    if (changed && !wasChanged) mediaFile.addToChangedTags(this);
    else if (!changed && wasChanged) mediaFile.removeFromChangedTags(this);

    mediaFile.updateStatusProperty();
  }

  /**
   * take over the Entry into the attributes of this EditableTagItem
   *
   * @param entry loaded via lljTran
   */
  public abstract void initValueFromEntry(Entry entry);

  /**
   * put the attributes of this EditableTag Item to the mediaUtil's Exif object, so that it can be written to disk
   *
   * @param entry that is ready to be written via lljTran
   */
  public abstract void setEntryValueForSaving(Entry entry);

  /**
   * @return the text that will be displayed in the type column
   */
  @Override
  public StringProperty getExifIDString() {
    if (exifIDString == null) {
      exifIDString = new SimpleStringProperty("" + exifTagInfo.getName());
    }
    return exifIDString;
  }

  /**
   * @return the tagInfo object describing data type etc of the tag
   */
  public ExifTagInfo getExifTag() {
    return exifTagInfo;
  }

  /**
   * read the children into the oberservable list to show them in the TreeTableView
   * not used with editable directory/tag structure, because this structure is constant
   *
   * @param children empty list, because tags have no sub elements
   */
  @Override
  public void cacheEditableChildren(ObservableList<TreeItem<EditableMetaInfoItem>> children) {
    //nothing to do: a Tag has no children
    //maybe in future if arrays are supported ;-)
  }

  /**
   * As soon as the user has entered a new value in MetaInfoEditableTagsView this method is called to save the value
   *
   * @param newValue the value when the user ended edit the field
   */
  @Override
  public void saveEditedValue(String newValue) {
    setValueFromString(new SimpleStringProperty(newValue));
  }

  /**
   * save changes of the tag to the exif header
   * or add the tag if it didn't exist before
   * <p>
   * if the stringValue is "" then the entry will be deleted and set to entry=null
   * <p>
   * if entry!=null the implementing subclasses are called to set value of entry
   */
  public void saveToExifHeader(Exif exifHeader) {
    Entry entry = null;
    if (isChanged()) {
      //new or changed
      if (isNewEntry()) {
        entry = new Entry(exifTagInfo.getDataType().getValue()); //no value until now. will be set it subclasses
        setEntryValueForSaving(entry);
        exifHeader.setTagValue(exifTagInfo.getId(), 0, entry, true);   //put it into exifHeader
      } else if (valueString.getValue().isEmpty()) {
        //deleted
        exifHeader.removeTag(exifTagInfo.getId(), true); //note exifTagInfo.getId() same as entry.getType()
      } else {
        entry = exifHeader.getTagValue(exifTagInfo.getId(), true); //lookup existing tag in exifHeader
        setEntryValueForSaving(entry);
      }
    }
  }

  /**
   * call this to re-initialize the tag after it has been written to disk: changed value is now the "original"
   */
  public void changesHaveBeenWritten() {
    originalValueString = valueString.getValue();
  }

  /**
   * @return the text that will be displayed in the value column
   */
  @Override
  public StringProperty getValueString() {
    return valueString; //no lazy load, so valueString can be returned directly
  }
}
