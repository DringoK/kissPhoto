package dringo.kissPhoto.view.mediaViewers;

import dringo.kissPhoto.view.MediaContentView;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static dringo.kissPhoto.KissPhoto.language;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for unknown ("other") files:
 * - Black Background
 * - Main Message "file format not supported" (can be switched off)
 * - Additional Message (simple text)
 * <ul>
 * <li>just a text is displayed, saying, that nothing can be displayed ;-)
 * </ul>
 *
 * @author Dringo
 * @since 2014-05-25
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers
 * @version 2020-11-12 Support for additional information why "this mediaFile cannot be shown" by any other viewer
 * @version 2017-10-08 minSize=0,0 added so that all other viewers can also be smaller (surrounding stackPane sets min size to smallest in stack)
 */
public class OtherViewer extends VBox {
  private final ContextMenu contextMenu = new ContextMenu();

  private final Text message;
  private final Text additionalMessage;

  /**
   * constructor to initialize the viewer
   *
   * @param contentView link to pane, where the viewer placed in (e.g. for binding sizes)
   */
  public OtherViewer(final MediaContentView contentView) {
    super();

    setFocusTraversable(true);

    //---- context menu items
    contentView.addContextMenuItems(contextMenu);  //every viewer of kissPhoto lies in a MediaContentView
    setOnContextMenuRequested(contextMenuEvent -> {
      contextMenu.setAutoHide(true);
      contextMenu.show(contentView, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    });
    //hide context menu if clicked "somewhere else" or request focus on mouse click
    setOnMouseClicked(mouseEvent -> {
      if (contextMenu.isShowing()) {
        contextMenu.hide(); //this closes the context Menu
        mouseEvent.consume();
      } else {
        requestFocus();
      }
    });
    InnerShadow iShadow = new javafx.scene.effect.InnerShadow();
    iShadow.setOffsetX(3.5f);
    iShadow.setOffsetY(3.5f);
    message = new Text(language.getString("sorry.file.cannot.be.displayed"));
    message.setEffect(iShadow);
    message.setFill(Color.GRAY);
    message.setFont(Font.font(null, FontWeight.BOLD, 24));
    message.setVisible(false);

    additionalMessage = new Text("");
    additionalMessage.setFill(Color.GRAY);
    additionalMessage.setFont(Font.font(null, FontPosture.ITALIC, 12));

    setAlignment(Pos.CENTER);
    getChildren().addAll(message, additionalMessage);

    //min size is by default the largest content (here the text)
    //but it must not prevent the StackPane in MediaContentView (which holds all Viewers) from getting smaller
    //As an effect the above text element might be cut
    setMinSize(0, 0);
  }

  /**
   * show or hide the main text "unsupported Media"
   * @param visible = true to show the main text
   */
  public void setMainMessageVisible(boolean visible){
    message.setVisible(visible);
  }

  /**
   * add an extra line of information, why media is unsupported or what can be done to show the media
   * @param additionalText the information to show in an extra line, empty String ("") or null clears the additional message
   */
  public void setAdditionalMessage(String additionalText){
    if (additionalText == null) {
      additionalMessage.setText("");
    } else {
      additionalMessage.setText(additionalText);
    }
  }

  /**
   * resets to the basic view: main message visible and no additional message
   */
  public void resetView(){
    message.setVisible(true);
    additionalMessage.setText("");
  }

}
