package dringo.kissPhoto.view.fileTableHelpers;

import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileList;
import dringo.kissPhoto.view.FileTableView;
import dringo.kissPhoto.view.inputFields.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyEvent;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
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
 * @since 2016-11-04
 * @version 2022-01-08 improvement getting focus after start edit if double-clicked on empty part of table (without text)
 * @version 2021-01-17 support saving the currently editing line, simplify commitEdit and cancelEdit
 * @version 2020-12-20 lambda expressions for event handlers
 * @version 2018-11-17 Ctrl-U now copies information from the line above in edit mode, (Shift) TAB moves to next (prev) column, fixed: keeping caret position fixed
 */
public class FileTableTextFieldCell extends TableCell<MediaFile, String> {
  static int lastCaretPosition = 0; //this is to save the caretPosition when moving up/down lines in editMode
  static boolean lastCaretPositionValid = false;
  static RestrictedInputField inputField;
  Boolean escPressed = false;
  FileTableView fileTableView; //link back to the view

  @Override
  public void startEdit() {
    super.startEdit();
    escPressed = false;
    fileTableView = (FileTableView) getTableColumn().getTableView();
    fileTableView.setEditingCell(this);

    createInputField();        //every time a new TextField

    setText(null);
    setGraphic((Node) inputField);
    this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    Platform.runLater(() -> {
      inputField.requestFocus();
      positionCaretOrSelect(); //works only, if it has the focus ;_)

      fileTableView.resetSelectSearchResultOnNextStartEdit(); //consume not before second positioning..
      lastCaretPositionValid = false; //also consume last caret position
    });
  }

  /**
   * restore the CaretPostion which is stored statically e.g.
   * <ul>
   *  <li>after changing the line in edit mode (here a new Cell is generated, but static information can be restored)</li>
   *  <li>after saving: FileTable has no access to the cell, but can use static</li>
   * </ul>
   */
  private void restoreCaretPositionIfValid() {
    if (lastCaretPositionValid) {
      inputField.deselect();
      inputField.positionCaret(lastCaretPosition); //move to last text cursor position in the new row
    }else{
      inputField.selectAll();
    }
  }
  public void positionCaretOrSelect() {
    //select searchResult if available
    if (fileTableView.isSelectSearchResultOnNextStartEdit()) {
      //select Search Result
      MediaFileList.SearchRec searchRec = ((FileTableView) getTableColumn().getTableView()).getSearchRec();
      if (searchRec != null)
        inputField.selectRange(searchRec.startPos, searchRec.endPos);
      else
        inputField.selectAll();
      ((FileTableView) getTableColumn().getTableView()).getPrimaryStage().requestFocus();  //put focus from dialog to main window

      // or select same Caret Position as in last row
    } else {
      restoreCaretPositionIfValid();
    }
  }

  @Override
  public void commitEdit(String newValue) {
    super.commitEdit(inputField.validate(newValue, getItem()));   //this will fire the change event, sets editing to false  and calls updateItem
    hideInputField();
    fileTableView.setEditingCell(null);
  }
  @Override
  public void cancelEdit() {
    if (!escPressed) { //if just focus lost (e.g. by clicking on different line) then behave like commitEdit (saveEditedValue is called)
      //calling commitEdit in cancelEdit would lead to NullPointerException! Therefore just call saveEditedValue
      MediaFile mediaFile = getTableRow().getItem();
      fileTableView.saveEditedValue(mediaFile, getTableColumn(), inputField.validate(inputField.getText(),getItem()));
    }
    super.cancelEdit(); //sets editing to false

    setText(getItem());          //reset the displayed text to the original item's (cell's) value
    hideInputField();
    fileTableView.setEditingCell(null);
  }

  /**
   * Perform all the special actions that are common to CommitEdit and CancelEdit
   */
  private void hideInputField() {
    this.setContentDisplay(ContentDisplay.TEXT_ONLY);   //switch back to string mode
    setGraphic(null);                                   //end graphic mode
  }

