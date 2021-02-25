package dringo.kissPhoto.view.dialogs;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.helper.I18Support;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * A dialog to change the language of the GUI of kissPhoto
 * Changes are written to the settings file only and effects are only applied after a restart
 * <p/>
 *
 * @author Ingo
 * @since 2014-05-02
 * @version 2020-12-20 globalSettings and language are now global (static in Kissphoto)
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 * @version 2014-06-16 multi screen support: center on main window instead of main screen
 */
public class LanguageDialog extends KissDialog {
  static final int borderSize = 8;

  RadioButton btn_en;
  RadioButton btn_de;

  /**
   * constructor to create the dialog
   *
   * @param owner          the window who owns this modal dialog
   */
  public LanguageDialog(Stage owner) {
    super(owner);
    initStyle(StageStyle.DECORATED);

    setOpacity(0.8);
    setTitle(language.getString("kissphoto.select.language"));

    setHeight(250);
    setWidth(400);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.BLACK); //1,1 --> use min size as set just before

    setScene(scene);

    ToggleGroup radioBtnGroup = new ToggleGroup();
    btn_en = new RadioButton("English (Default)");
    btn_en.setTextFill(Color.WHITE);
    btn_en.setToggleGroup(radioBtnGroup);
    btn_en.setSelected(I18Support.currentLanguage.equals(I18Support.ENGLISH));

    btn_de = new RadioButton("Deutsch");
    btn_de.setToggleGroup(radioBtnGroup);
    btn_de.setTextFill(Color.WHITE);
    btn_de.setSelected(I18Support.currentLanguage.equals(I18Support.GERMAN));

    Text messageLabel = new Text(language.getString("restart.kissphoto.to.apply.changes"));
    messageLabel.setFill(Color.WHITE);
    messageLabel.wrappingWidthProperty().bind(scene.widthProperty().subtract(2 * borderSize));
    messageLabel.setTextAlignment(TextAlignment.CENTER);

    VBox radioButtons = new VBox();
    radioButtons.setFillWidth(true);
    radioButtons.setSpacing(5.0);
    radioButtons.setPadding(new Insets(borderSize));
    radioButtons.getChildren().addAll(btn_en, btn_de);

    HBox buttonBox = new HBox();
    buttonBox.setSpacing(30.0);
    buttonBox.setAlignment(Pos.CENTER);

    Button okBtn = new Button(KissDialog.OK_LABEL);
    okBtn.setDefaultButton(true);
    okBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        modalResult_bool = OK_BOOL;
        close();
      }
    });
    Button cancelBtn = new Button(KissDialog.CANCEL_LABEL);
    cancelBtn.setCancelButton(true);
    cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        modalResult_bool = CANCEL_BOOL;
        close();
      }
    });
    buttonBox.getChildren().addAll(okBtn, cancelBtn);


    VBox vBox = new VBox();
    vBox.setAlignment(Pos.CENTER);
    vBox.setFillWidth(true);
    vBox.setSpacing(20.0);
    vBox.setPadding(new Insets(borderSize));

    vBox.getChildren().add(radioButtons);
    vBox.getChildren().add(messageLabel);
    vBox.getChildren().add(buttonBox);
    vBox.prefWidthProperty().bind(scene.widthProperty());
    vBox.prefHeightProperty().bind(scene.heightProperty());

    root.getChildren().add(vBox);
  }

  /**
   * shows the message box centered on the screen.
   * if OK_BOOL was pressed the according Language is stored into the settings file for the next start of kissPhoto
   *
   * @return the Button-ID-constant which was pressed to leave the message.
   * CANCEL_BOOL is returned if the box was left by pressing [x] of the window or CANCEL_BOOL Btn
   * OK_BOOL is returned if the language was changed
   */
  public boolean showModal() {
    modalResult_bool = CANCEL_BOOL;

    centerAndScaleDialog();
    showAndWait();

    if (modalResult_bool) {  //OK_BOOL=true ;-)
      if (btn_de.isSelected()) KissPhoto.globalSettings.setProperty(I18Support.LANGUAGE, I18Support.GERMAN);
      if (btn_en.isSelected()) KissPhoto.globalSettings.setProperty(I18Support.LANGUAGE, I18Support.ENGLISH);
    }

    return modalResult_bool;
  }
}
