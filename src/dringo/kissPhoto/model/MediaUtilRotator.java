package dringo.kissPhoto.model;


import mediautil.image.jpeg.LLJTran;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * Helper for rotating and mirroring image files (lossless)
 * it wraps the used library
 * -> currently mediautil 1.0  LLJTran (Lossless JPEG transformation) and fixes 1.0.zip
 * http://mediachest.sourceforge.net/mediautil/
 * http://mediachest.sourceforge.net/mediautil/javadocs/index.html
 * <p/>
 *
 * @author Dringo
 * @since 2017-10-26  first trial
 * @version 2022-01-03 combined with saving meta tags in MediaFileTaggedEditable.saveChanges()
 * @version 2020-12-20 corrections. Rotation of jpegs without exif header works now
 * @version 2018-10-21  orientation bit is set correctly now
 */

public class MediaUtilRotator extends MediaFileRotator {
  /**
   * lossless flip and/or rotate an ImageFile on disk
   * first rotate, then flip
   *
   * @param mediaFile        file to transform (must be an imageFile)
   * @param rotateOperation  clockwise rotation. ROTATE0 has no effect
   * @param flipVertically   mirroring
   * @param flipHorizontally mirroring
   * @return successful
   */
  @Override
  public boolean transform(MediaFile mediaFile, RotateOperation rotateOperation, boolean flipHorizontally, boolean flipVertically) {
    //Log.debugLevel = Log.LEVEL_NONE; //please no output on console (default is 3=Log.LEVEL_INFO) which shows ERROR, WARNING and INFO)

    boolean successful = true;
    if (mediaFile instanceof MediaFileTaggedEditable) {
      try {
        MediaFileTaggedEditable imageFile = (MediaFileTaggedEditable) mediaFile;

        // 1. Initialize LLJTran and Read the entire Image including Appx markers
        LLJTran llj = imageFile.getLljTran();
        //read the rest of the image file
        llj.read(LLJTran.READ_ALL, true);  //keep_appxs =true=retain unsupported APPx sections

        // 2. Transform the image using default options along with
        int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;// |LLJTran.OPT_XFORM_THUMBNAIL;  //correct orientation + Default= OPT_WRITE_ALL|OPT_XFORM_APPX | OPT_XFORM_ADJUST_EDGES (i.e correct the edges if resolution is not multiple of 8x8)
        int op = switch (rotateOperation) {
          case ROTATE90 -> LLJTran.ROT_90;
          case ROTATE180 -> LLJTran.ROT_180;
          case ROTATE270 -> LLJTran.ROT_270;
          default -> 0;
        };
        llj.transform(op, options);

        if (flipHorizontally) {
          llj.transform(LLJTran.FLIP_H, options);
        }
        if (flipVertically) {
          llj.transform(LLJTran.FLIP_V, options);
        }
      } catch (Exception e) {
        successful = false;
      }
    return successful;

    }else
    return false; //only MediaFileTaggedEditable files can be rotated on disk with MediaUtilRotator
  }
}
