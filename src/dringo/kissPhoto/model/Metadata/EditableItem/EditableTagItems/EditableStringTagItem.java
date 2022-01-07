package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.Exif.ExifTagInfo;
import javafx.beans.property.SimpleStringProperty;
import mediautil.image.jpeg.Entry;
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
   * @param exifTagInfo The object to be wrapped
   */
  public EditableStringTagItem(MediaFileTaggedEditable mediaFile, Exif imageInfo, ExifTagInfo exifTagInfo) {
    super(mediaFile, imageInfo, exifTagInfo);
  }

  /**
   * take over the Entry into the attributes of this EditableTagItem
   *
   * @param entry loaded via lljTran
   */
  @Override
  public void initValueFromEntry(Entry entry) {
    if (entry!=null && entry.getValue(0) instanceof String)  //only if the entry already existed in metaInfo
      valueString = new SimpleStringProperty((String)entry.getValue(0)); //index is ignored for string-Entries in mediautil.image.jpeg.Entry
    else
      valueString = new SimpleStringProperty("");
  }

  /**
   * put the attributes of this EditableTag Item to the mediaUtil's Exif object, so that it can be written to disk
   *
   * @param entry  that is ready to be written via lljTran
   */
  @Override
  public void setEntryValueForSaving(Entry entry) {
    if (entry != null) entry.setValue(0, valueString.get()); //index is ignored in context with strings in media util
  }

}
