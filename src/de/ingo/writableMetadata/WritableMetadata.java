package de.ingo.writableMetadata;

import com.drew.metadata.Metadata;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * wrapper for a com.drew.metada.Metadata
 * This wrapper collects all changes as WritableDirectory's in alist in memory that shall be applied to it in the file later
 * --> i.e. the setter methods add changes to the list (no duplicates, last change wins)
 * --> the writeTo() method start the conversion of the WritableDirectory's performs the linking an writes the changes in one run
 * <p>
 * Note: first it is tried to update the existing entries to avoid copying of the complete imageFile
 */
public class WritableMetadata {
  Metadata metadata; //link to read metadata of Drew Noaks
  //list of changes
  ObservableList<WritableDirectory> changedDirectories = FXCollections.observableArrayList();


  /**
   * add a new tag to a directory that has not been in that directory before
   *
   * @param writableDirectory the directory to which the entry is expected
   * @param tag               the tag-id of the entry
   * @param type              the type of the entry. If the entry is already present then just the type is updated
   */
  public void changeEntryType(WritableDirectory writableDirectory, int tag, int type) {
    addNewEntry(writableDirectory, tag, type); //the side-effect of addNewEntry is used: an existing entry is just updated with addNewEntry
  }

  /**
   * add a new tag to a directory that has not been in that directory before
   * Use one of the setValue(...) methods later on to set a value
   *
   * @param writableDirectory the directory to which the entry will be added
   * @param tag               the tag-id of the new entry
   * @param type              the type of the new entry. If the entry is already present then just the type is updated
   */
  public void addNewEntry(WritableDirectory writableDirectory, int tag, int type) {
    //todo: not yet implemented
    //look-up writableDirectory. Add to changedDirectories if not already in the list
    //ask writableDirectory to take of the change into its list (or update an existing entry)

  }

  /**
   * set a new Value to an existing entry or an entry that has bee added using addNewEntry()
   * If the entry's type does not match the handed type an exception is thrown
   *
   * @param writableDirectory the directory in which the tag is expected
   * @param tag               the id of the tag to be changed
   * @param newValue          one of the integer types
   * @throws Exception if type of ExifEntry does not match "int" (signed or unsigned, byte, word, dword, ...)
   */
  public void setValue(WritableDirectory writableDirectory, int tag, long newValue) {
    //todo: not yet implemented
    //look-up writableDirectory. Add to changedDirectories if not already in the list
    //ask writableDirectory to take of the change into its list (or update an existing entry)
  }

  /**
   * set a new Value to an existing entry or an entry that has bee added using addNewEntry()
   * If the entry's type does not match the handed type an exception is thrown
   *
   * @param newValue text
   * @throws Exception if type of ExifEntry does not match a "string"
   */
  public void setValue(WritableDirectory writableDirectory, int tag, String newValue) throws Exception {
    //todo: not yet implemented
    //look-up writableDirectory. Add to changedDirectories if not already in the list
    //ask writableDirectory to take of the change into its list (or update an existing entry)
  }

  /**
   * set a new Value to an existing entry or an entry that has bee added using addNewEntry()
   * If the entry's type does not match the handed type an exception is thrown
   *
   * @param newValue rational value
   * @throws Exception if type of ExifEntry does not match a "rational"
   */
  public void setValue(WritableDirectory writableDirectory, int tag, double newValue) throws Exception {
    //todo: not yet implemented
    //look-up writableDirectory. Add to changedDirectories if not already in the list
    //ask writableDirectory to take of the change into its list (or update an existing entry)
  }

  /**
   * set a new Value to an existing entry or an entry that has bee added using addNewEntry()
   * If the entry's type does not match the handed type an exception is thrown
   *
   * @param byteArray lowlevel data of undefined type. If length <=4 it will be stored in entry otherwise in data area of the IFD
   * @throws Exception if type of ExifEntry does not match "undefined"
   */
  public void setValue(WritableDirectory writableDirectory, int tag, byte[] byteArray) throws Exception {
    //todo: not yet implemented
    //look-up writableDirectory. Add to changedDirectories if not already in the list
    //ask writableDirectory to take of the change into its list (or update an existing entry)
  }
}
