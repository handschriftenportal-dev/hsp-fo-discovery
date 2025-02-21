package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.InfoService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InfoControllerTest extends AbstractRestControllerTest {

  @Value("${info.version}")
  private String mockVersion;

  @Value("${info.description}")
  private String mockDescription;

  @Value("${info.component}")
  private String mockName;

  @MockBean
  private BaseService<Void> baseService;

  @MockBean
  private InfoService infoService;

  @Override
  public Object getControllerToTest() {
    return new InfoController(baseService, infoService, mockName, mockDescription, mockVersion);
  }

  @Test
  void whenInfoEndpointIsCalled_thenResultIsCorrect() throws Exception {
    final SearchParams searchParams = SearchParams.builder()
        .withRows(0)
        .withFacets(List.of(FacetField.TYPE.getName()))
        .withFacetTermsExcluded(Collections.emptyList())
        .withIncludeMissingFacet(false)
        .build();
    final MetaData mockedResult = MetaData.builder()
        .withFacets(Map.of("type-facet", Map.of("hsp:description", 8L, "hsp:object", 3L, "hsp:description_retro", 1L, "hsp:digitization", 5L)))
        .build();
    Mockito.when(this.baseService.findMetaData(searchParams))
        .thenReturn(mockedResult);


    this.mockMvc.perform(get("/info/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(mockVersion))
        .andExpect(jsonPath("$.description").value(mockDescription))
        .andExpect(jsonPath("$.component").value(mockName))
        .andExpect(jsonPath("$.items['hsp:object']").value(3L))
        .andExpect(jsonPath("$.items['hsp:description']").value(8L))
        .andExpect(jsonPath("$.items['hsp:description_retro']").value(1L))
        .andExpect(jsonPath("$.items['hsp:digitization']").value(5L));
  }
}