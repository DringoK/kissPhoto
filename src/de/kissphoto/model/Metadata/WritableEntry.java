package de.kissphoto.model.Metadata;

import java.io.RandomAccessFile;

/**
 * This is an entry of an EXIF Directory (IFD=ImageFileDirectory)
 * and a wrapper for com.drew.metadata.Tag
 * Tag, Type, Size, Offset/data
 *
 * @Author: Dr. Ingo Kreuz
 * @Date: 2017-11-04
 * @modified:
 */

public class WritableEntry {
  public static final int INVALID = -1;
  public static final int TYPE_BYTE = 1;      //8bit (1 Byte) unsigned integer
  public static final int TYPE_ASCII = 2;      //8bit (1Byte) containgin one 7bit ASCII, terminated with NULL
  public static final int TYPE_SHORT = 3;      //16bit (4Byte) unsigned integer
  public static final int TYPE_LONG = 4;      //32bit (4Byte) unsigned integer
  public static final int TYPE_RATIONAL = 5;   //two longs: first numerator, second denominator
  public static final int TYPE_UNDEFINED = 7;  //8bit Byte that may take any intValue depending on the field definition
  public static final int TYPE_SLONG = 9;      //4 Byte signed integer 2s complement notation
  public static final int TYPE_SRATIONAL = 10; //Two SLONGs. The first SLONG=numerator, the second=denominator

  //tag structure see EXIF-Spec DC-008-Translation-2016-E_.pdf section 4.6 TAGs
  private static final int POS_TAG = 0;
  private static final int POS_TYPE = 2;
  private static final int POS_COUNT = 4;
  private static final int POS_VALUE_OFFSET = 8;
  private static final int TAG_LENGTH = 2;   //if tag-id is not written but has been read as last action then pos in array is shiftet for all other fields by this value

  int tag = INVALID;        //Entry Exif-Tag-Value
  int type = INVALID;       //final TiffDataFormat format = TiffDataFormat.fromTiffFormatCode(type);
  long count = INVALID;     //how many values e.g. ASCII length
  long originalCount = INVALID; //length of the original value
  long intValue = INVALID; //vill be saved in place of offset (count = 1).

  //----- Value (only one ot them is used) --------
  boolean isNewEntry = false; //true if the entry has been added and is currently not part of the exif-directory (i.e. the change is not an update but an insertion)

  double doubleValue = INVALID;
  String strValue = "";    //will be saved as an offset + an entry (count > 1)
  byte[] byteArrayValue = null; //low level representation of data or value for TYPE_UNDEFINED

  byte[] byteArrayEntry = new byte[12]; //low level representation of the entry

  long offset = INVALID;   //for data that is longer than 4 bytes: pointer to data area of IFD

  WritableDirectory writableDirectory = null; //link back to the directory containing the entry

  public WritableEntry(int tag, int type, WritableDirectory writableDirectory) {
    this.tag = tag;
    this.type = type;
    this.writableDirectory = writableDirectory;
  }

  /**
   * @param newValue one of the integer types
   * @throws Exception if type of ExifEntry does not match "int" (signed or unsigned, byte, word, dword, ...)
   */
  public void setValue(long newValue) throws Exception {
    if (type != TYPE_BYTE && type != TYPE_SHORT && type != TYPE_LONG && type != TYPE_SLONG) {
      throw new Exception("ExifEntry: type does not match int value");
    }
    count = 1;
    this.intValue = newValue;
  }

  /**
   * @param newValue text
   * @throws Exception if type of ExifEntry does not match "string"
   */
  public void setValue(String newValue) throws Exception {
    //type must be TYPE_ASCII
    if (type != TYPE_ASCII) {
      throw new Exception("ExifEntry: type does not match ASCII value");
    }
    count = newValue.length();
    this.strValue = newValue;
  }

  /**
   * @param newValue rational value
   * @throws Exception if type of ExifEntry does not match "rational"
   */
  public void setValue(double newValue) throws Exception {
    if (type != TYPE_RATIONAL && type != TYPE_SRATIONAL) {
      throw new Exception("ExifEntry: type does not match rational value");
    }
    count = 1;
    this.doubleValue = newValue;
  }

  /**
   * @param byteArray lowlevel data of undefined type. If length <=4 it will be stored in entry otherwise in data area of the IFD
   * @throws Exception if type of ExifEntry does not match "undefined"
   */
  public void setValue(byte[] byteArray) throws Exception {
    if (type != TYPE_UNDEFINED) {
      throw new Exception("ExifEntry: type does not match undefined value");
    }
    this.byteArrayValue = byteArray;
    this.count = byteArray.length;
  }

