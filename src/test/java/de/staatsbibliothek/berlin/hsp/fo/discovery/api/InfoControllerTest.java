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

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.InfoService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InfoControllerTest extends AbstractRestControllerTest {

  @Value("${info.version}")
  private String mockVersion;

  @Value("${info.description}")
  private String mockDescription;

  @Value("${info.component}")
  private String mockName;

  private final InfoService infoService;

  public InfoControllerTest() {
    this.infoService = mock(InfoService.class);
  }

  @Override
  public Object getControllerToTest() {
    return new InfoController(infoService, mockName, mockDescription, mockVersion);
  }

  @Test
  void whenInfoEndpointIsCalled_thenResultIsCorrect() throws Exception {
    final SearchParams searchParams = SearchParams.builder()
        .withRows(0)
        .withFacets(List.of(FacetField.TYPE.getName()))
        .withFacetTermsExcluded(Collections.emptyList())
        .withIncludeMissingFacet(false)
        .build();
    final MetaData mockedResult = MetaData.builder()
        .withFacets(Map.of("type-facet", Map.of("hsp:description", 8L, "hsp:object", 3L, "hsp:description_retro", 1L, "hsp:digitization", 5L)))
        .build();
    Mockito.when(this.infoService.getMetaData(searchParams))
        .thenReturn(mockedResult);

    this.mockMvc.perform(get("/info/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(mockVersion))
        .andExpect(jsonPath("$.description").value(mockDescription))
        .andExpect(jsonPath("$.component").value(mockName))
        .andExpect(jsonPath("$.component").value(mockName))
        .andExpect(jsonPath("$.items['hsp:object']").value(3L))
        .andExpect(jsonPath("$.items['hsp:description']").value(8L))
        .andExpect(jsonPath("$.items['hsp:description_retro']").value(1L))
        .andExpect(jsonPath("$.items['hsp:digitization']").value(5L))
        .andExpect(jsonPath("$.sitemap.items").value(12));
  }
}