  /**
   * refresh the cell content: synchronize inputField or cell's text with the item String
   *
   * @param item  show this string either in Label (non editing) or in editInputField (when editing)
   * @param empty if true the cell is cleared (null)
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
          restoreCaretPositionIfValid();
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

    // CommitEdit on Focus lost of FileTableView
    ((Node) inputField).focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) { //if focus lost (newValue of focussed=false)
        if (isEditing()) commitEdit(inputField.getText());
      }
    });

    //use EventFilter instead of setOnKeyPressed. Otherwise Up/Dn would first execute their default behaviour (home/end) before onKeyPressed is fired
    ((Node) inputField).addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<>() {
      @Override
      public void handle(KeyEvent event) {
        switch (event.getCode()) {
          case ENTER: //Enter will commit edit and stay in line
            commitEdit(inputField.getText());
            lastCaretPositionValid = false; //the next selection should not remember the old position of the text cursor
            event.consume();
            break;
          case ESCAPE:
            escPressed = true;
            lastCaretPositionValid = false; //the next selection should not remember the old position of the text cursor
            //normal processing of ESC (CancelEdit) --> no event.consume()
            break;
          case TAB:
            if (event.isShiftDown())
              selectPrevColumn();
            else
              selectNextColumn();
            break;
          case UP:
            moveCaretUpOrDown(true);
            event.consume();
            break;
          case DOWN:
            moveCaretUpOrDown(false);
            event.consume();
            break;
          case S:
            if (event.isControlDown()) { //ctrl-S (saving) will take over current changes
              fileTableView.saveEditedValue(fileTableView.getSelectionModel().getSelectedItem(), getTableColumn(), inputField.getText());
              lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again when saving is complete
              lastCaretPositionValid = true;
              //but does not consume the event, so that FileTable can initiate saving afterwards
            }
            break;
          case U:
            if (event.isControlDown()) {
              copyDescriptionDown();
              inputField.deselect();
              inputField.positionCaret(inputField.getLength()); //[end]: move caret to the end
            }
            break;
          case F3: //find next (menu not accessible while cell has focus)
            fileTableView.findNext();
            break;
        }
      }

      /**
       * ctrl-U tries to copy the information from the line above
       * overwriting all information in the current edit-Cell
       * if currently editing the first line (there is nothing above) nothing happens
       */
      private void copyDescriptionDown() {
        MediaFile aboveMediaFile = fileTableView.getAboveMediaFile();

        if (aboveMediaFile != null) { //only if not alread in the first line
          if (getTableColumn() == fileTableView.getPrefixColumn()) {
            inputField.setText(aboveMediaFile.getPrefix());
          } else if (getTableColumn() == fileTableView.getCounterColumn()) {
            inputField.setText(aboveMediaFile.getCounter());
          } else if (getTableColumn() == fileTableView.getDescriptionColumn()) {
            inputField.setText(aboveMediaFile.getDescription());
          } else if (getTableColumn() == fileTableView.getExtensionColumn()) {
            inputField.setText(aboveMediaFile.getExtension());
          } else if (getTableColumn() == fileTableView.getFileDateColumn()) {
            inputField.setText(aboveMediaFile.getModifiedDate());
          }
        }

      }

