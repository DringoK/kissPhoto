package dringo.kissPhoto.view;

import com.drew.metadata.Metadata;
import dringo.kissPhoto.model.MediaFile;
import dringo.kissPhoto.model.MediaFileTagged;
import dringo.kissPhoto.model.Metadata.MetaInfoItem;
import dringo.kissPhoto.model.Metadata.MetaInfoTreeItem;
import dringo.kissPhoto.model.Metadata.MetadataItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.StackPane;

/**
 * MIT License
 * <p>
 * Copyright (c)2021 kissPhoto
 * </p>
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * View for showing all metadata info of media files. Eg. Exif or IPTC
 * It is the viewer for MetaInfoTreeItems i.e. MetaData, Directory or Tag
 * <p/>
 * <p>
 * This is Pane with an integrated TreeTableView
 * </p>
 *
 * @author Dringo
 * @version 2021-03-20 First implementation
 * @since 2021-03-14
 */


public class MetaInfoView extends StackPane {

  TreeTableView<MetaInfoItem> treeTableView = new TreeTableView<>();
  TreeTableColumn<MetaInfoItem, String> keyColumn    = new TreeTableColumn<>("Key");
  TreeTableColumn<MetaInfoItem, String> valueColumn  = new TreeTableColumn<>("Value");

  /**
   * Create an empty TreeTableView.
   * setMediaFile(mediaFile) will connect it later to the current mediaFile
   */
  public MetaInfoView() {
    //connect columns to data
    // param.getValue() returns the TreeItem<MetaInfoItem> instance for a particular TreeTableView row,
    // param.getValue().getValue() returns the MetaInfoItem instance inside the TreeItem<MetaInfoItem>
    keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyString());
    keyColumn.setPrefWidth(250);
    treeTableView.getColumns().add(keyColumn);

    valueColumn.setCellValueFactory(param -> param.getValue().getValue().getValueString());
    valueColumn.prefWidthProperty().bind(widthProperty().subtract(keyColumn.widthProperty())); //the rest of the available space
    treeTableView.getColumns().add(valueColumn);

    treeTableView.setShowRoot(false);

    getChildren().add(treeTableView);
  }

  /**
   * try to set the root on metadata of the mediaFile to display its meta info
   * @param mediaFile for which meta info shall be displayed
   * @return true if the media file is compatible with metadataViewer (i.e. mediaFile is a MediaFileTagged)
   */
  public boolean setMediaFile(MediaFile mediaFile){
    if (!(mediaFile instanceof MediaFileTagged)){
      treeTableView.setRoot(null); //do not show anything
      setVisible(false);
      return false;
    }

    //status here: mediaFile is tagged and not null
    MetaInfoTreeItem metaInfoTreeItem = mediaFile.getCachedMetaInfo(this);
    treeTableView.setRoot(metaInfoTreeItem); //metaInfoTreeItem might be null = empty if there is no metadata available
    setVisible(true);
    return true;
  }

  /**
   * Cache support: lazy load the meta infos from media
   * @param mediaFileTagged link back to the mediaFile which tries to fill it's cache for this view
   * @return the MetadataTreeItem that should be cached
   */
  public MetaInfoTreeItem getViewerSpecificMediaInfo(MediaFileTagged mediaFileTagged){
    //lazy Load MetaData by calling getMetadata()
    //lazy load the MetaDataTreeItem if possible from cache (see MediaFile.getCachedMetaInfo())
    Metadata metadata = mediaFileTagged.getMetadata(); //get the cached value from the model
    if (metadata!=null) //if Metadata could be loaded or had been loaded before
      return new MetaInfoTreeItem(new MetadataItem(metadata));
    else
      return null;
  }

}
