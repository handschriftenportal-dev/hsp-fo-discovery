/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.HspDescriptionServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HspDescriptionControllerTest extends AbstractRestControllerTest {

  private final HspDescriptionServiceImpl descriptionService;

  public HspDescriptionControllerTest() {
    this.descriptionService = mock(HspDescriptionServiceImpl.class);
  }

  @Override
  public Object getControllerToTest() {
    return new HspDescriptionController(descriptionService);
  }

  @Test
  void whenGetByIdIsCalledAndIdIsWrong_thenNotFoundIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withPhrase("invalid-id")
        .withRows(2)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    Mockito.when(this.descriptionService.findDescriptions(params))
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
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    Mockito.when(this.descriptionService.findDescriptions(params))
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

    Mockito.when(this.descriptionService.findDescriptions(params))
        .thenReturn(mockResult);

    this.mockMvc.perform(get("/descriptions"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockResult)));
  }
}
