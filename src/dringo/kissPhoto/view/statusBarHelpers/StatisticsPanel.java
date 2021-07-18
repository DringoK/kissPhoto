package dringo.kissPhoto.view.statusBarHelpers;

import dringo.kissPhoto.KissPhoto;
import dringo.kissPhoto.view.FileTableView;
import dringo.kissPhoto.helper.StringHelper;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * Standard-Status-Bar - Display showing statistics:
 * It shows the number of files, selected, modified and deleted
 * A double-click on deleted-number opens the undelete dialog
 * <p/>
 *
 * @author Dringo
 * @since 2017-10-24
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers
 * @version 2019-07-07 inserted missing spaces after ":"
 */

public class StatisticsPanel extends HBox {
  private final static String filesCaption = KissPhoto.language.getString("filesStatistic");
  private final static String selectedCaption = KissPhoto.language.getString("selectedStatistic");
  private final static String modifiedCaption = KissPhoto.language.getString("modifiedStatistic");
  private final static String deletedCaption = KissPhoto.language.getString("deletedStatistic");

  private final Label filesLabel = new Label(filesCaption + " 0");
  private final Label selectedLabel = new Label(selectedCaption + " 0");
  private final Label modifiedLabel = new Label(modifiedCaption + " 0");
  private final Label deletedLabel = new Label(deletedCaption + " 0");

  /**
   * build the panel
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
    //remember: align right, therefore the standard entries last
    getChildren().addAll(deletedLabel, modifiedLabel, selectedLabel, filesLabel);
  }

  public void connectUndeleteDialog(FileTableView fileTableView) {
    //register Double-Click on Deleted-Label for Undelete-Dialog
    deletedLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() > 1) {
        fileTableView.unDeleteWithDialog();
      }
    });
  }

  public void showFilesNumber(int filesNumber) {
    filesLabel.setText(" " + filesCaption + " " + filesNumber + " ");
    //filesLabel.setPrefWidth(StringHelper.computeTextWidth(filesLabel.getFont(), filesLabel.getText(), 0.0D) + 5);
    filesLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
  }

  public void showSelectedNumber(int selectedNumber) {
    if (selectedNumber > 1) {
      selectedLabel.setText(" " + selectedCaption + " " + selectedNumber + " ");
      selectedLabel.setVisible(true);
      selectedLabel.setPrefWidth(StringHelper.computeTextWidth(selectedLabel.getFont(), selectedLabel.getText(), 0.0D) + 5);
      //selectedLabel.setMinWidth(Region.USE_COMPUTED_SIZE);
    } else {
      selectedLabel.setVisible(false);
      selectedLabel.setPrefWidth(0);
    }
  }

  public void showModifiedNumber(int modifiedNumber) {
    if (modifiedNumber > 0) {
      modifiedLabel.setText(" " + modifiedCaption + " " + modifiedNumber + " ");
      modifiedLabel.setVisible(true);
      modifiedLabel.setPrefWidth(StringHelper.computeTextWidth(modifiedLabel.getFont(), modifiedLabel.getText(), 0.0D) + 5);
      //modifiedLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
    } else {
      modifiedLabel.setVisible(false);
      modifiedLabel.setPrefWidth(0);
    }
  }

  public void showDeletedNumber(int deletedNumber) {
    if (deletedNumber > 0) {
      deletedLabel.setText(" " + deletedCaption + " " + deletedNumber + " ");
      deletedLabel.setVisible(true);
      deletedLabel.setPrefWidth(StringHelper.computeTextWidth(deletedLabel.getFont(), deletedLabel.getText(), 0.0D) + 5);
      //deletedLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
    } else {
      deletedLabel.setVisible(false);
      deletedLabel.setPrefWidth(0);
    }
  }
}
