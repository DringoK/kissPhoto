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
  XPosition(0x011e, ExifDir.IMAGE_INFO, "X Position", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  YPosition(0x011f, ExifDir.IMAGE_INFO, "Y Position", ExifTagDataType.RATIONAL, 0, ExifTagGroup.IFD0, null),
  ResolutionUnit(0x0128, ExifDir.IMAGE_INFO, "Resolution Unit", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, ResolutionUnitLookupValue.getValueMap()),
  PageNumber(0x0129, ExifDir.IMAGE_INFO, "Page Number", ExifTagDataType.SHORT, 2, ExifTagGroup.IFD0, null),
  Software(0x0131, ExifDir.OTHER, "Software", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ModifyDate(0x0132, ExifDir.DATE_TIME,  "Modify Date", ExifTagDataType.DATE_TIME, 0, ExifTagGroup.IFD0, null),
  Artist(0x013b, ExifDir.COPYRIGHT, "Artist", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  HostComputer(0x013c, ExifDir.OTHER, "Host Computer", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  Rating(0x4746, ExifDir.IMAGE_DESCRIPTION, "Rating", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  RatingPercent(0x4749, ExifDir.IMAGE_DESCRIPTION, "Rating Percent", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null),
  Copyright(0x8298, ExifDir.COPYRIGHT, "Copyright", ExifTagDataType.ASCII, 0, ExifTagGroup.IFD0, null),
  ExposureTime(0x829a, ExifDir.IMAGE_INFO, "Exposure Time", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null),
  FNumber(0x829d, ExifDir.IMAGE_INFO, "F-Number", ExifTagDataType.RATIONAL, 0, ExifTagGroup.EXIF_IFD, null), //Blendenzahl


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

  //continue at 0x84e8 page 8


  DUMMYLASTLine(0x0, ExifDir.NONE, "Eintrag löschen und darüber ein Strichpunkt setzen", ExifTagDataType.SHORT, 0, ExifTagGroup.IFD0, null);




  private final ExifDir exifDir;              //id of the ExifDirectory in which the tag shall be displayed

  private final int id;                       //Tag ID = Exif Entry Key
  private final String name;                  //description to be displayed instead of the tagID
  private final ExifTagDataType dataType;     //the type of the parameter
  private final int arraySize;                //0= not an array, 1=n (variable), x=dataType[x]=array of data Type size x
  private final ExifTagGroup group;           //grouping of ExifTags. If the standard does not define a group then use MISC
  private final Map<Integer, ?> lookupValues; //possible values allowed for that exif entry or null if all values allowed (if not look-up values)

  ExifTagInfo(int entryID, ExifDir exifDir, String entryName, ExifTagDataType dataType, int arraySize, ExifTagGroup group, Map<Integer, ?> lookupValues) {
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

  public Map<Integer, ?> getLookupValues() {
    return lookupValues;
  }
}