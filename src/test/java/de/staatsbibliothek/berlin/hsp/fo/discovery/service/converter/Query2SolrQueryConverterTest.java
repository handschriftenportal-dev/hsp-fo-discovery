package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ConfigBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Query2SolrQueryConverterTest {
  private final Query2SolrQueryConverter query2SolrQueryConverter;

  public Query2SolrQueryConverterTest() {
    query2SolrQueryConverter = new Query2SolrQueryConverter(ConfigBuilder.getFieldProvider(List.of(
        "field-search",
        "field-search-exact",
        "field-search-exact-no-punctuation",
        "repository-search",
        "repository-search-exact",
        "settlement-search",
        "settlement-search-exact"), Map.of()));
  }

  @Test
  void whenProvidedTermIsMixedQuoted_thenSimpleQuotationMarkShouldBeEscaped() {
    final String actualTerm = query2SolrQueryConverter.convert("'term\"", Collections.emptyList()).getQuery();

    assertThat(actualTerm, is("\'term\\\""));
  }

  @Test
  void whenProvidedTermContainsMidQuotationMark_thenQuotationMarkShouldBeEscaped() {
    final String actualTerm = query2SolrQueryConverter.convert("te\"rm", Collections.emptyList()).getQuery();

    assertThat(actualTerm, is("te\\\"rm"));
  }

  @Test
  void whenProvidedTermContainsSolrSpecialChar_thenSpecialCharIsEscaped() {
    final String actualTerm = query2SolrQueryConverter.convert("test[", Collections.emptyList()).getQuery();

    assertThat(actualTerm, is("test\\["));
  }

  @Test
  void whenProvidedTermContainsWildcardCharAndIsQuoted_thenWildcardCharsAreNotEscapedAndComplexPhraseIsCreated() {
    final String actualTerm = query2SolrQueryConverter.convert("\"test*?\"", List.of("settlement-search")).getQuery();

    assertThat(actualTerm, is("_query_:\"{!complexphrase}settlement-search-exact:(\\\"test*?\\\")\""));
  }

  @Test
  void whenProvidedTermContainsWildcardsAndMultipleFields_thenComplexPhraseOnMultipleFieldsIsCreated() {
    final String actualTerm = query2SolrQueryConverter.convert("\"test*?\"", List.of("settlement-search", "repository-search")).getQuery();

    assertThat(actualTerm, is("_query_:\"{!complexphrase}settlement-search-exact:(\\\"test*?\\\") OR repository-search-exact:(\\\"test*?\\\")\""));
  }

  @Test
  void whenProvidedTermContainsWildcardsAndIsNotQuoted_thenWildcardsAreEscaped() {
    final String actualTerm = query2SolrQueryConverter.convert("test*?", Collections.emptyList()).getQuery();

    assertThat(actualTerm, is("test\\*\\?"));
  }

  @Test
  void whenProvidedTermIsAsterisk_thenAsteriskIsNotEscaped() {
    final String actualTerm = query2SolrQueryConverter.convert("*", Collections.emptyList()).getQuery();

    assertThat(actualTerm, is("*"));
  }

  @Test
  void whenProvidedTermIsQuotedAsterisk_thenUnquotedAsteriskIsReturned() {
    final String actualTerm = query2SolrQueryConverter.convert("\"*\"", Collections.emptyList()).getQuery();

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
    final String actualTerm = query2SolrQueryConverter.convert("\"(test)*\"", List.of("settlement-search")).getQuery();

    assertThat(actualTerm, is("_query_:\"{!complexphrase}settlement-search-exact:(\\\"\\\\\\\\(test\\\\\\\\)*\\\")\""));
  }

  @Test
  void whenProvidedTermContainsComplexAndExactSearchTokens_thenMultipleConditionsAreConstructed() {
    final String query = "     \"(test)\"      \"foo\"";

    final String actualTerm = query2SolrQueryConverter.convert(query, List.of("settlement-search")).getQuery();

    assertThat(actualTerm, is("_query_:\"{!complexphrase}settlement-search-exact:(\\\"\\\\\\\\(test\\\\\\\\)\\\")\" AND _query_:\"{!complexphrase}settlement-search-exact:(\\\"foo\\\")\""));
  }

  @Test
  void whenProvidedTermContainsMultipleExactSearchTokens_thenMultipleConditionsAreConstructed() {
    final String query = "     \"(test)*\"      \"foo\"";

    final String actualTerm = query2SolrQueryConverter.convert(query, List.of("settlement-search")).getQuery();

    assertThat(actualTerm, is("_query_:\"{!complexphrase}settlement-search-exact:(\\\"\\\\\\\\(test\\\\\\\\)*\\\")\" AND _query_:\"{!complexphrase}settlement-search-exact:(\\\"foo\\\")\""));
  }

  @Test
  void whenProvidedTermContainsStandardAndExactSearchTokens_thenStandardQueryIsConstructed() {
    final String query = "     \"(test)\"     foo";

    final String actualTerm = query2SolrQueryConverter.convert(query, List.of("settlement-search")).getQuery();

    assertThat(actualTerm, is("_query_:\"{!complexphrase}settlement-search-exact:(\\\"\\\\\\\\(test\\\\\\\\)\\\")\" AND foo"));
  }

  @Test
  void givenExactFieldWithNoPunctuationAndExactSearchToken_whenConvertingToComplexPhraseEmbeddedQuery_thenNoPunctuationFieldIsIncluded() {
    final String query = "\"test\"";

    final String convertedQuery = query2SolrQueryConverter.convert(query, List.of("field-search")).getQuery();

    assertThat(convertedQuery, is("_query_:\"{!complexphrase}field-search-exact:(\\\"test\\\") OR field-search-exact-no-punctuation:(\\\"test\\\")\""));
  }

  @Test
  void whenProvidedTermContainsOnlyExactTokens_thenQueryTypeIsExact() {
    final String query = "\"test\" \"t*st\"";

    final QueryType queryType = query2SolrQueryConverter.convert(query, List.of("settlement-search")).getQueryType();

    assertThat(queryType, is(QueryType.EXACT));
  }

  @Test
  void whenProvidedTermContainsStandardAndExactTokens_thenQueryTypeIsMixed() {
    final String query = "\"test\" t*st";

    final QueryType isExact = query2SolrQueryConverter.convert(query, List.of("settlement-search")).getQueryType();

    assertThat(isExact, is(QueryType.MIXED));
  }

  @Test
  void whenProvidedTermContainsOnlyStandardTokens_thenQueryTypeIsStandard() {
    final String query = "test t*st";

    final QueryType queryType = query2SolrQueryConverter.convert(query, List.of("settlement-search")).getQueryType();

    assertThat(queryType, is(QueryType.STANDARD));
  }

  @Test
  void givenSimpleQuery_whenConvertingToEDisMaxEmbeddedQuery_thenResultIsCorrect() {
    final String query = "foo:bar";

    final String actualTerm = Query2SolrQueryConverter.createEmbeddedQueryWithQueryParser(QueryParser.EDISMAX, query);

    assertThat(actualTerm, is("_query_:\"{!edismax}foo:bar\""));
  }

  @Test
  void givenSimpleQuery_whenConvertingToComplexPhraseEmbeddedQuery_thenResultIsCorrect() {
    final String query = "foo:bar";

    final String actualTerm = Query2SolrQueryConverter.createEmbeddedQueryWithQueryParser(QueryParser.COMPLEX_PHASE, query);

    assertThat(actualTerm, is("_query_:\"{!complexphrase}foo:bar\""));
  }
}
