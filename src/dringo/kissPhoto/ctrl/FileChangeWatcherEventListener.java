package dringo.kissPhoto.ctrl;

import java.nio.file.WatchEvent;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * The interface which is used by FileChangeWatcher to execute an action if a change to the watched folder appears
 * <p/>
 *
 * @author Dringo
 * @since 2014-05-04 Created by Ingo on 04.05.2014.
 * @version 2014-06-06 Watch Event parameter splitted into filename and kind (otherwise the user would have to do it each time)
 */
public interface FileChangeWatcherEventListener {
  public void onFolderChanged(String filename, WatchEvent.Kind kind);
}
