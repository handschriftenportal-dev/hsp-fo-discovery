package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class StringTokenizer {
  private StringTokenizer() {}
  /**
   * Tokenizes a string by using the double quotation mark as delimiter, but preserving it within the corresponding token(s)
   *
   * @param str the String to be tokenized
   * @return a list of strings representing the tokens
   */
  public static List<String> tokenize(final String str) {
    final LinkedList<String> tokens = new LinkedList<>();
    String token = "";
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == '"' && i != 0 && str.charAt(i - 1) != '\\') {
        if (token.length() > 0 && (token.charAt(0) == '"' || i == str.length() -1)) {
          token = endQuotedToken(token, tokens, str.charAt(i));
        } else {
          token = startQuotedToken(token, tokens, str.charAt(i));
        }
      } else {
        token += str.charAt(i);
        if (i == str.length() - 1) {
          token = StringUtils.trim(token);
          if (token.length() != 0) {
            tokens.add(token);
          }
        }
      }
    }
    return finishTokenList(tokens);
  }

  private static String startQuotedToken(String token, List<String> tokens, final char currentCharacter) {
    token = StringUtils.trim(token);
    if (token.length() > 0) {
      tokens.add(token);
    }
    return String.valueOf(currentCharacter);
  }

  private static String endQuotedToken(String token, List<String> tokens, final char currentCharacter) {
    token += currentCharacter;
    if (token.length() > 2) {
      tokens.add(token);
    }
    return "";
  }

  /**
   * Removes the last item of the list and appends its content to second last item, if there is any
   *
   * @param tokens the list of token to manipulate
   * @return the original {@code List} but without its last item if the length of the list was larger than 1,
   * otherwise no change will be performed
   */
  private static List<String> finishTokenList(final LinkedList<String> tokens) {
    if (tokens.size() > 1) {
      final String lastItem = tokens.getLast();
      if (isBrokenQuotedToken(lastItem)) {
        final int nextToLastIndex = tokens.size() - 2;
        final String nextToLastItem = tokens.get(nextToLastIndex);
        tokens.set(nextToLastIndex, nextToLastItem + lastItem);
        tokens.remove(tokens.getLast());
      }
    }
    return tokens;
  }

  /**
   * Checks if a token starts with a quotation mark but doesn't end with it
   *
   * @param token the token to check
   * @return {@code true} if the quotation of the token is broken, {@code false} otherwise
   */
  private static boolean isBrokenQuotedToken(final String token) {
    if (StringUtils.isBlank(token)) {
      return false;
    } else {
      return token.charAt(0) == '"' && (token.length() == 1 || token.charAt(token.length() - 1) != '"');
    }
  }
}
