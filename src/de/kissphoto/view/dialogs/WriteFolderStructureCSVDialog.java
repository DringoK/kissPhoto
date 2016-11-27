package de.kissphoto.view.dialogs;

import de.kissphoto.helper.StringHelper;
import de.kissphoto.model.MediaFileList;
import de.kissphoto.view.StatusBar;
import de.kissphoto.view.inputFields.PathNameTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.text.MessageFormat;

/**
 * This is the Dialog Window for choosing a root folder and
 * a target csv-filename for writing a folder structure to csv
 *
 * @author: Ingo Kreuz
 * @date: 2014-05-30
 * @modified: 2014-06-05 java.io operations changed into java.nio
 * @modified: 2014-06-16 multi screen support: center on main window instead of main screen
 * @modified: 2016-11-01 RestrictedTextField no longer tries to store connection to FileTable locally
 */
public class WriteFolderStructureCSVDialog extends KissDialog {
  private static final String CSV_STD_FILENAME = "kissPhotoFolderStructure.csv";
  public static final String COMMA_SEPARATED_VALUES_FILE_SPREADSHEET = "comma.separated.values.file.spreadsheet";
  public static final String CSV = "*.CSV";
  public static final String ALL_FILES = "all.files";

  Scene scene;

  final TextField rootFolderTextField = new PathNameTextField(this);
  final TextField csvFilenameTextField = new PathNameTextField(this);

  final Button rootDirEditorBtn = new Button("...");
  final Button csvFilenameEditorBtn = new Button("...");

  final Button okBtn;

  FileChooser fileChooserDialog;
  DirectoryChooser directoryChooserDialog;

  MediaFileList mediaFileList; //link to "current Directory" for suggesting this as the root Directory
  StatusBar statusBar; //link to the status bar for error messages while exporting CSV

  public WriteFolderStructureCSVDialog(Stage owner, MediaFileList mediaFileList, final StatusBar statusBar) {
    super(owner);
    this.mediaFileList = mediaFileList;
    this.statusBar = statusBar;

    setTitle(language.getString("kissphoto.write.folder.structure.to.csv"));

    setHeight(200);
    setWidth(800);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.WHITE);  //1,1 --> use min Size as set just before
    setScene(scene);

