package dringo.kissPhoto.helper;

import java.io.File;
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

  /**
   * Extract filePath from absolutePath.
   * Note: the path includes an ending File.separator (i.e. / or \)
   *
   * @param absolutePath interpret the passed string as an absolute path and
   * @return the filePath portion (i.e. without name and extension). "" if File.separator not included in absolutePath
   */
  public static String extractPathname(String absolutePath) {
    if (absolutePath == null || absolutePath.isEmpty())
      return "";
    else
      return absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1); //+1 = including trailing /
    //if separator not found then [0, -1+1] = "" is returned
  }

  /**
   * Extract filename from absolutePath.
   *
   * @param absolutePath interpret the passed string as an absolute path and
   * @return the filename portion (i.e. only name and extension). absolutePath (unchanged) if File.separator not included in absolutePath
   */
  public static String extractFileName(String absolutePath) {
    if (absolutePath == null || absolutePath.isEmpty())
      return "";
    else
      return absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1); //the String from last File.separator on
    //if separator not found then [-1+1, ...] = absolutePath is returned
  }
}
