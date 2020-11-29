package de.kissphoto;

import de.kissphoto.helper.GlobalSettings;
import de.kissphoto.helper.I18Support;
import de.kissphoto.view.FileTableView;
import de.kissphoto.view.MainMenuBar;
import de.kissphoto.view.MediaContentView;
import de.kissphoto.view.StatusBar;
import de.kissphoto.view.dialogs.ExternalEditorsDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.util.ResourceBundle;


/**
 * Keep it simple! File renaming and Photo/Media File Management Main Class<br>
 * ========================================================<p>
 * The main KISS ideas of the application are
 * <ul>
 * <li>Rename files like in a wordprocesser's table: move around with the cursor, search and replace + mass rename, renumbering and sorting</li>
 * <li>photo management and photo show without installation or database</li>
 * <li>self containing: all information is in the picture files, filenames or directory names</li>
 * <li>auto numbering: file order can be changed, file numbering will follow</li>
 * <li>file date maintaining</li>
 * <li>mass renaming for filenames, EXIF-Info and time-stamps</li>
 * <li>runnable on all PC platforms which support JavaFX</li>
 * <li>The viewer treats movie clips like moving photos (like in Harry Potter's newspapers ;-)</li>
 * <li>optional seamless vlc support: play virtually all playable files if vlc is installed on the system additionally</li>
 * </ul>
 *
 * @author Ingo Kreuz<br>
 * @version see KISS_PHOTO_VERSION constant below
 * @since 2014-04-29
 * @version 2020-11-19 vlcj integrated, GlobalSettings made global(static), Player Options introduced
 * @version 2019-11-01 move up/down key handling improved, scrolling in FileTable improved, preview in UnDeleteDialog repaired, Cache speed improved (e.g. Background Loading Cancelling), reload File History repaired
 * @version 2019-07-07 Cache problems fixed
 * @version 2019-06-23 release candidate: fixes in Cache Algo, fixed issues with "second screen"/fullscreen, Strg-j menu internationalized, jpeg (with e) supported
 * @version 2018-11-17 rotation of images is now supported, improved inCell-editing (caretPosition, Tab-Support)
 * @version 2017-10-02 main window is moved into visible part of screen after startup (e.g. if resolution changed or 2nd screen has been disabled)
 * @version 2014-07-05 loading initialFileOrFolder after stage.show() to show messages during slow network access
 * <p>
 * Bugs:
 * ======================
 * planned features:
 * ======================
 * todo Autoplay ersetzen in Menü und Context-Menü des Players durch "Playlist Mode" und "Repeat Mode" und syncen mit Icons in PlayerControl
 * todo Fortschrittsbalken auch bei Drehen (weil Exif gelesen werden muss, dauert das manchmal länger)
 * todo Player in MediaPane hochheben und mit Burger-Menü (=rechtsklick) ergänzen (Burger immer sichtbar, auch für Photo, nicht ausblenden solange Maus darüber, nicht ausblenden bei Musik)
 * todo   audio abspielen (m4a, mp3, wav): Player nicht ausblenden!
 * todo Meta-Daten anzeigen. Fenster ausblendbar
 * todo Multi-Edit-Dialog: Hilfe nur auf Knopfdruck ("Hilfe zu %p %d..."), Aufruf Nummerieren-Dialog rein, EXIF-Kommentar rein (der bekommt %k, damit Übernahme möglich)
 * todo   Umbenennen-Dialog renovieren (Kontextmenü statt Buttons) und testen ob alle Ersetzungen auch funktionieren
 * todo   Umbenennen-Dialog. Default-Feld = Description, Cursor ans Ende stellen
 * todo   Umbenennen-Dialog sollte Link zu Nummerieren-Dialog haben und man sollte die Nummer auch löschen können
 * todo Zeit-Dialog: Übernehmen von Ändern, Exif, Digitalisierung Stunden/Minuten dazu/abziehen(anwenden immer auf alles andere)
 * todo Auto/One Click Grundformatierung: "Drehen", "Nummerierien", "Space"(, sonst nix)", falls die Bilder noch DSCN heißen, sonst Warnung
 * todo EXIF schreiben evtl über http://sourceforge.net/projects/image-tagger/
 * nice to haves:
 * ===============
 * todo doch nochmal schauen, ob die Updates vom FileWatcher nicht verwendet werden können. Siehe JavaFX Task: A Task Which Returns Partial Results
 * todo Nice to have: Undo-History
 */
