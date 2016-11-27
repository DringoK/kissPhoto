package de.kissphoto.view.mediaViewers.helper;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a mechanism to hide the playerControls after a certain time;
 * A Thread is started, when the PlayerControls are shown, it sleeps a certain time and hides the PlayerControls again
 * Every time the mouse is moving the Thread is resetted and the waiting begins again.
 * As a result the PlayerControls remain visible while the mouse is moving and for a certain time after the mouse is no longer moving
 * The Thread can also be stopped with immediate hiding. This is e.g. used when skipped to the next media
 *
 * @author Dr. Ingo Kreuz
 * @date 2014-08-03
 * @modified:
 */
public class PlayerControlsHiderThread {
  private Thread thread;
  private HiderTask hiderTask;
  private PlayerControls playerControls;

  /**
   * @param playerControls is the link to the PlayerControls object to show/hide
   * @constructor The Thread is built and waits to be started
   */
  public PlayerControlsHiderThread(PlayerControls playerControls) {
    this.playerControls = playerControls;

    hiderTask = new HiderTask();
    hiderTask.setPlayerControls(playerControls);

    thread = new Thread(hiderTask, "FileWatcher");
    thread.setDaemon(true); //close when main task is closed
    thread.start();

  }

  /**
   * PlayerControls is shown,
   * The thread is started and sleeps for certain time,
   * if it wakes up PlayerControls will be hidden
   * if it is already running the thread is resetted
   */
  public void showPlayerControls() {
    playerControls.setVisible(true);

    hiderTask.setDoHide(true);
    hiderTask.setDoArmTimer(true);
    resetThread();
  }

  /**
   * The thread is stopped and hiding is performed immediately
   * This is e.g. useful when the PlayerViewer skips to the next media
   */
  public void hidePlayerControlsImmediately() {
    hiderTask.setDoHide(true);
    hiderTask.setDoArmTimer(false);
    resetThread();
  }

  public void endThread() {
    hiderTask.setDoHide(false);
    hiderTask.setDoArmTimer(false);
    hiderTask.cancel(true);
  }

  /**
   * wake up the internal Thread from any waiting state
   * Flag variables should be set before to guide the thread what to do next
   * This throws an Exception in the Thread if it is waiting or sleeping
   */
  public void resetThread() {
    try {
      if (thread != null) thread.interrupt();
    } catch (Exception e) {
      //if not possible then leave it running
    }
  }

  /**
   * This Runnable is used to implement a timer by
   * sleeping until resetThread() the thread usually
   * - waits  until resetThread() is called or
   * - sleeps until it will hide the playerControls or resetThread() is called
   * The thread is ended together with the main program
   */
  private static class HiderTask extends Task<Void> {
    //commands for the thread what to do when resetThread() is called or the thread's timer runs out
    protected boolean doHide = false;       //if true hide the PlayerControls
    protected boolean doArmTimer = false;   //if true the Thread will next goto sleep for a certain time and then do the next


    protected PlayerControls playerControls;

    public void setDoHide(boolean doHide) {
      this.doHide = doHide;
    }

    public void setDoArmTimer(boolean doArmTimer) {
      this.doArmTimer = doArmTimer;
    }

    public void setPlayerControls(PlayerControls playerControls) {
      this.playerControls = playerControls;
    }

    @Override
    public Void call() {
      boolean interrupted = false;

      //System.out.println("start thread");
      while (!isCancelled()) { //run forever until program is ended
        try {
          //perform what had been scheduled
          if (!interrupted && doHide) {  //if interrupted then the timer just was reset
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                playerControls.setVisible(false);
              }
            });
          }

          //prepare/wait for next loop
          interrupted = false;
          if (doArmTimer) {
            Thread.sleep(1000);
          } else {
            Thread.sleep(Integer.MAX_VALUE); //sleep very long when waiting for next command then again do nothing but sleep
          }
        } catch (InterruptedException e) {
          interrupted = true;
          //System.out.println(String.format("doHide=%b, doArmTimer=%b", doHide, doArmTimer));
        }
      }
      //System.out.println("end thread");
      return null;
    }
  }
}