package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.StringTokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
 */
@Component
public class Query2SolrQueryConverter {

  private static final char ASTERISK = '*';
  private static final char QUESTION_MARK = '?';
  /* contains all solr relevant special chars, except those relevant for wild-carding */
  private static final List<Character> SPECIAL_CHARACTERS = List.of('\\', '+', '!', '(', ')', ':', '^', '[', ']', '\"', '{', '}', '~', '|', '&', ';', '/');
  /* contains all solr relevant special chars */
  private static final List<Character> SPECIAL_CHARACTERS_WITH_WILDCARDS = ListUtils.union(SPECIAL_CHARACTERS, List.of(QUESTION_MARK, ASTERISK));
  private static final Pattern WILDCARD_PATTERN = Pattern.compile("(?<!\\\\)\\*|\\?");
  private final FieldProvider fieldProvider;

  @Autowired
  public Query2SolrQueryConverter(final FieldProvider fieldProvider) {
    this.fieldProvider = fieldProvider;
  }

  /**
   * Converts a given query, represented by a search term (can be multiple search terms (words) as well) and the search fields,
   * the query should be performed on
   *
   * @param term              the term or terms to be searched for
   * @param fields            the search fields to be searched on
   * @return the converted (Solr) query
   */
  public SolrQueryParams convert(final String term, final List<String> fields) {
    return convert(term, fields, false, false);
  }

  /**
   * Converts a given query, represented by a search term (can be multiple search terms (words) as well) and the search fields,
   * the query should be performed on
   *
   * @param term              the term or terms to be searched for
   * @param fields            the search fields to be searched on
   * @param isNegotiated      if the expression should be negotiated
   * @param includeFieldNames if the query should contain the field's name
   * @return the converted (Solr) query
   */
  public SolrQueryParams convert(final String term, final List<String> fields, final boolean isNegotiated, final boolean includeFieldNames) {
    final List<QueryToken> queryTokens = StringTokenToQueryTokenConverter.convert(StringTokenizer.tokenize(term));
    final QueryType queryType = detectQueryType(queryTokens);
    final String query = convertQuery(fields, queryTokens, isNegotiated, includeFieldNames);
    return new SolrQueryParams(query, queryTokens, queryType);
  }

  /**
   * Converts the given term, so it can be queried by solr
   *
   * @param fields            the fields that should be queried
   * @param queryTokens       a {@code List} of {@code QueryTokens} that should be used to construct the Solr query
   * @param isNegotiated      if the expression should be negotiated
   * @param includeFieldNames if the query should contain the field's name
   * @return the converted term
   */
  public String convertQuery(final List<String> fields, final List<QueryToken> queryTokens, boolean isNegotiated, boolean includeFieldNames) {
    return queryTokens.stream()
        .map(qt -> createQuery(qt, fields, isNegotiated, includeFieldNames))
        .collect(Collectors.joining(" AND "));
  }

  /**
   * Determines the {@code QueryType} based on a given {@code List} of {@code TokenType}s
   *
   * @param tokens the tokens for determining the overall {@code QueryType}
   * @return the determined {@code QueryType}
   */
  private static QueryType detectQueryType(final List<QueryToken> tokens) {
    QueryType result = null;
    for(QueryToken qt : tokens) {
      result = combineTokenTypes(result, qt.getType());
    }
    return result;
  }

  /**
   * Combines a {@code QueryType} with a {@code TokenType}
   * {@code
   * QueryType.STANDARD + TokenType.STANDARD = QueryType.STANDARD
   * QueryType.STANDARD + TokenType.EXACT = QueryType.MIXED
   * QueryType.STANDARD + TokenType.COMPLEX = QueryType.MIXED
   * QueryType.MIXED + any TokenType = QueryType.MIXED
   * QueryType.EXACT + TokenType.STANDARD = QueryType.MIXED
   * QueryType.EXACT + TokenType.EXACT = QueryType.EXACT
   * QueryType.EXACT + TokenType.COMPLEX = QueryType.EXACT
   * }
   * @param qt the {@code QueryType} that should be combined
   * @param tt the {@code TokenType} that should be combined
   * @return the resulting {@code QueryType}
   */
  private static QueryType combineTokenTypes(final QueryType qt, final TokenType tt) {
    if(qt == null) {
      return convertTokenType(tt);
    } else if(QueryType.MIXED.equals(qt)) {
      return QueryType.MIXED;
    } else if(QueryType.STANDARD.equals(qt) && (TokenType.EXACT.equals(tt) || TokenType.COMPLEX.equals(tt))) {
      return QueryType.MIXED;
    } else if(QueryType.EXACT.equals(qt) && TokenType.STANDARD.equals(tt)) {
      return QueryType.MIXED;
    } else {
      return qt;
    }
  }

  /**
   * Converts a given {@code TokenType} to the corresponding {@code QueryType}
   * @param tt the {@code TokenType} thet should be converted
   * @return the resulting {@code QueryType}
   */
  private static QueryType convertTokenType(final TokenType tt) {
    return TokenType.STANDARD.equals(tt) ? QueryType.STANDARD : QueryType.EXACT;
  }

  /**
   * Creates a (Solr) query based on a given {@code QueryToken}
   *
   * @param qt                the {@code QueryToken} containing the {@code TokenType} and the {@code Token} (representing the search term)
   * @param fields            the fields on which to search
   * @param isNegotiated      if the expression should be negotiated
   * @param includeFieldNames if the query should contain the field's name
   * @return the generated query
   */
  public String createQuery(final QueryToken qt, final List<String> fields, boolean isNegotiated, boolean includeFieldNames) {
    return switch (qt.getType()) {
      case COMPLEX, EXACT -> createComplexQuery(qt.getToken(), fields, isNegotiated);
      case STANDARD -> createStandardQuery(qt.getToken(), fields, isNegotiated, includeFieldNames);
    };
  }

