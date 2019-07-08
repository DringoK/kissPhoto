package de.kissphoto.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a Cache strategy for speeding up drawing & displaying of pictures/movies:
 * <ul>
 * <li>The type of content and its size can be (very) different for every mediaFile of MediaFileList.
 * <li>The cache buffer therefore tries to keep MIN_FREE_MEM_SIZE bytes free by deleting the oldest media
 * from the cache until MIN-FREE_MEM_SIZE is free again.<br>
 * <li>the cacheBuffer is filled in the order of requests (always stored at the end)
 * <li>the oldest media is removed from cache if it is full
 * </ul>
 *
 * @author Ingo
 * @date 11.09.12
 * @modified: 2014-04-18: Preload can be disabled temporarily e.g. while moving files in fileTable view
 * @modified: 2014-05-04: MediaFiles (links) instead of indices are stored, because indices can be changed by the user
 * @modified: 2014-05-25: MIN_FREE_MEM_SIZE is kept instead of a fixed number of maximum cache elements
 * @modified: 2014-06-07: getContent interface to cache simplified, min Free memory from oldHeap is only used (@see getAvailableMem)
 * @modified: 2019-06-22: corrections to cache algo: make robust against wrong estimations by caring about exceptions
 * @modified: 2019-07-07: improvement of exception handling: subscribing to Error-Property
 */
public class MediaCache {
  private static final long MB = 1 << 20;      //=2^20
  private static final long MIN_FREE_MEM_SIZE = 300 * MB;
  private MediaFileList mediaList;   //link to the original data

  private ObservableList<MediaFile> cacheBuffer;
  private Runtime env = Runtime.getRuntime();

  private boolean enablePreload = true;

  /**
   * constructor to build a cache for accessing the list passed in the parameter
   *
   * @param mediaList link to the mediaList connected with the cache
   */
  public MediaCache(MediaFileList mediaList) {
    this.mediaList = mediaList;
    cacheBuffer = FXCollections.observableArrayList();

  }


