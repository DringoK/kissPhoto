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
 * This is a MediaFile that can have tags that can be edited or added
 * I use mediautil to read the tags
 *
 * @author Dringo
 * @version 2020-11-13 initial version
 * @since 2020-11-13
 */

public abstract class MediaFileTaggedEditable extends MediaFileTagged {
  //other = reserved
  LLJTran lljTran; //the file reader from mediautil
  Exif exifHeader;  //the exif header of the file
  boolean isSupportedFile; //currently, only JPG-Files are supported for writing tags. Set in constructor and corrected when read via mediaUtil in getEditableImageInfo
  ObservableList<EditableMetaInfoItem> changedMetaTags = null; //find all changed meta tags in one list for saving and showing that the file has been changed. lazy generation in addChangedTag()

  protected static MediaUtilRotator mediaUtilRotator = new MediaUtilRotator();

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
  public Exif getEditableImageInfo() {
    if (lljTran == null) //lazy load, load only if not already loaded
      System.out.println(getFileOnDisk() + ": MediaFileTaggedEditable.getEditableImageInfo->Mediautil read");

    AbstractImageInfo<?> info = null;

    try {
      lljTran = new LLJTran(fileOnDisk.toFile());
      lljTran.read(LLJTran.READ_HEADER, true);
      info = lljTran.getImageInfo();
    } catch (LLJTranException e) {
      isSupportedFile = false;
    } finally {
      lljTran.closeInternalInputStream();
    }

    if (info instanceof Exif) {
      //set existing Exif
      exifHeader = (Exif) info;
    } else {
      exifHeader = null;
    }

    return exifHeader;
  }

  public boolean isReallyEditable() {
    return isSupportedFile;
  }

