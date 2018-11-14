package de.kissphoto.view;

import de.kissphoto.helper.I18Support;
import de.kissphoto.view.statusBarHelpers.StatisticsPanel;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The status bar is shown at the bottom of the main window
 * it can show one line of text-information on top of a progress bar
 * and a statistics area at the right bottom side showing number of files, number of selected files etc.
 * <p/>
 * User: Ingo
 * Date: 20.09.12
 * modified: 2014-05-02 (I18Support)
 * modified: 2017-10-28 statistics and progressbar added
 */
public class StatusBar extends StackPane {
  private static ResourceBundle language = I18Support.languageBundle;

  BorderPane messageAndStatistics = new BorderPane();
  Text textInformation = new Text(language.getString("ready"));
  StatisticsPanel statisticsPanel = new StatisticsPanel();
  ProgressBar progressBar = new ProgressBar(0);

  MenuBar background = new MenuBar();       //Empty menu bar as the background to get the same color as the top of the window
  Menu dummyMenu = new Menu("");       //empty menu for menu bar - otherwise the menu bar will not be displayed

  public StatusBar() {
    //message  and statistics
    textInformation.setFill(Color.WHITE);
    setAlignment(textInformation, Pos.CENTER_LEFT);

    //progress bar
    progressBar.setVisible(false);
    progressBar.prefWidthProperty().bind(this.widthProperty().divide(2)); //progressBar uses half the width of

    messageAndStatistics.setLeft(textInformation);
    messageAndStatistics.setCenter(progressBar);
    messageAndStatistics.setRight(statisticsPanel);
    messageAndStatistics.setPadding(new Insets(4));

    //background = empty menu
    background.prefWidthProperty().bind(this.widthProperty());
    background.getMenus().add(dummyMenu);

    //background and statusContainer in Status Bar
    getChildren().addAll(background, messageAndStatistics);
  }

  //Text-Message
  public void showMessage(String text) {
    textInformation.setFill(Color.BLUE);
    textInformation.setText(text);
  }

  public void showError(String text) {
    textInformation.setFill(Color.INDIANRED);
    textInformation.setText(text);
  }

  public void clearMessage() {
    showMessage(language.getString("ready"));
  }

  //Progress
  public void showProgressBar() {
    progressBar.setVisible(true);
  }

  public void clearProgress() {
    progressBar.setVisible(false);
    progressBar.progressProperty().unbind(); //unregister all listenings...
    progressBar.setProgress(0);
  }

  //Statistics
  public void connectUndeleteDialog(FileTableView fileTableView) {
    statisticsPanel.connectUndeleteDialog(fileTableView);
  }

  //for binding with kissPhoto.ProgressProperty
  public DoubleProperty getProgressProperty() {
    return progressBar.progressProperty();
  }

  public void showFilesNumber(int filesNumber) {
    statisticsPanel.showFilesNumber(filesNumber);
  }

  public void showSelectedNumber(int selectedNumber) {
    statisticsPanel.showSelectedNumber(selectedNumber);
  }

  public void showModifiedNumber(int modifiedNumber) {
    statisticsPanel.showModifiedNumber(modifiedNumber);
  }

  public void showDeletedNumber(int deletedNumber) {
    statisticsPanel.showDeletedNumber(deletedNumber);
  }
}
