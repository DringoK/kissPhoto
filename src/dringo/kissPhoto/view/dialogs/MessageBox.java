package dringo.kissPhoto.view.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * A simple MessageBox consisting of
 * <ul>
 * <li>a message</li>
 * <li>standard buttons</li>
 * </ul>
 *
 * @author Dringo
 * @since 2012-09-23
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2017-10-14: Fixed: Scaling problems. Centrally solved in kissDialog
 * @version 2014-06-16: multi screen support: center on main window instead of main screen
 * @version 2014-05-02: (I18Support)
 */
public class MessageBox extends KissDialog {

  public static final int NONE_BTN = 0;     //no button was pressed: MessageBox left by [x] of the window
  public static final int OK_BTN = 1;
  public static final int CANCEL_BTN = 2;
  public static final int YES_BTN = 4;
  public static final int NO_BTN = 8;
  public static final int USER_BTN = 16;     //the constructor can receive one string for a user defined caption
  final int borderSize = 8;

  /**
   * constructor to create simple message dialog
   * To determine the visible buttons sum up the BTN-constants: e.g. OK_BTN+CANCEL_BTN shows OK_BOOL and CANCEL_BOOL-Btn
   *
   * @param message        a string with the message to show
   * @param visibleButtons sum up the constants OK_BTN, CANCEL_BTN, YES_BTN, ... to determine which buttons to show
   * @param userBtnText    if a user-Btn is requested you can provide the text here. If it is null "?" will be the label.
   */
  public MessageBox(Stage owner, String message, int visibleButtons, String userBtnText) {
    super(owner);
    int buttons = visibleButtons;
    initStyle(StageStyle.DECORATED);

    if (buttons == 0) {
      buttons = OK_BTN;
    }

    setOpacity(0.8);
    setTitle(language.getString("kissphoto.message"));

    setHeight(250);
    setWidth(400);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.BLACK); //1,1 --> use min size as set just before

    setScene(scene);

    Text messageLabel = new Text(message);
    messageLabel.setFill(Color.WHITE);
    messageLabel.wrappingWidthProperty().bind(scene.widthProperty().subtract(2 * borderSize));
    messageLabel.setTextAlignment(TextAlignment.CENTER);


    HBox buttonBox = new HBox();
    buttonBox.setSpacing(30.0);
    buttonBox.setAlignment(Pos.CENTER);

    VBox vBox = new VBox();
    vBox.setAlignment(Pos.CENTER);
    vBox.setFillWidth(true);
    vBox.setSpacing(40.0);
    vBox.setPadding(new Insets(borderSize));

    vBox.getChildren().add(messageLabel);
    vBox.getChildren().add(buttonBox);
    vBox.prefWidthProperty().bind(scene.widthProperty());
    vBox.prefHeightProperty().bind(scene.heightProperty());


    //add only desired Buttons
    if ((buttons & USER_BTN) > 0) {
      Button userBtn;
      if (userBtnText != null) {
        userBtn = new Button(userBtnText);
      } else {
        userBtn = new Button("?");
      }
      userBtn.setDefaultButton(true);
      userBtn.setOnAction(event -> {
        modalResult = USER_BTN;
        close();
      });
      buttonBox.getChildren().add(userBtn);
    }
    if ((buttons & YES_BTN) > 0) {
      Button yesBtn = new Button(YES_LABEL);
      yesBtn.setDefaultButton(true);
      yesBtn.setDefaultButton((visibleButtons & (USER_BTN | OK_BTN)) == 0);//YES is only default if there is no user button and no OK_BOOL button
      yesBtn.setOnAction(event -> {
        modalResult = YES_BTN;
        close();
      });
      buttonBox.getChildren().add(yesBtn);
    }
    if ((buttons & NO_BTN) > 0) {
      Button noBtn = new Button(NO_LABEL);
      noBtn.setCancelButton(true);
      noBtn.setOnAction(event -> {
        modalResult = NO_BTN;
        close();
      });
      buttonBox.getChildren().add(noBtn);
    }
    if ((buttons & OK_BTN) > 0) {
      Button okBtn = new Button(OK_LABEL);
      okBtn.setDefaultButton((visibleButtons & USER_BTN) == 0);//OK_BOOL is only default if there is no user button
      okBtn.setOnAction(event -> {
        modalResult = OK_BTN;
        close();
      });
      buttonBox.getChildren().add(okBtn);
    }
    if ((buttons & CANCEL_BTN) > 0) {
      Button cancelBtn = new Button(CANCEL_LABEL);
      cancelBtn.setCancelButton(true);
      cancelBtn.setOnAction(event -> {
        modalResult = CANCEL_BTN;
        close();
      });
      buttonBox.getChildren().add(cancelBtn);
    }

    root.getChildren().add(vBox);
  }

  /**
   * shows the message box centered on the screen.
   *
   * @return the Button-ID-constant which was pressed to leave the message.
   * 0 is returned if the box was left by pressing [x] of the window
   */
  public int showModal() {
    modalResult = NONE_BTN;

    centerAndScaleDialog();
    showAndWait();
    return modalResult;
  }
}
