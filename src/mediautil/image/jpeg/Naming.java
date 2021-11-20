/* MediaUtil LLJTran - $RCSfile: Naming.java,v $
 * Copyright (C) 1999-2005 Dmitriy Rogatkin, Suresh Mahalingam.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *	$Id: Naming.java,v 1.2 2005/08/13 21:55:51 drogatkin Exp $
 *
 * Some ideas and algorithms were borrowed from:
 * Thomas G. Lane, and James R. Weeks
 */
package mediautil.image.jpeg;

public final class Naming {

  // TODO: extend with expected result type and conversion func
  // for better vieweing
  static final Object[][] ExifTagNames = {
    {new Integer(mediautil.image.jpeg.Exif.NEWSUBFILETYPE), "NewSubFileType"},
    {new Integer(mediautil.image.jpeg.Exif.IMAGEWIDTH), "ImageWidth"},
    {new Integer(mediautil.image.jpeg.Exif.IMAGEHEIGHT), "ImageLength"},
    {new Integer(mediautil.image.jpeg.Exif.BITSPERSAMPLE), "BitsPerSample"},
    {new Integer(mediautil.image.jpeg.Exif.COMPRESSION), "Compression"},
    {new Integer(mediautil.image.jpeg.Exif.PHOTOMETRICINTERPRETATION), "PhotometricInterpretation"},
    {new Integer(mediautil.image.jpeg.Exif.FILLORDER), "FillOrder"},
    {new Integer(mediautil.image.jpeg.Exif.DOCUMENTNAME), "DocumentName"},
    {new Integer(mediautil.image.jpeg.Exif.IMAGEDESCRIPTION), "ImageDescription"},
    {new Integer(mediautil.image.jpeg.Exif.MAKE), "Make"},
    {new Integer(mediautil.image.jpeg.Exif.MODEL), "Model"},
    {new Integer(mediautil.image.jpeg.Exif.STRIPOFFSETS), "StripOffsets"},
    {new Integer(mediautil.image.jpeg.Exif.ORIENTATION), "Orientation"},
    {new Integer(mediautil.image.jpeg.Exif.SAMPLESPERPIXEL), "SamplesPerPixel"},
    {new Integer(mediautil.image.jpeg.Exif.ROWSPERSTRIP), "RowsPerStrip"},
    {new Integer(mediautil.image.jpeg.Exif.STRIPBYTECOUNTS), "StripByteCounts"},
    {new Integer(mediautil.image.jpeg.Exif.XRESOLUTION), "XResolution"},
    {new Integer(mediautil.image.jpeg.Exif.YRESOLUTION), "YResolution"},
    {new Integer(mediautil.image.jpeg.Exif.PLANARCONFIGURATION), "PlanarConfiguration"},
    {new Integer(mediautil.image.jpeg.Exif.RESOLUTIONUNIT), "ResolutionUnit"},
    {new Integer(mediautil.image.jpeg.Exif.TRANSFERFUNCTION), "TransferFunction"},
    {new Integer(mediautil.image.jpeg.Exif.SOFTWARE), "Software"},
    {new Integer(mediautil.image.jpeg.Exif.DATETIME), "DateTime"},
    {new Integer(mediautil.image.jpeg.Exif.ARTIST), "Artist"},
    {new Integer(mediautil.image.jpeg.Exif.WHITEPOINT), "WhitePoint"},
    {new Integer(mediautil.image.jpeg.Exif.PRIMARYCHROMATICITIES), "PrimaryChromaticities"},
    {new Integer(mediautil.image.jpeg.Exif.SUBIFDS), "SubIFDs"},
    {new Integer(mediautil.image.jpeg.Exif.JPEGTABLES), "JPEGTables"},
    {new Integer(mediautil.image.jpeg.Exif.TRANSFERRANGE), "TransferRange"},
    {new Integer(mediautil.image.jpeg.Exif.JPEGPROC), "JPEGProc"},
    {new Integer(mediautil.image.jpeg.Exif.JPEGINTERCHANGEFORMAT), "JPEGInterchangeFormat"},
    {new Integer(mediautil.image.jpeg.Exif.JPEGINTERCHANGEFORMATLENGTH), "JPEGInterchangeFormatLength"},
    {new Integer(mediautil.image.jpeg.Exif.YCBCRCOEFFICIENTS), "YCbCrCoefficients"},
    {new Integer(mediautil.image.jpeg.Exif.YCBCRSUBSAMPLING), "YCbCrSubSampling"},
    {new Integer(mediautil.image.jpeg.Exif.YCBCRPOSITIONING), "YCbCrPositioning"},
    {new Integer(mediautil.image.jpeg.Exif.REFERENCEBLACKWHITE), "ReferenceBlackWhite"},
    {new Integer(mediautil.image.jpeg.Exif.CFAREPEATPATTERNDIM), "CFARepeatPatternDim"},
    {new Integer(mediautil.image.jpeg.Exif.CFAPATTERN), "CFAPattern"},
    {new Integer(mediautil.image.jpeg.Exif.BATTERYLEVEL), "BatteryLevel"},
    {new Integer(mediautil.image.jpeg.Exif.COPYRIGHT), "Copyright"},
    {new Integer(mediautil.image.jpeg.Exif.EXPOSURETIME), "ExposureTime"},
    {new Integer(mediautil.image.jpeg.Exif.FNUMBER), "FNumber"},
    {new Integer(mediautil.image.jpeg.Exif.IPTC_NAA), "IPTC/NAA"},
    {new Integer(mediautil.image.jpeg.Exif.EXIFOFFSET), "ExifOffset"},
    {new Integer(mediautil.image.jpeg.Exif.INTERCOLORPROFILE), "InterColorProfile"},
    {new Integer(mediautil.image.jpeg.Exif.EXPOSUREPROGRAM), "ExposureProgram"},
    {new Integer(mediautil.image.jpeg.Exif.SPECTRALSENSITIVITY), "SpectralSensitivity"},
    {new Integer(mediautil.image.jpeg.Exif.GPSINFO), "GPSInfo"},
    {new Integer(mediautil.image.jpeg.Exif.ISOSPEEDRATINGS), "ISOSpeedRatings"},
    {new Integer(mediautil.image.jpeg.Exif.OECF), "OECF"},
    {new Integer(mediautil.image.jpeg.Exif.EXIFVERSION), "ExifVersion"},
    {new Integer(mediautil.image.jpeg.Exif.DATETIMEORIGINAL), "DateTimeOriginal"},
    {new Integer(mediautil.image.jpeg.Exif.DATETIMEDIGITIZED), "DateTimeDigitized"},
    {new Integer(mediautil.image.jpeg.Exif.COMPONENTSCONFIGURATION), "ComponentsConfiguration"},
    {new Integer(mediautil.image.jpeg.Exif.COMPRESSEDBITSPERPIXEL), "CompressedBitsPerPixel"},
    {new Integer(mediautil.image.jpeg.Exif.SHUTTERSPEEDVALUE), "ShutterSpeedValue"},
    {new Integer(mediautil.image.jpeg.Exif.APERTUREVALUE), "ApertureValue"},
    {new Integer(mediautil.image.jpeg.Exif.BRIGHTNESSVALUE), "BrightnessValue"},
    {new Integer(mediautil.image.jpeg.Exif.EXPOSUREBIASVALUE), "ExposureBiasValue"},
    {new Integer(mediautil.image.jpeg.Exif.MAXAPERTUREVALUE), "MaxApertureValue"},
    {new Integer(mediautil.image.jpeg.Exif.SUBJECTDISTANCE), "SubjectDistance"},
    {new Integer(mediautil.image.jpeg.Exif.METERINGMODE), "MeteringMode"},
    {new Integer(mediautil.image.jpeg.Exif.LIGHTSOURCE), "LightSource"},
    {new Integer(mediautil.image.jpeg.Exif.FLASH), "Flash"},
    {new Integer(mediautil.image.jpeg.Exif.FOCALLENGTH), "FocalLength"},
    {new Integer(mediautil.image.jpeg.Exif.MAKERNOTE), "MakerNote"},
    {new Integer(mediautil.image.jpeg.Exif.USERCOMMENT), "UserComment"},
    {new Integer(mediautil.image.jpeg.Exif.SUBSECTIME), "SubSecTime"},
    {new Integer(mediautil.image.jpeg.Exif.SUBSECTIMEORIGINAL), "SubSecTimeOriginal"},
    {new Integer(mediautil.image.jpeg.Exif.SUBSECTIMEDIGITIZED), "SubSecTimeDigitized"},
    {new Integer(mediautil.image.jpeg.Exif.FLASHPIXVERSION), "FlashPixVersion"},
    {new Integer(mediautil.image.jpeg.Exif.COLORSPACE), "ColorSpace"},
    {new Integer(mediautil.image.jpeg.Exif.EXIFIMAGEWIDTH), "ExifImageWidth"},
    {new Integer(mediautil.image.jpeg.Exif.EXIFIMAGELENGTH), "ExifImageLength"},
    {new Integer(mediautil.image.jpeg.Exif.INTEROPERABILITYOFFSET), "InteroperabilityOffset"},
    {new Integer(mediautil.image.jpeg.Exif.FLASHENERGY), "FlashEnergy"},
    {new Integer(mediautil.image.jpeg.Exif.SPATIALFREQUENCYRESPONSE), "SpatialFrequencyResponse"},
    {new Integer(mediautil.image.jpeg.Exif.FOCALPLANEXRESOLUTION), "FocalPlaneXResolution"},
    {new Integer(mediautil.image.jpeg.Exif.FOCALPLANEYRESOLUTION), "FocalPlaneYResolution"},
    {new Integer(mediautil.image.jpeg.Exif.FOCALPLANERESOLUTIONUNIT), "FocalPlaneResolutionUnit"},
    {new Integer(mediautil.image.jpeg.Exif.SUBJECTLOCATION), "SubjectLocation"},
    {new Integer(mediautil.image.jpeg.Exif.EXPOSUREINDEX), "ExposureIndex"},
    {new Integer(mediautil.image.jpeg.Exif.SENSINGMETHOD), "SensingMethod"},
    {new Integer(mediautil.image.jpeg.Exif.FILESOURCE), "FileSource"},
    {new Integer(mediautil.image.jpeg.Exif.SCENETYPE), "SceneType"},
    {new Integer(mediautil.image.jpeg.Exif.FOCALLENGTHIN35MMFILM), "FocalLengthIn35mmFilm"},
    {new Integer(mediautil.image.jpeg.Exif.SHARPNESS), "Sharpness"},
    {new Integer(mediautil.image.jpeg.Exif.CUSTOMRENDERED), "CustomRendered"},
    {new Integer(mediautil.image.jpeg.Exif.SATURATION), "Saturation"},
    {new Integer(mediautil.image.jpeg.Exif.WHITEBALANCE), "WhiteBalance"},
    {new Integer(mediautil.image.jpeg.Exif.DIGITALZOOMRATIO), "DigitalZoomRatio"},
    {new Integer(mediautil.image.jpeg.Exif.CONTRAST), "Contrast"},
    {new Integer(mediautil.image.jpeg.Exif.GAINCONTROL), "GainControl"},
    {new Integer(mediautil.image.jpeg.Exif.EXPOSUREMODE), "ExposureMode"},
    {new Integer(mediautil.image.jpeg.Exif.DIGITALZOOMRATIO), "DigitalZoomRatio"},
    {new Integer(mediautil.image.jpeg.Exif.PRINTMODE), "PrintMode"},
    {new Integer(Exif.SCENECAPTURETYPE), "SceneCaptureType"}
  };

