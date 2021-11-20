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
 * This class represents an editable Tag Entry in MetaInfoEditableTagsView which holds an "DATE_TIME" value, i.e. ASCII in the form "YYYY:MM:DD HH:MM:SS"
 * An EditableTag resides in an EditableDirectory and has no children
 * It wraps an Entry-object of mediautil (which would be called "Tag" in meta-data-extractor, which is used in the read-only version of TagItem)
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-13 First implementation
 * @since 2021-11-13
 */

public class EditableDateTimeTagItem extends EditableTagItem {

  /**
   * Constructor to wrap an Entry object
   * @param entryID The object to be wrapped
   */
  public EditableDateTimeTagItem(int entryID, Exif imageInfo) {
    super(entryID, imageInfo );
  }

  /**
   * @param value that has be edited as a StringProperty or Integer(0), if conversion was not possible (=ignore conversion exceptions)
   */
  @Override
  public void setValueFromString(StringProperty value) {
    super.setValueFromString(value);
    entry.setValue(0, getStoringFormat(value));
    valueString = getDisplayFormat(value);
  }

  /**
   * @return the text that will be displayed in the value column or null if no String is stored in the Entry
   */
  @Override
  public StringProperty getValueString() {
    if (valueString == null){
      if (entry.getValue(0) instanceof String)
        valueString = getDisplayFormat(new SimpleStringProperty((String)(entry.getValue(0)))); //index is ignored for string-Entries in Entry.java
    }
    return valueString;
  }

  /**
   * current implementation only checks length==19
   * @param str the date-string to be investigated
   * @return true if str is a valid DateTime-String in the form YYYY:MM:DD HH:MM:SS or YYYY-MM-DD HH:MM:SS
   */
  private boolean isValidDateTime(String str){
    return (str !=null) && (str.length()==19);
  }

  /**
   * convert a valid string into storing format i.e. YYYY:MM:DD HH:MM:SS
   * @param value to be converted e.g. YYYY-MM-DD HH:MM:SS
   * @return the string with ":" at position 4 and 7
   */
  private String getStoringFormat(StringProperty value){
    if (isValidDateTime(value.get())) {
      StringBuilder str = new StringBuilder(value.getValue());
      str.setCharAt(4, ':'); //after YYYY
      str.setCharAt(7, ':'); //after YYYY:MM
      return str.toString();
    }else{
      return null;
    }
  }

  /**
   * convert a valid string into display format i.e. YYYY-MM-DD HH:MM:SS
   * @param value to be converted e.g. YYYY:MM:DD HH:MM:SS
   * @return the string with "-" at position 4 and 7
   */
  private StringProperty getDisplayFormat(StringProperty value){
    if (isValidDateTime(value.get())) {
      StringBuilder str = new StringBuilder(value.getValue());
      str.setCharAt(4, '-'); //after YYYY
      str.setCharAt(7, '-'); //after YYYY:MM
      return new SimpleStringProperty(str.toString());
    }else{
      return null;
    }
  }
}
