package de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight;

public class DOMHelper {
  private DOMHelper() {}

  /**
   * Creates the textual representation of an opening tag with the given {@code tagName}
   *
   * @param tagName the name of the tag
   * @return the opening tag
   */
  public static String getOpeningTag(final String tagName) {
    return String.format("<%s>", tagName);
  }

  /**
   * Creates the textual representation of a closing tag with the given {@code tagName}
   *
   * @param tagName the name of the tag
   * @return the closing tag
   */
  public static String getClosingTag(final String tagName) {
    return String.format("</%s>", tagName);
  }

  /**
   * Wraps a given text with an HTML tag
   *
   * @param text    the text to be wrapped
   * @param tagName the HTML tag that should be used
   * @return the wrapped text
   */
  public static String wrapWithTag(final String text, final String tagName) {
    return String.format("<%1$s>%2$s</%1$s>", tagName, text);
  }
}
