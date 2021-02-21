package dringo.kissPhoto.view;

import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * View to show meta data, i.e. EXIF data etc.
 * <p/>
 * @author Dringo
 * since 2012-11-04
 * @version 2020-12-20 language now static in KissPhoto, lambda expressions for event handlers
 */
public class MetaDataView extends TabPane {

  private final Stage primaryStage;
  private final MediaContentView mediaContentView;
  protected final StatusBar statusBar;

  public MetaDataView(Stage primaryStage, MediaContentView mediaContentView, StatusBar statusBar) {
    super();
    this.primaryStage = primaryStage;
    this.mediaContentView = mediaContentView;
    this.statusBar = statusBar;

    setMinSize(100.0, 100.0);
    setPrefSize(300.0, 300.0);
  }


  /**
   * Show a Tab for every Metadata directory and show a table with according metadata tabs in a table in every tab.
   * <p/>
   * As it can be assumed that many pictures in one directory have similar tags/directories
   * the existing tabs are reused if possible for faster browsing in files and for saving memory.
   */
  public void show(Iterable<Directory> metadataDirectories) {
    boolean found = false;
    ObservableList<Tab> tabsToDelete = FXCollections.observableArrayList(getTabs()); //all reused tabs are removed from this list the rest will be deleted at the end
    Tab currentTab = null;

    for (Directory directory : metadataDirectories) {
      //lookup if such a tab already exists
      for (Tab tab : getTabs()) {
        if (tab.getText().equalsIgnoreCase(directory.getName())) {
          found = true;
          currentTab = tab;
          tabsToDelete.remove(tab);  //this tab should not be deleted, because it will be reused :-)
          break;
        }
      }
      if (!found) currentTab = createTab(directory);
      fillTab(currentTab, directory);
    }

    //remove all unused tabs
    getTabs().removeAll(tabsToDelete);
  }

  /**
   * create a tab for the directory, create a table view for the tags and add it to this (Tabview)
   * Note: the tag-table will empty until it is filled with fillTab()
   *
   * @param directory of exif
   * @return the javaFX tab for the directory
   */
  private Tab createTab(Directory directory) {
    Tab tab = new Tab(directory.getName());
    TableView table = new TableView();

    //readonly Column to show TagNames
    TableColumn<Tag, String> tagNameColumn = new TableColumn("Tag Name");
    tagNameColumn.setPrefWidth(100);
    tagNameColumn.setEditable(false);
    tagNameColumn.setCellValueFactory(new PropertyValueFactory<Tag, String>("tagName"));

    //Column to show TagValues
    TableColumn<Tag, String> tagValueColumn = new TableColumn("Description");
    tagNameColumn.setPrefWidth(200);
    tagNameColumn.setEditable(false);
    tagNameColumn.setCellValueFactory(new PropertyValueFactory<Tag, String>(FileTableView.DESCRIPTION));

    table.getColumns().addAll(tagNameColumn, tagValueColumn);
    tab.setContent(table);
    getTabs().add(tab);
    return tab;
  }

  /**
   * fill the tab's tableView with the tags of the directory
   * The tab's tableView must not be null (use createTab to create the tabs and everything is fine ;-)
   *
   * @param tab
   * @param directory
   */
  private void fillTab(Tab tab, Directory directory) {
    TableView tableView = (TableView) tab.getContent();
    tableView.setItems(FXCollections.observableArrayList(directory.getTags()));
  }
}
