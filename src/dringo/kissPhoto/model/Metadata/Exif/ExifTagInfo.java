package dringo.kissPhoto.model.Metadata.Exif;

import dringo.kissPhoto.model.Metadata.Exif.LookupValues.OrientationLookupValue;
import dringo.kissPhoto.model.Metadata.Exif.LookupValues.ResolutionUnitLookupValue;

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
  MODEL(0x110, ExifDir.OTHER, "Model", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  ORIENTATION(0x0112, ExifDir.IMAGE_INFO, "Orientation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, OrientationLookupValue.getValueMap()),
  XRESOLUTION(0x011a, ExifDir.IMAGE_INFO, "X Resolution", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null),
  YRESOLUTION(0x011b, ExifDir.IMAGE_INFO, "Y Resolution", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null),
  PAGENAME(0x011d, ExifDir.IMAGE_INFO, "Page Name", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  XPOSITION(0x011e, ExifDir.IMAGE_INFO, "X Position", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null),
  YPOSITION(0x011f, ExifDir.IMAGE_INFO, "Y Position", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null),
  RESOLUTIONUNIT(0x0128, ExifDir.IMAGE_INFO, "Resolution Unit", ExifTagDataType.SHORT, ExifTagGroup.IFD0, ResolutionUnitLookupValue.getValueMap()),
  PAGENUMBER(0x0129, ExifDir.IMAGE_INFO, "Page Number", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),  //array size 2
  SOFTWARE(0x0131, ExifDir.OTHER, "Software", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  MODIFYDATE(0x0132, ExifDir.DATE_TIME, "Modify Date", ExifTagDataType.DATE_TIME, ExifTagGroup.IFD0, null),
  ARTIST(0x013b, ExifDir.COPYRIGHT, "Artist", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  HOSTCOMPUTER(0x013c, ExifDir.OTHER, "Host Computer", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),


  //-------------- from here on: tree-builder in EditableRootItem (constructor) will stop to add the entries ------------
  //(because ExifDir.NONE is found for the first time)
  /*these values make no sense to be changed unless the picture content is changed*/
  NEWSUBFILETYPE(0x00fe, ExifDir.NONE, "Sub File Type", ExifTagDataType.LONG, ExifTagGroup.IFD0, null), //Full-resolution image, reduced-resolution image ... //TIFF
  OLDSUBFILETYPE(0x00ff, ExifDir.NONE, "Sub File Type", ExifTagDataType.LONG, ExifTagGroup.IFD0, null), //Full-resolution image, reduced-resolution image ... //TIFF
  IMAGEWIDTH(0x0100, ExifDir.NONE, "Image Width", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  IMAGEHEIGHT(0x0101, ExifDir.NONE, "Image Height", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  BITSPERSAMPLE(0x0102, ExifDir.NONE, "Bits Per Sample", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null),  //array size n
  COMPRESSION(0x103, ExifDir.NONE, "Compression", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),    //1=uncompressed, ... 7=JPEG, ... 34933 PNG, .., 65535=Pentax PEF Compressed
  PHOTOMETRICINTERPRETATION(0x0106, ExifDir.NONE, "Photometric Interpretation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //0=White is Zero, 1=Black is Zero, 2=RGB...
  THRESHOLDING(0x107, ExifDir.NONE, "Thresholding", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //1=No dithering, 2=ordered dither, 3=randomized dither
  CELLWIDTH(0x108, ExifDir.NONE, "Cell Width", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  CELLLENGTH(0x109, ExifDir.NONE, "Cell Length", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  FILLORDER(0x10a, ExifDir.NONE, "Fill Order", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null), //1=normal, 2=reversed
  STRIPOFFSETS(0x0111, ExifDir.NONE, "Strip Offsets", ExifTagDataType.LONG, ExifTagGroup.MISC, null),    //PreviewImageStart IFD0, PreviewImageStart All, JpgFromRawStart SubIFD2
  SAMPLESPERPIXEL(0x0115, ExifDir.NONE, "Samples Per Pixel", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  ROWSPERSTRIP(0x0116, ExifDir.NONE, "Rows Per Strip", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  STRIPBYTECOUNTS(0x0117, ExifDir.NONE, "Strip Byte Counts", ExifTagDataType.LONG, ExifTagGroup.MISC, null), //PreviewImageLength IFDO/All, JpgFromRawLength SubIFD2
  MINSAMPLEVALUE(0x0118, ExifDir.NONE, "Min Sample Value", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  MAXSAMPLEVALUE(0x0119, ExifDir.NONE, "Max Sample Value", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  PLANARCONFIGURATION(0x011c, ExifDir.NONE, "Planar Configuration", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //1=Chunky, 2=Planar
  FREEOFFSETS(0x0120, ExifDir.NONE, "Free Offsets", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  FREEBYTECOUNTS(0x0121, ExifDir.NONE, "Free Byte Counts", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  GRAYRESPONSEUNIT(0x0122, ExifDir.NONE, "Gray Response Unit", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //1=0.1, 2=0.001, 3=0.0001, 4=1e-05, 5=1e-06
  GRAYRESPONSECURVE(0x0123, ExifDir.NONE, "Gray Response Curve", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  T4OPTIONS(0x0124, ExifDir.NONE, "T4 Options", ExifTagDataType.NO, ExifTagGroup.MISC, null), //Bit 0=2-Dimensional encoding, Bit 1=Uncompressed, Bit2=Fill Bits added
  T6OPTIONS(0x0125, ExifDir.NONE, "T6 Options", ExifTagDataType.NO, ExifTagGroup.MISC, null), //Bit 1=Uncompressed
  COLORRESPONSEUNIT(0x012c, ExifDir.NONE, "Color Response Unit", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  TRANSFERFUNCTION(0x012d, ExifDir.NONE, "Transfer Function", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //array size 768
  PREDICTOR(0x013d, ExifDir.NONE, "Predictor", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //1=None, 2=Horizontal differencing
  WHITEPOINT(0x013e, ExifDir.NONE, "White Point", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null), //array size 2
  PRIMARYCHROMATICITIES(0x013f, ExifDir.NONE, "Primary Chromaticities", ExifTagDataType.RATIONAL, ExifTagGroup.IFD0, null), //array size 6
  COLORMAP(0x0140, ExifDir.NONE, "Color Map", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  HALFTONEHINTS(0x0141, ExifDir.NONE, "Halftone Hints", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null), //array size 2
  TILEWIDTH(0x0142, ExifDir.NONE, "Tile Width", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  TILELENGTH(0x0143, ExifDir.NONE, "Tile Length", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  TILEOFFSET(0x0144, ExifDir.NONE, "Tile Offset", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  TILEBYTECOUNTS(0x0145, ExifDir.NONE, "Tile Byte Counts", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  BADFAXLINES(0x0146, ExifDir.NONE, "Bad Fax Lines", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  CLEANFAXLINES(0x0147, ExifDir.NONE, "Clean Fax Lines", ExifTagDataType.NO, ExifTagGroup.MISC, null), //0=clean, 1=regenerated, 2=unclean
  CONSECUTIVEBADFAXLINES(0x0148, ExifDir.NONE, "Consecutive Bad Fax Lines", ExifTagDataType.NO, ExifTagGroup.MISC, null),
  //weiter bei 0x14a





  DUMMYLASTLine(0x0, ExifDir.NONE, "Eintrag löschen und darüber ein Strichpunkt setzen", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null);




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