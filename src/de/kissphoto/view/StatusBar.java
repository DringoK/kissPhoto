package de.kissphoto.view;

import de.kissphoto.helper.I18Support;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The status bar is shown at the bottom of the main window
 * it can show one line of text-information on top of a progress bar
 * <p/>
 * User: Ingo
 * Date: 20.09.12
 * modified: 2014-05-02 (I18Support)
 */
public class StatusBar extends StackPane {
  private static ResourceBundle language = I18Support.languageBundle;

  StackPane statusContainer = new StackPane();
  Text textInformation = new Text(language.getString("ready"));
  ProgressBar progressBar = new ProgressBar();
  MenuBar background = new MenuBar();         //Empty menu bar as the background to get the same color as the top of the window
  Menu dummyMenu = new Menu("");              //empty menu for menu bar - otherwise the menu bar will not be displayed

  public StatusBar() {
    background.prefWidthProperty().bind(this.widthProperty());
    background.getMenus().add(dummyMenu);

    statusContainer.getChildren().addAll(textInformation, progressBar);
    statusContainer.setPadding(new Insets(5.0));

    this.getChildren().addAll(background, statusContainer);

    textInformation.setFill(Color.WHITE);
    this.setAlignment(textInformation, Pos.CENTER_LEFT);
    this.setAlignment(progressBar, Pos.CENTER_RIGHT);

    progressBar.setVisible(false);
  }

  public void showMessage(String text) {
    textInformation.setFill(Color.BLUE);
    textInformation.setText(text);
  }

  public void showError(String text) {
    textInformation.setFill(Color.INDIANRED);
    textInformation.setText(text);
  }

  public void clear() {
    showMessage("");
  }

}
