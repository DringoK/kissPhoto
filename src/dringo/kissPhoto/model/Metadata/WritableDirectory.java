package dringo.kissPhoto.model.Metadata;

import com.drew.metadata.Directory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * wrapper for a com.drew.metada.Directory
 * This wrapper collects all changes as WritableEntries in list in memory that shall be applied to it in the file later
 * --> i.e. the setter methods add changes to the list (no duplicates, last change wins)
 * --> the writeTo() method start the conversion into a byte array that can be written by WritableMetadata
 * <p>
 * Note: first it is tried to update the exisiting entries to avoid copying of the complete imageFile
 */
public class WritableDirectory {
  Directory directory;    //link to according directory read by library of Drew Noaks

  //list of changes in the directory
  ObservableList<WritableEntry> changedEntries = FXCollections.observableArrayList();

}
