package dringo.kissPhoto.model;

import mediautil.image.jpeg.LLJTran;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * Helper for rotating, mirroring image files (lossless)
 * it wraps the used library
 * -> currently mediautil 1.0  LLJTran (Lossless JPEG transformation) and fixes 1.0.zip
 * http://mediachest.sourceforge.net/mediautil/
 * http://mediachest.sourceforge.net/mediautil/javadocs/index.html
 * <p/>
 *
 * @author Dringo
 * @since 2017-10-26  first trial
 * @version 2020-12-20 corrections. Rotation of jpegs without exif header works now
 * @version 2018-10-21  orientation bit is set correctly now
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

/*
        //System.out.println(llj.getComment());
        //llj.setComment("kissPhoto rotation");

        //Modify Exif-Entries
        AbstractImageInfo<?> imageInfo = llj.getImageInfo();

        //if exif directory is existing
        if (imageInfo instanceof Exif exifInfo) { //includes test on !=null.  note: this inlines Exif exifInfo = (Exif) imageInfo;

          // Change Date/Time entries in Exif
          Entry entry;

          entry= exifInfo.getTagValue(Exif.DATETIME, true);//true= use mainIFD (false would be subIFD of thumbnail)
          if(entry != null) {
            System.out.println("changeValue DateTime");
            entry.setValue(0, "1990:05:11 00:01:02");   //index is ignored in connection with String values
          }else {
            System.out.println("setTagValue DateTime");
            exifInfo.setTagValue(Exif.DATETIME, 0, new Entry("1990:05:11 11:22:33"), true);
          }

          entry= exifInfo.getTagValue(Exif.ARTIST, true);//true= use mainIFD (false would be subIFD of thumbnail)
          if(entry != null) {
            System.out.println("changeValue Artist");
            entry.setValue(0, "Künstler=Ingo");   //index is ignored in connection with String values
          }else {
            System.out.println("setTagValue Artist");
            exifInfo.setTagValue(Exif.ARTIST, 0, new Entry("Künstler neu=Ingo"), true);
          }

          entry = exifInfo.getTagValue(Exif.DATETIMEORIGINAL, true);
          if(entry != null)
            entry.setValue(0, "1991:06:12 01:02:03");
          entry = exifInfo.getTagValue(Exif.DATETIMEDIGITIZED, true);
          if(entry != null)
            entry.setValue(0, "1992:07:13 02:03:04");


          //write all changes back to the APPx-buffers
          llj.refreshAppx();
        }
  */
        // 3. Save the Image which is already transformed as specified by the
        //    input transformation in Step 2, along with the Exif header.
        OutputStream out = new BufferedOutputStream(new FileOutputStream(imageFile.getFileOnDisk().toFile()));
        llj.save(out, LLJTran.OPT_WRITE_ALL);

        out.close();
      } finally {
        // Cleanup
        llj.freeMemory();
      }
    } catch (Exception e) {
      e.printStackTrace();
      successful = false;
    }

    return successful;
  }

}
