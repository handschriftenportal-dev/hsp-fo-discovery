package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

  private static final String DIVIDER_PATTERN = "[\\s,.;\\t\\n\\(\\)]+";

  private StringHelper() {}

  /**
   * Determines the start position of the leftmost word
   * @param string the string to check within
   * @param start the start position of the interval to check within
   * @param end the end position to check within
   * @return an {@code int} representing the position.
   * if no dividing character can be found within the given interval, the {@code end} position ist returned
   */
  public static int leftBoundary(final String string, final int start, final int end) {
    if(start == 0) {
      return 0;
    }
    return firstIndexOf(string, DIVIDER_PATTERN, start, end);
  }

  /**
   * Determines the end position of the rightmost word
   * @param string the string to check within
   * @param start the start position of the interval to check within
   * @param end the end position to check within
   * @return an {@code int} representing the position.
   * if no dividing character can be found within the given interval, the {@code end} position ist returned
   */
  public static int rightBoundary(final String string, final int start, final int end) {
    if(end >= string.length()) {
      return string.length();
    }
    return lastIndexOf(string, DIVIDER_PATTERN, start, end);
  }

  private static int firstIndexOf(final String string, final String patternStr, final int start, int end) {
    end = Math.min(string.length(), end);
    final Matcher matcher = Pattern.compile(patternStr).matcher(string).region(start, end);
    if(matcher.find() && matcher.end() < end) {
      return matcher.end();
    }
    return end;
  }

  public static int lastIndexOf(final String string, final String patternStr, final int start, int end) {
    end = Math.min(string.length(), end);
    final Matcher matcher = Pattern.compile(patternStr).matcher(string).region(start, end);
    int result = end;
    while(matcher.find()) {
      if(matcher.start() < end) {
        result = matcher.start();
      } else {
        break;
      }
    }
    return result;
  }

  public static boolean isText(final String argument) {
    return !NumberUtils.isDigits(argument);
  }

  public static String removeBoosting(final String str) {
    if(str != null) {
      return str.replaceAll("\\^\\d+$", "");
    }
    return null;
  }

  /**
   * Counts words of the given string by using {@code " "} as word delimiter
   * @param string the string thats words should be counted
   * @return
   */
  public static int countWords(final String string) {
    if(StringUtils.isNotEmpty(string)) {
      return string.split(" ").length;
    }
    return 0;
  }
}
