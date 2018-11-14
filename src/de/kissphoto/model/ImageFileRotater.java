package de.kissphoto.model;

import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * Helper for rotating, mirroring image files (lossless)
 * it wraps the used library
 * -> currently mediautil 1.0  LLJTran (Lossless JPEG transformation) and fixes 1.0.zip
 * http://mediachest.sourceforge.net/mediautil/
 * http://mediachest.sourceforge.net/mediautil/javadocs/index.html
 * <p/>
 *
 * @author Dr. Ingo Kreuz
 * @date: 2017-10-26  first trial
 * @modified: 2018-10-21  orientation bit is set correctly now
 */

public class ImageFileRotater extends MediaFileRotater {
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

    try {
      ImageFile imageFile = (ImageFile) mediaFile;

      // 1. Initialize LLJTran and Read the entire Image including Appx markers
      LLJTran llj = new LLJTran(imageFile.getFileOnDisk().toFile());
      try {
        llj.read(LLJTran.READ_ALL, true);  //keep_appxs =true=retain EXIF-Info

        // 2. Transform the image using default options along with
        int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;  //option=change orientation only in header
        int op = 0;
        switch (rotateOperation) {
          case ROTATE90:
            op = LLJTran.ROT_90;
            break;
          case ROTATE180:
            op = LLJTran.ROT_180;
            break;
          case ROTATE270:
            op = LLJTran.ROT_270;
            break;
        }
        llj.transform(op, options);

        if (flipHorizontally) {
          llj.transform(LLJTran.FLIP_H, options);
        }
        if (flipVertically) {
          llj.transform(LLJTran.FLIP_V, options);
        }

        //set orientation to 1  (Picture's top/left is top/left) because this is the desired rotation/flipping now
        AbstractImageInfo imageInfo = llj.getImageInfo();

        if (imageInfo != null) {
          Exif exif = (Exif) imageInfo;  //java.lang.ClassCastException: mediautil.image.jpeg.JPEG cannot be cast to mediautil.image.jpeg.Exif

          Entry entry = exif.getTagValue(Exif.ORIENTATION, true); //true= use mainIFD (false would bei subIFD of thumbnail)
          if (entry != null) {
            entry.setValue(0, (short) 1); //because entry can hold intValue[] 0 is the index of the only intValue for orientation tag
          }
        }

        // 3. Save the Image which is already transformed as specified by the
        //    input transformation in Step 2, along with the Exif header.
        OutputStream out = new BufferedOutputStream(new FileOutputStream(imageFile.getFileOnDisk().toFile()));
        llj.save(out, LLJTran.OPT_WRITE_ALL | LLJTran.OPT_XFORM_ORIENTATION);
        out.close();
      } finally {
        // Cleanup
        llj.freeMemory();
      }
    } catch (Exception e) {
      System.out.println(e);
      successful = false;
    }

    return successful;
  }

}