public class KissPhoto extends Application {
  public static final String KISS_PHOTO_VERSION = "0.20.11 work in progress"; // <------------------------------------------------------------------------------
  public static final String KISS_PHOTO = "kissPhoto ";
  public static ResourceBundle language = null;

  //set in void main(args) = in static context, so the need to be static
  private static String initialFileOrFolder;
  public static boolean optionNoVLC = false; //if -noVLC is provided then prevent from searching for and using of vlc
  private static boolean optionHelp = false; //-Help will println a short helptext

  private MainMenuBar mainMenuBar;
  private FileTableView fileTableView;
  private MediaContentView mediaContentView;
  private StatusBar statusBar;
  protected Stage primaryStage;
  protected Scene scene;

  //all classes can access the settings file
  public static GlobalSettings globalSettings = new GlobalSettings();

  SplitPane mainSplitPane = new SplitPane();
  SplitPane detailsArea = new SplitPane();

  //------- Default Window size/position if no valid settings file found
  private static final double default_X = 0;
  private static final double default_Y = 0;
  private static final double default_width = 1000;
  private static final double default_height = 800;
  private static final double MAIN_SPLIT_PANE_DEFAULT_DIVIDER_POS = 0.5;
  private static final double DETAILS_AREA_DEFAULT_DIVIDER_POS = 0.99;

  //------------- IDs for GlobalSettings-File
  private static final String STAGE_X = "StageX";
  private static final String STAGE_Y = "StageY";
  private static final String STAGE_WIDTH = "StageWidth";
  private static final String STAGE_HEIGHT = "StageHeight";

  private static final String MAIN_SPLIT_PANE_DIVIDER_POSITION = "mainSplitPane_DividerPosition";
  private static final String DETAILS_AREA_DIVIDER_POSITION = "detailsArea_DividerPosition";

  /**
   * Entry point
   * @param args the command line arguments handed by command shell:<br>
   *             [filename] (file or folder): will be opened<br>
   *             -noVLC: prevent the search for and use of vlc (for testing or to save resources)
   *             -help: a short help text about parameters
   */
  public static void main(String[] args) {
    if (args.length >= 1) {
      //if first parameter would contain "-" then it was an option and no fileOrFoler is provided
      //first argument is fileOrFolder to open if it does not contain a "-"
      if (!args[0].contains("-")) {
        initialFileOrFolder = args[0]; //first parameter is treated as file or folder to open
      }

      //search for options
      for (int i=0;i<args.length;i++){
        if (args[i].equalsIgnoreCase("-noVLC")) optionNoVLC=true;
        else if (args[i].equalsIgnoreCase("-help")) optionHelp=true;
      }

    } else {
      initialFileOrFolder = "";
    }

    Application.launch(args);
  }