    VBox rootArea = new VBox();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());

    GridPane gridPane = new GridPane();
    gridPane.setHgap(5);
    gridPane.setVgap(2);
    gridPane.prefHeightProperty().bind(scene.heightProperty());
    gridPane.prefWidthProperty().bind(scene.widthProperty());
    gridPane.setAlignment(Pos.CENTER);
    Insets mainPadding = new Insets(7, 7, 7, 7);
    gridPane.setPadding(mainPadding);

    //Editors
    final double LABEL_COL_WIDTH = 160;
    final double ELLIPSES_BTN_COL_WIDTH = 30;

    rootFolderTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        enableValidButtons();
      }
    });
    csvFilenameTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        enableValidButtons();
      }
    });


    gridPane.add(new Label(language.getString("root.folder")), 0, 0);      //column, row
    gridPane.add(new Label(language.getString("output.file")), 0, 1);
    gridPane.add(rootFolderTextField, 1, 0);
    gridPane.add(csvFilenameTextField, 1, 1);
    gridPane.add(rootDirEditorBtn, 2, 0);
    gridPane.add(csvFilenameEditorBtn, 2, 1);
    rootFolderTextField.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));
    csvFilenameTextField.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));

    //Ellipses Button Action Listeners
    rootDirEditorBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showDirectoryChooser();
        enableValidButtons();
      }
    });
    csvFilenameEditorBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFileChooser();
        enableValidButtons();
      }
    });

    //OK_BOOL / Cancel Buttons
    HBox buttonBox = new HBox();
    buttonBox.setSpacing(30.0);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(7));


    okBtn = new Button(KissDialog.OK_LABEL);
    okBtn.setDefaultButton(true);
    okBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        try {
          exportFolderStructureToCSV(rootFolderTextField.getText(), csvFilenameTextField.getText());
          statusBar.showMessage(language.getString("csv.successfully.generated.for.directory.structure"));
        } catch (IOException e) {
          statusBar.showError(MessageFormat.format(language.getString("error.generating.csv.file.for.directory.structure.0"), e.getMessage()));
        }
        modalResult_bool = OK_BOOL;
        close();
      }
    });
    Button cancelBtn = new Button(KissDialog.CANCEL_LABEL);
    cancelBtn.setCancelButton(true);
    cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        modalResult_bool = CANCEL_BOOL;
        close();
      }
    });
    buttonBox.getChildren().addAll(okBtn, cancelBtn);

    rootArea.getChildren().addAll(gridPane, buttonBox);
    root.getChildren().add(rootArea);
  }

  private void enableValidButtons() {
    okBtn.setDisable(csvFilenameTextField.getText().isEmpty() || rootFolderTextField.getText().isEmpty());
  }

  /**
   * show the OS's directory chooser to select a rootFolder
   * if dialog is closed with OK_BOOL then the masterExtTextField is updated with the chosen directory path
   * if no path is provided in masterExtTextField then
   * the currentFolder of mediaFileList is the initial directory for the dialog
   *
   * @return the File to be used as root directory or null, if the dialog was not closed with OK_BOOL
   */
  private void showDirectoryChooser() {
    if (directoryChooserDialog == null) {
      directoryChooserDialog = new DirectoryChooser();
      directoryChooserDialog.setTitle(language.getString("kissphoto.write.folder.structure.select.root.directory"));
    }
    Path initDir = Paths.get(rootFolderTextField.getText()); //first try to parse the editInputField

    if (!Files.exists(initDir))
      if (mediaFileList.getCurrentFolder() != null)  //only if something has been loaded before
        initDir = mediaFileList.getCurrentFolder().toAbsolutePath();

    if (Files.exists(initDir))
      directoryChooserDialog.setInitialDirectory(initDir.toFile());

    File chosenDir = directoryChooserDialog.showDialog(getOwner());   //already asks if existing file should be replaced ;-)

    if (chosenDir != null) {
      rootFolderTextField.setText(chosenDir.getAbsolutePath());
    }
  }

  /**
   * show the OS's file chooser to select an output filename (csv)
   * if dialog is closed with OK_BOOL then the csvFilenameTextField is updated with the chosen filename
   * if no path is provided in csvFilenameTextField then
   * the currentFolder of mediaFileList is the initial directory for the dialog
   *
   * @return the File to write in or null, if the dialog was not closed with OK_BOOL
   */
  private void showFileChooser() {
    if (fileChooserDialog == null) {
      fileChooserDialog = new FileChooser();
      fileChooserDialog.setTitle(language.getString("kissphoto.write.folder.structure.provide.a.filename.for.csv.output"));

      fileChooserDialog.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter(language.getString(COMMA_SEPARATED_VALUES_FILE_SPREADSHEET), CSV),
          new FileChooser.ExtensionFilter(language.getString(ALL_FILES), "*.*")
      );
    }

    if (csvFilenameTextField.getText().isEmpty()) {
      if (mediaFileList.getCurrentFolder() != null)  //only if something has been loaded before
        fileChooserDialog.setInitialDirectory(mediaFileList.getCurrentFolder().toAbsolutePath().toFile());
      fileChooserDialog.setInitialFileName(CSV_STD_FILENAME);
    } else {
      //initialize with editInputField's content
      Path initDir = Paths.get(StringHelper.extractPathname(csvFilenameTextField.getText())); //first: try to parse the editInputField

      if (!Files.exists(initDir))
        if (mediaFileList.getCurrentFolder() != null)  //only if something has been loaded before
          initDir = mediaFileList.getCurrentFolder().toAbsolutePath();
      if (Files.exists(initDir))
        fileChooserDialog.setInitialDirectory(initDir.toFile());


      fileChooserDialog.setInitialFileName(StringHelper.extractFileName(csvFilenameTextField.getText()));
    }

    File chosenFile = fileChooserDialog.showSaveDialog(getOwner());   //already asks if existing file should be replaced ;-)

    if (chosenFile != null) {
      csvFilenameTextField.setText(chosenFile.getAbsolutePath());
    }
  }

  Writer writer = null;

  /**
   * The CSV file has the following format:
   * full path to rootDir
   * [empty line]
   * tree of directories
   *
   * @param rootDirName path to the rootDir i.e. the staring Point of recursive export
   * @param filename    path to the file to write into. If the file already exists it will be overwritten
   * @throws java.io.IOException from FileWriter class
   */
  public void exportFolderStructureToCSV(String rootDirName, String filename) throws IOException {
    try {
      writer = new OutputStreamWriter(new FileOutputStream(filename), "ISO-8859-1");  //(UTF-8 does not work with Excel), US-ASCII, ISO-8859-1, UTF-8, UTF-16-BE, UTF-16LE, UTF-16

      //-------- write full path to rootDirName
      writer.write("\"" + rootDirName + "\"\n\n");

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(rootDirName))) {
        //-------- write recursivly all subfolder' directory trees
        for (Path currentDir : stream) {
          if (Files.isDirectory(currentDir) && !Files.isHidden(currentDir)) //collect all directories, ignore hidden files
            exportFolderStructureToCSVOneColumn(currentDir, 0);
        }
      } catch (IOException | DirectoryIteratorException x) {
        //ignore problems with file system
      }
    } finally {
      if (writer != null) writer.close();
    }

  }

  /**
   * write the directry's name
   * if sub folders exist recursively write the first one on next level (one leading separator)
   * and all others recursively on the level which has been passed +1 (level+1 leading separators)
   * Every indent level results in one table column:
   * subFolder1
   * subFolder2
   * -> subFolder2.1
   * -> subFolder2.2
   * ->              -> subFolder2.2.1
   * ->              -> subFolder2.2.2 ...
   * subFolder3
   * ...
   *
   * @param rootDir the (recursive) root of the subtree to output
   * @param level   the level of the subtree (for generating enough separator characters (=empty columns))
   */
  private void exportFolderStructureToCSVMultiColumns(Path rootDir, int level) throws IOException {
    final char sep = StringHelper.getLocaleCSVSeparator();  //just a shortcut
    boolean first = true;

    //write own name
    writer.write("\"" + rootDir.getFileName() + "\"");

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir)) {
      //-------- write recursivly all subfolder' directory trees
      for (Path subDir : stream) {
        if (Files.isDirectory(subDir) && !Files.isHidden(subDir)) { //collect all directories, ignore hidden files
          if (first) {
            writer.write(sep);
            first = false;
          } else {
            writer.write(StringHelper.repeat(Character.toString(sep), level + 1));
          }
          exportFolderStructureToCSVMultiColumns(subDir, level + 1);

        }
      }
    } catch (IOException | DirectoryIteratorException x) {
      //ignore problems with file system
    }

    writer.write("\n");
  }

  private static final String SEP = "     ";

  /**
   * write the directry's name
   * if subFolders exist recursivly write them recursivle on the level which has been passed +1 (level+1 times leading separators
   * only one column is used in CSV: for every level SEP is written before the dir name for indention
   * <p/>
   * subFolder1
   * subFolder2
   * subFolder2.1
   * subFolder2.2
   * subFolder2.2.1
   * subFolder2.2.2 ...
   * subFolder3
   * ...
   *
   * @param rootDir the (recursive) root of the subtree to output
   * @param level   the level of the subtree (for generating enough separator characters (=empty columns))
   */
  private void exportFolderStructureToCSVOneColumn(Path rootDir, int level) throws IOException {
    //write own name
    writer.write("\"" + StringHelper.repeat(SEP, level) + rootDir.getFileName() + "\"\n");

    //recursively write all subFolders
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir)) {
      //-------- write recursivly all subfolder' directory trees
      for (Path subDir : stream) {
        if (Files.isDirectory(subDir) && !Files.isHidden(subDir)) //collect all directories, ignore hidden files
          exportFolderStructureToCSVOneColumn(subDir, level + 1);
      }
    }
  }

  /**
   * show the dialog
   *
   * @return "true" if the dialog has been closed using OK_BOOL else false
   */
  public boolean showModal() {
    modalResult_bool = false;

    //initialize textFields if empty
    if (rootFolderTextField.getText().isEmpty()) {
      if (mediaFileList.getCurrentFolder() != null)  //only if something has been loaded before
        rootFolderTextField.setText(mediaFileList.getCurrentFolder().toAbsolutePath().toString());
    }
    if (csvFilenameTextField.getText().isEmpty()) {
      if (mediaFileList.getCurrentFolder() != null)  //only if something has been loaded before
        csvFilenameTextField.setText(mediaFileList.getCurrentFolder().toAbsolutePath().resolve(CSV_STD_FILENAME).toString());
    }

    enableValidButtons();

    centerOnOwner();
    toFront();
    repaint();
    showAndWait();

    return modalResult_bool;
  }
}
