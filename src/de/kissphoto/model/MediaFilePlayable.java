package de.kissphoto.model;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.media.Media;

import java.nio.file.Path;

/**
 * This is a MediaFile that can be played by JavaFx's mediaPlayer
 *
 * @Author: ikreuz
 * @Date: 2014-06-10
 * @modified: 2019-06-22 mediaCache corrections: getMediaContentException() added
 */
public abstract class MediaFilePlayable extends MediaFileTagged {
  protected MediaFilePlayable(Path file, MediaFileList parent) {
    super(file, parent);
  }

  @Override
  public Object getMediaContent() {
    if (!isMediaContentValid()) {
      try {
        content = new Media(fileOnDisk.toFile().toURI().toString());
      } catch (Exception e) {
        content = null;  //not supported
      }

    }
    return content;
  }

  @Override
  public Exception getMediaContentException() {
    return null;
  }


  @Override
  public long getContentApproxMemSize() {
    return 40000000; //40MB for a player (tried out)
  }

  @Override
  public ReadOnlyDoubleProperty getContentProgressProperty() {
    return null; //no progress available for playable media
  }
}
