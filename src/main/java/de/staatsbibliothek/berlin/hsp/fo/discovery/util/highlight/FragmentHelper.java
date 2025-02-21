package de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.StringHelper;

import java.util.ArrayList;

public class FragmentHelper {

  private FragmentHelper() {}

  /**
   * Fragments the given {@code highlight information}. The number of fragments is determined by the number of highlighted terms wrapped by
   * a tag of {@code tagsName}
   * @param highlightInformation the highlight informtation to fragment
   * @param tagName the tag's name that is used for wrapping the highlighted information
   * @param padding the padding that should be used to fill up the highlight information with adjacent words
   * @return an {@code Array} containing the fragments
   */
  public static String[] fragmentHighlightInformation(final String highlightInformation, final String tagName, final int padding) {
    final Interval[] highlightPositions = HighlightHelper.gatherHighlightPositions(highlightInformation, tagName, false);
    final Interval[] fragmentPositions = FragmentHelper.calculateHighlightPositionWithPadding(highlightInformation, highlightPositions, padding);

    return fragmentString(highlightInformation, fragmentPositions);
  }

  private static Interval[] calculateHighlightPositionWithPadding(final String string, final Interval[] intervals, final int paddingSize) {
    final Interval[] result = new Interval[intervals.length];
    for (int i = 0; i < intervals.length; i++) {
      result[i] = calculateIntervalWithPadding(string, intervals[i], paddingSize);
    }
    return HighlightHelper.mergeIntervals(result);
  }

  private static Interval calculateIntervalWithPadding(final String string, final Interval interval, final int paddingSize) {
    final int leftBoundary = StringHelper.leftBoundary(string, Math.max(0, interval.start - paddingSize), interval.start);
    final int rightBoundary = StringHelper.rightBoundary(string, interval.end, interval.end + paddingSize);
    return new Interval(leftBoundary, rightBoundary);
  }

  private static String[] fragmentString(final String str, final Interval[] intervals) {
    final ArrayList<String> result = new ArrayList<>(intervals.length);
    for (Interval interval : intervals) {
      result.add(str.substring(interval.start, interval.end));
    }

    return result.toArray(String[]::new);
  }
}
