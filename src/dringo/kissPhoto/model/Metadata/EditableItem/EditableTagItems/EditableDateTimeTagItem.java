package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.Exif.ExifTagInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mediautil.image.jpeg.Entry;
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
   * DateTime is stored in Displayformat in valueString and converted on demand for saving only
   * @param exifTagInfo The object to be wrapped
   */
  public EditableDateTimeTagItem(MediaFileTaggedEditable mediaFile, Exif imageInfo, ExifTagInfo exifTagInfo) {
    super(mediaFile, imageInfo, exifTagInfo);
  }

  /**
   * take over the Entry into the attributes of this EditableTagItem
   *
   * @param entry loaded via lljTran
   */
  @Override
  public void initValueFromEntry(Entry entry) {
    if (entry != null && entry.getValue(0) instanceof String) //only if the entry already existed in metaInfo
      valueString = getDisplayFormat(new SimpleStringProperty((String)(entry.getValue(0)))); //index is ignored for string-Entries in Entry.java
    else
      valueString = new SimpleStringProperty("");
  }

  /**
   * put the attributes of this EditableTag Item to the mediaUtil's Exif object, so that it can be written to disk
   *
   * @param entry that is ready to be written via lljTran
   */
  @Override
  public void setEntryValueForSaving(Entry entry) {
    if (entry != null) entry.setValue(0, getStoringFormat(valueString));
  }

  /**
   * @param value that has be edited as a StringProperty or Integer(0), if conversion was not possible (=ignore conversion exceptions)
   */
  @Override
  public void setValueFromString(SimpleStringProperty value) {
    super.setValueFromString(value);
    valueString = getDisplayFormat(value);
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
      StringBuilder str = new StringBuilder(value.get());
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
  private SimpleStringProperty getDisplayFormat(StringProperty value){
    if (isValidDateTime(value.get())) {
      StringBuilder str = new StringBuilder(value.get());
      str.setCharAt(4, '-'); //after YYYY
      str.setCharAt(7, '-'); //after YYYY:MM
      return new SimpleStringProperty(str.toString());
    }else{
      return null;
    }
  }
}
