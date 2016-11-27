package de.kissphoto.view.mediaViewers;

import de.kissphoto.helper.I18Support;
import de.kissphoto.view.MediaContentView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.*;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)<br><br>
 * <br>
 * This is a helper Class vor Viewers implementing the interface ZoomableViewer (e.g. PhotoViewer and MovieViewer)
 * All routines for Zooming an moving with the viewport are same for these classes and therefore implemented here uniquely
 *
 * @author Dr. Ingo Kreuz
 * @date 2016-11-06 all zooming/moving routines moved from PhotoViewer to this class
 * @modified:
 */

public class ViewportZoomer {
  public static ResourceBundle language = I18Support.languageBundle;

  //link back to the viewer which uses this helper
  ZoomableViewer viewer;

  //a number between 0 and 1 (i.e. 0..100%) means Image is displayed smaller than original size(zoomed out)
  //a number >1 (i.e. 100%) is displayed larger than original size means the Image is zoomed in
  private static final double ZOOM_STEP_FACTOR = 1.1;   //e.g. 1.1= 110%:zoomIn=zoomFactor*ZOOM_STEP_FACTOR, zoomOut=zoomFactor/ZOOM_STEP_FACTOR
  private static final double MOVE_STEP_FACTOR = 0.10;  //e.g. 0.10 = 10%: percentage of the visible part the viewport is moved by the 4 move methods
  private static final double KEEP_PERCENT_VISIBLE = 0.05; //e.g. 0.05 = 5% when panning the picture by keyboard (arrow keys) keep some pixels visible when moving out
  private double zoomFactor = 0;  //will be initialized in initializeZooming().

  //--------------------- mouse support -------------------------
  private double mouseDownX = 0;  //remember where dragging of mouse
  private double mouseDownY = 0;
  private double mouseDownMinX = 0;
  private double mouseDownMinY = 0;

  /**
   * @param viewer link back to the viewer which uses this helper
   * @constructor
   */
  public ViewportZoomer(ZoomableViewer viewer) {
    this.viewer = viewer;

    installZoomMouseEventHandlers();
    installKeyboardEventHandler();
  }


  private void handleMouseScrollEvent(ScrollEvent scrollEvent) {
    if (scrollEvent.isControlDown()) {
      if (scrollEvent.getDeltaY() > 0)
        zoomIn();
      else
        zoomOut();
      scrollEvent.consume();
    }
  }

  private void handleMouseDown(MouseEvent mouseEvent) {

    //---initialize drag origin for moving a zoomed image
    if (viewer.getViewport() != null) { //only if zooming is currently active
      mouseDownX = mouseEvent.getX();
      mouseDownY = mouseEvent.getY();
      mouseDownMinX = viewer.getViewport().getMinX();
      mouseDownMinY = viewer.getViewport().getMinY();
      mouseEvent.consume();
    }

    //-- pressing mouse wheel resets zooming
    if (mouseEvent.isMiddleButtonDown()) {
      zoomToFit();
    }
  }

  private void handleMouseDragged(MouseEvent mouseEvent) {
    if (viewer.getViewport() != null) { //only if zooming is currently active
      if (mouseEvent.isPrimaryButtonDown()) {
        double newX = mouseDownMinX + ((mouseDownX - mouseEvent.getX()) / zoomFactor);
        double newY = mouseDownMinY + ((mouseDownY - mouseEvent.getY()) / zoomFactor);
        viewer.setViewport(new Rectangle2D(newX, newY, viewer.getViewport().getWidth(), viewer.getViewport().getHeight()));
      }
      mouseEvent.consume();
    }
  }


  //---------------------------------- Zooming -------------------------------------

  /**
   * set a viewport with the same zoom factor as it was when zoomToFit was active
   * initialize the currentZoom property
   * If the Viewport was already set (zooming was already active) nothing happens
   */
  private void initializeZooming() {
    if (viewer.getViewport() == null) {
      double ratioX = viewer.getFitWidth() / viewer.getMediaWidth();
      double ratioY = viewer.getFitHeight() / viewer.getMediaHeight();
      //the smaller value of these is the current "limiting factor" when displaying the picture
      zoomFactor = Math.min(ratioX, ratioY);

      viewer.setViewport(new Rectangle2D(0, 0, viewer.getMediaWidth(), viewer.getMediaHeight())); //at first the viewport shows the complete Image
    }

  }


