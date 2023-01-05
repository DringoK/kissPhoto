package dringo.kissPhoto.view.dialogs;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.FileTableView;
import dringo.kissPhoto.view.inputFields.FileNameTextField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.MessageFormat;

/**
 * MIT License
 * Copyright (c)2023 kissPhoto
 *
 * This is the Dialog Window for Find and Replace
 *
 * @author Ingo Kreuz
 * @since 2014-05-03
 * @version 2023-01-05 height adjusted
 * @version 2022-09-04 clean up primaryStage parameter
 * @version 2021-01-09 own statusBar implementation to avoid show statistics. FindNext ShortCut F3 support here and from fileTable, TextFieldCell and MainMenu
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 * @version 2016-11-04 indicate search mode: in all or in selected lines only
 * @version 2014-06-16 multi screen support: center on main window instead of main screen
 */
public class FindReplaceDialog extends KissDialog {
  final static String findCaption = KissPhoto.language.getString("find");
  final static String replaceCaption = KissPhoto.language.getString("replace");
  final static String findNextCaption = KissPhoto.language.getString("find.next");


  //graphical elements that are read or changed during searching
  final TextField findTextField = new TextField();
  final FileNameTextField replaceTextField = new FileNameTextField(this); //limit characters to valid characters for fileNames (todo counter and date not yet covered)
  final Button findBtn = new Button(findCaption);
  final Button replaceBtn = new Button(replaceCaption);
  final Button replaceAllBtn = new Button(KissPhoto.language.getString("replace.all"));
  final Label searchSelectedLabel = new Label();
  final Text statusMessage = new Text(); //local statusBar in the Dialog to show search-Results

  boolean findFirstMode = true; //findButton is FindFirst in the beginning (false=FindNextMode)
  boolean found = true;
  boolean searchSelectionMode = false; //true=search all selected lines, false=search from current line
  boolean startedSearchFromFirstLine = false; //if true at the end of a search/replace all without selection user will be asked if to continue from the start

  int firstCounter = 0; //if replaceAll is continued from the beginning the first counter-result is added to the final result
  //link to mediaFileList window for getting selection etc
  final FileTableView fileTableView;

  /**
   * constructor
   * @param primaryStage   link to main window (necessary because fileTableView.getScene.getWindow() is null during start up), when binding showProperty in MainMenuBar
   * @param fileTableView  link to fileTableView to search in
   */
  public FindReplaceDialog(Stage primaryStage, final FileTableView fileTableView) {
    super(primaryStage);
    initModality(Modality.NONE); //this is the only non-modal dialog

    this.fileTableView = fileTableView;

    setTitle(KissPhoto.language.getString("kissphoto.findreplace"));

    setHeight(250);
    setWidth(450);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.WHITE);  //1,1 --> use min Size as set just before
    setScene(scene);