  /**
   * entry is converted dependent on it's type into
   * - byteArrayEntry (Directory part of IFD)
   * - byteArrayValue (Data part of IFD)
   * an written into the next 12-2=10 bytes of the file.
   * It is assumed the the tag type byte have been read last and the file pointer is just before the type bytes
   *
   * @param file                which is open to read. File Pointer is just after the two tag type bytes and before type bytes
   * @param isMotorolaByteOrder false=intel (little endian), true=motorola (big endian)
   * @throws Exception if error occured during conversion into low level byteArray format
   */
  public void updateValueOnDisk(RandomAccessFile file, boolean isMotorolaByteOrder) throws Exception {
    //update does not change the tag-id.
    //therefore all field's position are shiftet by TAG_LENGTH (i.e. the first field is type)
    switch (type) {
      //all unsigned integer types fit into 4 byte and can be treated in the same way
      case TYPE_BYTE:
      case TYPE_SHORT:
      case TYPE_LONG:
      case TYPE_SLONG:
        putWord(type, byteArrayEntry, POS_TYPE - TAG_LENGTH, isMotorolaByteOrder);
        putDWord(count, byteArrayEntry, POS_COUNT - TAG_LENGTH, isMotorolaByteOrder);
        putDWord(intValue, byteArrayEntry, POS_VALUE_OFFSET - TAG_LENGTH, isMotorolaByteOrder);
        byteArrayValue = null;
        file.write(byteArrayEntry);
        break;
      //unsupported types
      case TYPE_RATIONAL:   //4+4 Bytes using offset and data area
      case TYPE_SRATIONAL:  //4+4 Bytes using offset and data area
      case TYPE_ASCII:      //n Bytes + one Zero-Byte using offset and data area
      case TYPE_UNDEFINED:  //n Bytes using offset and data area
      default:
        throw new Exception("ExifEntry.updateValueOnDisk does not support the tag type as by now");
    }

  }

  /**
   * translate value into a lowlevel byte[] representation dependent on motorola flag and save it into the byte array
   * starting from pos
   *
   * @param value               to translate into 16bit word = 2 Bytes, may be negative or positive
   * @param byteArray           to save the value in
   * @param pos                 to start saving from this position
   * @param isMotorolaByteOrder true=motorola=big endian, false=intel=little endian
   * @throws Exception if pos is so large that the word would be written behind the array
   */
  private void putWord(long value, byte[] byteArray, int pos, boolean isMotorolaByteOrder) throws Exception {
    if (pos + 1 >= byteArray.length) {
      throw new Exception("ExifEntry: word cannot be written behind byte array length");
    }

    if (isMotorolaByteOrder) {
      byteArray[pos + 2] = (byte) ((value >> 8) & 0xFF);  //higher byte lower word
      byteArray[pos + 3] = (byte) (value & 0xFF);         //lower byte lower word
    } else {
      byteArray[pos] = (byte) (value & 0xFF);         //lower byte lower word
      byteArray[pos + 1] = (byte) ((value >> 8) & 0xFF);  //higher byte lower word
    }
  }

  /**
   * translate value into a lowlevel byte[] representation dependent on motorola flag and save it into the byte array
   * starting from pos
   *
   * @param value               to translate into 32bit word = 4 Bytes, may be negative or positive
   * @param byteArray           to save the value in
   * @param pos                 to start saving from this position
   * @param isMotorolaByteOrder true=motorola=big endian, false=intel=little endian
   * @throws Exception if pos is so large that the word would be written behind the array
   */
  private void putDWord(long value, byte[] byteArray, int pos, boolean isMotorolaByteOrder) throws Exception {
    if (pos + 3 >= byteArray.length) {
      throw new Exception("ExifEntry: long cannot be written behind byte array length");
    }

    if (isMotorolaByteOrder) {
      byteArray[pos] = (byte) ((value >> 24) & 0xFF); //higher byte higher word
      byteArray[pos + 1] = (byte) ((value >> 16) & 0xFF); //higher byte higher word
      byteArray[pos + 2] = (byte) ((value >> 8) & 0xFF);  //higher byte lower word
      byteArray[pos + 3] = (byte) (value & 0xFF);         //lower byte lower word
    } else {
      byteArray[pos] = (byte) (value & 0xFF);         //lower byte lower word
      byteArray[pos + 1] = (byte) ((value >> 8) & 0xFF);  //higher byte lower word
      byteArray[pos + 2] = (byte) ((value >> 16) & 0xFF); //higher byte higher word
      byteArray[pos + 3] = (byte) ((value >> 32) & 0xFF); //higher byte higher word
    }
  }
}
