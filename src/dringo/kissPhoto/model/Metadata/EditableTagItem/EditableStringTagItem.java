package dringo.kissPhoto.model.Metadata.EditableTagItem;

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
   * @param entryID The object to be wrapped
   */
  public EditableStringTagItem(int entryID, Exif imageInfo) {
    super(entryID, imageInfo);
  }

  /**
   * @param value that has be edited as a StringProperty or Integer(0), if conversion was not possible (=ignore conversion exceptions)
   */
  @Override
  public void setValueFromString(StringProperty value) {
    super.setValueFromString(value);

    entry.setValue(0, value.getValue());
    valueString = value;
  }

  /**
   * @return the text that will be displayed in the value column or null if no String is stored in the Entry
   */
  @Override
  public StringProperty getValueString() {
    if (valueString == null){
      if (entry.getValue(0) instanceof String)
        valueString = new SimpleStringProperty((String)entry.getValue(0)); //index is ignored for string-Entries in Entry.java
    }
    return valueString;
  }
}
