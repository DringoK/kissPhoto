package dringo.kissPhoto.model.Metadata.Exif;

import dringo.kissPhoto.model.Metadata.LookupValues.OrientationLookupValue;
import mediautil.image.jpeg.Exif;

import java.util.Map;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This enum defines all editable Exif tags (=entries of an IFD) currently supported by kissPhoto
 * together with all information about the tag from the exif standard (see EXIF tags.pdf or https://sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html)
 * Additionally the exifDir is defined in which it shall be displayed on GUI
 *
 * Note: currently only "simple" data types string, numbers are supported
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-10 First implementation
 * @since 2021-11-10
 */
public enum ExifTagInfo {
  DOCUMENTNAME(Exif.DOCUMENTNAME, ExifDir.IMAGE_DESCRIPTION, "Document Name", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  IMAGEDESCRIPTION(Exif.IMAGEDESCRIPTION, ExifDir.IMAGE_DESCRIPTION, "Image Description", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  MAKE(Exif.MAKE, ExifDir.OTHER, "Make", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  MODEL(Exif.MODEL, ExifDir.OTHER, "Model", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null),
  ORIENTATION(Exif.ORIENTATION, ExifDir.OTHER, "Orientation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, OrientationLookupValue.getValueMap()),

  //--> weiter gehts bei Exif.SAMPLESPERPIXEL 0x0115


  /*these values make no sense to be changed unless the picture content is changed*/
  NEWSUBFILETYPE(Exif.NEWSUBFILETYPE, ExifDir.NONE, "SubfileType", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  IMAGEWIDTH(Exif.IMAGEWIDTH, ExifDir.NONE, "ImageWidth", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  IMAGEHEIGHT(Exif.IMAGEHEIGHT, ExifDir.NONE, "ImageHeight", ExifTagDataType.LONG, ExifTagGroup.IFD0, null),
  BITSPERSAMPLE(Exif.BITSPERSAMPLE, ExifDir.NONE, "BitsPerSample", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null),
  COMPRESSION(Exif.COMPRESSION, ExifDir.NONE, "Compression", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  PHOTOMETRICINTERPRETATION(Exif.PHOTOMETRICINTERPRETATION, ExifDir.NONE, "PhotometricInterpretation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null),
  FILLORDER(Exif.FILLORDER, ExifDir.NONE, "FillOrder", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null),
  STRIPOFFSETS(Exif.STRIPOFFSETS, ExifDir.NONE, "PreviewImageStart", ExifTagDataType.LONG, ExifTagGroup.IFD0, null);

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