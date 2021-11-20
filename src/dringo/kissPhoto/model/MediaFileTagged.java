package dringo.kissPhoto.model;


import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import dringo.kissPhoto.helper.ObservableStringList;
import dringo.kissPhoto.model.Metadata.MetaInfoProperty;
import dringo.kissPhoto.model.Metadata.MetaInfoTreeItem;
import dringo.kissPhoto.view.MetaInfoAllTagsView;

import java.nio.file.Path;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is a MediaFile that can have tags
 * I use metadata-extractor to read the tags
 * see: https://github.com/drewnoakes/metadata-extractor/releases
 *
 *
 * @author Dringo
 * @version 2021-11-07 metainfo column support (="" if not MediaFileTagged), reflection for FileTableView eliminated
 * @version 2021-04-07 metaInfoView supported. Cache support completed
 * @version 2017-10-28 updated to latest metadata-extractor version. Now I use source instead of jar because drew noaks does not deliver jar for latest version
 * @since 2014-06-10
 */
public abstract class MediaFileTagged extends MediaFile {
  Metadata metadata;                 //see http://code.google.com/p/metadata-extractor/wiki/GettingStarted for displaying "all current tags"

  protected MetaInfoTreeItem metaInfoTreeItem = null; //cached metaInfo root?
  MetaInfoProperty metaInfoProperty = new MetaInfoProperty();

  protected MediaFileTagged(Path file, MediaFileList parent) {
    super(file, parent);
    //metadata = null; //lazy load: load it when getMetaData is called
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
      } catch (Exception e) {
        //e.printStackTrace();
      }

    return metadata;
  }
  /**
   * cache strategy for metadata TreeTableView: Cache the root of the Tree on first access
   * @param metaInfoAllTagsView link to the viewer that knows how to fill the cache
   * @return the cached element or null if *this* is not a MediaFileTagged
   */
  public MetaInfoTreeItem getMetaInfoCached(MetaInfoAllTagsView metaInfoAllTagsView){
    if (metaInfoTreeItem ==null){ //if invalid and only available for Subclass MediaFileTagged which can have MetaInfos
      //if not in cache then ask the viewer to load it
      mediaCache.maintainCacheSizeByFlushingOldest(); //
      metaInfoTreeItem = metaInfoAllTagsView.getViewerSpecificMediaInfo(this);
    }
    return metaInfoTreeItem;
  }

  private ObservableStringList lastMetaInfoColumnPath = null;
  /**
   * metaInfoColumn.setCellValueFactory calls this method everytime it tries to update cell-content in this column
   * @param metaInfoColumnPath path in the tree view
   * @return null
   */
  @Override
  public MetaInfoProperty getMetaInfo(ObservableStringList metaInfoColumnPath){
    if (lastMetaInfoColumnPath != metaInfoColumnPath) {
      metaInfoProperty.setMetaDataPath(this, metaInfoColumnPath);
    }
    return metaInfoProperty;
  }


  /**
   * Flush the media content to free memory
   * don't forget to clear it in the cache also (or use flushFromCache instead)
   */
  @Override
  public void flushMediaContent() {
    super.flushMediaContent();
    metadata=null;
    metaInfoTreeItem=null;

  }

}
