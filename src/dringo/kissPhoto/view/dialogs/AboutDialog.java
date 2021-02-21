package dringo.kissPhoto.view.dialogs;

import dringo.kissPhoto.helper.AppStarter;
import dringo.kissPhoto.view.mediaViewers.PlayerViewerVLCJ;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.MessageFormat;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * This is the Dialog showing the About Window
 *
 * @author Dringo
 * @since 2014-04-23
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers@version 2020-12-20 housekeeping
 * @version 2014-05-02 (I18Support)
 * @version 2014-06-16 multi screen support: center on main window instead of main screen
 * @version 2017-10-14 Fixed: Scaling problems. Centrally solved in kissDialog
 */
public class AboutDialog extends KissDialog {
  private static final String DESCRIPTION_TEXTSTYLE = "-fx-text-fill: white; -fx-font: bold 13 \"sanserif\"; ";
  private final VBox copyrightPane = new VBox();


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


    Button okBtn = new Button(OK_LABEL);
    okBtn.setFocusTraversable(true);
    okBtn.setCancelButton(true);
    okBtn.setDefaultButton(true);
    okBtn.setOnAction(actionEvent -> close());

    Button copyrightBtn = new Button("Copyright Info");
    copyrightBtn.setFocusTraversable(true);
    copyrightBtn.setOnAction(actionEvent -> {
      copyrightPane.setVisible(!copyrightPane.isVisible());
    });

    buttonBox.getChildren().addAll(okBtn, copyrightBtn);


    copyrightPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);"); //30% transparency black
    copyrightPane.setPadding(new Insets(10,10,10,10));
    copyrightPane.setFocusTraversable(true); //can receive key events


    copyrightPane.setVisible(false);
    //Mouseclick or any key will "close" copyright pane
    copyrightPane.setOnMouseClicked(mouseEvent -> {
      copyrightPane.setVisible(false);
      mouseEvent.consume();
    });
    copyrightPane.setOnKeyPressed(keyEvent -> {
      copyrightPane.setVisible(false);
      keyEvent.consume();
    });

    VBox contentPane = buildContentPane();
    contentPane.setStyle("-fx-background-color: rgba(100, 100, 255, 0.2);"); //20% transparency blue
    contentPane.prefWidthProperty().bind(copyrightPane.widthProperty());

    copyrightPane.getChildren().add(contentPane);

    StackPane rootArea = new StackPane();
    rootArea.prefHeightProperty().bind(scene.heightProperty());
    rootArea.prefWidthProperty().bind(scene.widthProperty());
    rootArea.setAlignment(Pos.BOTTOM_LEFT);

    rootArea.getChildren().addAll(imageView, buttonBox, copyrightPane);
    root.getChildren().add(rootArea);
  }

  private VBox buildContentPane() {
    VBox contentPane = new VBox();
    contentPane.setPadding(new Insets(10,10,10,10));


    Label headline = new Label("\nkissPhoto");
    headline.setStyle("-fx-text-fill: white; -fx-font: bold 20 \"sanserif\"; ");
    contentPane.getChildren().add(headline);

    Label description = new Label();
    description.setStyle(DESCRIPTION_TEXTSTYLE);
    description.setText(language.getString("about.description"));
    contentPane.getChildren().add(description);


    contentPane.getChildren().addAll(
      buildLink("OpenJDK/OpenJFX:", "https://bell-sw.com/"),
      buildLink("Adobe XMP-Core:", "https://github.com/drewnoakes/adobe-xmp-core"),
      buildLink("MetaData-Extractor:","https://github.com/drewnoakes/metadata-extractor"),
      buildLink("JNA:","https://github.com/java-native-access/jna"),
      buildLink("Caprica VLCJ-Natives:","https://github.com/caprica/vlcj-natives"),
      buildLink("Caprica VLCJ:","https://github.com/caprica/vlcj"),
      buildLink("Caprica VLCJ-javafx:","https://github.com/caprica/vlcj-javafx"),
      buildLink("Caprica VLCJ-File-Filters:","https://github.com/caprica/vlcj-file-filters"),
      buildLink("MediaUtil:","http://mediachest.sourceforge.net/mediautil")
    );

    Label vlcInfo = buildVLCInfo();
    vlcInfo.prefWidthProperty().bind(contentPane.widthProperty());
    vlcInfo.setAlignment(Pos.TOP_CENTER);

    contentPane.getChildren().add(vlcInfo);

    return contentPane;
  }

  private HBox buildLink(String description, String url){
    HBox hBox = new HBox();

    Label label = new Label(description);
    label.setStyle(DESCRIPTION_TEXTSTYLE);
    label.setPrefWidth(170);

    Hyperlink hyperlink = new Hyperlink(url);
    hyperlink.setStyle(DESCRIPTION_TEXTSTYLE);
    hyperlink.setOnAction( event -> {
      AppStarter.tryToBrowse(url);
    });

    hBox.getChildren().addAll(label, hyperlink);
    return hBox;
  }

  private Label buildVLCInfo() {
    String vlcInfoText;
    if (PlayerViewerVLCJ.isVlcAvailable())
      vlcInfoText = MessageFormat.format(language.getString("vlc.is.used.version.found.0"), PlayerViewerVLCJ.getCurrentVLCVersion());
    else
      if (PlayerViewerVLCJ.getCurrentVLCVersion().equals(""))
        vlcInfoText = MessageFormat.format(language.getString("vlc.installation.not.found"), PlayerViewerVLCJ.getRequiredVLCVersion());
      else
        vlcInfoText =MessageFormat.format(language.getString("vlc.found.version.0.but.required.1"), PlayerViewerVLCJ.getCurrentVLCVersion(),PlayerViewerVLCJ.getRequiredVLCVersion());

    Label vlcInfo = new Label();
    vlcInfo.setStyle("-fx-text-fill: lightblue; -fx-font: bold 14 \"serif\";" );
    vlcInfo.setText("\n"+vlcInfoText+"\n\n");

    return vlcInfo;
  }

  /**
   * show the modal dialog
   */
  public void showModal() {
    centerAndScaleDialog();
    showAndWait();
  }


}
