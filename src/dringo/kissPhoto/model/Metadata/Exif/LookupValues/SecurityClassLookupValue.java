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
public class SecurityClassLookupValue extends ASCIILookupValue {
  static{
    valueMap.put("C", new SecurityClassLookupValue("C", "Confidential"));
    valueMap.put("R", new SecurityClassLookupValue("R", "Restricted"));
    valueMap.put("S", new SecurityClassLookupValue("S", "Secret"));
    valueMap.put("T", new SecurityClassLookupValue("T", "Top Secret"));
    valueMap.put("U", new SecurityClassLookupValue("U", "Unclassified"));
  }

  public SecurityClassLookupValue(String value, String description) {
    super(value, description);
  }
}
