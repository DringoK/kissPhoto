package dringo.kissPhoto.view.mediaViewers;

import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.view.MediaContentView;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for Attributes of media files.<br>
 * This Viewer can be overlayed to all other viewers thus overlaying file attributes
 * It is not implemented as a filling JavaFx Pane but consists of a small Pane and a text which is added to the
 * bottom of the Pane passed with the constructor. (Added to bottom means it's pos-properties are bound to the bottom border)
 * <p/>
 * <ul>
 * <li> prefix
 * <li> counter
 * <li> description
 * <li> file date
 * </ul>
 * <br>
 * Which of the attributes are visible can be chosen by the user (standard: just description)
 *
 * @author Dringo
 * @version 2020-11-06 bug fixing: handle empty mediaFile
 * @version 2014-06-01 changed from overlaying fullscreen panel to a small panel with bound position to the bottom border
 *            otherwise mouse events would not be recognized from the underlying viewer
 */
public class AttributesViewer extends StackPane {
  MediaContentView mediaContentView; //link to the underlying ContentView (for resizing etc)
  MediaFile mediaFile;   //attributes currently displayed from media file  (@see setMedia)
  Text text = new Text();

  boolean displayPrefix = false;
  boolean displayCounter = false;
  boolean displayExtension = false;
  boolean displayFileDate = false;


  /**
   * constructor to initialize the viewer
   *
   * @param contentView link to pane, where the viewer placed in (e.g. for binding sizes)
   */
  public AttributesViewer(MediaContentView contentView) {
    super();

    this.mediaContentView = contentView;
    setPrefHeight(25);
    prefWidthProperty().bind(contentView.widthProperty());  //use full width
    translateYProperty().bind(contentView.heightProperty().subtract(heightProperty())); //bind to bottom border

    text.setFill(Color.WHITE);
    text.setFont(Font.font(null, FontWeight.BOLD, 14));

    //50% transparent black background
    Pane textPane = new Pane();
    textPane.prefHeightProperty().bind(heightProperty());
    textPane.prefWidthProperty().bind(widthProperty());

    textPane.setStyle("-fx-background-color: black;");
    textPane.setOpacity(0.5);
    textPane.setPadding(new Insets(5, 5, 5, 5));

    //pack background and text into stack and place in the bottom border
    getChildren().addAll(textPane, text);

  }

  public void setMedia(MediaFile mediaFile) {
    this.mediaFile = mediaFile;     //might be null e.g. if current directory is empty
    refreshText();
  }

  private static final String sep = "  ";

  private void refreshText() {
    String message = "";
    if (mediaFile != null) {
      if (displayPrefix) message = mediaFile.getPrefix() + sep;
      if (displayCounter) message = message + mediaFile.getCounter() + sep;
      message = message + mediaFile.getDescription() + sep;  //if visible: description can't be switched off...this is the master
      if (displayExtension) message = message + mediaFile.getExtension() + sep;
      if (displayFileDate) message = message + mediaFile.getModifiedDate();
    }
    text.setText(message);
  }

  /**
   * copy the state (i.e. if and what is to be displayed)
   * especially used when full screen mode is enabled: then the state of the normal and the full screen mediaContentView's
   * attributes viewers are synchronized
   *
   * @param otherAttrViewer  the attr viewer to copy the state to
   */
  public void copyState(AttributesViewer otherAttrViewer) {
    setVisible(otherAttrViewer.isVisible());
    displayPrefix = otherAttrViewer.isDisplayPrefix();
    displayCounter = otherAttrViewer.isDisplayCounter();
    displayExtension = otherAttrViewer.isDisplayExtension();
    displayFileDate = otherAttrViewer.isDisplayFileDate();
    refreshText();
  }

  //------------------------------------- getters and setters ----------------------
  public void setDisplayPrefix(boolean displayPrefix) {
    this.displayPrefix = displayPrefix;
    refreshText();
  }

  public boolean isDisplayPrefix() {
    return displayPrefix;
  }

  public void setDisplayCounter(boolean displayCounter) {
    this.displayCounter = displayCounter;
    refreshText();
  }

  public boolean isDisplayCounter() {
    return displayCounter;
  }

  public boolean isDisplayExtension() {
    return displayExtension;
  }

  public void setDisplayExtension(boolean displayExtension) {
    this.displayExtension = displayExtension;
    refreshText();
  }

  public void setDisplayFileDate(boolean displayFileDate) {
    this.displayFileDate = displayFileDate;
    refreshText();
  }

  public boolean isDisplayFileDate() {
    return displayFileDate;
  }
}