  /**
   * zoom in by the factor ZOOM_STEP_FACTOR
   */
  public void zoomIn() {
    if (viewer.getViewport() == null) initializeZooming();
    zoom(zoomFactor * ZOOM_STEP_FACTOR);  //zooming in increases the zoomFactor
  }

  /**
   * zoom out by the factor ZOOM_STEP_FACTOR
   */
  public void zoomOut() {
    if (viewer.getViewport() == null) initializeZooming();
    zoom(zoomFactor / ZOOM_STEP_FACTOR);  //zooming out decreases the zoomFactor
  }

  /**
   * zoom to 100% i.e. one pixel of the image is one pixel of the screen
   */
  public void zoom100() {
    if (viewer.getViewport() == null) initializeZooming();
    zoom(1);
  }


  /**
   * The viewport's size of the ImageView will be adjusted so that a given zoomFactor is gained
   * At the same time the viewport is moved to keep the center of the visible part of the image.
   * Zoomfactor 100% means one screen pixel is one pixel of the image
   *
   * @param newZoomFactor 1=100%, 0.5=50%, etc.
   */
  public void zoom(double newZoomFactor) {
    if (viewer.getViewport() == null) initializeZooming();

    //zoom
    double newWidth = Math.min(viewer.getFitWidth(), viewer.getMediaWidth()) / newZoomFactor;
    double newHeight = Math.min(viewer.getFitHeight(), viewer.getMediaHeight()) / newZoomFactor;

    //move viewport to keep center of visible part
    double minX = getViewportCenterX() - (newWidth / 2);  // :2= center
    double minY = getViewportCenterY() - (newHeight / 2);

    viewer.setViewport(new Rectangle2D(minX, minY, newWidth, newHeight));
    zoomFactor = newZoomFactor;
  }

  /**
   * zoom to the given rectangle (e.g. selected by mouse-down/move/up):
   * determine the new zoomFactor as minimum of horizontal or vertical factor defined by the rectangle
   * set the center of the rectangle as the new center of the viewport
   *
   * @param minX upper left point ("mouse down") in screen coordinates
   * @param minY upper left point ("mouse down") in screen coordinates
   * @param maxX lower right point ("mouse up") in screen coordinates
   * @param maxY lower right point ("mouse up") in screen coordinates
   */
  public void zoom(double minX, double minY, double maxX, double maxY) {
    if (viewer.getViewport() == null) initializeZooming();

    //determine minimum zoomFactor as newZoomFactor;
    double ratioX = Math.abs(maxX - minX) * zoomFactor / viewer.getMediaWidth();
    double ratioY = Math.abs(maxY - minY) * zoomFactor / viewer.getMediaHeight();
    //the smaller value of these is the current "limiting factor" when displaying the picture
    double newZoomFactor = Math.min(ratioX, ratioY);

    //zoom
    double newWidth = Math.min(viewer.getFitWidth(), viewer.getMediaWidth()) / newZoomFactor;    //in Screen-Coordinates
    double newHeight = Math.min(viewer.getFitHeight(), viewer.getMediaHeight()) / newZoomFactor;

    //move viewport to center to the center of the rectangle
    double newMinX = (Math.abs(maxX - minX) / 2) - (newWidth / 2);  // :2= center
    double newMinY = (Math.abs(maxY - minY) / 2) - (newHeight / 2);

    viewer.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
    zoomFactor = newZoomFactor;
  }

  /**
   * resetPlayer zooming: The complete image is shown, zooming and panning disabled (viewport is deleted)
   */
  public void zoomToFit() {
    viewer.setViewport(null);
  }


  //---------------------------------- Moving  -------------------------------------

