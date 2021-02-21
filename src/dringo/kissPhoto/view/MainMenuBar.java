package dringo.kissPhoto.view;


import dringo.kissPhoto.model.ImageFileRotater;
import dringo.kissPhoto.view.dialogs.AboutDialog;
import dringo.kissPhoto.view.dialogs.ExternalEditorsDialog;
import dringo.kissPhoto.view.dialogs.LanguageDialog;
import dringo.kissPhoto.view.dialogs.WriteFolderStructureCSVDialog;
import dringo.kissPhoto.view.viewerHelpers.PlayerControlPanel;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The main menu bar is defined here
 * <p/>
 *
 * @author Ingo
 * @since 2012-09-09
 * @version 2021-01-09 findNext in editMenu
 * @version 2020-11-19 globalSettings is now global (static in Kissphoto)  and therefore no longer necessary in this class (formerly just passed through)
 * @version 2018-11-17 Image menu only active if an image is selected
 * @version 2017-10-29 Flipping and Rotation of JPEG-Images added (imageMenu)
 * @version 2017-10-21 FileHistory-Support added and reopen removed (because equal to ctrl-0)
 * @version 2017-10-15 see also PlayerViewer: handlers installed for mediaPlayer.StatusProperty and  autoPlayProperty to sync the player and the menus (main/context)
 * @version 2017-10-13 added AutoFill(Down) Menu-Item to edit menu + Default Column Widths to View menu
 * @version 2015-10-04 moving changed to ctr-Cursor up/down: Shift-Alt-Cursor up/down does not work under Windows 10 (menu is activated instead)
 * @version 2014-16-16 support for full screen mode added to view menu
 * @version 2014-05-02 (I18Support, Reopen added)
 * @version 2014-04-29 added about menu and no longer used the MenuItemBuilder (which is deprecated now)
 */
public class MainMenuBar extends MenuBar {
  protected FileTableView fileTableView; //link to fileTableView to call methods via menu/shortcut
  protected MediaContentView mediaContentView; //link to mediaContentView for full screen etc
  private final Stage primaryStage; //link to embedding window
  private final String versionString; //link to versionString of Main Window for About Dialog

  private final Menu fileMenu = new Menu(language.getString("fileMenu"));
  private final Menu editMenu = new Menu(language.getString("editMenu"));
  private final Menu viewMenu = new Menu(language.getString("viewMenu"));
  private final Menu imageMenu = new Menu(language.getString("image"));
  private final Menu playerMenu = new Menu(language.getString("player"));
  private final Menu extrasMenu = new Menu(language.getString("extrasMenu"));
  private final Menu helpMenu = new Menu(language.getString("helpMenu"));

  public final static KeyCodeCombination PLAY_PAUSE_KEYCODE = new KeyCodeCombination(KeyCode.SPACE);
  public final static KeyCodeCombination REWIND_KEYCODE = new KeyCodeCombination(KeyCode.LEFT, KeyCodeCombination.CONTROL_DOWN);
  public final static KeyCodeCombination PLAYLIST_MODE_KEYCODE = new KeyCodeCombination(KeyCode.P, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN);
  public final static KeyCodeCombination REPEAT_MODE_KEYCODE = new KeyCodeCombination(KeyCode.R, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN);

  public final static KeyCombination fullScreenKeyCombinationStart = new KeyCodeCombination(KeyCode.F5);
  public final static KeyCombination fullScreenKeyCombinationEnd = new KeyCodeCombination(KeyCode.ESCAPE);


  /**
   * create the main menu bar and install all shortcuts/handlers
   *
   * @param primaryStage   link to the main window
   * @param fileTableView  link for calling of most of the methods, i.e. Filetable.open etc.
   * @param versionString  link for the about Dialog
   */
  public MainMenuBar(Stage primaryStage, FileTableView fileTableView, MediaContentView mediaContentView, String versionString) {
    super();
    this.primaryStage = primaryStage;
    this.fileTableView = fileTableView;
    this.mediaContentView = mediaContentView;
    this.versionString = versionString;

    createFileMenu();
    createEditMenu();
    createViewMenu();
    createImageMenu();
    createPlayerMenu();
    createExtrasMenu();
    createHelpMenu();

    //imageMenu is active only if an image is selected, i.e. if the PhotoViewer is visible
    imageMenu.disableProperty().bind(mediaContentView.getIsImageActive().not());
    //playerMenu is active only if a playable media (movie/sound) is selected, i.e. if the PlayerViewer is visible
    playerMenu.disableProperty().bind(mediaContentView.getIsPlayerActive().not());

  }

