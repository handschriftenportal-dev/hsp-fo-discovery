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
