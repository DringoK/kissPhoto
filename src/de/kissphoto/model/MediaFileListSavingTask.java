package de.kissphoto.model;
/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br>
 * Here is the task defined to save the changes. Especially because rotation of images might be slow
 * (the complete file needs to be rewritten) saving might take a long time. This task ensures that the
 * GUI will not freeze.
 * The task communicates with the progress bar in the status bar and respects cancelling
 *
 * @author: Dr. Ingo Kreuz
 * @date: 2018-09-22
 * @modified: 2018-11-17 housekeeping
 */

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

// Returns the number of Errors while saving
public class MediaFileListSavingTask extends Task<Integer> {
  private final ObservableList<MediaFile> deletedFileList;
  private final ObservableList<MediaFile> fileList;
  private final MediaCache mediaCache;
  private final int numberOfChangesToSave;

  public MediaFileListSavingTask(ObservableList<MediaFile> deletedFileList, ObservableList<MediaFile> fileList, int numberOfChangesToSave, MediaCache mediaCache) {
    this.numberOfChangesToSave = numberOfChangesToSave;
    this.fileList = fileList;
    this.deletedFileList = deletedFileList;
    this.mediaCache = mediaCache;
  }

  /**
   * All changes to the media files contained in currently loaded file list (folder)
   * are written to disk: Renaming, TimeStamp-Changes or changes to EXIF-info (todo)
   * <p/>
   * Strategy for renaming:
   * temporarily filename can occur, if filnames are only different in their counter an renumbering was performed
   * Therefore 2 steps are possible
   * <ul>
   * <li>first loop over files: try to rename, if fail apply an intermediate name which is unique
   * <li>second loop over files: try to rename again from the intermediate name
   * </ul>
   * If the second loop also is not successful (e.g. the fail was because of invalid name or write protect) the
   * file remains unchanged and it's status is marked as "rename error" (see MediaFile.performRename())
   *
   * @return 0 if successful or >0 = the number of errors occurred
   */
  @Override
  protected Integer call() throws Exception {
    int errorCount = 0;
    int secondTryNecessary = 0;
    int step = 0;

    try {
      //first delete all files from disk which are in deletedFileList
      //(do it first to avoid renaming problems, if another file has got the name of a deleted file in between)
      ObservableList<MediaFile> deletedListCopy = FXCollections.observableArrayList(deletedFileList); //copy list for iteration
      for (MediaFile mediaFile : deletedListCopy) {
        step++;
        updateProgress(step, numberOfChangesToSave);

        mediaCache.flush(mediaFile);

        try {
          //perform deletion on disk
          if (mediaFile.performDeleteFile())
            //if successful
            deletedFileList.remove(mediaFile);    //delete from list with files to delete (the undelete list ;-)
            //it already has been immediately deleted from the view when moving to deletion list, so not necessary to delete from fileList
          else
            errorCount++;

        } catch (Exception e) {
          errorCount++;
        }

      }

      //first loop for renaming and the only loop for all other changes
      if (!isCancelled()) {
        for (MediaFile mediaFile : fileList) {
          if (mediaFile.isChanged()) {
            step++;
            updateProgress(step, numberOfChangesToSave);
            try {
              //give the GUI-Thread a chance to update the progressBar and Cancel-Button
              Thread.sleep(10);
            } catch (InterruptedException interrupted) {
              if (isCancelled()) {
                break;
              }
            }

            MediaFile.SaveResult saveResult = mediaFile.saveChanges();
            if (saveResult == MediaFile.SaveResult.NEEDS_2ND_TRY) secondTryNecessary++;
            if (saveResult == MediaFile.SaveResult.ERROR) errorCount++;
          }
        }
      }

      //if secondTryNecessary>0 after first loop: in worst case all files (but one) have intermediate filenames now
      if (!isCancelled()) {
        if (secondTryNecessary > 0) {
          step = 0;
          for (MediaFile mediaFile : fileList) {
            if (mediaFile.isChanged()) {
              step++;
              updateProgress(step, numberOfChangesToSave);
              try {
                //give the GUI-Thread a chance to update the progressBar and Cancel-Button
                Thread.sleep(10);
              } catch (InterruptedException interrupted) {
                if (isCancelled()) {
                  break;
                }
              }

              MediaFile.SaveResult saveResult = mediaFile.saveChanges();
              if (saveResult == MediaFile.SaveResult.NEEDS_2ND_TRY)
                errorCount++; //now it is an error: will only appear if user entered conflicting filenames
              if (saveResult == MediaFile.SaveResult.ERROR) errorCount++;
            }
          }
        }
      }

    } catch (Exception e) {
      errorCount++; //especially when security exceptions occur  (just count them as one error) 
    }
    return errorCount;
  }

}