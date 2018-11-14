package de.kissphoto.model;


import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import de.ingo.writableMetadata.WritableEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This is a MediaFile that can have tags
 * I use metadata-extractor to read the tags
 * see: https://github.com/drewnoakes/metadata-extractor/releases
 *
 *
 * @Author: ikreuz
 * @Date: 2014-06-10
 * @modified: 2017-10-28 updated to latest meatadata-extractor version. Now I use source instead of jar because drew noaks does not deliver jar for latest version
 */
public abstract class MediaFileTagged extends MediaFile {
  ObservableList<WritableEntry> exifChanges = FXCollections.observableArrayList();
  Metadata metadata;                 //see http://code.google.com/p/metadata-extractor/wiki/GettingStarted

  protected MediaFileTagged(Path file, MediaFileList parent) {
    super(file, parent);
    metadata = null; //lazy load: load it when getMetaData is called
  }

  /**
   * read tags from fileOnDisk
   * implements lazy load: only read metadata when needed, then keep it
   *
   * @return Metadata structure
   */
  public Metadata getMetadata() {
    if (metadata == null)  //lazy load
      try {
        metadata = ImageMetadataReader.readMetadata(fileOnDisk.toFile());
      } catch (ImageProcessingException e) {
        //e.printStackTrace();
      } catch (IOException e) {
        //e.printStackTrace();
      }


    return metadata;
  }

  /**
   * Iterator for getting the Directories found in Metadata
   * uses lazy load: if Metadata not yet loaded, then getMetadata is called internally
   *
   * @return iterable for the directories found in Metadata
   */
  public Iterable<Directory> getMetadataDirectories() {
    if (metadata == null) getMetadata();
    if (metadata != null) //no error while loading?
      return metadata.getDirectories();
    else
      return null;
  }

  /**
   * Iterator for getting the tags found in a directory
   *
   * @param directory is the directory the tags are in
   * @return iterable for the tags of the directory
   */
  public Iterable<Tag> getMetadataTags(Directory directory) {
    if (directory != null)
      return directory.getTags();
    else
      return null;
  }

  /**
   * sets the orientation in EXIF base directory (without affecting the image data)
   * This method just puts the update to the list of changes
   * All changes can be written to the file using safeExifChanges()
   *
   * @param orientation
   */
  public void setExifOrientation(int orientation) {

  }

  public void saveExifChanges() {

  }

}
