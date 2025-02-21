package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspBaseEntity;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HspBaseEntityControllerTest extends AbstractRestControllerTest {
  @MockBean
  private BaseService<HspBaseEntity> hspBaseEntityService;

  @MockBean
  private AuthorityFileService authorityFileService;

  private final HighlightConfig highlightConfig;

  @Autowired
  public HspBaseEntityControllerTest(final HspConfig config, final HighlightConfig highlightConfig) {
    this.config = config;
    this.highlightConfig = highlightConfig;
  }

  @Override
  public Object getControllerToTest() {
    return new HspBaseEntityController(hspBaseEntityService, authorityFileService, config, highlightConfig);
  }

  @Test
  void givenNoId_whenGetByIdIsCalled_thenBadRequestIsReturned() throws Exception {
    this.mockMvc.perform(get("/any/"))
        .andExpect(status().isNotFound());
  }

  @Test
  void givenHspIdWithoutMatchingObject_whenGetByIdIsCalled_thenNotFoundIsReturned() throws Exception {
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withDisplayFields(ArrayUtils.toArray(DisplayField.ID, DisplayField.TYPE, DisplayField.GROUP_ID))
        .withPhrase("HSP-01234567-0123-0123-0123-0123456789ab")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    Mockito.when(this.hspBaseEntityService.find(params))
        .thenReturn(new Result<>(Collections.emptyList()));

    this.mockMvc.perform(get("/any/HSP-01234567-0123-0123-0123-0123456789ab"))
        .andExpect(status().isNotFound());
  }

  @Test
  void givenHspId_whenGetByIdIsCalled_thenHspObjectIsReturned() throws Exception {
    final HspBaseEntity mockedHspBaseEntity = new HspBaseEntity("HSP-01234567-0123-0123-0123-0123456789ab", "hsp:object");
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withDisplayFields(ArrayUtils.toArray(DisplayField.ID, DisplayField.TYPE, DisplayField.GROUP_ID))
        .withPhrase("HSP-01234567-0123-0123-0123-0123456789ab")
        .withRows(2)
        .withSearchFields(List.of("id-search"))
        .build();

    Mockito.when(this.hspBaseEntityService.find(params))
        .thenReturn(new Result<>(List.of(mockedHspBaseEntity)));

    this.mockMvc.perform(get("/any/HSP-01234567-0123-0123-0123-0123456789ab"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockedHspBaseEntity)));
  }

  @Test
  void givenAuthorityFileId_whenGetByIdIsCalled_thenAuthorityIdIsReturned() throws Exception {
    final HspBaseEntity mockedHspBaseEntity = new HspBaseEntity("NORM-01234567-0123-0123-0123-0123456789ab", "hsp:authority-file");

    Mockito.when(this.authorityFileService.findById("NORM-01234567-0123-0123-0123-0123456789ab", HspBaseEntity[].class))
        .thenReturn(new HspBaseEntity[]{mockedHspBaseEntity});

    this.mockMvc.perform(get("/any/NORM-01234567-0123-0123-0123-0123456789ab"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockedHspBaseEntity)));
  }

  @Test
  void givenInvalidId_whenGetByIdIsCalled_thenInternalServerErrorIsReturned() throws Exception {
    this.mockMvc.perform(get("/any/NORM-01234567"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenAuthorityFileIdWithoutMatchingAuthorityFile_whenGetByIdIsCalled_thenNotFoundIsReturned() throws Exception {
    Mockito.when(this.authorityFileService.findById("NORM-01234567-0123-0123-0123-0123456789ab", HspBaseEntity[].class))
        .thenReturn(new HspBaseEntity[]{});

    this.mockMvc.perform(get("/any/NORM-01234567-0123-0123-0123-0123456789ab"))
        .andExpect(status().isNotFound());
  }
}