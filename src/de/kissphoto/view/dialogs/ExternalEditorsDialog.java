package de.kissphoto.view.dialogs;

import de.kissphoto.helper.GlobalSettings;
import de.kissphoto.helper.StringHelper;
import de.kissphoto.model.ImageFile;
import de.kissphoto.model.MediaFile;
import de.kissphoto.model.MovieFile;
import de.kissphoto.model.OtherFile;
import de.kissphoto.view.inputFields.PathNameTextField;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * This is the Dialog Window for changing the external editors
 * Every Media-File Subclass has one
 * <p/>
 * Whenever a subclass of MediaFile is added this dialog should be enhanced
 * i.e. only this dialog centrally "knows" the subclasses of MediaFile
 * <p/>
 *
 * @author: Ingo Kreuz
 * @date: 2014-05-07
 * @modified: 2014-06-16 multi screen support: center on main window instead of main screen
 * @modified: 2016-11-01 RestrictedTextField no longer tries to store connection to FileTable locally
 */
public class ExternalEditorsDialog extends KissDialog {
  private static final String KISSPHOTO_SPECIFY_EXTERNAL_EDITORS = "kissphoto.specify.external.editors";
  public static final String ELLIPSES = "...";

  final TextField imageFileEditorTextField1 = new PathNameTextField(this);
  final TextField imageFileEditorTextField2 = new PathNameTextField(this);

  final TextField movieFileEditorTextField1 = new PathNameTextField(this);
  final TextField movieFileEditorTextField2 = new PathNameTextField(this);

  final TextField otherFileEditorTextField1 = new PathNameTextField(this);
  final TextField otherFileEditorTextField2 = new PathNameTextField(this);

  final Button imageFileEditorBtn1 = new Button(ELLIPSES);
  final Button imageFileEditorBtn2 = new Button(ELLIPSES);

  final Button movieFileEditorBtn1 = new Button(ELLIPSES);
  final Button movieFileEditorBtn2 = new Button(ELLIPSES);

  final Button otherFileEditorBtn1 = new Button(ELLIPSES);
  final Button otherFileEditorBtn2 = new Button(ELLIPSES);

  FileChooser fileChooserDialog;

