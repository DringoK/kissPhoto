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
public class ResolutionUnitLookupValue extends IntegerLookupValue {
  static{
    valueMap.put(1, new ResolutionUnitLookupValue(1, "None"));     //not standard Exif
    valueMap.put(2, new ResolutionUnitLookupValue(2, "Inch"));
    valueMap.put(3, new ResolutionUnitLookupValue(3, "cm"));
  }

  public ResolutionUnitLookupValue(int value, String name) {
    super(value, name);
  }
}
