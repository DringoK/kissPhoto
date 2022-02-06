package dringo.kissPhoto.model.Metadata.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This enum defines all allowed data types for Exif tags according to Exif 2.3, chapter 4.6.2.
 * plus derived data types (e.g. dataTime is technically an ASCII, but to be treated differntly by KissPhoto)
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-12
 * @since 2021-11-12
 */
public enum ExifTagDataType {
  //Tag types according to chapter 4.6.2 of Exif 2.3 standard (Jeita CP-3451B)
  BYTE(1, "byte"),            //8-bit unsigned integer
  ASCII(2, "ascii"),          //8-bit byte containing on 7-bit ASCII code. Terminated with NULL
  SHORT(3, "short"),          //16-bit (2-byte) unsigned integer
  LONG(4, "long"),            //32-bit (4-byte) unsigned integer
  RATIONAL(5, "rational"),    //two LONGs. The first LONG is the numerator, the second LONG expresses the denominator : numerator/denominator
  UNDEFINED(7, "undefined"),  //8-bit byte that may take any value depending on the field definition
  SLONG(9, "slong"),          //32-bit (4-byte) signed integer (2's complement notation)
  SRATIONAL(10, "srational"), //Two SLONGs. the first SLONG is the numerator and the second SLONG is the denominator

  //derived types. //their ID is the same as their official type
  DATE_TIME(2, "dateTime"),   //YYYY:MM:DD HH:MM:SS in 24-hour format (20 Bytes incl 1 blank and trailing NULL). ->officially stored as an ASCII
  UTF8(1, "UTF8"),            //Bytes interpreted as  UTF8
  FRACTION(5, "fraction"),       //same as rational byte edited as x/y
  //not or not fully supported. Not editable
  NO(0, "no"),
  DOUBLE(0, "Double"), //not in the EXIF-Standard, but used e.g. in 0x85d8 ModelTransform
  FLOAT(0, "Float"); //not in the EXIF Standard, but used e.g. in 0xcfa ProfileHueSatMapData1

  private final int value;
  private final String name;

  ExifTagDataType(int i, String name) {
    value = i;
    this.name = name;
  }

  public int getValue() {
    return value;
  }
  public String getName() {return name; }
}
