package dringo.kissPhoto.model.Metadata.EditableTagItem;

import dringo.kissPhoto.model.Metadata.Exif.EditableExifTag;
import mediautil.image.jpeg.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class can be used to generate a EditableTagItem simply by providing an exif-id
 * if it is supported an editableTagItem of the appropriate subclass is generated or null if not
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-13 First implementation
 * @since 2021-11-13
 */

public class EditableTagItemFactory{
  private  EditableTagItemFactory() {
  }

  public static EditableTagItem getTag(int exifID, Exif imageInfo){
    return switch (EditableExifTag.fromID(exifID).getDataType()) {
      case BYTE, SHORT, LONG, SLONG -> new EditableIntTagItem(exifID, imageInfo);
      case DATE_TIME -> new EditableDateTimeTagItem(exifID, imageInfo);
      case ASCII -> new EditableStringTagItem(exifID, imageInfo);
      default -> null;
    };
  }
}
