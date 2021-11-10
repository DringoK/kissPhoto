package mediautil.image.jpeg;

import java.io.OutputStream;

/**
 * Mediautil reworked for KissPhoto bei Dringo
 *
 * This class was part of the LLJTran.java file.
 * It holds all variables ("a record") for writing
 * It is used in LLJTran
 *
 * @version 2021-11-09 I extracted it into an own file for better Java compatibility and changed all c-like definitions into Java definitions
 * @since 2021-11-09
 * @author Dringo. Originally Dmitriy Rogatkin and Suresh Mahalingam (msuresh@cheerful.com)
 */
class IterativeWriteVars {
  public static final int WRITE_BEGIN = 0;
  public static final int WRITE_COMMENTS = 1;
  public static final int WRITE_APPXS = 2;
  public static final int WRITE_DQT = 3;
  public static final int WRITE_DHT = 4;
  public static final int WRITE_START = 5;
  public static final int WRITE_DCT = 6;
  public static final int WRITE_COMPLETE = 7;

  // For info
  public int minWriteRequest;
  public int maxWriteRequest;

  public OutputStream os;
  public int op;
  public int options;
  public int restart_interval;
  public String comment;
  public Class custom_appx;
  public int state = WRITE_COMPLETE;
  public byte[] huffTables;
  public int currentAppxPos, currentAppx;

  public byte[] saveAppxs;
  public int svX;
  public int svY;
  public int svWidthMCU;
  public int svHeightMCU;

  // initWriteDCT vars
  public boolean transformDct;
  public int[][][][][] new_dct_coefs;

  public double currentProgress, callbackProgress, progressPerMcu;
  public int[] last_dc;
  public int restarts_to_go;
  public int xCropOffsetMCU;
  public int yCropOffsetMCU;

  public boolean handleXEdge;
  public boolean handleYEdge;
  public int new_ix, new_iy;
  public boolean pullDownMode;

  // For unused method writeJpeg
  public boolean restoreVars;

  // if transformDct true it indicates if full Dct array needs to be allocated
  // or if the rows of old dct array can be reused.
  public boolean reuseDctRows = true;

  public void freeMemory() {
    huffTables = null;
    new_dct_coefs = null;
    last_dc = null;
  }
}
