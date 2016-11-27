package de.kissphoto.model;

import javafx.scene.control.TreeItem;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br>
 * This tree reflects a structure of clustered MediaFiles
 * e.g. in CopyFileDatesExtDialog
 * the result of searching for same names with different extensions
 * <p/>
 * is a tree of the sort
 * filename1.masterExt
 * filename1.ext1
 * filename1.ext2
 * filename2.masterExt
 * filename2.ext2
 * filename2.ext3
 * filename3.masterExt
 * filename3.ext1
 * filename3.ext2
 * filename3.ext3
 * ...
 *
 * @author: Dr. Ingo Kreuz
 * @date: 2014-06-19
 * @modified:
 */
public class MediaFileTreeItem extends TreeItem<MediaFile> {

  public MediaFileTreeItem(MediaFile mediaFile) {

  }
}
