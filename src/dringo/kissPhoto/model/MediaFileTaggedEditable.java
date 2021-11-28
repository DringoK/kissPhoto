package dringo.kissPhoto.model;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoTreeItem;
import dringo.kissPhoto.view.MetaInfoEditableTagsView;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

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
  boolean isSupportedFile; //currently, only JPG-Files are supported for writing tags. Set in constructor and corrected when read via mediautil in getEditableImageInfo

  protected EditableMetaInfoTreeItem editableMetaInfoTreeItem; //cached editableMetaInfo root

  protected MediaFileTaggedEditable(Path file, MediaFileList parent) {
    super(file, parent);
    //lljTran=null; //lazy load: load it when getEditableImageInfo is called
    isSupportedFile = (getExtension().equalsIgnoreCase("jpg")) || (getExtension().equalsIgnoreCase("JPEG"));
  }

  /**
   * read editable tags from fileOnDisk using LLJTran (mediautil)
   * implements lazy load: only read metadata when needed, then keep it in cache
   *
   * @return an Exif structure (derived from AbstractImageInfo) or null if header not readable
   */
  public Exif getEditableImageInfo(){
    if (lljTran == null) //lazy load, load only if not already loaded
      System.out.println(getFileOnDisk() + ": MediaFileTaggedEditable.getEditableImageInfo->Mediautil read");

      AbstractImageInfo<?> info=null;

      try {
        lljTran = new LLJTran(fileOnDisk.toFile());
        lljTran.read(LLJTran.READ_HEADER, true);
        info = lljTran.getImageInfo();
      }catch (LLJTranException e){
        isSupportedFile = false;
      }finally {
        lljTran.closeInternalInputStream();
      }

    if (info instanceof Exif) {
      //set existing Exif
      imageInfo = (Exif) info;
    } else {
      //generate an empty Exif (will only be written if a tag has been changed later by the user
      lljTran.addAppx(LLJTran.dummyExifHeader, 0, LLJTran.dummyExifHeader.length, true);
      imageInfo = (Exif) lljTran.getImageInfo(); // This would have changed
    }

    return imageInfo;
  }

  public boolean isReallyEditable(){
    return isSupportedFile;
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
