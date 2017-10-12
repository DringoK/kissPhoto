package de.kissphoto.view.fileTableHelpers;

import de.kissphoto.model.MediaFile;
import de.kissphoto.model.MediaFileList;
import de.kissphoto.view.FileTableView;
import de.kissphoto.view.inputFields.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * TextFieldCells represent a single cell of FileTableView-tables and are generated bei TextFieldCellFactory for every
 * cell of the table
 * <p>
 * Special features are
 * <ul>
 * <li> TextfieldCells which restrict input e.g. to numberinput or valid filenamecharacters are used for editing
 * <li> from one editing line to the next line cursor up/dn can be used without loosing editing mode
 * <li> search results can be marked
 * <li> commitEdit is automatically fired when focus is lost (e.g. when selecting the MediaView
 * <li> the same TextFieldCell can be used for all columns: depended on the edited column the correct editor-Textfield is used
 * </ul>
 * <p>
 * <p/>
 *
 * @author Ingo
 * @date: 2016-11-04
 * @modified:
 */
class TextFieldCell extends TableCell<MediaFile, String> {
  RestrictedInputField inputField;
  Boolean escPressed = false;
  Boolean stayInEditMode = false; //will be true while moving from one line to the next with cursor-keys up/down

  @Override
  public void startEdit() {
    super.startEdit();
    escPressed = false;
    stayInEditMode = false;

    createInputField();        //every time a new TextField

    inputField.setText(getString());
    positionCaretOrSelect();   //according values in SearchResult


    setText(null);
    setGraphic((Node) inputField);
    this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        inputField.requestFocus();
        //position caret again??? only then it works :-(
        positionCaretOrSelect();

        ((FileTableView) getTableColumn().getTableView()).resetSelectSearchResultOnNextStartEdit(); //consume not before second positioning..
        ((FileTableView) getTableColumn().getTableView()).invalidateLastCaretPosition(); //also consume last caret position
      }
    });
  }

  private void positionCaretOrSelect() {
    //select searchResult if available
    if (((FileTableView) getTableColumn().getTableView()).isSelectSearchResultOnNextStartEdit()) {
      //select Search Result
      MediaFileList.SearchRec searchRec = ((FileTableView) getTableColumn().getTableView()).getSearchRec();
      if (searchRec != null)
        inputField.selectRange(searchRec.startPos, searchRec.endPos);
      else
        inputField.selectAll();
      ((FileTableView) getTableColumn().getTableView()).getPrimaryStage().requestFocus();  //put focus from dialog to main window

      // or select same Caret Position than in last row
    } else if (((FileTableView) getTableColumn().getTableView()).isLastCaretPositionValid()) {
      inputField.deselect();
      inputField.positionCaret(((FileTableView) getTableColumn().getTableView()).getLastCaretPosition()); //move to last text cursor position in the new row

      //select all
    } else {
      //default behavior of TableView is already selectAll...
      //inputField.selectAll();
    }
  }

  /**
   * Perform all the special actions that are common to CommitEdit and CancelEdit
   */
  private void completeCommitAndCancelEdit() {
    setText(getItem());
    this.setContentDisplay(ContentDisplay.TEXT_ONLY);
    setGraphic(null);
    if (!stayInEditMode) ((FileTableView) getTableColumn().getTableView()).setEditable(false);
  }

  @Override
  public void cancelEdit() {
    if (!escPressed) { //if just focus lost (e.g. by clicking on different line) then behave like commitEdit (saveEditedValue is called)
      //calling commitEdit in cancelEdit would lead to NullPointerException! Therefore just call saveEditedValue
      ((FileTableView) getTableColumn().getTableView()).saveEditedValue(getTableRow(), getTableColumn(), inputField.getText());
    }
    super.cancelEdit();

    completeCommitAndCancelEdit();
  }

  @Override
  public void commitEdit(String newValue) {
    super.commitEdit(newValue);
    completeCommitAndCancelEdit();
  }

  /**
   * refresh the cell content
   *
   * @param item  show this string either in Label (non editing) or in editInputField (when editing)
   * @param empty
   */
  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if (empty) {
      setText(null);
      setGraphic(null);
      this.setContentDisplay(ContentDisplay.TEXT_ONLY);
    } else {
      if (isEditing()) {
        if (inputField != null) {
          inputField.setText(getString());
        }
        setText(null);
        setGraphic((Node) inputField);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      } else {
        setText(getString());
        setGraphic(null);
        this.setContentDisplay(ContentDisplay.TEXT_ONLY);
      }
    }
  }

  /**
   * either a RestrictedTextField or a SeparatorInputField, both Controls are implementing TextFieldComboBox interface
   */
  private void createInputField() {
    //fix the connection to the underlying Model when created
    //and store this connection in the Textfield (pass to constructor)
    TableColumn<MediaFile, String> editingColumn = getTableColumn();
    FileTableView fileTableView = (FileTableView) getTableColumn().getTableView();

    if ((editingColumn == fileTableView.getPrefixColumn()) ||
        (editingColumn == fileTableView.getDescriptionColumn()) ||
        (editingColumn == fileTableView.getExtensionColumn())) {
      inputField = new FileNameTextField(getString(), fileTableView.getPrimaryStage());
    } else if ((editingColumn == fileTableView.getCounterColumn())) {
      inputField = new NumberTextField(getString(), fileTableView.getPrimaryStage());
    } else if (editingColumn == fileTableView.getSeparatorColumn()) {
      inputField = new SeparatorInputField(getString());
    } else if (editingColumn == fileTableView.getFileDateColumn()) {
      inputField = new DateTimeTextField(getString(), fileTableView.getPrimaryStage());
    } else {
      inputField = new UnrestrictedTextField(getString(), fileTableView.getPrimaryStage());  //all other columns are freely editable as a default
    }

    inputField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

    //doesn't work??? I wanted to color the highlight-fill to gray when focus lost (e.g. while searching)
    //editInputField.setStyle("-fx-highlight-fill: lightgray");       //see http://docs.oracle.com/javafx/2/css_tutorial/jfxpub-css_tutorial.htm


    // CommitEdit on Focus lost of FileTableView
    ((Node) inputField).focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) { //if focus lost (newValue of focussed=false)
        if (isEditing()) commitEdit(inputField.getText());
      }
      ;
    });

    ((Node) inputField).setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) { //Enter will commit edit and stay in line
          commitEdit(inputField.getText());
          fileTableView.invalidateLastCaretPosition();  //the next selection should not remember the old position of the text cursor
          event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
          escPressed = true;
          fileTableView.invalidateLastCaretPosition(); //the next selection should not remember the old position of the text cursor
          //normal processing of ESC (CancelEdit) --> no event.consume()
        } else if (event.getCode() == KeyCode.UP) {
          moveCaretUpOrDown(true);
          event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
          moveCaretUpOrDown(false);
          event.consume();
        }
      }

      /**
       * Helper for moving the caret a line up or down
       *
       * @param up: true = up, false=down
       */
      private void moveCaretUpOrDown(boolean up) {

        //selectAbove/BelowCell() does not work as expected when running over borders of viewport/flow
        int currentLineEdited = fileTableView.getSelectionModel().getSelectedIndex();

        stayInEditMode = true; //for commitEdit of old row
        if (up) {
          if (currentLineEdited > 0) {
            ((FileTableView) getTableColumn().getTableView()).setLastCaretPosition(inputField.getCaretPosition());
            commitEdit(inputField.getText());
            currentLineEdited--;
          }
        } else {    //down
          if (currentLineEdited < fileTableView.getRowCount() - 1) {
            ((FileTableView) getTableColumn().getTableView()).setLastCaretPosition(inputField.getCaretPosition());
            commitEdit(inputField.getText());
            currentLineEdited++;
          }
        }
        stayInEditMode = false; 

        fileTableView.scrollViewportToIndex(currentLineEdited);
        fileTableView.getSelectionModel().focus(currentLineEdited);
        fileTableView.getSelectionModel().clearAndSelect(currentLineEdited, editingColumn);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            fileTableView.edit(fileTableView.getSelectionModel().getSelectedIndex(), editingColumn);
          }
        });
      }
    });
  }

  private String getString() {
    if (getItem() == null) return "";
    else return getItem();
  }
} //class TextFieldCell
