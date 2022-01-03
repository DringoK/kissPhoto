package dringo.kissPhoto.model;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoTreeItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems.EditableTagItem;
import dringo.kissPhoto.view.MetaInfoEditableTagsView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

import java.nio.file.Path;
import java.text.MessageFormat;

import static dringo.kissPhoto.KissPhoto.language;

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
  Exif exifHeader;  //the exif header of the file
  boolean isSupportedFile; //currently, only JPG-Files are supported for writing tags. Set in constructor and corrected when read via mediaUtil in getEditableImageInfo
  ObservableList<EditableMetaInfoItem> changedMetaTags = null; //find all changed meta tags in one list for saving and showing that the file has been changed. lazy generation in addChangedTag()

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
      exifHeader = (Exif) info;
    } else {
      //generate an empty Exif (will only be written if a tag has been changed later by the user
      lljTran.addAppx(LLJTran.dummyExifHeader, 0, LLJTran.dummyExifHeader.length, true);
      exifHeader = (Exif) lljTran.getImageInfo(); // This would have changed
    }

    return exifHeader;
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

  /**
   * changedTags list is controlled by EditableTagItem:
   * whenever it is changed it will report it to MediaFileTaggedEditable.
   * Therefore: just call this method from EditableTagItem to prevent redundant "changed" info becoming inconsistent!
   * @param item the EditableTagItem that has been changed
   */
  public void addToChangedTags(EditableTagItem item){
    if (changedMetaTags == null) changedMetaTags = FXCollections.observableArrayList(); //lazy generation: changes in meta tags are seldom

    //prevent doubles
    if (!changedMetaTags.contains(item)) changedMetaTags.add(item);

  }

  /**
   * changedTags list is controlled by EditableTagItem:
   * whenever it is no longer changed (e.g. because changes are saved or undone by the user) it will report it to MediaFileTaggedEditable.
   * Therefore: just call this method from EditableTagItem to prevent redundant "changed" info becoming inconsistent!
   * @param item the EditableTagItem that is no longer changed
   */
  public void removeFromChangedTags(EditableTagItem item){
    if (changedMetaTags != null)
      changedMetaTags.remove(item);
  }

  /**
   * extend the isChanged function by metaTags changes
   * @return  true if any changes are currently in changedMetaTags list
   */
  @Override
  public boolean isChanged() {
    return super.isChanged() || (changedMetaTags !=null && changedMetaTags.size()>0);
  }

  /**
   * extend the getChangesText function by metaTags changes
    * @return a text describing all changes to the media file (including meta tag changes)
   */
  @Override
  public String getChangesText() {
    String s = super.getChangesText();

    if (changedMetaTags != null){
      System.out.println("MediaFileTaggedEditable.getChangesText. Size="+changedMetaTags.size());

      if (changedMetaTags.size() > 0){
        if (!s.isEmpty()) s = s + ", "; //delimiter if there are other changes

        if (changedMetaTags.size() > 1) {
          s = MessageFormat.format(language.getString("0.1.meta.tags.are.changed"), s, changedMetaTags.size());
        } else {
          //then size==1
          s = MessageFormat.format(language.getString("0.1.meta.tag.is.changed"), s);
        }
      }
    }
    return s;
  }
}
