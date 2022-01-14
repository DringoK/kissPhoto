package dringo.kissPhoto.view.MetaInfoEditableTagsViewHelper;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import dringo.kissPhoto.model.Metadata.EditableItem.EditableTagItems.*;
import dringo.kissPhoto.view.MetaInfoEditableTagsView;
import dringo.kissPhoto.view.inputFields.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

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
 * @since 2021-11-15
 * @version 2022-01-07 first working version
 */
public class EditableTagTextFieldCell extends TreeTableCell<EditableMetaInfoItem, String> {
  static int lastCaretPosition = 0; //this is to save the caretPosition when moving up/down lines in editMode
  static boolean lastCaretPositionValid = false;
  static RestrictedInputField inputField;
  Boolean escPressed = false;

  MetaInfoEditableTagsView treeTableView;

  @Override
  public void startEdit() {
    //only start editing for Tags (not for directories)
    if (!(getTableRow().getItem() instanceof EditableTagItem)){
      return;
    }

    super.startEdit();
    escPressed = false;

    createInputField();        //every time a new TextField

    inputField.setText(getString());


    setText(null);
    setGraphic((Node) inputField);
    this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    Platform.runLater(()->inputField.requestFocus());
    restoreCaretPositionIfValid();

    lastCaretPositionValid = false; //also consume last caret position
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

  @Override
  public void commitEdit(String newValue) {
    super.commitEdit(newValue);   //this will fire the change event, sets editing to false  and calls updateItem
    hideInputField();
  }
  @Override
  public void cancelEdit() {
    if (!escPressed) { //if just focus lost (e.g. by clicking on different line) then behave like commitEdit (saveEditedValue is called)
      //calling commitEdit in cancelEdit would lead to NullPointerException! Therefore, just call saveEditedValue
      EditableMetaInfoItem editableMetaInfoItem = getTableRow().getItem();
      editableMetaInfoItem.saveEditedValue(inputField.getText()); //update model
      updateItem(inputField.getText(), false);   // update view: update the item within this cell, so that it represents the new value
    }
    super.cancelEdit();     //sets editing to false
    setText(getItem());     //reset the displayed text to the original (or just changed if !escPressed) item's (cell's) value
    hideInputField();
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
    EditableMetaInfoItem item = getTableRow().getItem();
    treeTableView = ((MetaInfoEditableTagsView)getTableColumn().getTreeTableView()); //link back to view for saving edited values
    Stage stage = treeTableView.getFileTableView().getPrimaryStage(); //for bubble help if no allowed characters entered in restricted TextFields

    if (item instanceof EditableStringTagItem)
      inputField = new UnrestrictedTextField(getString(), stage);
    else if (item instanceof EditableDateTimeTagItem)
      inputField = new DateTimeTextField(getString(), stage);
    else if (item instanceof EditableIntTagItem)
      inputField = new NumberTextField(getString(), stage);
    else if (item instanceof EditableRationalTagItem)
      inputField = new RationalTextField(getString(), stage);

    inputField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

    // CommitEdit on Focus lost of TreeTable
    ((Node) inputField).focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) { //if focus lost (newValue of focussed=false)
        if (isEditing()) commitEdit(inputField.getText());
      }
    });

    //use EventFilter instead of setOnKeyPressed. Otherwise, Up/Dn would first execute their default behaviour (home/end) before onKeyPressed is fired
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
              item.saveEditedValue(inputField.getText());
              lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again when saving is complete
              lastCaretPositionValid = true;
              //but does not consume the event, so that FileTable or Main Menu can initiate saving afterwards
            }
            break;
        }
      }


      /**
       * Helper for moving the caret a line up or down
       *
       * @param up: true = up, false=down
       */
      private void moveCaretUpOrDown(boolean up) {
        //selectAbove/BelowCell() does not work as expected when running over borders of viewport/flow
        int currentLineEdited = treeTableView.getSelectionModel().getSelectedIndex();

        lastCaretPosition = inputField.getCaretPosition(); //remember last caretPosition so that it can be used again in next/prev line
        lastCaretPositionValid = true;
        if (up) {
          if (currentLineEdited > 0) {
            commitEdit(inputField.getText());
            currentLineEdited--;
          }
        } else {    //down
          if (currentLineEdited < treeTableView.getExpandedItemCount() - 1) {  //total number of currently visible rows
            commitEdit(inputField.getText());
            currentLineEdited++;
          }
        }
        treeTableView.setEditable(true); //CommitEdit for the previous cell had ended edit mode

        //**currently no viewport support as in Filetable
//        if (up)
//          fileTableView.scrollViewportToIndex(currentLineEdited, FileTableView.Alignment.TOP);
//        else
//          fileTableView.scrollViewportToIndex(currentLineEdited, FileTableView.Alignment.BOTTOM);
//
//        fileTableView.getSelectionModel().focus(currentLineEdited);
//        fileTableView.getSelectionModel().clearAndSelect(currentLineEdited, editingColumn);
//
//        fileTableView.edit(fileTableView.getSelectionModel().getSelectedIndex(), editingColumn);
      }
    });
  }

  private String getString() {
    if (getItem() == null) return "";
    else return getItem();
  }
} //class TextFieldCell
