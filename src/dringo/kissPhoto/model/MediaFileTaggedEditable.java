package dringo.kissPhoto.model;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoTreeItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableRootItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems.EditableTagItem;
import dringo.kissPhoto.view.MetaInfoEditableTagsView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mediautil.image.jpeg.AbstractImageInfo;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.MessageFormat;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 * <p>
 * This is a MediaFile that can have tags that can be edited or added.
 * Also transformations (rotate, flip) can be saved to disk.
 * I use mediautil to read or write the tags
 *
 * @author Dringo
 * @since 2021-11-13
 * @version 2022-01-01 first working version
 */

public abstract class MediaFileTaggedEditable extends MediaFileTagged {
  //other = reserved
  boolean supportedFile; //currently, only JPG-Files are supported for writing tags. Set in constructor and corrected when read via readExifHeader() or saveChanges()
  ObservableList<EditableMetaInfoItem> changedMetaTags = null; //find all changed meta tags in one list for saving and showing that the file has been changed. lazy generation in addChangedTag()

  protected EditableMetaInfoTreeItem rootTreeItem; //cached editableMetaInfo root. As soon as needed for the first time it is loaded by getMetaInfoCached();

  protected MediaFileTaggedEditable(Path file, MediaFileList parent) {
    super(file, parent);
    supportedFile = (getExtension().equalsIgnoreCase(".jpg")) || (getExtension().equalsIgnoreCase(".jpeg"));
  }

  /**
   * cache strategy for metadata TreeTableView: Cache the root of the Tree on first access
   * tagsLoaded will be set to true, as soon as loaded sucessfully
   * if tagsLoaded was already true nothing happens
   *
   * @param metaInfoEditableTagsView link to the viewer that knows how to fill the cache
   * @return the cached element or null if *this* is not a MediaFileTagged
   */
  public EditableMetaInfoTreeItem getMetaInfoCached(MetaInfoEditableTagsView metaInfoEditableTagsView) {
    if (rootTreeItem == null) { //if invalid and only available for Subclass EditableMediaFileTagged which can have editable MetaInfos
      //if not in cache then ask the viewer to load it
      mediaCache.maintainCacheSizeByFlushingOldest(); //
      rootTreeItem = metaInfoEditableTagsView.getViewerSpecificMediaInfo(this);
    }
    return rootTreeItem;
  }

  /**
   * changedTags list is controlled by EditableTagItem:
   * whenever it is changed it will report it to MediaFileTaggedEditable.
   * Therefore: just call this method from EditableTagItem to prevent redundant "changed" info becoming inconsistent!
   *
   * @param item the EditableTagItem that has been changed
   */
  public void addToChangedTags(EditableTagItem item) {
    if (changedMetaTags == null)
      changedMetaTags = FXCollections.observableArrayList(); //lazy generation: changes in meta tags are seldom

    //prevent doubles
    if (!changedMetaTags.contains(item)) changedMetaTags.add(item);

  }

  /**
   * changedTags list is controlled by EditableTagItem:
   * whenever it is no longer changed (e.g. because changes are saved or undone by the user) it will report it to MediaFileTaggedEditable.
   * Therefore: just call this method from EditableTagItem to prevent redundant "changed" info becoming inconsistent!
   *
   * @param item the EditableTagItem that is no longer changed
   */
  public void removeFromChangedTags(EditableTagItem item) {
    if (changedMetaTags != null)
      changedMetaTags.remove(item);
  }

  /**
   * return if any metaTags have been changed
   */
  private boolean isMetaDataChanged() {
    return changedMetaTags != null && changedMetaTags.size() > 0;
  }

  /**
   * extend the isChanged function by metaTags changes
   *
   * @return true if any changes are currently in changedMetaTags list
   */
  @Override
  public boolean isChanged() {
    return super.isChanged() || isMetaDataChanged();
  }

  /**
   * extend the getChangesText function by metaTags changes
   *
   * @return a text describing all changes to the media file (including meta tag changes)
   */
  @Override
  public String getChangesText() {
    String s = super.getChangesText();

    if (changedMetaTags != null && changedMetaTags.size() > 0) {
      if (!s.isEmpty()) s = s + ", "; //delimiter if there are other changes

      if (changedMetaTags.size() > 1) {
        s = MessageFormat.format(language.getString("0.1.meta.tags.are.changed"), s, changedMetaTags.size());
      } else {
        //then size==1
        s = MessageFormat.format(language.getString("0.1.meta.tag.is.changed"), s);
      }
    }
    return s;
  }

  /**
   * if canRotate() (currently only for JPEG-Images)
   * transformations (rotation, flipping) are performed during saveChanges() using mediaUtil
   * if not (e.g. for PNG-Images) nothing happens
   * Note: planned operations are not modified and
   *
   * @param llj the lljTran object that has already performed read(READ_ALL, true)
   * @return successful
   */
  private boolean performTransformation(LLJTran llj) {
    boolean successful = false;

    if (canTransformInFile()) {
      try {
        int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;// |LLJTran.OPT_XFORM_THUMBNAIL;  //correct orientation + Default= OPT_WRITE_ALL|OPT_XFORM_APPX | OPT_XFORM_ADJUST_EDGES (i.e correct the edges if resolution is not multiple of 8x8)
        int op = switch (rotateOperation) {
          case ROTATE90 -> LLJTran.ROT_90;
          case ROTATE180 -> LLJTran.ROT_180;
          case ROTATE270 -> LLJTran.ROT_270;
          default -> 0;
        };
        llj.transform(op, options);

        if (flipHorizontally) {
          llj.transform(LLJTran.FLIP_H, options);
        }
        if (flipVertically) {
          llj.transform(LLJTran.FLIP_V, options);
        }
        successful = true;
      } catch (Exception e) {
        //successful remains false
      }
    }

    return successful;
  }

