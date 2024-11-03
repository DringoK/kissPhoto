package dringo.kissPhoto.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * @author Dringo
 * @since: 2021
 * @version: 2024-10-06 helpers for previous/next folder added
 *
 */
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

  public static enum Direction{previous, next}
  /**
   * return the alphabetically next folder on the same level of the given fileOrFolder
   * keep it simple: don't leave the parent folder
   *
   * @param fileOrFolder
   * @return nextFolder or null, if there is none
   */
  public static Path getNeighbourFolder(Path fileOrFolder, Direction direction){
    if (fileOrFolder == null || !Files.exists(fileOrFolder)) return null;
    Path givenFolder = extractFolder(fileOrFolder);

    Path parentDir = givenFolder.getParent();
    if (parentDir == null) return null;     //theoretically if "\" had been provided in parameter

    List<Path> subFolders = getDirectSubFoldersSorted(parentDir);
      if (subFolders==null) return null;

    //find index of givenFolder in List
    int i=-1;
    boolean found = false;
    for (Path folder:subFolders){
      i++;
      if (folder.compareTo(givenFolder)==0){
        found=true;
        break;
      }
    }

    if (!found) return null;

    if (direction == Direction.next){
      i++;
      if (i< subFolders.size())
        return subFolders.get(i);
      else
        return null;
    }else{
      i--;
      if (i>=0)
        return subFolders.get(i);
      else
        return null;
    }

    }


  /**
   * return an alphabetically sorted list of all direct subdirectories
   * @param parentFolder the folder in which the subdirectories are collected
   * @return the list (may be empty) or null, if parentFolder does not exist
   */
  public static List<Path> getDirectSubFoldersSorted(Path parentFolder){
    if (!Files.exists(parentFolder)) return null;

    List<Path> folderList = new ArrayList<Path>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentFolder)) {
      for (Path folder : stream) {
        if (Files.isDirectory(folder) && !Files.isHidden(folder))
          folderList.add(folder);//wrap the folder as a MediaFile and specialize it according its media type
      }
    folderList.sort(Comparator.naturalOrder());

    } catch (IOException e) {
    }

    return folderList;
  }
}
