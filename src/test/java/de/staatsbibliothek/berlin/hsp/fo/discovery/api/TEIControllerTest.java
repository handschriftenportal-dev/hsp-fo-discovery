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

import de.staatsbibliothek.berlin.hsp.fo.discovery.service.TEIService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.TEIServiceImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TEIControllerTest extends AbstractRestControllerTest {
  private final TEIService teiService;

  public TEIControllerTest() {
    this.teiService = mock(TEIServiceImpl.class);
  }

  @Override
  public Object getControllerToTest() {
    return new TEIController(this.teiService);
  }

  @Test
  void whenTeiDocumentIsCalledWithoutId_thenNotFoundIsReturned() throws Exception {
    // perform request and check expectations
    this.mockMvc.perform(get("/tei/")).andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenTeiDocumentIsCalledNotExistingId_thenNotFoundIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withGrouping(true)
        .withPhrase("def")
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    // prepare result
    final List<String> mockedResult = List.of();

    // set the mocked result
    Mockito.when(this.teiService.findHspTEIDocuments(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/def"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenTeiDocumentIdIsNotUnique_thenInternalServerErrorIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withRows(2)
        .withPhrase("nonUnique")
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    // prepare result
    final List<String> mockedResult = List.of("a", "b");

    // set the mocked response
    Mockito.when(this.teiService.findHspTEIDocuments(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/nonUnique"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void whenTeiDocumentIsCalledWithId_thenTeiDocumentIsReturned() throws Exception {
    final SearchParams params = SearchParams.builder()
        .withRows(2)
        .withPhrase("teiId")
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    // prepare result
    final List<String> mockedResult = List.of("tei-content");

    // set the mocked result
    Mockito.when(this.teiService.findHspTEIDocuments(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/tei/teiId"))
        .andExpect(status().isOk())
        .andExpect(content().string("tei-content"));
  }
}
