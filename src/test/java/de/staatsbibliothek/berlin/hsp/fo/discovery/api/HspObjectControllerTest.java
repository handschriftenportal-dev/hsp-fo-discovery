package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObject;
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

class HspObjectControllerTest extends AbstractRestControllerTest {
  @MockBean
  private BaseService<HspObject> service;
  private final HspConfig config;
  private final HighlightConfig highlightConfig;

  @Autowired
  public HspObjectControllerTest(final HspConfig config, final HighlightConfig highlightConfig) {
    this.config = config;
    this.highlightConfig = highlightConfig;
  }

  @Override
  public Object getControllerToTest() {
    return new HspObjectController(service, config, highlightConfig);
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

    this.mockMvc.perform(get("/kods/invalid-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenGetByIdIsCalled_thenDescriptionIsReturned() throws Exception {
    final HspObject mockedKOD = new HspObject("valid-id", "hsp:object");
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withPhrase("valid-id")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    Mockito.when(this.service.find(params))
        .thenReturn(new Result<>(List.of(mockedKOD)));

    this.mockMvc.perform(get("/kods/valid-id"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockedKOD)));
  }

  @Test
  void whenGetAllIsCalled_thenAllDescriptionsAreReturned() throws Exception {
    final HspObject mockedKOD01 = new HspObject("valid-id-01", "hsp:object");
    final HspObject mockedKOD02 = new HspObject("valid-id-02", "hsp:object");
    final HspObject mockedKOD03 = new HspObject("valid-id-03", "hsp:object");
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withRows(10)
        .withStart(0)
        .build();

    final Result<List<HspObject>> mockResult = new Result<>(List.of(mockedKOD01, mockedKOD02, mockedKOD03));

    Mockito.when(this.service.find(params))
        .thenReturn(mockResult);

    this.mockMvc.perform(get("/kods"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockResult)));
  }
}