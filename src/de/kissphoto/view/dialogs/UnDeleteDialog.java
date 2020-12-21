package de.kissphoto.view.dialogs;

import de.kissphoto.model.MediaFile;
import de.kissphoto.view.MediaContentView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static de.kissphoto.KissPhoto.language;

/**
 * This is the Dialog Window for UnDeletion
 *
 * @author Dr. Ingo kreuz
 * @since 2012-10-05
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 * @version 2014-06-16 multi screen support: center on main window instead of main screen
 * @version 2014-05-02 (I18Support)
 */
public class UnDeleteDialog extends KissDialog {
  public static final int NONE_BTN = 0;               //no button was pressed: MessageBox left by [x] of the window
  public static final int UNDELETE_SELECTION_BTN = 1;   //unDeleteBtn has closed the dialog. The selection in the dialog shall be unDeleted in mediaFileList
  public static final int CANCEL_BTN = 2;

  final ListView<MediaFile> listView = new ListView<>();
  final MediaContentView mediaContentView = new MediaContentView(this);
  //no fileTableViewConnection set, i.e. no moving up/down (=no prev/next media) when MediaContentView would have the focus

  private final Button unDeleteSelectionBtn;
  private final Button cancelBtn;

  public UnDeleteDialog(Stage owner) {
    super(owner);

    setTitle(language.getString("kissphoto.un.delete.select.deleted.files.for.recovery"));
    setHeight(600);
    setWidth(1000);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.WHITE);  //1,1 --> use min Size as set just before
    setScene(scene);

    VBox rootArea = new VBox();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());

    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    listView.setEditable(false);
    //install keylisteners for ESC and ENTER for listView because ListView would consume them
    listView.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<>() {
      @Override
      public void handle(KeyEvent event) {
        //no support for toggeling AutoPlay
        //no default
        switch (event.getCode()) {
          case ESCAPE -> cancelBtn.fire();
          case ENTER -> unDeleteSelectionBtn.fire();
          case P -> mediaContentView.getPlayerViewer().getPlayerControls().togglePlayPause();
          case S -> mediaContentView.getPlayerViewer().stop();
        }
      }
    });

    SplitPane splitPane = new SplitPane();
    splitPane.prefWidthProperty().bind(scene.widthProperty());
    splitPane.prefHeightProperty().bind(scene.heightProperty());
    splitPane.setDividerPosition(0, 0.5);
    splitPane.getItems().addAll(listView, mediaContentView);

    HBox buttonBox = new HBox();
    buttonBox.setSpacing(7.0);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(7, 7, 7, 7));

    unDeleteSelectionBtn = new Button(language.getString("un.delete.selection"));
    unDeleteSelectionBtn.setDefaultButton(true);
    unDeleteSelectionBtn.setFocusTraversable(true);
    unDeleteSelectionBtn.setOnAction(actionEvent -> {
      modalResult = UNDELETE_SELECTION_BTN;
      close();
    });

    cancelBtn = new Button(KissDialog.CANCEL_LABEL);
    cancelBtn.setCancelButton(true);
    cancelBtn.setFocusTraversable(true);
    cancelBtn.setOnAction(actionEvent -> {
      modalResult = CANCEL_BTN;
      close();
    });
    buttonBox.getChildren().addAll(unDeleteSelectionBtn, cancelBtn);

    rootArea.getChildren().addAll(splitPane, buttonBox);
    root.getChildren().add(rootArea);
  }

  /**
   * show the files which have been marked for deletion in a list
   * show the modal dialog
   * <p/>
   * if deletedFileList is empty nothing will happen and NONE_BTN is returned
   *
   * @param deletedFileList the list filesToBeDeleted list from MediaFileList
   * @return the button-constant which was used to close the dialog
   */
  public int showModal(ObservableList<MediaFile> deletedFileList) {
    modalResult = NONE_BTN; //closing without using a button as default

    if (deletedFileList.size() > 0) {
      FXCollections.sort(deletedFileList);
      listView.setItems(deletedFileList);
      mediaContentView.setMedia(null, null);

      //Install Selection Listener to show selected media
      SelectedLineNumberChangeListener selectedLineNumberChangeListener = new SelectedLineNumberChangeListener(mediaContentView, deletedFileList);
      listView.getSelectionModel().selectedIndexProperty().addListener(selectedLineNumberChangeListener);
      listView.getSelectionModel().selectFirst();

      centerAndScaleDialog();
      showAndWait();

      //uninstall listener to prevent from funny effects, when changing the passed list externally later
      listView.getSelectionModel().selectedIndexProperty().removeListener(selectedLineNumberChangeListener);
    }

    return modalResult;
  }

  /**
   * after the modal dialog has been closed the caller should call this method
   * to avoid any uncontrolled background playing
   */
  public void stopPlayers() {
    mediaContentView.getPlayerViewer().resetPlayer();
  }

  /**
   * get resulting value after closing the dialog
   *
   * @return start field value
   */
  public ObservableList<MediaFile> getFilesToUndelete() {
    return listView.getSelectionModel().getSelectedItems();
  }

  /**
   * clean up the dialog:
   * clearMessage the internal list of the dialog
   * Use this as soon as undeletion is completed
   */
  public void cleanUp() {
    listView.setItems(null);
  }

  private static class SelectedLineNumberChangeListener implements ChangeListener<Number> {
    private final MediaContentView mediaContentView;
    private final ObservableList<MediaFile> mediaFileList;

    public SelectedLineNumberChangeListener(MediaContentView mediaContentView, ObservableList<MediaFile> mediaFileList) {
      this.mediaContentView = mediaContentView;
      this.mediaFileList = mediaFileList;
    }

    @Override
    public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
      if (newNumber.intValue() >= 0) { //only if selection is valid
        mediaContentView.setMedia(mediaFileList.get(newNumber.intValue()), null);
      }
    }
  }

}
