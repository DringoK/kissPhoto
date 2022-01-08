package dringo.kissPhoto.model.Metadata.Exif.LookupValues;

import java.util.HashMap;
import java.util.Map;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class is the base class for all number based lookup values
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-13 First implementation
 * @since 2021-11-13
 */
public class IntegerLookupValue {
  private final int value;                 //Tag ID = Exif Entry Key
  private final String name;               //description to be displayed instead of the tagID

  protected static Map<Integer, IntegerLookupValue> valueMap = new HashMap<>();

  protected IntegerLookupValue(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public static IntegerLookupValue fromID(int entryID){
    return valueMap.get(entryID);
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static Map<Integer, IntegerLookupValue> getValueMap() {
    return valueMap;
  }
}
