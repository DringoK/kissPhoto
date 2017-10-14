package de.kissphoto.view;


import de.kissphoto.helper.GlobalSettings;
import de.kissphoto.helper.I18Support;
import de.kissphoto.view.dialogs.AboutDialog;
import de.kissphoto.view.dialogs.ExternalEditorsDialog;
import de.kissphoto.view.dialogs.LanguageDialog;
import de.kissphoto.view.dialogs.WriteFolderStructureCSVDialog;
import de.kissphoto.view.mediaViewers.helper.PlayerViewer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The main menu bar is defined here
 * <p/>
 *
 * @author: Ingo
 * @date: 2012-09-09
 * @modified: 2014-04-29 added about menu and no longer used the MenuItemBuilder (which is deprecated now)
 * @modified: 2014-05-02 (I18Support, Reopen added)
 * @modified: 2014-16-16 support for full screen mode added to view menu
 * @modified: 2015-10-04 moving changed to ctr-Cursor up/down: Shift-Alt-Cursor up/down does not work under Windows 10 (menu is activated instead)
 * @modified: 2017-10-13 added AutoFill(Down) Menu-Item to edit menu + Default Column Widths to View menu
 */
public class MainMenuBar extends MenuBar {
  private static ResourceBundle language = I18Support.languageBundle;

  protected FileTableView fileTableView; //link to fileTableView to call methods via menu/shortcut
  protected MediaContentView mediaContentView; //link to mediaContentView for full screen etc
  private Stage primaryStage; //link to embedding window
  private String versionString; //link to versionString of Main Window for About Dialog

  private GlobalSettings globalSettings; //link to global settings for LanguageDialog

  /**
   * create the main menu bar and install all shortcuts/handlers
   *
   * @param primaryStage   link to the main window
   * @param fileTableView  link for calling of most of the methods, i.e. Filetable.open etc.
   * @param versionString  link for the about Dialog
   * @param globalSettings link needed in LanguageDialog
   */
  public MainMenuBar(Stage primaryStage, FileTableView fileTableView, MediaContentView mediaContentView, String versionString, GlobalSettings globalSettings) {
    super();
    this.primaryStage = primaryStage;
    this.fileTableView = fileTableView;
    this.mediaContentView = mediaContentView;
    this.versionString = versionString;
    this.globalSettings = globalSettings;

    this.prefWidthProperty().bind(primaryStage.widthProperty());

    createFileMenu();
    createEditMenu();
    createViewMenu();
    createPlayerMenu();
    createExtrasMenu();
    createHelpMenu();
  }

