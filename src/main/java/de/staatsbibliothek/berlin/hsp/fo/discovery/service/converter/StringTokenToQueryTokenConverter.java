package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.StringTokenizer;

import java.util.ArrayList;
import java.util.List;

public class StringTokenToQueryTokenConverter {

  private StringTokenToQueryTokenConverter() {
  }

  public static List<QueryToken> convert(final String string) {
    final List<String> strTokens = StringTokenizer.tokenize(string);

    return convert(strTokens);
  }

  /**
   * Converts a list of {@code String}s to a list of {@code QueryToken}s, based on its content
   *
   * @param strTokens the tokens to match
   * @return the list of {@code QueryTokens}
   */
  public static List<QueryToken> convert(final List<String> strTokens) {
    ArrayList<QueryToken> queryTokens = new ArrayList<>(strTokens.size());
    for (String strToken : strTokens) {
      if (Query2SolrQueryConverter.isQuoted(strToken)) {
        if (Query2SolrQueryConverter.containsWildcards(strToken)) {
          queryTokens.add(new QueryToken(strToken, TokenType.COMPLEX));
        } else {
          queryTokens.add(new QueryToken(strToken, TokenType.EXACT));
        }
      } else {
        queryTokens.add(new QueryToken(strToken, TokenType.STANDARD));
      }
    }
    return queryTokens;
  }
}
