package de.kissphoto.view.mediaViewers;

import de.kissphoto.view.MediaContentView;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This Class implements an empty viewer for movie clips.
 * It is used whenever there is no chance to play videos on this machine
 * and it just shows a hint that installing VLC-Player would solve the problem
 * It supports the same interface as MovieViewer but all methods do nothing ("dummy")
 *
 * @author Dr. Ingo Kreuz
 * @date 2019-11-02
 * @modified:
 */
public class MovieViewerDummy extends MovieViewer {
  protected MediaView mediaView;
  private static Text message;

  /**
   * @constructor to initialize the viewer
   */
  public MovieViewerDummy(final MediaContentView contentView) {
    super(contentView);   //mediaContentView of father class is now = contentView

    setFocusTraversable(true);
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
    message = new Text("Sorry -This video file is not playable on this computer. Install VLC-Player and retry");
    message.setEffect(iShadow);
    message.setFill(Color.GRAY);
    message.setFont(Font.font(null, FontWeight.BOLD, 24));
    getChildren().addAll(message);

    //min size is by default the largest content (here the text)
    //but it must not prevent the StackPane in MediaContentView (which holds all Viewers) from getting smaller
    //As an effect the above text element might be cut
    setMinSize(0, 0);

  }

  /**
   * put the media (movie) into the MovieViewer and play it
   *
   * @param media        the media to show
   * @param seekPosition if not null it is tried to seek this position as soon as the movie is loaded/visible
   */
  public void setMedia(Media media, Duration seekPosition) {
  }


  //----------------------- Implement ZoomableViewer Interface ----------------------------

  @Override
  public void setViewport(Rectangle2D value) {
  }

  @Override
  public Rectangle2D getViewport() {
    return null;
  }

  @Override
  public double getMediaWidth() {
    return 0;
  }

  @Override
  public double getMediaHeight() {
    return 0;
  }

  @Override
  public double getFitWidth() {
    return mediaView.getFitWidth();
  }

  @Override
  public double getFitHeight() {
    return mediaView.getFitHeight();
  }

  @Override
  public void installResizeHandler() {
  }

  @Override
  public void zoomToFit() {
  }

}