  /**
   * ########################### File Menu Items and actions ###########################
   */
  private void createFileMenu() {
    Menu fileMenu = new Menu(language.getString("fileMenu"));

    final MenuItem openItem = new MenuItem(language.getString("openMenu"));
    openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    openItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.chooseFileOrFolder();
      }
    });
    fileMenu.getItems().add(openItem);

    final MenuItem reOpenItem = new MenuItem(language.getString("reopenMenu"));
    reOpenItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
    reOpenItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.reOpenFolder();
      }
    });
    reOpenItem.setDisable(true); //disabled until the first loading of a directory
    fileTableView.setReOpenMenuItem(reOpenItem); //for controlling the disabled state
    fileMenu.getItems().add(reOpenItem);

    MenuItem saveItem = new MenuItem(language.getString("saveMenu"));
    saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    saveItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.saveFolder();
      }
    });
    fileMenu.getItems().add(saveItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    MenuItem exportCSVItem = new MenuItem(language.getString("exportCSVMenu"));
    exportCSVItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
    exportCSVItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.chooseFilenameAndExportToCSV();
      }
    });
    fileMenu.getItems().add(exportCSVItem);

    MenuItem exportFolderCSVItem = new MenuItem(language.getString("writeFolderStructureCSVMenu"));
    exportFolderCSVItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    exportFolderCSVItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        WriteFolderStructureCSVDialog writeFolderStructureCSVDialog = new WriteFolderStructureCSVDialog(primaryStage, fileTableView.getMediaFileList(), fileTableView.getStatusBar());
        writeFolderStructureCSVDialog.showModal();
      }
    });
    fileMenu.getItems().add(exportFolderCSVItem);

    fileMenu.getItems().add(new SeparatorMenuItem());

    MenuItem exitItem = new MenuItem(language.getString("exitMenu"));
    exitItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
    exitItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        //note: Platform.exit() and primaryStage.close() would not fire the "onClose"-Event-Handler!
        primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
      }
    });
    fileMenu.getItems().add(exitItem);


    getMenus().add(fileMenu);
  }

  /**
   * ########################### Edit Menu Items ###########################
   */
  private void createEditMenu() {
    Menu editMenu = new Menu(language.getString("editMenu"));

    final MenuItem findReplaceItem = new MenuItem(language.getString("findReplaceMenu"));
    findReplaceItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
    findReplaceItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.findAndReplace();
      }
    });
    editMenu.getItems().add(findReplaceItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem renameItem = new MenuItem(language.getString("renameMenu"));
    renameItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
    renameItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.rename();
      }
    });
    editMenu.getItems().add(renameItem);

    final MenuItem autoFillDownItem = new MenuItem(language.getString("autofill.copy.down"));
    autoFillDownItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
    autoFillDownItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.copyDescriptionDown();
      }
    });
    editMenu.getItems().add(autoFillDownItem);

    final MenuItem externalMainEditorItem = new MenuItem(language.getString("external.editor.1"));
    externalMainEditorItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.CONTROL_DOWN));
    externalMainEditorItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.executeExternalEditorForSelection(true);
      }
    });
    editMenu.getItems().add(externalMainEditorItem);

    final MenuItem external2ndEditorItem = new MenuItem(language.getString("external.editor.2"));
    external2ndEditorItem.setAccelerator(new KeyCodeCombination(KeyCode.F2, KeyCombination.SHIFT_DOWN));
    external2ndEditorItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.executeExternalEditorForSelection(false);
      }
    });
    editMenu.getItems().add(external2ndEditorItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem timeStampItem = new MenuItem(language.getString("timeStampMenu"));
    timeStampItem.setDisable(true); //not yet implemented
    editMenu.getItems().add(timeStampItem);

    final MenuItem copyTimeStampExtItem = new MenuItem(language.getString("copy.file.dates.by.extension"));
    copyTimeStampExtItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        fileTableView.copyFileDatesByExtension();
      }
    });
    editMenu.getItems().add(copyTimeStampExtItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem deleteItem = new MenuItem(language.getString("deleteMenu"));
    deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
    deleteItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.deleteSelectedFiles(false);
      }
    });
    editMenu.getItems().add(deleteItem);

    final MenuItem unDeleteItem = new MenuItem(language.getString("undeleteMenu"));
    unDeleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN));
    unDeleteItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.unDeleteWithDialog();
      }
    });
    unDeleteItem.setDisable(true);      //non-active until first deletion
    editMenu.getItems().add(unDeleteItem);
    fileTableView.setUnDeleteMenuItem(unDeleteItem);     //pass a link to the father window so there enabling/disabling can be controlled

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem cutItem = new MenuItem(language.getString("cutMenu"));
    cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
    cutItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.cutToClipboard();
      }
    });
    editMenu.getItems().add(cutItem);

    final MenuItem pasteItem = new MenuItem(language.getString("pasteMenu"));
    pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
    pasteItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.pasteFromClipboard();
      }
    });
    pasteItem.setDisable(true);      //non-active until first deletion
    editMenu.getItems().add(pasteItem);
    fileTableView.setPasteMenuItem(pasteItem);     //pass a link to the father window so there enabling/disabling can be controlled

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem autoNumberItem = new MenuItem(language.getString("renumber.standardMenu"));
    autoNumberItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    autoNumberItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.renumberSelectionRelativeToIndices();
      }
    });
    editMenu.getItems().add(autoNumberItem);

    final MenuItem reNumberGlobalItem = new MenuItem(language.getString("renumber.globalMenu"));
    reNumberGlobalItem.setAccelerator((new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)));
    reNumberGlobalItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.renumberWithDialog(true);
      }
    });
    editMenu.getItems().add(reNumberGlobalItem);

    final MenuItem reNumberLocalItem = new MenuItem(language.getString("renumber.localMenu"));
    reNumberLocalItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
    reNumberLocalItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.renumberWithDialog(false);
      }
    });
    editMenu.getItems().add(reNumberLocalItem);

    editMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem moveUpItem = new MenuItem(language.getString("move.upMenu"));
    //shift-alt ok: compatible with windows: alt+??=menu, shift-alt-arrow = move, compatible with ubuntu: shift-alt used for changing windows
    //here only for displaying the shortcut. It must be set additionally directly in FileTableView constructor to override standard use of Shift-Ctrl-Up/Down=extend selection
    moveUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    moveUpItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.moveSelectedFilesUp();
      }
    });
    editMenu.getItems().add(moveUpItem);

    final MenuItem moveDnItem = new MenuItem(language.getString("move.downMenu"));
    moveDnItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
    moveDnItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.moveSelectedFilesDown();
      }
    });
    editMenu.getItems().add(moveDnItem);

    getMenus().add(editMenu);
  }

  /**
   * ########################### View Menu Items ###########################
   */
  private void createViewMenu() {
    Menu viewMenu = new Menu(language.getString("viewMenu"));

    final MenuItem resetSortingItem = new MenuItem(language.getString("reset.sortingMenu"));
    resetSortingItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    resetSortingItem.setOnAction(new EventHandler<ActionEvent>() {

      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.resetSortOrder();
      }
    });
    viewMenu.getItems().add(resetSortingItem);

    final MenuItem resetColWidthItem = new MenuItem(language.getString("reset.column.widths"));
    resetColWidthItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    resetColWidthItem.setOnAction(new EventHandler<ActionEvent>() {

      public void handle(ActionEvent event) {
        event.consume();
        fileTableView.setDefaultColumnWidths();
      }
    });
    viewMenu.getItems().add(resetColWidthItem);

    viewMenu.getItems().add(new SeparatorMenuItem());

    final MenuItem fullScreenItem = new MenuItem(language.getString("full.screen"));
    fullScreenItem.setAccelerator((new KeyCodeCombination(KeyCode.F5)));
    fullScreenItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        mediaContentView.toggleFullScreenAndNormal();
        actionEvent.consume();
      }
    });
    mediaContentView.addToFullScreenItems(fullScreenItem);

    MenuItem showOnNextScreenItem = new MenuItem(language.getString(MediaContentView.SHOW_ON_NEXT_SCREEN_FULLSCREEN));
    showOnNextScreenItem.setAccelerator((new KeyCodeCombination(KeyCode.TAB))); //TAB, previous shift-Tab is not shown in menu
    showOnNextScreenItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        mediaContentView.showFullScreenOnNextScreen(true);
      }
    });
    showOnNextScreenItem.setDisable(true); //enable only in Full screen mode
    mediaContentView.addToShowOnNextScreenItems(showOnNextScreenItem);

    viewMenu.getItems().addAll(fullScreenItem, showOnNextScreenItem);

    final MenuItem slideShowItem = new MenuItem(language.getString("slide.showMenu"));
    slideShowItem.setDisable(true); //not yet implemented
    viewMenu.getItems().add(slideShowItem);
    getMenus().add(viewMenu);
  }

  private void createPlayerMenu() {
    Menu playerMenu = new Menu(language.getString("player"));
    CheckMenuItem autoPlayItem = new CheckMenuItem(language.getString(PlayerViewer.AUTO_PLAY));
    autoPlayItem.setSelected(true);
    autoPlayItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
    autoPlayItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        mediaContentView.getMovieViewer().toggleAutoPlay(true);
      }
    });

    MenuItem playPauseItem = new MenuItem(language.getString(PlayerViewer.PLAY));  //P = Pause/Play --> the text is exchanged from MovieViewer
    playPauseItem.setAccelerator(new KeyCodeCombination(KeyCode.P));
    playPauseItem.setDisable(true);
    playPauseItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        mediaContentView.getMovieViewer().togglePlayPause();
      }
    });

    MenuItem stopItem = new MenuItem(language.getString(PlayerViewer.STOP)); //S=Stop/Rewind
    stopItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_ANY));
    stopItem.setDisable(true);
    stopItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        mediaContentView.getMovieViewer().stop();
      }
    });
    playerMenu.getItems().addAll(autoPlayItem, playPauseItem, stopItem, new SeparatorMenuItem());
    mediaContentView.getMovieViewer().registerMainMenuItems(autoPlayItem, playPauseItem, stopItem);

    getMenus().add(playerMenu);
  }

  /**
   * ########################### Extras Menu Items ###########################
   */
  LanguageDialog languageDialog;
  ExternalEditorsDialog externalEditorsDialog;

  private void createExtrasMenu() {
    Menu extrasMenu = new Menu(language.getString("extrasMenu"));

    MenuItem languageItem = new MenuItem(language.getString("language.settingsMenu"));
    languageItem.setOnAction(new EventHandler<ActionEvent>() {

      public void handle(ActionEvent event) {
        event.consume();
        if (languageDialog == null) languageDialog = new LanguageDialog(primaryStage, globalSettings);
        languageDialog.showModal();
      }
    });
    extrasMenu.getItems().add(languageItem);

    MenuItem externalEditorsItem = new MenuItem(language.getString("specify.external.editors"));
    externalEditorsItem.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        event.consume();
        if (externalEditorsDialog == null) externalEditorsDialog = new ExternalEditorsDialog(primaryStage);
        externalEditorsDialog.showModal(globalSettings);
      }
    });
    extrasMenu.getItems().add(externalEditorsItem);
    getMenus().add(extrasMenu);

  }

  /**
   * ########################### Help Menu Items ###########################
   */
  AboutDialog aboutDialog;

  private void createHelpMenu() {
    Menu helpMenu = new Menu(language.getString("helpMenu"));
    MenuItem aboutItem = new MenuItem(language.getString("aboutMenu"));
    aboutItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
    aboutItem.setOnAction(new EventHandler<ActionEvent>() {

      public void handle(ActionEvent event) {
        event.consume();
        if (aboutDialog == null) aboutDialog = new AboutDialog(primaryStage, versionString);
        aboutDialog.showModal();
      }
    });
    helpMenu.getItems().add(aboutItem);
    getMenus().add(helpMenu);
  }

}
