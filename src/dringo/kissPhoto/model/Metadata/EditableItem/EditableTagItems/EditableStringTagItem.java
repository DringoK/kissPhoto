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
 * This class represents an editable Tag Entry in MetaInfoEditableTagsView which holds an "String" value, i.e. ASCII
 * An EditableTag resides in an EditableDirectory and has no children
 * It wraps an Entry-object of mediautil (which would be called "Tag" in meta-data-extractor, which is used in the read-only version of TagItem)
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-13 First implementation
 * @since 2021-11-13
 */

public class EditableStringTagItem extends EditableTagItem {

  /**
   * Constructor to wrap an Entry object
   * @param exifTag The object to be wrapped
   */
  public EditableStringTagItem(MediaFileTaggedEditable mediaFile, Exif imageInfo, ExifTag exifTag) {
    super(mediaFile, imageInfo, exifTag);
  }

  /**
   * @return the text that will be displayed in the value column or null if no String is stored in the Entry
   */
  @Override
  public StringProperty getValueString() {
    if (stringValue == null){   //lazy generation not before it is displayed for the first time
      if (entry!=null && entry.getValue(0) instanceof String)  //only if the entry already existed in metaInfo
        stringValue = new SimpleStringProperty((String)entry.getValue(0)); //index is ignored for string-Entries in mediautil.image.jpeg.Entry
      else
        stringValue = new SimpleStringProperty("");
    }
    return stringValue;
  }

  /**
   * save changes of the tag to the exif header
   * or add the tag if it didn't exist before
   */
  @Override
  public void saveToExifHeader(Exif exifHeader) {
    super.saveToExifHeader(exifHeader);
    entry.setValue(0, stringValue.get()); //index is ignored in context with strings in media util
  }
}
