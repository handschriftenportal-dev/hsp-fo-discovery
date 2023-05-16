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

import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchFieldStemmed;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.solr.common.params.SolrParams;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
class SearchParams2SolrParamsConverterTest {
  @Test
  void whenStartAndRowsAreGiven_QueryContainsStartAndRows() {
    final SearchParams params = SearchParams.builder()
        .withPhrase("test")
        .withRows(13)
        .withStart(1)
        .build();
    final SolrParams solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("start=1&rows=13"), is(true));
  }

  @Test
  void whenHighlightingIsTrue_QueryContainsHighlightingParameters() {
    SolrParams solrParams;
    final SearchParams paramsWithHL;
    final SearchParams params = SearchParams.builder()
        .withPhrase("test")
        .withSearchFields(ArrayUtils.toArray(SearchField.SETTLEMENT))
        .build();

    solrParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(solrParams.toString().contains("hl=on"), is(false));
    assertThat(solrParams.toString().contains("hl."), is(false));

    paramsWithHL = SearchParams.builder()
        .withPhrase("test")
        .withHighlight(true)
        .withHighlightFields(ArrayUtils.toArray(SearchField.REPOSITORY))
        .withHighlightPhrase("test-2")
        .withSearchFields(ArrayUtils.toArray(SearchField.SETTLEMENT))
        .build();

    solrParams = SearchParams2SolrParamsConverter.convert(paramsWithHL);

    assertThat(solrParams.toString().contains("hl=on"), is(true));
    assertThat(solrParams.toString().contains("hl.fl=repository-search&"), is(true));
    assertThat(solrParams.toString().contains("hl.q=test-2&"), is(true));
    assertThat(solrParams.toString().contains("hl.highlightMultiTerm=true"), is(true));
    assertThat(solrParams.toString().contains("hl.snippets="), is(true));
    assertThat(solrParams.toString().contains("hl.maxAnalyzedChars=" + (Integer.MAX_VALUE - 1)), is(true));
    assertThat(solrParams.toString().contains("hl.mergeContiguous=true"), is(true));
    assertThat(solrParams.toString().contains("qf=settlement-search"), is(true));
    assertThat(solrParams.toString().contains("qf=repository-search"), is(true));
  }

  @Test
  void whenHighlightingIsTrueAndSearchFieldsContainGroupId_HighlightingFieldsDoNotContainGroupId() {
    SolrParams solrParams;
    final SearchParams paramsWithHL;
    final SearchParams params = SearchParams.builder()
        .withPhrase("test")
        .withSearchFields(ArrayUtils.toArray(SearchField.SETTLEMENT))
        .build();

    solrParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(solrParams.toString().contains("hl=on"), is(false));
    assertThat(solrParams.toString().contains("hl."), is(false));

    paramsWithHL = SearchParams.builder()
        .withPhrase("test")
        .withHighlight(true)
        .withHighlightFields(ArrayUtils.toArray(SearchField.REPOSITORY, SearchField.GROUP_ID))
        .withHighlightPhrase("test-2")
        .withSearchFields(ArrayUtils.toArray(SearchField.SETTLEMENT))
        .build();

    solrParams = SearchParams2SolrParamsConverter.convert(paramsWithHL);

    assertThat(solrParams.toString().contains("hl.fl=group-id-search"), is(false));
  }

  @Test
  void whenGroupingIsNecessary_QueryContainsGroupingParameters() {
    SearchParams params;
    params =  SearchParams.builder()
        .withGrouping(true)
        .withPhrase("test")
        .build();
    SolrParams solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("group=true"), is(true));
    assertThat(solrParams.toString().contains("group.field=group-id-search"), is(true));
    assertThat(solrParams.toString().contains("group.limit=100"), is(true));
    assertThat(solrParams.toString().contains("group.ngroups=true"), is(true));

    params = SearchParams.builder()
        .withPhrase("test")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("group="), is(false));
    assertThat(solrParams.toString().contains("group.field="), is(false));
    assertThat(solrParams.toString().contains("group.limit="), is(false));
    assertThat(solrParams.toString().contains("group.ngroups="), is(false));

    params = SearchParams.builder()
        .withPhrase("testid")
        .withGrouping(true)
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("group=true"), is(true));
    assertThat(solrParams.toString().contains("group.field=group-id-search"), is(true));
    assertThat(solrParams.toString().contains("group.limit=100"), is(true));
    assertThat(solrParams.toString().contains("group.ngroups=true"), is(true));
    
    params =SearchParams.builder()
        .withPhrase("testid")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
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
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("fq="), is(false));

    params = SearchParams.builder()
        .withPhrase("test")
        .withFilterQueries(Map.of("material-facet:paper", "material-facet"))
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
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
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("facet=true"), is(true));
    assertThat(solrParams.toString().contains("facet.excludeTerms=hsp:digitized"), is(true));
    assertThat(solrParams.toString().contains("facet.mincount=1"), is(true));
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

    solrParams = SearchParams2SolrParamsConverter.convert(params);
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
    solrParams = SearchParams2SolrParamsConverter.convert(params);

    /* check prio 1 field */
    assertThat(solrParams.toString().contains(SearchField.ID.getName() + "^" + SearchField.ID.getBoost()), is(true));

    /* check prio 2 field */
    assertThat(solrParams.toString()
        .contains(SearchField.SETTLEMENT.getName() + "^" + SearchField.SETTLEMENT.getBoost()), is(true));

    /* check prio 3 field */
    assertThat(solrParams.toString()
        .contains(SearchField.MATERIAL.getName() + "^" + SearchField.MATERIAL.getBoost()), is(true));

    /* check unboosted field */
    assertThat(!solrParams.toString().contains(SearchField.FULLTEXT.getName() + "^"), is(true));
  }

  @Test
  void whenValidSortPhraseIsProvided_QueryContainsSortParam() {
    SearchParams params;
    SolrParams solrParams;

    params = SearchParams.builder()
        .withPhrase("test")
        .withSortPhrase("orig-date-from-sort asc")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("sort=orig-date-from-sort+asc"), is(true));

    params = SearchParams.builder()
        .withPhrase("searching")
        .withSortPhrase("orig-date-to-sort desc")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
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
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("sort="), is(false));

    params = SearchParams.builder()
        .withPhrase("searching")
        .withSortPhrase("popularity desc").build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("sort="), is(false));
  }

  @Test
  void whenPhraseAndQueryAreEmpty_thenCorrectDefaultValueIsReturned() {
    final SearchParams params = SearchParams.builder().build();

    final SolrParams sParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(sParams.get("q"), is("*:*"));
  }

  @Test
  void whenPhraseIsQuotedAndSearchFieldsAreTextType_thenFieldsAreConvertedToExactFieldNames() {
    final SearchParams params = SearchParams.builder()
        .withPhrase("\"test\"")
        .withSearchFields(Arrays.array(SearchField.SETTLEMENT, SearchField.REPOSITORY))
        .build();

    final SolrParams sParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(sParams.getParams("qf"), arrayWithSize(2));
    assertThat(sParams.getParams("qf"), arrayContainingInAnyOrder("settlement-search-exact^100", "repository-search-exact^100"));
  }

  @Test
  void whenSearchPhraseIsQuoted_thenHighlightingQueryIsPerformedOnExactFieldNames() {
    final SearchParams params = SearchParams.builder()
        .withPhrase("\"Nürnberg\"")
        .withSearchFields(Arrays.array(SearchField.SETTLEMENT))
        .withHighlight(true)
        .withHighlightPhrase("\"Nürnberg\"")
        .withHighlightSnippetCount(1)
        .withHighlightFields(Arrays.array(SearchField.REPOSITORY))
        .build();

    final SolrParams resultParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(resultParams.getParams("qf"), arrayWithSize(2));
    // original field is still boosted, but highlighting doesn't mind and so do we
    assertThat(resultParams.getParams("qf"), arrayContainingInAnyOrder("settlement-search-exact^100", "repository-search-exact"));
  }

  @Test
  void whenNoQueryOperatorIsGiven_thenANDIsAdd() {
    final SearchParams params = SearchParams.builder().build();

    final SolrParams actualParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(actualParams.get("q.op"), is("AND"));
  }

  @Test
  void whenQueryOperatorIsOR_thenORisAdd() {
    final SearchParams params = SearchParams.builder()
        .withQueryOperator(SearchParams.QueryOperator.OR)
        .build();

    final SolrParams actualParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(actualParams.get("q.op"), is("OR"));
  }

  @Test
  void whenSearchingFuzzyOnAllFields_thenAllStemmedFieldsAreIncluded() {
    final SearchParams params = SearchParams.builder()
        .build();

    final SolrParams actualParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(List.of(actualParams.getParams("qf")), hasItems(SearchField.getNamesBoosted(SearchField.values(), false)));
    assertThat(List.of(actualParams.getParams("qf")), hasItems(SearchFieldStemmed.getNames(SearchField.values(), true)));
  }

  @Test
  void whenSearchingFuzzyOnUnstemmableField_thenNoStemmedFieldIsIncluded() {
    final SearchParams params = SearchParams.builder()
        .withSearchFields(ArrayUtils.toArray(SearchField.PERSON_AUTHOR))
        .build();

    final SolrParams actualParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(List.of(actualParams.getParams("qf")), contains(SearchField.PERSON_AUTHOR.getName()));
  }

  @Test
  void whenSearching_spellCheckQueryIsAdded() {
    final SearchParams params = SearchParams.builder()
        .withQuery("abc")
        .build();

    final SolrParams actualParams = SearchParams2SolrParamsConverter.convert(params);

    assertThat(actualParams.getParams("spellcheck.q"), arrayContaining("abc"));
  }
}
