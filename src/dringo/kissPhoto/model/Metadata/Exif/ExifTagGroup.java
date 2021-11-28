package dringo.kissPhoto.model.Metadata.Exif;
/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This enum defines all tag groups Exif tags according to Exif Tags.pdf
 * These groups are used to group tags e.g. in metadata-extractor library
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-12
 * @since 2021-11-12
 */
public enum ExifTagGroup {
  //Tag types according to chapter 4.6.2 of Exif 2.3 standard (Jeita CP-3451B)
  INTEROP_IFD(1, "Interop-IFD"),  //e.g. tags Interop Index and Version
  IFD0(2, "IFD0"),                //e.g. tags Processing Software, Image Width, ...
  IFD2(3, "IFD2"),                //e.g. tag JpgFromRawLength
  SUB_IFD(4, "Sub-IFD"),          //e.g. tags Sample Format
  SUB_IFD1(5, "Sub-IFD1"),        //e.g. tag OtherImageLength
  SUB_IFD2(6, "Sub-IFD2"),        //e.g. tag OtherImageLength
  MAKER_NOTES(7, "Maker-Notes"),  //e.g. tags PreviewImageStart
  EXIF_IFD(8, "Exif-IFD"),        //e.g. tag OtherImageLength
  GPS (9,"GPS"),                  //e.g. tags GPSLatitude, GPSLatitudeRef
  XMP (10,"XMP"),                 //Extensible Metadata Platform, e.g. ACDSee-tags
  IPTC (11, "IPTC"),              //International Press Telecommunications Council, e.g. by-line, city, ...
  MISC (10,"MISC");               //"no group" will be treated as "misc"


  private final int value;
  private final String name;

  ExifTagGroup(int i, String name) {
    value = i;
    this.name =name;
  }

  public int getValue() {
    return value;
  }
  public String getName(){return name;}
}
