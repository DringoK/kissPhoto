package dringo.kissPhoto.view;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.statusBarHelpers.StatisticsPanel;
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

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * The status bar is shown at the bottom of the main window (and in the
 * it can show one line of text-information on top of a progress bar
 * and a statistics area at the right bottom side showing number of files, number of selected files etc.
 * <p/>
 * @author Ingo
 * @since 2012-09-20
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers
 * @version 2017-10-28 statistics and progressbar added
 * @version 2014-05-02 (I18Support)
 */
public class StatusBar extends StackPane {
  BorderPane messageAndStatistics = new BorderPane();
  Text textInformation = new Text(KissPhoto.language.getString("ready"));
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
    showMessage(KissPhoto.language.getString("ready"));
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
