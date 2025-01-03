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
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;

import static java.util.Objects.isNull;

/**
 * MIT License
 * Copyright (c)2023 kissPhoto
 * <p>
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * TextFieldCells represent a single cell of FileTableView-tables and are generated bei TextFieldCellFactory for every
 * cell of the table
 * <p>
 * Special features are
 * <ul>
 * <li> TextFieldCells which restrict input e.g. to numberInput or valid filenameCharacters are used for editing
 * <li> from one editing line to the next line cursor up/dn can be used without loosing editing mode
 * <li> search results can be marked
 * <li> commitEdit is automatically fired when focus is lost (e.g. when selecting the MediaView
 * <li> the same TextFieldCell can be used for all columns: depended on the edited column the correct editor-Textfield is used
 * </ul>
 * <p>
 * <p/>
 *
 * @author Ingo
 * @version 2023-01-29 Support moving file while in editing mode + improve keeping caret position again
 * @version 2023-01-05 Support delete/undelete file while keeping editing mode + improve keeping caret position
 * @version 2018-11-17 Ctrl-U now copies information from the line above in edit mode, (Shift) TAB moves to next (prev) column, fixed: keeping caret position fixed
 * @since 2016-11-04
 */
public class FileTableTextFieldCell extends TableCell<MediaFile, String> {
  static int lastCaretPosition = 0; //this is to save the caretPosition when moving up/down lines in editMode
  static boolean lastCaretPositionValid = false;  //will become true when the position is stored and remain true until a new TextFieldCell is produced by TextFieldCellFactory in FileTableView (=when new edit starts)
  static RestrictedInputField inputField;
  Boolean escPressed = false;
  FileTableView fileTableView; //link back to the view

