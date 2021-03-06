package dringo.kissPhoto.model.Metadata.Exif;

import dringo.kissPhoto.model.Metadata.Exif.LookupValues.OrientationLookupValue;
import dringo.kissPhoto.model.Metadata.Exif.LookupValues.ResolutionUnitLookupValue;
import dringo.kissPhoto.model.Metadata.Exif.LookupValues.SecurityClassLookupValue;

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
 * @version 2022-01-27 further tags added
 * @version 2021-11-10 First implementation
 * @since 2021-11-10
 */
public enum ExifTagInfo {
  DocumentName(0x10d, ExifDir.IMAGE_DESCRIPTION, "Document Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ImageDescription(0x10e, ExifDir.IMAGE_DESCRIPTION, "Image Description", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  Make(0x10f, ExifDir.OTHER, "Make", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  Model(0x110, ExifDir.OTHER, "Model", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  Orientation(0x0112, ExifDir.IMAGE_INFO, "Orientation", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, OrientationLookupValue.getValueMap()),
  XResolution(0x011a, ExifDir.IMAGE_INFO, "X Resolution", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  YResolution(0x011b, ExifDir.IMAGE_INFO, "Y Resolution", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  PageName(0x011d, ExifDir.IMAGE_INFO, "Page Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ResolutionUnit(0x0128, ExifDir.IMAGE_INFO, "Resolution Unit", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, ResolutionUnitLookupValue.getValueMap()),
  PageNumber(0x0129, ExifDir.IMAGE_INFO, "Page Number", ExifTagDataType.SHORT, 2, ExifTagGroup.IFD0, null),
  Software(0x0131, ExifDir.OTHER, "Software", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ModifyDate(0x0132, ExifDir.DATE_TIME,  "Date Time Modify", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.IFD0, null),
  Artist(0x013b, ExifDir.COPYRIGHT, "Artist", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  HostComputer(0x013c, ExifDir.OTHER, "Host Computer", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  Rating(0x4746, ExifDir.IMAGE_DESCRIPTION, "Rating", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  RatingPercent(0x4749, ExifDir.IMAGE_DESCRIPTION, "Rating Percent", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  Copyright(0x8298, ExifDir.COPYRIGHT, "Copyright", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  DateTimeOriginal(0x9003, ExifDir.DATE_TIME,  "Date Time Original (image taken)", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.EXIF_IFD, null),
  CreateDate(0x9004, ExifDir.DATE_TIME,  "Date Time Created (digitized)", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.EXIF_IFD, null),
  OffsetTime(0x9010, ExifDir.DATE_TIME,  "Time Zone Offset Modify", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.EXIF_IFD, null), //time zone for ModifyDate
  OffsetTimeOriginal(0x9011, ExifDir.DATE_TIME,  "Time Zone Offset Original (image taken)", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.EXIF_IFD, null), //time zone for DateTimeOriginal
  OffsetTimeDigitized(0x9012, ExifDir.DATE_TIME,  "Time Zone Offset Created (digitized)", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.EXIF_IFD, null), //time zone for CreateDate
  ImageNumber(0x9211, ExifDir.IMAGE_INFO, "Image Number", ExifTagDataType.LONG, 0, ExifTagGroup.EXIF_IFD, null),
  SecurityClassification(0x9212, ExifDir.IMAGE_INFO, "Security Classification", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, SecurityClassLookupValue.getValueMap()),
  ImageHistory(0x9213, ExifDir.IMAGE_INFO, "Image History", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  //UserComment(0x9286, ExifDir.IMAGE_DESCRIPTION, "User Comment", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null),  //UNDEFINED currently not supported!!!!!!!!!!!!!!!!!!
  //tags 0x9c9b-0x9c9f are used by Windows Explorer; special characters in these values are converted to UTF-8 by default, or Windows Latin1 with the -L option. XPTitle is ignored by Windows Explorer if ImageDescription exists
  //todo: implement UTF8
  XPTitle(0x9c9b, ExifDir.IMAGE_DESCRIPTION, "Win XP Title", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  XPComment(0x9c9c, ExifDir.IMAGE_DESCRIPTION, "Win XP Comment", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  XPAuthor(0x9c9d, ExifDir.IMAGE_DESCRIPTION, "Win XP Author", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null), //ignored by Windows Explorer if Artist exists
  XPKeywords(0x9c9e, ExifDir.IMAGE_DESCRIPTION, "Win XP Keywords", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  XPSubject(0x9c9f, ExifDir.IMAGE_DESCRIPTION, "Win XP Subject", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),

  ImageUniqueID(0xa420, ExifDir.COPYRIGHT, "Image Unique ID", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  OwnerName(0xa430, ExifDir.COPYRIGHT, "Camera Owner Name", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  CameraLabel(0xc7a1, ExifDir.OTHER, "Camera Label", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),


  //-------------- from here on: tree-builder in EditableRootItem (constructor) will stop to add the entries ------------
  //(because ExifDir.NONE is found for the first time)
  /*these values make no sense to be changed unless the picture content is changed*/
  NewSubfileType(0x00fe, ExifDir.NONE, "Sub File Type", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //Full-resolution image, reduced-resolution image ... //TIFF
  OldSubfileType(0x00ff, ExifDir.NONE, "Sub File Type", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //Full-resolution image, reduced-resolution image ... //TIFF
  ImageWidth(0x0100, ExifDir.NONE, "Image Width", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null),
  ImageHeight(0x0101, ExifDir.NONE, "Image Height", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null),
  BitsPerSample(0x0102, ExifDir.NONE, "Bits Per Sample", ExifTagDataType.SHORT, 1, ExifTagGroup.IFD0, null),  //array size n
  Compression(0x103, ExifDir.NONE, "Compression", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),    //1=uncompressed, ... 7=JPEG, ... 34933 PNG, .., 65535=Pentax PEF Compressed
  PhotometricInterpretation(0x0106, ExifDir.NONE, "Photometric Interpretation", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //0=White is Zero, 1=Black is Zero, 2=RGB...
  Thresholding(0x107, ExifDir.NONE, "Thresholding", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //1=No dithering, 2=ordered dither, 3=randomized dither
  CellWidth(0x108, ExifDir.NONE, "Cell Width", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  CellLength(0x109, ExifDir.NONE, "Cell Length", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  FillOrder(0x10a, ExifDir.NONE, "Fill Order", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //1=normal, 2=reversed
  StripOffsets(0x0111, ExifDir.NONE, "Strip Offsets", ExifTagDataType.LONG, 0, ExifTagGroup.MISC, null),    //PreviewImageStart IFD0, PreviewImageStart All, JpgFromRawStart SubIFD2
  SamplesPerPixel(0x0115, ExifDir.NONE, "Samples Per Pixel", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  RowsPerStrip(0x0116, ExifDir.NONE, "Rows Per Strip", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null),
  StripByteCounts(0x0117, ExifDir.NONE, "Strip Byte Counts", ExifTagDataType.LONG, 0, ExifTagGroup.MISC, null), //PreviewImageLength IFDO/All, JpgFromRawLength SubIFD2
  MinSampleValue(0x0118, ExifDir.NONE, "Min Sample Value", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  MaxSampleValue(0x0119, ExifDir.NONE, "Max Sample Value", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  PlanarConfiguration(0x011c, ExifDir.NONE, "Planar Configuration", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //1=Chunky, 2=Planar
  XPosition(0x011e, ExifDir.IMAGE_INFO, "X Position", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  YPosition(0x011f, ExifDir.IMAGE_INFO, "Y Position", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  FreeOffsets(0x0120, ExifDir.NONE, "Free Offsets", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FreeByteCounts(0x0121, ExifDir.NONE, "Free Byte Counts", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  GrayResponseUnit(0x0122, ExifDir.NONE, "Gray Response Unit", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //1=0.1, 2=0.001, 3=0.0001, 4=1e-05, 5=1e-06
  GrayResponseCurve(0x0123, ExifDir.NONE, "Gray Response Curve", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  T4Options(0x0124, ExifDir.NONE, "T4 Options", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //Bit 0=2-Dimensional encoding, Bit 1=Uncompressed, Bit2=Fill Bits added
  T6Options(0x0125, ExifDir.NONE, "T6 Options", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //Bit 1=Uncompressed
  ColorResponseUnit(0x012c, ExifDir.NONE, "Color Response Unit", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TransferFunction(0x012d, ExifDir.NONE, "Transfer Function", ExifTagDataType.SHORT, 768, ExifTagGroup.IFD0, null),
  Predictor(0x013d, ExifDir.NONE, "Predictor", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //1=None, 2=Horizontal differencing
  WhitePoint(0x013e, ExifDir.NONE, "White Point", ExifTagDataType.RATIONAL, 2, ExifTagGroup.IFD0, null),
  PrimaryChromaticities(0x013f, ExifDir.NONE, "Primary Chromaticities", ExifTagDataType.RATIONAL, 6, ExifTagGroup.IFD0, null),
  ColorMap(0x0140, ExifDir.NONE, "Color Map", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  HalftoneHints(0x0141, ExifDir.NONE, "Halftone Hints", ExifTagDataType.SHORT, 2, ExifTagGroup.IFD0, null),
  TileWidth(0x0142, ExifDir.NONE, "Tile Width", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null),
  TileLength(0x0143, ExifDir.NONE, "Tile Length", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null),
  TileOffset(0x0144, ExifDir.NONE, "Tile Offset", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TileByteCounts(0x0145, ExifDir.NONE, "Tile Byte Counts", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  BadFaxLines(0x0146, ExifDir.NONE, "Bad Fax Lines", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  CleanFaxLines(0x0147, ExifDir.NONE, "Clean Fax Lines", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=clean, 1=regenerated, 2=unclean
  ConsecutiveBadFaxLines(0x0148, ExifDir.NONE, "Consecutive Bad Fax Lines", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SubIFDA100DAtaOffset(0x014a, ExifDir.NONE, "SubIFD A100 Data Offset", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null), //see Exif Tags. The Data Offset in Original Sony DSLR A100 ARW images
  InkSet(0x014c, ExifDir.NONE, "Ink Set", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //1=CMYK, 2=not CMYK
  InkNames(0x014d, ExifDir.NONE, "Ink Names", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  NumberOfInks(0x014e, ExifDir.NONE, "Number of inks", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  DotRange(0x0150, ExifDir.NONE, "Dot Range", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TargetPrinter(0x0151, ExifDir.NONE, "Target Printer", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ExtraSapmles(0x0152, ExifDir.NONE, "Extra Sample", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=unspecified, 1=Associated Alpha, 2=Unassociated Alpha
  SampleFormat(0x0153, ExifDir.NONE, "Sample Format", ExifTagDataType.NO, 0, ExifTagGroup.SUB_IFD, null), //SamplePerPixel values [values 0-3] 1=unsigned, 2=signed, 3=float, 4=undefined, 5=complex int, 6=complex float
  SMinSampleValue(0x0154, ExifDir.NONE, "S Min Sample Value", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SMaxSampleValue(0x0155, ExifDir.NONE, "S Max Sample Value", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TransferRange(0x0156, ExifDir.NONE, "Transfer Range", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ClipPath(0x0157, ExifDir.NONE, "Clip Path", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  XClipPathUnits(0x0158, ExifDir.NONE, "X Clip Path Units", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  YClipPathUnits(0x0159, ExifDir.NONE, "Y Clip Path Units", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  Indexed(0x015a, ExifDir.NONE, "Indexed", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=not indexed, 1=indexed
  JPEGTables(0x015b, ExifDir.NONE, "JPEG Tables", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  OPIProxy(0x015f, ExifDir.NONE, "OPI Proxy", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //Open PrePress Interface. 0=Higher resolution image does not exist, 1= Higher resolution image exists
  GlobalParametersIFD(0x0190, ExifDir.NONE, "Global Parameters IFD", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //see EXIF tags
  ProfileType(0x0191, ExifDir.NONE, "Profile Type", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=unspecified, 1=Group3 FAX
  FAXProfile(0x0192, ExifDir.NONE, "Fax Profile", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=unknown, 1=minimal B&W lossles,S , 2=Extended B&W,F , 3=Lossles JBIG B&W,J , 4=Lossy color and grayscale,C , 5=Lossless color and grayscale,L , 6=Mixed raster content,M , 7=Profile T, 255=Multi Profiles
  CodingMethods(0x0193, ExifDir.NONE, "Coding Methods", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //Bit0=Unspecified Compression, Bit1=Modified Huffman, Bit2=Modified Read, Bit3=Modified MR, Bit4=JBIG, Bit5=Baseline JPEG, Bit6=JBIG color
  VersionYear(0x0194, ExifDir.NONE, "Version Year", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ModeNumber(0x0195, ExifDir.NONE, "Mode Number", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  Decode(0x01b1, ExifDir.NONE, "Decode", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  DefaultImageColor(0x01b2, ExifDir.NONE, "Default Image Color", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  T82Options(0x01b3, ExifDir.NONE, "T82 Options", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPEGTables1b5(0x01b5, ExifDir.NONE, "JPEG Tables 1b5", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPEGProc(0x0200, ExifDir.NONE, "JPEG Proc", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //1=Baseline, 14=Lossless
  ThumbnailOffset(0x0201, ExifDir.NONE, "Thumbnail Offset", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //also in IFD1, IFD2, SubIFD, SubIFD1, SubIFD2, MakerNotes,  aka PreviewImageStart, JpgFromRawStart, OtherImageStart
  ThumbnailLength(0x0202, ExifDir.NONE, "Thumbnail Length", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //also in IFD1, IFD2, SubIFD, SubIFD1, SubIFD2, MakerNotes,  aka PreviewImageLength, JpgFromRawLength, OtherImageLength
  JPEGRestartInterval(0x0203, ExifDir.NONE, "JPEG Restart Interval", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPEGLosslessPredictors(0x0205, ExifDir.NONE, "JPEG Lossless Predictors", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPEGPointTransforms(0x0206, ExifDir.NONE, "JPEG Point Transforms", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPEGQTables(0x0207, ExifDir.NONE, "JPEG Q Tables", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPEGDCTables(0x0208, ExifDir.NONE, "JPEG DC Tables", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPEGACTables(0x0209, ExifDir.NONE, "JPEG AC Tables", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  YCbCrCoefficients(0x0211, ExifDir.NONE, "YCbCr Coefficients", ExifTagDataType.RATIONAL, 3, ExifTagGroup.IFD0, null),
  YCbCrSubSampling(0x0212, ExifDir.NONE, "YCbCr Sub Sampling", ExifTagDataType.SHORT, 2, ExifTagGroup.IFD0, null), //11=YCbCr4:4:4, 12=YCbCr4:4:0, 14=YCbCr4:4:1, 21=YCbCr4:2:2, 22=YCbCr4:2:0, 24=YCbCr4:2:1, 41=YCbCr4:1:1, 42=YCbCr4:1:0
  YCbCrPositioning(0x0213, ExifDir.NONE, "YCbCr Positioning", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //1=Centered, 2=Co-sited
  ReferenceBlackWhite(0x0214, ExifDir.NONE, "Reference Black White", ExifTagDataType.RATIONAL, 6, ExifTagGroup.IFD0, null),
  StripRowCounts(0x022f, ExifDir.NONE, "Strip Row Counts", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ApplicationNotes(0x02bc, ExifDir.NONE, "Application Notes XMP", ExifTagDataType.BYTE, 0, ExifTagGroup.IFD0, null),
  USPTOMiscellaneous(0x03e7, ExifDir.NONE, "USPTO Miscellaneous", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //United Stated Patent and Trademark Office (USPTO)

  RelatedImageFileFormat(0x1000, ExifDir.NONE, "Related Image File Format", ExifTagDataType.ASCII, 0, ExifTagGroup.INTEROP_IFD, null),
  RelatedImageWidth(0x1001, ExifDir.NONE, "Related Image Width", ExifTagDataType.SHORT, 0, ExifTagGroup.INTEROP_IFD, null),
  RelatedImageHeight(0x1002, ExifDir.NONE, "Related Image Height/Length", ExifTagDataType.SHORT, 0, ExifTagGroup.INTEROP_IFD, null), //...length in DCF spec.

  XP_DIP_XML(0x4747, ExifDir.NONE, "XP DIP XML", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  StichInfo(0x4748, ExifDir.NONE, "Microsoft Stitch Info", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),

  SonyRawFileType(0x7000, ExifDir.NONE, "Sony Raw File Type", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Sony Uncompressed 14bit RAW, 1=Sony Uncompressed 12bit RAW, 2=Sony Compressed RAW, 3=Sony Lossless Compressed RAW
  VignettingCorrParams(0x7032, ExifDir.NONE, "Vignetting Correction Parameters", ExifTagDataType.SHORT, 17, ExifTagGroup.SUB_IFD, null), //Sony ARW images
  ChromaticAberrationCorrParams(0x7035, ExifDir.NONE, "Chromatic Aberration Correction Parameters", ExifTagDataType.SHORT, 33, ExifTagGroup.SUB_IFD, null), //Sony ARW images
  DistortionCorrParams(0x7037, ExifDir.NONE, "Distortion Correction Parameters", ExifTagDataType.SHORT, 17, ExifTagGroup.SUB_IFD, null), //Sony ARW images

  ImageID(0x800d, ExifDir.NONE, "Image ID", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  WangTag1(0x80a3, ExifDir.NONE, "Wang Tag 1", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  WangAnnotation(0x80a4, ExifDir.NONE, "Wang Annotation", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  WangTag3(0x80a5, ExifDir.NONE, "Wang Tag 3", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  WangTag4(0x80a6, ExifDir.NONE, "Wang Tag 4", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageReferencePoints(0x80b9, ExifDir.NONE, "Image Reference Points", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  RegionXformTackPoint(0x80ba, ExifDir.NONE, "Region Xform Tack Point", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  WarpQuadrilateral(0x80bb, ExifDir.NONE, "Warp Quadrilateral", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  AffineTransformMat(0x80bc, ExifDir.NONE, "Affine Transformation Matrix", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  Matteing(0x80e3, ExifDir.NONE, "Matteing", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //matte archive paper
  DataType(0x80e4, ExifDir.NONE, "Data Type", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageDepth(0x80e5, ExifDir.NONE, "Image Depth", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TileDepth(0x80e6, ExifDir.NONE, "Tile Depth", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageFullWidth(0x8214, ExifDir.NONE, "Image Full Width", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageFullHeight(0x8215, ExifDir.NONE, "Image Full Height", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TextureFormat(0x8216, ExifDir.NONE, "Texture Format", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  WrapModes(0x8217, ExifDir.NONE, "Wrap Modes", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FovCot(0x8218, ExifDir.NONE, "Field of View Cotangent", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MatrixWorldToScreen(0x8219, ExifDir.NONE, "Matrix World to Screen", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MatrixWorldToCamera(0x821a, ExifDir.NONE, "Matrix World to Camera", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  Model2(0x827d, ExifDir.NONE, "Model 2", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  CFARepeatPatternDim(0x828d, ExifDir.NONE, "CFA Repeat Pattern Dim", ExifTagDataType.SHORT, 2, ExifTagGroup.SUB_IFD, null),
  CFAPattern2(0x828e, ExifDir.NONE, "CFA Pattern 2", ExifTagDataType.BYTE, 1, ExifTagGroup.SUB_IFD, null),
  BatteryLevel(0x828f, ExifDir.NONE, "Battery Level", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  KodakIFD(0x8290, ExifDir.NONE, "Kodak IFD", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //used in various types of Kodak images
  ExposureTime(0x829a, ExifDir.IMAGE_INFO, "Exposure Time", ExifTagDataType.FRACTION, 0, ExifTagGroup.EXIF_IFD, null),
  FNumber(0x829d, ExifDir.IMAGE_INFO, "F-Number", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null), //Blendenzahl
  MDFileTag(0x82a5, ExifDir.NONE, "Molecular Dynamics File Tag", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //tags 0x82a5-0x82ac are used in Molecular Dynamics GEL files
  MDScalePixel(0x82a6, ExifDir.NONE, "Molecular Dynamics Scale Pixel", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MDColorTable(0x82a7, ExifDir.NONE, "Molecular Dynamics Color Table", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MDLabName(0x82a8, ExifDir.NONE, "Molecular Dynamics Lab Name", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MDSampleInfo(0x82a9, ExifDir.NONE, "Molecular Dynamics Sample Info", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MDPrepDate(0x82aa, ExifDir.NONE, "Molecular Dynamics Prep Date", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MDPrepTime(0x82ab, ExifDir.NONE, "Molecular Dynamics Prep Time", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MDFileUnits(0x82ac, ExifDir.NONE, "Molecular Dynamics File Units", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  PixelScale(0x830e, ExifDir.NONE, "Pixel Scale", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  AdventScale(0x8335, ExifDir.NONE, "Advent Scale", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  AdventRevision(0x8336, ExifDir.NONE, "Advent Revision", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  UIC1Tag(0x835c, ExifDir.NONE, "UIC 1 Tag", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),   //UIC=University of Illinois at Chicago
  UIC2Tag(0x835d, ExifDir.NONE, "UIC 2 Tag", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),   //UIC=University of Illinois at Chicago
  UIC3Tag(0x835e, ExifDir.NONE, "UIC 3 Tag", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),   //UIC=University of Illinois at Chicago
  UIC4Tag(0x835f, ExifDir.NONE, "UIC 4 Tag", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),   //UIC=University of Illinois at Chicago
  IPTCNAA(0x83bb, ExifDir.NONE, "IPTC NAA", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null),  //IPTC=International Press Telecommunications Council NAA=Newspaper Association of America
  IntergraphPacketData(0x847e, ExifDir.NONE, "Intergraph Packet Data", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  IntergraphFlagRegisters(0x847f, ExifDir.NONE, "Intergraph Flag Registers", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  IntergraphMatrix(0x8480, ExifDir.NONE, "Intergraph Matrix", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  INGRReserved(0x8481, ExifDir.NONE, "INGR Reserved", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ModelTiePoint(0x8482, ExifDir.NONE, "Model Tie Point", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  Site(0x84e0, ExifDir.NONE, "Site", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ColorSequence(0x84e1, ExifDir.NONE, "Color Sequence", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  IT8Header(0x84e2, ExifDir.NONE, "IT8 Header", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  RasterPadding(0x84e3, ExifDir.NONE, "Raster Padding", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Byte, 1=Word, 2=LongWord, 9=Sector, 10=LongSector
  BitsPerRunLength(0x84e4, ExifDir.NONE, "Bits per Run Length", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  BitsPerExtendedRunLength(0x84e5, ExifDir.NONE, "Bits per Extended Run Length", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ColorTable(0x84e6, ExifDir.NONE, "Color Table", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageColorIndicator(0x84e7, ExifDir.NONE, "Image Color Indicator", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Unspecified Image Color 1=Specified Image Color
  BackgroundColorIndicator(0x84e8, ExifDir.NONE, "Background Color Indicator", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Unspecified Background Color 1=Specified Background Color
  ImageColorValue(0x84e9, ExifDir.NONE, "Image Color Value", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  BackgroundColorValue(0x84ea, ExifDir.NONE, "Background Color Value", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  PixelIntensityRange(0x84eb, ExifDir.NONE, "Pixel Intensity Range", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TransparencyIndicator(0x84ec, ExifDir.NONE, "Transparency Indicator", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ColorCharacterization(0x84ed, ExifDir.NONE, "Color Characterization", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  HCUsage(0x84ee, ExifDir.NONE, "HC Usage", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),  //0=CT, 1=LineArt, 2=Trap
  TrapIndicator(0x84ef, ExifDir.NONE, "Trap Indicator", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  CMYKEquivalent(0x84f0, ExifDir.NONE, "CMYK Equivalent", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SEMInfo(0x8546, ExifDir.NONE, "Scanning Electron Microscope Info", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null), //found in some scanning electron microscope images
  AFCP_IPTC(0x8568, ExifDir.NONE, "AFCP_IPTC", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //see IPTC Tags
  PixelMagicJBIGOptions(0x85b8, ExifDir.NONE, "Pixel Magic JBIG Options", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  JPLCartoIFD(0x85d7, ExifDir.NONE, "JPL Carto IFD", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //JPL=Nasa Jet Propulsion Laboratory
  ModelTransform(0x85d8, ExifDir.NONE, "Model Transform", ExifTagDataType.DOUBLE, 16, ExifTagGroup.IFD0, null),
  WB_GRGBLevels(0x8602, ExifDir.NONE, "WB G RGB Levels", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null),   //Found in IFD0 of Leaf MOS images
  LeafData(0x8606, ExifDir.NONE, "Leaf Data", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null),   //see Leaf Tags
  PhotoshopSettings(0x8649, ExifDir.NONE, "Photoshop Settings", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null),   //see Photoshop Tags
  ExifOffset(0x8769, ExifDir.NONE, "Exif Offset", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null),   //see Exif Tags
  ICCProfile(0x8773, ExifDir.NONE, "ICC Profile", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null),   //ICC=International Color Consortium, see ICC Profile Tags
  TIFF_FXExtensions(0x877f, ExifDir.NONE, "TIFF FX Extensions", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),   //Bit 0=Resolution/Image Width, Bit1=N Layer Profile M, Bit2=Shared Data, Bit3=B&W JBIG2, Bit4=JBIG2 Profile M
  MultiProfiles(0x8780, ExifDir.NONE, "Multi Profiles", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),   //Bit 0=Profile S, 1=F, 2=J, 3=C, 4=L, 5=M, 6=T, 7=Resolution/Image Width, 8=N Layer Profile M, 9=Shared Data, 10=JBIG2 Profile M
  SharedData(0x8781, ExifDir.NONE, "Shared Data", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  T88Options(0x8782, ExifDir.NONE, "T88 Options", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageLayer(0x87ac, ExifDir.NONE, "ImageLayer", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  GeoTiffDirectory(0x87af, ExifDir.NONE, "Geo TIFF Directory", ExifTagDataType.SHORT, 5, ExifTagGroup.IFD0, null), //[0.5]?? Geo Tiff Tags may be read and written as a block, but they aren't extracted unless specifically requested. Byte order changes are handled automatically when copying between TIFF images with different byte order.
  GeoTiffDoubleParams(0x87b0, ExifDir.NONE, "Geo TIFF Double Parameters", ExifTagDataType.DOUBLE, 125, ExifTagGroup.IFD0, null), //[0.125]??
  GeoTiffAsciiParams(0x87b1, ExifDir.NONE, "Geo TIFF ASCII Parameters", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  JBIGOptions(0x87be, ExifDir.NONE, "JBIG Options", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ExposureProgram(0x8822, ExifDir.NONE, "Exposure Program", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Not Defined, 1=Manual, 2=Program AE, 3=Aperture-priority AE, 4=shutter speed priority AE, 5=creative (Slow speed), 6=Action (High speed), 7=Portrait, 8=Landscape, 9=Bulb (used by Canon EOS 7D)
  SpectralSensitivity(0x8824, ExifDir.NONE, "Spectral Sensitivity", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  GPSInfo(0x8825, ExifDir.NONE, "GPS Info", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null), //see GPS Tags
  ISO(0x8827, ExifDir.NONE, "ISO Speed/Photographic Sensitivity", ExifTagDataType.SHORT, 1, ExifTagGroup.EXIF_IFD, null),
  OECF(0x8828, ExifDir.NONE, "Opto-Electric Conv Factor", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  INTERLACE(0x8829, ExifDir.NONE, "Interlace", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TimeZoneOffset(0x882a, ExifDir.NONE,  "Time Zone Offset", ExifTagDataType.SHORT, 1, ExifTagGroup.EXIF_IFD, null), //1st value: offset of DateTimeOriginal from GMT in hours, 2nd value optional: offset of ModifyDate
  SelfTimerMode(0x882b, ExifDir.NONE, "Self Timer Mode", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null),
  SensitivityType(0x8830, ExifDir.NONE, "Sensitivity Type", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //applies to 0x8827(ISO): 0=Unknown, 1=Standard Output Sensitivity, 2=Recommended Exposure index, 3=ISO Speed, 4=1&2, 5=1&3, 6=2&3, 7=1&2&3
  StandardOutputSensitivity(0x8831, ExifDir.NONE, "Standard Output Sensitivity", ExifTagDataType.LONG, 0, ExifTagGroup.EXIF_IFD, null),
  RecommendedExposureIndex(0x8832, ExifDir.NONE, "Recommended Exposure Index", ExifTagDataType.LONG, 0, ExifTagGroup.EXIF_IFD, null),
  ISOSpeed(0x8833, ExifDir.NONE, "ISO Speed", ExifTagDataType.LONG, 0, ExifTagGroup.EXIF_IFD, null),
  ISOSpeedLatitudeyyy(0x8834, ExifDir.NONE, "ISO Speed Latitude yyy", ExifTagDataType.LONG, 0, ExifTagGroup.EXIF_IFD, null),
  ISOSpeedLatitudezzz(0x8835, ExifDir.NONE, "ISO Speed Latitude zzz", ExifTagDataType.LONG, 0, ExifTagGroup.EXIF_IFD, null),
  FaxRecvParams(0x885c, ExifDir.NONE, "Fax Receive Parameters", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FaxSubAddress(0x885d, ExifDir.NONE, "Fax Sub Address", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FaxRecvTime(0x885e, ExifDir.NONE, "Fax Receive Time", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FedexEDR(0x8871, ExifDir.NONE, "Fedex EDR", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  LeafSubIFD(0x888a, ExifDir.NONE, "Leaf Sub IFD", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //see Leaf SubIFD Tags

  ExifVersion(0x9000, ExifDir.NONE, "Exif Version", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null), //type
  GooglePlusUploadCode(0x9009, ExifDir.NONE,  "Google Plus Upload Code", ExifTagDataType.UNDEFINED, 1, ExifTagGroup.EXIF_IFD, null),
  ComponentsConfiguration(0x9101, ExifDir.IMAGE_INFO,  "Components Configuration", ExifTagDataType.UNDEFINED, 4, ExifTagGroup.EXIF_IFD, null), //0=-, 1=Y, 2=Cb, 3=Cr, 4=R, 5=G, 6=B
  CompressedBitsPerPixel(0x9102, ExifDir.IMAGE_INFO,  "Compressed Bits per Pixel", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  ShutterSpeedValue(0x9201, ExifDir.IMAGE_INFO,  "Shutter Speed Value", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.EXIF_IFD, null), //displayed in seconds but stored as an APEX value
  ApertureValue(0x9202, ExifDir.IMAGE_INFO,  "Aperture Value", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null), //displayed as an F number but stored as an APEX value
  BrightnessValue(0x9203, ExifDir.IMAGE_INFO,  "Brightness Value", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  ExposureCompensation(0x9204, ExifDir.IMAGE_INFO,  "Exposure Compensation/Bias value", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  MaxApertureValue(0x9205, ExifDir.IMAGE_INFO,  "Max Aperture Value", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null), //displayed as an F number but stored as an APEX value
  SubjectDistance(0x9206, ExifDir.IMAGE_INFO,  "Subject Distance", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  MeteringMode(0x9207, ExifDir.IMAGE_INFO,  "MeteringMode", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=unknown, 1=Average, 2=Center-weighted average, 3=Spot, 4=Multi-Spot, 5=Multi-segment, 6=partial, 255=other
  LightSource(0x9208, ExifDir.IMAGE_INFO,  "Light Source", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //see EXIF LightSource Values
  Flash(0x9209, ExifDir.IMAGE_INFO,  "Flash Value", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //see Flash Values
  FocalLength(0x920a, ExifDir.IMAGE_INFO,  "Focal Length", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  FlashEnergy(0x920b, ExifDir.IMAGE_INFO,  "Flash Energy", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SpatialFrequencyResponse(0x920c, ExifDir.IMAGE_INFO,  "Spatial Frequency Response", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  Noise(0x920d, ExifDir.IMAGE_INFO,  "Noise", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FocalPlaneXResolution(0x920e, ExifDir.IMAGE_INFO,  "Focal Plane X Resolution", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FocalPlaneYResolution(0x920f, ExifDir.IMAGE_INFO,  "Focal Plane Y Resolution", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FocalPlaneResolutionUnit(0x9210, ExifDir.IMAGE_INFO,  "Focal Plane Resolution Unit", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //1=None, 2=inches, 3=cm, 4=mm
  SubjectArea(0x9214, ExifDir.IMAGE_INFO, "Subject Area", ExifTagDataType.SHORT, 1, ExifTagGroup.EXIF_IFD, null),
  ExposureIndex(0x9215, ExifDir.IMAGE_INFO, "Exposure Index", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  TIFFEPStandardID(0x9216, ExifDir.NONE, "TIFF EP Standard ID", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SensingMethod(0x9217, ExifDir.IMAGE_INFO, "Sensing Method", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //1=Monochrome area, 2=One-Chip color area, 3=Two-Chip color area, 4=Three-Chip color area, 5=Color sequential area, 6=Monochrome linear, 7=Tri linear, 8=Color sequential linear
  CIP3DataFile(0x923a, ExifDir.NONE, "CIP3 Data File", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //CIP= International Cooperation for Integration of pre press, press and post press
  CIP3Sheet(0x923b, ExifDir.NONE, "CIP3 Sheet", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  CIP3Side(0x923c, ExifDir.NONE, "CIP3 Side", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MakerNote(0x927c, ExifDir.NONE, "Maker Note", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null),  //see Apple-Tags, Nikon, Canon, Casio, DJI, FLIR, FujiFilm, HP, Kodak, Kyocera, Minolta, Olympus, Leica, Panasonic, Pentax, PhaseOne, Recony, Ricoh, Samsung, Sanyo, Sigma, Sony, Ericsson, Unknown
  SubSecTime(0x9290, ExifDir.DATE_TIME,  "Fractional Seconds for Modify", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null), //time zone for ModifyDate
  SubSecTimeOriginal(0x9291, ExifDir.DATE_TIME,  "Fractional Seconds for Original (image taken)", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null), //time zone for DateTimeOriginal
  SubSecTimeDigitized(0x9292, ExifDir.DATE_TIME,  "Fractional Seconds for Created (digitized)", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null), //time zone for CreateDate
  MSDocumentText(0x932f, ExifDir.NONE, "MS Document Text", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MSPropertySetStorage(0x9330, ExifDir.NONE, "MS Property Set Storage", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  MSDocumentTextPosition(0x9331, ExifDir.NONE, "MS Document Text Position", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageSourceData(0x935c, ExifDir.NONE, "Image Source Data", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null), //see: Photoshop Document Data Tags
  AmbientTemperature(0x9400, ExifDir.IMAGE_INFO, "Ambient Temperature [celsius]", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.EXIF_IFD, null),  //called Temperature by the EXIF spec
  Humidity(0x9401, ExifDir.IMAGE_INFO, "Humidity [percent]", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  Pressure(0x9402, ExifDir.IMAGE_INFO, "Air Pressure [hPa/mbar]", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  WaterDepth(0x9403, ExifDir.IMAGE_INFO, "Under Water Depth [m]", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.EXIF_IFD, null), //negative for above water
  Acceleration(0x9404, ExifDir.IMAGE_INFO, "Camera Acceleration (directionless) [mGal or 10E-5 m/s2]", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  CameraElevationAngle(0x9405, ExifDir.IMAGE_INFO, "Camera Elevation Angle [degree]", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.EXIF_IFD, null),

  FlashpixVersion(0xa000, ExifDir.NONE, "Flashpix Version", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null),
  //the value of 0x2 is not standard EXIF. Instead, an Adobe RGB image is indicated by "Uncalibrated" with an InteropIndex of "R03". The values 0xfffd and 0xfffe are also non-standard, and are used by some Sony cameras)
  ColorSpace(0xa001, ExifDir.IMAGE_INFO, "Color Space", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0x1 = sRGB 0x2 = Adobe RGB 0xfffd = Wide Gamut RGB 0xfffe = ICC Profile 0xffff = Uncalibrated
  ExifImageWidth(0xa002, ExifDir.IMAGE_INFO, "EXIF Image Width [pixel]", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //called PixelXDimension by the EXIF spec.
  ExifImageHeight(0xa003, ExifDir.IMAGE_INFO, "EXIF Image Height [pixel]", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //called PixelYDimension by the EXIF spec.
  RelatedSoundFile(0xa004, ExifDir.IMAGE_DESCRIPTION, "Related Sound File", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  InteropOffset(0xa005, ExifDir.NONE, "Inter op Offset", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //see EXIF Tags
  SamsungRawPointerOffset(0xa010, ExifDir.NONE, "Samsung RAW Pointers Offset", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SamsungRawPointerLength(0xa011, ExifDir.NONE, "Samsung RAW Pointers Length", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SamsungRawByteOrder(0xa101, ExifDir.NONE, "Samsung RAW Byte Order", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SamsungRawUnknown(0xa102, ExifDir.NONE, "Samsung RAW Unknown", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FlashEnergyA2(0xa20b, ExifDir.IMAGE_INFO, "Flash Energy", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  SpatialFrequencyResponseA2(0xa20c, ExifDir.IMAGE_INFO, "Spatial Frequency Response", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  NoiseA2(0xa20d, ExifDir.IMAGE_INFO, "Noise", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  FocalPlaneXResolutionA2(0xa20e, ExifDir.IMAGE_INFO, "Focal Plane X Resolution", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  FocalPlaneYResolutionA2(0xa20f, ExifDir.IMAGE_INFO, "Focal Plane Y Resolution", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  FocalPlaneResolutionUnitA2(0xa210, ExifDir.IMAGE_INFO, "Focal Plane Resolution Unit", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //1=None*, 2=inches, 3=cm, 4=mm*, 5=um*; *1,4,5 not standard EXIF
  ImageNumberA2(0xa211, ExifDir.IMAGE_DESCRIPTION, "Image Number", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SecurityClassificationA2(0xa212, ExifDir.IMAGE_DESCRIPTION, "Security Classification", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageHistoryA2(0xa213, ExifDir.IMAGE_DESCRIPTION, "Image History", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SubjectLocation(0xa214, ExifDir.IMAGE_DESCRIPTION, "Subject Location", ExifTagDataType.SHORT, 2, ExifTagGroup.EXIF_IFD, null),
  ExposureIndexA2(0xa215, ExifDir.IMAGE_INFO, "Exposure Index", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  TIFF_EPSStandardID(0xa216, ExifDir.NONE, "TIFF EPS Standard ID", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SensingMethodA2(0xa217, ExifDir.IMAGE_INFO, "Sensing Method", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //1=not defined, 2=One-chip color area, 3=Two-chip color area, 4=Three-chip color area, 7=Trilinear, 8=Color sequential linear
  FileSource(0xa300, ExifDir.IMAGE_INFO, "File Source", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null), //1=Film Scanner, 2=Reflection Print Scanner, 3=Digital Camera, "\x03\x00\x00\x00" = Sigma Digital Camera
  SceneType(0xa301, ExifDir.IMAGE_INFO, "Scene Type", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null), //1=directly photographed
  CFAPattern(0xa302, ExifDir.NONE, "CFAPattern", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null),
  CustomRendered(0xa401, ExifDir.IMAGE_INFO, "Custom Rendered", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0,1=Standard,other: Apple. 0=Normal, 1=Custom, 2=HDR (no original saved), 3=HDR (original saved), 4 = Original (for HDR), 6 = Panorama, 7 = Portrait HDR, 8 = Portrait
  ExposureMode(0xa402, ExifDir.IMAGE_INFO, "Exposure Mode", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Auto, 1=Manual, 2=Auto bracket
  WhiteBalance(0xa403, ExifDir.IMAGE_INFO, "White Balance", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Auto, 1=Manual
  DigitalZoomRatio(0xa404, ExifDir.IMAGE_INFO, "Digital Zoom Ratio", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  FocalLengthIn35mmFormat(0xa405, ExifDir.IMAGE_INFO, "Focal Length in 35mm Film", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null),
  SceneCaptureType(0xa406, ExifDir.IMAGE_INFO, "SceneCaptureType", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Standard, 1=Landscape, 2=Portrait, 3=Night 4=other(Samsung only)
  GainControl(0xa407, ExifDir.IMAGE_INFO, "Gain Control", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=None, 1=Low gain up, 2=High gain up, 3=Low gain down, 4=High gain down
  Contrast(0xa408, ExifDir.IMAGE_INFO, "Contrast", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Normal, 1=Low, 2=High
  Saturation(0xa409, ExifDir.IMAGE_INFO, "Saturation", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Normal, 1=Low, 2=High
  Sharpness(0xa40a, ExifDir.IMAGE_INFO, "Sharpness", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Normal, 1=Soft, 2=Hard
  DeviceSettingDescription(0xa40b, ExifDir.IMAGE_INFO, "Device Setting Description", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  SubjectDistanceRange(0xa40c, ExifDir.IMAGE_INFO, "Subject Distance Range", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Unknown, 1=Macro, 2=Close, 3=Distant
  SerialNumber(0xa431, ExifDir.IMAGE_INFO, "Body Serial Number", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  LensInfo(0xa432, ExifDir.IMAGE_INFO, "Lens Info", ExifTagDataType.RATIONAL, 4, ExifTagGroup.EXIF_IFD, null), //4 values giving focal and aperture ranges, called Lens Specification by the Exif spec
  LensMake(0xa433, ExifDir.IMAGE_INFO, "Lens Make", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  LensModel(0xa434, ExifDir.IMAGE_INFO, "Lens Model", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  LensSerialNumber(0xa435, ExifDir.IMAGE_INFO, "Lens Serial Number", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  CompositeImage(0xa460, ExifDir.IMAGE_INFO, "Composite Image", ExifTagDataType.SHORT, 0, ExifTagGroup.EXIF_IFD, null), //0=Unknown, 1=Not a Composite Image, 2=General Composite Image, 3=Composite Image Captured While Shooting
  CompositeImageCount(0xa461, ExifDir.IMAGE_INFO, "Source Image Number of Composite Image", ExifTagDataType.SHORT, 2, ExifTagGroup.EXIF_IFD, null), //2 values: 1. Number of source images 2. Number of images used.
  //CompositeImageExposureTimes: 11 or more values: 1. Total exposure time period, 2. Total exposure of all source images, 3. Total exposure of all used images, 4. Max exposure time of source images, 5. Max exposure time of used images, 6. Min exposure time of source images, 7. Min exposure of used images, 8. Number of sequences, 9. Number of source images in sequence. 10-N. Exposure times of each source image.
  CompositeImageExposureTimes(0xa462, ExifDir.IMAGE_INFO, "Composite Image Exposure Time", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null), //Called SourceExposureTimesOfCompositeImage by the EXIF spec
  GDALMetadata(0xa480, ExifDir.IMAGE_INFO, "GDAL Metadata", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null), //GDAL=Geospatial Data Abstraction Library
  GDALNoData(0xa481, ExifDir.IMAGE_INFO, "GDAL No Data", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null), //GDAL=Geospatial Data Abstraction Library
  Gamma(0xa500, ExifDir.IMAGE_INFO, "Gamma", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  ExpandSoftware(0xafc0, ExifDir.NONE, "Expand Software", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ExpandLens(0xafc1, ExifDir.NONE, "Expand Lens", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ExpandFilm(0xafc2, ExifDir.NONE, "Expand Film", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ExpandFilterLens(0xafc3, ExifDir.NONE, "Expand Filter Lens", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ExpandScanner(0xafc4, ExifDir.NONE, "Expand Scanner", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ExpandFlashLamp(0xafc5, ExifDir.NONE, "Expand Flash Lamp", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),

  HasselbladRawImage(0xb4c3, ExifDir.NONE, "Hasselblad RAW Image", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  PixelFormat(0xbc01, ExifDir.NONE, "Pixel Format", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //(tags 0xbc** are used in Windows HD Photo (HDP and WDP) images. The actual javafx.scene.image.PixelFormat values are 16-byte GUID's but the leading 15 bytes, '6fddc324-4e03-4bfe-b1853-d77768dc9', have been removed below to avoid unnecessary clutter
  //0x5 = Black & White, 0x8 = 8-bit Gray, 0x9 = 16-bit BGR555, 0xa = 16-bit BGR565, 0xb = 16-bit Gray, 0xc = 24-bit BGR, 0xd = 24-bit RGB, 0xe = 32-bit BGR, 0xf = 32-bit BGRA
  //0x10 = 32-bit PBGRA, 0x11 = 32-bit Gray Float, 0x12 = 48-bit RGB Fixed Point, EXIF Tags https://exiftool.org/TagNames/EXIF.html, 15 von 23 30.01.2022, 12:30, 0x13 = 32-bit BGR101010, 0x15 = 48-bit RGB, 0x16 = 64-bit RGBA,
  //0x17 = 64-bit PRGBA, 0x18 = 96-bit RGB Fixed Point, 0x19 = 128-bit RGBA Float, 0x1a = 128-bit PRGBA Float, 0x1b = 128-bit RGB Float, 0x1c = 32-bit CMYK, 0x1d = 64-bit RGBA Fixed Point, 0x1e = 128-bit RGBA Fixed Point, 0x1f = 64-bit CMYK
  //0x20 = 24-bit 3 Channels, 0x21 = 32-bit 4 Channels, 0x22 = 40-bit 5 Channels, 0x23 = 48-bit 6 Channels, 0x24 = 56-bit 7 Channels, 0x25 = 64-bit 8 Channels, 0x26 = 48-bit 3 Channels, 0x27 = 64-bit 4 Channels, 0x28 = 80-bit 5 Channels, 0x29 = 96-bit 6 Channels
  //0x2a = 112-bit 7 Channels, 0x2b = 128-bit 8 Channels, 0x2c = 40-bit CMYK Alpha, 0x2d = 80-bit CMYK Alpha, 0x2e = 32-bit 3 Channels Alpha, 0x2f = 40-bit 4 Channels Alpha
  //0x30 = 48-bit 5 Channels Alpha, 0x31 = 56-bit 6 Channels Alpha, 0x32 = 64-bit 7 Channels Alpha, 0x33 = 72-bit 8 Channels Alpha, 0x34 = 64-bit 3 Channels Alpha, 0x35 = 80-bit 4 Channels Alpha, 0x36 = 96-bit 5 Channels Alpha, 0x37 = 112-bit 6 Channels Alpha
  //0x38 = 128-bit 7 Channels Alpha, 0x39 = 144-bit 8 Channels Alpha, 0x3a = 64-bit RGBA Half, 0x3b = 48-bit RGB Half, 0x3d = 32-bit RGBE, 0x3e = 16-bit Gray Half, 0x3f = 32-bit Gray Fixed Point
  Transformation(0xbc02, ExifDir.NONE, "Transformation", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Horizontal(normal), 1=Mirror vertical, 2=Mirror horizontal, 3=Rotate 180, 4=Rotate 90CW, 5=Mirror horizontal and rotate 90CW, 6=Mirror horizontal and rotate 270CW, 7=Rotate 270CW
  Uncompressed(0xbc03, ExifDir.NONE, "Uncompressed", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=No, 1=Yes
  ImageType(0xbc04, ExifDir.NONE, "Image Type", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //Bit0=Preview, Bit1=Page
  ImageWidthBC(0xbc80, ExifDir.NONE, "Image Width", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageHeightBC(0xbc81, ExifDir.NONE, "Image Height", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  WidthResolution(0xbc82, ExifDir.NONE, "Width Resolution", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  HeightResolution(0xbc83, ExifDir.NONE, "Height Resolution", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageOffset(0xbcc0, ExifDir.NONE, "Image Offset", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageByteCount(0xbcc1, ExifDir.NONE, "Image Byte Count", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  AlphaOffset(0xbcc2, ExifDir.NONE, "Alpha Offset", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  AlphaByteCount(0xbcc3, ExifDir.NONE, "Alpha Byte Count", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ImageDataDiscard(0xbcc4, ExifDir.NONE, "Image Data Discard", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Full Resolution, 1=Flexbits Discarded, 2=HighPass Frequency Data Discarded, 3=Highpass and LowPass Frequency Data Discarded
  AlphaDataDiscard(0xbcc5, ExifDir.NONE, "Alpha Data Discard", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Full Resolution, 1=Flexbits Discarded, 2=HighPass Frequency Data Discarded, 3=Highpass and LowPass Frequency Data Discarded

  OceScanJobDesc(0xc427, ExifDir.NONE, "Oce Scan Job Description", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  OceApplicationSelector(0xc428, ExifDir.NONE, "Oce Application Selector", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  OceIDNumber(0xc429, ExifDir.NONE, "Oce ID Number", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  OceImageLogic(0xc42a, ExifDir.NONE, "Oce Image Logic", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  Annotations(0xc44f, ExifDir.NONE, "Annotations", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  PrintIM(0xc4a5, ExifDir.NONE, "Print IM", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null), //subTags not decoded
  HasselbladExif(0xc51b, ExifDir.NONE, "Hasselblad Exif", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  OriginalFileName(0xc573, ExifDir.NONE, "Original File Name", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //used by some obscure software
  UPTOOriginalContentType(0xc580, ExifDir.NONE, "UPTO Original Content Type", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //0=Text or Drawing, 1=Grayscale, 2=Color
  CR2CFAPattern(0xc5e0, ExifDir.NONE, "CR 2 CFA Pattern", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //1='0 1 1 2'=[Red,Green][Green,Blue], 2='2 1 1 0'=[Blue,Green][Green,Red], 3='1 2 0 1'=[Green,Blue][Red,Green], 4=' 1 0 2 1'=[Green,Red][Blue,Green]
  //tags 0xc612-0xcd3b are defined by the DNG (Digital Negative)specification unless otherwise noted. See https://helpx.adobe.com/photoshop/digital-negative.html for the specification
  DNGVersion(0xc612, ExifDir.NONE, "DNG Version", ExifTagDataType.BYTE, 4, ExifTagGroup.IFD0, null),
  DNGBackwardVersion(0xc613, ExifDir.NONE, "DNG Backward Version", ExifTagDataType.BYTE, 4, ExifTagGroup.IFD0, null),
  UniqueCameraModel(0xc614, ExifDir.NONE, "Unique Camera Model", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  LocalizedCameraModel(0xc615, ExifDir.NONE, "Localized Camera Model", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  CFAPlaneColor(0xc616, ExifDir.NONE, "CFA Plane Color", ExifTagDataType.NO, 0, ExifTagGroup.SUB_IFD, null),
  CFALayout(0xc617, ExifDir.NONE, "CFA Layout", ExifTagDataType.NO, 0, ExifTagGroup.SUB_IFD, null),  //1=Rectangular, 2=Even columns offset down 1/2 row, 3=Even columns offset up 1/2 row, 4=Even rows offset right 1/2 column, 5=Even rows offset left 1/2 column,
  // 6=Even rows offset up by 1/2 row, even columns offset left by 1/2 column, 7=Even rows offset up by 1/2 row, even columns offset right by 1/2 column, 8=Even rows offset down by 1/2 row, even columns offset left by 1/2 column, 9=Even rows offset down by 1/2 row, even columns offset right by 1/2 column
  LinearizationTable(0xc618, ExifDir.NONE, "Linearization Table", ExifTagDataType.SHORT, 1, ExifTagGroup.SUB_IFD, null),
  BlackLevelRepeatDim(0xc619, ExifDir.NONE, "Black Level Repeat Dim", ExifTagDataType.SHORT, 1, ExifTagGroup.SUB_IFD, null),
  BlackLevel(0xc61a, ExifDir.NONE, "Black Level", ExifTagDataType.RATIONAL, 1, ExifTagGroup.SUB_IFD, null),
  BlackLevelDeltaH(0xc61b, ExifDir.NONE, "Black Level Delta H", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.SUB_IFD, null),
  BlackLevelDeltaV(0xc61c, ExifDir.NONE, "Black Level Delta V", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.SUB_IFD, null),
  WhiteLevel(0xc61d, ExifDir.NONE, "White Level", ExifTagDataType.LONG, 1, ExifTagGroup.SUB_IFD, null),
  DefaultScale(0xc61e, ExifDir.NONE, "Default Scale", ExifTagDataType.RATIONAL, 2, ExifTagGroup.SUB_IFD, null),
  DefaultCropOrigin(0xc61f, ExifDir.NONE, "Default Crop Origin", ExifTagDataType.LONG, 2, ExifTagGroup.SUB_IFD, null),
  DefaultCropSize(0xc620, ExifDir.NONE, "Default Crop Size", ExifTagDataType.LONG, 2, ExifTagGroup.SUB_IFD, null),
  ColorMatrix1(0xc621, ExifDir.NONE, "Color Matrix 1", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  ColorMatrix2(0xc622, ExifDir.NONE, "Color Matrix 2", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  CameraCalibration1(0xc623, ExifDir.NONE, "Camera Calibration 1", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  CameraCalibration2(0xc624, ExifDir.NONE, "Camera Calibration 2", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  ReductionMatrix1(0xc625, ExifDir.NONE, "Reduction Matrix 1", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  ReductionMatrix2(0xc626, ExifDir.NONE, "Reduction Matrix 2", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  AnalogBalance(0xc627, ExifDir.NONE, "Analog Balance", ExifTagDataType.RATIONAL, 1, ExifTagGroup.IFD0, null),
  AsShotNeutral(0xc628, ExifDir.NONE, "As Shot Neutral", ExifTagDataType.RATIONAL, 1, ExifTagGroup.IFD0, null),
  AsShotWhiteXY(0xc629, ExifDir.NONE, "As Shot White XY", ExifTagDataType.RATIONAL, 2, ExifTagGroup.IFD0, null),
  BaselineExposure(0xc62a, ExifDir.NONE, "Baseline Exposure", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.IFD0, null),
  BaselineNoise(0xc62b, ExifDir.NONE, "Baseline Noise", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  BaselineSharpness(0xc62c, ExifDir.NONE, "Baseline Sharpness", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  BayerGreenSplit(0xc62d, ExifDir.NONE, "Bayer Green Split", ExifTagDataType.LONG, 0, ExifTagGroup.SUB_IFD, null),
  LinearResponseLimit(0xc62e, ExifDir.NONE, "Linear Response Limit", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  CameraSerialNumber(0xc62f, ExifDir.NONE, "Camera Serial Number", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  DNGLensInfo(0xc630, ExifDir.NONE, "DNG Lens Info", ExifTagDataType.RATIONAL, 4, ExifTagGroup.IFD0, null),
  ChromaBlurRadius(0xc631, ExifDir.NONE, "Chroma Blur Radius", ExifTagDataType.RATIONAL, 0, ExifTagGroup.SUB_IFD, null),
  AntiAliasStrength(0xc632, ExifDir.NONE, "Anti Alias Strength", ExifTagDataType.RATIONAL, 0, ExifTagGroup.SUB_IFD, null),
  ShadowScale(0xc633, ExifDir.NONE, "Shadow Scale", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  DNGPrivateData(0xc634, ExifDir.NONE, "DNG Private Data", ExifTagDataType.BYTE, 0, ExifTagGroup.IFD0, null), //or Subtags SR2Private, DNGAdobeData (undef), MakerNotePentax, MakerNotePetax5, MakerNoteRicohPentax...all IFD0
  MakerNoteSafety(0xc635, ExifDir.NONE, "Maker Note Safety", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //0=Unsafe, 1=Safe
  RawImageSegmentation(0xc640, ExifDir.NONE, "RAW Image Segmentation", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //used in segmented Canon CR2 images. 3 numbers: 1. Number of segments minus one; 2. Pixel width of segments except last; 3. Pixel width of last segment
  CalibrationIlluminant1(0xc65a, ExifDir.NONE, "Calibration Illuminant 1", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),  //EXIF LightSource Values
  CalibrationIlluminant2(0xc65b, ExifDir.NONE, "Calibration Illuminant 2", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),  //EXIF LightSource Values
  BestQualityScale(0xc65c, ExifDir.NONE, "Best Quality Scale", ExifTagDataType.RATIONAL, 0, ExifTagGroup.SUB_IFD, null),
  RawDataUniqueID(0xc65d, ExifDir.NONE, "RAW Data Unique ID", ExifTagDataType.BYTE, 16, ExifTagGroup.IFD0, null),
  AliasLayerMetadata(0xc660, ExifDir.NONE, "Alias Layer Metadata", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),  //Used by Alias Sketchbook Pro
  OriginalRAWFileName(0xc68b, ExifDir.NONE, "Original RAW File Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  OriginalRAWFileData(0xc68c, ExifDir.NONE, "Original RAW File Data", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null), //DNG Original Raw Tags
  ActiveArea(0xc68d, ExifDir.NONE, "Active Area", ExifTagDataType.LONG, 4, ExifTagGroup.SUB_IFD, null),
  MaskedAreas(0xc68e, ExifDir.NONE, "Masked Areas", ExifTagDataType.LONG, 1, ExifTagGroup.SUB_IFD, null),
  AsShotICCProfile(0xc68f, ExifDir.NONE, "As Shot ICC Profile", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null), //see ICC Profile Tags
  AsShotPreProfileMatrix(0xc690, ExifDir.NONE, "As Shot Pre Profile Matrix", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  CurrentICCProfile(0xc691, ExifDir.NONE, "Current ICC Profile", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null), //see ICC Profile Tags
  CurrentPreProfileMatrix(0xc692, ExifDir.NONE, "Current Pre Profile Matrix", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  ColorimetricReference(0xc6bf, ExifDir.NONE, "Colorimetric Reference", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  SRawType(0xc6c5, ExifDir.NONE, "S RAW Type", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null),
  PanasonicTitle(0xc6d2, ExifDir.NONE, "Panasonic Title", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null), //proprietary Panasonic tag used for baby/pet name etc
  PanasonicTitle2(0xc6d3, ExifDir.NONE, "Panasonic Title 2", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null), //proprietary Panasonic tag used for baby/pet name with age
  CameraCalibrationSig(0xc6f3, ExifDir.NONE, "Camera Calibration Sig", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ProfileCalibrationSig(0xc6f4, ExifDir.NONE, "Profile Calibration Sig", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ProfileIFD(0xc6f5, ExifDir.NONE, "Profile IFD", ExifTagDataType.NO, 0, ExifTagGroup.IFD0, null), //see Exif Tags
  AsShotProfileName(0xc6f6, ExifDir.NONE, "As Shot Profile Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  NoiseReductionApplied(0xc6f7, ExifDir.NONE, "Noise Reduction Applied", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.IFD0, null),
  ProfileName(0xc6f8, ExifDir.NONE, "Profile Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ProfileHueSatMapsDims(0xc6f9, ExifDir.NONE, "Profile Hue Sat Maps Dims", ExifTagDataType.LONG, 3, ExifTagGroup.IFD0, null),
  ProfileHueSatMapData1(0xc6fa, ExifDir.NONE, "Profile Hue Sat Map Data 1", ExifTagDataType.FLOAT, 1, ExifTagGroup.IFD0, null),
  ProfileHueSatMapData2(0xc6fb, ExifDir.NONE, "Profile Hue Sat Map Data 2", ExifTagDataType.FLOAT, 1, ExifTagGroup.IFD0, null),
  ProfileToneCurve(0xc6fc, ExifDir.NONE, "Profile Tone Curve", ExifTagDataType.FLOAT, 1, ExifTagGroup.IFD0, null),
  ProfileEmbedPolicy(0xc6fd, ExifDir.NONE, "Profile Embed Policy", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null),//0=Allow Copying, 1=Embed if Used, 2=Never Embed, 3=No Restrictions
  ProfileCopyright(0xc6fe, ExifDir.NONE, "Profile Copyright", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ForwardMatrix1(0xc714, ExifDir.NONE, "Forward Matrix 1", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  ForwardMatrix2(0xc715, ExifDir.NONE, "Forward Matrix 2", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  PreviewApplicationName(0xc716, ExifDir.NONE, "Preview Application Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  PreviewApplicationVersion(0xc717, ExifDir.NONE, "Preview Application Version", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  PreviewSettingsName(0xc718, ExifDir.NONE, "Preview Settings Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  PreviewSettingsDigest(0xc719, ExifDir.NONE, "Preview Settings Digest", ExifTagDataType.BYTE, 0, ExifTagGroup.IFD0, null),
  PreviewColorSpace(0xc71a, ExifDir.NONE, "Preview Color Space", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //0=Unknown, 1=Gray Gamma 2.2, 2=sRGB, 3=Adobe RGB, 4=ProPhoto RGB
  PreviewDateTime(0xc71b, ExifDir.NONE, "Preview Date Time", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.IFD0, null),
  RawImageDigest(0xc71c, ExifDir.NONE, "RAW Image Digest", ExifTagDataType.BYTE, 16, ExifTagGroup.IFD0, null),
  OriginalRawFileDigest(0xc71d, ExifDir.NONE, "Original RAW File Digest", ExifTagDataType.BYTE, 16, ExifTagGroup.IFD0, null),
  SubTileBlockSize(0xc71e, ExifDir.NONE, "Sub Tile Block Size", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  RowInterleaveFactor(0xc71f, ExifDir.NONE, "Row Interleave Factor", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null),
  ProfileLookTableDims(0xc725, ExifDir.NONE, "Profile Look Table Dims", ExifTagDataType.LONG, 3, ExifTagGroup.IFD0, null),
  ProfileLookTableData(0xc726, ExifDir.NONE, "Profile Look Table Data", ExifTagDataType.FLOAT, 1, ExifTagGroup.IFD0, null),
  //Values for OpcodeList1-3
  //1=WarpRectilinear, 2=WarpFisheye, 3=FixVignetteRadial, 4=FixBadPixelsConstant, 5=FixBadPixelsList, 6=TrimBounds, 7=MapTable, 8=MapPolynomial, 9=GainMap, 10=DeltaPerRow, 11=DeltaPerColumn, 12=ScalePerRow, 13=ScalePerColumn, 14=WarpRectilinear2
  OpcodeList1(0xc740, ExifDir.NONE, "Opcode List 1", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.SUB_IFD, null),
  OpcodeList2(0xc741, ExifDir.NONE, "Opcode List 2", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.SUB_IFD, null),
  OpcodeList3(0xc74e, ExifDir.NONE, "Opcode List 3", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.SUB_IFD, null), //should this be 0xc743??? (=Typo in Exif Tags list?)
  NoiseProfile(0xc761, ExifDir.NONE, "Noise Profile", ExifTagDataType.DOUBLE, 1, ExifTagGroup.SUB_IFD, null),
  TimeCodes(0xc763, ExifDir.NONE, "Time Codes", ExifTagDataType.BYTE, 1, ExifTagGroup.IFD0, null),
  FrameRate(0xc764, ExifDir.NONE, "Frame Rate", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.IFD0, null),
  TStop(0xc772, ExifDir.NONE, "T Stop", ExifTagDataType.RATIONAL, 1, ExifTagGroup.IFD0, null),
  ReelName(0xc789, ExifDir.NONE, "Reel Name", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  OriginalDefaultFinalSize(0xc791, ExifDir.NONE, "Original Default Final Size", ExifTagDataType.LONG, 2, ExifTagGroup.IFD0, null),
  OriginalBestQualitySize(0xc792, ExifDir.NONE, "Original Best Quality Size", ExifTagDataType.LONG, 2, ExifTagGroup.IFD0, null), //called OriginalBestQualityFinalSize by the DNG spec
  OriginalDefaultCropSize(0xc793, ExifDir.NONE, "Original Default Crop Size", ExifTagDataType.RATIONAL, 2, ExifTagGroup.IFD0, null),
  ProfileHueSatMapEncoding(0xc7a3, ExifDir.NONE, "Profile Hue Sat Map Encoding", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //0=Linear, 1=sRGB
  ProfileLookTableEncoding(0xc7a4, ExifDir.NONE, "Profile Look Table Encoding", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //0=Linear, 1=sRGB
  BaselineExposureOffset(0xc7a5, ExifDir.NONE, "Baseline Exposure Offset", ExifTagDataType.SRATIONAL, 0, ExifTagGroup.IFD0, null),
  DefaultBlackRender(0xc7a6, ExifDir.NONE, "Default Black Render", ExifTagDataType.LONG, 0, ExifTagGroup.IFD0, null), //0=Auto, 1=None
  NewRawImageDigest(0xc7a7, ExifDir.NONE, "New RAW Image Digest", ExifTagDataType.BYTE, 16, ExifTagGroup.IFD0, null),
  RawToPreviewGain(0xc7a8, ExifDir.NONE, "RAW to Preview Gain", ExifTagDataType.DOUBLE, 0, ExifTagGroup.IFD0, null),
  CacheVersion(0xc7aa, ExifDir.NONE, "Cache Version", ExifTagDataType.LONG, 0, ExifTagGroup.SUB_IFD2, null),
  DefaultUserCrop(0xc7b5, ExifDir.NONE, "Default User Crop", ExifTagDataType.RATIONAL, 4, ExifTagGroup.SUB_IFD, null),
  NikonNEFInfo(0xc7d5, ExifDir.NONE, "Nikon NEF Info", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //see Nikon NEFInfo Tags
  //tags 0xc7e9-0xc7ee added by DNG 1.5.0.0
  DepthFormat(0xc7e9, ExifDir.NONE, "Depth Format", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //0=Unknown, 1=Linear, 2=Inverse
  DepthNear(0xc7ea, ExifDir.NONE, "Depth Near", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  DepthFar(0xc7eb, ExifDir.NONE, "Depth Far", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  DepthUnits(0xc7ec, ExifDir.NONE, "Depth Units", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //0=Unknown, 1=Meters
  DepthMeasureType(0xc7ed, ExifDir.NONE, "Depth Measure Type", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //0=Unknown, 1=Optical Axis, 2=Optical Ray
  EnhanceParams(0xc7ee, ExifDir.NONE, "Enhance Params", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ProfileGainTableMap(0xcd2d, ExifDir.NONE, "Profile Gain Table Map", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.SUB_IFD, null),
  SemanticName(0xcd2e, ExifDir.NONE, "Semantic Name", ExifTagDataType.NO, 0, ExifTagGroup.SUB_IFD, null),
  SemanticInstanceIFD(0xcd30, ExifDir.NONE, "Semantic Instance IFD", ExifTagDataType.NO, 0, ExifTagGroup.SUB_IFD, null),
  CalibrationIlluminant3(0xcd31, ExifDir.NONE, "Calibration Illuminant 3", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null), //see EXIF LightSource Values
  CameraCalibration3(0xcd32, ExifDir.NONE, "Camera Calibration 3", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  ColorMatrix3(0xcd33, ExifDir.NONE, "Color Matrix 3", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  ForwardMatrix3(0xcd34, ExifDir.NONE, "Forward Matrix 3", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  IlluminantData1(0xcd35, ExifDir.NONE, "Illuminant Data 1", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null),
  IlluminantData2(0xcd36, ExifDir.NONE, "Illuminant Data 2", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null),
  IlluminantData3(0xcd37, ExifDir.NONE, "Illuminant Data 3", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null),
  MaskSubArea(0xcd38, ExifDir.NONE, "Mask Sub Area", ExifTagDataType.NO, 0, ExifTagGroup.SUB_IFD, null),
  ProfileHueSatMapData3(0xcd39, ExifDir.NONE, "Profile Hue Sat Map Data 3", ExifTagDataType.FLOAT, 1, ExifTagGroup.IFD0, null),
  ReductionMatrix3(0xcd3a, ExifDir.NONE, "Reduction Matrix 3", ExifTagDataType.SRATIONAL, 1, ExifTagGroup.IFD0, null),
  RGBTables(0xcd3b, ExifDir.NONE, "RGBTables", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.IFD0, null),

  Padding(0xea1c, ExifDir.NONE, "Padding", ExifTagDataType.UNDEFINED, 0, ExifTagGroup.EXIF_IFD, null),
  OffsetSchema(0xea1d, ExifDir.NONE, "Offset Schema", ExifTagDataType.SLONG, 0, ExifTagGroup.EXIF_IFD, null),  //Microsoft's ill-conceived maker note offset difference

  //tags 0xfde8-0xfdea and 0xfe4c-0xfe58 are generated by Photoshop Camera RAW. Some names are the same as other EXIF tags, but ExifTool will avoid writing these unless they already exist in the file
  OwnerNameFD(0xfde8, ExifDir.NONE, "Owner Name fde8", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  SerialNumberFD(0xfde9, ExifDir.NONE, "Serial Number fde9", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  Lens(0xfdea, ExifDir.NONE, "Lens fdea", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  KDC_IFD(0xfe00, ExifDir.NONE, "Kodak KDC IFD Tags", ExifTagDataType.NO, 0, ExifTagGroup.MISC, null), //used in some Kodak KDC images
  RawFile(0xfe4c, ExifDir.NONE, "Raw File", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  Converter(0xfe4d, ExifDir.NONE, "Converter", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  WhiteBalanceFE(0xfe4e, ExifDir.NONE, "White Balance FE", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  Exposure(0xfe51, ExifDir.NONE, "Exposure", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  Shadows(0xfe52, ExifDir.NONE, "Shadows", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  Brightness(0xfe53, ExifDir.NONE, "Brightness", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  ContrastFE(0xfe54, ExifDir.NONE, "Contrast FE", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  SaturationFE(0xfe55, ExifDir.NONE, "Saturation FE", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  SharpnessFE(0xfe56, ExifDir.NONE, "Sharpness FE", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  Smoothness(0xfe57, ExifDir.NONE, "Smoothness", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null),
  MoireFilter(0xfe58, ExifDir.NONE, "Moire Filter", ExifTagDataType.ASCII, 0, ExifTagGroup.EXIF_IFD, null);

  private final ExifDir exifDir;              //id of the ExifDirectory in which the tag shall be displayed

  private final int id;                       //Tag ID = Exif Entry Key
  private final String name;                  //description to be displayed instead of the tagID
  private final ExifTagDataType dataType;     //the type of the parameter
  private final int arraySize;                //0= not an array, 1=n (variable), x=dataType[x]=array of data Type size x
  private final ExifTagGroup group;           //grouping of ExifTags. If the standard does not define a group then use MISC
  private final Map<?,?> lookupValues;        //possible values allowed for that exif entry or null if all values allowed (if not look-up values)

  ExifTagInfo(int entryID, ExifDir exifDir, String entryName, ExifTagDataType dataType, int arraySize, ExifTagGroup group, Map<?,?> lookupValues) {
    this.id = entryID;
    this.exifDir = exifDir;
    this.name = entryName;
    this.dataType = dataType;
    this.arraySize = arraySize;
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

  public int getArraySize() {
    return arraySize;
  }

  public ExifTagGroup getGroup() {
    return group;
  }

  public Map<?,?> getLookupValues() {
    return lookupValues;
  }
}