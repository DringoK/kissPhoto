package de.kissphoto.view.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.MessageFormat;

import static de.kissphoto.KissPhoto.language;

/**
 * This is the Dialog showing the About Window
 *
 * @author Dr. Ingo Kreuz
 * @since 2014-04-23
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2014-05-02 (I18Support)
 * @version 2014-06-16 multi screen support: center on main window instead of main screen
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 */
public class AboutDialog extends KissDialog {

  public AboutDialog(Stage owner, String versionString) {
    super(StageStyle.UTILITY, owner);

    setTitle(MessageFormat.format(language.getString("about.kissphoto.0"), versionString));
    setHeight(530);
    setWidth(700);
    setMinHeight(getHeight());
    setMinWidth(getWidth());

    Group root = new Group();
    scene = new Scene(root, 1, 1, Color.BLACK);  //1,1 --> use min Size as set just before
    setScene(scene);

    ImageView imageView = new ImageView();
    imageView.setPreserveRatio(true);
    imageView.fitHeightProperty().bind(scene.heightProperty());
    imageView.fitWidthProperty().bind(scene.widthProperty());
    try {
      imageView.setImage(new Image(getClass().getResourceAsStream("/images/KissPhotoSplash.jpg")));
    } catch (Exception e) {
      //if not possible then don't show splash screen :-(
    }

    HBox buttonBox = new HBox();
    buttonBox.setSpacing(7.0);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setMaxHeight(30);
    buttonBox.setPadding(new Insets(7));


    Button okBtn = new Button(KissDialog.OK_LABEL);
    okBtn.setFocusTraversable(true);
    okBtn.setCancelButton(true);
    okBtn.setDefaultButton(true);
    okBtn.setOnAction(actionEvent -> close());
    buttonBox.getChildren().addAll(okBtn);

    StackPane rootArea = new StackPane();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());
    rootArea.setAlignment(Pos.BOTTOM_LEFT);

    rootArea.getChildren().addAll(imageView, buttonBox);
    root.getChildren().add(rootArea);
  }

  /**
   * show the modal dialog
   */
  public void showModal() {
    centerAndScaleDialog();
    showAndWait();
  }

}