  /**
   * cache strategy for metadata TreeTableView: Cache the root of the Tree on first access
   *
   * @param metaInfoEditableTagsView link to the viewer that knows how to fill the cache
   * @return the cached element or null if *this* is not a MediaFileTagged
   */
  public EditableMetaInfoTreeItem getMetaInfoCached(MetaInfoEditableTagsView metaInfoEditableTagsView) {
    if (lljTran == null) { //if invalid and only available for Subclass EditableMediaFileTagged which can have editable MetaInfos
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
   * extend the isChanged function by metaTags changes
   *
   * @return true if any changes are currently in changedMetaTags list
   */
  @Override
  public boolean isChanged() {
    return super.isChanged() || isMetaDataChanged();
  }

  /**
   * return if any metaTags have been changed
   */
  private boolean isMetaDataChanged() {
    return changedMetaTags != null && changedMetaTags.size() > 0;
  }

  /**
   * extend the getChangesText function by metaTags changes
   *
   * @return a text describing all changes to the media file (including meta tag changes)
   */
  @Override
  public String getChangesText() {
    String s = super.getChangesText();

    if (changedMetaTags != null) {
      System.out.println("MediaFileTaggedEditable.getChangesText. Size=" + changedMetaTags.size());

      if (changedMetaTags.size() > 0) {
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

  @Override
  public MediaFileRotator getMediaFileRotator() {
    return mediaUtilRotator;
  }


  /**
   * the default operation is empty
   * so this method needs to be overwritten when a specific MediaFileRotater is provided
   * if canRotate==false then the transformation will not be saved later and a warning should be shown on GUI
   *
   * @return successful
   */
  private boolean performTransformation() {
    //reset planned operations
    boolean successful = true;

    if (canRotate()) {
      successful = getMediaFileRotator().transform(this, rotateOperation, flipHorizontally, flipVertically);

      //reset planned operations
      rotateOperation = MediaUtilRotator.RotateOperation.ROTATE0;
      flipHorizontally = false;
      flipVertically = false;

      updateStatusProperty();
      flushFromCache(); //the file needs to be read again
    }

    return successful;
  }

  /**
   * the changes made in the metaTags are applied to the disk
   * <p/>
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

    if (isTransformed() || isMetaDataChanged()) {

      if (lljTran == null) { //lazy laod. it should have been read in all cases in my opinion
        System.out.println("MediaFileTaggedEditable.saveChanges: lljTran was null and has been lazy loaded here");
        getEditableImageInfo();
      }

      //we need at least a dummy exif header
      if (exifHeader == null){
        //generate an empty Exif (will only be written if a tag has been changed later by the user
        lljTran.addAppx(LLJTran.dummyExifHeader, 0, LLJTran.dummyExifHeader.length, true);
        exifHeader = (Exif) lljTran.getImageInfo();
      }

        //transform using MediaUtilRotator
      if (isTransformed()) {
        successful = performTransformation();

        //above transformation has changed timestamp in filesystem
        setTimeStampChanged(true); //to reset to the previous timestamp if it was not already timeStampChanged
      }

      //save all changed metaTags
      for (EditableMetaInfoItem tag:changedMetaTags){
        if (tag instanceof EditableTagItem)
          ((EditableTagItem)tag).saveToExifHeader(exifHeader); //only tags can be edited (not directories not the root)
      }

      //save the file = write new header and transformations
      try {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(getFileOnDisk().toFile()));
        lljTran.save(out, LLJTran.OPT_WRITE_ALL);
        out.close();
      } catch (Exception e) {
        e.printStackTrace();
        successful = false;
      } finally {
        // Cleanup
        //lljTran.freeMemory(); //muss eigentlich nicht gemacht werden: das sollte der Garbage Collector auch so hinkriegen
      }
    }

    SaveResult result = super.saveChanges();

    if (!successful && result == SaveResult.SUCCESSFUL)
      result = SaveResult.ERROR; // SaveResult.NEEDS_2ND_TRY (second trial) has priority to keep consistence
    return result;
  }

  /**
   * MediaUtilRotator can use the open file to perform the transformations
   *
   * @return llJTran object is open (during saveChanges only) or null, if lljTran is not open
   */
  public LLJTran getLljTran() {
    return lljTran;
  }
}

// Old test code remove before release

        /*
        //System.out.println(llj.getComment());
        //llj.setComment("kissPhoto rotation");

        //Modify Exif-Entries
        AbstractImageInfo<?> imageInfo = llj.getImageInfo();

        //if exif directory is existing
        if (imageInfo instanceof Exif exifInfo) { //includes test on !=null.  note: this inlines Exif exifInfo = (Exif) imageInfo;

          // Change Date/Time entries in Exif
          Entry entry;

          entry= exifInfo.getTagValue(Exif.DATETIME, true);//true= use mainIFD (false would be subIFD of thumbnail)
          if(entry != null) {
            System.out.println("changeValue DateTime");
            entry.setValue(0, "1990:05:11 00:01:02");   //index is ignored in connection with String values
          }else {
            System.out.println("setTagValue DateTime");
            exifInfo.setTagValue(Exif.DATETIME, 0, new Entry("1990:05:11 11:22:33"), true);
          }

          entry= exifInfo.getTagValue(Exif.ARTIST, true);//true= use mainIFD (false would be subIFD of thumbnail)
          if(entry != null) {
            System.out.println("changeValue Artist");
            entry.setValue(0, "Künstler=Ingo");   //index is ignored in connection with String values
          }else {
            System.out.println("setTagValue Artist");
            exifInfo.setTagValue(Exif.ARTIST, 0, new Entry("Künstler neu=Ingo"), true);
          }

          entry = exifInfo.getTagValue(Exif.DATETIMEORIGINAL, true);
          if(entry != null)
            entry.setValue(0, "1991:06:12 01:02:03");
          entry = exifInfo.getTagValue(Exif.DATETIMEDIGITIZED, true);
          if(entry != null)
            entry.setValue(0, "1992:07:13 02:03:04");


          //write all changes back to the APPx-buffers
          llj.refreshAppx();
        }
  */
