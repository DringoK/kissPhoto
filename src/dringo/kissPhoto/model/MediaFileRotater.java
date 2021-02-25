package dringo.kissPhoto.model;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * Helper for rotating, mirroring media files (lossless if possible)
 * For every media type a subclass of MediaFileRotater can be derived to perform rotation or mirroring of the media file
 * Every subclass of mediaFile has to install a MediaFileRotater
 *
 * @author Dringo
 * @since 2018-10-21
 * @version initial version
 */


public class MediaFileRotater {
  public enum RotateOperation {ROTATE0, ROTATE90, ROTATE180, ROTATE270} //clockwise
  //do not support transverse and transpose (diagonally mirrored) because same as rotate+flip. Keep it simple!!

  /**
   * flip and/or rotate an ImageFile on disk
   * first rotate, then flip
   * <p>
   * This (empty) implementation should be overwritten in the subclass to handle the specific media type
   * As soon as a specific rotater is provided the according mediaFile should override the canRotate() to return true
   *
   * @param mediaFile        file to transform
   * @param rotateOperation  clockwise rotation. ROTATE0 has no effect
   * @param flipVertically   mirroring
   * @param flipHorizontally mirroring
   * @return successful
   */
  public boolean transform(MediaFile mediaFile, RotateOperation rotateOperation, boolean flipHorizontally, boolean flipVertically) {
    //the default implementation is empty but probably overwritten in subclass
    return true;
  }
}