  @Override
  public void startEdit() {
    super.startEdit();
    System.out.println("FileTableTextFieldCell.startEdit: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);

    escPressed = false;
    fileTableView = (FileTableView) getTableColumn().getTableView();
    fileTableView.setEditingCell(this);

    createInputField();        //every time a new TextField

    setText(null);
    setGraphic((Node) inputField);
    this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    inputField.requestFocus();  //selection or last Caret Position is restored in input field's focus handler (see createInputField())
  }

  /**
   * restore the CaretPosition which is stored statically e.g.
   * <ul>
   *  <li>after changing the line in edit mode (here a new Cell is generated, but static information can be restored)</li>
   *  <li>after saving: FileTable has no access to the cell, but can use static</li>
   * </ul>
   */
  private void restoreCaretPositionIfValid() {
    System.out.println("FileTableTextFieldCell.restoreCaretPositionIfValid: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);

    if (lastCaretPositionValid) {
      inputField.deselect();
      inputField.positionCaret(lastCaretPosition); //move to last text cursor position in the new row
      System.out.println("FileTableTextFieldCell.restoreCaretPositionIfValid--->Positioned :-) !!!!!!!!!: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
    } else {
      System.out.println("FileTableTextFieldCell.restoreCaretPositionIfValid()--> Select All");
      inputField.selectAll();
    }

  }

  public void positionCaretOrSelect() {
    System.out.println("FileTableTextFieldCell.positionCaretOrSelect(): pos = " + (!isNull(inputField) ? inputField.getCaretPosition() : "-") + " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);

    //select searchResult if available
    if (fileTableView.isSelectSearchResultOnNextStartEdit()) {
      //select Search Result
      MediaFileList.SearchRec searchRec = ((FileTableView) getTableColumn().getTableView()).getSearchRec();
      if (searchRec != null) {
        System.out.println("FileTableTextFieldCell.positionCaretOrSelect().if SelectSearchResult: pos = " + (!isNull(inputField) ? inputField.getCaretPosition() : "-") + " StartPos = " + searchRec.startPos + " EndPos = " + searchRec.endPos + "    lastPos=" + lastCaretPosition);
        inputField.selectRange(searchRec.startPos, searchRec.endPos);
      }
      fileTableView.resetSelectSearchResultOnNextStartEdit(); //consume

      // or select same Caret Position as in last row
    } else {
      System.out.println("FileTableTextFieldCell.positionCaretOrSelect().else: pos = " + (!isNull(inputField) ? inputField.getCaretPosition() : "-") + " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
      restoreCaretPositionIfValid();
    }
    System.out.println("FileTableTextFieldCell.positionCaretOrSelect()--->Ende: pos = " + (!isNull(inputField) ? inputField.getCaretPosition() : "-") + " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
  }

  @Override
  public void commitEdit(String newValue) {
    super.commitEdit(inputField.validate(newValue, getItem()));   //this will fire the change event, sets editing to false  and calls updateItem
    hideInputField();
    fileTableView.setEditingCell(null);
    System.out.println("FileTableTextFieldCell.commitEdit: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
  }

  @Override
  public void cancelEdit() {
    if (!escPressed) { //if just focus lost (e.g. by clicking on different line) then behave like commitEdit (saveEditedValue is called)
      //calling commitEdit in cancelEdit would lead to NullPointerException! Therefore, just call saveEditedValue
      MediaFile mediaFile = getTableRow().getItem();
      fileTableView.saveEditedValue(mediaFile, getTableColumn(), inputField.validate(inputField.getText(), getItem()));
    }
    super.cancelEdit(); //sets editing to false

    setText(getItem());          //reset the displayed text to the original item's (cell's) value
    hideInputField();
    fileTableView.setEditingCell(null);
    System.out.println("FileTableTextFieldCell.cancelEdit: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
  }

  /**
   * Perform all the special actions that are common to CommitEdit and CancelEdit
   */
  private void hideInputField() {
    this.setContentDisplay(ContentDisplay.TEXT_ONLY);   //switch back to string mode
    setGraphic(null);                                   //end graphic mode
    System.out.println("FileTableTextFieldCell.hideInputField: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
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
        if (inputField != null && inputField.getText().isEmpty()) {   //only if possible and necessary
          inputField.setText(getString());
          //restoreCaretPositionIfValid();
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
    System.out.println("FileTableTextFieldCell.updateItem: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);

  }

  /**
   * either a RestrictedTextField or a SeparatorInputField, both Controls are implementing TextFieldComboBox interface
   */
  private void createInputField() {
    //fix the connection to the underlying Model when created
    //and store this connection in the TextField (pass to constructor)
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

    //----Focus Handler: getFocus=> restore Selection/CaretPos, loose Focus=> CommitEdit on Focus lost of FileTableView
    ((Node) inputField).focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        System.out.println("FileTableTextFieldCell.createInputField.focusChanged: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
        Platform.runLater(this::positionCaretOrSelect);
      }else{  //if focus lost (newValue of focussed=false)
        if (isEditing()) commitEdit(inputField.getText());
      }

    });

    ((Node)inputField).setOnInputMethodTextChanged(new EventHandler<InputMethodEvent>() {
      @Override
      public void handle(InputMethodEvent event) {
        System.out.println("FileTableTextFieldCell.createInputField.InputMethodTextChanged: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
      }
    });

    //use EventFilter instead of setOnKeyPressed. Otherwise, Up/Dn would first execute their default behaviour (home/end) before onKeyPressed is fired
    ((Node) inputField).addEventFilter(KeyEvent.KEY_PRESSED, event -> {
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
          if (event.isAltDown() && event.isShiftDown()){
            moveFileUpOrDown(true, editingColumn);
          }else {
            moveCaretUpOrDown(true, editingColumn);
          }
          event.consume();
          break;
        case DOWN:
          if (event.isAltDown() && event.isShiftDown()){
            moveFileUpOrDown(false, editingColumn);
          }else {
            moveCaretUpOrDown(false, editingColumn);
          }
          event.consume();
          break;
        case DELETE:
          if (event.isControlDown() && !event.isShiftDown()){           //ctrl-Delete
            deleteFile(editingColumn);
            event.consume();
          }
          break;
        case Z:
          if (event.isControlDown() && !event.isShiftDown()){           //ctrl-Z = undelete
            undeleteFile(editingColumn);
            event.consume();
          }
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
    });
  }

  /**
   * ctrl-U tries to copy the information from the line above
   * overwriting all information in the current edit-Cell
   * if currently editing the first line (there is nothing above) nothing happens
   */
  private void copyDescriptionDown() {
    MediaFile aboveMediaFile = fileTableView.getAboveMediaFile();

    if (aboveMediaFile != null) { //only if not already in the first line
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
   * commit edit, jump to beginning of next cell and start edit there
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
   * Helper for moving the caret a line up or down, remaining in inline edit mode
   *
   * @param up: true = up, false=down
   */
  private void moveCaretUpOrDown(boolean up, TableColumn<MediaFile, String> editingColumn) {
    lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again in next/prev line
    lastCaretPositionValid = true;
    System.out.println("----------------------------\nFileTableTextFieldCell.moveCaretPositionUpOrDown: pos = " + (!isNull(inputField)?inputField.getCaretPosition():"-") +  " lastPos=" + lastCaretPosition + " valid=" + lastCaretPositionValid);
    if (up)
      fileTableView.showPreviousMedia();
    else //down
      fileTableView.showNextMedia();

    fileTableView.edit(fileTableView.getSelectionModel().getSelectedIndex(), editingColumn); //startEdit will re-set the caretPosition :-)
  }

  /**
   * Helper for deleting the currently editing file
   * moving caret to next file and remain in inline edit mode
   * if there is no next file the previous file is selected
   * if there is also no previous file editing mode is ended (=not restored)
   */
  private void deleteFile(TableColumn<MediaFile, String>editingColumn){
    lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again in next/prev line
    lastCaretPositionValid = true;

    fileTableView.deleteCurrentLineAndMoveToNext();

    fileTableView.edit(fileTableView.getSelectionModel().getSelectedIndex(), editingColumn); //startEdit will re-set the caretPosition :-)
  }

  /**
   * Helper for undeleting the last file while in edit mode
   * moving caret to previous file (=the restored file) and remain in inline edit mode
   * if there was nothing to restore, nothing happens
   */
  private void undeleteFile(TableColumn<MediaFile, String>editingColumn){
    lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again in next/prev line
    lastCaretPositionValid = true;

    fileTableView.undeleteLastDeletedFile();

    fileTableView.edit(fileTableView.getSelectionModel().getSelectedIndex(), editingColumn); //startEdit will re-set the caretPosition :-)
  }

  /**
   * Helper for moving the currently editing file in the FileList up/down
   * @param up if true moving up, if false moving down is performed
   */
  private void moveFileUpOrDown(boolean up, TableColumn<MediaFile, String>editingColumn){
    lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again in next/prev line
    lastCaretPositionValid = true;

    if (up) {
      fileTableView.moveSelectedFilesUp();
    }else {
      fileTableView.moveSelectedFilesDown();
    }

    fileTableView.edit(fileTableView.getSelectionModel().getSelectedIndex(), editingColumn); //startEdit will re-set the caretPosition :-)
  }

  /**
   * overwrite default getString for TextFieldCell
   * @return getItem() or "" if getItem() returns null
   */
  private String getString() {
    if (getItem() == null) return "";
    else return getItem();
  }
} //class TextFieldCell