  /**
   * ########################### File Menu Items and actions ###########################
   */
  private void createFileMenu() {

    final MenuItem openItem = new MenuItem(language.getString("openMenu"));
    openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    openItem.setOnAction(event -> {
      event.consume();
      fileTableView.chooseFileOrFolder();
    });
    fileMenu.getItems().add(openItem);

    MenuItem saveItem = new MenuItem(language.getString("saveMenu"));
    saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    saveItem.setOnAction(event -> {
      event.consume();
      fileTableView.saveFolder();
    });
    fileMenu.getItems().add(saveItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    MenuItem exportCSVItem = new MenuItem(language.getString("exportCSVMenu"));
    exportCSVItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
    exportCSVItem.setOnAction(event -> {
      event.consume();
      fileTableView.chooseFilenameAndExportToCSV();
    });
    fileMenu.getItems().add(exportCSVItem);

    MenuItem exportFolderCSVItem = new MenuItem(language.getString("writeFolderStructureCSVMenu"));
    exportFolderCSVItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    exportFolderCSVItem.setOnAction(event -> {
      event.consume();
      WriteFolderStructureCSVDialog writeFolderStructureCSVDialog = new WriteFolderStructureCSVDialog(primaryStage, fileTableView.getMediaFileList(), fileTableView.getStatusBar());
      writeFolderStructureCSVDialog.showModal();
    });
    fileMenu.getItems().add(exportFolderCSVItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    MenuItem exitItem = new MenuItem(language.getString("exitMenu"));
    exitItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
    exitItem.setOnAction(event -> {
      event.consume();
      //note: Platform.exit() and primaryStage.close() would not fire the "onClose"-Event-Handler!
      primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    });
    fileMenu.getItems().add(exitItem);


    getMenus().add(fileMenu);
  }

  public void addRecentlyMenu(Menu recentlyMenu) {
    fileMenu.getItems().add(1, recentlyMenu);
  }
  /**
   * ########################### Edit Menu Items ###########################
   */
  private void createEditMenu() {
    final MenuItem findReplaceItem = new MenuItem(language.getString("findReplaceMenu"));
    findReplaceItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
    findReplaceItem.setOnAction(event -> {
      event.consume();
      fileTableView.findAndReplace();
    });
    editMenu.getItems().add(findReplaceItem);

    final MenuItem findNextItem = new MenuItem(language.getString("find.next"));
    findNextItem.setAccelerator(new KeyCodeCombination(KeyCode.F3));
    findNextItem.disableProperty().bind(fileTableView.getFindReplaceDialogShowingProperty().not());
    findNextItem.setOnAction(event -> {
      event.consume();
      fileTableView.findNext();
    });
    editMenu.getItems().add(findNextItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem renameItem = new MenuItem(language.getString("renameMenu"));
    renameItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
    renameItem.setOnAction(event -> {
      event.consume();
      fileTableView.rename();
    });
    editMenu.getItems().add(renameItem);

    final MenuItem autoFillDownItem = new MenuItem(language.getString("autofill.copy.down"));
    autoFillDownItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
    autoFillDownItem.setOnAction(event -> {
      event.consume();
      fileTableView.copyDescriptionDown();
    });
    editMenu.getItems().add(autoFillDownItem);

    final MenuItem externalMainEditorItem = new MenuItem(language.getString("external.editor.1"));
    externalMainEditorItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.CONTROL_DOWN));
    externalMainEditorItem.setOnAction(event -> {
      event.consume();
      fileTableView.executeExternalEditorForSelection(true);
    });
    editMenu.getItems().add(externalMainEditorItem);

    final MenuItem external2ndEditorItem = new MenuItem(language.getString("external.editor.2"));
    external2ndEditorItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.SHIFT_DOWN));
    external2ndEditorItem.setOnAction(event -> {
      event.consume();
      fileTableView.executeExternalEditorForSelection(false);
    });
    editMenu.getItems().add(external2ndEditorItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem timeStampItem = new MenuItem(language.getString("timeStampMenu"));
    timeStampItem.setDisable(true); //not yet implemented
    editMenu.getItems().add(timeStampItem);

    final MenuItem copyTimeStampExtItem = new MenuItem(language.getString("copy.file.dates.by.extension"));
    copyTimeStampExtItem.setOnAction(actionEvent -> fileTableView.copyFileDatesByExtension());
    editMenu.getItems().add(copyTimeStampExtItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem deleteItem = new MenuItem(language.getString("deleteMenu"));
    deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
    deleteItem.setOnAction(event -> {
      event.consume();
      fileTableView.deleteSelectedFiles(false);
    });
    editMenu.getItems().add(deleteItem);

    final MenuItem unDeleteItem = new MenuItem(language.getString("undeleteMenu"));
    unDeleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN));
    unDeleteItem.setOnAction(event -> {
      event.consume();
      fileTableView.unDeleteWithDialog();
    });
    unDeleteItem.setDisable(true);      //non-active until first deletion
    editMenu.getItems().add(unDeleteItem);
    fileTableView.setUnDeleteMenuItem(unDeleteItem);     //pass a link to the father window so there enabling/disabling can be controlled

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem cutItem = new MenuItem(language.getString("cutMenu"));
    cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
    cutItem.setOnAction(event -> {
      event.consume();
      fileTableView.cutToClipboard();
    });
    editMenu.getItems().add(cutItem);

