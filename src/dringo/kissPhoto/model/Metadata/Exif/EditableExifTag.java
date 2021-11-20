package dringo.kissPhoto.model.Metadata.Exif;

import dringo.kissPhoto.model.Metadata.LookupValues.OrientationLookupValue;
import mediautil.image.jpeg.Exif;

import java.util.HashMap;
import java.util.Map;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class defines all editable Exif tags (=entries of an IFD) currently supported by kissPhoto
 * As mediautil is used for writing exif directories all entries are taken over from there
 * Note: currently only "simple" data types string, numbers are supported
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-10 First implementation
 * @since 2021-11-10
 */
public class EditableExifTag {
  private final int id;                    //Tag ID = Exif Entry Key
  private final String name;               //description to be displayed instead of the tagID
  private final ExifTagDataType dataType;  //the type of the parameter
  private final ExifTagGroup group;        //grouping of ExifTags. If the standard does not define a group then use MISC
  private final Map<Integer,?> lookupValues;          //possible values allowed for that exif entry or null if all values allowed (if not look-up values)

  private static Map<Integer, EditableExifTag> entryMap = new HashMap<>();

  static{

    entryMap.put(Exif.DOCUMENTNAME, new EditableExifTag(Exif.DOCUMENTNAME, "Document Name", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.IMAGEDESCRIPTION, new EditableExifTag(Exif.IMAGEDESCRIPTION, "Image Description", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.MAKE, new EditableExifTag(Exif.MAKE, "Make", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.MODEL, new EditableExifTag(Exif.MODEL, "Model", ExifTagDataType.ASCII, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.ORIENTATION, new EditableExifTag(Exif.ORIENTATION, "Orientation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, OrientationLookupValue.getValueMap() ));

    //--> weiter gehts bei Exif.SAMPLESPERPIXEL 0x0115


    /*these values make no sense to be changed unless the picture content is changed*/
    entryMap.put(Exif.NEWSUBFILETYPE, new EditableExifTag(Exif.NEWSUBFILETYPE, "SubfileType", ExifTagDataType.LONG, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.IMAGEWIDTH, new EditableExifTag(Exif.IMAGEWIDTH, "ImageWidth", ExifTagDataType.LONG, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.IMAGEHEIGHT, new EditableExifTag(Exif.IMAGEHEIGHT, "ImageHeight", ExifTagDataType.LONG, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.BITSPERSAMPLE, new EditableExifTag(Exif.BITSPERSAMPLE, "BitsPerSample", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.COMPRESSION, new EditableExifTag(Exif.COMPRESSION, "Compression", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.PHOTOMETRICINTERPRETATION, new EditableExifTag(Exif.PHOTOMETRICINTERPRETATION, "PhotometricInterpretation", ExifTagDataType.SHORT, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.FILLORDER, new EditableExifTag(Exif.FILLORDER, "FillOrder", ExifTagDataType.ARRAY_SHORT, ExifTagGroup.IFD0, null ));
    entryMap.put(Exif.STRIPOFFSETS, new EditableExifTag(Exif.STRIPOFFSETS, "PreviewImageStart", ExifTagDataType.LONG, ExifTagGroup.IFD0, null ));

  }

  private EditableExifTag(int entryID, String entryName, ExifTagDataType dataType, ExifTagGroup group, Map<Integer,?> lookupValues) {
    this.id = entryID;
    this.name = entryName;
    this.dataType = dataType;
    this.group = group;
    this.lookupValues = lookupValues;
  }


  public static EditableExifTag fromID(int entryID){
    return entryMap.get(entryID);
  }

  public int getId() {
    return id;
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

  public Map<Integer,?> getLookupValues() {
    return lookupValues;
  }
}

/*
 * todo
 * - Exif Tags, die unterstützt werden sollen hier eintragen
 * - EditibleTagItem..definieren
 * - Editable DirectoryItem Standard-struktur aufbauen mit EditableTagItems
 */