    VBox rootArea = new VBox();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());
    rootArea.setOnKeyPressed(keyEvent->{
      if (keyEvent.getCode() == KeyCode.F3){ //support FindNext KeyCombination also if Dialog has the Focus
        handleFindFirst_FindNext();
      }
    });

    GridPane gridPane = new GridPane();
    gridPane.setHgap(5);
    gridPane.setVgap(2);
    gridPane.prefHeightProperty().bind(scene.heightProperty());
    gridPane.prefWidthProperty().bind(scene.widthProperty());
    gridPane.setAlignment(Pos.CENTER);
    Insets mainPadding = new Insets(7, 7, 7, 7);
    gridPane.setPadding(mainPadding);

    final double LABEL_COL_WIDTH = 100;
    Label findLabel = new Label(findCaption);
    gridPane.add(findLabel, 0, 0);      //column, row
    findTextField.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH));
    findTextField.setOnKeyReleased(keyEvent -> handleFindTextFieldChanged(keyEvent));
    gridPane.add(findTextField, 1, 0);

    Label replaceLabel = new Label(replaceCaption);
    gridPane.add(replaceLabel, 0, 1);
    gridPane.add(replaceTextField, 1, 1);

    gridPane.add(searchSelectedLabel, 1, 2);

    HBox buttonBox = new HBox();
    buttonBox.setSpacing(7.0);
    buttonBox.setPadding(mainPadding);
    buttonBox.setAlignment(Pos.CENTER);

    //--- find first/find next
    findBtn.setDefaultButton(true);
    findBtn.setOnAction(actionEvent -> handleFindFirst_FindNext());

    //--- replace
    replaceBtn.setDisable(true);  //not possible until first find
    replaceBtn.setOnAction(actionEvent -> handleReplace());

    //--- replace all
    replaceAllBtn.setOnAction(actionEvent -> handleReplaceAll());

    //--- close
    Button closeBtn = new Button(KissPhoto.language.getString("close"));
    closeBtn.setCancelButton(true);
    closeBtn.setOnAction(actionEvent -> close());
    buttonBox.getChildren().addAll(findBtn, replaceBtn, replaceAllBtn, closeBtn);

    //--- statusMessage
    HBox statusBar = new HBox();
    statusBar.setPadding(mainPadding);
    statusBar.prefWidthProperty().bind(rootArea.widthProperty());
    statusBar.getChildren().add(statusMessage);

    rootArea.getChildren().addAll(gridPane, buttonBox, statusBar);
    root.getChildren().add(rootArea);

  }

  public void showWarningStatus(String message){
    statusMessage.setText(message);
    statusMessage.setFill(Color.BROWN);
  }
  public void showStatus(String message){
    statusMessage.setText(message);
    statusMessage.setFill(Color.BLUE);
  }

  //------------------------ react on Buttons and input ----------------------------------------

  /**
   * executed every time the content of the find textfield has changed
   * to start a new search
   */
  private void handleFindTextFieldChanged(KeyEvent keyEvent) {
    fileTableView.closeSearchAndRestoreSelection();
    findFirstMode = true; //when textField has been changed always use find First
    enableValidButtons();
    keyEvent.consume();
  }

  /**
   * executed when find-button has been pressed
   */
  public void handleFindFirst_FindNext() {
    if (findBtn.isDisabled()) return; //ignore call if button is disabled

    if (findFirstMode) {
      showSelectionInfo();   //update status bar of search dialog

      startedSearchFromFirstLine = fileTableView.isFirstLineSelectedOnly();
      found = fileTableView.findFirst(findTextField.getText());
    } else {
      found = fileTableView.findNext(findTextField.getText());
    }

    findFirstMode = !found;    //stay in findFirst Mode if nothing was found
    enableValidButtons();
    //repaint();
    showSearchResultInStatusBar();

    if (!found) {
      fileTableView.closeSearchAndRestoreSelection();
      if (ifAtTheEndAskIfContinueFromFirstLine()) handleFindFirst_FindNext();
    }
  }

  /**
   * executed when replace-btn has been pressed
   */
  private void handleReplace() {
    found = fileTableView.replaceAndFindNext(findTextField.getText(), replaceTextField.getText());

    findFirstMode = !found;    //stay in findFirst Mode if nothing was found
    enableValidButtons();
    //repaint();
    showSearchResultInStatusBar();

    if (!found) {
      fileTableView.closeSearchAndRestoreSelection();
      if (ifAtTheEndAskIfContinueFromFirstLine()) handleFindFirst_FindNext();
    }
  }

  /**
   * executed when replaceAll-Button has been pressed
   */
  private void handleReplaceAll() {
    if (findFirstMode) startedSearchFromFirstLine = fileTableView.isFirstLineSelectedOnly();

    int counter = fileTableView.replaceAll(findTextField.getText(), replaceTextField.getText(), findFirstMode);
    findFirstMode = true;
    fileTableView.closeSearchAndRestoreSelection();
    enableValidButtons();
    //repaint();

    counter = counter + firstCounter;  //if an old result exists then add it
    if (counter == 0)
      showWarningStatus(KissPhoto.language.getString(FileTableView.NOTHING_FOUND));
    else if (counter == 1)
      showStatus(KissPhoto.language.getString("one.occurrence.has.been.replaced"));
    else
      showStatus(MessageFormat.format(KissPhoto.language.getString("0.occurrences.have.been.replaced"), Integer.toString(counter)));

    if (ifAtTheEndAskIfContinueFromFirstLine()) {
      firstCounter = counter;  //save old result so it can be added to the new result
      handleReplaceAll();
      firstCounter = 0; //reset for further searches
    }
  }

  //********************************** helpers *********************************

  /**
   * ask user
   * if "yes": First Line is selected and true is returned to indicate that previous search/replace shall continue
   * ask only if user has not already started search from the first line or (same effect)
   * the last continue search has moved selection to first line
   * and only if in search from current line mode (not searching in selection)
   *
   * @return true if search/replace shall continue else false
   */
  private boolean ifAtTheEndAskIfContinueFromFirstLine() {
    if (!searchSelectionMode && !startedSearchFromFirstLine) {
      int result = new MessageBox((Stage) getOwner(), KissPhoto.language.getString("continue.search.from.beginning.of.table"),
          MessageBox.YES_BTN + MessageBox.NO_BTN,
          KissPhoto.language.getString("continue.search")).showModal();
      if (result == MessageBox.YES_BTN) {
        fileTableView.selectFirstLine();
        return true;
      } else
        return false;
    }
    return false;
  }

  private void showSearchResultInStatusBar() {
    //show result in statusBar
    if (found)
      showStatus(KissPhoto.language.getString("found"));
    else
      showWarningStatus(KissPhoto.language.getString(FileTableView.NOTHING_FOUND));
  }

  /**
   * update searchSelectionMode
   * update caption of searchSelectedLabel: either searching in...lines or ..in all lines
   * color of text is changed additionally
   */
  private void showSelectionInfo() {
    int selectedLines = fileTableView.getSelectionModel().getSelectedItems().size();
    searchSelectionMode = (selectedLines > 1);
    if (searchSelectionMode) {
      searchSelectedLabel.setText(MessageFormat.format(KissPhoto.language.getString("searching.in.0.selected.lines"), selectedLines));
      searchSelectedLabel.setTextFill(Color.BLUE);
    } else {
      searchSelectedLabel.setText(KissPhoto.language.getString("searching.from.current.line"));
      searchSelectedLabel.setTextFill(Color.GREEN);
    }
  }

  /**
   * display findFirstMode: Enabling of replaceBtn und Caption of findBtn
   */
  public void enableValidButtons() {
    replaceBtn.setDisable(findFirstMode);
    if (findFirstMode) {
      findBtn.setText(findCaption);
    } else {
      findBtn.setText(findNextCaption);
    }

    findBtn.setDisable(findTextField.getText().isEmpty());
    replaceAllBtn.setDisable((findTextField.getText().isEmpty()));
  }

  /**
   * initialize and
   * show the modal dialog
   */
  public void showModal() {
    findFirstMode = true;
    enableValidButtons();

    showSelectionInfo();

    findTextField.requestFocus();

    centerAndScaleDialog();
    showAndWait();

    //if there was a selection with size>0 then restore the selection when closing the dialog window
    fileTableView.closeSearchAndRestoreSelection();
  }
}
