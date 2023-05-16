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
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspBaseEntity;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspBaseEntityService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.HspBaseEntityServiceImpl;
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

public class HspBaseEntityControllerTest extends AbstractRestControllerTest {
  private final HspBaseEntityService hspBaseEntityService;

  public HspBaseEntityControllerTest() {
    this.hspBaseEntityService = mock(HspBaseEntityServiceImpl.class);
  }

  @Override
  public Object getControllerToTest() {
    return new HspBaseEntityController(this.hspBaseEntityService);
  }

  @Test
  void whenGetByIdIsCalledAndIdIsMissing_thenBadRequestIsReturned() throws Exception {
    this.mockMvc.perform(get("/any/"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenGetByIdIsCalledAndIdIsWrong_thenNotFoundIsReturned() throws Exception {
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withDisplayFields(ArrayUtils.toArray(DisplayField.ID, DisplayField.TYPE, DisplayField.GROUP_ID))
        .withPhrase("invalid-id")
        .withRows(2)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    Mockito.when(this.hspBaseEntityService.findHspBaseEntities(params))
        .thenReturn(new Result<>(Collections.emptyList()));

    this.mockMvc.perform(get("/any/invalid-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenGetByIdIsCalled_thenHspBaseEntityIsReturned() throws Exception {
    final HspBaseEntity mockedHspBaseEntity = new HspBaseEntity("valid-id", "hsp:object");
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withDisplayFields(ArrayUtils.toArray(DisplayField.ID, DisplayField.TYPE, DisplayField.GROUP_ID))
        .withPhrase("valid-id")
        .withRows(2)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    Mockito.when(this.hspBaseEntityService.findHspBaseEntities(params))
        .thenReturn(new Result<>(List.of(mockedHspBaseEntity)));

    this.mockMvc.perform(get("/any/valid-id"))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponseBuilder.getJson(mockedHspBaseEntity)));
  }
}