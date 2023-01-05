package dringo.kissPhoto.view;


import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.model.MediaFile;
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

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The main menu bar is defined here
 * <p/>
 *
 * @author Ingo
 * @since 2012-09-09
 * @version 2023-01-05 undelete last file supported. Gray-out of undelete menuItems now use Bindings
 * @version 2022-09-04 clean up primaryStage parameter
 * @version 2021-11-01 clean prefixes, numbering added
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
  public static final String RENAME_MENU = "renameMenu";
  public static final String RENUMBER_STANDARD_MENU = "renumber.standardMenu";
  public static final String CLEAN_PREFIXES = "clean.prefixes";
  public static final String CLEAN_COUNTERS = "clean.counters";
  private final FileTableView fileTableView; //link to fileTableView to call methods via menu
  private final MediaContentView mediaContentView; //link to mediaContentView for full screen etc
  private final MetaInfoView metaInfoView; //link to metaInfoView for showing/hiding it via view menu

  private final Menu fileMenu = new Menu(KissPhoto.language.getString("fileMenu"));
  private final Menu editMenu = new Menu(KissPhoto.language.getString("editMenu"));
  private final Menu viewMenu = new Menu(KissPhoto.language.getString("viewMenu"));
  private final Menu imageMenu = new Menu(KissPhoto.language.getString("image"));
  private final Menu playerMenu = new Menu(KissPhoto.language.getString("player"));
  private final Menu extrasMenu = new Menu(KissPhoto.language.getString("extrasMenu"));
  private final Menu helpMenu = new Menu(KissPhoto.language.getString("helpMenu"));

  public final static KeyCodeCombination PLAY_PAUSE_KEYCODE = new KeyCodeCombination(KeyCode.SPACE);
  public final static KeyCodeCombination REWIND_KEYCODE = new KeyCodeCombination(KeyCode.LEFT, KeyCodeCombination.CONTROL_DOWN);
  public final static KeyCodeCombination PLAYLIST_MODE_KEYCODE = new KeyCodeCombination(KeyCode.P, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN);
  public final static KeyCodeCombination REPEAT_MODE_KEYCODE = new KeyCodeCombination(KeyCode.R, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN);

  public final static KeyCombination fullScreenKeyCombinationStart = new KeyCodeCombination(KeyCode.F5);
  public final static KeyCombination fullScreenKeyCombinationEnd = new KeyCodeCombination(KeyCode.ESCAPE);


  /**
   * create the main menu bar and install all shortcuts/handlers
   *
   * @param fileTableView  link for calling of most of the methods, i.e. Filetable.open etc.
   * @param mediaContentView link to mediaContentView for player menu items
   * @param metaInfoView link to Exif/Metadata View for navigation/show hide etc
   * @param primaryStage link to main window because getScene().getWindow() is null during startup, because Menu is added after creation only
   */
  public MainMenuBar(FileTableView fileTableView, MediaContentView mediaContentView, MetaInfoView metaInfoView, Stage primaryStage) {
    super();
    this.fileTableView = fileTableView;
    this.mediaContentView = mediaContentView;
    this.metaInfoView = metaInfoView;

    createFileMenu();
    createEditMenu(primaryStage);
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

    final MenuItem openItem = new MenuItem(KissPhoto.language.getString("openMenu"));
    openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    openItem.setOnAction(event -> {
      event.consume();
      fileTableView.chooseFileOrFolder();
    });
    fileMenu.getItems().add(openItem);

    MenuItem saveItem = new MenuItem(KissPhoto.language.getString("saveMenu"));
    saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    saveItem.setOnAction(event -> {
      event.consume();
      fileTableView.saveFolder();
    });
    fileMenu.getItems().add(saveItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    MenuItem exportCSVItem = new MenuItem(KissPhoto.language.getString("exportCSVMenu"));
    exportCSVItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
    exportCSVItem.setOnAction(event -> {
      event.consume();
      fileTableView.chooseFilenameAndExportToCSV();
    });
    fileMenu.getItems().add(exportCSVItem);

    MenuItem exportFolderCSVItem = new MenuItem(KissPhoto.language.getString("writeFolderStructureCSVMenu"));
    exportFolderCSVItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    exportFolderCSVItem.setOnAction(event -> {
      event.consume();
      WriteFolderStructureCSVDialog writeFolderStructureCSVDialog = new WriteFolderStructureCSVDialog(fileTableView.getPrimaryStage(), fileTableView.getMediaFileList(), fileTableView.getStatusBar());
      writeFolderStructureCSVDialog.showModal();
    });
    fileMenu.getItems().add(exportFolderCSVItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    MenuItem exitItem = new MenuItem(KissPhoto.language.getString("exitMenu"));
    exitItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
    exitItem.setOnAction(event -> {
      event.consume();
      //note: Platform.exit() and primaryStage.close() would not fire the "onClose"-Event-Handler!
      fileTableView.getPrimaryStage().fireEvent(new WindowEvent(fileTableView.getPrimaryStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
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
  private void createEditMenu(Stage primaryStage) {
    final MenuItem findReplaceItem = new MenuItem(KissPhoto.language.getString("findReplaceMenu"));
    findReplaceItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
    findReplaceItem.setOnAction(event -> {
      event.consume();
      fileTableView.findAndReplace();
    });
    editMenu.getItems().add(findReplaceItem);

    final MenuItem findNextItem = new MenuItem(KissPhoto.language.getString("find.next"));
    findNextItem.setAccelerator(new KeyCodeCombination(KeyCode.F3));
    findNextItem.disableProperty().bind(fileTableView.getFindReplaceDialogShowingProperty(primaryStage).not());
    findNextItem.setOnAction(event -> {
      event.consume();
      fileTableView.findNext();
    });
    editMenu.getItems().add(findNextItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem renameItem = new MenuItem(KissPhoto.language.getString(RENAME_MENU));
    renameItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
    renameItem.setOnAction(event -> {
      event.consume();
      fileTableView.rename();
    });
    editMenu.getItems().add(renameItem);

    final MenuItem autoFillDownItem = new MenuItem(KissPhoto.language.getString("autofill.copy.down"));
    autoFillDownItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
    autoFillDownItem.setOnAction(event -> {
      event.consume();
      fileTableView.copyDescriptionDown();
    });
    editMenu.getItems().add(autoFillDownItem);

    final MenuItem externalMainEditorItem = new MenuItem(KissPhoto.language.getString("external.editor.1"));
    externalMainEditorItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.CONTROL_DOWN));
    externalMainEditorItem.setOnAction(event -> {
      event.consume();
      fileTableView.executeExternalEditorForSelection(true);
    });
    editMenu.getItems().add(externalMainEditorItem);

    final MenuItem external2ndEditorItem = new MenuItem(KissPhoto.language.getString("external.editor.2"));
    external2ndEditorItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.SHIFT_DOWN));
    external2ndEditorItem.setOnAction(event -> {
      event.consume();
      fileTableView.executeExternalEditorForSelection(false);
    });
    editMenu.getItems().add(external2ndEditorItem);

    final MenuItem cleanPrefixItem = new MenuItem(KissPhoto.language.getString(CLEAN_PREFIXES));
    cleanPrefixItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    cleanPrefixItem.setOnAction(event -> {
      event.consume();
      fileTableView.cleanPrefix();
    });
    editMenu.getItems().add(cleanPrefixItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem timeStampItem = new MenuItem(KissPhoto.language.getString("timeStampMenu"));
    timeStampItem.setDisable(true); //not yet implemented
    editMenu.getItems().add(timeStampItem);

    final MenuItem copyTimeStampExtItem = new MenuItem(KissPhoto.language.getString("copy.file.dates.by.extension"));
    copyTimeStampExtItem.setOnAction(actionEvent -> fileTableView.copyFileDatesByExtension());
    editMenu.getItems().add(copyTimeStampExtItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem deleteItem = new MenuItem(KissPhoto.language.getString("deleteMenu"));
    deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCodeCombination.CONTROL_DOWN));
    deleteItem.setOnAction(event -> {
      event.consume();
      fileTableView.deleteSelectedFiles(false);
    });
    editMenu.getItems().add(deleteItem);

    final MenuItem unDeleteLastItem = new MenuItem(KissPhoto.language.getString("undelete.last.file"));
    unDeleteLastItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
    unDeleteLastItem.setOnAction(event -> {
      event.consume();
      fileTableView.undeleteLastDeletedFile();
    });
    unDeleteLastItem.setDisable(true);      //non-active until first deletion
    editMenu.getItems().add(unDeleteLastItem);

    final MenuItem unDeleteItem = new MenuItem(KissPhoto.language.getString("undeleteMenu"));
    unDeleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCodeCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
    unDeleteItem.setOnAction(event -> {
      event.consume();
      fileTableView.unDeleteWithDialog();
    });
    unDeleteItem.setDisable(true);      //non-active until first deletion
    editMenu.getItems().add(unDeleteItem);
    fileTableView.registerGrayingDeleteMenuItems(unDeleteLastItem, unDeleteItem);     //pass a link to the father window so there enabling/disabling can be controlled

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem cutItem = new MenuItem(KissPhoto.language.getString("cutMenu"));
    cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
    cutItem.setOnAction(event -> {
      event.consume();
      fileTableView.cutToClipboard();
    });
    editMenu.getItems().add(cutItem);

    final MenuItem pasteItem = new MenuItem(KissPhoto.language.getString("pasteMenu"));
    pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
    pasteItem.setOnAction(event -> {
      event.consume();
      fileTableView.pasteFromClipboard();
    });
    pasteItem.setDisable(true);      //non-active until first deletion
    editMenu.getItems().add(pasteItem);
    fileTableView.setPasteMenuItem(pasteItem);     //pass a link to the father window so there enabling/disabling can be controlled

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem autoNumberItem = new MenuItem(KissPhoto.language.getString(RENUMBER_STANDARD_MENU));
    autoNumberItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    autoNumberItem.setOnAction(event -> {
      event.consume();
      fileTableView.renumberSelectionStandard();
    });
    editMenu.getItems().add(autoNumberItem);

    final MenuItem reNumberLocalItem = new MenuItem(KissPhoto.language.getString("renumber.localMenu"));
    reNumberLocalItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
    reNumberLocalItem.setOnAction(event -> {
      event.consume();
      fileTableView.renumberWithDialog();
    });
    editMenu.getItems().add(reNumberLocalItem);

    final MenuItem cleanCountersItem = new MenuItem(KissPhoto.language.getString(CLEAN_COUNTERS));
    cleanCountersItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    cleanCountersItem.setOnAction(event -> {
      event.consume();
      fileTableView.cleanCounter();
    });
    editMenu.getItems().add(cleanCountersItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem moveUpItem = new MenuItem(KissPhoto.language.getString("move.upMenu"));
    //shift-alt ok: compatible with windows: alt+??=menu, shift-alt-arrow = move, compatible with ubuntu: shift-alt used for changing windows
    //here only for displaying the shortcut. It must be set additionally directly in FileTableView constructor to override standard use of Shift-Ctrl-Up/Down=extend selection
    moveUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    moveUpItem.setOnAction(event -> {
      event.consume();
      fileTableView.moveSelectedFilesUp();

    });
    editMenu.getItems().add(moveUpItem);

    final MenuItem moveDnItem = new MenuItem(KissPhoto.language.getString("move.downMenu"));
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
    //--------------- FileTableViews' View
    final MenuItem resetSortingItem = new MenuItem(KissPhoto.language.getString("reset.sortingMenu"));
    resetSortingItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    resetSortingItem.setOnAction(event -> {
      event.consume();
      fileTableView.resetSortOrder();
    });
    viewMenu.getItems().add(resetSortingItem);

    final MenuItem resetColWidthItem = new MenuItem(KissPhoto.language.getString("reset.column.widths"));
    resetColWidthItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    resetColWidthItem.setOnAction(event -> {
      event.consume();
      fileTableView.setDefaultColumnWidths();
    });
    viewMenu.getItems().add(resetColWidthItem);

    //----------------- MetaInfoView's View
    viewMenu.getItems().add(new SeparatorMenuItem());

    final CheckMenuItem showMetaInfoItem = new CheckMenuItem(KissPhoto.language.getString("show.meta.data"));
    showMetaInfoItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
    showMetaInfoItem.selectedProperty().bindBidirectional(metaInfoView.visibleProperty());
    showMetaInfoItem.setOnAction(event -> {
      event.consume();
      //because the changed selection is bound to visibility it is not necessary to set it here again
      if (showMetaInfoItem.isSelected()) { //i.e. if metaInfoView is visible
        metaInfoView.guaranteeMinimumHeight(); //after manual showing guarantee a minimum height to ensure it really became visible
      }
    });
    viewMenu.getItems().add(showMetaInfoItem);

    final MenuItem showGPSLocationItem = new MenuItem(KissPhoto.language.getString("show.gps.location.in.google.maps"));
    showGPSLocationItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
    showGPSLocationItem.setOnAction(event -> {
      event.consume();
      metaInfoView.showGPSPositionInGoogleMaps();
    });
    viewMenu.getItems().add(showGPSLocationItem);


    //----------------- MediaView's View
    viewMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem fullScreenItem = new MenuItem();
    mediaContentView.setFullScreenMenuItemText(fullScreenItem); //initializes including accelerator key

    fullScreenItem.setOnAction(actionEvent -> {
      actionEvent.consume();
      mediaContentView.toggleFullScreenAndNormal();
    });
    mediaContentView.getIsFullScreenActiveProperty().addListener((observable, oldValue, newValue) -> mediaContentView.setFullScreenMenuItemText(fullScreenItem));

    MenuItem showOnNextScreenItem = new MenuItem(KissPhoto.language.getString(MediaContentView.SHOW_ON_NEXT_SCREEN_FULLSCREEN));
    showOnNextScreenItem.setAccelerator((new KeyCodeCombination(KeyCode.TAB))); //TAB, previous shift-Tab is not shown in menu
    showOnNextScreenItem.setOnAction(actionEvent -> mediaContentView.showFullScreenStageOnNextScreen(true));
    showOnNextScreenItem.disableProperty().bind(mediaContentView.getIsFullScreenActiveProperty().not());  //only enabled if fullScreen-Mode is active

    viewMenu.getItems().addAll(fullScreenItem, showOnNextScreenItem);

    //final MenuItem slideShowItem = new MenuItem(language.getString("slide.showMenu"));
    //slideShowItem.setDisable(true); //not yet implemented
    //viewMenu.getItems().add(slideShowItem);


    viewMenu.setOnShowing(event -> {
      //enable the menu item only if gps data is available
      showGPSLocationItem.setDisable(!metaInfoView.isValidGpsAvailable());
    });

    viewMenu.setOnHiding(event -> {
      //while hidden enable the menu item to enable the accelerator-key
      showGPSLocationItem.setDisable(false);
    });

    getMenus().add(viewMenu);
  }

  private void createImageMenu() {
    MenuItem exifOrientationItem = new MenuItem(KissPhoto.language.getString("rotate.and.flip.jpeg.according.exif.lossless"));
    exifOrientationItem.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN));
    exifOrientationItem.setOnAction(event -> fileTableView.setOrientationAccordingExif());
    imageMenu.getItems().addAll(exifOrientationItem, new SeparatorMenuItem());

    MenuItem rotateRightItem = new MenuItem(KissPhoto.language.getString("rotate.jpeg.90.right.lossless"));
    rotateRightItem.setAccelerator(new KeyCodeCombination(KeyCode.R));
    rotateRightItem.setOnAction(event -> fileTableView.rotateSelectedFiles(MediaFile.RotateOperation.ROTATE90));
    imageMenu.getItems().add(rotateRightItem);

    MenuItem rotateLeftItem = new MenuItem(KissPhoto.language.getString("rotate.jpeg.90.left.lossless"));
    rotateLeftItem.setAccelerator(new KeyCodeCombination(KeyCode.L));
    rotateLeftItem.setOnAction(event -> fileTableView.rotateSelectedFiles(MediaFile.RotateOperation.ROTATE270));
    imageMenu.getItems().add(rotateLeftItem);

    MenuItem rotate180Item = new MenuItem(KissPhoto.language.getString("rotate.jpeg.180.lossless"));
    rotate180Item.setAccelerator(new KeyCodeCombination(KeyCode.T));
    rotate180Item.setOnAction(event -> fileTableView.rotateSelectedFiles(MediaFile.RotateOperation.ROTATE180));
    imageMenu.getItems().addAll(rotate180Item, new SeparatorMenuItem());

    MenuItem flipHItem = new MenuItem(KissPhoto.language.getString("flip.jpeg.horizontally.lossless"));
    flipHItem.setAccelerator(new KeyCodeCombination(KeyCode.H));
    flipHItem.setOnAction(event -> fileTableView.flipSelectedFiles(true));
    imageMenu.getItems().add(flipHItem);

    MenuItem flipVItem = new MenuItem(KissPhoto.language.getString("flip.jpeg.vertically.lossless"));
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

    playPauseItem = new MenuItem(KissPhoto.language.getString("play"));  //Pause/Play --> two states reflected by setting text
    playPauseItem.setAccelerator(MainMenuBar.PLAY_PAUSE_KEYCODE);
    playPauseItem.setOnAction(actionEvent -> playerControls.togglePlayPause());
    playerControls.bindPlayPauseMenuItem(playPauseItem); //keep state of playControls and menuItem synced

    rewindItem = new MenuItem(KissPhoto.language.getString("rewind"));  //Pause/Play --> two states reflected by setting text
    rewindItem.setAccelerator(MainMenuBar.REWIND_KEYCODE);
    rewindItem.setOnAction(actionEvent -> playerControls.rewind());

    playListModeItem = new CheckMenuItem(KissPhoto.language.getString("playlist.mode"));
    playListModeItem.setAccelerator(MainMenuBar.PLAYLIST_MODE_KEYCODE);
    //playListModeItem.setOnAction(actionEvent -> playerControls.setPlayListMode(!playerControls.isPlayListMode())); //toggle --> not necessary, because bidirectional binding
    playerControls.bindBidirectionalPlaylistModeMenuItem(playListModeItem); //keep state of playControls and menuItem synced

    repeatModeItem = new CheckMenuItem(KissPhoto.language.getString("repeat.mode"));
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
    MenuItem languageItem = new MenuItem(KissPhoto.language.getString("language.settingsMenu"));
    languageItem.setOnAction(event -> {
      event.consume();
      if (languageDialog == null) languageDialog = new LanguageDialog(fileTableView.getPrimaryStage());
      languageDialog.showModal();
    });
    extrasMenu.getItems().add(languageItem);

    MenuItem externalEditorsItem = new MenuItem(KissPhoto.language.getString("specify.external.editors"));
    externalEditorsItem.setOnAction(event -> {
      event.consume();
      if (externalEditorsDialog == null) externalEditorsDialog = new ExternalEditorsDialog(fileTableView.getPrimaryStage());
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
    MenuItem aboutItem = new MenuItem(KissPhoto.language.getString("aboutMenu"));
    aboutItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
    aboutItem.setOnAction(event -> {
      event.consume();
      if (aboutDialog == null) aboutDialog = new AboutDialog(fileTableView.getPrimaryStage(), KissPhoto.KISS_PHOTO_VERSION);
      aboutDialog.showModal();
    });
    helpMenu.getItems().add(aboutItem);
    getMenus().add(helpMenu);
  }

}
