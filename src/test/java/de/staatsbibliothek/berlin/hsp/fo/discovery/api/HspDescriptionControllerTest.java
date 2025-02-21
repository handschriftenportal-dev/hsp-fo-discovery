package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HspDescriptionControllerTest extends AbstractRestControllerTest {
  @MockBean
  private BaseService<HspDescription> service;
  private final HighlightConfig highlightConfig;

  @Autowired
  public HspDescriptionControllerTest(final HspConfig config, final HighlightConfig highlightConfig) {
    this.config = config;
    this.highlightConfig = highlightConfig;
  }

  @Override
  public Object getControllerToTest() {
    return new HspDescriptionController(service, config, highlightConfig);
  }

  @Test
  void whenGetByIdIsCalledAndIdIsWrong_thenNotFoundIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withPhrase("invalid-id")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    Mockito.when(this.service.find(params))
        .thenReturn(new Result<>(Collections.emptyList()));

    this.mockMvc.perform(get("/descriptions/invalid-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenGetByIdIsCalled_thenDescriptionIsReturned() throws Exception {
    final HspDescription mockedDescription = new HspDescription("valid-id", "hsp:description");
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withPhrase("valid-id")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    Mockito.when(this.service.find(params))
        .thenReturn(new Result<>(List.of(mockedDescription)));

    this.mockMvc.perform(get("/descriptions/valid-id"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockedDescription)));
  }

  @Test
  void whenGetAllIsCalled_thenAllDescriptionsAreReturned() throws Exception {
    final HspDescription mockedDescription01 = new HspDescription("valid-id-01", "hsp:description");
    final HspDescription mockedDescription02 = new HspDescription("valid-id-02", "hsp:description");
    final HspDescription mockedDescription03 = new HspDescription("valid-id-03", "hsp:description");
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withRows(10)
        .withStart(0)
        .build();

    final Result<List<HspDescription>> mockResult = new Result<>(List.of(mockedDescription01, mockedDescription02, mockedDescription03));

    Mockito.when(this.service.find(params))
        .thenReturn(mockResult);

    this.mockMvc.perform(get("/descriptions"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockResult)));
  }
}
