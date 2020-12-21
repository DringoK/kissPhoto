package de.kissphoto.view.mediaViewers;

import de.kissphoto.model.MediaFile;
import de.kissphoto.view.MainMenuBar;
import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.viewerHelpers.PlayerControlPanel;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.util.Duration;

import static de.kissphoto.KissPhoto.language;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class is the base class for all viewer with MediaPlayers (i.e. MovieViewerFX (implemented) and MovieViewerVLC
 * It basically consists of a MediaPlayer (to be defined in implementing subclasses) and PlayerControls.
 *
 * @author Dr. Ingo Kreuz
 * @since 2014-07-24
 * @version 2020-12-20: MediaFile-Type and cache content is now controlled by the viewers: only the know what they accept and what should be cached to speed up viewing
 * @version 2020-11-29 sync of menuItems now directly in PlayerControls. Common parts of ZoomableViewer implementation pulled up from the MovieViewers
 * @version 2020-11-02 media Viewers now determine itself if they can show/play a file (no longer the content view)
 * @version 2020-10-25 made abstract and moved JavaFx-Player into MovieViewerFX, so than MovieViewerVLC could be added
 * @version 2017-10-20 single click and space (if not zoomed) will toggle Play/Pause now additionally
 * @version 2017-10-15 handlers installed for mediaPlayer.StatusProperty and  autoPlayProperty to sync the player and the menus (main/context)
 * @version 2017-10-08 autoplay suspended while edit-mode/multi-edit-mode
 *
 */
abstract public class PlayerViewer extends MediaViewerZoomable {

  protected boolean finished; //true if the endOfMedia event had been detected, false if any other status has been detected

  /**
   * @param mediaContentView remember the view where this viewer resides in
   * initialize the play status
   */
  public PlayerViewer(MediaContentView mediaContentView) {
    super(mediaContentView);
  }

  @Override
  void setViewerControlPanel() {
    //do not call super.setViewerControls, because complete repacement!
    viewerControlPanel = new PlayerControlPanel(this);
  }

  /**
   * call this before setting PlayerViewer to null, e.g. to end internal thread
   */
  @Override
  public void cleanUp() {
    super.cleanUp();
    releaseAll();
  }
  /**
   * add the player functions to the context-menu
   */
  protected void initPlayerContextMenu() {
    PlayerControlPanel playerControls = (PlayerControlPanel) viewerControlPanel; //temp to save casting

    //---- Player support
    //contextMenu items for controlling checking and disabling
    MenuItem playPauseItem = new MenuItem(language.getString("play"));  //Pause/Play --> two states reflected by setting text
    playPauseItem.setAccelerator(MainMenuBar.PLAY_PAUSE_KEYCODE);
    playPauseItem.setOnAction(actionEvent -> playerControls.togglePlayPause() );
    playerControls.bindPlayPauseMenuItem(playPauseItem); //keep state of playControls and menuItem synced

    MenuItem rewindItem = new MenuItem(language.getString("rewind"));  //Pause/Play --> two states reflected by setting text
    rewindItem.setAccelerator(MainMenuBar.REWIND_KEYCODE);
    rewindItem.setOnAction(actionEvent -> playerControls.rewind());

    CheckMenuItem playListModeItem = new CheckMenuItem(language.getString("playlist.mode"));
    playListModeItem.setAccelerator(MainMenuBar.PLAYLIST_MODE_KEYCODE);
    //playListModeItem.setOnAction(actionEvent -> playerControls.setPlayListMode(!playerControls.isPlayListMode())); //toggle --> not necessary because of bidirectional binding
    playerControls.bindBidirectionalPlaylistModeMenuItem(playListModeItem); //keep state of playControls and menuItem synced

    CheckMenuItem repeatModeItem = new CheckMenuItem(language.getString("repeat.mode"));
    repeatModeItem.setAccelerator(MainMenuBar.REPEAT_MODE_KEYCODE);
    //repeatModeItem.setOnAction(actionEvent -> playerControls.setRepeatMode(!playerControls.isRepeatMode())); //toggle --> not necessary because of bidirectional binding
    playerControls.bindBidirectionalRepeatMenuItem(repeatModeItem); //keep state of playControls and menuItem synced

    contextMenu.getItems().addAll(playPauseItem, rewindItem, playListModeItem, repeatModeItem, new SeparatorMenuItem());
  }
  /**
   *
   * @return the status of the media player of this PlayerViewer
   */
  abstract public javafx.scene.media.MediaPlayer.Status getStatus();

/**
   * put the media (movie) into the MovieViewer and play it if "playing" was active before or pause it if not
   *
   * @param mediaFile containing the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the playable media is loaded/visible
   * @return true if the file could be played, false if not
   */
  abstract public boolean setMediaFileIfCompatible(MediaFile mediaFile, Duration seekPosition);

  /**
   * reset the player: stop it and free all event Handlers
   */
  abstract public void resetPlayer();

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  abstract public void play();

  /**
   * start player from the beginning to implement repeat track
   */
  abstract public void rewindAndPlayWhenFinished();

  /**
   * start player and adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  abstract public void pause();


  /**
   * stop, rewind
   * adjust menuItems (disable/enable)
   * if mediaPlayer is null (currently no media file displayed) nothing happens
   */
  abstract public void stop();

  /**
   * seek Position (Duration)
   * if mediaPlayer is null (currently no media file displayed) nothin happens
   * @param newPos position to jump to
   */
  abstract public void seek(Duration newPos);

  /**
   * get the current position of the media currently playing
   * @return current position
   */
  abstract public Duration getCurrentTime();

  /**
   * if mediaPlayer is null (currently no media file displayed) Duration(0) is returned
   * @return the total length of the currently loaded media
   */
  abstract public Duration getTotalDuration();

  /**
   * Call this method when closing the main program to release all external resources
   */
  abstract public void releaseAll();

  public PlayerControlPanel getPlayerControls(){
    return (PlayerControlPanel) viewerControlPanel;
  }

  //----------------------- Implement common part of ZoomableViewer Interface ----------------------------


  /**
   * reinstall the player shortcuts (incl. viewPortShortcuts) because main menu not active while player is active
   */
  @Override
  public void installKeyboardHandlers() {
    //no super.installKeyboardHandlers because complete replace
    PlayerControlPanel playerControls = (PlayerControlPanel) viewerControlPanel; //one central cast

    setOnKeyPressed(event -> {
      boolean handled = false;

      //try player shortcuts first
      switch (event.getCode()) {
        case SPACE:
          if (!event.isControlDown() && !event.isShiftDown()) {
            playerControls.togglePlayPause();
            handled = true;
          }
          break;
        case P:
          if (event.isControlDown() && event.isShiftDown()) {
            playerControls.setPlayListMode(!playerControls.isPlayListMode());
            handled = true;
          }
          break;
        case R:
          if (event.isControlDown() && event.isShiftDown()) {
            playerControls.setRepeatMode(!playerControls.isRepeatMode());
            handled = true;
          }
          break;
        case LEFT:
          if (event.isControlDown() && !event.isShiftDown()) {
            playerControls.rewind();
            handled = true;
          }
      }

     //try viewport shortcuts
      if (!handled){
        handled = handleKeyPressed(event);  //inherited from MediaViewerZoomable
      }
      if (handled) event.consume();
    });
  }
}
