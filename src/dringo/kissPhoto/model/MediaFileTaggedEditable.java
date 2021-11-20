package dringo.kissPhoto.model;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoTreeItem;
import dringo.kissPhoto.view.MetaInfoEditableTagsView;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;

import java.nio.file.Path;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is a MediaFile that can have tags that can be edited or added
 * I use mediautil to read the tags
 *
 * @author Dringo
 * @version 2020-11-13 initial version
 * @since 2020-11-13
 */

public abstract class MediaFileTaggedEditable extends MediaFileTagged{
  LLJTran lljTran; //the file
  Exif imageInfo;  //the exif header of the file

  protected EditableMetaInfoTreeItem editableMetaInfoTreeItem; //cached editableMetaInfo root

  protected MediaFileTaggedEditable(Path file, MediaFileList parent) {
    super(file, parent);
    //lljTran=null; //lazy load: load it when getEditableImageInfo is called
  }

  /**
   * read editable tags from fileOnDisk using LLJTran (mediautil)
   * implements lazy load: only read metadata when needed, then keep it
   *
   * @return an Exif structure (derived from AbstractImageInfo) or null if header not readable
   */
  public Exif getEditableImageInfo(){
    if (lljTran == null) //lazy load
      try {
        lljTran = new LLJTran(fileOnDisk.toFile());
        lljTran.read(LLJTran.READ_HEADER, true);
        AbstractImageInfo<?> info = lljTran.getImageInfo();
        if (info instanceof Exif)
          imageInfo = (Exif) info;
      }catch (Exception e){
        e.printStackTrace(); //ignore problems while reading: if not readable null is returned
      }finally {
        lljTran.closeInternalInputStream();
      }
    return imageInfo;
  }

  /**
   * cache strategy for metadata TreeTableView: Cache the root of the Tree on first access
   * @param metaInfoEditableTagsView link to the viewer that knows how to fill the cache
   * @return the cached element or null if *this* is not a MediaFileTagged
   */
  public EditableMetaInfoTreeItem getMetaInfoCached(MetaInfoEditableTagsView metaInfoEditableTagsView){
    if (lljTran ==null){ //if invalid and only available for Subclass EditableMediaFileTagged which can have editable MetaInfos
      //if not in cache then ask the viewer to load it
      mediaCache.maintainCacheSizeByFlushingOldest(); //
      editableMetaInfoTreeItem = metaInfoEditableTagsView.getViewerSpecificMediaInfo(this);
    }
    return editableMetaInfoTreeItem;
  }
}