package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.StringToSortPhraseConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SortField;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.format.support.FormattingConversionService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HspCatalogControllerTest extends AbstractRestControllerTest {
  @MockBean
  private BaseService<HspCatalog> mockedService;

  private final HighlightConfig highlightConfig;

  @Autowired
  public HspCatalogControllerTest(final HspConfig config, final HighlightConfig highlightConfig) {
    this.config = config;
    this.highlightConfig = highlightConfig;
  }

  @Override
  public Object getControllerToTest() {
    return new HspCatalogController(mockedService, config, highlightConfig);
  }

  @Override
  public FormattingConversionService addConverter(FormattingConversionService conversionService) {
    conversionService.addConverter(new StringToSortPhraseConverter());
    return super.addConverter(conversionService);
  }


  @Test
  void givenInvalidId_whenGetByIdIsCalled_thenNotFoundIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withPhrase("invalid-id")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    Mockito.when(this.mockedService.find(params))
        .thenReturn(new Result<>(Collections.emptyList()));

    this.mockMvc.perform(get("/catalog/invalid-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  void givenValidId_whenGetByIdIsCalled_thenCatalogIsReturned() throws Exception {
    final HspCatalog mockedCatalog = new HspCatalog("valid-id", "hsp:catalog");
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withPhrase("valid-id")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    Mockito.when(this.mockedService.find(params))
        .thenReturn(new Result<>(List.of(mockedCatalog)));

    this.mockMvc.perform(get("/catalogs/valid-id"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockedCatalog)));
  }

  @Test
  void whenGetAllIsCalled_thenAllCatalogsAreReturned() throws Exception {
    final HspCatalog mockedCatalog01 = new HspCatalog("valid-id-01", "hsp:catalog");
    final HspCatalog mockedCatalog02 = new HspCatalog("valid-id-02", "hsp:catalog");
    final HspCatalog mockedCatalog03 = new HspCatalog("valid-id-03", "hsp:catalog");
    final SortField sort = SortField.PUBLISH_YEAR_DESC;

    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withSortPhrase(sort.getSortPhrase())
        .withRows(10)
        .withStart(0)
        .build();

    final Result<List<HspCatalog>> mockResult = new Result<>(List.of(mockedCatalog01, mockedCatalog02, mockedCatalog03));

    Mockito.when(this.mockedService.find(params))
        .thenReturn(mockResult);

    this.mockMvc.perform(get("/catalogs"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockResult)));
  }

  @Test
  void givenValidFacets_whenSearchIsCalled_thenAllFacetsArePassed() throws Exception {
    ArgumentCaptor<BaseService.SearchParams> searchParamsCaptor = ArgumentCaptor.forClass(BaseService.SearchParams.class);
    this.mockMvc.perform(get("/catalogs/search")
        .param("ff", "catalog-author-facet" )
        .param("ff", "catalog-publisher-facet")
        .param("q", "*")
        .param("qf", "fulltext-search"));
    verify(this.mockedService).find(searchParamsCaptor.capture());

    SearchParams captuedSearchParams = searchParamsCaptor.getValue();
    assertThat(captuedSearchParams.getFacets(), hasItems("catalog-author-facet", "catalog-publisher-facet"));
  }

  @Test
  void givenInValidFacets_whenSearchIsCalled_thenInvalidsFacetsAreRemoved() throws Exception {
    ArgumentCaptor<BaseService.SearchParams> searchParamsCaptor = ArgumentCaptor.forClass(BaseService.SearchParams.class);
    this.mockMvc.perform(get("/catalogs/search")
        .param("ff", "catalog-author-facet" )
        .param("ff", "catalog-publisher-facet")
        .param("ff", "invalid-facet")
        .param("q", "*")
        .param("qf", "fulltext-search"));
    verify(this.mockedService).find(searchParamsCaptor.capture());

    SearchParams captuedSearchParams = searchParamsCaptor.getValue();
    assertThat(captuedSearchParams.getFacets(), hasItems("catalog-author-facet", "catalog-publisher-facet"));
  }

  @Test
  void givenNoFacets_whenSearchIsCalled_thenDefaultFacetsAreUsed() throws Exception {
    ArgumentCaptor<BaseService.SearchParams> searchParamsCaptor = ArgumentCaptor.forClass(BaseService.SearchParams.class);
    this.mockMvc.perform(get("/catalogs/search")
        .param("q", "*")
        .param("qf", "fulltext-search"));
    verify(this.mockedService).find(searchParamsCaptor.capture());

    SearchParams captuedSearchParams = searchParamsCaptor.getValue();
    assertThat(captuedSearchParams.getFacets(), hasItems("catalog-author-facet", "catalog-publisher-facet"));
  }
}
