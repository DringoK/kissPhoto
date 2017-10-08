package de.kissphoto.view.mediaViewers;

import de.kissphoto.helper.I18Support;
import de.kissphoto.view.MediaContentView;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements a viewer for unknown ("other") files
 * <ul>
 * <li>just a text is displayed, saying, that nothing can be displayed ;-)
 * </ul>
 *
 * @author Dr. Ingo Kreuz
 * @date 2014-05-25
 * @changes: 2014-05-25:
 * @modified: 2017-10-08: minSize=0,0 added so that all other viewers can also be smaller (surrounding stackPane sets min size to smallest in stack)
 */
public class OtherViewer extends StackPane {
  private static ResourceBundle language = I18Support.languageBundle;
  private ContextMenu contextMenu = new ContextMenu();
  private static Text message;

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
    setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
      @Override
      public void handle(ContextMenuEvent contextMenuEvent) {
        contextMenu.setAutoHide(true);
        contextMenu.show(contentView, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
      }
    });
    //hide context menu if clicked "somewhere else" or request focus on mouse click
    setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (contextMenu.isShowing()) {
          contextMenu.hide(); //this closes the context Menu
          mouseEvent.consume();
        } else {
          requestFocus();
        }
      }
    });
    InnerShadow iShadow = new javafx.scene.effect.InnerShadow();
    iShadow.setOffsetX(3.5f);
    iShadow.setOffsetY(3.5f);
    message = new Text(language.getString("sorry.file.cannot.be.displayed"));
    message.setEffect(iShadow);
    message.setFill(Color.GRAY);
    message.setFont(Font.font(null, FontWeight.BOLD, 24));
    getChildren().addAll(message);

    //min size is by default the largest content (here the text)
    //but it must not prevent the StackPane in MediaContentView (which holds all Viewers) from getting smaller
    //As an effect the above text element might be cut
    setMinSize(0, 0);
  }
}
