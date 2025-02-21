/*
 * MIT License
 *
 * Copyright (c) 2021 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.Query2SolrQueryConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.rsql.SolrVisitor;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ConfigBuilder;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
class SolrVisitorTest {
  private final RSQLParser parser;
  private final SolrVisitor visitor;

  public SolrVisitorTest() {

    FieldProvider fieldProvider = ConfigBuilder.getFieldProvider(
        List.of(
            "binding-search",
            "binding-orig-place-search^10",
            "booklet-search",
            "collection-search",
            "item-iconography-search",
            "item-text-search",
            "item-text-search",
            "fulltext-search",
            "fulltext-search-exact",
            "repository-search",
            "repository-search-exact",
            "orig-place-search^5"),
        Map.of("FIELD-GROUP-ORIGIN", List.of("orig-place-search", "binding-orig-place-search")));
    this.parser = new RSQLParser();
    Query2SolrQueryConverter query2SolrQueryConverter = new Query2SolrQueryConverter(fieldProvider);
    this.visitor = new SolrVisitor(fieldProvider, query2SolrQueryConverter);
  }

  private String parse(final String query) {
    Node node = parser.parse(query);
    return node.accept(visitor);
  }

  @Test
  void whenCalledWithEqualityComparator_ResultingSolrQueryIsValid() {
    final String query = "binding-search==value";
    final String expectedQuery = "binding-search:value";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithEqualityNegationComparator_ResultingSolrQueryIsValid() {
    final String query = "binding-search!=value";
    final String expectedQuery = "-binding-search:value";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithOrOperator_ResultingSolrQueryIsValid() {
    final String query = "binding-search==value1,booklet-search==value2";
    final String expectedQuery = "(binding-search:value1 OR booklet-search:value2)";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithAndOperator_ResultingSolrQueryIsValid() {
    final String query = "binding-search==value1;booklet-search==value2";
    final String expectedQuery = "binding-search:value1 AND booklet-search:value2";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithInOperator_ResultingSolrQueryIsValid() {
    final String query = "binding-search=in=(value1,value2,value3)";
    final String expectedQuery = "binding-search:(\"value1\" OR \"value2\" OR \"value3\")";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String queryWithOneArgument = "binding-search=in=(value1)";
    final String expectedQueryWithOneArgument = "binding-search:(\"value1\")";
    Node nodeWithOneArgument = parser.parse(queryWithOneArgument);
    final String solrQueryWithOneArgument = nodeWithOneArgument.accept(visitor);
    assertThat(solrQueryWithOneArgument, is(expectedQueryWithOneArgument));
  }

  @Test
  void whenCalledWithGreaterThanComparator_ResultingSolrQueryIsValid() {
    final String query = "binding-search=gt=value";
    final String expectedQuery = "binding-search:{\"value\" TO *}";
    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "binding-search=gt=1234";
    final String expectedNumericQuery = "binding-search:{1234 TO *}";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithGreaterThanOrEqualsComparator_ResultingSolrQueryIsValid() {
    final String query = "binding-search=ge=value";
    final String expectedQuery = "binding-search:[\"value\" TO *]";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "binding-search=ge=1234";
    final String expectedNumericQuery = "binding-search:[1234 TO *]";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithLessThanComparator_ResultingSolrQueryIsValid() {
    final String query = "binding-search=lt=value";
    final String expectedQuery = "binding-search:{* TO \"value\"}";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "binding-search=lt=1234";
    final String expectedNumericQuery = "binding-search:{* TO 1234}";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithLessThanOrEqualsComparator_ResultingSolrQueryIsValid() {
    final String query = "binding-search=le=value";
    final String expectedQuery = "binding-search:[* TO \"value\"]";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "binding-search=le=1234";
    final String expectedNumericQuery = "binding-search:[* TO 1234]";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithMultipleFieldNamesInQuery_allFieldnamesAreCollected() {
    final String query =
        "binding-search==value1;booklet-search==value2;binding-search=lt=value3;collection-search=in=(value1,value2,value3)";
    final Set<String> fields = new HashSet<>();
    Node node = parser.parse(query);
    node.accept(visitor, fields);
    assertThat(fields, containsInAnyOrder("binding-search", "booklet-search", "collection-search"));
  }

  /**
   * boosting tests go here
   */
/*  @Test
  void whenCalledWithBoostedFieldAndUsingSimpleComparison_BoostIsAppended() {
    final String query = SearchField.ID.getName() + "==testid";
    final String expectedQuery =
        SearchField.ID.getName() + ":testid" + SearchField.ID.getBoost();

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithUnboostedFieldAndUsingSimpleComparison_BoostIsNotAppended() {
    final String query = SearchField.FULLTEXT.getName() + "==\"testid 123\"";
    final String expectedQuery = SearchField.FULLTEXT.getName() + ":testid+AND+123";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }*/

/*  @Test
  void whenCalledWithBoostedFieldAndUsingInOperator_BoostIsAppended() {
    final String idFieldName = SearchField.ID.getName();
    final String query = idFieldName + "=in=(value1,value2,value3)";
    final String expectedQuery =
        idFieldName + ":(\"value1\" OR \"value2\" OR \"value3\")^" + SearchField.ID.getBoost();

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }*/