  @Override
  public void start(Stage stage) {
    globalSettings.load();

    try {
      I18Support.setLanguage(globalSettings.getProperty(I18Support.LANGUAGE));
    } catch (Exception e) {
      //keep default language if property or settings file could not be found
    }
    language = I18Support.languageBundle;      //not before now assign language: after setLanguage() has been executed

    if (optionHelp) System.out.println(language.getString("program.parameter.help.text"));

    primaryStage = stage;  //make this parameter value available in event Handler (see setOnClosedEvent)
    primaryStage.setTitle(KISS_PHOTO + KISS_PHOTO_VERSION);
    Group root = new Group();
    scene = new Scene(root, default_width, default_height);
    primaryStage.setScene(scene);

    stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/KissPhotoIcon.jpg")));

    //Create the View-Areas
    statusBar = new StatusBar();
    statusBar.showMessage("");
    mediaContentView = new MediaContentView(primaryStage); //Area for showing Media

    fileTableView = new FileTableView(primaryStage, mediaContentView, statusBar); //File table and directory
    statusBar.connectUndeleteDialog(fileTableView);
    mediaContentView.setFileTableView(fileTableView);

    mainMenuBar = new MainMenuBar(primaryStage, fileTableView, mediaContentView, KISS_PHOTO_VERSION);
    mainMenuBar.addRecentlyMenu(fileTableView.getFileHistory().getRecentlyFilesMenu());
    // Left and right split pane
    mainSplitPane.prefWidthProperty().bind(scene.widthProperty());
    mainSplitPane.prefHeightProperty().bind(scene.heightProperty());
    mainSplitPane.setDividerPosition(0, MAIN_SPLIT_PANE_DEFAULT_DIVIDER_POS);

    // File Details: Upper (picture/player) and lower (EXIF) split pane
    detailsArea.setOrientation(Orientation.VERTICAL);
    detailsArea.prefHeightProperty().bind(scene.heightProperty());
    detailsArea.prefWidthProperty().bind(scene.widthProperty());
    //todo: folgende Zeile aktivieren, sobald MetadataView in detailsArea eingefügt ist
    detailsArea.setDividerPosition(0, DETAILS_AREA_DEFAULT_DIVIDER_POS);

    detailsArea.getItems().addAll(mediaContentView);

    mainSplitPane.getItems().addAll(fileTableView, detailsArea);

    //root-Area
    final BorderPane rootArea = new BorderPane();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());

