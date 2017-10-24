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


/**
 * Keep it simple! Photo Management Main Class<br>
 * ===========================================<p>
 * The main KISS ideas of the application are
 * <ul>
 * <li> photo management and photo show without installation or database
 * <li>self containing: all information is in the picture files, filenames or directory names
 * <li>auto numbering: file order can be changed, file numbering will follow
 * <li>file date maintaining
 * <li>mass renaming for filenames, EXIF-Info and time-stamps
 * <li>runnable on all PC platforms which support Java
 * <li>The viewer treats movie clips like moving photos (like in Harry Potter ;-)
 * </ul>
 *
 * @Author: Ingo Kreuz<br>
 * @version: see KISS_PHOTO_VERSION constant below
 * @modified: 2014-04-29
 * @modified: 2014-07-05 loading initialFileOrFolder after stage.show() to show messages during slow network access
 * @modified: 2017-10-02 main window is moved into visible part of screen after startup (e.g. if resolution changed or 2nd screen has been disabled)
 * <p>
 * Bugs:
 * ======================
 * todo FileTableView.scrollViewportToIndex überdenken: scrollt z.B. nicht zum letzten File, falls das gelöscht wurde
 * todo audio abspielen (m4a, mp3, wav): Player nicht ausblenden!
 * todo ??? statt commitEdit lieber "nächsteZeile"; in FileTableView implementieren, die ggf "nächsteZeile" von Cell aufruft
 * todo Hinweis in Bild auf rechte Maustaste als Hint, damit man auf die Idee kommt es anzuklicken (nur wenn nicht Vollbild?!?)
 * todo wenn nicht Vollbild, dann bei Cursor hoch/runter Fokus auf Tabelle
 * todo Umbenennen-Dialog renovieren (Kontextmenü statt Buttons) und testen ob alle Ersetzungen auch funktionieren
 * todo Umbenennen-Dialog. Default-Feld = Description, Cursor ans Ende stellen
 * todo Umbenennen-Dialog sollte Link zu Nummerieren-Dialog haben und man sollte die Nummer auch l&ouml;schen k&ouml;nnen
 * planned features:
 * ======================
 * todo JPG drehen. Z.B. http://mediachest.sourceforge.net/mediautil/
 * todo Auto/One Click Grundformatierung: "Drehen", "Nummerierien", "Space"(, sonst nix)", falls die Bilder noch DSCN hei&szlig;en, sonst Warnung
 * todo EXIF anzeigen
 * todo EXIF-Info über Umbenennen (=ändern!!!) in Dateinamen etc. reinholen
 * todo Datum ändern, auch EXIF
 * todo alle EXIF ändern
 * nice to haves:
 * ===============
 * <p>
 * todo 2014-05-24: auto column width using field.setPrefWidth(TextUtils.computeTextWidth(field.getFont(), field.getText(), 0.0D) + 10);
 * todo Nice to have: Undo-History
 * <p>
 */
public class KissPhoto extends Application {
  public static final String KISS_PHOTO_VERSION = "0.8.15"; // <------------------------------------------------------------------------------
  public static final String KISS_PHOTO = "kissPhoto ";

  private static String initialFileOrFolder;

  private MainMenuBar mainMenuBar;
  private FileTableView fileTableView;
  private MediaContentView mediaContentView;
  private StatusBar statusBar;
  protected Stage primaryStage;
  protected Scene scene;
  private GlobalSettings globalSettings = new GlobalSettings();

  SplitPane mainSplitPane = new SplitPane();
  SplitPane detailsArea = new SplitPane();

  //------------- IDs for GlobalSettings-File
  private static final double default_X = 0;
  private static final double default_Y = 0;
  private static final double default_width = 1000;
  private static final double default_height = 800;
  private static final double MAIN_SPLIT_PANE_DEFAULT_DIVIDER_POS = 0.5;
  private static final double DETAILS_AREA_DEFAULT_DIVIDER_POS = 0.99;

  private static final String STAGE_X = "StageX";
  private static final String STAGE_Y = "StageY";
  private static final String STAGE_WIDTH = "StageWidth";
  private static final String STAGE_HEIGHT = "StageHeight";

  private static final String MAIN_SPLIT_PANE_DIVIDER_POSITION = "mainSplitPane_DividerPosition";
  private static final String DETAILS_AREA_DIVIDER_POSITION = "detailsArea_DividerPosition";


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


  /**
   * * @param args the command line arguments handed by command shell
   *      
   */
  public static void main(String[] args) {

    if (args.length >= 1) {
      initialFileOrFolder = args[0]; //first parameter is treated as file or folder to open
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
      //leave default language if property or settings file could not be found
    }

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

    fileTableView = new FileTableView(primaryStage, mediaContentView, statusBar, globalSettings); //File table and directory
    statusBar.connectUndeleteDialog(fileTableView);
    mediaContentView.setFileTableView(fileTableView);

    mainMenuBar = new MainMenuBar(primaryStage, fileTableView, mediaContentView, KISS_PHOTO_VERSION, globalSettings);
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

    ExternalEditorsDialog.initializeAllSupportedMediaFileClasses(globalSettings);

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
}
