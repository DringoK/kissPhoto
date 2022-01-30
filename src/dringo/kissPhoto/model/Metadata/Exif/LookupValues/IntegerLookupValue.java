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
 * This class is the base class for all number based lookup values
 * <p/>
 *
 * @author Dringo
 * @version 2022-01-30 improved variable names, common super class LookupValue introduced
 * @since 2021-11-13
 */
public class IntegerLookupValue extends LookupValue {
  private final int value;            //int value to be stored
  protected static Map<Integer, IntegerLookupValue> valueMap = new HashMap<>();

  protected IntegerLookupValue(int value, String description) {
    super(description);
    this.value = value;
  }

  public static IntegerLookupValue fromValue(int value){
    return valueMap.get(value);
  }

  public int getValue() {
    return value;
  }

  public static Map<Integer, IntegerLookupValue> getValueMap(){return valueMap;}
}
