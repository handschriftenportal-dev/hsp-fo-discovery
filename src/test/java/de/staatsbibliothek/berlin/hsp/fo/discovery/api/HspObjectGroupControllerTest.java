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
package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.StringToSortPhraseConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.HspObjectGroupServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.TestDataProvider;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SortField;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HspObjectGroupControllerTest extends AbstractRestControllerTest {

  private final HspConfig config;
  private final HighlightConfig highlightConfig;
  protected final List<String> facets;
  protected static final Map<String, String> DEFAULT_FILTER = new HashMap<>();
  public HspObjectGroupService objectGroupService;

  static {
    DEFAULT_FILTER.put(
        "type-facet:(\"hsp:object\" OR \"hsp:description\" OR \"hsp:description_retro\")",
        "type-facet");
  }

  public HspObjectGroupControllerTest(@Autowired HspConfig config, @Autowired HighlightConfig highlightConfig) {
    this.config = config;
    this.highlightConfig = highlightConfig;
    /* use fixed list instead of config values, as the controller holds them in some kind of random order */
    this.facets = config.getDefaultFacets().stream().sorted().toList();
    this.objectGroupService = Mockito.mock(HspObjectGroupServiceImpl.class);
    Mockito.when(this.objectGroupService.getTypeFilter()).thenReturn(Map.of(FacetField.TYPE.getName(), List.of(HspType.HSP_OBJECT.getValue(), HspType.HSP_DESCRIPTION.getValue(), HspType.HSP_DESCRIPTION_RETRO.getValue())));
  }

  @Override
  public Object getControllerToTest() {
    return new HspObjectGroupController(this.objectGroupService, this.config, this.highlightConfig);
  }

  @Override
  public FormattingConversionService addConverter(FormattingConversionService conversionService) {
    conversionService.addConverter(new StringToSortPhraseConverter());
    return super.addConverter(conversionService);
  }

  @Test
  void whenCalledNotExistingId_thenNotFoundIsReturned() throws Exception {
    // prepare response
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(), TestDataProvider.getMetadata());

    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withRows(2)
        .withPhrase("def")
        .withSearchFields(List.of("id-search"))
        .build();

    // set the mocked response
    Mockito.when(this.objectGroupService.find(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/def"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenCalledWithId_thenHOGIsReturned() throws Exception {
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(TestDataProvider.getTestData()), null);
    
    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withRows(2)
        .withPhrase("existingId")
        .withSearchFields(List.of("id-search"))
        .build();
    // set the mocked result
    Mockito.when(this.objectGroupService.find(params)).thenReturn(mockedResult);

    // build expected json
    final String expectedJson = jsonResponseBuilder.getJson(new Result<>(TestDataProvider.getTestData()));

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/existingId"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andReturn();
  }

  @Test
  void WhenCalledWithFailingSearchTerm_thenEmptyResultIsReturned() throws Exception {
    // prepare response
    final Result<List<String>> mockedResult = new Result<>(List.of());

    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFilterQueries(DEFAULT_FILTER)
        .withFacets(this.facets)
        .withHighlight(true)
        .withPhrase("fail")
        .withRows(10)
        .withSearchFields(List.of("id-search"))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withUseSpellCorrection(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    Mockito.when(this.objectGroupService.findHspObjectGroupIds(params))
        .thenReturn(mockedResult);

    // mock the request for completing the group
    final Result<List<HspObjectGroup>> groupResult = new Result<>(new ArrayList<>());
    Mockito.when(this.objectGroupService.find(params)).thenReturn(groupResult);

    final String jsonResponse = jsonResponseBuilder.getJsonResult(groupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/search?q=fail&qf=id-search&start=0&rows=10"))
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
        .withHighlight(true)
        .withPhrase("success")
        .withRows(10)
        .withSearchFields(List.of("id-search"))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withUseSpellCorrection(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    // set the mocked response
    Mockito.when(this.objectGroupService.findHspObjectGroupIds(params)).thenReturn(mockedResult);

    final MetaData resultGroupMetaData = TestDataProvider.getMetadata();
    final HspObjectGroup hspObjectGroup = TestDataProvider.getTestData();

    resultGroupMetaData.setHighlighting(Map.of(hspObjectGroup.getGroupId(), Map.of(hspObjectGroup.getGroupId(), highlightingFragments)));
    final Result<List<HspObjectGroup>> mockedGroupResponse = new Result<>(List.of(hspObjectGroup), resultGroupMetaData);
    Mockito.when(this.objectGroupService.find(params)).thenReturn(mockedGroupResponse);

    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResponse);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/search?q=success&qf=id-search&start=0&rows=10"))
        .andDo(MockMvcResultHandlers.print())
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
        .withPhrase("test")
        .withRows(10)
        .withSearchFields(List.of("id-search"))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withUseSpellCorrection(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    Mockito.when(this.objectGroupService.findHspObjectGroupIds(params))
        .thenReturn(mockedResult);

    Mockito.when(this.objectGroupService.getTypeFilter()).thenReturn(Map.of("type-facet", List.of("hsp:object", "hsp:description", "hsp:description_retro")));

    final Result<List<HspObjectGroup>> mockedGroupResult = new Result<>(List.of(TestDataProvider.getTestData()), TestDataProvider.getMetadata());

    Mockito.when(this.objectGroupService.find(params))
        .thenReturn(mockedGroupResult);
    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/search?q=test&qf=id-search&hl=false"))
        .andExpect(content().json(jsonResponse))
        .andExpect(status().isOk());
  }

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
    Mockito.when(this.objectGroupService.find(params))
        .thenReturn(mockedResult);

    // mock the request for completing the group
    final String query = String.format("group-id-search" + ":(%s)",
        TestDataProvider.getTestData().getHspObject().getGroupId());
    final SearchParams qParams = SearchParams.builder()
        .withGrouping(true)
        .withQuery(query)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(fieldProvider.getFieldNames())
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withStart(0)
        .build();

    final Result<List<HspObjectGroup>> mockedGroupResult = new Result<>(List.of(TestDataProvider.getTestData()));

    Mockito.when(this.objectGroupService.find(qParams))
        .thenReturn(mockedGroupResult);

    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/search?q=success&start=0&rows=10&hl=false"))
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
        .withPhrase("not-existing")
        .withRows(10)
        .withSearchFields(List.of("id-search"))
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withUseSpellCorrection(true)
        .withStart(0)
        .withStats(this.config.getStats())
        .build();

    // set the mocked result
    Mockito.when(this.objectGroupService.find(params))
        .thenReturn(new Result<>(List.of(), MetaData.builder()
            .withNumFound(0)
            .withRows(10)
            .build()));

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/search?q=not-existing&qf=id-search&start=0&rows=10&hl=false"))
        .andExpect(jsonPath("$.metadata.numFound").value(0))
        .andExpect(jsonPath("$.metadata.start").value(0))
        .andExpect(jsonPath("$.metadata.rows").value(10)) //sic
        .andExpect(status().isOk());
  }

  @AfterEach
  public void onTearDown() {
    Mockito.reset(this.objectGroupService);
  }
}