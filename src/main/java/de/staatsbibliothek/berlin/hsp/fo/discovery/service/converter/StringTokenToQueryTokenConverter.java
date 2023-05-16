/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.type.QueryType;

import java.util.ArrayList;
import java.util.List;

public class StringTokenToQueryTokenConverter {
  /**
   * Converts a list of {@code String}s to a list of {@code QueryToken}s, based on it's content
   * @param strTokens the tokens to convert
   * @return the list of {@code QueryTokens}
   */
  public static List<QueryToken> convert(final List<String> strTokens) {
    ArrayList<QueryToken> queryTokens = new ArrayList<>(strTokens.size());
    for(String strToken : strTokens) {
      if(Query2SolrQueryConverter.isQuoted(strToken)) {
        if(Query2SolrQueryConverter.containsWildcards(strToken)) {
          queryTokens.add(new QueryToken(strToken, QueryType.COMPLEX));
        }
        else {
          queryTokens.add(new QueryToken(strToken, QueryType.EXACT));
        }
      } else {
        queryTokens.add(new QueryToken(strToken, QueryType.STANDARD));
      }
    }
    return queryTokens;
  }
}
