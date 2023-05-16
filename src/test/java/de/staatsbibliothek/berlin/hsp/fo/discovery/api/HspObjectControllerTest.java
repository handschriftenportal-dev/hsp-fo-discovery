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

import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.HspObjectServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HspObjectControllerTest extends AbstractRestControllerTest {

  private final HspObjectServiceImpl hspObjectServiceImpl;

  public HspObjectControllerTest() {
    this.hspObjectServiceImpl = mock(HspObjectServiceImpl.class);
  }

  @Override
  public Object getControllerToTest() {
    return new HspObjectController(hspObjectServiceImpl);
  }

  @Test
  void whenGetByIdIsCalledAndIdIsWrong_thenNotFoundIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(new DisplayField[]{})
        .withPhrase("invalid-id")
        .withRows(2)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    Mockito.when(this.hspObjectServiceImpl.findHspObjects(params))
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
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    Mockito.when(this.hspObjectServiceImpl.findHspObjects(params))
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

    Mockito.when(this.hspObjectServiceImpl.findHspObjects(params))
        .thenReturn(mockResult);

    this.mockMvc.perform(get("/kods"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockResult)));
  }
}