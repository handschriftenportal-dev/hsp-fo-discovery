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
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.StringTokenizer;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Follows the solr default search behaviour: a search term or phrase surrounded by quotation marks
 * should be treated as exact search, whereas a search term or phrase without quotation marks
 * should be treated as a non-exact search, fuzzy search (including stemming etc.)
 * Additionally, all solr relevant special chars are escaped, except they are semantically necessary
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 */
public class Query2SolrQueryConverter {

  private static final char ASTERISK = '*';
  private static final char QUESTION_MARK = '?';

  /* contains all solr relevant special chars, except those relevant for wild-carding */
  private static final List<Character> SPECIAL_CHARACTERS = List.of('\\', '+', '!', '(', ')', ':', '^', '[', ']', '\"', '{', '}', '~', '|', '&', ';', '/');

  /* contains all solr relevant special chars */
  private static final List<Character> SPECIAL_CHARACTERS_WITH_WILDCARDS = ListUtils.union(SPECIAL_CHARACTERS, List.of(QUESTION_MARK, ASTERISK));

  private static final Pattern WILDCARD_PATTERN = Pattern.compile("(?<!\\\\)\\*|\\?");

  private Query2SolrQueryConverter() {
  }

  /**
   * Converts the given term, so it can be queried by solr
   *
   * @param term   the term to be converted
   * @param fields the fields that should be queried
   * @return the converted term
   */
  public static String convert(final String term, final SearchField[] fields) {
    if (Objects.nonNull(term)) {
      final List<QueryToken> queryTokens = StringTokenToQueryTokenConverter.convert(StringTokenizer.tokenize(term));
      if(queryTokens.stream().allMatch(qt -> qt.getType().equals(QueryType.COMPLEX) || qt.getType().equals(QueryType.EXACT))) {
        return queryTokens.stream().map(qt -> createQuery(qt, fields)).collect(Collectors.joining(" AND "));
      }
      if (isQuoted(term)) {
        if (containsWildcards(term)) {
          return createComplexQuery(term, fields);
        } else {
          return createExactQuery(term);
        }
      } else {
        return createStandardQuery(term);
      }
    }
    return "";
  }

  public static String createQuery(final QueryToken qt, final SearchField[] fields) {
    switch(qt.getType()) {
      case COMPLEX:
        return createComplexQuery(qt.getToken(), fields);
      case EXACT:
        return createExactQuery(qt.getToken());
      case STANDARD:
        return createStandardQuery(qt.getToken());
      default:
        return "";
    }
  }

  public static String createComplexQuery(final String query, final SearchField[] fields) {
    if("\"*\"".equals(query)) {
      return "*";
    }
    final String escapedTerm = escapeForEmbeddedComplexPhrase(query);
    final String[] exactFieldNames = SearchField.getNamesForExactSearch(fields);
    final Optional<String> optQuery = createEmbeddedComplexPhraseQuery(escapedTerm, exactFieldNames, true);
    return optQuery.orElse("*:*");
  }

  public static String createExactQuery(final String query) {
    return escapeTermIgnoringSurroundingQuotationMarks(query, "\\");
  }

  public static String createStandardQuery(final String query) {
    if(query.length() == 1 && query.charAt(0) == ASTERISK) {
      return query;
    }
    else {
      return escapeTerm(query, "\\");
    }
  }

  /**
   * Checks if a given term is quoted (surrounded by leading and trailing double quotation marks)
   *
   * @param term a term to be checked
   * @return true if the term is quoted, false otherwise
   */
  public static boolean isQuoted(final String term) {
    if (Objects.nonNull(term) && term.length() > 2) {
      return term.startsWith("\"") && term.endsWith("\"");
    }
    return false;
  }

  private static String escapeForEmbeddedComplexPhrase(final String term) {
    String result = removeFirstAndLastChar(term);
    result = escapeTermIgnoringWildCards(result, "\\\\\\\\");
    result = addEscapedQuotationMarks(result);

    return result;
  }

  /**
   * Escapes a given term by considering all solr relevant special chars
   * @param term the term to be escaped
   * @return the escaped term
   */
  private static String escapeTerm(final String term, final String escapeSequence) {
    return escapeQueryChars(term, SPECIAL_CHARACTERS_WITH_WILDCARDS::contains, escapeSequence);
  }

