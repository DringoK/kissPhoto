package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.Exif.ExifTagInfo;
import javafx.beans.property.SimpleStringProperty;
import mediautil.gen.Rational;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class represents an editable Tag Entry in MetaInfoEditableTagsView which holds a rational value, i.e. a number with maximum one decimal point
 * An EditableTag resides in an EditableDirectory and has no children
 * It wraps an Entry-object of mediautil (which would be called "Tag" in meta-data-extractor, which is used in the read-only version of TagItem)
 * <p/>
 *
 * @author Dringo
 * @since 2022-01-14
 * @version 2022-01-14
 */

public class EditableRationalTagItem extends EditableTagItem {
  /**
   * Constructor to wrap an Entry object
   * The int-Value is stored in stringValue only and converted to int or Integer if needed only
   *
   * @param exifTagInfo The object to be wrapped
   */
  public EditableRationalTagItem(MediaFileTaggedEditable mediaFile, Exif imageInfo, ExifTagInfo exifTagInfo) {
    super(mediaFile, imageInfo, exifTagInfo);
  }

  /**
   * take over the Entry into the attributes of this EditableTagItem
   *
   * @param entry loaded via lljTran
   */
  @Override
  public void initValueFromEntry(Entry entry) {
    if (entry != null && entry.getValue(0) instanceof Rational) //only if the entry already existed in metaInfo
      valueString = new SimpleStringProperty("" + ((Rational)entry.getValue(0)).floatValue());
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
        float floatVal = Float.parseFloat(valueString.get());
        entry.setValue(0, new Rational(floatVal));
      } catch (NumberFormatException e) {
        entry.setValue(0, 0); //in error case set 0 as a default value
      }
    }
  }
}
