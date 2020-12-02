package de.kissphoto.view.mediaViewers;

import de.kissphoto.model.MediaFile;
import de.kissphoto.view.MainMenuBar;
import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.viewerHelpers.PlayerControls;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
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
 * @version 2020-11-29 sync of menuItems now directly in PlayerControls. Common parts of ZoomableViewer implementation pulled up from the MovieViewers
 * @version 2020-11-02 media Viewers now determine itself if they can show/play a file (no longer the content view)
 * @version 2020-10-25 made abstract and moved JavaFx-Player into MovieViewerFX, so than MovieViewerVLC could be added
 * @version 2017-10-20 single click and space (if not zoomed) will toggle Play/Pause now additionally
 * @version 2017-10-15 handlers installed for mediaPlayer.StatusProperty and  autoPlayProperty to sync the player and the menus (main/context)
 * @version 2017-10-08 autoplay suspended while edit-mode/multi-edit-mode
 *
 */
abstract public class PlayerViewer extends StackPane implements ZoomableViewer {

  protected boolean finished; //true if the endOfMedia event had been detected, false if any other status has been detected

  //contextMenu items for controlling checking and disabling
  protected ContextMenu contextMenu = new ContextMenu();
  private MenuItem playPauseItem;
  private MenuItem rewindItem;
  private CheckMenuItem playListModeItem;
  private CheckMenuItem repeatModeItem;

  protected MediaContentView mediaContentView; //link to the underlying mediaContentView (e.g.for binding sizes and for next media after endOfMedia)

  protected PlayerControls playerControls;

  private boolean lastMouseButtonWasPrimary = false;
  private boolean lastMouseDownWasMouseDragged = false;
  protected ViewportZoomer viewportZoomer;


  /**
   * @param mediaContentView remember the view where this viewer resides in
   * initialize the play status
   */
  public PlayerViewer(MediaContentView mediaContentView) {
    super();

    this.mediaContentView = mediaContentView;
    playerControls = new PlayerControls(this);
    setAlignment(playerControls, Pos.TOP_CENTER);
    //will be added to this StackPane in the implementing subclasses on top of their mediaView

    viewportZoomer = new ViewportZoomer(this);
    installMouseHandlers();
    installKeyboardHandlers();

    //visible only while hovering
    playerControls.setVisible(false);
  }

  /**
   * @param event mouseEvent to be handled
   * @return if event has been handled
   */
  public boolean handleMouseMoved(MouseEvent event) {
    playerControls.resetThreadAndShow();
    return true;
  }
  /**
   * @param event mouseEvent to be handled
   * @return if event has been handled
   */
  public boolean handleMouseDragged(MouseEvent event) {
    lastMouseDownWasMouseDragged = true;
    return false; //nothing has happened for the user, so don't block further actions if any
  }
  /**
   * @param event mouseEvent to be handled
   * @return if event has been handled
   */
  public boolean handleMousePressed(MouseEvent event) {
    lastMouseButtonWasPrimary = event.isPrimaryButtonDown();
    return false; //nothing has happened for the user
  }
  /**
   *
   * @param event mouseEvent to be handled
   * @return if event has been handled
   */
  public boolean handleMouseClicked(MouseEvent event) {
    boolean handled = false;
    //do not handle double-click i.e. this is full screen
    if (event.getClickCount() == 1 && lastMouseButtonWasPrimary && !lastMouseDownWasMouseDragged) {
      //togglePlayPause();// would interfere with the controlArea of playerControls
      handled = true;
      }
    lastMouseDownWasMouseDragged = false;
    return handled;
  }
  /**
   * call this before setting PlayerViewer to null, e.g. to end internal thread
   */
  public void cleanUp() {
    playerControls.cleanUp();
    releaseAll();
  }
  /**
   * add the player functions to the context-menu
   */
  protected void initPlayerContextMenu() {
    //---- Player support
    playPauseItem = new MenuItem(language.getString("play"));  //Pause/Play --> two states reflected by setting text
    playPauseItem.setAccelerator(MainMenuBar.PLAY_PAUSE_KEYCODE);
    playPauseItem.setOnAction(actionEvent -> playerControls.togglePlayPause() );
    playerControls.bindPlayPauseMenuItem(playPauseItem); //keep state of playControls and menuItem synced

    rewindItem = new MenuItem(language.getString("rewind"));  //Pause/Play --> two states reflected by setting text
    rewindItem.setAccelerator(MainMenuBar.REWIND_KEYCODE);
    rewindItem.setOnAction(actionEvent -> playerControls.rewind());

    playListModeItem = new CheckMenuItem(language.getString("playlist.mode"));
    playListModeItem.setAccelerator(MainMenuBar.PLAYLIST_MODE_KEYCODE);
    //playListModeItem.setOnAction(actionEvent -> playerControls.setPlayListMode(!playerControls.isPlayListMode())); //toggle --> not necessary because of bidirectional binding
    playerControls.bindBidirectionalPlaylistModeMenuItem(playListModeItem); //keep state of playControls and menuItem synced

    repeatModeItem = new CheckMenuItem(language.getString("repeat.mode"));
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

  public PlayerControls getPlayerControls(){
    return playerControls;
  }

  //----------------------- Implement common part of ZoomableViewer Interface ----------------------------

  @Override
  public void installResizeHandler() {
    prefWidthProperty().addListener((observable, oldValue, newValue) -> viewportZoomer.handleResize());
    prefHeightProperty().addListener((observable, oldValue, newValue) -> viewportZoomer.handleResize());
  }

  @Override
  public void zoomToFit() {
    viewportZoomer.zoomToFit();
  }

  private void installMouseHandlers() {
    setOnScroll(event -> {
      boolean handled = viewportZoomer.handleMouseScroll(event);
      if (handled) event.consume();
    });
    setOnMousePressed(event -> {
      boolean handled = viewportZoomer.handleMousePressed(event);
      handled = handleMousePressed(event) || handled; //inherited from PlayerViewer
      if (handled) event.consume();
    });

    setOnMouseMoved(event -> {
      boolean handled = handleMouseMoved(event); //inherited from PlayerViewer
      if (handled) event.consume();
    });
    setOnMouseDragged(event -> {
      boolean handled = viewportZoomer.handleMouseDragged(event);
      handled = handleMouseDragged(event) || handled; //inherited from PlayerViewer
      if (handled) event.consume();
    });
    setOnMouseReleased(event -> {
      boolean handled = viewportZoomer.handleMouseReleased(event);
      if (handled) event.consume();
    });
    setOnMouseClicked(event -> {
      //clicks must only be handled by one class to perform only one action at a time
      boolean handled = viewportZoomer.handleMouseClicked(event);
      if (!handled) {
        handled = handleMouseClicked(event);
      } //inherited from PlayerViewer
      if (handled) event.consume();
    });

  }

  /**
   * reinstall the player shortcuts (incl. viewPortShortcuts) because main menu not active while player is active
   */
  private void installKeyboardHandlers() {
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
      }

     //try viewport shortcuts
      if (!handled){
        handled = viewportZoomer.handleKeyPressed(event);
      }
      if (handled) event.consume();
    });
  }

  public ContextMenu getContextMenu() {
    return contextMenu;
  }
  public MediaContentView getMediaContentView(){
    return mediaContentView;
  }
}
