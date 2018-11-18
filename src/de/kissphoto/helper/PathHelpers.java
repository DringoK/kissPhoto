package de.kissphoto.helper;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathHelpers {

  public static Path extractFolder(Path fileOrFolder) {
    //determine folder
    if (Files.isDirectory(fileOrFolder)) {
      return fileOrFolder;
    } else {
      return fileOrFolder.getParent();
    }
  }

}
