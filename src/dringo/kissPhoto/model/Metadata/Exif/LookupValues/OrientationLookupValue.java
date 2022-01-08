package dringo.kissPhoto.model.Metadata.Exif.LookupValues;

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
 * @version 2022-01-08 no longer dependent from mediaUtil
 * @since 2021-11-10
 */
public class OrientationLookupValue extends IntegerLookupValue {
  static{
    valueMap.put(1, new OrientationLookupValue(1, "Top Left"));     //Horizontal
    valueMap.put(2, new OrientationLookupValue(2, "Top Right"));    //Mirror horizontal
    valueMap.put(3, new OrientationLookupValue(3, "Bottom Right")); //Rotate 180
    valueMap.put(4, new OrientationLookupValue(4, "Bottom Left"));  //Mirror vertical
    valueMap.put(5, new OrientationLookupValue(5, "Left Top"));     //Mirror horizontal and rot 270 CW
    valueMap.put(6, new OrientationLookupValue(6, "Right Top"));    //Rotate 90 CW
    valueMap.put(7, new OrientationLookupValue(7, "Right Bottom")); //Mirror Horizontal, Rotate 90 CW
    valueMap.put(8, new OrientationLookupValue(8, "Left Bottom"));  //Rotate 270 CW
  }

  public OrientationLookupValue(int value, String name) {
    super(value, name);
  }
}
