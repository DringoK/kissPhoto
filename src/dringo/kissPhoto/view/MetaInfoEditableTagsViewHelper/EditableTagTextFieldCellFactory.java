package dringo.kissPhoto.view.MetaInfoEditableTagsViewHelper;

import dringo.kissPhoto.model.Metadata.EditableItem.EditableMetaInfoItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * kissPhoto for managing and viewing your photos and media, but keep it simple...stupid ;-)
 * <p/>
 * This factory is used for editing MetaInfoEditableTagsView tree-table:
 * for every cell of the tree-table a TextFieldCell is generated
 * <p>
 * Special features are listed in TextFieldCell-class
 * <p/>
 *
 * @author Ingo
 * @since 2021-11-15
 * @version 2022-01-07 first working version
 */
public class EditableTagTextFieldCellFactory implements Callback<TreeTableColumn<EditableMetaInfoItem, String>, TreeTableCell<EditableMetaInfoItem, String>> {

  /**
   * The <code>call</code> method is called when required, and is given a
   * single argument of type P (here: TreeTableColumn<EditableMetaInfoItem, String>),
   * with a requirement that an object of type R (here: TreeTableCell<EditableMetaInfoItem, String>)
   * is returned.
   *
   * @param param The single argument upon which the returned value should be
   *              determined.
   * @return An object of type R that may be determined based on the provided
   * parameter value.
   */
  @Override
  public TreeTableCell<EditableMetaInfoItem, String> call(TreeTableColumn<EditableMetaInfoItem, String> param) {
    return new EditableTagTextFieldCell();
  }
} //class TextFieldCellFactory
