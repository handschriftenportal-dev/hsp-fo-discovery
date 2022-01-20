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
package de.staatsbibliothek.berlin.hsp.fo.discovery.ui;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.CustomizedResponseEntityExceptionHandler;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.QueryParser;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.QuerySearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.TermSearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SortField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.infrastructure.persistence.solr.SolrConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.HspDescriptionForFulltextTests;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.HspObjectForTeiDocTests;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.JsonResponseBuilder;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.converter.StringToSortPhraseConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.Result;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@SpringBootTest(classes = {
    HspObjectControllerTest.TestConfiguration.class,
    HspObjectControllerTest.SolrTestConfiguration.class
})
class HspObjectControllerTest {

  private MockMvc mockMvc;

  private HspObjectGroupService solrService;

  private HspObjectController hspObjectControllerController;

  private HspObjectGroup hspObjectGroup;

  private HspDescriptionForFulltextTests fulltextDescription;
  
  private HspObjectForTeiDocTests teiDocumentObject;
  
  protected JsonResponseBuilder jsonResponseBuilder;
  
  public static final Map<String, String> DEFAULT_FILTER = new HashMap<>();
  
  private MetaData metaData;
  
  private HspConfig hspConfig;
  
  private List<String> facets;

  static {
    DEFAULT_FILTER.put("type-facet:(\"hsp:object\" OR \"hsp:description\")", "type-facet");
  }
  
  @EnableConfigurationProperties(HspConfig.class)
  public static class TestConfiguration {}

  @EnableConfigurationProperties(SolrConfig.class)
  public static class SolrTestConfiguration {}

  @Autowired
  public HspObjectControllerTest(final HspConfig hspConfig) {
    
    jsonResponseBuilder = new JsonResponseBuilder();
    
    FormattingConversionService conversionService = new FormattingConversionService();
    conversionService.addConverter(new StringToSortPhraseConverter());
    
    this.hspConfig = hspConfig;
    
    /* use fixed list instead of config values, as the controller holds them in some kind of random order */
    this.facets = List.of("settlement-facet", "type-facet", "material-facet");
    this.solrService = Mockito.mock(HspObjectGroupService.class);
    this.hspObjectControllerController = new HspObjectController(this.solrService, hspConfig);
    this.mockMvc = MockMvcBuilders
        .standaloneSetup(hspObjectControllerController, new CustomizedResponseEntityExceptionHandler())
        .setConversionService(conversionService)
        .build();

    this.hspObjectGroup = new HspObjectGroup();
    final HspObject hspObject = new HspObject();
    hspObject.setId("hspObjectId123");
    hspObject.setGroupId("hspObjectId123");
    hspObject.setRepository("Wolfenbüttel");
    hspObject.setSettlement("Wolfenbüttel");
    hspObject.setIdno("hspObjectIdno123");
    hspObject.setType("hsp:object");
    hspObjectGroup.setHspObject(hspObject);

    final HspDescription hspDescription = new HspDescription();
    hspDescription.setId("hspDescription123");
    hspDescription.setGroupId(hspObject.getGroupId());
    hspDescription.setRepository(hspObject.getRepository());
    hspDescription.setSettlement(hspObject.getSettlement());
    hspDescription.setIdno("hspDescriptionIdno123");
    hspDescription.setType("hsp:description");
    hspDescription.setRendering("content");
    hspObjectGroup.setHspDescriptions(Arrays.asList(hspDescription));

    this.fulltextDescription = new HspDescriptionForFulltextTests();
    fulltextDescription.setId(hspDescription.getId());
    fulltextDescription.setGroupId(hspObject.getGroupId());
    fulltextDescription.setRepository(hspObject.getRepository());
    fulltextDescription.setSettlement(hspObject.getSettlement());
    fulltextDescription.setIdno(hspDescription.getIdno());
    fulltextDescription.setType(hspDescription.getType());
    fulltextDescription.setRendering("content");

    this.teiDocumentObject = new HspObjectForTeiDocTests();
    teiDocumentObject.setId(hspObject.getId());
    teiDocumentObject.setGroupId(hspObject.getGroupId());
    teiDocumentObject.setRepository(hspObject.getRepository());
    teiDocumentObject.setSettlement(hspObject.getSettlement());
    teiDocumentObject.setIdno(hspObject.getIdno());
    teiDocumentObject.setType(hspObject.getType());
    teiDocumentObject.setTeiDocument("tei-content");
    
    this.metaData = new MetaData.Builder()
        .withHighlighting(Map.of())
        .withFacets(Map.of())
        .withRows(10)
        .withStats(Map.of())
        .build();
  }

