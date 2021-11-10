package dringo.kissPhoto.model.Metadata;

import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.model.MediaFileTagged;
import javafx.beans.property.StringPropertyBase;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * <p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p>
 * This class wraps a tag value as a StringPropertyBase so it can be displayed in a StringTable (FileTableView)
 * By calling setMetaDataPath(path) it is defined which tag has to be wrapped.
 * It is used by FileTableView for displaying the metaInfoColumn
 * <p>caching:
 * <li>the first access to get() looks up the value (description) of the tag. A second access and
 * <li>all further accesses return just the same string until setMetaDataPath is called </li>
 *
 * <p/>
 * todo: write access via set() should change the data in metadata, update lastValue, and put the tag to a changelist in MediaFileTagged
 *
 * @author Dringo
 * @version 2021-11-07 First implementation
 * @since 2021-11-07
 */

public class MetaInfoProperty extends StringPropertyBase {
  ObservableStringList tagPath = null;
  MediaFileTagged mediaFileTagged = null;
  String lastValue = null;
  Tag tag = null;

  /**
   * Returns the {@code Object} that contains this property. If this property
   * is not contained in an {@code Object}, {@code null} is returned.
   * --> no bean support therefore always null is returned
   *
   * @return {@code null}
   */
  @Override
  public Object getBean() {
    return null;
  }

  /**
   * Returns the name of this property. If the property does not have a name,
   * this method returns an empty {@code String}.
   * --> no name is supported. Always "" is returned
   *
   * @return the name or an empty {@code String}
   */
  @Override
  public String getName() {
    return "";
  }

  /**
   * define which tag is to be wrapped and of which object (metadata of MediaFileTagged)
   *
   * @param tagPath  the tree path
   * @param mediaFileTagged link to the values
   */
  public void setMetaDataPath(MediaFileTagged mediaFileTagged, ObservableStringList tagPath) {
    this.tagPath = tagPath;
    this.mediaFileTagged = mediaFileTagged;
    lastValue = null;
  }

  /**
   * {@inheritDoc}
   *
   * @returns the value of the metadata tag defined by setMetaDataPath or "" if not found
   */
  @Override
  public String get() {
    super.get();
    if (tagPath == null) return "";
    if (lastValue!=null) {
      return lastValue;  //if unchanged since last setMetaDataPath it can still be used
    }

    tag = null;

    if (tagPath.getSize()>1) { //only if path is long enough to be valid
      for (Directory directory : mediaFileTagged.getMetadata().getDirectories()) {
        if (directory.getName().equalsIgnoreCase(tagPath.get(1))) { //if directory found
          //try to find tag
          for (Tag searchTag : directory.getTags()) {
            if (searchTag.getTagName().equalsIgnoreCase(tagPath.get(0))) {
              this.tag = searchTag;
              break;
            }
          }
          if (tag != null) break;
        }
      }
    }
    if (tag != null) {
      lastValue = tag.getDescription();
    } else
      lastValue = "";
    return lastValue;
  }

  /**
   * {@inheritDoc}
   *
   * @param newValue
   */
  @Override
  public void set(String newValue) {
    super.set(newValue);
  }
}
