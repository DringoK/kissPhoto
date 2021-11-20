package dringo.kissPhoto.model.Metadata.LookupValues;

import mediautil.image.jpeg.Exif;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class defines all possible values for tag orientation (0x0112, IFD0)
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-10 First implementation
 * @since 2021-11-10
 */
public class OrientationLookupValue extends IntegerLookupValue {
  static{
    valueMap.put(Exif.ORIENTATION_TOPLEFT, new OrientationLookupValue(Exif.ORIENTATION_TOPLEFT, "Top Left"));       //Horizontal
    valueMap.put(Exif.ORIENTATION_TOPRIGHT, new OrientationLookupValue(Exif.ORIENTATION_TOPRIGHT, "Top Right"));    //Mirror horizontal
    valueMap.put(Exif.ORIENTATION_BOTRIGHT, new OrientationLookupValue(Exif.ORIENTATION_BOTRIGHT, "Bottom Right")); //Rotate 180
    valueMap.put(Exif.ORIENTATION_BOTLEFT, new OrientationLookupValue(Exif.ORIENTATION_BOTLEFT, "Bottom Left"));    //Mirror vertical
    valueMap.put(Exif.ORIENTATION_LEFTTOP, new OrientationLookupValue(Exif.ORIENTATION_LEFTTOP, "Left Top"));       //Mirror horizontal and rot 270 CW
    valueMap.put(Exif.ORIENTATION_RIGHTTOP, new OrientationLookupValue(Exif.ORIENTATION_RIGHTTOP, "Right Top"));    //Rotate 90 CW
    valueMap.put(Exif.ORIENTATION_RIGHTBOT, new OrientationLookupValue(Exif.ORIENTATION_RIGHTBOT, "Right Bottom")); //Mirror Horizontal, Rotate 90 CW
    valueMap.put(Exif.ORIENTATION_LEFTBOT, new OrientationLookupValue(Exif.ORIENTATION_LEFTBOT, "Left Bottom"));    //Rotate 270 CW
  }

  public OrientationLookupValue(int value, String name) {
    super(value, name);
  }
}
