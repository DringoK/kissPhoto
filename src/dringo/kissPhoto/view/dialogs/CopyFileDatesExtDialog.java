package dringo.kissPhoto.view.dialogs;

import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileList;
import dringo.kissPhoto.model.OtherFile;
import dringo.kissPhoto.view.FileTableView;
import dringo.kissPhoto.view.inputFields.FileNameTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * this dialog is for copying filedates from a "master-extension".
 * Imagine you've converted your video clips from mov-container to mp4 container, e.g. using XMediaRecode.
 * Then the mp4 version of the clips have the conversion date as their file date.
 * Use this dialog and specify "mov" as the master to copy the filedates from each mov file to all files with the same name (except extension).
 * <p/>
 *
 * @author Dringo
 * @since 2014-06-19
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 */
public class CopyFileDatesExtDialog extends KissDialog {
  public static final int FILEDATE_COL_WIDTH = 155;
  ObservableList<MediaFile> sortedFileList; //"current Directory" sorted for resultingFileName for preview

  final TextField masterExtTextField = new FileNameTextField(this);
  final TreeTableView<MediaFile> treeTableView = new TreeTableView<>();
  TreeItem<MediaFile> root;

  final Button okBtn;


  /**
   * constructor
   *
   * @param owner         //the father stage for which this dialog is modal
   * @param mediaFileList //link to mediaFileList for preview
   */
  public CopyFileDatesExtDialog(Stage owner, MediaFileList mediaFileList) {
    super(owner);
    sortedFileList = FXCollections.observableArrayList(mediaFileList.getFileList()); //copy
    FXCollections.sort(sortedFileList);

    setTitle(language.getString("kissphoto.copy.file.dates.by.master.extension"));

    setHeight(600);
    setWidth(800);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.WHITE);  //1,1 --> use min Size as set just before
    setScene(scene);

    VBox inputArea = new VBox();

    HBox masterExtAreaBox = new HBox();
    masterExtAreaBox.setSpacing(30.0);
    masterExtAreaBox.setAlignment(Pos.CENTER);
    masterExtAreaBox.setPadding(new Insets(7));
    masterExtAreaBox.getChildren().addAll(new Label(language.getString("master.extension")), masterExtTextField);

    masterExtTextField.setOnKeyReleased(keyEvent -> buildClusteredFileList());

    //OK / Cancel Buttons
    HBox buttonBox = new HBox();
    buttonBox.setSpacing(30.0);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(7));


    okBtn = new Button(OK_LABEL);
    okBtn.setDefaultButton(true);
    okBtn.setOnAction(actionEvent -> {
      modalResult_bool = OK_BOOL;
      close();
    });
    Button cancelBtn = new Button(CANCEL_LABEL);
    cancelBtn.setCancelButton(true);
    cancelBtn.setOnAction(actionEvent -> {
      modalResult_bool = CANCEL_BOOL;
      close();
    });
    buttonBox.getChildren().addAll(okBtn, cancelBtn);

    inputArea.getChildren().addAll(masterExtAreaBox, buttonBox, new Label("   " + language.getString("preview")));

    initTreeTableView(mediaFileList);

    BorderPane rootArea = new BorderPane();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());
    rootArea.setTop(inputArea);
    rootArea.setCenter(treeTableView); //use the rest of the dialog for the input

    root.getChildren().add(rootArea);
  }

  /**
   * build the treetable view with the columns
   * resultingFilename (tree column=main column)
   * fileDate
   *
   * @param mediaFileList link to the original mediaFileList for getting counterPosition
   */
  private void initTreeTableView(MediaFileList mediaFileList) {

    //this is just for setting a sensible name as the root node  (will not be shown)
    Path rootName = Paths.get("result");
    MediaFile rootMediaFile = new OtherFile(rootName, mediaFileList);
    root = new TreeItem<>(rootMediaFile);

    treeTableView.setRoot(root);

    TreeTableColumn<MediaFile, String> fileNameCol = new TreeTableColumn<>(language.getString("file.name"));
    fileNameCol.prefWidthProperty().bind(treeTableView.widthProperty().subtract(FILEDATE_COL_WIDTH + 2)); //two extra pixels to avoid scroll bar
    fileNameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("ResultingFilename"));

    TreeTableColumn<MediaFile, String> fileDateColumn = new TreeTableColumn<>(language.getString(FileTableView.MODIFIED));
    fileDateColumn.setPrefWidth(FILEDATE_COL_WIDTH);
    fileDateColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileDate"));

    treeTableView.getColumns().setAll(fileNameCol, fileDateColumn);
    treeTableView.setEditable(false);
    treeTableView.setShowRoot(false);
  }

  /**
   * the master extension is looked up in mediaFileList
   * for all found master files the mediaFiles with the same name (but different extension) are collected under the master file
   */
  private void buildClusteredFileList() {
    String ext = masterExtTextField.getText();
    if (!masterExtTextField.getText().startsWith("."))
      ext = "." + ext;     //for comparing with MediaFiles.getExtension() the leading . is necessary

    //clearMessage old result
    root.getChildren().clear();
    //lookup all MediaFiles with the given Extension

    int index = 0;  //maintain the index of currentFile while running through sortedFileList
    int searchIndex; //run backwards and forwards while in the same filename (but extension)
    boolean found;

    for (MediaFile currentFile : sortedFileList) {
      if (currentFile.getExtension().equalsIgnoreCase(ext)) {
        TreeItem<MediaFile> newElement = new TreeItem<>(currentFile);
        //search backwards in sortedFileList for elements with same name (but extension)
        searchIndex = index - 1;
        found = true;
        while (searchIndex > 0 && found) {
          MediaFile child = sortedFileList.get(searchIndex);
          found = child.isSameResultingNameButExtension(currentFile);
          if (found) newElement.getChildren().add(new TreeItem<>(child));
          searchIndex--;
        }

        //search forwards in sortedFileList for elements with same name (but extension)
        searchIndex = index + 1;
        found = true;
        while (searchIndex < sortedFileList.size() && found) {
          MediaFile child = sortedFileList.get(searchIndex);
          found = child.isSameResultingNameButExtension(currentFile);
          if (found) newElement.getChildren().add(new TreeItem<>(child));
          searchIndex++;
        }
        newElement.setExpanded(true);
        root.getChildren().add(newElement);
      }
      index++;
    }

  }

  /**
   * if the dialog has been closed using OK you can use this method to get the tree
   * representing the date update jobs:
   * Every subtree of this tree has the following form:
   * filename.masterExt
   * filename.ext1
   * filename.ext2
   * ...
   * So the caller can set the filedates in the children-list to the fileDate of the root (masterExt)
   *
   * @return clustered list (tree) with same filenames as the masterExt
   */
  public TreeItem<MediaFile> getResult() {
    return treeTableView.getRoot();
  }

  /**
   * show the dialog
   * If the dialog has been closed with OK-Btn then you can use getResult to get the clustered list (tree) with
   * masterExt-File and their list of files with same name but different extension
   *
   * @return "true" if the dialog has been closed using OK-Btn else false
   */
  public boolean showModal() {
    modalResult_bool = false;

    //initialize textFields if empty
    if (masterExtTextField.getText().isEmpty()) {
      masterExtTextField.setText("mov");   //todo can be put into settings file in the future
    }
    buildClusteredFileList();

    centerAndScaleDialog();
    showAndWait();

    return modalResult_bool;
  }


}
