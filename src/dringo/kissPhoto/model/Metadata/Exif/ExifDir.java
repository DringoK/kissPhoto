package dringo.kissPhoto.model.Metadata.Exif;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This enum defines all visible branches in the editableTagsView
 * They are different to the ExifTagGroups, because (e.g.) dates should be editable in one branch only
 * <p/>
 *
 * @author Dringo
 * @version 2021-11-28
 * @since 2021-11-28
 */
public enum ExifDir {
  NONE(0, "not.supported"), //If tags are marked with NONE, they will not be shown and are therefore not editable

  //from here on visible branches will be generated in EditableRootItem
  IMAGE_DESCRIPTION(1, language.getString("image.description")),
  COPYRIGHT(2, language.getString("copyright")),
  DATE_TIME(3, language.getString("date.time")),
  OTHER(4, language.getString("other"));

  private final int value;
  private final String name;

  ExifDir(int i, String name) {
    value = i;
    this.name = name;
  }

  public int getValue() {
    return value;
  }
  public String getName(){return name;}
}
