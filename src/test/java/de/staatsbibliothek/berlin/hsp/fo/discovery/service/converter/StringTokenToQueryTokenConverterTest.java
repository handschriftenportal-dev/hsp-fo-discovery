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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class StringTokenToQueryTokenConverterTest {
  @Test
  void whenStringTokenIsUnquoted_thenStandardQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("abc");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("abc"));
    assertThat(actualQueryTokens.get(0).getType(), is(QueryType.STANDARD));
  }

  @Test
  void whenStringTokenIsUnquotedAndContainsEscapedQuotationMark_thenStandardQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\\\"abc\\\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\\\"abc\\\""));
    assertThat(actualQueryTokens.get(0).getType(), is(QueryType.STANDARD));
  }

  @Test
  void whenStringTokenIsUnquotedAndContainsWildcardCharacter_thenStandardQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("ab*c");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("ab*c"));
    assertThat(actualQueryTokens.get(0).getType(), is(QueryType.STANDARD));
  }

  @Test
  void whenStringTokenIsQuoted_thenExactQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\"abc\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\"abc\""));
    assertThat(actualQueryTokens.get(0).getType(), is(QueryType.EXACT));
  }

  @Test
  void whenStringTokenIsQuotedAndContainsWildcardCharacter_thenComplexQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\"ab*c\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\"ab*c\""));
    assertThat(actualQueryTokens.get(0).getType(), is(QueryType.COMPLEX));
  }

  @Test
  void whenStringTokenIsQuotedAndContainsEscapedWildcardCharacter_thenExactQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\"ab\\*c\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\"ab\\*c\""));
    assertThat(actualQueryTokens.get(0).getType(), is(QueryType.EXACT));
  }

  @Test
  void whenMultipleStringTokensArePassed_thenExactQueryTokensAreReturned() {
    final List<String> stringTokens = List.of("\"ab\\*c\"", "\"ab*c\"", "ab*c");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(3));

    assertThat(actualQueryTokens.get(0).getToken(), is("\"ab\\*c\""));
    assertThat(actualQueryTokens.get(0).getType(), is(QueryType.EXACT));

    assertThat(actualQueryTokens.get(1).getToken(), is("\"ab*c\""));
    assertThat(actualQueryTokens.get(1).getType(), is(QueryType.COMPLEX));

    assertThat(actualQueryTokens.get(2).getToken(), is("ab*c"));
    assertThat(actualQueryTokens.get(2).getType(), is(QueryType.STANDARD));
  }
}
