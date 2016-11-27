package de.kissphoto.helper;

import java.io.File;
import java.text.DecimalFormatSymbols;

/**
 * Created with IntelliJ IDEA.
 * User: Ingo
 * Date: 04.10.12
 * <p/>
 * String utilities.
 * These simple routines avoid to import the complete apache StringUtils-jar
 */
public class StringHelper {

  /**
   * generate string containing repeated string s
   * e.g.
   * repeat("0", 5) --> "00000"
   * repeat("ab", 2) --> "abab"
   * repeat("",2) --> ""
   * repeat(null, 2) --> null
   * repeat("ab", 0) --> ""
   *
   * @param s string to be repeated
   * @param n number of repetitions
   * @return a string containing n repetitions of s
   */
  public static String repeat(String s, int n) {
    if (s == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) {
      sb.append(s);
    }
    return sb.toString();
  }


  /**
   * get the locale separator character from Platform or Default character if not supported by platform
   * on many platforms (except windows) there is no list character defined
   * to be compatible with excel the following strategy is used:
   * if the decimal separator is , then use ; else use , (like comma separated values indicate)
   */
  public static char getLocaleCSVSeparator() {

    if (new DecimalFormatSymbols().getDecimalSeparator() == ',') {
      return ';';
    } else {
      return ',';
    }
  }

  /**
   * Convert String to QuotedString: a leading and a trailing quote (") and double all all previously existing quotes
   *
   * @param aString: the String to be converted:
   * @return the converted string
   */
  public static String quote(String aString) {
    return "\"" + aString.replaceAll("\"", "\"\"") + "\"";
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
