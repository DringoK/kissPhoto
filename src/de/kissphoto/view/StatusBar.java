package de.kissphoto.view;

import de.kissphoto.helper.I18Support;
import de.kissphoto.view.statusBarHelpers.ProgressBarPanel;
import de.kissphoto.view.statusBarHelpers.StatisticsPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
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
 * <p/>
 * User: Ingo
 * Date: 20.09.12
 * modified: 2014-05-02 (I18Support)
 * modified: 2017-10-24 statistics and progressbar added
 */
public class StatusBar extends StackPane {
  private static ResourceBundle language = I18Support.languageBundle;

  StackPane statusContainer = new StackPane();
  BorderPane messageAndStatistics = new BorderPane();
  Text textInformation = new Text(language.getString("ready"));
  StatisticsPanel statisticsPanel = new StatisticsPanel();
  ProgressBarPanel progressBarPanel = new ProgressBarPanel();

  MenuBar background = new MenuBar();         //Empty menu bar as the background to get the same color as the top of the window
  Menu dummyMenu = new Menu("");              //empty menu for menu bar - otherwise the menu bar will not be displayed

  public StatusBar() {
    //message  and statistics
    textInformation.setFill(Color.WHITE);
    this.setAlignment(textInformation, Pos.CENTER_LEFT);
    this.setAlignment(statisticsPanel, Pos.CENTER_RIGHT);

    messageAndStatistics.setLeft(textInformation);
    messageAndStatistics.setRight(statisticsPanel);

    //progress bar
    progressBarPanel.setVisible(false);

    //put all in statusContainer
    statusContainer.getChildren().addAll(messageAndStatistics, progressBarPanel);
    statusContainer.setPadding(new Insets(5.0));

    //background = empty menu
    background.prefWidthProperty().bind(this.widthProperty());
    background.getMenus().add(dummyMenu);

    //background and statusContainer in Status Bar
    this.getChildren().addAll(background, statusContainer);
  }

  //Text-Message
  public void showMessage(String text) {
    textInformation.setFill(Color.BLUE);
    textInformation.setText(text);
    messageAndStatistics.setVisible(true);
    progressBarPanel.setVisible(false);
  }

  public void showError(String text) {
    textInformation.setFill(Color.INDIANRED);
    textInformation.setText(text);
    messageAndStatistics.setVisible(true);
    progressBarPanel.setVisible(false);
  }

  public void clearMessage() {
    showMessage(language.getString("ready"));
  }

  //Statistics
  public void connectUndeleteDialog(FileTableView fileTableView) {
    statisticsPanel.connectUndeleteDialog(fileTableView);
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

  //Progress
  public void setProgress(double percent) {
    progressBarPanel.setProgress(percent);
    messageAndStatistics.setVisible(false);
    progressBarPanel.setVisible(true);
  }

  public void setCaption(String newCaption) {
    progressBarPanel.setCaption(newCaption);
  }


}
