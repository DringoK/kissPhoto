package mediautil.image.jpeg;

import java.io.InputStream;

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
class IterativeReadVars {
  public static final int READING_STAGE = 1;
  public static final int READING_DCT_STAGE = 2;
  public static final int READING_APPX_STAGE = 3;
  public static final int IMAGE_READ_STAGE = 4;
  public static final int DONE_STAGE = 5;

  // For info
  public int minReadRequest;
  public int maxReadRequest;

  public InputStream is;
  public int readUpto;
  public int stage;
  public int sections;
  public boolean keep_appxs, appxsCleared;
  public int appxPos, appxLen;
  public boolean throwException;

  //Vars for readDCT
  public double currentProgress, callbackProgress, progressPerMcu;
  public int[] last_dc;
  public int[][] DCT;
  public int next_restart_num;
  public int ix, iy;
}
