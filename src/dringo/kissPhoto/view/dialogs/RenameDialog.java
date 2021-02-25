package dringo.kissPhoto.view.dialogs;

import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.view.FileTableView;
import dringo.kissPhoto.view.inputFields.FileNameTextField;
import dringo.kissPhoto.view.inputFields.SeparatorInputField;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.text.MessageFormat;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is the Dialog for Renaming Files.
 *
 * @author Dringo
 * @since 2012-10-05
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2017-10-14: Fixed: Scaling problems. Centrally solved in kissDialog
 * @version 2016-11-02: RestrictedTextfield has changed signature
 * @version 2014-06-22: extra column for the counter's separator (the character after the counter)
 * @version 2014-06-16: multi screen support: center on main window instead of main screen
 */

public class RenameDialog extends KissDialog {
  public static final int NONE_BTN = 0; //no button was pressed: MessageBox left by [x] of the window
  public static final int RENAME_BTN = 1;
  public static final int CANCEL_BTN = 2;

  private static final int INSET = 7;

  TextField prefixTextField = new FileNameTextField(this);
  SeparatorInputField separatorTextField = new SeparatorInputField();
  TextField descriptionTextField = new FileNameTextField(this);
  TextField extensionTextField = new FileNameTextField(this);
  CheckBox prefixCheckBox = new CheckBox(language.getString(FileTableView.PREFIX));
  CheckBox separatorCheckBox = new CheckBox(language.getString(FileTableView.SEPARATOR));
  CheckBox descriptionCheckBox = new CheckBox(language.getString(FileTableView.DESCRIPTION));
  CheckBox extensionCheckBox = new CheckBox(language.getString(FileTableView.EXTENSION));

  Button prefixBtn;
  Button counterBtn;
  Button separatorBtn;
  Button descriptionBtn;
  Button extensionBtn;
  Button dateBtn;
  Button timeBtn;