  static final Object[][] CIFFPropsNames = {
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_DESCRIPTION), "Description"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_MODELNAME), "ModelName"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_FIRMWAREVERSION), "FirmwareVersion"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_COMPONENTVESRION), "ComponentVesrion"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_ROMOPERATIONMODE), "ROMOperationMode"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_OWNERNAME), "OwnerName"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_IMAGEFILENAME), "ImageFilename"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_THUMBNAILFILENAME), "ThumbnailFilename"},

    {new Integer(mediautil.image.jpeg.CIFF.K_TC_TARGETIMAGETYPE), "TargetImageType"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SR_RELEASEMETHOD), "ReleaseMethod"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SR_RELEASETIMING), "ReleaseTiming"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_RELEASESETTING), "ReleaseSetting"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_BODYSENSITIVITY), "BodySensitivity"},

    {new Integer(mediautil.image.jpeg.CIFF.K_TC_IMAGEFORMAT), "ImageFormat"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_RECORDID), "RecordId"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SELFTIMERTIME), "SelfTimerTime"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SR_TARGETDISTANCESETTING), "TargetDistanceSetting"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_BODYID), "BodyId"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_CAPTURETIME), "CaptureTime"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_IMAGESPEC), "ImageSpec"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SR_EF), "EF"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_MI_EV), "EV"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SERIALNUMBER), "SerialNumber"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SR_EXPOSURE), "Exposure"},

    {new Integer(mediautil.image.jpeg.CIFF.K_TC_CAMERAOBJECT), "CameraObject"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_SHOOTINGRECORD), "ShootingRecord"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_MEASUREDINFO), "MeasuredInfo"},
    {new Integer(mediautil.image.jpeg.CIFF.K_TC_CAMERASPECIFICATION), "CameraSpecification"}
  };

  public static String[] ExifTagTypes = {"B",  // BYTE
    "A",  // ASCII
    "S",  // SHORT
    "L",  // LONG
    "R",  // RATIONAL
    "SB",  // SBYTE
    "U",  // UNDEFINED
    "SS",  // SSHORT
    "SL",  // SLONG
    "SR",  // SRATIONAL
  };

  public static String[] OrientationNames = {
    "TopLeft",
    "TopRight",
    "BotRight",
    "BotLeft",
    "LeftTop",
    "RightTop",
    "RightBot",
    "LeftBot"
  };

  public static String getCIFFTypeName(int type) {
    switch (type & mediautil.image.jpeg.CIFF.K_DATATYPEMASK) {
      case mediautil.image.jpeg.CIFF.K_DT_BYTE:
        return "Byte";
      case mediautil.image.jpeg.CIFF.K_DT_ASCII:
        return "ASCII";
      case mediautil.image.jpeg.CIFF.K_DT_WORD:
        return "Word";
      case mediautil.image.jpeg.CIFF.K_DT_DWORD:
        return "Double word";
      case mediautil.image.jpeg.CIFF.K_DT_BYTE2:
        return "Byte2";
      case mediautil.image.jpeg.CIFF.K_DT_HEAPTYPEPROPERTY1:
        return "Heap1";
      case CIFF.K_DT_HEAPTYPEPROPERTY2:
        return "Heap2";
    }
    return "Unknown";
  }

  public static String getTagName(Integer tag) {
    String result = (String) tagnames.get(tag);
    return (result != null) ? result : ("0x" + tag.toHexString(tag.intValue()));
  }

  public static String getPropName(Integer tag) {
    String result = (String) propnames.get(tag);
    return (result != null) ? result : ("0x" + tag.toHexString(tag.intValue()));
  }

  public static String getTypeName(int type) {
    return ExifTagTypes[type - 1];
  }

  static java.util.Hashtable tagnames;
  static java.util.Hashtable propnames;

  static {
    tagnames = new java.util.Hashtable(ExifTagNames.length);
    for (int i = 0; i < ExifTagNames.length; i++)
      tagnames.put(ExifTagNames[i][0], ExifTagNames[i][1]);
    propnames = new java.util.Hashtable(CIFFPropsNames.length);
    for (int i = 0; i < CIFFPropsNames.length; i++)
      propnames.put(CIFFPropsNames[i][0], CIFFPropsNames[i][1]);
  }

}
