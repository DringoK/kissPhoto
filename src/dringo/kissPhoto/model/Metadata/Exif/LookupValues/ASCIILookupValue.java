package dringo.kissPhoto.model.Metadata.Exif.LookupValues;

import java.util.HashMap;
import java.util.Map;

/**
 * MIT License
 * <p>
 * Copyright (c)2022 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class is the base class for all string based lookup values
 * <p/>
 *
 * @author Dringo
 * @version 2022-01-30 First implementation
 * @since 2021-01-30
 */
public class ASCIILookupValue extends LookupValue{
  private final String value;              //String value to be stored
  protected static Map<String, ASCIILookupValue> valueMap = new HashMap<>();

  protected ASCIILookupValue(String value, String description) {
    super(description);
    this.value = value;
  }

  public static ASCIILookupValue fromValue(String value){
    return valueMap.get(value);
  }

  public String getValue() {
    return value;
  }

  public static Map<String, ASCIILookupValue> getValueMap(){return valueMap;}

}