    rootArea.setTop(mainMenuBar);
    rootArea.setCenter(mainSplitPane);
    rootArea.setBottom(statusBar);
    root.getChildren().add(rootArea);


    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      public void handle(final WindowEvent event) {

        //--ask for unsaved changes
        if (!fileTableView.askIfContinueUnsavedChanges()) {
          event.consume(); // Consuming the close event prevents the application from closing
        } else {
          storeLastMainWindowSettings(); //--save current window sizes to settings file
          globalSettings.store(); //all settings not only Windows-Settings

          fileTableView.stopWatcherThread();
        }
      }
    });

    ExternalEditorsDialog.initializeAllSupportedMediaFileClasses();

    //close the splash screen that might be provided by java -splash:file.jpg  or manifest SplashScreen-Image: images/splash.gif
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash != null) splash.close();

    restoreLastMainWindowSettings();
    primaryStage.show();  //show main window before opening files to show messages while loading


    //wait until resizing has been performed and scene width has followed stage width
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        ensureStageToBeVisible(primaryStage);
        fileTableView.openInitialFolder(initialFileOrFolder);
      }
    });
  }

  @Override
  public final void stop(){
    //release all external ressources e.g. VLC.dll
    mediaContentView.cleanUp();
  }
  /**
   * Store last main window settings from Global-Settings properties file
   * assumes:
   * - globalSettings not null
   * - primaryStage not null
   */
  private void storeLastMainWindowSettings() {
    globalSettings.setProperty(STAGE_X, Double.toString(primaryStage.getX()));
    globalSettings.setProperty(STAGE_Y, Double.toString(primaryStage.getY()));
    globalSettings.setProperty(STAGE_WIDTH, Double.toString(primaryStage.getWidth()));
    globalSettings.setProperty(STAGE_HEIGHT, Double.toString(primaryStage.getHeight()));

    globalSettings.setProperty(MAIN_SPLIT_PANE_DIVIDER_POSITION, Double.toString(mainSplitPane.getDividerPositions()[0]));

    //todo: folgenden Block aktivieren, sobald MetadataView in detailsArea eingefügt ist
    //globalSettings.setProperty(DETAILS_AREA_DIVIDER_POSITION, Double.toString(detailsArea.getDividerPositions()[0]));
    fileTableView.storeLastSettings();
  }

  /**
   * Restore last main window settings from Global-Settings properties file
   * assumes:
   * - globalSettings already loaded
   * - stage not null
   */
  private void restoreLastMainWindowSettings() {
    try {
      primaryStage.setX(Double.parseDouble(globalSettings.getProperty(STAGE_X)));
    } catch (Exception e) {
      primaryStage.setX(default_X);
    }

    try {
      primaryStage.setY(Double.parseDouble(globalSettings.getProperty(STAGE_Y)));
    } catch (Exception e) {
      primaryStage.setY(default_Y);
    }

    try {
      primaryStage.setWidth(Double.parseDouble(globalSettings.getProperty(STAGE_WIDTH)));
    } catch (Exception e) {
      primaryStage.setWidth(default_width);
    }

    try {
      primaryStage.setHeight(Double.parseDouble(globalSettings.getProperty(STAGE_HEIGHT)));
    } catch (Exception e) {
      primaryStage.setHeight(default_height);
    }

    try {
      mainSplitPane.setDividerPosition(0, Double.parseDouble(globalSettings.getProperty(MAIN_SPLIT_PANE_DIVIDER_POSITION)));
    } catch (Exception e) {
      mainSplitPane.setDividerPosition(0, MAIN_SPLIT_PANE_DEFAULT_DIVIDER_POS);
    }

    //todo: folgenden Block aktivieren, sobald MetadataView in detailsArea eingefügt ist
/*    try {
      detailsArea.setDividerPosition(0, Double.parseDouble(globalSettings.getProperty(DETAILS_AREA_DIVIDER_POSITION)));
    } catch (Exception e) {
      detailsArea.setDividerPosition(0,DETAILS_AREA_DEFAULT_DIVIDER_POS);
    }
*/
    fileTableView.restoreLastSettings();
  }

  /**
   * It might happen that the screen's resolution has changed or a stage has been shown on a second screen last time
   * and therefore the stage is not completely visible now.
   * This method checks the stage's boundaries and moves the stage completely into a visible area
   * For this it might be necessary to make the Stage smaller.
   * note: if a second screen is active, but the monitor is switched off...there is no chance ;-)
   *
   * Especially call this method after restoreLastMainWindowSettings() has been used...
   *
   * When called during start-up: wait after resizing of stage has been performed (using runlater)
   * so that the scene size has followed stage size and the decoWidth (border) can be calculated correctly
   *
   * @param stage  (not null) the window which will be sized
   * @throws Exception if stage or scene in stage is null
   */
  public void ensureStageToBeVisible(Stage stage) {
    Rectangle2D bounds;  //bounds per screen
    //global bounds along all screens
    double minX = 0;
    double maxX = 0;
    double minY = 0;
    double maxY = 0;


    //determine entire bounds of all screens
    for (Screen s : Screen.getScreens()) {
      bounds = s.getVisualBounds();
      if (minX > bounds.getMinX()) {
        minX = bounds.getMinX();
      }
      if (maxX < bounds.getMaxX()) {
        maxX = bounds.getMaxX();
      }
      if (minY > bounds.getMinY()) {
        minY = bounds.getMinY();
      }
      if (maxY < bounds.getMaxY()) {
        maxY = bounds.getMaxY();
      }
    }

    //the border may be invisible, so widen the allowed rectangle for placing the window
    double decoWidth = (stage.getWidth() - stage.getScene().getWidth()) / 2.0;   //vertically the border is the same, but the title is additionally that's why only the horizontal width is taken

    minX = minX - decoWidth;
    maxX = maxX + decoWidth;
    minY = minY - decoWidth;
    maxY = maxY + decoWidth;

    //right part not visible --> move to the left
    double invisiblePixels = (stage.getX() + stage.getWidth()) - maxX;
    if (invisiblePixels > 0) {
      stage.setX(stage.getX() - invisiblePixels);
    }
    //lower part not visible --> move up
    invisiblePixels = (stage.getY() + stage.getHeight()) - maxY;
    if (invisiblePixels > 0) {
      stage.setY(stage.getY() - invisiblePixels);
    }

    //left part not visible --> move to the right
    if (stage.getX() < minX) {
      stage.setX(minX);
    }
    //upper part not visible --> move down
    if (stage.getY() < minY) {
      stage.setY(minY);
    }

    //status: either the stage already fits on the screens or
    //if it is larger then the screens it is now in the left upper corner

    //adapt width if stage is larger than visible screens
    if (stage.getWidth() > maxX) {
      stage.setWidth(maxX + decoWidth);  //maxX already includes right decoWidth, add left decoWidth
    }
    if (stage.getHeight() > maxY) {
      stage.setHeight(maxY + decoWidth); //maxY already includes lower decoWidth, add upper decoWidth
    }
  }
}
