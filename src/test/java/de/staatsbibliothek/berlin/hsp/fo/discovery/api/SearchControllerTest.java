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

package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.StringToSortPhraseConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.SearchService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SortField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.SearchServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.TestDataProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.support.FormattingConversionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SearchControllerTest extends AbstractRestControllerTest {

  private final SearchService searchService;

  private final HspConfig config;
  protected final List<String> facets;
  protected static final Map<String, String> DEFAULT_FILTER = new HashMap<>();

  static {
    DEFAULT_FILTER.put(
        "type-facet:(\"hsp:object\" OR \"hsp:description\" OR \"hsp:description_retro\")",
        "type-facet");
  }

  @Override
  public Object getControllerToTest() {
    return new SearchController(searchService, config);
  }

  @Override
  public FormattingConversionService addConverter(FormattingConversionService conversionService) {
    conversionService.addConverter(new StringToSortPhraseConverter());
    return super.addConverter(conversionService);
  }

  public SearchControllerTest(@Autowired HspConfig config) {
    this.config = config;
    /* use fixed list instead of config values, as the controller holds them in some kind of random order */
    this.facets = List.of("settlement-facet", "type-facet", "material-facet");
    this.searchService = mock(SearchServiceImpl.class);
  }

  @Test
  void WhenCalledWithFailingSearchTerm_thenEmptyResultIsReturned() throws Exception {
    // prepare response
    final Result<List<String>> mockedResult = new Result<>(List.of());

    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFilterQueries(DEFAULT_FILTER)
        .withFacets(this.facets)
        .withHighlightSnippetCount(this.config.getSnippetCount())
        .withPhrase("fail")
        .withRows(10)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withSpellcheck(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    Mockito.when(this.searchService.findHspObjectGroupIds(params))
        .thenReturn(mockedResult);

    // mock the request for completing the group
    final String query = SearchField.GROUP_ID.getName() + ":" + TestDataProvider.getTestData().getHspObject().getGroupId();
    final SearchParams qParams = SearchParams.builder()
        .withQuery(query)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .withStart(0)
        .build();
    final Result<List<HspObjectGroup>> groupResult = new Result<>(new ArrayList<>());
    Mockito.when(this.searchService.findHspObjectGroups(qParams)).thenReturn(groupResult);

    final String jsonResponse = jsonResponseBuilder.getJsonResult(groupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/search?q=fail&qf=id-search&start=0&rows=10"))
        .andExpect(content().json(jsonResponse))
        .andExpect(status().isOk());
  }

  @Test
  void WhenCalledWithHighlighting_thenResultContainsHighlightingInformation() throws Exception {
    // prepare response
    final NamedList<List<String>> highlightingInformation = new NamedList<>();
    final List<String> highlightingFragments = new ArrayList<>();
    highlightingFragments.add("fragment1");
    highlightingFragments.add("fragment2");
    highlightingFragments.add("fragment3");
    highlightingInformation.add("titel_1", highlightingFragments);

    final Result<List<String>> mockedResult = new Result<>(List.of(TestDataProvider.getTestData().getGroupId()), TestDataProvider.getMetadata());

    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFacets(this.facets)
        .withFilterQueries(DEFAULT_FILTER)
        .withHighlightSnippetCount(this.config.getSnippetCount())
        .withPhrase("success")
        .withRows(10)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withSpellcheck(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    // set the mocked response
    Mockito.when(this.searchService.findHspObjectGroupIds(params)).thenReturn(mockedResult);

    // mock the request for completing the group
    final String query = SearchField.GROUP_ID.getName() + ":("+ TestDataProvider.getTestData().getHspObject().getGroupId() + ")";
    final SearchParams qParams = SearchParams.builder()
        .withGrouping(true)
        .withHighlight(true)
        .withHighlightFields(params.getSearchFields())
        .withHighlightPhrase("success")
        .withHighlightSnippetCount(config.getSnippetCount())
        .withQuery(query)
        .withQueryOperator(SearchParams.QueryOperator.OR)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    final MetaData resultGroupMetaData = TestDataProvider.getMetadata();
    final HspObjectGroup hspObjectGroup = TestDataProvider.getTestData();

    resultGroupMetaData.setHighlighting(Map.of(hspObjectGroup.getGroupId(), Map.of(hspObjectGroup.getGroupId(), highlightingFragments)));
    final Result<List<HspObjectGroup>> mockedGroupResponse = new Result<>(List.of(hspObjectGroup), resultGroupMetaData);
    Mockito.when(this.searchService.findHspObjectGroups(qParams)).thenReturn(mockedGroupResponse);

    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResponse);

    // perform request and check expectations
    this.mockMvc.perform(get("/search?q=success&qf=id-search&start=0&rows=10"))
        .andExpect(content().json(jsonResponse))
        .andExpect(status().isOk());
  }

  @Test
  void WhenCalledWithFacetParam_thenResponseContainsFacetInformation() throws Exception {
    // prepare response
    final Map<String, Map<String, Long>> facets = new HashMap<>();
    final Map<String, Long> facet = Map.of("Leipzig", 2L, "Berlin", 5L, "Dessau", 1L);

    facets.put(FacetField.REPOSITORY.getName(), facet);
    // only checking the extraction of the facet information, so no need to deal with actual response
    // objects
    final Result<List<String>> mockedResult = new Result<>(List.of(TestDataProvider.getTestData().getGroupId()));
    final MetaData resultMetaData = MetaData.builder().withRows(10).build();
    resultMetaData.setFacets(facets);
    resultMetaData.setStats(Map.of());
    mockedResult.setMetadata(resultMetaData);

    // set the mocked response
    SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFacets(this.facets)
        .withFilterQueries(DEFAULT_FILTER)
        .withHighlightSnippetCount(this.config.getSnippetCount())
        .withPhrase("test")
        .withRows(10)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withSpellcheck(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    Mockito.when(this.searchService.findHspObjectGroupIds(params))
        .thenReturn(mockedResult);

    final SearchParams qParams = SearchParams.builder()
        .withGrouping(true)
        .withQuery(SearchField.GROUP_ID.getName() + ":("+ TestDataProvider.getTestData().getHspObject().getGroupId() + ")")
        .withQueryOperator(SearchParams.QueryOperator.OR)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .withStart(0)
        .build();

    final Result<List<HspObjectGroup>> mockedGroupResult = new Result<>(List.of(TestDataProvider.getTestData()), TestDataProvider.getMetadata());

    Mockito.when(this.searchService.findHspObjectGroups(qParams))
        .thenReturn(mockedGroupResult);
    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects?q=test&qf=id-search&hl=false"))
        .andExpect(content().json(jsonResponse))
        .andExpect(status().isOk());
  }

  //ToDo think about where numFound should come from at all
  // @Test
  void WhenCalledWithSucceedingSearchTerm_thenResultIsReturned_andNumFoundIsNotPartOfMetadata()
      throws Exception {
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(TestDataProvider.getTestData()), new MetaData());

    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFilterQueries(DEFAULT_FILTER)
        .withPhrase("success")
        .withRows(10)
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .build();

    // set the mocked result
    Mockito.when(this.searchService.findHspObjectGroups(params))
        .thenReturn(mockedResult);

    // mock the request for completing the group
    final String query = String.format(SearchField.GROUP_ID.getName() + ":(%s)",
        TestDataProvider.getTestData().getHspObject().getGroupId());
    final SearchParams qParams = SearchParams.builder()
        .withGrouping(true)
        .withQuery(query)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(SearchField.values())
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withStart(0)
        .build();

    final Result<List<HspObjectGroup>> mockedGroupResult = new Result<>(List.of(TestDataProvider.getTestData()));

    Mockito.when(this.searchService.findHspObjectGroups(qParams))
        .thenReturn(mockedGroupResult);

    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects?q=success&start=0&rows=10&hl=false"))
        .andExpect(content().json(jsonResponse))
        .andExpect(jsonPath("$.metadata.numFound").doesNotExist())
        .andExpect(status().isOk());
  }

  @Test
  void whenNoResultIsFound_thenMetadataIsContainedNonetheless() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFilterQueries(DEFAULT_FILTER)
        .withFacets(this.facets)
        .withHighlightSnippetCount(this.config.getSnippetCount())
        .withPhrase("not-existing")
        .withRows(10)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withSpellcheck(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    // set the mocked result
    Mockito.when(this.searchService.findHspObjectGroupIds(params))
        .thenReturn(new Result<>(List.of(), MetaData.builder()
            .withNumFound(0)
            .withRows(10)
            .build()));

    // perform request and check expectations
    this.mockMvc.perform(get("/search?q=not-existing&qf=id-search&start=0&rows=10&hl=false"))
        .andExpect(jsonPath("$.metadata.numFound").value(0))
        .andExpect(jsonPath("$.metadata.start").value(0))
        .andExpect(jsonPath("$.metadata.rows").value(10)) //sic
        .andExpect(status().isOk());

  }
}