  //remember:
  // if viewport is null then zoom to fit
  // Viewport is a rectangle 2D in coordinates of the image (not the view)
  // --> when you want to move the picture to the left, the viewport must be moved to the right
  // the image area described by the viewport will be shown and zoomed into ImageView

  /**
   * move the viewport right by MOVE_STEP_FACTOR percentage of the visible part
   * ignore if left image border is already at the right rim of the view (KEEP_PIXELS_VISIBLE pixels remain visible)
   * if the complete image is visible in the viewport moving is ignored
   * if no viewport is defined yet nothing will happen
   */
  public void moveRight() {
    if (viewer.getViewport() == null) return;

    double newX = viewer.getViewport().getMinX() - (viewer.getViewport().getWidth() * MOVE_STEP_FACTOR);
    if (newX > (viewer.getFitWidth() * (1 - KEEP_PERCENT_VISIBLE)) / zoomFactor - 2 / zoomFactor)
      newX = -viewer.getFitWidth() / zoomFactor + 2 / zoomFactor; //stop when right border of the image is at left rim (KEEP_PERCENT_VISIBLE still visible)

    viewer.setViewport(new Rectangle2D(newX, viewer.getViewport().getMinY(), viewer.getViewport().getWidth(), viewer.getViewport().getHeight()));
  }

  /**
   * move the picture left by moving the viewport right by MOVE_STEP_FACTOR percentage of the visible part
   * ignore if viewport is already at the right image border (KEEP_PERCENT_VISIBLE pixels remain visible)
   *
   * @todo: PERCENT VISIBLE überdenken: je nachdem wie groß die Vergrößerung ist, sollte sich das ändern (KEEP_PERCENT_VISIBLE verrechnen mit zoomFactor???)
   * if no viewport is defined yet nothing will happen
   * @todo: andere move-Richtungen übertragen
   */
  public void moveLeft() {
    if (viewer.getViewport() == null) return;

    double newX = viewer.getViewport().getMinX() + (viewer.getViewport().getWidth() * MOVE_STEP_FACTOR);   //as the viewport moves right over the picture, the picture seems to move left!
    if (newX > (viewer.getMediaWidth() * (1 - KEEP_PERCENT_VISIBLE))) // stop when viewport leaves image width (keep_percent_visible% before;-)
      newX = (viewer.getMediaWidth() * (1 - KEEP_PERCENT_VISIBLE));

    viewer.setViewport(new Rectangle2D(newX, viewer.getViewport().getMinY(), viewer.getViewport().getWidth(), viewer.getViewport().getHeight()));
  }

  /**
   * move the viewport down by MOVE_STEP_FACTOR percentage of the visible part
   * ignore if top rim of the image is already at the bottom of the view (KEEP_PIXELS_VISIBLE pixels remain visible)
   * if the complete image is visible in the viewport moving is ignored
   * if no viewport is defined yet nothing will happen
   */
  public void moveDown() {
    if (viewer.getViewport() == null) return;

    double newY = viewer.getViewport().getMinY() - (viewer.getViewport().getHeight() * MOVE_STEP_FACTOR);
    if (-newY > viewer.getFitHeight() / zoomFactor - 2 / zoomFactor)
      newY = -viewer.getFitHeight() / zoomFactor + 2 / zoomFactor; //stop when right border of the image is at left rim (two pixels still visible)

    viewer.setViewport(new Rectangle2D(viewer.getViewport().getMinX(), newY, viewer.getViewport().getWidth(), viewer.getViewport().getHeight()));
  }

  /**
   * move the viewport up by MOVE_STEP_FACTOR percentage of the visible part
   * ignore if lower rim of picture is already at the top of the view (KEEP_PIXELS_VISIBLE pixels remain visible)
   * if the complete image is visible in the viewport moving is ignored
   * if no viewport is defined yet nothing will happen
   */
  public void moveUp() {
    if (viewer.getViewport() == null) return;

    double newY = viewer.getViewport().getMinY() + (viewer.getViewport().getHeight() * MOVE_STEP_FACTOR);
    if (newY > viewer.getFitHeight() - 2 / zoomFactor)
      newY = viewer.getFitHeight() - 2 / zoomFactor; //stop when left border of the image is at right rim (two pixels still visible)

    viewer.setViewport(new Rectangle2D(viewer.getViewport().getMinX(), newY, viewer.getViewport().getWidth(), viewer.getViewport().getHeight()));
  }

