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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
 * <li>runnable on all PC platforms supporting Java
 * <li>The viewer treats movie clips like moving photos (like in Harry Potter ;-)
 * </ul>
 *
 * @Author: Ingo Kreuz<br>
 * @version: see KISS_PHOTO_VERSION constant below
 * @modified: 2014-04-29
 * @modified: 2014-07-05 loading initialFileOrFolder after stage.show() to show messages during slow network access
 * <p>
 * Bugs:
 * ======================
 * Icon wird nicht mehr gefunden (ich habe hier gar nichts geändert): siehe unten ca. Zeile 173
 * todo Zoom funktioniert nicht richtig bei Hochkantbildern
 * <p>
 * todo Nice to Haves
 * todo 2013-11-10: Nice to have: Undo (Edit-Reset Selected Files...) auch f&uuml;r einzelne Zeilen/selection :-)
 * todo Nice to have: Undo-History
 * todo 2014-05-24: auto column width using field.setPrefWidth(TextUtils.computeTextWidth(field.getFont(), field.getText(), 0.0D) + 10);
 * todo Statuszeile: Anzahl Dateien, Anzahl &Auml;nderungen, Anzahl Markierung, Anzahl L&ouml;schen (Doppelklick k&ouml;nnte hier auch Undelete-Dialog &ouml;ffnen)
 * <p>
 * urgent:
 * ======================
 * todo Umbenennen-Dialog sollte Link zu Nummerieren-Dialog haben und man sollte die Nummer auch l&ouml;schen k&ouml;nnen
 * todo RecentlyOpened - Liste
 * todo Ctrl-U k&ouml;nnte alles (besser: nur Prefix und Description) von Zeile dr&uuml;ber (vor erster markierter Zeile) in alle markierten kopieren (wie Excel)
 * todo Doppelklick auf Bild muss Vollbild toggeln
 * todo statt commitEdit lieber &bdquo;n&auml;chsteZeile&ldquo; in FileTableView implementieren, die ggf &bdquo;n&auml;chsteZeile&ldquo; von Cell aufruft
 * todo ???Auto/One Click Grundformatierung: "Drehen", "Nummerierien", "Space"(, sonst nix)", falls die Bilder noch DSCN hei&szlig;en, sonst Warnung
 * todo strg-Leertaste muss im Editiermodus Vergr&ouml;&szlig;erung zur&uuml;ckstellen (dann f&uuml;r 100%-Zoom eine andere Tastenkombination!!!)
 * todo Hinweis in Bild auf rechte Maustaste als Hint, damit man auf die Idee kommt es anzuklicken (nur wenn nicht vollbild?!?)
 * todo wenn nicht Vollbild, dann bei Cursor hoch/runter Fokus auf Tabelle
 * todo Umbenennen-Dialog. Default-Feld = Description, Cursor ans Ende stellen
 * <p>
 * planned features:
 * ======================
 * todo EXIF anzeigen
 * todo EXIF-Info &uuml;ber Umbenennen (=&Auml;ndern!!!) in Dateinamen etc. reinholen
 * todo Ausrichtung &uuml;ber EXIF &auml;ndern
 * todo Datum &auml;ndern, auch EXIF
 * todo alle EXIF &auml;ndern
 */
public class KissPhoto extends Application {
  public static final String KISS_PHOTO_VERSION = "0.8.8g work in progress"; //<------------------------------------------------------------------------------
  public static final String KISS_PHOTO = "kissPhoto ";

  private static String initialFileOrFolder;

  private MainMenuBar mainMenuBar;
  private FileTableView fileTableView;
  private MediaContentView mediaContentView;
  private StatusBar statusBar;
  protected Stage primaryStage;
  protected Scene scene;
  private GlobalSettings globalSettings = new GlobalSettings();


  //------------- IDs for GlobalSettings-File
  private static final String STAGE_WIDTH = "StageWidth";
  private static final String STAGE_HEIGHT = "StageHeight";


  /**
   * Restore last main window settings from Global-Settings properties file
   * assumes:
   * - globalSettings already loaded
   * - stage not null
   */
  private void restoreLastMainWindowSettings() {
    try {
      primaryStage.setWidth(Double.parseDouble(globalSettings.getProperty(STAGE_WIDTH)));
    } catch (Exception e) {
      primaryStage.setWidth(1000);
    }

    try {
      primaryStage.setHeight(Double.parseDouble(globalSettings.getProperty(STAGE_HEIGHT)));
    } catch (Exception e) {
      primaryStage.setHeight(800);
    }
  }

  /**
   * Store last main window settings from Global-Settings properties file
   * assumes:
   * - globalSettings not null
   * - primaryStage not null
   */
  private void storeLastMainWindowSettings() {
    globalSettings.setProperty(STAGE_WIDTH, Double.toString(primaryStage.getWidth()));
    globalSettings.setProperty(STAGE_HEIGHT, Double.toString(primaryStage.getHeight()));
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

  /**
   * force repaint by reseting the scene
   * This solves a repainting bug in JavaFx 1.8.05...sometimes
   */
  private void repaint() {
    primaryStage.setScene(null);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        primaryStage.setScene(scene);
      }
    });
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
    scene = new Scene(root, 1000, 800);
    primaryStage.setScene(scene);
    //todo: warum tut das plötzlich nicht mehr? das jpg wird nicht mehr gefunden. About tut auch nicht mehr???

    stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/KissPhotoIcon.jpg")));

    //Create the View-Areas
    statusBar = new StatusBar();
    statusBar.showMessage("");
    mediaContentView = new MediaContentView(primaryStage); //Area for showing Media
    fileTableView = new FileTableView(primaryStage, mediaContentView, statusBar, globalSettings); //File table and directory
    mediaContentView.setFileTableView(fileTableView);

    mainMenuBar = new MainMenuBar(primaryStage, fileTableView, mediaContentView, KISS_PHOTO_VERSION, globalSettings);

    // Left and right split pane
    SplitPane mainSplitPane = new SplitPane();
    mainSplitPane.prefWidthProperty().bind(scene.widthProperty());
    mainSplitPane.prefHeightProperty().bind(scene.heightProperty());
    mainSplitPane.setDividerPosition(0, 0.5);

    // File Details: Upper (picture/player) and lower (EXIF) split pane
    SplitPane detailsArea = new SplitPane();
    detailsArea.setOrientation(Orientation.VERTICAL);
    detailsArea.prefHeightProperty().bind(scene.heightProperty());
    detailsArea.prefWidthProperty().bind(scene.widthProperty());
    detailsArea.setDividerPosition(0, 0.99);

    detailsArea.getItems().addAll(mediaContentView, new Pane());

    mainSplitPane.getItems().addAll(fileTableView, detailsArea);

    //container for splitPane, otherwise it behaves funny (splitters wake-up at the left)
    HBox appArea = new HBox();
    appArea.prefHeightProperty().bind(scene.heightProperty());
    appArea.prefWidthProperty().bind(scene.widthProperty());
    appArea.getChildren().add(mainSplitPane);

    //root-Area
    final BorderPane rootArea = new BorderPane();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());

    rootArea.setTop(mainMenuBar);
    rootArea.setCenter(appArea);
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

    restoreLastMainWindowSettings();
    ExternalEditorsDialog.initializeAllSupportedMediaFileClasses(globalSettings);

    //close the splash screen that might be provided by java -splash:file.jpg  or manifest SplashScreen-Image: images/splash.gif
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash != null) splash.close();

    primaryStage.show();  //show main window before opening files to show messages while loading

    //--------- finally load initial file or folder ---------------
    fileTableView.openInitialFolder(initialFileOrFolder);
  }
}