  /**
   * use mediautil LljTran to read the Exif header
   * if supportedFile=false nothing happens
   * if LLjTran could not open the header supportedFile will be set to false
   *
   * @return the root of the tree structure of Exif directories and Exif tags if successful or nul if not
   */
  public EditableRootItem readExifHeader() {
    if (!supportedFile) return null;

    LLJTran llj = new LLJTran(getFileOnDisk().toFile());
    try {
      llj.read(LLJTran.READ_HEADER, true);
      AbstractImageInfo<?> imageInfo = llj.getImageInfo();
      if (llj != null) llj.freeMemory(); //free everything (except exifHeader)

      if (imageInfo instanceof Exif) { //if Metadata could be loaded and is an Exif header
        return new EditableRootItem(this, (Exif) imageInfo);  //EditableRootItem defines the tree structure
        //note: imageInfo is local variable and therefore does not need to be set to null for gc
      } else {
        return null;
      }
    } catch (LLJTranException e) {
      supportedFile = false;  //if reading is not possible this does not seem to be a supported file. Further reading is
      return null;
    }

  }

  /**
   * the changes made in the metaTags and the transformations (rotate, flip) are applied to the file on disk.
   * <ul>
   * <li>if not is supportedFile nothing happens
   * <li>if not isTransformed() nor isMetaDataChanged() nothing happens
   * </ul>
   * note: the changes of the ancestors will be applied in any case
   * <p>
   * A backup-file is put into the deleted folder before the changes are applied
   *
   * @return <ul>
   * <li>SaveResult.SUCCESSFUL if successful</li>
   * <li>SaveResult.NEEDS_2ND_TRY if an intermediate filename has been given and a second run is necessary</li>
   * <li>SaveResult.ERROR if another error has occurred (e.g. write protect/access denied etc)</li>
   * </ul>
   */
  @Override
  public SaveResult saveChanges() {
    boolean successful = true;
    LLJTran llj;

    if (supportedFile && (isTransformed() || isMetaDataChanged())) {
      llj = new LLJTran(fileOnDisk.toFile());
      try {
        llj.read(LLJTran.READ_ALL, true);
      } catch (LLJTranException e) {
        successful = false;
        supportedFile = false;
      }

      if (successful) { //no error occurred during opening --> llj is valid
        AbstractImageInfo<?> imageInfo = llj.getImageInfo();
        Exif exifHeader = null;

        //try to use the existing header
        if (imageInfo instanceof Exif) {  //includes test of not null
          exifHeader = (Exif) imageInfo;
        } else { //was not included
          //we need at least a dummy exif header
          //generate an empty Exif (will only be written if a tag has been changed later by the user
          llj.addAppx(LLJTran.dummyExifHeader, 0, LLJTran.dummyExifHeader.length, true);
          exifHeader = (Exif) llj.getImageInfo();
        }

        //transform using MediaUtil
        boolean wasTransformed = isTransformed();
        if (wasTransformed) {
          successful = performTransformation(llj);

          //above transformation has changed timestamp in filesystem
          setTimeStampChanged(true); //to reset to the previous timestamp if it was not already timeStampChanged
        }

        //llj.setComment("kissPhoto rotation/meta tags changed");

        //save all changed metaTags
        if (changedMetaTags != null) {
          for (EditableMetaInfoItem tag : changedMetaTags) {
            if (tag instanceof EditableTagItem)
              ((EditableTagItem) tag).saveToExifHeader(exifHeader); //only tags can be edited (not directories not the root)
          }
          //write all changes back to the APPx-buffers
          llj.refreshAppx();
        }

        //save the file = write new header and transformations
        try {
          OutputStream out = new BufferedOutputStream(new FileOutputStream(getFileOnDisk().toFile()));
          llj.save(out, LLJTran.OPT_WRITE_ALL);

          out.close();
        } catch (Exception e) {
          e.printStackTrace();
          successful = false;
          //supportedFile is only changed during reading
        } finally {
          llj.freeMemory();
        }
        //successful = true

        //--successful: reset transformations
        if (wasTransformed) {
          resetTransformations();
        }

        flushFromCache(); //the file needs to be read again

        //--successful: reset all changes in the EditableTagItem
        if (changedMetaTags != null) {
          for (EditableMetaInfoItem tag : changedMetaTags) {
            if (tag instanceof EditableTagItem) //only tags can be edited (not directories not the root)
              ((EditableTagItem) tag).changesHaveBeenWritten();
          }
          changedMetaTags = null; //list is now no longer necessary
        }
        updateStatusProperty();

      } else { //not a supported file = error during opening
        successful = false;
      }
    }  //if supported and changed
    //finally, perform all other changes derived from ancestors
    SaveResult result = super.saveChanges();

    //if not successful then change Successful to Error (leave all other results unchanged)
    if (!successful && result == SaveResult.SUCCESSFUL)
      result = SaveResult.ERROR; // SaveResult.NEEDS_2ND_TRY (second trial) has priority to keep consistence

    return result;
  }

  /**
   * Flush the media content to free memory
   */
  @Override
  public void flushMediaContent() {
    rootTreeItem = null;
    super.flushMediaContent();
  }
}