  /**
   * move the viewport to the center of the Image
   * if the complete image is visible in the viewport moving is ignored
   * if no viewport is defined yet nothing will happen
   */
  public void moveToCenter() {
    if (viewer.getViewport() == null) return;

    double newX = (viewer.getMediaWidth() - viewer.getViewport().getWidth()) / 2;
    double newY = (viewer.getMediaHeight() - viewer.getViewport().getHeight()) / 2;

    viewer.setViewport(new Rectangle2D(newX, newY, viewer.getViewport().getWidth(), viewer.getViewport().getHeight()));
  }

  /**
   * installs a context menu to the viewers mediaContent view and adds the events handlers for showing/hiding the context menu
   *
   * @param mediaContentView where to add the context Menu to
   * @param contextMenu      the context menu to add to the mediaContentView
   */
  public void installContextMenu(MediaContentView mediaContentView, ContextMenu contextMenu) {

    mediaContentView.addContextMenuItems(contextMenu);  //every viewer of kissPhoto lies in a MediaContentView

    //----------------- install event handlers

    ((Node) viewer).setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
      @Override
      public void handle(ContextMenuEvent contextMenuEvent) {
        contextMenu.setAutoHide(true);
        contextMenu.show(mediaContentView, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
      }
    });
    //hide context menu if clicked "somewhere else" or request focus on mouse click
    ((Node) viewer).setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (contextMenu.isShowing()) {
          contextMenu.hide(); //this closes the context Menu
          mouseEvent.consume();
        } else {
          ((Node) viewer).requestFocus();
        }
      }
    });

  }

  /**
   * Build all ContextMenu items for Zooming and Panning/Moving supported by this class
   * either by adding to an existing Context Menu or (if null is passed into) to an new one
   *
   * @param existingContextMenu if existing (not null) the zooming/panning items are just added, if null, then a new context menu is generated
   */
  public ContextMenu addContextMenuItems(ContextMenu existingContextMenu) {
    ContextMenu contextMenu;

    //take over existing contextMenu or generate new one
    if (existingContextMenu != null)
      contextMenu = existingContextMenu;
    else
      contextMenu = new ContextMenu();

    //---- zooming menu menu items
    MenuItem zoomInItem = new MenuItem(language.getString("zoom.in.ctrl.mouse.wheel.up")); //+
    zoomInItem.setAccelerator(new KeyCodeCombination(KeyCode.PLUS));
    zoomInItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        zoomIn();
        actionEvent.consume();
      }
    });

    MenuItem zoomOutItem = new MenuItem(language.getString("zoom.out.ctrl.mouse.wheel.down")); //-
    zoomOutItem.setAccelerator(new KeyCodeCombination(KeyCode.MINUS));
    zoomOutItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        zoomOut();
        actionEvent.consume();
      }
    });

    MenuItem zoomFitItem = new MenuItem(language.getString("zoom.to.fit.middle.mouse.button")); //space
    zoomFitItem.setAccelerator(new KeyCodeCombination(KeyCode.SPACE));
    zoomFitItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        zoomToFit();
        actionEvent.consume();
      }
    });

    MenuItem zoom100Item = new MenuItem(language.getString("zoom.100")); //ctrl-space
    zoom100Item.setAccelerator(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN));
    zoom100Item.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        zoom100();
        actionEvent.consume();
      }
    });

    contextMenu.getItems().addAll(zoomInItem, zoomOutItem, zoomFitItem, zoom100Item);

    //---- moving menu menu items
    MenuItem moveToCenterItem = new MenuItem(language.getString("move.to.center"));//shift-space
    moveToCenterItem.setAccelerator(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHIFT_DOWN));
    moveToCenterItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        moveToCenter();
        actionEvent.consume();
      }
    });

    MenuItem moveLeftItem = new MenuItem(language.getString("move.left.drag.mouse.left.button"));//left arrow
    moveLeftItem.setAccelerator(new KeyCodeCombination(KeyCode.LEFT));
    moveLeftItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        moveLeft();
        actionEvent.consume();
      }
    });

    MenuItem moveRightItem = new MenuItem(language.getString("move.right.drag.mouse.left.button"));//right arrow
    moveRightItem.setAccelerator(new KeyCodeCombination(KeyCode.RIGHT));
    moveRightItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        moveRight();
        actionEvent.consume();
      }
    });

    MenuItem moveUpItem = new MenuItem(language.getString("move.up.drag.mouse.left.button"));//up arrow
    moveUpItem.setAccelerator(new KeyCodeCombination(KeyCode.UP));
    moveUpItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        moveUp();
        actionEvent.consume();
      }
    });

    MenuItem moveDownItem = new MenuItem(language.getString("move.down.drag.mouse.left.button"));//down arrow
    moveDownItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN));
    moveDownItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        moveDown();
        actionEvent.consume();
      }
    });

    contextMenu.getItems().addAll(new SeparatorMenuItem(), moveToCenterItem, moveLeftItem, moveRightItem, moveUpItem, moveDownItem);

    //---- MediaContentView's menu items
    contextMenu.getItems().add(new SeparatorMenuItem());

    return contextMenu;
  }

  public void installZoomMouseEventHandlers() {
    //----------- mouse support ----------------
    ((Node) viewer).setCursor(Cursor.OPEN_HAND);

    //zooming
    ((Node) viewer).setOnScroll(new EventHandler<ScrollEvent>() {
      @Override
      public void handle(ScrollEvent scrollEvent) {
        handleMouseScrollEvent(scrollEvent);
      }
    });

    //dragging = moving viewport
    ((Node) viewer).setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        handleMouseDown(mouseEvent);
        ((Node) viewer).setCursor(Cursor.CLOSED_HAND);
      }
    });
    ((Node) viewer).setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        handleMouseDragged(mouseEvent);
      }
    });
    ((Node) viewer).setOnMouseReleased(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        ((Node) viewer).setCursor(Cursor.OPEN_HAND);
      }
    });

  }

  public void installKeyboardEventHandler() {
    //------------- keyboard support ---------------
    ((Node) viewer).setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {

        boolean eventHandled = true;

        switch (keyEvent.getCode()) {
          case SPACE:
            if (keyEvent.isControlDown())
              zoom100();
            else if (keyEvent.isShiftDown())
              moveToCenter();
            else
              zoomToFit();
            break;

          case LEFT:
            moveLeft();
            break;
          case RIGHT:
            moveRight();
            break;
          case UP:
            if (viewer.getViewport() != null) //only if currently zoomed
              moveUp();               //pan around
            else                      //otherwise go back directly
              return;                 //not consuming the event and letting MediaContentView move to prev mediaFile
            break;
          case DOWN:
            if (viewer.getViewport() != null) //only if currently zoomed
              moveDown();             //pan around
            else                      //otherwise go back directly
              return;                 //not consuming the event and letting MediaContentView move to next mediaFile
            break;
          default:
            eventHandled = false;
        }

        if (!eventHandled) {
          eventHandled = true;
          switch (keyEvent.getText()) {
            case "+":
              zoomIn();
              break;//get numpad and normal keys
            case "-":
              zoomOut();
              break;
            default:
              eventHandled = false;
          }
        }
        if (eventHandled) keyEvent.consume();
      }
    });

  }

  //------------------------------- Helpers ------------------------------------
  private double getViewportCenterY() {
    return (viewer.getViewport().getMinY() + viewer.getViewport().getMaxY()) / 2;
  }

  private double getViewportCenterX() {
    return (viewer.getViewport().getMinX() + viewer.getViewport().getMaxX()) / 2;
  }


}