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

import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Query2SolrQueryConverterTest {
  @Test
  void whenProvidedTermIsMixedQuoted_thenSimpleQuotationMarkShouldBeEscaped() {
    final String actualTerm = Query2SolrQueryConverter.convert("'term\"", ArrayUtils.toArray());

    assertThat(actualTerm, is("\'term\\\""));
  }

  @Test
  void whenProvidedTermContainsMidQuotationMark_thenQuotationMarkShouldBeEscaped() {
    final String actualTerm = Query2SolrQueryConverter.convert("te\"rm", ArrayUtils.toArray());

    assertThat(actualTerm, is("te\\\"rm"));
  }

  @Test
  void whenProvidedTermContainsSolrSpecialChar_thenSpecialCharIsEscaped() {
    final String actualTerm = Query2SolrQueryConverter.convert("test[", ArrayUtils.toArray());

    assertThat(actualTerm, is("test\\["));
  }

  @Test
  void whenProvidedTermContainsWildcardCharAndIsQuoted_thenWildcardCharsAreNotEscapedAndComplexPhraseIsCreated() {
    final String actualTerm = Query2SolrQueryConverter.convert("\"test*?\"", ArrayUtils.toArray(SearchField.SETTLEMENT));

    assertThat(actualTerm, is("_query_:\"{!complexphrase inOrder=true}settlement-search-exact:(\\\"test*?\\\")\""));
  }

  @Test
  void whenProvidedTermContainsWildcardsAndMultipleFields_thenComplexPhraseOnMultipleFieldsIsCreated() {
    final String actualTerm = Query2SolrQueryConverter.convert("\"test*?\"", ArrayUtils.toArray(SearchField.SETTLEMENT, SearchField.REPOSITORY));

    assertThat(actualTerm, is("_query_:\"{!complexphrase inOrder=true}settlement-search-exact:(\\\"test*?\\\") OR repository-search-exact:(\\\"test*?\\\")\""));
  }

  @Test
  void whenProvidedTermContainsWildcardsAndIsNotQuoted_thenWildcardsAreEscaped() {
    final String actualTerm = Query2SolrQueryConverter.convert("test*?", ArrayUtils.toArray());

    assertThat(actualTerm, is("test\\*\\?"));
  }

  @Test
  void whenProvidedTermIsAsterisk_thenAsteriskIsNotEscaped() {
    final String actualTerm = Query2SolrQueryConverter.convert("*", ArrayUtils.toArray());

    assertThat(actualTerm, is("*"));
  }

  @Test
  void whenProvidedTermIsQuotedAsterisk_thenUnquotedAsteriskIsReturned() {
    final String actualTerm = Query2SolrQueryConverter.convert("\"*\"", ArrayUtils.toArray());

    assertThat(actualTerm, is("*"));
  }

  @Test
  void whenProvidedTermIsQuoted_thenIsQuotedReturnsTrue() {
    final boolean isQuoted = Query2SolrQueryConverter.isQuoted("\"test\"");

    assertThat(isQuoted, is(true));
  }

  @Test
  void whenProvidedTermIsNotQuoted_thenIsQuotedReturnsFalse() {
    final boolean isQuoted = Query2SolrQueryConverter.isQuoted("test");

    assertThat(isQuoted, is(false));
  }

  @Test
  void whenProvidedTermContainsSpecialCharsAndWildcards_thenSpecialCharsAreQuotedCorrectlyAndComplexPhraseIsConstructed() {
    final String actualTerm = Query2SolrQueryConverter.convert("\"(test)*\"", ArrayUtils.toArray(SearchField.SETTLEMENT));

    assertThat(actualTerm, is("_query_:\"{!complexphrase inOrder=true}settlement-search-exact:(\\\"\\\\\\\\(test\\\\\\\\)*\\\")\""));
  }

  @Test
  void whenProvidedTermContainsComplexAndExactSearchTokens_thenMultipleConditionsAreConstructed() {
    final String query = "     \"(test)\"      \"foo\"";

    final String actualTerm = Query2SolrQueryConverter.convert(query, ArrayUtils.toArray(SearchField.SETTLEMENT));

    assertThat(actualTerm, is("\"\\(test\\)\" AND \"foo\""));
  }

  @Test
  void whenProvidedTermContainsMultipleExactSearchTokens_thenMultipleConditionsAreConstructed() {
    final String query = "     \"(test)*\"      \"foo\"";

    final String actualTerm = Query2SolrQueryConverter.convert(query, ArrayUtils.toArray(SearchField.SETTLEMENT));

    assertThat(actualTerm, is("_query_:\"{!complexphrase inOrder=true}settlement-search-exact:(\\\"\\\\\\\\(test\\\\\\\\)*\\\")\" AND \"foo\""));
  }

  @Test
  void whenProvidedTermContainsStandardAndExactSearchTokens_thenStandardQueryIsConstructed() {
    final String query = "     \"(test)\"     foo";

    final String actualTerm = Query2SolrQueryConverter.convert(query, ArrayUtils.toArray(SearchField.SETTLEMENT));

    assertThat(actualTerm, is("     \\\"\\(test\\)\\\"     foo"));
  }
}
