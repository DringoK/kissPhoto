package dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems;

import dringo.kissPhoto.model.MediaFileTaggedEditable;
import dringo.kissPhoto.model.Metadata.Exif.ExifTagInfo;
import dringo.kissPhoto.view.inputFields.FractionTextField;
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
 * This class represents an editable Tag Entry in MetaInfoEditableTagsView which holds a fraction value, i.e. two numbers separated by a slash
 * An EditableTag resides in an EditableDirectory and has no children
 * It wraps an Entry-object of mediaUtil (which would be called "Tag" in meta-data-extractor, which is used in the read-only version of TagItem)
 * <p/>
 *
 * @author Dringo
 * @since 2022-01-20
 * @version 2022-01-20
 */

public class EditableFractionTagItem extends EditableTagItem {
  /**
   * Constructor to wrap an Entry object
   * The int-Value is stored in stringValue only and converted to int or Integer if needed only
   *
   * @param exifTagInfo The object to be wrapped
   */
  public EditableFractionTagItem(MediaFileTaggedEditable mediaFile, Exif imageInfo, ExifTagInfo exifTagInfo) {
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
      valueString = new SimpleStringProperty(entry.getValue(0).toString());
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
    //index is always 0 for simple rationals
    if (entry != null) {
      try {
        int numerator;
        int denominator = 1;
        int pos = valueString.get().indexOf(FractionTextField.SEPARATOR);
        if (pos>0){
          numerator   = Integer.parseInt(valueString.get().substring(0, pos));
          if (pos<valueString.get().length()-1) //only change denominator if / is not last char (otherwise assume denominator=1 (as initialized))
            denominator = Integer.parseInt(valueString.get().substring(pos+1));
        }else{
          numerator = Integer.parseInt(valueString.get());
        }
        entry.setValue(0, new Rational(numerator,denominator));
      } catch (NumberFormatException e) {
        entry.setValue(0, 0); //in error case set 0 as a default value
      }
    }
  }
}
