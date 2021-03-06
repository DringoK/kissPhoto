package dringo.kissPhoto.view.dialogs;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.inputFields.NumberTextField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * This is the Dialog Window for Renumbering
 *
 * @author Dringo
 * @since 2012-10-05
 * @version 2021-11-01 simplification: global numbering parameter removed. Selection is numbered as entered, no tricks with numbers relative to list position
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 * @version 2016-11-01 RestrictedTextField no longer tries to store connection to FileTable-->adaption here: pass null to constructor
 * @version 2014-06-16 multi screen support: center on main window instead of main screen
 * @version 2014-05-02 (I18Support)
 */
public class RenumberDialog extends KissDialog {
  public static final int NONE_BTN = 0; //no button was pressed: MessageBox left by [x] of the window
  public static final int CANCEL_BTN = 2;
  public static final int RENUM_SELECTION_BTN = 4;

  NumberTextField startTextField = new NumberTextField(this);
  NumberTextField stepTextField = new NumberTextField(this);
  NumberTextField digitsTextField = new NumberTextField(this);

  public RenumberDialog(Stage owner) {
    super(owner);

    setTitle(KissPhoto.language.getString("kissphoto.renumber.media.files"));
    setHeight(170);
    setWidth(300);
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


    Label startLabel = new Label(KissPhoto.language.getString("start.with"));
    startLabel.setTooltip(new Tooltip(KissPhoto.language.getString("offset.which.value.to.start.numbering.with")));
    gridPane.add(startLabel, 0, 1);
    gridPane.add(startTextField, 1, 1);

    Label stepLabel = new Label(KissPhoto.language.getString("step.size"));
    stepLabel.setTooltip(new Tooltip(KissPhoto.language.getString("StepSize.Explanation")));
    gridPane.add(stepLabel, 0, 2);
    gridPane.add(stepTextField, 1, 2);

    Label digitsLabel = new Label(KissPhoto.language.getString("digits.0.auto"));
    digitsLabel.setTooltip(new Tooltip(KissPhoto.language.getString("how.many.digits.shall.be.used.for.the.numbers.using.leading.zeros")));
    gridPane.add(digitsLabel, 0, 3);
    gridPane.add(digitsTextField, 1, 3);


    HBox buttonBox = new HBox();
    buttonBox.setSpacing(7.0);
    buttonBox.setPadding(mainPadding);
    buttonBox.setAlignment(Pos.CENTER);

    Button renumSelectionBtn = new Button(KissPhoto.language.getString("renumber.selection"));
    renumSelectionBtn.setOnAction(actionEvent -> {
      modalResult = RENUM_SELECTION_BTN;
      close();
    });
    Button cancelBtn = new Button(CANCEL_LABEL);
    cancelBtn.setCancelButton(true);
    cancelBtn.setOnAction(actionEvent -> {
      modalResult = CANCEL_BTN;
      close();
    });
    buttonBox.getChildren().addAll(renumSelectionBtn, cancelBtn);

    rootArea.getChildren().addAll(gridPane, buttonBox);
    root.getChildren().add(rootArea);
  }

  /**
   * initialize the input fields with the passed values
   * show the modal dialog
   *
   * @param start           init value for start input field
   * @param step            init value for step input field
   * @param digits          init value for digits input field
   * @return the button-constant which was used to close the dialog
   */
  public int showModal(int start, int step, int digits) {
    modalResult = NONE_BTN; //closing without using a button as default

    startTextField.setText(Integer.toString(start));
    stepTextField.setText(Integer.toString(step));
    digitsTextField.setText(Integer.toString(digits));

    startTextField.requestFocus();

    centerAndScaleDialog();
    showAndWait();

    return modalResult;
  }

  /**
   * get resulting value after closing the dialog
   *
   * @return start field value
   */
  public int getStart() {
    return (Integer.parseInt(startTextField.getText()));
  }

  /**
   * get resulting value after closing the dialog
   *
   * @return step field value
   */
  public int getStep() {
    return (Integer.parseInt(stepTextField.getText()));
  }

  /**
   * get resulting value after closing the dialog
   *
   * @return digits field value
   */
  public int getDigits() {
    return (Integer.parseInt(digitsTextField.getText()));
  }
}
