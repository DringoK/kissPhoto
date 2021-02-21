package dringo.kissPhoto.ctrl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * kissPhoto for managing and viewing your photos, but keep it simple-stupid ;-)
 * <p/>
 * Heuristic to guess the position of the counter in the filenames of MediaFileList
 * The nth number in the filename will be used in MediaFiles to auto (re)number the files
 * <p/>
 *
 * @author Ingo
 * @since 2012-09-02
 * @version 2020-12-20: bug fixing with string compare
 * @version 2014-06-05: java.nio used (except conversion to file array)
 * @version 2014-06-04: ik: bug fix: the first files have been used (might be dirs) instead of the the found fileNames (which are files) More robust (null/not exist, ...)
 * @version 2014-05-11: ik: if the 2-file heuristic does not find counter return 1-file heuristic
 */
public class CounterPositionHeuristic {


  /**
   * Guess the files' counter position by investigating a folder:
   * <ul>
   * <li>if there is no file in the folder guess 0 (prefix)</li>
   * <li>if there is one file in the folder use heuristic with one filename parameter (see below)</li>
   * <li>if there are more than one files in the folder use heuristic with two filename parameters (see below)</li>
   * </ul>
   * For convenience: if the folder is a file: the heuristic with on filename parameter is called
   *
   * @param folder is the folder to be investigated
   * @return the guessed counter position ("nth number is the counter")
   */
  public int guessCounterPosition(Path folder) {
    final int MAXFILENAMES = 2; //currently only the first two filenames are investigated

    if (folder == null) return 0;

    Path directory = folder;
    if (Files.isRegularFile(folder))
      directory = directory.getParent();


    if (Files.exists(directory)) {
      File[] files = directory.toFile().listFiles();
      if (files == null) return 0;

      String[] fileNames = new String[MAXFILENAMES];

      //find the first two fileNames
      int i = 0;
      int found = 0;
      while (i < files.length && found < MAXFILENAMES) {
        if (!files[i].isDirectory()) {
          fileNames[found] = files[i].getName();
          found++;
        }
        i++;
      }

      if (found == 0) {
        return 0; //default is 0 = prefix
      } else {
        if (found == 1) {
          return guessCounterPosition(fileNames[0]);
        } else {   //found = 2
          i = guessCounterPosition(fileNames[0], fileNames[1]);
          if (i == 0) //the 2 file heuristic was not successful try the 1 file heuristic
            i = guessCounterPosition(fileNames[0]);

          return i;
        }
      }
    }

    return 0; //as a default
  }

  /**
   * investigate the filenames to determine the counter position ("nth number in the filename")
   * <ul>
   * <li>if two filenames are provided the position of the first number which is not equal in both files is calculated</li>
   * <li>of zero (0) if no number is contained in the non equal parts</li>
   * <li>if one filename is the empty string or null the heuristic with one parameter is called</li>
   * </ul>
   * Examples:
   * <ul>
   * <li>filename1="DSCN_05389.jpg", filename2="DSCN_05400.jpg returns 1</li>
   * <li>filename1="Day1_01.jpg", filename2="Day1_02.jpg" returns 2 as the second number seems to be the counter</li>
   * <li>filename1="02-09-2012_day1_01 Here are 5 friends.jpg",<br>
   * filename2="02-09-2012_day2_02 Here are 5 other friends.jpg" returns 4</li>
   * <li>filename1="Day1_02.jpg", filename2="Day1_02.jpg" returns 0 (no number in the non-identical parts of the strings)</li>
   * <li>filename1="01a.jpg", filename2="01b.jpg" returns 0 (no number in the non-identical parts of the strings)</li>
   * <li>filename1="hello.jpg", filename2="world.jpg" returns 0 (no number in the non-identical parts of the strings)</li>
   * </ul>
   *
   * @param filename1 a first filename
   * @param filename2 a second filename to detect a "counting" number between first and second
   * @return the likely position of the counter in the filename ("the nth number is the counter"),
   *         0 is returned if the counter should be added as a prefix (no number in the non-equal part of the filenames)
   */
  public int guessCounterPosition(String filename1, String filename2) {
    //if too few arguments use heuristic with just one filename
    if ((filename1 == null) || filename1.equals("")) return guessCounterPosition(filename2);
    if ((filename2 == null) || filename2.equals("")) return guessCounterPosition(filename1);

    int i = 0;   //the current char-position in the strings
    int pos = 0; //the position of the number (the nth number)
    boolean found = false;

    //count numbers in the identical parts
    while ((i < filename1.length()) && (i < filename2.length()) &&
        (filename1.charAt(i) == filename2.charAt(i)) && !found) {
      if (Character.isDigit(filename1.charAt(i))) {
        pos++;  //a number was found
        //find last digit of the number
        i++;  //starting from next character
        while ((i < filename1.length()) && (i < filename2.length()) &&
            (Character.isDigit(filename1.charAt(i))) &&
            (filename1.charAt(i) == filename2.charAt(i))) {
          i++;
        }
        // if during the above search (for the end of the number) a difference had been detected in the number then
        // the counting number has been found
        if ((i < filename1.length()) && (i < filename2.length()) &&
            (Character.isDigit(filename1.charAt(i))) &&
            (Character.isDigit(filename2.charAt(i))) &&
            (filename1.charAt(i) != filename2.charAt(i))) {
          found = true;
        }

      } else {
        i++; //ignore non-digit
      }
    }

    //if the counting number was not found during identical part search it is the next after identical part
    if (!found) {
      //heuristic only looks for a further number in the first name to determine if there is a next number to be the counter
      //or if a prefix should be used (pos remains 0 in that case)
      while ((i < filename1.length()) && !Character.isDigit(filename1.charAt(i))) {
        i++; //ignore non-digits
      }
      if ((i < filename1.length()) && Character.isDigit(filename1.charAt(i))) {
        pos++;  //a further number has been found and this must be the counter
      } else {
        pos = 0;  //propose a prefix counter if no number is in the non-equal part of the strings
      }
    }

    return pos;
  }

