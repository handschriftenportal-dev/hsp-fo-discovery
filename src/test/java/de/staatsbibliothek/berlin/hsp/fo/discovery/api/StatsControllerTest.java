package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatsControllerTest extends AbstractRestControllerTest {

  @MockBean
  private BaseService<Void> infoService;

  @Override
  public Object getControllerToTest() {
    return new StatsController(infoService);
  }

  @Test
  void whenStatsEndpointIsCalled_thenResultIsCorrect() throws Exception {
    //KODs
    final SearchParams baseParams = SearchParams.builder()
        .withRows(0)
        .withFacetMinCount(1)
        .withFacetTermsExcluded(Collections.emptyList())
        .withIncludeMissingFacet(true)
        .build();

    final SearchParams kodSearchParams = baseParams.toBuilder()
        .withFacets(List.of(FacetField.REPOSITORY_ID.getName()))
        .withFilterQueries(Map.of("type-facet:(\"hsp:object\")", "type-facet"))
        .build();

    final MetaData mockedResultKOD = MetaData.builder()
        .withFacets(Map.of("repository-id-facet", Map.of("test repo 1", 1L, "test repo 2", 2L)))
        .build();
    Mockito.when(this.infoService.findMetaData(kodSearchParams))
        .thenReturn(mockedResultKOD);

    // Retro Descriptions
    final SearchParams descriptionRetroSearchParams = baseParams.toBuilder()
        .withFacets(List.of(FacetField.REPOSITORY_ID.getName()))
        .withFilterQueries(Map.of("type-facet:(\"hsp:description_retro\")", "type-facet"))
        .build();

    final MetaData mockedResultDescription = MetaData.builder()
        .withFacets(Map.of("repository-id-facet", Map.of("test repo 3", 3L, "test repo 4", 4L)))
        .build();
    Mockito.when(this.infoService.findMetaData(descriptionRetroSearchParams))
        .thenReturn(mockedResultDescription);

    // Descriptions
    final SearchParams descriptionExternalSearchParams = baseParams.toBuilder()
        .withFacets(List.of(FacetField.REPOSITORY_ID.getName()))
        .withFilterQueries(Map.of("type-facet:(\"hsp:description\")", "type-facet", "desc-status-facet:(\"extern\")", "desc-status-facet"))
        .build();

    final MetaData mockedResultDescriptionExternal = MetaData.builder()
        .withFacets(Map.of("repository-id-facet", Map.of("test repo 5", 5L, "test repo 6", 6L)))
        .build();
    Mockito.when(this.infoService.findMetaData(descriptionExternalSearchParams))
        .thenReturn(mockedResultDescriptionExternal);

    final SearchParams descriptionInternalSearchParams = baseParams.toBuilder()
        .withFacets(List.of(FacetField.REPOSITORY_ID.getName()))
        .withFilterQueries(Map.of("type-facet:(\"hsp:description\")", "type-facet", "desc-status-facet:(\"intern\")", "desc-status-facet"))
        .build();

    final MetaData mockedResultDescriptionInternal = MetaData.builder()
        .withFacets(Map.of("repository-id-facet", Map.of("test repo 7", 7L, "test repo 8", 8L)))
        .build();
    Mockito.when(this.infoService.findMetaData(descriptionInternalSearchParams))
        .thenReturn(mockedResultDescriptionInternal);

    // Digitized
    final SearchParams digitizedSearchParams = baseParams.toBuilder()
        .withFacets(List.of(FacetField.REPOSITORY_ID.getName()))
        .withFilterQueries(Map.of("type-facet:(\"hsp:digitized\")", "type-facet"))
        .build();


    final MetaData mockedResultDigitized = MetaData.builder()
        .withFacets(Map.of("repository-id-facet", Map.of("test repo 9", 9L, "test repo 10", 10L)))
        .build();
    Mockito.when(this.infoService.findMetaData(digitizedSearchParams))
        .thenReturn(mockedResultDigitized);

    // Catalogs
    final SearchParams catalogsSearchParams = baseParams.toBuilder()
        .withFacets(List.of(FacetField.REPOSITORY_ID.getName()))
        .withFilterQueries(Map.of("type-facet:(\"hsp:catalog\")", "type-facet"))
        .build();


    final MetaData mockedResultCatalog = MetaData.builder()
        .withFacets(Map.of("repository-id-facet", Map.of("test repo 11", 11L, "test repo 12", 12L)))
        .build();
    Mockito.when(this.infoService.findMetaData(catalogsSearchParams))
        .thenReturn(mockedResultCatalog);

    this.mockMvc.perform(get("/stats/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kulturobjekte.all").value(3L))
        .andExpect(jsonPath("$.kulturobjekte.institution['test repo 1']").value(1L))
        .andExpect(jsonPath("$.kulturobjekte.institution['test repo 2']").value(2L))
        .andExpect(jsonPath("$.digitalisate.all").value(19L))
        .andExpect(jsonPath("$.digitalisate.institution['test repo 9']").value(9L))
        .andExpect(jsonPath("$.digitalisate.institution['test repo 10']").value(10L))
        .andExpect(jsonPath("$.beschreibungen.all").value(33))
        .andExpect(jsonPath("$.beschreibungen.institution['test repo 4']").value(4L))
        .andExpect(jsonPath("$.beschreibungen.institution['test repo 5']").value(5L))
        .andExpect(jsonPath("$.beschreibungen.institution['test repo 6']").value(6L))
        .andExpect(jsonPath("$.beschreibungen.retro").value(7L))
        .andExpect(jsonPath("$.beschreibungen.extern").value(11L))
        .andExpect(jsonPath("$.beschreibungen.intern").value(15L))
        .andExpect(jsonPath("$.kataloge.all").value(23L))
        .andExpect(jsonPath("$.kataloge.institution['test repo 11']").value(11L))
        .andExpect(jsonPath("$.kataloge.institution['test repo 12']").value(12L));
  }
}