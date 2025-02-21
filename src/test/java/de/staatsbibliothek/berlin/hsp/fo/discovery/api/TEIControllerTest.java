package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.TEIService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TEIControllerTest extends AbstractRestControllerTest {
  private final TEIService teiService;
  private final HspConfig config;
  private final HighlightConfig highlightConfig;

  @Autowired
  public TEIControllerTest(final HspConfig config, final HighlightConfig highlightConfig) {
    this.config = config;
    this.highlightConfig = highlightConfig;
    this.teiService = mock(TEIService.class);
  }

  @Override
  public Object getControllerToTest() {
    return new TEIController(teiService, config, highlightConfig);
  }

  @Test
  void whenTeiDocumentIsCalledWithoutId_thenNotFoundIsReturned() throws Exception {
    // perform request and check expectations
    this.mockMvc.perform(get("/tei/")).andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void whenTeiDocumentIsCalledNotExistingId_thenNotFoundIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(ArrayUtils.toArray(DisplayField.TEI_DOCUMENT))
        .withPhrase("def")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    // prepare result
    final Result<List<String>> mockedResult = new Result<>(List.of());

    // set the mocked result
    Mockito.when(this.teiService.find(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/def"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenTeiDocumentIdIsNotUnique_thenInternalServerErrorIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{DisplayField.TEI_DOCUMENT})
        .withPhrase("nonUnique")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    // prepare result
    final Result<List<String>> mockedResult = new Result<>(List.of("a", "b"));

    // set the mocked response
    Mockito.when(this.teiService.find(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/nonUnique"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void whenTeiDocumentIsCalledWithId_thenTeiDocumentIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(ArrayUtils.toArray(DisplayField.TEI_DOCUMENT))
        .withPhrase("teiId")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    // prepare result
    final Result<List<String>> mockedResult = new Result<>(List.of("tei-content"));

    // set the mocked result
    Mockito.when(this.teiService.find(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/teiId"))
        .andExpect(status().isOk())
        .andExpect(content().string("tei-content"));
  }
}