  /**
   * Calculate the available memory of oldHeap only.
   * <p/>
   * see: MemoryPoolMBBean and MemoryUsage in Oracle documentation
   * and thank you to Mike Lue for his answer on StackOverflow question 6487802 ;-)
   * <p/>
   * note:
   * Eden Heap and Survivor Heap don't play a role...
   * ...also the value from Runtime does not help:
   * env= getRuntime()env.maxMemory() - env.totalMemory() + env.freeMemory();       //usedMem = totalMem - freeMem, available = maxMem-UsedMem
   *
   * @return the available memory in bytes from OldHeap
   */
  public long getAvailableMem() {
    MemoryUsage oldHeapUsage = null;

    //System.out.println("--------------------------------");
    //System.out.println("Cache Size: " + cacheBuffer.size());
    for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
      if (mpBean.getType() == MemoryType.HEAP && !mpBean.getName().contains("den") && !mpBean.getName().contains("urvivor")) { //not (E)den or (S)urvivor heap is OldHeap
        oldHeapUsage = mpBean.getUsage();
        //System.out.println("oldHeapUsage found: " + mpBean.getName());
      }
    }
    //System.out.println(String.format("available: %.2f MB, OldHeap: %.2f MB", (double) ((env.maxMemory() - env.totalMemory() + env.freeMemory()) / MB),
    //    (double) (oldHeapUsage.getMax() - oldHeapUsage.getUsed()) / MB));

/*
    for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
      if (mpBean.getType() == MemoryType.HEAP) {
        System.out.printf(
            "Name: %s: used= %.2f MB, committed= %.2f MB, max=%.2f MB\n",
            mpBean.getName(), (double) mpBean.getUsage().getUsed() / MB, (double) mpBean.getUsage().getCommitted() / MB, (double) mpBean.getUsage().getMax() / MB
        );
      }
    }
*/
    //return env.maxMemory() - env.totalMemory() + env.freeMemory();       //usedMem = totalMem - freeMem, available = maxMem-UsedMem
    if (oldHeapUsage != null)
      return oldHeapUsage.getMax() - oldHeapUsage.getUsed();
    else
      return 0;
  }

  /**
   * @param mediaFile object to be looked for
   * @return if the object is in the cache-ring buffer
   */
  private boolean isInCache(MediaFile mediaFile) {
    return (cacheBuffer.indexOf(mediaFile) >= 0);
  }

  /**
   * If the cache is full
   * i.e. less memory available then MIN_FREE_MEM_SIZE
   * then the oldest entries are deleted (and content flushed)
   */
  private void maintainCacheSizeByFlushingOldest() {
    //flushing cached content does not have direct effect on getAvailableMem (until gc() has run)
    //therefore we add a "guessed" value for every MediaFile that has been flushed to the getAvailableMem()
    //to forecast the effect of flushing and to determine how many mediaFiles need to be flushed until MIN_FREE_MEM_SIZE
    //is respected again
    long approxMemFreed = 0;

    //memory full?
    //remove the oldest MediaFiles from cache until MIN_FREE_MEM_SIZE is reached again
    while (getAvailableMem() + approxMemFreed <= MIN_FREE_MEM_SIZE && cacheBuffer.size() > 0) {//size = 0 means: to less memory for caching at all
      approxMemFreed = approxMemFreed + cacheBuffer.get(0).flushMediaContent(); //begin of the list contains the oldest element
      cacheBuffer.remove(0);
      //System.out.println("flushing --> cache Buffer Size = " + cacheBuffer.size() + " approxMemFreed=" + approxMemFreed + " availMem="+getAvailableMem());
    }
    env.gc();
    //System.out.println("available mem after gc=" + getAvailableMem());
  }


  /**
   * get the MediaContent out of the Cache if possible. If not fill the Cache from disk by accessing MediaFileList
   * (passed in the constructor).
   *
   * @param mediaFile the mediaFile's content will be returned, the mediaFile will be maintained in Cache
   * @return the according MediaFile.Content which is thereby maintained in the cache
   */
  public Object getCachedMediaContent(MediaFile mediaFile) {
    int index = mediaList.getFileList().indexOf(mediaFile);
    if (index >= 0)
      return getCachedMediaContent(index, mediaFile);
    else
      return null;
  }

  /**
   * get the MediaContent out of the Cache if possible. If not fill the Cache from disk by accessing MediaFileList
   * (passed in the constructor).
   *
   * @param mediaFile the mediaFile's content will be returned, the mediaFile will be maintained in Cache
   * @param index     of the Media in MediaFileList (as shown in FileTableView) (to get next/previous media)
   * @return the according MediaFile.Content which is thereby maintained in the cache
   */
  private Object getCachedMediaContent(int index, MediaFile mediaFile) {
    maintainCacheSizeByFlushingOldest();

    Object content = mediaFile.getMediaContent(); //get cached content or load if necessary
    //store the new element in the Cache
    if (!isInCache(mediaFile)) cacheBuffer.add(mediaFile); //add to the end of the list (as the "newest")


    if (enablePreload) {
      //preload previous media if necessary async in background
      if (index > 0) { //if there exists a 'previous'
        maintainCacheSizeByFlushingOldest();

        mediaFile = mediaList.getFileList().get(index - 1);
        if (!isInCache(mediaFile) || (mediaFile.content == null)) { //if not in cache or invalid (because loading failed)
          //maintainCacheSizeByFlushingOldest(); //not necessary again when MIN_FREE_MEM_SIZE is large enough
          mediaFile.getMediaContent();
          if (!isInCache(mediaFile)) cacheBuffer.add(mediaFile);//and remember that it is now in memory
        }
      }
      //preload next media if necessary async. in background
      if (index < mediaList.getFileList().size() - 1) { //if there exists a 'next'
        maintainCacheSizeByFlushingOldest();

        mediaFile = mediaList.getFileList().get(index + 1);
        if (!isInCache(mediaFile) || (mediaFile.content == null)) { //if not in cache or invalid (because loading failed)
          //maintainCacheSizeByFlushingOldest(); //not necessary again when MIN_FREE_MEM_SIZE is large enough
          mediaFile.getMediaContent();
          if (!isInCache(mediaFile)) cacheBuffer.add(mediaFile);//and remember that it is now in memory
        }
      }
    }
    return content;
  }

  /**
   * if the mediaFile is in the Cache then flush it and remove it from the cacheBuffer list
   *
   * @param mediaFile the file to flush (e.g. because it is deleted from disk)
   */
  public void flush(MediaFile mediaFile) {
    if (isInCache(mediaFile)) mediaFile.flushMediaContent();
    cacheBuffer.remove(mediaFile);
  }

  /**
   * Empty Cache by marking all Cache Elements as invalid
   */
  public void flushAll() {

    for (MediaFile element : cacheBuffer) {
      element.flushMediaContent();
    }
    cacheBuffer.clear();
  }

  /**
   * Enable Preload: Enable loading the neighbour files (one before and one after) of the file loaded with getCachedMediaContent(index)
   * in the Cache.
   * By default preloading is on
   */
  public void enablePreLoad() {
    enablePreload = true;
  }

  /**
   * Disable Preload: Disable loading the neighbour files (one before and one after) of the file loaded with getCachedMediaContent(index)
   * in the Cache.
   * By default preloading is on
   */
  public void disablePreLoad() {
    enablePreload = false;
  }
}
