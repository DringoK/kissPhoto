package dringo.kissPhoto.model.Metadata.Exif;

import dringo.kissPhoto.model.Metadata.Exif.LookupValues.OrientationLookupValue;

import java.util.Map;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This enum defines all editable Exif tags (=entries of an IFD) currently supported by kissPhoto
 * together with all information about the tag as defined in the exif standard (see "Doku\EXIF Tags.pdf" or <a href="https://sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html">https://sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html</a>)
 * Additionally the exifDir is defined in which it shall be displayed on GUI
 *
 * Note: currently only "simple" data types string, numbers and date/times are supported
 * <p/>
 *
 * @author Dringo
 * @version 2022-01-08 further tags added
 * @version 2021-11-10 First implementation
 * @since 2021-11-10
 */
public enum ExifTagInfo {
  DOCUMENTNAME(0x10d, ExifDir.IMAGE_DESCRIPTION, "Document Name", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  IMAGEDESCRIPTION(0x10e, ExifDir.IMAGE_DESCRIPTION, "Image Description", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  MAKE(0x10f, ExifDir.OTHER, "Make", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  MODEL(0x100, ExifDir.OTHER, "Model", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  ORIENTATION(0x0112, ExifDir.IMAGE_INFO, "Orientation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, OrientationLookupValue.getValueMap()),
  XRESOLUTION(0x011a, ExifDir.IMAGE_INFO, "X Resolution", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null), //rational not yet implemented
  YRESOLUTION(0x011b, ExifDir.IMAGE_INFO, "X Resolution", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null), //rational not yet implemented

  //--> weiter gehts bei Exif.SAMPLESPERPIXEL 0x0115


  //-------------- from here on: tree-builder in EditableRootItem (constructor) will stop to add the entries ------------
  //(because ExifDir.NONE is found for the first time)
  /*these values make no sense to be changed unless the picture content is changed*/
  NEWSUBFILETYPE(0x00fe, ExifDir.NONE, "SubfileType", ExifTagDataType.LONG, ExifTagGroup.IFD0, null), //Full-resolution image, reduced-resolution image ... //TIFF
  OLDSUBFILETYPE(0x00ff, ExifDir.NONE, "SubfileType", ExifTagDataType.LONG, ExifTagGroup.IFD0, null), //Full-resolution image, reduced-resolution image ... //TIFF
  IMAGEWIDTH(0x0100, ExifDir.NONE, "ImageWidth", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  IMAGEHEIGHT(0x0101, ExifDir.NONE, "ImageHeight", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  BITSPERSAMPLE(0x0102, ExifDir.NONE, "BitsPerSample", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null),
  COMPRESSION(0x103, ExifDir.NONE, "Compression", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),    //1=uncompressed, ... 7=JPEG, ... 34933 PNG, .., 65535=Pentax PEF Compressed
  PHOTOMETRICINTERPRETATION(0x0106, ExifDir.NONE, "PhotometricInterpretation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //0=White is Zero, 1=Black is Zero, 2=RGB...
  THRESHOLDING(0x107, ExifDir.NONE, "Thresholding", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //1=No dithering, 2=ordered dither, 3=randomized dither
  CELLWIDTH(0x108, ExifDir.NONE, "CellWidth", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  CELLLENGTH(0x109, ExifDir.NONE, "CellLength", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  FILLORDER(0x10a, ExifDir.NONE, "FillOrder", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null), //1=normal, 2=reversed
  STRIPOFFSETS(0x0111, ExifDir.NONE, "StripOffsets", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),    //PreviewImageStart IFD0, PreviewImageStart All, JpgFromRawStart SubIFD2
  SAMPLESPERPIXEL(0x0115, ExifDir.NONE, "SamplesPerPixel", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  ROWSPERSTRIP(0x0116, ExifDir.NONE, "RowsPerStrip", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  STRIPBYTECOUNTS(0x0117, ExifDir.NONE, "StripByteCounts", ExifTagDataType.LONG, ExifTagGroup.IFD0, null), //PreviewImageLength IFDO/All, JpgFromRawLength SubIFD2
  MINSAMPLEVALUE(0x0118, ExifDir.NONE, "MinSampleValue", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  MAXSAMPLEVALUE(0x0119, ExifDir.NONE, "MaxSampleValue", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null); //<<<<<<<<<<<<<<<<<<<< when continue: comma!!!!!!!!!!!!






  private final ExifDir exifDir;              //id of the ExifDirectory in which the tag shall be displayed

  private final int id;                       //Tag ID = Exif Entry Key
  private final String name;                  //description to be displayed instead of the tagID
  private final ExifTagDataType dataType;     //the type of the parameter
  private final ExifTagGroup group;           //grouping of ExifTags. If the standard does not define a group then use MISC
  private final Map<Integer, ?> lookupValues; //possible values allowed for that exif entry or null if all values allowed (if not look-up values)

  ExifTagInfo(int entryID, ExifDir exifDir, String entryName, ExifTagDataType dataType, ExifTagGroup group, Map<Integer, ?> lookupValues) {
    this.id = entryID;
    this.exifDir = exifDir;
    this.name = entryName;
    this.dataType = dataType;
    this.group = group;
    this.lookupValues = lookupValues;
  }

  public int getId() {
    return id;
  }

  public ExifDir getExifDir() {
    return exifDir;
  }

  public String getName() {
    return name;
  }

  public ExifTagDataType getDataType() {
    return dataType;
  }

  public ExifTagGroup getGroup() {
    return group;
  }

  public Map<Integer, ?> getLookupValues() {
    return lookupValues;
  }
}