  /**
   * Creates an embedded complex phrase query
   *
   * @param term         the term to be searched for
   * @param fields       the fields to be searched on
   * @param isNegotiated      if the expression should be negotiated
   * @return the resulting embedded complex phrase query
   */
  public String createComplexQuery(final String term, final List<String> fields, boolean isNegotiated) {
    if("\"*\"".equals(term)) {
      return "*";
    }
    final String escapedTerm = escapeForEmbeddedComplexPhrase(term);
    final List<String> exactFieldNames = ListUtils.union(fieldProvider.getExactNames(fields), fieldProvider.getExactNoPunctuationNames(fields));
    final Optional<String> optQuery = createEmbeddedComplexPhraseQuery(escapedTerm, FieldProvider.removeBoostingFactors(exactFieldNames), isNegotiated);
    return optQuery.orElse("*:*");
  }

  /**
   * Creates a standard query by simply escaping the search term
   *
   * @param term              the termed to be escaped
   * @param isNegotiated      if the expression should be negotiated
   * @param includeFieldNames if the query should contain the field's name
   * @return the standard query
   */
  public static String createStandardQuery(final String term, final List<String> fields, boolean isNegotiated, boolean includeFieldNames) {
    final String escapedTerm;
    if(term.length() == 1 && term.charAt(0) == ASTERISK) {
      escapedTerm = term;
    }
    else {
      escapedTerm = escapeTerm(term, "\\");
    }
    if(includeFieldNames) {
      return spreadPhrase(escapedTerm, fields, "OR", isNegotiated).orElse(StringUtils.EMPTY);
    } else {
      return escapedTerm;
    }
  }

  /**
   * Checks if a given term is quoted (surrounded by leading and trailing double quotation marks)
   *
   * @param term the term to be checked
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
   * Adds (double) escaped, leading and trailing quotation marks
   * @param term the term to be quoted
   * @return the quoted term
   */
  private static String addEscapedQuotationMarks(final String term) {
    return String.format("\\\"%s\\\"", term);
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
   *
   * @param term         the argument term
   * @param fields       the fields the query should be applied to
   * @param isNegotiated whether the query should be negotiated or nor not
   * @param operator     the operator to be used
   * @return an {@code Optional} containing the created query, an empty {@code Optional} otherwise
   */
  public Optional<String> spreadQuery(final String term, final List<String> fields, final String operator, final boolean isNegotiated) {
    if (CollectionUtils.isEmpty(fields) || StringUtils.isBlank(term)) {
      return Optional.empty();
    }
    return Optional.of(fields.stream()
        .map(f -> String.format("%s%s:(%s)%s", isNegotiated ? "-" : "", f, term, fieldProvider.getBoosting(f)))
        .collect(Collectors.joining(String.format(" %s ", operator))));
  }

  /**
   * Creates a multi term query by applying a given phrase to a field and chains the terms by using an operator
   *
   * @param phrase         the argument term
   * @param fields       the fields the query should be applied to
   * @param isNegotiated      if the expression should be negotiated
   * @param operator     the operator to be used
   * @return an {@code Optional} containing the created query, an empty {@code Optional} otherwise
   */
  public static Optional<String> spreadPhrase(final String phrase, final List<String> fields, final String operator, final boolean isNegotiated) {
    if (CollectionUtils.isEmpty(fields) || StringUtils.isBlank(phrase)) {
      return Optional.empty();
    }
    return Optional.of(fields.stream()
        .map(f -> String.format("%s%s:%s", isNegotiated ? "-" : "", f, phrase))
        .collect(Collectors.joining(String.format(" %s ", operator))));
  }

  /**
   * Creates an embedded complexPhrase query
   *
   * @param term         the term that should be queried
   * @param fields       the fields that the query should be applied to
   * @param isNegotiated      if the expression should be negotiated
   * @return an {Optional} containing the query, an empty {@code Optional} if an error occurred
   */
  private Optional<String> createEmbeddedComplexPhraseQuery(final String term, final List<String>fields, boolean isNegotiated) {
    final Optional<String> spreadQuery = spreadQuery(term, fields, "OR", isNegotiated);
    if (spreadQuery.isPresent()) {
      final String complexPhraseQuery = createEmbeddedQueryWithQueryParser(QueryParser.COMPLEX_PHASE, spreadQuery.get());
      return Optional.of(complexPhraseQuery);
    }
    return Optional.empty();
  }


  private static String createQueryWithQueryParser(final QueryParser queryParser, final String query) {
    return String.format("{!%s}%s", queryParser.getValue(), query);
  }

  /**
   * Creates an embedded query by wrapping a given query
   * @param query the query to embed
   * @return the embedded query
   */
  public static String createEmbeddedQuery(final String query) {
    return String.format("_query_:\"%s\"", query);
  }

  /**
   * Creates an embedded query by using the given queryParser as @see <a href="https://solr.apache.org/guide/solr/latest/query-guide/local-params.html#query-type-short-form">Query Type Short Form</a>
   * @param queryParser the {@link QueryParser} to be used as Query Type Short Form
   * @param query the actual query
   * @return the resulting embedded query
   */
  public static String createEmbeddedQueryWithQueryParser(final QueryParser queryParser, final String query)  {
   final String queryWithQueryParser = createQueryWithQueryParser(queryParser, query);
   return createEmbeddedQuery(queryWithQueryParser);
  }

  @Data
  @AllArgsConstructor
  public static class SolrQueryParams {
    String query;
    List<QueryToken> tokens;
    QueryType queryType;
  }
}
