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
package de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
class SolrVisitorTest {

  private final RSQLParser parser;
  private final SolrVisitor visitor;

  public SolrVisitorTest() {
    this.parser = new RSQLParser();
    this.visitor = new SolrVisitor();
  }

  private String parse(final String query) {
    Node node = parser.parse(query);
    return node.accept(visitor);
  }

  @Test
  void whenCalledWithEqualityComparator_ResultingSolrQueryIsValid() {
    final String query = "fieldname==value";
    final String expectedQuery = "fieldname:\"value\"";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithEqualityNegationComparator_ResultingSolrQueryIsValid() {
    final String query = "fieldname!=value";
    final String expectedQuery = "-fieldname:\"value\"";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithOrOperator_ResultingSolrQueryIsValid() {
    final String query = "fieldname1==value1,fieldname2==value2";
    final String expectedQuery = "fieldname1:\"value1\" OR fieldname2:\"value2\"";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithAndOperator_ResultingSolrQueryIsValid() {
    final String query = "fieldname1==value1;fieldname2==value2";
    final String expectedQuery = "fieldname1:\"value1\" AND fieldname2:\"value2\"";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithInOperator_ResultingSolrQueryIsValid() {
    final String query = "fieldname=in=(value1,value2,value3)";
    final String expectedQuery = "fieldname:(\"value1\" OR \"value2\" OR \"value3\")";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String queryWithOneArgument = "fieldname=in=(value1)";
    final String expectedQueryWithOneArgument = "fieldname:(\"value1\")";
    Node nodeWithOneArgument = parser.parse(queryWithOneArgument);
    final String solrQueryWithOneArgument = nodeWithOneArgument.accept(visitor);
    assertThat(solrQueryWithOneArgument, is(expectedQueryWithOneArgument));
  }

  @Test
  void whenCalledWithGreaterThanComparator_ResultingSolrQueryIsValid() {
    final String query = "fieldname=gt=value";
    final String expectedQuery = "fieldname:{\"value\" TO *}";
    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "fieldname=gt=1234";
    final String expectedNumericQuery = "fieldname:{1234 TO *}";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithGreaterThanOrEqualsComparator_ResultingSolrQueryIsValid() {
    final String query = "fieldname=ge=value";
    final String expectedQuery = "fieldname:[\"value\" TO *]";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "fieldname=ge=1234";
    final String expectedNumericQuery = "fieldname:[1234 TO *]";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithLessThanComparator_ResultingSolrQueryIsValid() {
    final String query = "fieldname=lt=value";
    final String expectedQuery = "fieldname:{* TO \"value\"}";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "fieldname=lt=1234";
    final String expectedNumericQuery = "fieldname:{* TO 1234}";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithLessThanOrEqualsComparator_ResultingSolrQueryIsValid() {
    final String query = "fieldname=le=value";
    final String expectedQuery = "fieldname:[* TO \"value\"]";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));

    final String numericQuery = "fieldname=le=1234";
    final String expectedNumericQuery = "fieldname:[* TO 1234]";
    Node numericNode = parser.parse(numericQuery);
    final String numericSolrQuery = numericNode.accept(visitor);
    assertThat(numericSolrQuery, is(expectedNumericQuery));
  }

  @Test
  void whenCalledWithMultipleFieldNamesInQuery_allFieldnamesAreCollected() {
    final String query =
        "fieldname1==value1;fieldname2==value2;fieldname1=lt=value3;fieldname3=in=(value1,value2,value3)";
    final Set<String> fields = new HashSet<>();
    Node node = parser.parse(query);
    node.accept(visitor, fields);
    assertThat(fields, contains("fieldname1", "fieldname2", "fieldname3"));
    assertThat(fields, hasSize(3));
  }

  /**
   * boosting tests go here
   */
  @Test
  void whenCalledWithBoostedFieldAndUsingSimpleComparison_BoostIsAppended() {
    final String query = SearchField.ID.getName() + "==testid";
    final String expectedQuery =
        SearchField.ID.getName() + ":\"testid\"^" + SearchField.ID.getBoost();

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithUnboostedFieldAndUsingSimpleComparison_BoostIsNotAppended() {
    final String query = SearchField.FULLTEXT.getName() + "==testid";
    final String expectedQuery = SearchField.FULLTEXT.getName() + ":\"testid\"";

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithBoostedFieldAndUsingInOperator_BoostIsAppended() {
    final String idFieldName = SearchField.ID.getName();
    final String query = idFieldName + "=in=(value1,value2,value3)";
    final String expectedQuery =
        idFieldName + ":(\"value1\" OR \"value2\" OR \"value3\")^" + SearchField.ID.getBoost();

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithBoostedFieldAndUsingLessThanOrEqualsComparator_BoostIsAppended() {
    final String idFieldName = SearchField.ID.getName();
    final String query = idFieldName + "=le=value";
    final String expectedQuery = idFieldName + ":[* TO \"value\"]^" + SearchField.ID.getBoost();

    final String solrQuery = parse(query);

    assertThat(solrQuery, is(expectedQuery));
  }

  @Test
  void whenCalledWithGroupField_ResultingQueryIsValidAndFieldGroupIsReplacedAndFieldNamesAreCollected() {
    final String query = "FIELD-GROUP-ORIGIN==searchterm";
    final String expectedQuery = "(" 
        + SearchField.BINDING_ORIG_PLACE.getName() + ":\"searchterm\" OR "
        + SearchField.ORIG_PLACE.getName() + ":\"searchterm\"^100"
        + ")";
    final Set<String> fields = new HashSet<>();
    final Node node = parser.parse(query);
    final String solrQuery = node.accept(visitor, fields);

    assertThat(fields, hasSize(2));
    assertThat(fields, hasItems(
        SearchField.BINDING_ORIG_PLACE.getName(),
        SearchField.ORIG_PLACE.getName()
    ));
    assertThat(solrQuery, is(expectedQuery));
  }
  
  @Test
  void whenCalledWithGroupFieldAsPartOfLogicalNode_ResultingQueryIsValidAndFieldGroupIsReplacedAndFieldNamesAreCollected() {
    final String query = "item-text-search==searchterm1;FIELD-GROUP-ORIGIN!=searchterm2,item-iconography-search==searchterm3";
    final String expectedQuery = "item-text-search:\"searchterm1\" AND "
        + "("
        + "-binding-orig-place-search:\"searchterm2\" OR "
        + "-orig-place-search:\"searchterm2\"^100"
        + ") OR "
        + "item-iconography-search:\"searchterm3\"";
    final Set<String> fields = new HashSet<>();
    final Node node = parser.parse(query);
    final String solrQuery = node.accept(visitor, fields);

    assertThat(fields, hasSize(4));
    assertThat(fields, hasItems(
        SearchField.ITEM_TEXT.getName(),
        SearchField.BINDING_ORIG_PLACE.getName(),
        SearchField.ORIG_PLACE.getName(),
        SearchField.ITEM_ICONOGRAPHY.getName()));
    assertThat(solrQuery, is(expectedQuery));
  }
}
