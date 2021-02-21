package dringo.kissPhoto.view.fileTableHelpers;

import dringo.kissPhoto.model.MediaFile;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This factory is used for showing a FileTableView table:
 * for every cell of the table a TextFieldCell is generated
 * <p>
 * Special features are listed in TextFieldCell-class
 * <p/>
 *
 * @author Ingo
 * @since 2016-11-04
 * @version initial version
 */
public class TextFieldCellFactory implements Callback<TableColumn<MediaFile, String>, TableCell<MediaFile, String>> {

  @Override
  public TableCell<MediaFile, String> call(TableColumn<MediaFile, String> param) {
    return new TextFieldCell();
  }

} //class TextFieldCellFactory
