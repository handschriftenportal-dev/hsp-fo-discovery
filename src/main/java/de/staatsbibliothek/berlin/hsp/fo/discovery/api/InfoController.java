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
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.InfoService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Tag(name = "Info")
public class InfoController {

  protected final InfoService infoService;
  protected final String componentName;
  protected final String componentDescription;
  protected final String componentVersion;

  public InfoController(InfoService infoService,
                        @Value("${info.component}") final String componentName,
                        @Value("${info.description}")
                        final String componentDescription,
                        @Value("${info.version}")
                        final String componentVersion) {
    this.infoService = infoService;
    this.componentName = componentName;
    this.componentDescription = componentDescription;
    this.componentVersion = componentVersion;
  }

  @GetMapping(path = "/info")
  public ResponseEntity<String> getInfo() {
    log.info("GET request for /info");
    final SearchParams searchParams = SearchParams.builder()
        .withRows(0)
        .withFacets(List.of(FacetField.TYPE.getName()))
        .withFacetTermsExcluded(Collections.emptyList())
        .withIncludeMissingFacet(false)
        .build();

    final MetaData metaData = infoService.getMetaData(searchParams);
    final Map<String, Long> itemCounts = metaData.getFacets()
        .get(FacetField.TYPE.getName());

    final JSONObject response = new JSONObject();
    response.put("component", componentName);
    response.put("description", componentDescription);
    response.put("version", componentVersion);
    response.put("items", itemCounts);
    response.put("sitemap", new JSONObject(Map.of("items", InfoService.getItemCount(metaData, List.of(HspType.HSP_OBJECT, HspType.HSP_DESCRIPTION, HspType.HSP_DESCRIPTION_RETRO)))));
    return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);
  }
}