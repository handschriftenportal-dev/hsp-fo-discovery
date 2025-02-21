package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StringTokenizerTest {
  @Test
  void whenStringIsEmpty_thenNoTokensAreReturned() {
    final String queryString = "";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, empty());
  }

  @Test
  void whenStringContainsOnlyWhiteSpaces_thenNoTokensAreReturned() {
    final String queryString = "      ";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, empty());
  }

  @Test
  void whenStringContainsUnquotedTerm_thenCorrectTokenIsReturned() {
    final String queryString = "foo bar";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, contains("foo bar"));
  }

  @Test
  void whenStringContainsQuotedTerm_thenCorrectTokenIsReturned() {
    final String queryString = "\"foo bar\"";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, contains("\"foo bar\""));
  }

  @Test
  void whenStringContainsSingledEscapedQuotationMark_thenCorrectTokensAreReturned() {
    final String queryString = "te\"rm";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, contains("te\"rm"));
  }

  @Test
  void whenStringContainsQuotedTermWithWhitespaces_thenCorrectTokenIsReturned() {
    final String queryString = "\"   foo bar   \"";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, contains("\"   foo bar   \""));
  }

  @Test
  void whenStringContainsQuotedTermWithEscapedQuotationMarks_thenCorrectTokenIsReturned() {
    final String queryString = "\"\\\"foo bar\\\"\"";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, contains("\"\\\"foo bar\\\"\""));
  }

  @Test
  void whenStringContainsQuotedAndUnquotedTerm_thenCorrectTokensAreReturned() {
    final String queryString = " foo    \" bar \"";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, contains("foo", "\" bar \""));
  }

  @Test
  void whenStringIsVeryComplex_thenCorrectTokensAreReturned() {
    final String queryString = "   foo    \" \\\" bar \\\" \"    \"foobar\"    baz     quz \"\"";

    final List<String> actualTokens = StringTokenizer.tokenize(queryString);

    assertThat(actualTokens, contains("foo", "\" \\\" bar \\\" \"", "\"foobar\"", "baz     quz"));
  }
}
