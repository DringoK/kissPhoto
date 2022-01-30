package dringo.kissPhoto.model.Metadata.Exif.LookupValues;

/**
 * MIT License
 * <p>
 * Copyright (c)2022 kissPhoto
 * </p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This class is the base class for all lookup values
 * <p/>
 *
 * @author Dringo
 * @version 2022-01-30 First implementation
 * @since 2021-01-30
 */
public abstract class LookupValue {
  //value will is defined in subclasses only (String, or int)
  private final String description;    //description to be displayed instead of the value

  protected LookupValue(String name) {
    this.description = name;
  }

  public String getDescription() {
    return description;
  }

}
