package dringo.kissPhoto;

import dringo.kissPhoto.helper.GlobalSettings;
import dringo.kissPhoto.helper.I18Support;
import dringo.kissPhoto.view.*;
import dringo.kissPhoto.view.dialogs.ExternalEditorsDialog;
import dringo.kissPhoto.view.MetaInfoView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mediautil.gen.Log;

import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * Keep it simple! File renaming and Photo/Media File Management Main Class<br>
 * ========================================================<p>
 * The main KISS ideas of the application are
 * <ul>
 * <li>Rename files like in a word processor's table:  move around with the cursor, search and replace + mass rename, renumbering and sorting</li>
 * <li>photo management and photo show without any additional files or database</li>
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
 * @since 2014-04-29
 * @version see below in constant KISS_PHOTO_VERSION
 * <p>
 *
 * Next:
 * todo lookup-Felder in Editable Meta Tags anzeigen und mit Pulldown editieren
 * todo Bei Namenskonflikt (z.B. wenn Datum als Zahl genommen und alles andere gelöscht) wird beim umbenennen automatisch Zahl angehängt. Dann muss Sternchen weg und Zahl in Anzeige übernommen werden
 * todo settingsdatei: aktiven TagsView-Tab speichern/wiederherstellen
 * todo settingsdatei: Pfad in EditableTagsView speichern/wiederherstellen
 * todo Exif schreiben in Datei
 * todo Komplette Exif-Spec übernehmen in Exif-Klasse
 * todo Test:String-Tag
 * todo Test:lookup-Tag
 * todo Test:Datum-Tag
 * todo Test:number-Tag
 * todo Multi-Edit-Dialog: Copyright und Event-Beschreibung
 * todo Multi-Edit-Dialog: Datum-Dialog mit Berechnungen.
 * todo Helper, der Zeit-Differenz zwischen zwei Files ausgibt
 * todo System.out in MetaInfoEditableTagsView und MetaInfoAllTagsView und MediaFileTaggedEditable.getEditableImageInfo entfernen
 * Bugs:
 * todo Name-Parsing-Heuristik tut nicht falls 1, 2, ... und ein Datum 2021_01_02 drin ist. Idee: angeklickte Datei und nicht erste Datei untersuchen
 * ======================
 * planned features:
 * ======================
 * todo Meldung initalFile genauer: 1. übergebene Datei nicht gefunden ... +2. vorheriges Verzeichnis nicht gefunden
 * todo overscan mit padding im ContenView (FullScreen only) und auch speichern in globalSettings (nur context-Menü und Tastenkombi/Mausrad)
 *
 * todo Fortschrittsbalken auch bei Drehen (weil Exif gelesen werden muss, dauert das manchmal länger) am besten immer wenn Exif gelesen wird (z.B. auch beim Sortieren von Exif-Spalte??)
 *
 * todo Änderungen während Speichern verhindern (z.B. Modales Fenster mit Abbrechen-Knopf). Inbesondere, wenn viele Drehungen/Exif-Operationen dabei sind
 * todo Abbrechen von Speichern ermöglichen (z.B. Modales Fenster mit Abbrechen-Knopf): Insbesondere, wenn Zugriff auf Ziellaufwerk langsam ist und doch nicht gespeichert werden soll
 *
 * todo Multi-Edit Exif: Datum, Autor, Beschreibung, Copyright
 * todo Multi-Edit-Dialog: Hilfe nur auf Knopfdruck ("Hilfe zu %p %d..."), Aufruf Nummerieren-Dialog rein, EXIF-Kommentar rein (der bekommt %k, damit Übernahme möglich)
 * todo   Umbenennen-Dialog renovieren (Kontextmenü statt Buttons) und testen ob alle Ersetzungen auch funktionieren
 * todo   Umbenennen-Dialog. Default-Feld = Description, Cursor ans Ende stellen
 * todo   Umbenennen-Dialog sollte Link zu Nummerieren-Dialog haben und man sollte die Nummer auch löschen können
 * todo Zeit-Dialog: Übernehmen von Ändern, Exif, Digitalisierung Stunden/Minuten dazu/abziehen(anwenden immer auf alles andere)
 * todo Auto/One Click Grundformatierung: "Drehen", "Nummerieren", "Space"(, sonst nix)", falls die Bilder noch DSCN heißen, sonst Warnung
 * todo EXIF schreiben evtl über http://sourceforge.net/projects/image-tagger/
 * nice to haves:
 * ===============
 * todo doch nochmal schauen, ob die Updates vom FileWatcher nicht verwendet werden können. Siehe JavaFX Task: A Task Which Returns Partial Results
 * todo Nice to have: Undo-History
 */