  /**
   * Escapes a given term by considering all solr relevant special chars, except wild-carding chars
   * @param term the term to be escaped
   * @return the escaped term
   */
  private static String escapeTermIgnoringWildCards(final String term, final String escapeSequence) {
    return escapeQueryChars(term, SPECIAL_CHARACTERS::contains, escapeSequence);
  }

  /**
   * Adds (double) leading and trailing quotation marks
   * @param term the term to be quoted
   * @return the quoted term
   */
  private static String addQuotationMarks(final String term) {
    return String.format("\"%s\"", term);
  }

  /**
   * Adds (double) escaped, leading and trailing quotation marks
   * @param term the term to be quoted
   * @return the quoted term
   */
  private static String addEscapedQuotationMarks(final String term) {
    return String.format("\\\"%s\\\"", term);
  }

  /**
   * Escapes a given term while ignoring leading and trailing double quotation marks
   * @param term the term to be escaped
   * @return the escaped term
   */

  private static String escapeTermIgnoringSurroundingQuotationMarks(final String term, final String escapeSequence) {
    final String unquotedTerm = removeFirstAndLastChar(term);
    final String escapedAndUnquotedTerm = escapeTerm(unquotedTerm, escapeSequence);

    return addQuotationMarks(escapedAndUnquotedTerm);

  }

  /**
   * Removes the first and the last char of a given term
   * @param quotedTerm the term to be cut
   * @return the cut term
   */
  private static String removeFirstAndLastChar(final String quotedTerm) {
    return quotedTerm.substring(1, quotedTerm.length() - 1);
  }

  /**
   * Checks if a given term contains unescaped wild-carding chars
   * @param term the term to be checked
   * @return true if the term contains any wildcard character, false otherwise
   */
  public static boolean containsWildcards(final String term) {
    return Objects.nonNull(term) && (WILDCARD_PATTERN.matcher(term).find());
  }

  /**
   * based on Solr's ClientUtils {@link ClientUtils#escapeQueryChars(String)} but without escaping the wildcard characters
   *
   * @param s         the term to escape
   * @param predicate function that is applied to each character of {@code s} to decide whether the character should be escaped or not
   * @return the escaped term
   */
  private static String escapeQueryChars(final String s, final Predicate<Character> predicate, final String escapeSequence) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (predicate.test(c)) {
        sb.append(escapeSequence);
      }
      sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Creates a multi clause query by applying a given term to a list of fields and chains it by using an operator and checking for equality
   * @param term the argument term
   * @param fields the fields the query should be applied to
   * @param operator the operator to be used
   * @return an {@code Optional} containig the created query, an empty {@code Optional} otherwise
   */
  public static Optional<String> spreadQuery(final String term, final String[] fields, final String operator) {
    if (ArrayUtils.isEmpty(fields) || StringUtils.isBlank(term)) {
      return Optional.empty();
    }
    return Optional.of(Arrays.stream(fields)
        .map(f -> String.format("%s:(%s)", f, term))
        .collect(Collectors.joining(String.format(" %s ", operator))));
  }

  /**
   * Creates an embedded complexPhrase query
   * @param term the term that should be queried
   * @param fields the fields that the should be applied to
   * @param inOrder whether to match terms in specified order or not
   * @return an {Optional} containing the query, an empty {@code Optional} if an error occurred
   */
  private static Optional<String> createEmbeddedComplexPhraseQuery(final String term, final String[] fields, final boolean inOrder) {
    final Optional<String> spreadQuery = spreadQuery(term, fields, "OR");
    if (spreadQuery.isPresent()) {
      final String complexPhraseQuery = createComplexPhraseQuery(inOrder, spreadQuery.get());
      return Optional.of(createEmbeddedQuery(complexPhraseQuery));
    }
    return Optional.empty();
  }

  /**
   * Creates a complexPhrase query @see <a href="https://solr.apache.org/guide/8_6/other-parsers.html#complex-phrase-query-parser">Complex Query Parser</a>
   * @param inOrdner whether to match terms in specified order or not
   * @param query the query to be used
   * @return the complex phrase query
   */
  public static String createComplexPhraseQuery(final boolean inOrdner, final String query) {
    return String.format("{!complexphrase inOrder=%s}%s", inOrdner, query);
  }

  /**
   * Creates an embedded query by wrapping a given query
   * @param query the query to embed
   * @return the embedded query
   */
  public static String createEmbeddedQuery(final String query) {
    return String.format("_query_:\"%s\"", query);
  }
}