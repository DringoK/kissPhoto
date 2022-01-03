package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import dringo.kissPhoto.model.Metadata.Exif.ExifTag;
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
  protected Entry entry;
  private final ExifTag exifTag;              //link to the entry in the list of editable exif tags, to find out, how it can be edited etc.
  protected StringProperty stringValue; //current value
  private final String originalStringValue;   //the value, of the item as it is currently saved, to find out, if the value has changed. Null, if the exif tag/value didn't exist until now

  /**
   * Constructor to wrap an Entry object
   * if the entry exists in exifHeader then the value is loaded
   * if it is new then an empty entry is generated and newEntry=true
   * @param exifTag The object to be wrapped
   */
  public EditableTagItem(MediaFileTaggedEditable mediaFile, Exif exifHeader, ExifTag exifTag) {
    super(mediaFile, exifHeader);
    this.exifTag = exifTag;

    //try to load the tag entry if it already exists
    entry = exifHeader.getTagValue(exifTag.getId(), true);

    //generate a new entry
    if (entry == null){
      originalStringValue = null;   //indicates a new value
      //entry and exifHeader remain being null and will be generated on save only
    }else{
      //remember the original value to find out, if the value has been changed and needs to be written to the file
      originalStringValue = getValueString().get();
    }
  }

  public boolean isNewEntry(){
    return originalStringValue ==null;
  }

  public boolean isChanged(){
    if (stringValue==null) return false; //not yet edited
    else if (stringValue.get().isEmpty() && originalStringValue==null) return false; //empty==null
    else return !stringValue.get().equals(originalStringValue); //note: if originalStringValue==null 'equals' will return false
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
   * @return the text that will be displayed in the tree column
   */
  @Override
  public StringProperty getKeyString() {
    if (keyString==null){
      keyString = new SimpleStringProperty(exifTag.getName());
    }
    return keyString;
  }

  /**
   * maintains the originalStringValue and the hasChanged property
   *  @param value that has be edited as a StringProperty
   */
  public void setValueFromString(StringProperty value){
    boolean wasChanged = isChanged();
    stringValue = value; //save the edited value
    boolean changed = isChanged();

    //sync mediaFile.changedMetaTags list if changedStatus changed ;-)
    if (changed && !wasChanged) mediaFile.addToChangedTags(this);
    else if (!changed && wasChanged) mediaFile.removeFromChangedTags(this);

    mediaFile.updateStatusProperty();
  }

  /**
   * @return the value of the Tag so it can be displayed and edited as a String
   */
  public abstract StringProperty getValueString();


  /**
   * @return the text that will be displayed in the type column
   */
  @Override
  public StringProperty getExifIDString() {
    if (exifIDString == null){
        exifIDString = new SimpleStringProperty(""+ exifTag.getName());
    }
    return exifIDString;
  }

  /**
   * @return the mediautil Entry connected with this TagItem
   */
  public Entry getEntry() {
    return entry;
  }

  /**
   * @return the tagInfo object describing data type etc of the tag
   */
  public ExifTag getExifTag() {
    return exifTag;
  }

  /**
   * call this method for all tags before imangeInfo (Exif-Header) is written
   * a new entry will only be added if it has been changed
   * note: this method has no effect on entries that already existed. Their value changes are always considered
   */
  public void addToExifIfNewAndChanged(){
    if (isNewEntry() && isChanged()){
      exifHeader.setTagValue(entry.getType(), 0, entry, true); //add a new tag
    }
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
   */
  public void saveToExifHeader(Exif exifHeader) {
    if (isChanged() && isNewEntry()) {
      entry = new Entry(exifTag.getDataType().getValue()); //no value until now. will be set it subclasses
      exifHeader.setTagValue(exifTag.getId(), 0, entry, true);   //put it into exifHeader
    }
    //concrete subclasses will set the value afterwards when redefining this method
  }
}
