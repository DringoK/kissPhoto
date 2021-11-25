package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

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
   * @param entryID The object to be wrapped
   */
  public EditableIntTagItem(int entryID, Exif imageInfo) {
    super(entryID, imageInfo);
  }

  /**
   * @param value that has be edited as a StringProperty or Integer(0), if conversion was not possible (=ignore conversion exceptions)
   */
  @Override
  public void setValueFromString(StringProperty value) {
    super.setValueFromString(value);
    try {
      entry.setValue(0, Integer.parseInt(value.get()));
      valueString = value;
    }catch (NumberFormatException e){
      entry.setValue(0, 0);
      valueString = new SimpleStringProperty("-");  //i.e. invalid
    }
  }

  /**
   * @return the text that will be displayed in the value column or null, if not a valid integer is stored for the entry
   */
  @Override
  public StringProperty getValueString() {
    if (valueString==null)                      //lazy load
      if (entry.getValue(0) instanceof Integer)
        valueString = new SimpleStringProperty("" + entry.getValue(0));
    return valueString;
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
}
