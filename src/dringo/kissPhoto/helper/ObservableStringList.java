package dringo.kissPhoto.helper;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * MIT License
 * Copyright (c)2021 kissPhoto
 *
 * add CSV support to SimpleListProperty<String> and simplify use
 */

public class ObservableStringList extends SimpleListProperty<String> {

  final static char sep = StringHelper.getLocaleCSVSeparator();  //just a shortcut

  /**
   * The constructor of {@code SimpleListProperty}
   */
  public ObservableStringList() {
    super(FXCollections.observableArrayList());
  }

  @Override
  public boolean add(String element) {
    if (element!=null)
      return super.add(element);
    else
      return true;
  }

  /**
   * transform the Stringlist into a single CSV-String. Each element is in quotes
   * the locale specific separator is used
   * @return the list in a single CSV-String
   */
  public String toCSVString(){
    String s = "";
    for (String element:this) {
      element.replaceAll("\"", "\\\""); //use escape for quotes inside the string
      s += "\"" + element + "\"" + sep;
    }
    if (s.length()>1)
      s = s.substring(0, s.length()-1); //remove last sep (-1)
    return s;
  }

  /**
   * read the list from a CSV-String previously generated using toCSVString()
   * All elements are added to the end of the already existing list
   * note: parsing is not optimized for performance as a lot of string-copying is used ;-)
   * note: only quotes are used for parsing, the separator is ignored making the parsing independent from locale
   * @param s the CSV String to be parsed
   */
  public void appendFromCSVString(String s){
    if (s==null) return; //nothing to append if null

    String remaining = s;

    //cursor for simple parsing
    int pos;

    while (remaining.length() > 0){
      //find opening quote
      pos = findFirstNonEscapedQuote(remaining);
      if (pos > -1) { //if found
        remaining = remaining.substring(pos+1); //remove everything before and including quote

        //find closing quote
        pos = findFirstNonEscapedQuote(remaining);
        if (pos>-1){  //if found
          add(remaining.substring(0,pos)); //without closing quote (-1)
          if (remaining.length()>pos+1)
            remaining = remaining.substring(pos+2); //consume the added element, pos+0=last char, +1=quote, +2=first char after quote
          else
            remaining="";
        }else{
          break; //closing quote not found --> end search
        }
      }else{
        break; //opening quote not found --> end search
      }
    }
    if (remaining.length()>0) //if no more quote found, but still text remaining, then add the text and ignore missing quote
      add(remaining);
  }

  /**
   * @param s the String to search in
   * @return the index of the first quote (") that is not escaped (\") or -1 if non inside s
   */
  public int findFirstNonEscapedQuote(String s){
    int pos = -1; //search continues always behind pos, so at -1+1=0 in the beginning
    boolean found = false;

    while (pos < s.length() && !found) {
      pos = s.indexOf("\"", pos +1);
      if (pos>0) //if not the first character then see if there is a quote before
        found = (s.charAt(pos-1) != '\\'); //found if not escape char before quote
      else {
        found = (pos == 0); //if found at first position there cannot be a quote before
        break;
      }
    }
    if (found)
      return pos;
    else
      return -1;
  }
}
