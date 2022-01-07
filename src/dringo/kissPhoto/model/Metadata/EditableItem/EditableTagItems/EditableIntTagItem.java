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
   * The int-Value is stored in stringValue only and converted to int or Integer if needed only
   *
   * @param exifTagInfo The object to be wrapped
   */
  public EditableIntTagItem(MediaFileTaggedEditable mediaFile, Exif imageInfo, ExifTagInfo exifTagInfo) {
    super(mediaFile, imageInfo, exifTagInfo);
  }

  /**
   * take over the Entry into the attributes of this EditableTagItem
   *
   * @param entry loaded via lljTran
   */
  @Override
  public void initValueFromEntry(Entry entry) {
    if (entry != null && entry.getValue(0) instanceof Integer) //only if the entry already existed in metaInfo
      valueString = new SimpleStringProperty("" + entry.getValue(0));
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
    //index is always 0 for simple integers
    if (entry != null) {
      try {
        entry.setValue(0, Integer.parseInt(valueString.get()));
      } catch (NumberFormatException e) {
        entry.setValue(0, 0); //in error case set 0 as a default value
      }
    }
  }
}
