package de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.ListHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HighlightHelper {

  private static final Map<String, Pattern> patternCache = new HashMap<>();

  private HighlightHelper() {}

  /**
   * Merges contiguous elements of the given {@code tagName} if there's only whitespaces in between
   * For example ({@code tagName} equals 'pre'):
   * {@code <pre>foo</pre> <pre>bar</pre>} results in <pre>foo bar</pre>}
   * {@code <pre>foo</pre> baz <pre>bar</pre>} results in <pre>foo</pre> baz <pre>bar</pre>}
   *
   * @param html    the string that contains the elements to merge
   * @param tagName the tag name of the elements
   * @return the html containing the merged elements
   */
  public static String mergeContiguousElements(final String html, final String tagName) {
    Interval[] intervals;
    intervals = gatherHighlightPositions(html, tagName, true);
    intervals = mergeIntervals(intervals);

    final String contentWithoutHighlighting = removeHighlighting(html, tagName);

    return addHighlighting(contentWithoutHighlighting, tagName, intervals);
  }

  /**
   * Merges the highlight information of two lists.
   *
   * @param hl1 a list containing the first highlight information, expected as the list's first element
   * @param hl2 a list containing the second highlight information, expected as the list's first element
   * @param tagName          the tag's name that is used for wrapping the highlighted information
   * @return the merged highlight information. If the size of one of two list doesn't equal one, the intersection of both lists is returned
   */
  public static List<String> mergeHighlights(final List<String> hl1, final List<String> hl2, final String tagName) {
    final BiPredicate<String, String> isEqual = (s1, s2) -> removeHighlighting(s1, tagName).equals(removeHighlighting(s2, tagName));
    final BinaryOperator<String> merge = (s1, s2) -> mergeHighlightItem(s1, s2, tagName);

    return ListHelper.merge(hl1, hl2, isEqual, merge);
  }

  /**
   * Merges the highlight information of two fields. Expecting that the content is equal, besides the position of the highlighting tags
   *
   * @param highlightItem a string representing the first highlight information
   * @param anotherHighlightItem a string representing the second highlight information
   * @param tagName         the tag's name that is used for wrapping the highlighted information
   * @return the merged highlight information. If one of the highlight information is empty, the other one is returned. If both are empty, an empty string is returned
   */
  private static String mergeHighlightItem(final String highlightItem, final String anotherHighlightItem, final String tagName) {
    final Interval[] highlightIntervals = determineAndMergeIntervals(highlightItem, anotherHighlightItem, tagName);
    final String contentWithoutHL = removeHighlighting(highlightItem, tagName);

    return addHighlighting(contentWithoutHL, tagName, highlightIntervals);
  }

  /**
   * Determines and merges the highlight intervals of two highlight items
   * @param highlightItem the first highlight item
   * @param anotherHighlightItem the second highlight item
   * @param tagName the tag's name that is used for wrapping the highlighted information
   * @return
   */
  private static Interval[] determineAndMergeIntervals(final String highlightItem, final String anotherHighlightItem, final String tagName) {
    final Interval[] firstIntervals = gatherHighlightPositions(highlightItem, tagName, true);
    final Interval[] secondIntervals = gatherHighlightPositions(anotherHighlightItem, tagName, true);

    return mergeIntervals(ArrayUtils.addAll(firstIntervals, secondIntervals));
  }

  /**
   * Determines highlighted segments within a highlighted text. The segments are marked by HTML tags.
   * The found segments are represented by {@code Intervals}s
   * @param highlightedText the text, which should be analysed for highlighted segements
   * @param tagName the HTML tag's name, which is marking the highlighted segments
   * @param ignoreTags if {@code TRUE}, the size of the tags itself will be considered when calculating the intervals,
   *                   otherwise the tag's size will be ignored
   * @return the intervals of all found segments
   */
  static Interval[] gatherHighlightPositions(final String highlightedText, final String tagName, final boolean ignoreTags) {
    final Matcher matcher = getPattern(tagName).matcher(highlightedText);
    int currPointer = 0;
    final int openingTagSize = DOMHelper.getOpeningTag(tagName).length();
    final int closingTagSize = DOMHelper.getClosingTag(tagName).length();
    final int totalTagSize = openingTagSize + closingTagSize;
    final List<Interval> result = new ArrayList<>();

    /* when matching groups, the first group only contains the matching group's content,
      while the second group contains the whole pattern (i.e. the highlighting tags) */
    while(matcher.find()) {
      int start;
      int end;
      /* when ignoring tags (i.e. its length) all previous tags needs to be subtracted */
      if(ignoreTags) {
        start = matcher.start(0) - (currPointer * totalTagSize);
        end = matcher.end(0)  - ((currPointer + 1) * totalTagSize);
      }
      else  {
        start = matcher.start(0);
        end = matcher.end(0);
      }

      result.add(new Interval(start, end));
      currPointer++;
    }
    return result.toArray(Interval[]::new);
  }

  /**
   * Compiles a pattern for an HTML tag containing arbitrary characters
   * @param tagName the tag's name
   * @return the compiled pattern
   */
  private static Pattern getPattern(final String tagName) {
    patternCache.computeIfAbsent(tagName, k -> Pattern.compile(String.format("<%1$s>(.*?)<\\/%1$s>", tagName)));
    return patternCache.get(tagName);
  }

  /**
   * Merges an array of {@code Interval}s. If two intervals are overlapping,
   * the intervals are merged. This also applies to newly created (merged) intervals
   * @param arr the intervals, which should be merged
   * @return an {@code Array} containing the merged intervals
   */
  static Interval[] mergeIntervals(Interval[] arr) {
    if (arr.length == 0) return arr;

    Stack<Interval> stack = new Stack<>();
    Arrays.sort(arr, Comparator.comparingInt(i -> i.start));
    stack.push(arr[0]);

    // Start from the next interval and merge if necessary
    for (int i = 1; i < arr.length; i++) {
      // get interval from stack top
      Interval top = stack.peek();
      Interval current = arr[i];

      // if current's interval is not overlapping with stack top (by considering 1 char as token spacer)
      // push it to the stack
      if (top.end + 1 < current.start)
        stack.push(current);

        // Otherwise update the ending time of top with ending of current
        // interval is more
      else if (top.end < current.end) {
        top.end = current.end;
        stack.pop();
        stack.push(top);
      }
    }
    return stack.toArray(Interval[]::new);
  }

  /**
   * Removes highlighting tags by given tag name
   * @param highlighted the highlighted text
   * @param tagName the tag which is used to markup highlight fragments
   * @return the text without highlight tags
   */
  static String removeHighlighting(final String highlighted, final String tagName) {
    return getPattern(tagName).matcher(highlighted).replaceAll(m -> m.group(1));
  }

  /**
   * Adds highlight tags to the given text, determined by highlight intervals
   * @param text the text which should be highlighted
   * @param tagName the tag's name, which should be used for highlighting
   * @param highlightIntervals the intervals determining the segments that should be highlighted
   * @return
   */
  static String addHighlighting(final String text, final String tagName, final Interval[] highlightIntervals) {
    int offset = 0;
    String plainSegment;
    String highlightedSegment;
    final StringBuilder builder = new StringBuilder(text);
    for (Interval interval : highlightIntervals) {
      plainSegment = builder.substring(interval.start + offset, interval.end + offset);
      highlightedSegment = DOMHelper.wrapWithTag(plainSegment, tagName);
      builder.replace(interval.start + offset, interval.end + offset, highlightedSegment);
      offset += highlightedSegment.length() - plainSegment.length();
    }
    return builder.toString();
  }
}
