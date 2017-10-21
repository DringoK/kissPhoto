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
 * @modified: 2017-10-08 fixed: while zooming not the complete space of the surrounding Pane has been used
 * @modified: 2017-10-21: Event-Handling (mouse/keyboard) centralized, so that viewport events and player viewer events can be handled
 */

public class ViewportZoomer {
  public static ResourceBundle language = I18Support.languageBundle;

  //link back to the viewer which uses this helper
  ZoomableViewer viewer;

  //a number between 0 and 1 (i.e. 0..100%) means Image is displayed smaller than original size(zoomed out)
  //a number >1 (i.e. 100%) is displayed larger than original size means the Image is zoomed in
  private static final double ZOOM_STEP_FACTOR = 1.1;   //e.g. 1.1= 110%:zoomIn=zoomFactor*ZOOM_STEP_FACTOR, zoomOut=zoomFactor/ZOOM_STEP_FACTOR
  private static final double MOVE_STEP_FACTOR = 0.10;  //e.g. 0.10 = 10%: percentage of the visible part the viewport is moved by the 4 move methods
  private double zoomFactor = 0;  //will be initialized in initializeZooming(). Is the same for all type of media!

  ContextMenu contextMenu = null; //initialized in installContextMenu()

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

    ((Node) viewer).setCursor(Cursor.OPEN_HAND);
    viewer.installResizeHandler();
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
    if (viewer.getViewport() == null) initializeZooming();  //if zoomToFit

    //zoom: Viewport has shape of viewer
    double newWidth = viewer.getFitWidth() / newZoomFactor;
    double newHeight = viewer.getFitHeight() / newZoomFactor;

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

  /**
   * Whenever the size of the viewer is changed, than this method must be called
   * It corrects the viewport when zooming is active, to use as much space as possible
   */
  public void handleResize() {
    if (viewer.getViewport() != null) { //only if zooming was acitive
      zoom(zoomFactor); //zoom to same zoomfactor again to reset the viewport and to fit it into the new window
    }

  }
  //---------------------------------- Moving  -------------------------------------

  //remember:
  // if viewport is null then zoom to fit
  // Viewport is a rectangle 2D in coordinates of the image (not the view)
  // --> when you want to move the picture to the left, the viewport must be moved to the right
  // the image area described by the viewport will be shown and zoomed into ImageView

  /**
   * the viewport is centered horizontally relatively to the image
   * this makes especially sense if the image is horizontally smaller than >zoomTofit<
   * if no viewport is defined yet (zoomToFit is active) nothing will happen
   */
  public void centerMediaX() {
    Rectangle2D viewport = viewer.getViewport();
    if (viewport == null) return;

    double newX = (viewer.getMediaWidth() - viewport.getWidth()) / 2;

    viewer.setViewport(new Rectangle2D(newX, viewport.getMinY(), viewport.getWidth(), viewport.getHeight()));
  }

  /**
   * the viewport is centered vertically relatively to the image
   * this makes especially sense if the image is vertically smaller than >zoomTofit<
   * if no viewport is defined yet (zoomToFit is active) nothing will happen
   */
  public void centerMediaY() {
    Rectangle2D viewport = viewer.getViewport();
    if (viewport == null) return;

    double newY = (viewer.getMediaHeight() - viewport.getHeight()) / 2;

    viewer.setViewport(new Rectangle2D(viewport.getMinX(), newY, viewport.getWidth(), viewport.getHeight()));
  }

  /**
   * move the viewport to the center of the Image
   * if the complete image is visible in the viewport moving is ignored
   * if no viewport is defined yet (zoomToFit is active) nothing will happen
   */
  public void centerMedia() {
    Rectangle2D viewport = viewer.getViewport();
    if (viewport == null) return;

    double newX = (viewer.getMediaWidth() - viewport.getWidth()) / 2;
    double newY = (viewer.getMediaHeight() - viewport.getHeight()) / 2;

    viewer.setViewport(new Rectangle2D(newX, newY, viewport.getWidth(), viewport.getHeight()));

  }

  /**
   * move the viewport right by MOVE_STEP_FACTOR percentage of the visible part
   * note: image will visibly move to the other side but cursor will move viewport not picture!!!
   * stop/ignore if right border of the image is already visible
   * if the complete width of the image is visible in the viewport is horizontally centered on the picture=picture is centered

   * if no viewport is defined yet (zoomToFit is active) nothing will happen
   */
  public void moveRight() {
    Rectangle2D viewport = viewer.getViewport();
    if (viewport == null) return;

    //if not the complete width is visible then move
    if (viewport.getWidth() < viewer.getMediaWidth()) {
      double newX = viewport.getMinX() + (viewport.getWidth() * MOVE_STEP_FACTOR);    //move ...percent of the viewport
      if ((newX + viewport.getWidth()) > viewer.getMediaWidth()) {
        newX = viewer.getMediaWidth() - viewport.getWidth();
      }
      viewer.setViewport(new Rectangle2D(newX, viewport.getMinY(), viewport.getWidth(), viewport.getHeight()));
    } else {
      centerMediaX();
    }
  }

