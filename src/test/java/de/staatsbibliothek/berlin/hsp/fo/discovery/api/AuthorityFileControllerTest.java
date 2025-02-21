package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.service.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.adapter.GNDEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorityFileControllerTest extends AbstractRestControllerTest {

  @MockBean
  private AuthorityFileService authorityFileService;

  @Override
  public Object getControllerToTest() {
    return new AuthorityFileController(authorityFileService);
  }

  @Test
  void givenValidId_whenGetByIdIsCalled_thenAuthorityFileIsReturned() throws Exception {
    final String authorityFileId = "NORM-01234567-89ab-cdef-0123-456789abcdef";
    final GNDEntity[] mockedResult = { GNDEntity.builder()
        .withId(authorityFileId)
        .withPreferredName("Test Name")
        .withTypeName("Place")
        .withGndId("GND-Id")
            .build() };

    Mockito.when(this.authorityFileService.findById(authorityFileId, GNDEntity[].class)).thenReturn(mockedResult);
    
    this.mockMvc.perform(get("/authority-files/NORM-01234567-89ab-cdef-0123-456789abcdef"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockedResult[0])));
  }

  @Test
  void givenValidIdMatchingTwoAuthorityFiles_whenGetByIdIsCalled_thenInternalServerErrorIsReturned() throws Exception {
    final String authorityFileId = "NORM-01234567-89ab-cdef-0123-456789abcdef";
    final GNDEntity mockedGNDEntity = GNDEntity.builder()
        .withId(authorityFileId)
        .withPreferredName("Test Name")
        .withTypeName("Place")
        .withGndId("GND-Id")
        .build();
      final GNDEntity[] mockedResult = { mockedGNDEntity, mockedGNDEntity };

    Mockito.when(this.authorityFileService.findById(authorityFileId, GNDEntity[].class)).thenReturn(mockedResult);

    this.mockMvc.perform(get("/authority-files/NORM-01234567-89ab-cdef-0123-456789abcdef"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void givenValidIdWithoutMatchingAuthorityFile_whenGetByIdIsCalled_thenNotFoundIsReturned() throws Exception {
    this.mockMvc.perform(get("/authority-files/ORM-01234567-89ab-cdef-0123-456789abcdef"))
        .andExpect(status().isNotFound());
  }
}