public class KissPhoto extends Application {
  //please check Log.debugLevel in main()
  public static final String KISS_PHOTO_VERSION = "0.22.831"; // <------------------------------------------------------------------------------
  public static final String KISS_PHOTO = "kissPhoto ";
  public static ResourceBundle language = null;

  //set in void main(args) = in static context, so the need to be static
  private static String initialFileOrFolder = "";
  public static boolean optionNoVLC = false; //if -noVLC is provided then prevent from searching for and using of vlc
  public static boolean optionNoFX = false;  //if -noFX is provided then use the DummyPlayerViewer (for FX incompatible installations without VLC)
  private static boolean optionHelp = false; //-Help will println a short helptext

  private final SplitPane mainSplitPane = new SplitPane();
  private final SplitPane detailsSplitPane = new SplitPane();

  private Stage primaryStage;
  private Scene scene;

  private FileTableView fileTableView;
  private MediaContentView mediaContentView;
  private MetaInfoView metaInfoView;
  private StatusBar statusBar;

  //all classes can access the settings file
  public static GlobalSettings globalSettings = new GlobalSettings();


  //------- Default Window size/position if no valid settings file found
  private static final double default_X = 0;
  private static final double default_Y = 0;
  private static final double default_width = 1000;
  private static final double default_height = 800;
  private static final double MAIN_SPLIT_PANE_DEFAULT_DIVIDER_POS = 0.5;

  //------------- IDs for GlobalSettings-File
  private static final String STAGE_X = "StageX";
  private static final String STAGE_Y = "StageY";
  private static final String STAGE_WIDTH = "StageWidth";
  private static final String STAGE_HEIGHT = "StageHeight";

  private static final String MAIN_SPLIT_PANE_DIVIDER_POSITION = "mainSplitPane_DividerPosition";

  /**
   * Entry point
   *
   * @param args the command line arguments handed by command shell:<br>
   *             [filename] (file or folder): will be opened<br>
   *             -noVLC: prevent the search for and use of vlc (for testing or to save resources)
   *             -help: a short help text about parameters
   */
  public static void main(String[] args) {
    //Debug-Level currently used in mediaUtil and ImageFileRotater
    Log.debugLevel = Log.LEVEL_NONE; //please no output on console (default is 3=Log.LEVEL_INFO) which shows ERROR, WARNING and INFO)
    //Log.debugLevel = Log.LEVEL_DEBUG;

    int i = 0;

    if (args.length > 0) {
      //first parameter might be filename
      if (args[0].startsWith("\"")) { //if starting with quote then collect parameters as filename parts until closing quotes
        initialFileOrFolder = args[0];
        i++;

        while (i < args.length && !args[i-1].endsWith("\"")) { //collect Filename until closing quotes
          initialFileOrFolder = initialFileOrFolder + " " + args[i];
          i++;
        }
      } else {
        if (!isOption(args[0])) {
          initialFileOrFolder = args[0];
          i++;
        }
      }

      //options
      while (i < args.length) {
        if (isOption(args[i])) {
          String arg = args[i].toLowerCase(Locale.ROOT);
          if (arg.endsWith("novlc")) optionNoVLC = true; //accepts -novlc or --NOVLC  -noVLC or /noVLC...
          if (arg.endsWith("nofx")) optionNoFX = true;
          if (arg.endsWith("help")) optionHelp = true;
        }
        i++;
      }
      if (optionNoVLC) System.out.println("option 'noVLC' detected");
      if (optionNoFX) System.out.println("option 'noFX' detected");

    }
    //System.out.println("Java-Version "+System.getProperty("java.version"));

    Application.launch(args);
}

static private boolean isOption(String arg){
    return arg.startsWith("-") || arg.startsWith("/");
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
    scene.getStylesheets().add(getClass().getResource("/kissPhoto.css").toExternalForm());      //styling esp. for TreeTableView to look like a TableView
    primaryStage.setScene(scene);

    stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/KissPhotoIcon.jpg")));

    //Create the View-Areas
    statusBar = new StatusBar();
    statusBar.showMessage("");
    mediaContentView = new MediaContentView(primaryStage); //Area for showing Media

    metaInfoView = new MetaInfoView(detailsSplitPane); //it needs a handle to the SplitPane where it resides because its size controls the split position
    fileTableView = new FileTableView(primaryStage, mediaContentView, metaInfoView, statusBar); //File table and directory
    statusBar.connectUndeleteDialog(fileTableView);
    mediaContentView.setOtherViews(fileTableView, metaInfoView);
    metaInfoView.setOtherViews(fileTableView, mediaContentView, statusBar);


    MainMenuBar mainMenuBar = new MainMenuBar(primaryStage, fileTableView, mediaContentView, metaInfoView);
    mainMenuBar.addRecentlyMenu(fileTableView.getFileHistory().getRecentlyFilesMenu());
    // Left and right split pane
    mainSplitPane.prefWidthProperty().bind(scene.widthProperty());
    mainSplitPane.prefHeightProperty().bind(scene.heightProperty());
    mainSplitPane.setDividerPosition(0, MAIN_SPLIT_PANE_DEFAULT_DIVIDER_POS);

    //File Details: Upper (picture/player) and lower (EXIF) split pane
    //note: the position of the detailsArea's divider is managed in MetaInfoView!
    detailsSplitPane.setOrientation(Orientation.VERTICAL);
    detailsSplitPane.prefHeightProperty().bind(scene.heightProperty());
    detailsSplitPane.prefWidthProperty().bind(scene.widthProperty());
    detailsSplitPane.getItems().addAll(mediaContentView, metaInfoView);

    mainSplitPane.getItems().addAll(fileTableView, detailsSplitPane);

    //root-Area
    final BorderPane rootArea = new BorderPane();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());