  /**
   * move the viewport left by MOVE_STEP_FACTOR percentage of the visible part
   * note: image will visibly move to the other side but cursor will move viewport not picture!!!
   * stop/ignore if left border of the image is already visible
   * if the complete width of the image is visible in the viewport is horizontally centered on the picture=picture is centered
   *
   * if no viewport is defined yet (zoomToFit is active) nothing will happen
   */
  public void moveLeft() {
    Rectangle2D viewport = viewer.getViewport();
    if (viewport == null) return;

    //if not the complete width is visible then move
    if (viewport.getWidth() < viewer.getMediaWidth()) {
      double newX = viewport.getMinX() - (viewport.getWidth() * MOVE_STEP_FACTOR);    //move ...percent of the viewport
      if (newX < 0) {
        newX = 0;
      }
      viewer.setViewport(new Rectangle2D(newX, viewport.getMinY(), viewport.getWidth(), viewport.getHeight()));
    } else {
      centerMediaX();
    }
  }

  /**
   * move the viewport down by MOVE_STEP_FACTOR percentage of the visible part
   * note: image will visibly move to the other side but cursor will move viewport not picture!!!
   * stop/ignore if lower border of the image is already visible
   * if the complete height of the image is visible in the viewport is vertically centered on the picture=picture is centered

   * if no viewport is defined yet (zoomToFit is active) nothing will happen
   */
  public void moveDown() {
    Rectangle2D viewport = viewer.getViewport();
    if (viewport == null) return;

    //if not the complete height is visible then move
    if (viewport.getHeight() < viewer.getMediaHeight()) {
      double newY = viewport.getMinY() + (viewport.getHeight() * MOVE_STEP_FACTOR);    //move ...percent of the viewport
      if ((newY + viewport.getHeight()) > viewer.getMediaHeight()) {
        newY = viewer.getMediaHeight() - viewport.getHeight();
      }
      viewer.setViewport(new Rectangle2D(viewport.getMinX(), newY, viewport.getWidth(), viewport.getHeight()));
    } else {
      centerMediaY();
    }
  }

  /**
   * move the viewport up by MOVE_STEP_FACTOR percentage of the visible part
   * note: image will visibly move to the other side but cursor will move viewport not picture!!!
   * stop/ignore if upper border of the image is already visible
   * if the complete height of the image is visible in the viewport is vertically centered on the picture=picture is centered
   *
   * if no viewport is defined yet (zoomToFit is active) nothing will happen
   */
  public void moveUp() {
    Rectangle2D viewport = viewer.getViewport();
    if (viewport == null) return;

    //if not the complete height is visible then move
    if (viewport.getHeight() < viewer.getMediaHeight()) {
      double newY = viewport.getMinY() - (viewport.getHeight() * MOVE_STEP_FACTOR);    //move ...percent of the viewport
      if (newY < 0) {
        newY = 0;
      }
      viewer.setViewport(new Rectangle2D(viewport.getMinX(), newY, viewport.getWidth(), viewport.getHeight()));
    } else {
      centerMediaY();
    }
  }

  //see also handleMouseMoved() for moving with mouse