  public ExternalEditorsDialog(Stage owner) {
    super(owner);
    setTitle(language.getString(KISSPHOTO_SPECIFY_EXTERNAL_EDITORS));

    setHeight(300);
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

    gridPane.add(new Label(language.getString("image.editor.1")), 0, 0);      //column, row
    gridPane.add(new Label(language.getString("image.editor.2")), 0, 1);
    gridPane.add(imageFileEditorTextField1, 1, 0);
    gridPane.add(imageFileEditorTextField2, 1, 1);
    gridPane.add(imageFileEditorBtn1, 2, 0);
    gridPane.add(imageFileEditorBtn2, 2, 1);
    imageFileEditorTextField1.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));
    imageFileEditorTextField2.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));

    gridPane.add(new Label(language.getString("movie.editor.1")), 0, 2);      //column, row
    gridPane.add(new Label(language.getString("movie.editor.2")), 0, 3);
    gridPane.add(movieFileEditorTextField1, 1, 2);
    gridPane.add(movieFileEditorTextField2, 1, 3);
    gridPane.add(movieFileEditorBtn1, 2, 2);
    gridPane.add(movieFileEditorBtn2, 2, 3);
    movieFileEditorTextField1.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));
    movieFileEditorTextField2.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));

    gridPane.add(new Label(language.getString("standard.editor.1")), 0, 4);      //column, row
    gridPane.add(new Label(language.getString("standard.editor.2")), 0, 5);
    gridPane.add(otherFileEditorTextField1, 1, 4);
    gridPane.add(otherFileEditorTextField2, 1, 5);
    gridPane.add(otherFileEditorBtn1, 2, 4);
    gridPane.add(otherFileEditorBtn2, 2, 5);
    otherFileEditorTextField1.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));
    otherFileEditorTextField2.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH + ELLIPSES_BTN_COL_WIDTH));

    //Ellipses Button Action Listeners
    imageFileEditorBtn1.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFileChooserForTextField(imageFileEditorTextField1);
      }
    });
    imageFileEditorBtn2.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFileChooserForTextField(imageFileEditorTextField2);
      }
    });
    movieFileEditorBtn1.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFileChooserForTextField(movieFileEditorTextField1);
      }
    });
    movieFileEditorBtn2.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFileChooserForTextField(movieFileEditorTextField2);
      }
    });
    otherFileEditorBtn1.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFileChooserForTextField(otherFileEditorTextField1);
      }
    });
    otherFileEditorBtn2.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        showFileChooserForTextField(otherFileEditorTextField2);
      }
    });

    //OK_BOOL / Cancel Buttons
    HBox buttonBox = new HBox();
    buttonBox.setSpacing(30.0);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(7));


    Button okBtn = new Button(KissDialog.OK_LABEL);
    okBtn.setDefaultButton(true);
    okBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
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

  private void showFileChooserForTextField(TextField textField) {
    if (fileChooserDialog == null) fileChooserDialog = new FileChooser();

    fileChooserDialog.setTitle(language.getString(KISSPHOTO_SPECIFY_EXTERNAL_EDITORS));

    String currentPath = StringHelper.extractPathname(textField.getText());
    if (!currentPath.isEmpty())
      fileChooserDialog.setInitialDirectory(new File(currentPath));
    fileChooserDialog.setInitialFileName(StringHelper.extractFileName(textField.getText()));

    File file = fileChooserDialog.showOpenDialog(this);
    if (file != null) {
      textField.setText(file.getAbsolutePath());
    }

  }

  /**
   * initialize and show the modal dialog
   * The Textfields are set to the current external editors of the supported subclasses of MediaFile
   *
   * @return true if the dialog has been closed with OK_BOOL, else false
   */
  public boolean showModal(GlobalSettings globalSettings) {
    modalResult_bool = false;

    imageFileEditorTextField1.setText(ImageFile.getExternalMainEditorPath());
    imageFileEditorTextField2.setText(ImageFile.getExternal2ndEditorPath());

    movieFileEditorTextField1.setText(MovieFile.getExternalMainEditorPath());
    movieFileEditorTextField2.setText(MovieFile.getExternal2ndEditorPath());

    otherFileEditorTextField1.setText(OtherFile.getExternalMainEditorPath());
    otherFileEditorTextField2.setText(OtherFile.getExternal2ndEditorPath());

    centerOnOwner();
    toFront();
    repaint();
    showAndWait();

    if (modalResult_bool) {
      saveExternalEditorPaths(globalSettings);
    }
    return modalResult_bool;
  }

  /**
   * save the text field's content to global settings
   * and change the settings in the MediaFile subclasses
   *
   * @param globalSettings link to the settings file
   */
  public void saveExternalEditorPaths(GlobalSettings globalSettings) {
    ImageFile.setExternalEditorPaths(imageFileEditorTextField1.getText(), imageFileEditorTextField2.getText(), globalSettings);
    MovieFile.setExternalEditorPaths(movieFileEditorTextField1.getText(), movieFileEditorTextField2.getText(), globalSettings);
    OtherFile.setExternalEditorPaths(otherFileEditorTextField1.getText(), otherFileEditorTextField2.getText(), globalSettings);
  }

  /**
   * Main class must call this for reloading the the external editors
   * for all supported subclasses of MediaFile
   *
   * @param globalSettings link to the settings file
   */
  public static void initializeAllSupportedMediaFileClasses(GlobalSettings globalSettings) {
    ImageFile.loadExternalEditorPaths(globalSettings);
    MovieFile.loadExternalEditorPaths(globalSettings);
    OtherFile.loadExternalEditorPaths(globalSettings);
  }

  /**
   * GUI can call this method to execute the editor specified for the selection
   * The type of the first selected file determines the editor to call
   * All selected media files will be passed to that editor as parameters
   *
   * @param selection  the current selection
   * @param mainEditor true to call the mainEditor, false to call the 2nd Editor
   */
  public static void executeExternalEditor(ObservableList<MediaFile> selection, boolean mainEditor) {
    if (selection != null && selection.size() > 0) {
      MediaFile firstFile = selection.get(0);

      if (firstFile.getClass() == ImageFile.class) ImageFile.executeExternalEditor(selection, mainEditor);
      else if (firstFile.getClass() == MovieFile.class) MovieFile.executeExternalEditor(selection, mainEditor);
      else OtherFile.executeExternalEditor(selection, mainEditor);
    }
  }
}