package de.kissphoto.view.dialogs;

import de.kissphoto.view.FileTableView;
import de.kissphoto.view.StatusBar;
import de.kissphoto.view.inputFields.FileNameTextField;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.MessageFormat;

import static de.kissphoto.view.FileTableView.NOTHING_FOUND;

/**
 * This is the Dialog Window for Find and Replace
 *
 * @author: Ingo Kreuz
 * @date: 2014-05-03
 * @modified: 2014-06-16 multi screen support: center on main window instead of main screen
 * @modified: 2016-11-04 indicate search mode: in all or in selected lines only
 */
public class FindReplaceDialog extends KissDialog {
  final static String findCaption = language.getString("find");
  final static String replaceCaption = language.getString("replace");
  final static String findNextCaption = language.getString("find.next");


  //graphical elements that are read or changed during searching
  final TextField findTextField = new TextField();
  final FileNameTextField replaceTextField = new FileNameTextField(this); //limit characters to valid characters for fileNames (todo counter and date not yet covered)
  final Button findBtn = new Button(findCaption);
  final Button replaceBtn = new Button(replaceCaption);
  final Button replaceAllBtn = new Button(language.getString("replace.all"));
  Label searchSelectedLabel = new Label();
  StatusBar statusBar = new StatusBar(); //local statusBar in the Dialog to show search-Results

  boolean findFirstMode = true; //findButton is FindFirst in the beginnen (false=FindNextMode)
  boolean found = true;
  boolean searchSelectionMode = false; //true=search all selected lines, false=search from current line
  boolean startedSearchFromFirstLine = false; //if true at the end of a search/replace all without selection user will be asked if to continue from the start

  int firstCounter = 0; //if replaceAll is continued from the beginning the first counter-result is added to the final result
  //link to parent window for getting selection etc
  FileTableView fileTableView;

  public FindReplaceDialog(Stage owner, final FileTableView fileTableView) {
    super(owner);
    initModality(Modality.NONE); //this is the only non-modal dialog

    this.fileTableView = fileTableView;

    setTitle(language.getString("kissphoto.findreplace"));

    setHeight(200);
    setWidth(450);
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

    final double LABEL_COL_WIDTH = 100;
    Label findLabel = new Label(findCaption);
    gridPane.add(findLabel, 0, 0);      //column, row
    findTextField.prefWidthProperty().bind(gridPane.widthProperty().subtract(LABEL_COL_WIDTH));
    findTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        handleFindTextFieldChanged();
      }
    });
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
    findBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        handleFindFirst_FindNext();
      }
    });

    //--- replace
    replaceBtn.setDisable(true);  //not possible until first find
    replaceBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        handleReplace();
      }
    });

    //--- replace all
    replaceAllBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        handleReplaceAll();
      }
    });

    //--- close
    Button closeBtn = new Button(language.getString("close"));
    closeBtn.setCancelButton(true);
    closeBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        close();
      }
    });
    buttonBox.getChildren().addAll(findBtn, replaceBtn, replaceAllBtn, closeBtn);

    rootArea.getChildren().addAll(gridPane, buttonBox, statusBar);
    root.getChildren().add(rootArea);
  }

  //------------------------ react on Buttons and input ----------------------------------------

  /**
   * executed every time the content of the find textfield has changed
   * to start a new search
   */
  private void handleFindTextFieldChanged() {
    fileTableView.closeSearchAndRestoreSelection();
    findFirstMode = true; //when textField has been changed always use find First
    enableValidButtons();
  }

  /**
   * executed when find-button has been pressed
   */
  private void handleFindFirst_FindNext() {
    if (findFirstMode) {
      showSelectionInfo();

      startedSearchFromFirstLine = fileTableView.isFirstLineSelectedOnly();
      found = fileTableView.findFirst(findTextField.getText());
    } else {
      found = fileTableView.findNext(findTextField.getText());
    }

    findFirstMode = !found;    //stay in findFirst Mode if nothing was found
    enableValidButtons();
    repaint();
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
    repaint();
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
    repaint();

    counter = counter + firstCounter;  //if an old result exists then add it
    if (counter == 0)
      statusBar.showError(language.getString(NOTHING_FOUND));
    else if (counter == 1)
      statusBar.showMessage(language.getString("one.occurrence.has.been.replaced"));
    else
      statusBar.showMessage(MessageFormat.format(language.getString("0.occurrences.have.been.replaced"), Integer.toString(counter)));

    if (ifAtTheEndAskIfContinueFromFirstLine()) {
      firstCounter = counter;  //save old result so it can be added to the new result
      handleReplaceAll();
      firstCounter = 0; //reset for further searches
    }
    ;
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
      int result = new MessageBox((Stage) getOwner(), language.getString("continue.search.from.beginning.of.table"),
          MessageBox.YES_BTN + MessageBox.NO_BTN,
          language.getString("continue.search")).showModal();
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
      statusBar.showMessage(language.getString("found"));
    else
      statusBar.showError(language.getString(NOTHING_FOUND));
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
      searchSelectedLabel.setText(MessageFormat.format(language.getString("searching.in.0.selected.lines"), selectedLines));
      searchSelectedLabel.setTextFill(Color.BLUE);
    } else {
      searchSelectedLabel.setText(language.getString("searching.from.current.line"));
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

    centerOnOwner();
    toFront();
    repaint();
    showAndWait();

    //if there was a selection with size>0 then restore the selection when closing the dialog window
    fileTableView.closeSearchAndRestoreSelection();
  }
}
