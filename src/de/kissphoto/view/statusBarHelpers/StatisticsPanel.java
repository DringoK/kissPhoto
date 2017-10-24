package de.kissphoto.view.statusBarHelpers;

import de.kissphoto.helper.I18Support;
import de.kissphoto.helper.StringHelper;
import de.kissphoto.view.FileTableView;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.ResourceBundle;

/**
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * Standard-Status-Bar - Display showing statistics:
 * It shows the number of files, selected, modified and deleted
 * A double-click on deleted-number opens the undelete dialog
 * <p/>
 *
 * @author Dr. Ingo Kreuz
 * @date: 2017-10-24
 * @modified:
 */

public class StatisticsPanel extends HBox {
  private static ResourceBundle language = I18Support.languageBundle;

  private final static String filesCaption = language.getString("filesStatistic");
  private final static String selectedCaption = language.getString("selectedStatistic");
  private final static String modifiedCaption = language.getString("modifiedStatistic");
  private final static String deletedCaption = language.getString("deletedStatistic");

  private Label filesLabel = new Label(filesCaption + "0");
  private Label selectedLabel = new Label(selectedCaption + "0");
  private Label modifiedLabel = new Label(modifiedCaption + "0");
  private Label deletedLabel = new Label(deletedCaption + "0");

  /**
   * @constructor
   */
  public StatisticsPanel() {

    //filesLabel.setStyle("-fx-border-width: 1;-fx-border-color:grey");
    //selectedLabel.setStyle("-fx-border-width: 1;-fx-border-color:grey");
    //modifiedLabel.setStyle("-fx-border-width: 1;-fx-border-color:grey");
    //deletedLabel.setStyle("-fx-border-width: 1;-fx-border-color:grey");

    //initialize
    showFilesNumber(0);
    showSelectedNumber(0);
    showModifiedNumber(0);
    showDeletedNumber(0);

    setSpacing(0);
    getChildren().addAll(filesLabel, selectedLabel, deletedLabel, modifiedLabel);
  }

  public void connectUndeleteDialog(FileTableView fileTableView) {
    //register Double-Click on Deleted-Label for Undelete-Dialog
    deletedLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        if (event.getClickCount() > 1) {
          fileTableView.unDeleteWithDialog();
        }
      }
    });
  }

  public void showFilesNumber(int filesNumber) {
    filesLabel.setText(" " + filesCaption + filesNumber + " ");
    filesLabel.setPrefWidth(StringHelper.computeTextWidth(filesLabel.getFont(), filesLabel.getText(), 0.0D) + 5);
  }

  public void showSelectedNumber(int selectedNumber) {
    if (selectedNumber > 1) {
      selectedLabel.setText(" " + selectedCaption + selectedNumber + " ");
      selectedLabel.setVisible(true);
      selectedLabel.setPrefWidth(StringHelper.computeTextWidth(selectedLabel.getFont(), selectedLabel.getText(), 0.0D) + 5);
      //selectedLabel.setMinWidth(Region.USE_PREF_SIZE);
    } else {
      selectedLabel.setVisible(false);
      selectedLabel.setPrefWidth(0);
    }
  }

  public void showModifiedNumber(int modifiedNumber) {
    if (modifiedNumber > 0) {
      modifiedLabel.setText(" " + modifiedCaption + modifiedNumber + " ");
      modifiedLabel.setVisible(true);
      modifiedLabel.setPrefWidth(StringHelper.computeTextWidth(modifiedLabel.getFont(), modifiedLabel.getText(), 0.0D) + 5);
    } else {
      modifiedLabel.setVisible(false);
      modifiedLabel.setPrefWidth(0);
    }
  }

  public void showDeletedNumber(int deletedNumber) {
    if (deletedNumber > 0) {
      deletedLabel.setText(" " + deletedCaption + deletedNumber + " ");
      deletedLabel.setVisible(true);
      deletedLabel.setPrefWidth(StringHelper.computeTextWidth(deletedLabel.getFont(), deletedLabel.getText(), 0.0D) + 5);
    } else {
      deletedLabel.setVisible(false);
      deletedLabel.setPrefWidth(0);
    }
  }
}
