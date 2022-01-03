package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.Exif.ExifTag;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mediautil.image.jpeg.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class represents an editable Tag Entry in MetaInfoEditableTagsView which holds an "int" value, i.e. BYTE, SHORT, LONG or SLONG
 * An EditableTag resides in an EditableDirectory and has no children
 * It wraps an Entry-object of mediautil (which would be called "Tag" in meta-data-extractor, which is used in the read-only version of TagItem)
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-10 First implementation
 * @since 2021-11-10
 */

public class EditableIntTagItem extends EditableTagItem {

  /**
   * Constructor to wrap an Entry object
   * @param exifTag The object to be wrapped
   */
  public EditableIntTagItem(MediaFileTaggedEditable mediaFile, Exif imageInfo, ExifTag exifTag) {
    super(mediaFile, imageInfo, exifTag);
  }

  /**
   * @return the text that will be displayed in the value column or null, if not a valid integer is stored for the entry
   */
  @Override
  public StringProperty getValueString() {
    if (stringValue ==null)  //lazy generation not before it is displayed for the first time
      if (entry != null && entry.getValue(0) instanceof Integer) //only if the entry already existed in metaInfo
        stringValue = new SimpleStringProperty("" + entry.getValue(0));
      else
        stringValue = new SimpleStringProperty("");
    return stringValue;
  }

  /**
   * @return the int value stored in the entry or 0 if not a valid value is stored
   */
  public int getValue(){
    if (entry.getValue(0) instanceof Integer)
      return (Integer) (entry.getValue(0));
    else
      return 0;
  }

  /**
   * save changes of the tag to the exif header
   * or add the tag if it didn't exist before
   */
  @Override
  public void saveToExifHeader() {
    super.saveToExifHeader();
    //index is always 0 for simple integers
    try {
      entry.setValue(0, Integer.parseInt(valueString.get()));
    }catch (NumberFormatException e){
      entry.setValue(0, 0); //in error case set 0 as a default value
    }
  }
}