  @Test
  void whenCalledWithoutId_thenNotFoundIsReturned() throws Exception {
    // prepare response
    final Result<List<HspObjectGroup>> mockedResult = new Result<>();

    final TermSearchParams params = new TermSearchParams.Builder()
        .withGrouping(true)
        .withQueryOrSearchTerm(null)
        .withSearchFields(SearchField.ID.getName())
        .build();

    // set the mocked response
    Mockito.when(this.solrService.findHspObjectGroups(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/")).andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenCalledNotExistingId_thenNotFoundIsReturned() throws Exception {
    // prepare response
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(), this.metaData);

    final QuerySearchParams params = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withRows(Integer.MAX_VALUE)
        .withQueryOrSearchTerm(SearchField.GROUP_ID.getName() + ":" + "def")
        .withSearchFields(SearchField.GROUP_ID.getName())
        .withStart(0)
        .build();

    // set the mocked response
    Mockito.when(this.solrService.findHspObjectGroups(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/def"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenCalledWithId_thenHOGIsReturned() throws Exception {
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(this.hspObjectGroup), null);
    
    final QuerySearchParams params = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withRows(Integer.MAX_VALUE)
        .withQueryOrSearchTerm(SearchField.GROUP_ID.getName() + ":" + "existingId")
        .withSearchFields(SearchField.GROUP_ID.getName())
        .withStart(0)
        .build();
    // set the mocked result
    Mockito.when(this.solrService.findHspObjectGroups(params)).thenReturn(mockedResult);

    // build expected json
    final String expectedJson = jsonResponseBuilder.getJsonResult(new Result<>(this.hspObjectGroup));

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/existingId"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  //ToDo think about where numFound should came from at all
  // @Test
  void WhenCalledWithSucceedingSearchTerm_thenResultIsReturned_andNumFoundIsNotPartOfMetadata()
      throws Exception {
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(this.hspObjectGroup), new MetaData());

    final TermSearchParams params = new TermSearchParams.Builder()
        .withCollapse(true)
        .withFilterQueries(DEFAULT_FILTER)
        .withQueryOrSearchTerm("success")
        .withRows(10)
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .build();
    
    // set the mocked result
    Mockito.when(this.solrService.findHspObjectGroups(params))
        .thenReturn(mockedResult);

    // mock the request for completing the group
    final String query = String.format(SearchField.GROUP_ID.getName() + ":(%s)",
        this.hspObjectGroup.getHspObject().getGroupId());
    final QuerySearchParams qParams = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withQueryOrSearchTerm(query)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(SearchField.getNames())
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withStart(0)
        .build();

    final Result<List<HspObjectGroup>> mockedGroupResult = new Result<>(List.of(this.hspObjectGroup));
    
    Mockito.when(this.solrService.findHspObjectGroups(qParams))
        .thenReturn(mockedGroupResult);

    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects?q=success&start=0&rows=10&hl=false"))
        .andExpect(content().json(jsonResponse))
        .andExpect(jsonPath("$.metadata.numFound").doesNotExist())
        .andExpect(status().isOk());
  }
//
  @Test
  void WhenCalledWithFailingSearchTerm_thenEmptyResultIsReturned() throws Exception {
    // prepare response
    final Result<List<String>> mockedResult = new Result<>(List.of());
    
    
    // set the mocked response
    final TermSearchParams params = new TermSearchParams.Builder()
        .withCollapse(true)
        .withFilterQueries(DEFAULT_FILTER)
        .withFacets(this.facets)
        .withQueryOrSearchTerm("fail")
        .withRows(10)
        .withHightlightSnippetCount(this.hspConfig.getSnippetCount())
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withStart(0)
        .withStats(this.hspConfig.getStats())
        .build();

    Mockito.when(this.solrService.findHspObjectGroupIds(params))
        .thenReturn(mockedResult);

    // mock the request for completing the group
    final String query = SearchField.GROUP_ID.getName() + ":" + this.hspObjectGroup.getHspObject().getGroupId();
    final QuerySearchParams qParams = new QuerySearchParams.Builder()
        .withQueryOrSearchTerm(query)
        .withRows(Integer.MAX_VALUE)
        .withStart(0)
        .build();
    final Result<List<HspObjectGroup>> groupResult = new Result<>(new ArrayList<>());
    Mockito.when(this.solrService.findHspObjectGroups(qParams)).thenReturn(groupResult);

    final String jsonResponse = jsonResponseBuilder.getJsonResult(groupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects?q=fail&start=0&rows=10"))
        .andExpect(content().json(jsonResponse))
        .andExpect(status().isOk());
  }

  @Test
  void WhenCalledWithHighlighting_thenResultContainsHighlightingInformation() throws Exception {
    // prepare response
    final NamedList<List<String>> higlightingInformation = new NamedList<>();
    final List<String> highlightingFragments = new ArrayList<String>();
    highlightingFragments.add("fragment1");
    highlightingFragments.add("fragment2");
    highlightingFragments.add("fragment3");
    higlightingInformation.add("titel_1", highlightingFragments);

    final Result<List<String>> mockedResult = new Result<>(List.of(this.hspObjectGroup.getGroupId()), this.metaData);

    final TermSearchParams params = new TermSearchParams.Builder()
        .withCollapse(true)
        .withFacets(this.facets)
        .withFilterQueries(DEFAULT_FILTER)
        .withHightlightSnippetCount(this.hspConfig.getSnippetCount())
        .withQueryOrSearchTerm("success")
        .withRows(10)
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withStart(0)
        .withStats(this.hspConfig.getStats())
        .build();

    // set the mocked response
    Mockito.when(this.solrService.findHspObjectGroupIds(params)).thenReturn(mockedResult);

    // mock the request for completing the group
    final String query = SearchField.GROUP_ID.getName() + ":("+ this.hspObjectGroup.getHspObject().getGroupId() + ")";
    final QuerySearchParams qParams = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withHighlight(true)
        .withHighlightFields(params.getSearchFields())
        .withHighlightQuery("success")
        .withHighlightQueryParser(QueryParser.EDISMAX)
        .withHightlightSnippetCount(hspConfig.getSnippetCount())
        .withQueryOrSearchTerm(query)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(SearchField.getNames())
        .build();

    final MetaData resultGroupMetaData = SerializationUtils.clone(this.metaData);
    resultGroupMetaData.setHighlighting(Map.of(this.hspObjectGroup.getGroupId(), Map.of(this.hspObjectGroup.getGroupId(), highlightingFragments)));
    resultGroupMetaData.setStats(metaData.getStats());
    resultGroupMetaData.setFacets(metaData.getFacets());
    final Result<List<HspObjectGroup>> mockedGroupResponse = new Result<>(List.of(this.hspObjectGroup), resultGroupMetaData);
    Mockito.when(this.solrService.findHspObjectGroups(qParams)).thenReturn(mockedGroupResponse);

    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResponse);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects?q=success&start=0&rows=10"))
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
    final Result<List<String>> mockedResult = new Result<>(List.of(this.hspObjectGroup.getGroupId()));
    final MetaData resultMetaData = new MetaData.Builder().withRows(10).build();
    resultMetaData.setFacets(facets);
    resultMetaData.setStats(Map.of());
    mockedResult.setMetadata(resultMetaData);

    final List<FacetField> fl = new ArrayList<>();
    fl.add(FacetField.REPOSITORY);
    fl.add(FacetField.SETTLEMENT);

    // set the mocked response
    TermSearchParams params = new TermSearchParams.Builder()
        .withCollapse(true)
        .withFacets(this.facets)
        .withFilterQueries(DEFAULT_FILTER)
        .withQueryOrSearchTerm("test")
        .withRows(10)
        .withHightlightSnippetCount(this.hspConfig.getSnippetCount())
        .withSortPhrase(SortField.SCORE_DESC.getSortPhrase())
        .withStart(0)
        .withStats(this.hspConfig.getStats())
        .build();

    Mockito.when(this.solrService.findHspObjectGroupIds(params))
        .thenReturn(mockedResult);

    final QuerySearchParams qParams = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withQueryOrSearchTerm(SearchField.GROUP_ID.getName() + ":("+ this.hspObjectGroup.getHspObject().getGroupId() + ")")
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(SearchField.getNames())
        .withStart(0)
        .build();
    
//    .withGrouping(true)
//    .withQueryOrSearchTerm(SearchField.GROUP_ID.getName() + ":(" + String.join(" ", groupIds) + ")")
//    .withRows(Integer.MAX_VALUE)
//    .withSearchFields(sourceParams.getSearchFields())
//    .withStart(0)
//    .build();
    
    final Result<List<HspObjectGroup>> mockedGroupResult = new Result<>(List.of(this.hspObjectGroup), this.metaData);
    
    Mockito.when(this.solrService.findHspObjectGroups(qParams))
    .thenReturn(mockedGroupResult);
    
    // build expected json
    final String jsonResponse = jsonResponseBuilder.getJsonResult(mockedGroupResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects?q=test&hl=false"))
        .andExpect(content().json(jsonResponse))
        .andExpect(status().isOk());
  }

  @Test
  void whenFulltextIsCalledWithoutId_thenNotFoundIsReturned() throws Exception {
    final TermSearchParams params = new TermSearchParams.Builder()
        .build();
    
    // prepare result
    final List<String> mockedResponse = List.of();

    // set the mocked result
    Mockito.when(this.solrService.findHspDescriptionFulltext(params)).thenReturn(mockedResponse);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspfulltext/")).andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void whenFulltextIsCalledNotExistingId_thenNotFoundIsReturned() throws Exception {
    final TermSearchParams params = new TermSearchParams.Builder()
        .withGrouping(true)
        .withQueryOrSearchTerm(String.format("\"%s\"", "def"))
        .withSearchFields(SearchField.ID.getName())
        .build();

    // prepare result
    final List<String> result = List.of();

    // set the mocked result
    Mockito.when(this.solrService.findHspDescriptionFulltext(params)).thenReturn(result);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspfulltext/def"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenFulltextIdIsNotUnique_thenInternalServerErrorIsReturned() throws Exception {
    final TermSearchParams params = new TermSearchParams.Builder()
        .withRows(2)
        .withQueryOrSearchTerm(String.format("\"%s\"", "nonUnique"))
        .withSearchFields(SearchField.ID.getName())
        .build();
    
    // prepare response
    final List<String> mockedResult = List.of("a", "b");

    // set the mocked response
    Mockito.when(this.solrService.findHspDescriptionFulltext(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspfulltext/nonUnique"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void whenFulltextIsCalledWithId_thenFulltextIsReturned() throws Exception {
    final TermSearchParams params = new TermSearchParams.Builder()
        .withRows(2)
        .withQueryOrSearchTerm(String.format("\"%s\"", "existingId"))
        .withSearchFields(SearchField.ID.getName())
        .build();
    
    // prepare result
    final List<String> mockedResult = List.of("content");

    // set the mocked response
    Mockito.when(this.solrService.findHspDescriptionFulltext(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspfulltext/existingId"))
        .andExpect(status().isOk())
        .andExpect(content().string("content"));
  }

  @Test
  void whenTeiDocumentIsCalledWithoutId_thenNotFoundIsReturned() throws Exception {
    // perform request and check expectations
    this.mockMvc.perform(get("/tei/")).andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenTeiDocumentIsCalledNotExistingId_thenNotFoundIsReturned() throws Exception {
    final TermSearchParams params = new TermSearchParams.Builder()
        .withGrouping(true)
        .withQueryOrSearchTerm(String.format("\"%s\"", "def"))
        .withSearchFields(SearchField.ID.getName())
        .build();

    // prepare result
    final List<String> mockedResult = List.of();

    // set the mocked result
    Mockito.when(this.solrService.findHspTEIDocuments(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/def"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenTeiDocumentIdIsNotUnique_thenInternalServerErrorIsReturned() throws Exception {
    final TermSearchParams params = new TermSearchParams.Builder()
        .withRows(2)
        .withQueryOrSearchTerm(String.format("\"%s\"", "nonUnique"))
        .withSearchFields(SearchField.ID.getName())
        .build();

    // prepare result
    final List<String> mockedResult = List.of("a", "b");

    // set the mocked response
    Mockito.when(this.solrService.findHspTEIDocuments(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/nonUnique"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void whenTeiDocumentIsCalledWithId_thenTeiDocumentIsReturned() throws Exception {
    final TermSearchParams params = new TermSearchParams.Builder()
        .withRows(2)
        .withQueryOrSearchTerm(String.format("\"%s\"", "teiId"))
        .withSearchFields(SearchField.ID.getName())
        .build();

    // prepare result
    final List<String> mockedResult = List.of("tei-content");

    // set the mocked result
    Mockito.when(this.solrService.findHspTEIDocuments(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/teiId"))
        .andExpect(status().isOk())
        .andExpect(content().string("tei-content"));
  }

  @AfterEach
  public void onTearDown() {
    Mockito.reset(this.solrService);
  }
}