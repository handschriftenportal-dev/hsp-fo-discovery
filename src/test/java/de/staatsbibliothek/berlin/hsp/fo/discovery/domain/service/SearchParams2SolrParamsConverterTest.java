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
package de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.Test;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SearchField;
/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class SearchParams2SolrParamsConverterTest {
  @Test
  void whenStartAndRowsAreGiven_QueryContainsStartAndRows() {
    final QuerySearchParams params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
        .withRows(13)
        .withStart(1)
        .build();
    final SolrParams solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("start=1&rows=13"), is(true));
  }

  @Test
  void whenHighlightingIsTrue_QueryContainsHighlightingParameters() {
    SolrParams solrParams;
    final QuerySearchParams paramsWithHL;
    final QuerySearchParams params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("hl=on"), is(false));
    assertThat(solrParams.toString().contains("hl."), is(false));

    paramsWithHL = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("test")
        .withHighlight(true)
        .withHighlightQuery("field:test")
        .withHighlightQueryParser(QueryParser.EDISMAX)
        .withHighlightFields("field")
        .withSearchFields("field-search")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(paramsWithHL);
    assertThat(solrParams.toString().contains("hl=on"), is(true));
    assertThat(solrParams.toString().contains("hl.fl=field&"), is(true));
    assertThat(solrParams.toString().contains("hl.highlightMultiTerm=true"), is(true));
    assertThat(solrParams.toString().contains("hl.snippets="), is(true));
    assertThat(solrParams.toString().contains("hl.maxAnalyzedChars=" + Integer.MAX_VALUE), is(true));
    assertThat(solrParams.toString().contains("hl.mergeContiguous=true"), is(true));
    assertThat(solrParams.toString().contains("qf=field-search"), is(true));
  }

  @Test
  void whenGroupingIsNecessary_QueryContainsGroupingParameters() {
    AbstractParams<?> params;
    params = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withQueryOrSearchTerm("field:test")
        .build();
    SolrParams solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("group=true"), is(true));
    assertThat(solrParams.toString().contains("group.field=group-id-search"), is(true));
    assertThat(solrParams.toString().contains("group.limit=100"), is(true));
    assertThat(solrParams.toString().contains("group.ngroups=true"), is(true));

    params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("group="), is(false));
    assertThat(solrParams.toString().contains("group.field="), is(false));
    assertThat(solrParams.toString().contains("group.limit="), is(false));
    assertThat(solrParams.toString().contains("group.ngroups="), is(false));

    params = new TermSearchParams.Builder()
        .withQueryOrSearchTerm("testid")
        .withGrouping(true)
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("group=true"), is(true));
    assertThat(solrParams.toString().contains("group.field=group-id-search"), is(true));
    assertThat(solrParams.toString().contains("group.limit=100"), is(true));
    assertThat(solrParams.toString().contains("group.ngroups=true"), is(true));
    
    params = new TermSearchParams.Builder()
        .withQueryOrSearchTerm("testid")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("group="), is(false));
    assertThat(solrParams.toString().contains("group.field="), is(false));
    assertThat(solrParams.toString().contains("group.limit="), is(false));
    assertThat(solrParams.toString().contains("group.ngroups="), is(false));
  }

  @Test
  void whenFilterQueriesAreProvided_QueryContainsFilterQueriesWithExclusionTags() {
    QuerySearchParams params;
    SolrParams solrParams;

    params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("fq="), is(false));

    params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
        .withFilterQueries(Map.of("material-facet:paper", "material-facet"))
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("fq={!tag%3Dsolr_fq_material-facet}material-facet:paper"), is(true));
  }

  @Test
  void whenFacetsAreConfigured_QueryContainsTaggedFacets() {
    AbstractParams<?> params;
    SolrParams solrParams;

    params = new QuerySearchParams.Builder()
        .withFacets(List.of("material-facet", "settlement-facet"))
        .withQueryOrSearchTerm("field:test")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("facet=true"), is(true));
    assertThat(solrParams.toString().contains("facet.excludeTerms=hsp:digitized"), is(true));
    assertThat(solrParams.toString().contains("facet.mincount=1"), is(true));
    assertThat(solrParams.toString().contains("facet.missing=true"), is(true));
    assertThat(solrParams.toString().contains("facet.field={!ex%3Dsolr_fq_material-facet}material-facet"), is(true));
    assertThat(solrParams.toString().contains("facet.field={!ex%3Dsolr_fq_settlement-facet}settlement-facet"), is(true));

    params = new TermSearchParams.Builder()
        .withFacets(List.of("material-facet", "settlement-facet"))
        .withQueryOrSearchTerm("searching")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("facet=true"), is(true));
    assertThat(solrParams.toString().contains("facet.excludeTerms=hsp:digitized"), is(true));
    assertThat(solrParams.toString().contains("facet.mincount=1"), is(true));
    assertThat(solrParams.toString().contains("facet.missing=true"), is(true));
    assertThat(solrParams.toString().contains("facet.field={!ex%3Dsolr_fq_material-facet}material-facet"), is(true));
    assertThat(solrParams.toString().contains("facet.field={!ex%3Dsolr_fq_settlement-facet}settlement-facet"), is(true));
  }

  @Test
  void whenStatsAreConfigured_QueryContainsTaggedAndConfiguredStats() {
    AbstractParams<?> params;
    SolrParams solrParams;
    params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
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
    final TermSearchParams params = new TermSearchParams.Builder()
        .withQueryOrSearchTerm("searching")
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
    AbstractParams<?> params;
    SolrParams solrParams;

    params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
        .withSortPhrase("orig-date-from-sort asc")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("sort=orig-date-from-sort+asc"), is(true));

    params = new TermSearchParams.Builder()
        .withQueryOrSearchTerm("searching")
        .withSortPhrase("orig-date-to-sort desc")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("sort=orig-date-to-sort+desc"), is(true));
  }

  @Test
  void whenInvalidSortPhraseIsProvided_QueryContainsNoSortParam() {
    AbstractParams<?> params;
    SolrParams solrParams;
    
    params = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm("field:test")
        .withSortPhrase("price asc")
        .build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("sort="), is(false));

    params = new TermSearchParams.Builder()
        .withQueryOrSearchTerm("searching")
        .withSortPhrase("popularity desc").build();
    solrParams = SearchParams2SolrParamsConverter.convert(params);
    assertThat(solrParams.toString().contains("sort="), is(false));
  }
}