  /**
   * installs a context menu to the viewers mediaContent view and adds the events handlers for showing/hiding the context menu
   *
   * @param mediaContentView where to add the context Menu to
   * @param contextMenu      the context menu to add to the mediaContentView
   */
  public void installContextMenu(MediaContentView mediaContentView, ContextMenu contextMenu) {
    this.contextMenu = contextMenu;

    mediaContentView.addContextMenuItems(contextMenu);  //every viewer of kissPhoto lies in a MediaContentView

    //----------------- install event handlers

    ((Node) viewer).setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
      @Override
      public void handle(ContextMenuEvent contextMenuEvent) {
        contextMenu.setAutoHide(true);
        contextMenu.show(mediaContentView, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
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

    MenuItem zoomFitItem = new MenuItem(language.getString("zoom.to.fit.middle.mouse.button")); //ctrl-space
    zoomFitItem.setAccelerator(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN));
    zoomFitItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        zoomToFit();
        actionEvent.consume();
      }
    });

    MenuItem zoom100Item = new MenuItem(language.getString("zoom.100")); //shift-space
    zoom100Item.setAccelerator(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHIFT_DOWN));
    zoom100Item.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        zoom100();
        actionEvent.consume();
      }
    });

    contextMenu.getItems().addAll(zoomInItem, zoomOutItem, zoomFitItem, zoom100Item);

    //---- moving menu menu items
    MenuItem moveToCenterItem = new MenuItem(language.getString("move.to.center"));//ctrl-shift-space
    moveToCenterItem.setAccelerator(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
    moveToCenterItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        centerMedia();
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

  //----------- mouse support ----------------
  //zooming

  /**
   * @param scrollEvent
   * @return if event has been handled
   */
  public boolean handleMouseScroll(ScrollEvent scrollEvent) {
    boolean handled = false;
    if (scrollEvent.isControlDown()) {
      if (scrollEvent.getDeltaY() > 0)
        zoomIn();
      else
        zoomOut();
      handled = true;
    }
    return handled;
  }

  //dragging = moving viewport

  /**
   * @param mouseEvent
   * @return if event has been handled
   */
  public boolean handleMousePressed(MouseEvent mouseEvent) {
    boolean handled = false;

    //---initialize drag origin for moving a zoomed image
    if (viewer.getViewport() != null) { //only if zooming is currently active
      mouseDownX = mouseEvent.getX();
      mouseDownY = mouseEvent.getY();
      mouseDownMinX = viewer.getViewport().getMinX();
      mouseDownMinY = viewer.getViewport().getMinY();
      handled = true;
    }
    //-- pressing mouse wheel resets zooming
    if (mouseEvent.isMiddleButtonDown()) {
      zoomToFit();
      handled = true;
    }

    ((Node) viewer).setCursor(Cursor.CLOSED_HAND);
    return handled;
  }

  /**
   * @param mouseEvent
   * @return if event has been handled
   */
  public boolean handleMouseDragged(MouseEvent mouseEvent) {
    boolean handled = false;
    double newX;
    double newY;
    Rectangle2D viewport = viewer.getViewport();
    if (viewport != null) { //only if zooming is currently active
      if (mouseEvent.isPrimaryButtonDown()) {
        if (viewport.getWidth() < viewer.getMediaWidth()) {
          newX = mouseDownMinX + ((mouseDownX - mouseEvent.getX()) / zoomFactor);
          if (newX < 0) {
            newX = 0;
          } else if ((newX + viewport.getWidth()) > viewer.getMediaWidth()) {
            newX = viewer.getMediaWidth() - viewport.getWidth();
          }
        } else {
          //centerMediaX;
          newX = (viewer.getMediaWidth() - viewport.getWidth()) / 2;
        }

        if (viewport.getHeight() < viewer.getMediaHeight()) {
          newY = mouseDownMinY + ((mouseDownY - mouseEvent.getY()) / zoomFactor);
          if (newY < 0) {
            newY = 0;
          } else if ((newY + viewport.getHeight()) > viewer.getMediaHeight()) {
            newY = viewer.getMediaHeight() - viewport.getHeight();
          }
        } else {
          newY = (viewer.getMediaHeight() - viewport.getHeight()) / 2;
        }
        viewer.setViewport(new Rectangle2D(newX, newY, viewer.getViewport().getWidth(), viewer.getViewport().getHeight()));
        handled = true;
      }
    }
    return handled;
  }

  /**
   * @param mouseEvent
   * @return if event has been handled
   */
  public boolean handleMouseReleased(MouseEvent mouseEvent) {
    ((Node) viewer).setCursor(Cursor.OPEN_HAND);
    return false;
  }

  /**
   * @param mouseEvent
   * @return if envent has been handled
   */
  public boolean handleMouseClicked(MouseEvent mouseEvent) {
    boolean handled = false;
    //hide context menu if clicked "somewhere else" or request focus on mouse click
    if (contextMenu.isShowing()) {
      contextMenu.hide(); //this closes the context Menu
      handled = true;
    }
    //if the user clicks on an element he expects to set the focus there (for keyboard zooming/panning)
    ((Node) viewer).requestFocus();
    return handled;
  }

  //------------- keyboard support ---------------

  /**
   * @param event
   * @return if event has been handled
   */
  public boolean handleKeyPressed(KeyEvent event) {

    boolean eventHandled = false;

    if (viewer.getViewport() != null) { //zooming/panning only if viewport ist active
      switch (event.getCode()) {
        case SPACE:
          if (event.isControlDown() && event.isShiftDown()) {
            centerMedia();
            eventHandled = true;
          } else if (event.isControlDown()) {
            zoomToFit();
            eventHandled = true;
          } else if (event.isShiftDown()) {
            zoom100();
            eventHandled = true;
          }
          break;

        case LEFT:
          moveLeft();
          eventHandled = true;
          break;
        case RIGHT:
          moveRight();
          eventHandled = true;
          break;
        case UP:
          moveUp();
          eventHandled = true;
          break;
        case DOWN:
          moveDown();
          eventHandled = true;
          break;
        default:
          eventHandled = false;
      }
    }

    //--- Zoom in/out is always possible: if not already zoomed, zooming is started
    if (!eventHandled) {
      switch (event.getText()) {
        case "+":
          zoomIn();
          eventHandled = true;
          break;//get numpad and normal keys
        case "-":
          zoomOut();
          eventHandled = true;
          break;
        default:
          eventHandled = false;
      }
    }
    return eventHandled;
  }

  public double getZoomFactor() {
    return zoomFactor;
  }

  //------------------------------- Helpers ------------------------------------
  private double getViewportCenterY() {
    return (viewer.getViewport().getMinY() + viewer.getViewport().getMaxY()) / 2;
  }

  private double getViewportCenterX() {
    return (viewer.getViewport().getMinX() + viewer.getViewport().getMaxX()) / 2;
  }


}