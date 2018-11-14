package de.kissphoto;

import de.kissphoto.view.mediaViewers.helper.RotatablePaneLayouter;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RotationTest extends Application {

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) {

    //image in a StackPane to be rotated
    final ImageView imageView = new ImageView("file:D:/Test_org.jpg");
    imageView.setPreserveRatio(true);
    StackPane stackPane = new StackPane(imageView); //a stackPane is used to center the image
    stackPane.setStyle("-fx-background-color: black;");
    imageView.fitWidthProperty().bind(stackPane.widthProperty());
    imageView.fitHeightProperty().bind(stackPane.heightProperty());

    //container for layouting rotated Panes
    RotatablePaneLayouter root = new RotatablePaneLayouter(stackPane);
    root.setStyle("-fx-background-color: blue;");

    Scene scene = new Scene(root, 1024, 768);

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
          //rotate additionally 90°
          stackPane.setRotate((stackPane.getRotate() + 90) % 360);
        }
      }
    });

    primaryStage.setTitle("Rotation test");
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