/*  @Test
  void whenCalledWithBoostedFieldAndUsingLessThanOrEqualsComparator_BoostIsAppended() {
    final String idFieldName = SearchField.ID.getName();
    final String query = idFieldName + "=le=value";
    final String expectedQuery = idFieldName + ":[* TO \"value\"]^" + SearchField.ID.getBoost();

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }*/

  @Test
  void whenCalledWithGroupField_ResultingQueryIsValidAndFieldGroupIsReplacedAndFieldNamesAreCollected() {
    final String query = "FIELD-GROUP-ORIGIN==searchterm";
    final String expectedQuery = "(orig-place-search:searchterm OR binding-orig-place-search:searchterm)";
    final Set<String> fields = new HashSet<>();
    final Node node = parser.parse(query);
    final String solrQuery = node.accept(visitor, fields);

    assertThat(fields, containsInAnyOrder("orig-place-search^5", "binding-orig-place-search^10"));
    assertThat(solrQuery, is(expectedQuery));
  }
  
  @Test
  void whenCalledWithGroupFieldAsPartOfLogicalNode_ResultingQueryIsValidAndFieldGroupIsReplacedAndFieldNamesAreCollected() {
    final String query = "item-text-search==searchterm1;FIELD-GROUP-ORIGIN!=searchterm2,item-iconography-search==searchterm3";
    final String expectedQuery = "(item-text-search:searchterm1 AND "
        + "("
        + "-orig-place-search:searchterm2 OR "
        + "-binding-orig-place-search:searchterm2"
        + ") OR "
        + "item-iconography-search:searchterm3)";
    final Set<String> fields = new HashSet<>();
    final Node node = parser.parse(query);
    final String solrQuery = node.accept(visitor, fields);

    assertThat(fields, containsInAnyOrder("orig-place-search^5", "binding-orig-place-search^10", "item-text-search", "item-iconography-search"));
    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void givenMultipleSearchTerms() {
    final String query = "fulltext-search==\"Leipzig \\\"Bibli*thek\\\"\"";
    final String expectedQuery = "fulltext-search:Leipzig AND _query_:\"{!complexphrase}fulltext-search-exact:(\\\"Bibli*thek\\\")\"";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }
  @Test
  void givenRSQLQueryWithDefaultSearch_whenConverting_thenResultingQueryIsCorrect() {
    final String query = "repository-search==\"Leipzig\"";
    final String expectedQuery = "repository-search:Leipzig";

    final String resultingQuery = parse(query);

    assertThat(resultingQuery, is(expectedQuery));
  }

  @Test
  void givenRSQLQueryWithExactSearch_whenConverting_thenResultingQueryIsCorrect() {
    final String query = "repository-search==\"\\\"Leipzig\\\"\"";
    final String expectedQuery = "_query_:\"{!complexphrase}repository-search-exact:(\\\"Leipzig\\\")\"";

    final String resultingQuery = parse(query);

    assertThat(resultingQuery, is(expectedQuery));
  }

  @Test
  void givenRSQLQueryWithComplexSearch_whenConverting_thenResultingQueryIsCorrect() {
    final String query = "repository-search==\"\\\"Leipz*g\\\"\"";
    final String expectedQuery = "_query_:\"{!complexphrase}repository-search-exact:(\\\"Leipz*g\\\")\"";

    final String resultingQuery = parse(query);

    assertThat(resultingQuery, is(expectedQuery));
  }

  @Test
  void givenRSQLQueryWithCombinedTerm_whenConverting_thenResultingQueryIsCorrect() {
    final String query = "repository-search==\"Leipzig \\\"Bibliothek\\\"\"";
    final String expectedQuery = "repository-search:Leipzig AND _query_:\"{!complexphrase}repository-search-exact:(\\\"Bibliothek\\\")\"";

    final String resultingQuery = parse(query);

    assertThat(resultingQuery, is(expectedQuery));
  }

  @Test
  void givenRSQLQueryWithBrackets_whenConverting_thenResultingBracketingIsCorrect() {
    final String query = "(booklet-search==\"a\" or booklet-search==\"b\") and (booklet-search==\"c\" or booklet-search==\"d\")";
    final String expectedQuery = "(booklet-search:a OR booklet-search:b) AND (booklet-search:c OR booklet-search:d)";

    final String resultingQuery = parse(query);

    assertThat(resultingQuery, is(expectedQuery));
  }

  @Test
  void givenRSQLQueryWithComplexBrackets_whenConverting_thenResultingBracketingIsCorrect() {
    final String query = "((booklet-search==\"a\" or booklet-search==\"b\") or booklet-search==\"c\") and ((booklet-search==\"d\" or booklet-search==\"e\") and booklet-search==\"f\")";
    final String expectedQuery = "((booklet-search:a OR booklet-search:b) OR booklet-search:c) AND (booklet-search:d OR booklet-search:e) AND booklet-search:f";

    final String resultingQuery = parse(query);

    assertThat(resultingQuery, is(expectedQuery));
  }
}