    rootArea.setTop(mainMenuBar);
    rootArea.setCenter(mainSplitPane);
    rootArea.setBottom(statusBar);
    root.getChildren().add(rootArea);


    primaryStage.setOnCloseRequest(event -> {

      //--ask for unsaved changes
      if (!fileTableView.askIfContinueUnsavedChanges()) {
        event.consume(); // Consuming the close event prevents the application from closing
      } else {
        storeLastMainWindowSettings(); //--save current window sizes to settings file
        globalSettings.store(); //all settings not only Windows-Settings

        fileTableView.stopWatcherThread();
      }
    });

    ExternalEditorsDialog.initializeAllSupportedMediaFileClasses();

    //close the splash screen that might be provided by java -splash:file.jpg  or manifest SplashScreen-Image: images/splash.gif
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash != null) splash.close();

    primaryStage.show();  //show main window before opening files to show messages while loading
    restoreLastMainWindowSettings();


    //wait until resizing has been performed and scene width has followed stage width
    Platform.runLater(() -> {
      ensureStageToBeVisible(primaryStage);
      fileTableView.openInitialFolder(initialFileOrFolder);
    });
  }

  @Override
  public final void stop() {
    //release all external resources e.g. VLC.dll
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

    metaInfoView.storeVisibilityInGlobalSettings();
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

    metaInfoView.restoreVisibilityFromGlobalSettings();
    fileTableView.restoreLastSettings();
  }

  /**
   * It might happen that the screen's resolution has changed or a stage has been shown on a second screen last time
   * and therefore the stage is not completely visible now.
   * This method checks the stage's boundaries and moves the stage completely into a visible area
   * For this it might be necessary to make the Stage smaller.
   * note: if a second screen is active, but the monitor is switched off...there is no chance ;-)
   * <p>
   * Especially call this method after restoreLastMainWindowSettings() has been used...
   * <p>
   * When called during start-up: wait after resizing of stage has been performed (using runlater)
   * so that the scene size has followed stage size and the decoWidth (border) can be calculated correctly
   *
   * @param stage (not null) the window which will be sized
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

  public FileTableView getFileTableView() {
    return fileTableView;
  }

  public StatusBar getStatusBar() {
    return statusBar;
  }
  public MediaContentView getMediaContentView() {
    return mediaContentView;
  }
  public MetaInfoView getMetaInfoView() {
    return metaInfoView;
  }

}