      /**
       * commitedit, jump to beginning of next cell and start edit there
       */
      private void selectNextColumn() {
        int currentLine = fileTableView.getSelectionModel().getSelectedIndex();

        commitEdit(inputField.getText());
        lastCaretPosition = 0; //place caret at the beginning of next column
        lastCaretPositionValid = true;

        TableColumn<MediaFile, String> nextColumn;
        if (getTableColumn() == fileTableView.getPrefixColumn()) {
          nextColumn = fileTableView.getCounterColumn();
        } else if (getTableColumn() == fileTableView.getCounterColumn()) {
          nextColumn = fileTableView.getSeparatorColumn();
        } else if (getTableColumn() == fileTableView.getSeparatorColumn()) {
          nextColumn = fileTableView.getDescriptionColumn();
        } else if (getTableColumn() == fileTableView.getDescriptionColumn()) {
          nextColumn = fileTableView.getExtensionColumn();
        } else if (getTableColumn() == fileTableView.getExtensionColumn()) {
          nextColumn = fileTableView.getFileDateColumn();
        } else if (getTableColumn() == fileTableView.getFileDateColumn()) {
          nextColumn = fileTableView.getPrefixColumn();  //circle: jump from last to first column
        } else {
          //default
          nextColumn = fileTableView.getDescriptionColumn();
        }

        getTableColumn().getTableView().setEditable(true); //CommitEdit for the previous cell had ended edit mode
        fileTableView.edit(currentLine, nextColumn);
      }

      /**
       * commit edit, jump to end of previous cell and start edit there
       */
      private void selectPrevColumn() {
        int currentLine = fileTableView.getSelectionModel().getSelectedIndex();

        commitEdit(inputField.getText());
        lastCaretPosition = Integer.MAX_VALUE; //place caret at the end of previous column
        lastCaretPositionValid = true;

        TableColumn<MediaFile, String> prevColumn;
        if (getTableColumn() == fileTableView.getPrefixColumn()) {
          prevColumn = fileTableView.getFileDateColumn(); //circle: jump from first to last column
        } else if (getTableColumn() == fileTableView.getCounterColumn()) {
          prevColumn = fileTableView.getPrefixColumn();
        } else if (getTableColumn() == fileTableView.getSeparatorColumn()) {
          prevColumn = fileTableView.getCounterColumn();
        } else if (getTableColumn() == fileTableView.getDescriptionColumn()) {
          prevColumn = fileTableView.getSeparatorColumn();
        } else if (getTableColumn() == fileTableView.getExtensionColumn()) {
          prevColumn = fileTableView.getDescriptionColumn();
        } else if (getTableColumn() == fileTableView.getFileDateColumn()) {
          prevColumn = fileTableView.getExtensionColumn();
        } else {
          //default
          prevColumn = fileTableView.getDescriptionColumn();
        }
        getTableColumn().getTableView().setEditable(true); //CommitEdit for the previous cell had ended edit mode
        fileTableView.edit(currentLine, prevColumn);
      }

      /**
       * Helper for moving the caret a line up or down
       *
       * @param up: true = up, false=down
       */
      private void moveCaretUpOrDown(boolean up) {

        //selectAbove/BelowCell() does not work as expected when running over borders of viewport/flow
        int currentLineEdited = fileTableView.getSelectionModel().getSelectedIndex();

        lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again in next/prev line
        lastCaretPositionValid = true;
        if (up) {
          if (currentLineEdited > 0) {
            commitEdit(inputField.getText());
            currentLineEdited--;
          }
        } else {    //down
          if (currentLineEdited < fileTableView.getRowCount() - 1) {
            commitEdit(inputField.getText());
            currentLineEdited++;
          }
        }
        getTableColumn().getTableView().setEditable(true); //CommitEdit for the previous cell had ended edit mode

        if (up)
          fileTableView.scrollViewportToIndex(currentLineEdited, FileTableView.Alignment.TOP);
        else
          fileTableView.scrollViewportToIndex(currentLineEdited, FileTableView.Alignment.BOTTOM);

        fileTableView.getSelectionModel().focus(currentLineEdited);
        fileTableView.getSelectionModel().clearAndSelect(currentLineEdited, editingColumn);

        fileTableView.edit(fileTableView.getSelectionModel().getSelectedIndex(), editingColumn);
      }
    });
  }

  private String getString() {
    if (getItem() == null) return "";
    else return getItem();
  }
} //class TextFieldCell