  /**
   * investigate the filenames to determine the counter position ("nth number in the filename")
   * <ul>
   * <li>the position of the first number in the filename that is not a date</li>
   * <li>if filename is empty or null or no number is contained, 0 is returned indicating that a prefix counter should be used</></li>
   * </ul>
   * Examples:
   * <ul>
   * <li>filename="hello.jpg" returns 0</li>
   * <li>filename="DSCN_05389.jpg" returns 1</li>
   * <li>filename="Day1_01.jpg" returns 2 (as the heuristic sees "1_01" as an (incomplete) date (return last number of the date))</li>
   * <li>filename="2012-09-02_01 Here are 5 friends.jpg" returns 4 (first number after date)</li>
   * <li>filename="2012-09-02_day1_01 Here are 5 friends.jpg" returns 5(ignore first date, "1_01" as an (incomplete) 2nd date (return last number of the date))</li>
   * <li>filename="2012_09_01 - 2012_09_02-01 weekend in the mountains.jpg" returns 7</li>
   * <li>filename="2012_09_01-2012_09_02 weekend in the mountains.jpg" returns 6 (last number of the last date)</li>
   * <li>filename="2012 09 01-2012 09 02_01 weekend in the mountains.jpg" returns 7</li>
   * </ul>
   *
   * @param filename to be investigated
   * @return the likely position of the counter in the filename (the nth number is the counter)<br>
   * or zero if a prefix counter is proposed (no number is in the filename)
   */
  public int guessCounterPosition(String filename) {
    if ((filename == null) || filename.equals("")) {
      return 1;
    } else {
      boolean found = false;
      int pos = 0; //position of counter to be returned
      int i = 0;    //charAt-Position running through the filename
      int datePartPos = 0;

      while ((i < filename.length()) && !found) {
        //find first digit of a number
        if (Character.isDigit(filename.charAt(i))) {
          pos++; //a number was found :-)

          //find last digit of the number
          i++;  //starting from next character
          while ((i < filename.length()) && (Character.isDigit(filename.charAt(i)))) {
            i++;
          }

          if (datePartPos >= 2) { //==2, but >= is more robust;-)
            datePartPos = 0;      //then this number was the last (third) part of the date
            //continue search
          } else {
            //check if the found number is part of a date
            if ((i < filename.length()) &&
                ((filename.charAt(i) == ' ') ||  //date separators: space, _, -, .
                    (filename.charAt(i) == '_') ||
                    (filename.charAt(i) == '-') ||
                    (filename.charAt(i) == '.'))) {
              //look ahead: if the next char is a digit again, this might be a part of a date
              if ((i < filename.length() - 1) && Character.isDigit(filename.charAt(i + 1))) {
                datePartPos++;
              } else {
                found = true;  //if the date separator is not followed by digits again, the number was not part of a date
              }

              //note: i is still on the possible date separator, i.e. the position after the number

            } else {
              found = true; //number not followed by a date separator then it is the counter :-)
            }
          }

        } else {
          i++; //ignore non-digits
        }
      }

      //if no number was in the filename (pos==0) a prefix counter is proposed (return 0)
      return pos;
    }
  }
}
