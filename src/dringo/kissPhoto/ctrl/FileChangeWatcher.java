package dringo.kissPhoto.ctrl;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto

 * A thread uses a FileSystem.WatchService
 * to monitor if files if a directory have been changed externally
 * <p/>
 * This class is based on a solution found on com.thecoderscorner.niostuff;
 *
 * @author Dringo
 * @since 2014-05-04
 * @version 2020-11-29 code clean up
 * @version 2014-06-08 folder update is toooooo slow. Just a message is given to the user...that reopen could help...: not all events are signalled!!!
 * @version 2014-06-06 Thread handling improved: Thread.interrupt to stop thread and Watcher created in the thread
 */

public class FileChangeWatcher {
  private Thread fileWatcherThread;
  private MyWatchQueueReader queueReader;

  //for internally re-registering on continueWatching;
  private String lastPathToWatch;
  private FileChangeWatcherEventListener lastListener;

  public FileChangeWatcher() {
  }

  /**
   * stop any existing thread created before and thereby stop watching any old folder
   * then start a new thread and register the pathToWatch
   * register the listener to receive the events 'onFolderChanged" @see FileChangeWatcherEventListener-Interface
   *
   * @param pathToWatch a path to the folder to be watched
   * @param listener    a class implementing FilechangeWatcherEventListener-interface to receive "onFolderChanged"Events
   */
  public void registerFolderToWatch(String pathToWatch, FileChangeWatcherEventListener listener) {
    //stop watcher threads started before
    stopWatcherThread();

    // get the directory we want to watch, using the Paths singleton class
    Path toWatch = Paths.get(pathToWatch);

    // start the file watcher thread below
    queueReader = new MyWatchQueueReader(toWatch, listener);
    fileWatcherThread = new Thread(queueReader, "FileWatcher");
    fileWatcherThread.setDaemon(true); //close when main task is closed
    fileWatcherThread.start();

    //remember status for pausing/resuming
    queueReader.paused = false;
    lastPathToWatch = pathToWatch;
    lastListener = listener;
  }

  /**
   * end the internal watcher Tread (if exisitng) by calling close()
   * This throws an Exception in the Thread if waiting or setting isCancelled() to true thus ending the thread
   */
  public void stopWatcherThread() {
    try {
      if (fileWatcherThread != null) fileWatcherThread.interrupt();
    } catch (Exception e) {
      //if not possible then leave it running
    }
  }

  /**
   * set internal state for ignoring changes in the folder without de-registering the folder. Thread is stopped.
   * While paused the thread watch events will not produce onChanged - Events
   */
  public void pauseWatching() {
    queueReader.paused = true;
    stopWatcherThread();

    // waiting for termination of thread
    try {
      if (fileWatcherThread.isAlive())
        fileWatcherThread.join();   //join() waits for dying, wait() for any notification notify()
    } catch (Exception e) {
      //if main thread interrupted then continue directly
    }
  }

  /**
   * set internal state for no longer ignoring changes in the folder after pause
   * watch events will produce onChanged - Events again
   */
  public void continueWatching() {
    try {
      registerFolderToWatch(lastPathToWatch, lastListener);
    } catch (Exception e) {
      //ignore exceptions while registering
    }
    queueReader.paused = false;
  }

  /**
   * does the active thread produce onFolderChanged events?
   *
   * @return if the thread is currently paused
   */
  public boolean isPaused() {
    return queueReader.paused;
  }

  /**
   * This Runnable is used to constantly attempt to take from the watch
   * queue, and will receive all events that are registered with the
   * fileWatcher it is associated. In this sample for simplicity we
   * just output the kind of event and name of the file affected to
   * standard out.
   */
  private static class MyWatchQueueReader extends Task<Void> {
    private final FileChangeWatcherEventListener listener;
    private final Path pathToWatch;
    private String currentFilename;
    private WatchEvent.Kind<?> currentKind;
    protected boolean paused = false;

    /**
     * the watchService that is passed in from above
     */
    private WatchService watcher;

    public MyWatchQueueReader(Path toWatch, FileChangeWatcherEventListener listener) {
      this.pathToWatch = toWatch;
      this.listener = listener;
    }

    /**
     * In order to implement a file watcher, we loop forever
     * ensuring requesting to take the next item from the file
     * watchers queue.
     */
    @Override
    public Void call() throws Exception {
      //during saving the Thread is paused=ended and recreated
      Thread.sleep(5000); //wait 5s until all changes of the own savings have been signalled
      try {
        // make a new watch service that we can register interest in directories and files with.
        watcher = pathToWatch.getFileSystem().newWatchService();
        // register the directory
        pathToWatch.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE, OVERFLOW);

        // get the first event before looping
        WatchKey key = watcher.take();  //wait for event

        while (key != null) {
          // we have a polled event, now we traverse it and
          // receive all the states from it
          //for (WatchEvent event : key.pollEvents()) {
          WatchEvent<?> event = key.pollEvents().get(0); //signal just the first event
          currentFilename = event.context().toString();
          currentKind = event.kind();

          //access FXApplication Thread only via Platform.runLater (see tutorial from oracle about Task)
          //produce events only while not paused = while paused ignore events (consume them)
          if (!paused) Platform.runLater(() -> {
            listener.onFolderChanged(currentFilename, currentKind); //the event object is new every time from key.pollEvents()
          });
          //Thread.sleep(50); //give the listener a chance to process events before waiting for the next event
          //}
          key.reset();

          //Thread.sleep(50); //give the listener a chance to process events before waiting for the next event
          key = watcher.take(); //wait for next event
          if (isCancelled()) break;
        }
      } catch (Exception e) {
        //close() initiates a ClosedWatchServiceException which is used here for finishing the thread as shown in oracles tuturial
      } finally {
        if (watcher != null) watcher.close();
      }
      return null;
    }
  }
}