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

import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.HspObjectGroupServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.TestDataProvider;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HspObjectGroupControllerTest extends AbstractRestControllerTest {

  public HspObjectGroupService searchService;

  public HspObjectGroupControllerTest() {
    this.searchService = Mockito.mock(HspObjectGroupServiceImpl.class);
  }

  @Override
  public Object getControllerToTest() {
    return new HspObjectGroupController(searchService);
  }

  @Test
  void whenCalledWithoutId_thenNotFoundIsReturned() throws Exception {
    // prepare response
    final Result<List<HspObjectGroup>> mockedResult = new Result<>();

    final SearchParams params = SearchParams.builder()
        .withGrouping(true)
        .withPhrase(null)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();

    // set the mocked response
    Mockito.when(this.searchService.findHspObjectGroups(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/")).andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void whenCalledNotExistingId_thenNotFoundIsReturned() throws Exception {
    // prepare response
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(), TestDataProvider.getMetadata());

    final SearchParams params = SearchParams.builder()
        .withGrouping(true)
        .withRows(Integer.MAX_VALUE)
        .withPhrase("def")
        .withSearchFields(ArrayUtils.toArray(SearchField.GROUP_ID))
        .withStart(0)
        .build();

    // set the mocked response
    Mockito.when(this.searchService.findHspObjectGroups(params)).thenReturn(mockedResult);

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/def"))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenCalledWithId_thenHOGIsReturned() throws Exception {
    final Result<List<HspObjectGroup>> mockedResult = new Result<>(List.of(TestDataProvider.getTestData()), null);
    
    final SearchParams params = SearchParams.builder()
        .withGrouping(true)
        .withRows(Integer.MAX_VALUE)
        .withPhrase("existingId")
        .withSearchFields(ArrayUtils.toArray(SearchField.GROUP_ID))
        .withStart(0)
        .build();
    // set the mocked result
    Mockito.when(this.searchService.findHspObjectGroups(params)).thenReturn(mockedResult);

    // build expected json
    final String expectedJson = jsonResponseBuilder.getJson(new Result<>(TestDataProvider.getTestData()));

    // perform request and check expectations
    this.mockMvc.perform(get("/hspobjects/existingId"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson))
        .andReturn();
  }

  @AfterEach
  public void onTearDown() {
    Mockito.reset(this.searchService);
  }
}