    final MenuItem pasteItem = new MenuItem(language.getString("pasteMenu"));
    pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
    pasteItem.setOnAction(event -> {
      event.consume();
      fileTableView.pasteFromClipboard();
    });
    pasteItem.setDisable(true);      //non-active until first deletion
    editMenu.getItems().add(pasteItem);
    fileTableView.setPasteMenuItem(pasteItem);     //pass a link to the father window so there enabling/disabling can be controlled

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem autoNumberItem = new MenuItem(language.getString("renumber.standardMenu"));
    autoNumberItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    autoNumberItem.setOnAction(event -> {
      event.consume();
      fileTableView.renumberSelectionRelativeToIndices();
    });
    editMenu.getItems().add(autoNumberItem);

    final MenuItem reNumberGlobalItem = new MenuItem(language.getString("renumber.globalMenu"));
    reNumberGlobalItem.setAccelerator((new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)));
    reNumberGlobalItem.setOnAction(event -> {
      event.consume();
      fileTableView.renumberWithDialog(true);
    });
    editMenu.getItems().add(reNumberGlobalItem);

    final MenuItem reNumberLocalItem = new MenuItem(language.getString("renumber.localMenu"));
    reNumberLocalItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
    reNumberLocalItem.setOnAction(event -> {
      event.consume();
      fileTableView.renumberWithDialog(false);
    });
    editMenu.getItems().add(reNumberLocalItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem moveUpItem = new MenuItem(language.getString("move.upMenu"));
    //shift-alt ok: compatible with windows: alt+??=menu, shift-alt-arrow = move, compatible with ubuntu: shift-alt used for changing windows
    //here only for displaying the shortcut. It must be set additionally directly in FileTableView constructor to override standard use of Shift-Ctrl-Up/Down=extend selection
    moveUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    moveUpItem.setOnAction(event -> {
      event.consume();
      fileTableView.moveSelectedFilesUp();

    });
    editMenu.getItems().add(moveUpItem);

    final MenuItem moveDnItem = new MenuItem(language.getString("move.downMenu"));
    moveDnItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    moveDnItem.setOnAction(event -> {
      event.consume();
      fileTableView.moveSelectedFilesDown();
    });
    editMenu.getItems().add(moveDnItem);

    getMenus().add(editMenu);
  }

  /**
   * ########################### View Menu Items ###########################
   */
  private void createViewMenu() {
    final MenuItem resetSortingItem = new MenuItem(language.getString("reset.sortingMenu"));
    resetSortingItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    resetSortingItem.setOnAction(event -> {
      event.consume();
      fileTableView.resetSortOrder();
    });
    viewMenu.getItems().add(resetSortingItem);

    final MenuItem resetColWidthItem = new MenuItem(language.getString("reset.column.widths"));
    resetColWidthItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    resetColWidthItem.setOnAction(event -> {
      event.consume();
      fileTableView.setDefaultColumnWidths();
    });
    viewMenu.getItems().add(resetColWidthItem);

    viewMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem fullScreenItem = new MenuItem();
    mediaContentView.setFullScreenMenuItemText(fullScreenItem); //initializes including accelerator key

    fullScreenItem.setOnAction(actionEvent -> {
      actionEvent.consume();
      mediaContentView.toggleFullScreenAndNormal();
    });
    mediaContentView.getIsFullScreenActiveProperty().addListener((observable, oldValue, newValue) -> mediaContentView.setFullScreenMenuItemText(fullScreenItem));

    MenuItem showOnNextScreenItem = new MenuItem(language.getString(MediaContentView.SHOW_ON_NEXT_SCREEN_FULLSCREEN));
    showOnNextScreenItem.setAccelerator((new KeyCodeCombination(KeyCode.TAB))); //TAB, previous shift-Tab is not shown in menu
    showOnNextScreenItem.setOnAction(actionEvent -> mediaContentView.showFullScreenOnNextScreen(true));
    showOnNextScreenItem.disableProperty().bind(mediaContentView.getIsFullScreenActiveProperty().not());  //only enabled if fullScreen-Mode is active

    viewMenu.getItems().addAll(fullScreenItem, showOnNextScreenItem);

    final MenuItem slideShowItem = new MenuItem(language.getString("slide.showMenu"));
    slideShowItem.setDisable(true); //not yet implemented
    viewMenu.getItems().add(slideShowItem);
    getMenus().add(viewMenu);
  }

  private void createImageMenu() {
    MenuItem exifOrientationItem = new MenuItem(language.getString("rotate.and.flip.jpeg.according.exif.lossless"));
    exifOrientationItem.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN));
    exifOrientationItem.setOnAction(event -> fileTableView.setOrientationAccordingExif());
    imageMenu.getItems().addAll(exifOrientationItem, new SeparatorMenuItem());

    MenuItem rotateRightItem = new MenuItem(language.getString("rotate.jpeg.90.right.lossless"));
    rotateRightItem.setAccelerator(new KeyCodeCombination(KeyCode.R));
    rotateRightItem.setOnAction(event -> fileTableView.rotateSelectedFiles(ImageFileRotater.RotateOperation.ROTATE90));
    imageMenu.getItems().add(rotateRightItem);

    MenuItem rotateLeftItem = new MenuItem(language.getString("rotate.jpeg.90.left.lossless"));
    rotateLeftItem.setAccelerator(new KeyCodeCombination(KeyCode.L));
    rotateLeftItem.setOnAction(event -> fileTableView.rotateSelectedFiles(ImageFileRotater.RotateOperation.ROTATE270));
    imageMenu.getItems().add(rotateLeftItem);

    MenuItem rotate180Item = new MenuItem(language.getString("rotate.jpeg.180.lossless"));
    rotate180Item.setAccelerator(new KeyCodeCombination(KeyCode.T));
    rotate180Item.setOnAction(event -> fileTableView.rotateSelectedFiles(ImageFileRotater.RotateOperation.ROTATE180));
    imageMenu.getItems().addAll(rotate180Item, new SeparatorMenuItem());

    MenuItem flipHItem = new MenuItem(language.getString("flip.jpeg.horizontally.lossless"));
    flipHItem.setAccelerator(new KeyCodeCombination(KeyCode.H));
    flipHItem.setOnAction(event -> fileTableView.flipSelectedFiles(true));
    imageMenu.getItems().add(flipHItem);

    MenuItem flipVItem = new MenuItem(language.getString("flip.jpeg.vertically.lossless"));
    flipVItem.setAccelerator(new KeyCodeCombination(KeyCode.V));
    flipVItem.setOnAction(event -> fileTableView.flipSelectedFiles(false));
    imageMenu.getItems().add(flipVItem);

    getMenus().add(imageMenu);

  }
  private void createPlayerMenu() {
    PlayerControlPanel playerControls = mediaContentView.getPlayerViewer().getPlayerControls();
    MenuItem playPauseItem;
    MenuItem rewindItem;
    CheckMenuItem playListModeItem;
    CheckMenuItem repeatModeItem;

    playPauseItem = new MenuItem(language.getString("play"));  //Pause/Play --> two states reflected by setting text
    playPauseItem.setAccelerator(MainMenuBar.PLAY_PAUSE_KEYCODE);
    playPauseItem.setOnAction(actionEvent -> {
      playerControls.togglePlayPause();
      System.out.println("MainMenuBar.TogglePlayPause()");
    });
    playerControls.bindPlayPauseMenuItem(playPauseItem); //keep state of playControls and menuItem synced

    rewindItem = new MenuItem(language.getString("rewind"));  //Pause/Play --> two states reflected by setting text
    rewindItem.setAccelerator(MainMenuBar.REWIND_KEYCODE);
    rewindItem.setOnAction(actionEvent -> playerControls.rewind());

    playListModeItem = new CheckMenuItem(language.getString("playlist.mode"));
    playListModeItem.setAccelerator(MainMenuBar.PLAYLIST_MODE_KEYCODE);
    //playListModeItem.setOnAction(actionEvent -> playerControls.setPlayListMode(!playerControls.isPlayListMode())); //toggle --> not necessary, because bidirectional binding
    playerControls.bindBidirectionalPlaylistModeMenuItem(playListModeItem); //keep state of playControls and menuItem synced

    repeatModeItem = new CheckMenuItem(language.getString("repeat.mode"));
    repeatModeItem.setAccelerator(MainMenuBar.REPEAT_MODE_KEYCODE);
    //repeatModeItem.setOnAction(actionEvent -> playerControls.setRepeatMode(!playerControls.isRepeatMode())); //toggle --> not necessary, because bidirectional binding
    playerControls.bindBidirectionalRepeatMenuItem(repeatModeItem); //keep state of playControls and menuItem synced

    playerMenu.getItems().addAll(playPauseItem, rewindItem, playListModeItem, repeatModeItem);
    getMenus().add(playerMenu);
  }

  /**
   * ########################### Extras Menu Items ###########################
   */
  LanguageDialog languageDialog;
  ExternalEditorsDialog externalEditorsDialog;

  private void createExtrasMenu() {
    MenuItem languageItem = new MenuItem(language.getString("language.settingsMenu"));
    languageItem.setOnAction(event -> {
      event.consume();
      if (languageDialog == null) languageDialog = new LanguageDialog(primaryStage);
      languageDialog.showModal();
    });
    extrasMenu.getItems().add(languageItem);

    MenuItem externalEditorsItem = new MenuItem(language.getString("specify.external.editors"));
    externalEditorsItem.setOnAction(event -> {
      event.consume();
      if (externalEditorsDialog == null) externalEditorsDialog = new ExternalEditorsDialog(primaryStage);
      externalEditorsDialog.showModal();
    });
    extrasMenu.getItems().add(externalEditorsItem);
    getMenus().add(extrasMenu);

  }

  /**
   * ########################### Help Menu Items ###########################
   */
  AboutDialog aboutDialog;

  private void createHelpMenu() {
    MenuItem aboutItem = new MenuItem(language.getString("aboutMenu"));
    aboutItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
    aboutItem.setOnAction(event -> {
      event.consume();
      if (aboutDialog == null) aboutDialog = new AboutDialog(primaryStage, versionString);
      aboutDialog.showModal();
    });
    helpMenu.getItems().add(aboutItem);
    getMenus().add(helpMenu);
  }

}
