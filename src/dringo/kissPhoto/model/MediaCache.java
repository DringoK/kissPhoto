package dringo.kissPhoto.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a cache strategy for speeding up displaying of pictures/movies:
 * <ul>
 * <li>The type of content and its size can be (very) different for every mediaFile of MediaFileList.
 * <li>The cache buffer therefore tries to keep MIN_FREE_MEM_SIZE bytes free by deleting the oldest media
 * from the cache until MIN-FREE_MEM_SIZE is free again.<br>
 * <li>the cacheBuffer is filled in the order of requests (always stored at the end)
 * <li>the oldest media is removed from cache if it is full
 * </ul>
 *
 * @author Ingo
 * @since 2011-09-12
 * @version 2020-12-20: media Cache now cooperates directly with MediaFile. Preload strategy now in MediaFileList.
 * @version 2019-07-07: improvement of exception handling: subscribing to Error-Property
 * @version 2019-06-22: corrections to cache algo: make robust against wrong estimations by caring about exceptions
 * @version 2014-06-07: getContent interface to cache simplified, min Free memory from oldHeap is only used (@see getAvailableMem)
 * @version 2014-05-25: MIN_FREE_MEM_SIZE is kept instead of a fixed number of maximum cache elements
 * @version 2014-05-04: MediaFiles (links) instead of indices are stored, because indices can be changed by the user
 * @version 2014-04-18: Preload can be disabled temporarily e.g. while moving files in fileTable view
 */
public class MediaCache {
  private static final long MB = 1 << 20;      //=2^20
  private static final long MIN_FREE_MEM_SIZE = 300 * MB;

  private final ObservableList<MediaFile> cacheBuffer;
  private final Runtime env = Runtime.getRuntime();

  /**
   * constructor to build a cache for accessing the list passed in the parameter
   */
  public MediaCache() {
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
   * If the cache is full
   * i.e. less memory available then MIN_FREE_MEM_SIZE
   * then the oldest entries are deleted (and content flushed)
   */
  public void maintainCacheSizeByFlushingOldest() {
    //flushing cached content does not have direct effect on getAvailableMem (until gc() has run)
    //therefore we add a "guessed" value for every MediaFile that has been flushed to the getAvailableMem()
    //to forecast the effect of flushing and to determine how many mediaFiles need to be flushed until MIN_FREE_MEM_SIZE
    //is respected again
    long approxMemFreed = 0;

    //memory full?
    //remove the oldest MediaFiles from cache until MIN_FREE_MEM_SIZE is reached again
    while (getAvailableMem() + approxMemFreed <= MIN_FREE_MEM_SIZE && cacheBuffer.size() > 0) {//size = 0 means: to less memory for caching at all
      MediaFile oldestMediaFile = cacheBuffer.get(0);
      approxMemFreed = approxMemFreed + oldestMediaFile.getContentApproxMemSize(); //begin of the list contains the oldest element
      oldestMediaFile.flushMediaContent();
      cacheBuffer.remove(0);
      //System.out.println("flushing --> cache Buffer Size = " + cacheBuffer.size() + " approxMemFreed=" + approxMemFreed + " availMem="+getAvailableMem());
    }
    env.gc();
    //System.out.println("available mem after gc=" + getAvailableMem());
  }


  /**
   * add the media File into the cacheBuffer list
   * If it is already in Cache than put it at the end of the cache because "youngest" entry
   * the content of the media file remains valid until mediaFile.flushFromCache() is called
   * the cached content is stored in the MediaFile object. Putting it into the cache just determines
   * which if the content is valid and what is the "oldest" mediaFile that is flushed next if out of memory
   *
   * @param mediaFile  the file to be put into the cache
   */
  public void addAsLatest(MediaFile mediaFile) {
    cacheBuffer.remove(mediaFile);  //for the case it was already in the cache
    //add as new/youngest element
    cacheBuffer.add(mediaFile);//and remember that it is now in memory
  }

  /**
   * remove media file from the cacheBuffer list
   * don't forget to put the MediaFile's content to null to free the memory also
   *
   * @param mediaFile to be removed from cache
   */
  public void flush(MediaFile mediaFile) {
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
}
