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
package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.rsql.RsqlToQueryParamsConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.rsql.SolrVisitor;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ConfigBuilder;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SearchParams2SolrParamsConverterTest {
  private final HighlightConfig highlightConfig;
  private final SearchParams2SolrParamsConverter searchParams2SolrParamsConverter;

  public SearchParams2SolrParamsConverterTest() {
    FieldProvider fieldProvider = ConfigBuilder.getFieldProvider(List.of(
        "group-id-search",
        "id-search^10",
        "material-search^10",
        "person-author-search",
        "repository-search^100",
        "repository-search-exact^100",
        "settlement-search^100",
        "settlement-search-exact^100",
        "settlement-search-stemmed",
        "test-search",
        "test-2-search",
        "test-3-search"
    ), Map.of());
    highlightConfig = new HighlightConfig(3, 100, 250, "em");
    searchParams2SolrParamsConverter = new SearchParams2SolrParamsConverter();
    RsqlToQueryParamsConverter extendedSearchStringToQueryParamsConverter = new RsqlToQueryParamsConverter();
    extendedSearchStringToQueryParamsConverter.setSolrVisitor(new SolrVisitor(fieldProvider, new Query2SolrQueryConverter(fieldProvider)));
    searchParams2SolrParamsConverter.setExtendedSearchStringToQueryParamsConverter(extendedSearchStringToQueryParamsConverter);
    searchParams2SolrParamsConverter.setFieldProvider(fieldProvider);
    searchParams2SolrParamsConverter.setQuery2SolrQueryConverter(new Query2SolrQueryConverter(fieldProvider));
  }

  @Test
  void whenStartAndRowsAreGiven_QueryContainsStartAndRows() {
    final SearchParams params = SearchParams.builder()
        .withPhrase("test")
        .withRows(13)
        .withStart(1)
        .build();
    final SolrParams solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("start=1&rows=13"), is(true));
  }

  @Test
  void whenHighlightingIsTrue_QueryContainsHighlightingParameters() {
    SolrParams solrParams;
    final SearchParams params = SearchParams.builder()
        .withPhrase("test")
        .withSearchFields(List.of("settlement-search"))
        .build();

    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(solrParams.toString().contains("hl=on"), is(false));
    assertThat(solrParams.toString().contains("hl."), is(false));
    /* enable highlighting */
    params.setHighlight(true);

    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(solrParams.toString().contains("hl=on"), is(true));
    assertThat(solrParams.toString().contains("hl.fl=settlement-search&"), is(true));
    assertThat(solrParams.toString().contains("hl.q=test&"), is(true));
    assertThat(solrParams.toString().contains("hl.highlightMultiTerm=true"), is(true));
    assertThat(solrParams.toString().contains("hl.snippets="), is(true));
    assertThat(solrParams.toString().contains("hl.maxAnalyzedChars=" + (Integer.MAX_VALUE - 1)), is(true));
    assertThat(solrParams.toString().contains("hl.mergeContiguous=true"), is(true));
    assertThat(solrParams.toString().contains("qf=settlement-search"), is(true));
  }

  @Test
  void whenHighlightingIsTrueAndSearchFieldsContainGroupId_HighlightingFieldsDoNotContainGroupId() {
    SolrParams solrParams;
    final SearchParams paramsWithHL;
    final SearchParams params = SearchParams.builder()
        .withPhrase("test")
        .withSearchFields(List.of("settlement-search"))
        .build();

    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(solrParams.toString().contains("hl=on"), is(false));
    assertThat(solrParams.toString().contains("hl."), is(false));

    paramsWithHL = SearchParams.builder()
        .withPhrase("test")
        .withHighlight(true)
        .withHighlightFields(List.of("repository-search", "group-id-search"))
        .withSearchFields(List.of("settlement-search"))
        .build();

    solrParams = searchParams2SolrParamsConverter.convert(paramsWithHL, highlightConfig);

    assertThat(solrParams.toString().contains("hl.fl=group-id-search"), is(false));
  }

  @Test
  void whenGroupingIsNecessary_QueryContainsGroupingParameters() {
    SearchParams params;
    params =  SearchParams.builder()
        .withGrouping(true)
        .withPhrase("test")
        .build();
    SolrParams solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("group=true"), is(true));
    assertThat(solrParams.toString().contains("group.field=group-id-search"), is(true));
    assertThat(solrParams.toString().contains("group.limit=100"), is(true));
    assertThat(solrParams.toString().contains("group.ngroups=true"), is(true));

    params = SearchParams.builder()
        .withPhrase("test")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("group="), is(false));
    assertThat(solrParams.toString().contains("group.field="), is(false));
    assertThat(solrParams.toString().contains("group.limit="), is(false));
    assertThat(solrParams.toString().contains("group.ngroups="), is(false));

    params = SearchParams.builder()
        .withPhrase("testid")
        .withGrouping(true)
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("group=true"), is(true));
    assertThat(solrParams.toString().contains("group.field=group-id-search"), is(true));
    assertThat(solrParams.toString().contains("group.limit=100"), is(true));
    assertThat(solrParams.toString().contains("group.ngroups=true"), is(true));
    
    params =SearchParams.builder()
        .withPhrase("testid")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("group="), is(false));
    assertThat(solrParams.toString().contains("group.field="), is(false));
    assertThat(solrParams.toString().contains("group.limit="), is(false));
    assertThat(solrParams.toString().contains("group.ngroups="), is(false));
  }

  @Test
  void whenFilterQueriesAreProvided_QueryContainsFilterQueriesWithExclusionTags() {
    SearchParams params;
    SolrParams solrParams;

    params = SearchParams.builder()
        .withPhrase("field:test")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("fq="), is(false));

    params = SearchParams.builder()
        .withPhrase("test")
        .withFilterQueries(Map.of("material-facet:paper", "material-facet"))
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("fq={!tag%3Dsolr_fq_material-facet}material-facet:paper"), is(true));
  }

  @Test
  void whenFacetsAreConfigured_QueryContainsTaggedFacets() {
    SearchParams params;
    SolrParams solrParams;

    params = SearchParams.builder()
        .withFacets(List.of("material-facet", "settlement-facet"))
        .withPhrase("test")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("facet=true"), is(true));
    assertThat(solrParams.toString().contains("facet.excludeTerms=hsp:digitized"), is(true));
    assertThat(solrParams.toString().contains("facet.sort=count"), is(true));
    assertThat(solrParams.toString().contains("facet.missing=true"), is(true));
    assertThat(solrParams.toString().contains("facet.limit=-1"), is(true));
    assertThat(solrParams.toString().contains("facet.field={!ex%3Dsolr_fq_material-facet}material-facet"), is(true));
    assertThat(solrParams.toString().contains("facet.field={!ex%3Dsolr_fq_settlement-facet}settlement-facet"), is(true));
  }

  @Test
  void whenStatsAreConfigured_QueryContainsTaggedAndConfiguredStats() {
    SearchParams params;
    SolrParams solrParams;
    params = SearchParams.builder()
        .withPhrase("field:test")
        .withStats(List.of("orig-date-from-facet", "leaves-count-facet"))
        .build();

    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("stats=true"), is(true));
    assertThat(solrParams.toString().contains(
        "stats.field={!ex%3Dsolr_fq_orig-date-facet+min%3Dtrue+max%3Dtrue+count%3Dtrue+missing%3Dtrue}orig-date-from-facet"), is(true));
    assertThat(solrParams.toString().contains(
        "stats.field={!ex%3Dsolr_fq_leaves-count-facet+min%3Dtrue+max%3Dtrue+count%3Dtrue+missing%3Dtrue}leaves-count-facet"), is(true));
  }

  @Test
  void whenTermSearchIsUsed_QueryContainsBoostedFields() {
    final SolrParams solrParams;
    final SearchParams params = SearchParams.builder()
        .withPhrase("searching")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    /* check prio 1 field */
    assertThat(solrParams.toString().contains("id-search^10"), is(true));

    /* check prio 2 field */
    assertThat(solrParams.toString()
        .contains("settlement-search^100"), is(true));

    /* check prio 3 field */
    assertThat(solrParams.toString()
        .contains("material-search^10"), is(true));

    /* check unboosted field */
    assertThat(!solrParams.toString().contains("fulltext-search^"), is(true));
  }

  @Test
  void whenValidSortPhraseIsProvided_QueryContainsSortParam() {
    SearchParams params;
    SolrParams solrParams;

    params = SearchParams.builder()
        .withPhrase("test")
        .withSortPhrase("orig-date-from-sort asc")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("sort=orig-date-from-sort+asc"), is(true));

    params = SearchParams.builder()
        .withPhrase("searching")
        .withSortPhrase("orig-date-to-sort desc")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("sort=orig-date-to-sort+desc"), is(true));
  }

  @Test
  void whenInvalidSortPhraseIsProvided_QueryContainsNoSortParam() {
    SearchParams params;
    SolrParams solrParams;
    
    params = SearchParams.builder()
        .withPhrase("test")
        .withSortPhrase("price asc")
        .build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("sort="), is(false));

    params = SearchParams.builder()
        .withPhrase("searching")
        .withSortPhrase("popularity desc").build();
    solrParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);
    assertThat(solrParams.toString().contains("sort="), is(false));
  }

  @Test
  void whenPhraseAndQueryAreEmpty_thenCorrectDefaultValueIsReturned() {
    final SearchParams params = SearchParams.builder().build();

    final SolrParams sParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(sParams.get("q"), is("*:*"));
  }

  @Test
  void whenPhraseIsQuotedAndSearchFieldsAreTextType_thenFieldsAreConvertedToExactFieldNames() {
    final SearchParams params = SearchParams.builder()
        .withPhrase("\"test\"")
        .withSearchFields(List.of("settlement-search", "repository-search"))
        .build();

    final SolrParams sParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(sParams.getParams("qf"), arrayWithSize(2));
    assertThat(sParams.getParams("qf"), arrayContainingInAnyOrder("settlement-search-exact^100", "repository-search-exact^100"));
  }

  @Test
  void whenSearchPhraseIsQuoted_thenHighlightingQueryIsPerformedOnExactFieldNames() {
    final SearchParams params = SearchParams.builder()
        .withPhrase("\"Nürnberg\"")
        .withSearchFields(List.of("settlement-search"))
        .build();

    final SolrParams resultParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(resultParams.getParams("qf"), arrayWithSize(1));
    // original field is still boosted, but highlighting doesn't mind and so do we
    assertThat(resultParams.getParams("qf"), arrayContainingInAnyOrder("settlement-search-exact^100"));
  }

  @Test
  void whenNoQueryOperatorIsGiven_thenANDIsAdd() {
    final SearchParams params = SearchParams.builder().build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(actualParams.get("q.op"), is("AND"));
  }

  @Test
  void whenQueryOperatorIsOR_thenORisAdd() {
    final SearchParams params = SearchParams.builder()
        .withQueryOperator(SearchParams.QueryOperator.OR)
        .build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(actualParams.get("q.op"), is("OR"));
  }

  @Test
  void whenSearchingFuzzyOnAllFields_thenAllStemmedFieldsAreIncluded() {
    final SearchParams params = SearchParams.builder()
        .build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(List.of(actualParams.getParams("qf")), hasItems(
        "group-id-search",
        "id-search^10",
        "material-search^10",
        "person-author-search",
        "repository-search^100",
        "settlement-search^100",
        "settlement-search-stemmed"));
  }

  @Test
  void whenSearchingFuzzyOnUnstemmableField_thenNoStemmedFieldIsIncluded() {
    final SearchParams params = SearchParams.builder()
        .withSearchFields(List.of("person-author-search"))
        .build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(List.of(actualParams.getParams("qf")), contains("person-author-search"));
  }

  @Test
  void givenExtendedSearchPhrase_whenConverting2SolrParams_thenQueryAndFieldsAreCorrect() {
    final SearchParams params = SearchParams.builder()
        .withPhraseExtended("repository-search==\"Universitätsbibliothek Leipzig\"")
        .build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(actualParams.get("q"), is("repository-search:Universitätsbibliothek Leipzig"));
    assertThat(actualParams.get("qf"), is("repository-search^100"));
  }

  @Test
  void givenExtendedComplexSearchPhrase_whenConverting2SolrParams_thenQueryAndFieldsAreCorrect() {
    final SearchParams params = SearchParams.builder()
        .withPhraseExtended("repository-search==\"\\\"Universit*tsbibliothek\\\" Leipzig\"")
        .build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(actualParams.get("q"), is("_query_:\"{!complexphrase}repository-search-exact:(\\\"Universit*tsbibliothek\\\")^100\" AND repository-search:Leipzig"));
    assertThat(actualParams.get("qf"), is("repository-search^100"));
  }

  @Test
  void givenOverlappingSearchFieldsAndHighlightFields_whenConverting_thenQueryFieldsAreDisjunct() {
    final SearchParams params = SearchParams.builder()
        .withSearchFields(List.of("test-search"))
        .withHighlight(true)
        .withHighlightFields(List.of("test-search", "test-2-search", "test-3-search"))
        .build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(Arrays.stream(actualParams.getParams("qf")).toList(), containsInAnyOrder("test-search", "test-2-search", "test-3-search"));
  }

  @Test
  void givenSearchParamsForPersonSearchGroup_whenConverting2SolrParams_thenQueryFieldsAreNotContainingNullValue() {
    final SearchParams params = SearchParams.builder()
        .withHighlight(true)
        .withHighlightFields(List.of("person-author-search", "person-bookbinder-search", "person-commissioned-by-search", "person-conservator-search", "person-illuminator-search", "person-mentioned-in-search", "person-other-search", "person-previous-owner-search", "person-scribe-search", "person-translator-search"))
        .withQuery("test")
        .withSearchFields(List.of("person-author-search", "person-bookbinder-search", "person-commissioned-by-search", "person-conservator-search", "person-illuminator-search", "person-mentioned-in-search", "person-other-search", "person-previous-owner-search", "person-scribe-search", "person-translator-search"))
        .build();

    final SolrParams actualParams = searchParams2SolrParamsConverter.convert(params, highlightConfig);

    assertThat(Arrays.asList(actualParams.getParams("qf")), everyItem(notNullValue()));
  }
}