  public RenameDialog(Stage owner) {
    super(owner);

    setTitle(language.getString("kissphoto.rename.media.files"));
    setHeight(480);
    setWidth(650);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.WHITE); //1,1 --> use min size as set just before
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
    Insets mainPadding = new Insets(0, INSET, INSET, INSET);
    gridPane.setPadding(mainPadding);
    ColumnConstraints col1 = new ColumnConstraints();
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS);
    gridPane.getColumnConstraints().addAll(col1, col2);


    gridPane.add(prefixCheckBox, 0, 0);
    gridPane.add(prefixTextField, 1, 0);

    gridPane.add(separatorCheckBox, 0, 1);
    gridPane.add(separatorTextField, 1, 1);

    gridPane.add(descriptionCheckBox, 0, 2);
    gridPane.add(descriptionTextField, 1, 2);

    gridPane.add(extensionCheckBox, 0, 3);
    gridPane.add(extensionTextField, 1, 3);

    Label explanation = new Label(language.getString("check.the.fields.you.want.to.change"));
    VBox explanationPane = new VBox();
    explanationPane.setPadding(new Insets(0, INSET, 0, INSET));
    explanationPane.getChildren().addAll(explanation);

    GridPane buttonsGridPane = new GridPane();
    buttonsGridPane.setHgap(5);
    buttonsGridPane.setVgap(2);
    buttonsGridPane.prefHeightProperty().bind(scene.heightProperty());
    buttonsGridPane.prefWidthProperty().bind(scene.widthProperty());
    buttonsGridPane.setAlignment(Pos.CENTER);
    buttonsGridPane.setPadding(mainPadding);
    ColumnConstraints descCol1 = new ColumnConstraints();
    ColumnConstraints descCol2 = new ColumnConstraints();
    descCol1.setHalignment(HPos.RIGHT);
    descCol2.setHgrow(Priority.ALWAYS);
    buttonsGridPane.getColumnConstraints().addAll(descCol1, descCol2);


    prefixBtn = new Button(language.getString("ctrl.p"));
    prefixBtn.setFocusTraversable(false);
    prefixBtn.setOnAction(actionEvent -> {
      if (sceneProperty().get().getFocusOwner().equals(prefixTextField)
          || sceneProperty().get().getFocusOwner().equals(separatorTextField)
          || sceneProperty().get().getFocusOwner().equals(descriptionTextField)
          || sceneProperty().get().getFocusOwner().equals(extensionTextField)) {
        TextField focusedTextField = (TextField) scene.getFocusOwner();
        focusedTextField.insertText(focusedTextField.getCaretPosition(), MediaFile.PLACEHOLDER_PREFIX);
      }
    });
    buttonsGridPane.add(prefixBtn, 0, 0);
    Label prefixDesc = new Label(MessageFormat.format(language.getString("0.inserts.the.current.prefix.of.the.file"), MediaFile.PLACEHOLDER_PREFIX));
    buttonsGridPane.add(prefixDesc, 1, 0);

    counterBtn = new Button(language.getString("ctrl.c"));
    counterBtn.setFocusTraversable(false);
    counterBtn.setOnAction(actionEvent -> {
      if (sceneProperty().get().getFocusOwner().equals(prefixTextField)
          || sceneProperty().get().getFocusOwner().equals(separatorTextField)
          || sceneProperty().get().getFocusOwner().equals(descriptionTextField)
          || sceneProperty().get().getFocusOwner().equals(extensionTextField)) {
        TextField focusedTextField = (TextField) scene.getFocusOwner();
        focusedTextField.insertText(focusedTextField.getCaretPosition(), MediaFile.PLACEHOLDER_COUNTER);
      }
    });
    buttonsGridPane.add(counterBtn, 0, 1);
    Label counterDesc = new Label(MessageFormat.format(language.getString("0.inserts.the.current.counter.value.of.the.file"), MediaFile.PLACEHOLDER_COUNTER));
    buttonsGridPane.add(counterDesc, 1, 1);

    separatorBtn = new Button(language.getString("ctrl.s"));
    separatorBtn.setFocusTraversable(false);
    separatorBtn.setOnAction(actionEvent -> {
      if (sceneProperty().get().getFocusOwner().equals(prefixTextField)
          || sceneProperty().get().getFocusOwner().equals(separatorTextField)
          || sceneProperty().get().getFocusOwner().equals(descriptionTextField)
          || sceneProperty().get().getFocusOwner().equals(extensionTextField)) {
        TextField focusedTextField = (TextField) scene.getFocusOwner();
        focusedTextField.insertText(focusedTextField.getCaretPosition(), MediaFile.PLACEHOLDER_SEPARATOR);
      }
    });
    buttonsGridPane.add(separatorBtn, 0, 2);
    Label separatorDesc = new Label(MessageFormat.format(language.getString("0.inserts.the.current.separator.of.the.file"), MediaFile.PLACEHOLDER_SEPARATOR));
    buttonsGridPane.add(separatorDesc, 1, 2);

    descriptionBtn = new Button(language.getString("ctrl.d"));
    descriptionBtn.setFocusTraversable(false);
    descriptionBtn.setOnAction(actionEvent -> {
      if (sceneProperty().get().getFocusOwner().equals(prefixTextField)
          || sceneProperty().get().getFocusOwner().equals(separatorTextField)
          || sceneProperty().get().getFocusOwner().equals(descriptionTextField)
          || sceneProperty().get().getFocusOwner().equals(extensionTextField)) {
        TextField focusedTextField = (TextField) scene.getFocusOwner();
        focusedTextField.insertText(focusedTextField.getCaretPosition(), MediaFile.PLACEHOLDER_DESCRIPTION);
      }
    });
    buttonsGridPane.add(descriptionBtn, 0, 3);
    Label descriptionDesc = new Label(MessageFormat.format(language.getString("0.inserts.the.current.description.of.the.file"), MediaFile.PLACEHOLDER_DESCRIPTION));
    buttonsGridPane.add(descriptionDesc, 1, 3);

    extensionBtn = new Button(language.getString("ctrl.e"));
    extensionBtn.setFocusTraversable(false);
    extensionBtn.setOnAction(actionEvent -> {
      if (sceneProperty().get().getFocusOwner().equals(prefixTextField)
          || sceneProperty().get().getFocusOwner().equals(separatorTextField)
          || sceneProperty().get().getFocusOwner().equals(descriptionTextField)
          || sceneProperty().get().getFocusOwner().equals(extensionTextField)) {
        TextField focusedTextField = (TextField) scene.getFocusOwner();
        focusedTextField.insertText(focusedTextField.getCaretPosition(), MediaFile.PLACEHOLDER_EXTENSION);
      }
    });
    buttonsGridPane.add(extensionBtn, 0, 4);
    Label extensionDesc = new Label(MessageFormat.format(language.getString("0.inserts.the.current.extension.of.the.file"), MediaFile.PLACEHOLDER_EXTENSION));
    buttonsGridPane.add(extensionDesc, 1, 4);

    dateBtn = new Button(language.getString("ctrl.m"));
    dateBtn.setFocusTraversable(false);
    dateBtn.setOnAction(actionEvent -> {
      if (sceneProperty().get().getFocusOwner().equals(prefixTextField)
          || sceneProperty().get().getFocusOwner().equals(separatorTextField)
          || sceneProperty().get().getFocusOwner().equals(descriptionTextField)
          || sceneProperty().get().getFocusOwner().equals(extensionTextField)) {
        TextField focusedTextField = (TextField) scene.getFocusOwner();
        focusedTextField.insertText(focusedTextField.getCaretPosition(), MediaFile.PLACEHOLDER_DATE);
      }
    });
    buttonsGridPane.add(dateBtn, 0, 5);
    Label dateDesc = new Label(MessageFormat.format(language.getString("0.inserts.the.current.modified.date.of.the.file"), MediaFile.PLACEHOLDER_DATE));
    buttonsGridPane.add(dateDesc, 1, 5);

    timeBtn = new Button(language.getString("ctrl.t"));
    timeBtn.setFocusTraversable(false);
    timeBtn.setOnAction(actionEvent -> {
      if (sceneProperty().get().getFocusOwner().equals(prefixTextField)
          || sceneProperty().get().getFocusOwner().equals(separatorTextField)
          || sceneProperty().get().getFocusOwner().equals(descriptionTextField)
          || sceneProperty().get().getFocusOwner().equals(extensionTextField)) {
        TextField focusedTextField = (TextField) scene.getFocusOwner();
        focusedTextField.insertText(focusedTextField.getCaretPosition(), MediaFile.PLACEHOLDER_TIME);
      }
    });
    buttonsGridPane.add(timeBtn, 0, 6);
    Label timeDesc = new Label(MessageFormat.format(language.getString("0.inserts.the.current.modified.time.of.the.file"), MediaFile.PLACEHOLDER_TIME));
    buttonsGridPane.add(timeDesc, 1, 6);

    HBox buttonBox = new HBox();
    buttonBox.setSpacing(INSET);
    buttonBox.setPadding(mainPadding);
    buttonBox.setAlignment(Pos.CENTER);

    Button renameBtn = new Button(language.getString("rename"));
    renameBtn.setDefaultButton(true);
    renameBtn.setOnAction(actionEvent -> {
      modalResult = RENAME_BTN;
      close();
    });

    Button cancelBtn = new Button(KissDialog.CANCEL_LABEL);
    cancelBtn.setCancelButton(true);
    cancelBtn.setOnAction(actionEvent -> {
      modalResult = CANCEL_BTN;
      close();
    });
    buttonBox.getChildren().addAll(renameBtn, cancelBtn);

    rootArea.getChildren().addAll(gridPane, explanationPane, buttonsGridPane, buttonBox);
    root.getChildren().add(rootArea);

    installTextFieldListeners();
  }

  private void installTextFieldListeners() {

    EventHandler<KeyEvent> textFieldEventHandler = event -> {
      //whenever text is typed in the according checkbox is set automatically
      if (event.getSource().equals(prefixTextField)) prefixCheckBox.setSelected(true);
      else if (event.getSource().equals(descriptionTextField)) descriptionCheckBox.setSelected(true);
      else if (event.getSource().equals(extensionTextField)) extensionCheckBox.setSelected(true);

      //insert placeholders (%-Codes)
      if (event.isShortcutDown() && event.getCode() == KeyCode.P) {
        prefixBtn.fire();
        event.consume();
      } else if (event.isShortcutDown() && event.getCode() == KeyCode.C) {
        counterBtn.fire();
        event.consume();
      } else if (event.isShortcutDown() && event.getCode() == KeyCode.S) {
        separatorBtn.fire();
        event.consume();
      } else if (event.isShortcutDown() && event.getCode() == KeyCode.D) {
        descriptionBtn.fire();
        event.consume();
      } else if (event.isShortcutDown() && event.getCode() == KeyCode.E) {
        extensionBtn.fire();
        event.consume();
      } else if (event.isShortcutDown() && event.getCode() == KeyCode.M) {
        dateBtn.fire();
        event.consume();
      } else if (event.isShortcutDown() && event.getCode() == KeyCode.T) {
        timeBtn.fire();
        event.consume();
      }
    };

    prefixTextField.addEventFilter(KeyEvent.KEY_PRESSED, textFieldEventHandler);
    descriptionTextField.addEventFilter(KeyEvent.KEY_PRESSED, textFieldEventHandler);
    extensionTextField.addEventFilter(KeyEvent.KEY_PRESSED, textFieldEventHandler);

    separatorTextField.setOnShowing((EventHandler<Event>) event -> separatorCheckBox.setSelected(true));
  }

  /**
   * initialize the input fields with the passed values
   * show the modal dialog
   *
   * @param initPrefix      init value for textfield
   * @param initSeparator   init value for textfield
   * @param initDescription init value for textfield
   * @param initExtension   init value for textfield
   * @return the button-constant which was used to close the dialog
   */
  public int showModal(String initPrefix, String initSeparator, String initDescription, String initExtension) {
    modalResult = NONE_BTN;
    prefixTextField.setText(initPrefix);
    separatorTextField.setText(initSeparator);
    descriptionTextField.setText(initDescription);
    extensionTextField.setText(initExtension);


    centerAndScaleDialog();
    descriptionTextField.requestFocus();
    showAndWait();

    return modalResult;
  }

  /**
   * get resulting values after closing the dialog
   */
  public String getPrefix() {
    return prefixTextField.getText();
  }

  public String getSeparator() {
    return separatorTextField.getText();
  }

  public String getDescription() {
    return descriptionTextField.getText();
  }

  public String getExtension() {
    return extensionTextField.getText();
  }

  public boolean isPrefixChecked() {
    return prefixCheckBox.isSelected();
  }

  public boolean isSeparatorChecked() {
    return separatorCheckBox.isSelected();
  }

  public boolean isDescriptionChecked() {
    return descriptionCheckBox.isSelected();
  }

  public boolean isExtensionChecked() {
    return extensionCheckBox.isSelected();
  }
}
