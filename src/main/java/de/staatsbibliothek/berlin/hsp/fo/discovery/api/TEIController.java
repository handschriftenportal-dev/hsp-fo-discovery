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

import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.TEIService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Tag(name = "TEI", description = "TEI for KODs and Descriptions")
public class TEIController extends BaseController {
  private final TEIService teiService;

  @Autowired
  public TEIController(final TEIService teiService) {
    this.teiService = teiService;
  }

  @GetMapping(path = "/tei/", produces = MediaType.TEXT_XML_VALUE)
  @Hidden
  public String getXmlDocumentById() {
    throw ExceptionFactory.getException(ExceptionType.MISSING_PARAMETER,
        "The required field 'id' is missing");
  }

  /**
   * Get the original TEI XML document.
   *
   * @param id HSP description or HSP object ID
   * @return TEI XML doc for usage in webapp
   */
  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_XML_VALUE), description = "the TEI XML document", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no TEI was found for the given Id", responseCode = "404"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If the result is not unique", responseCode = "500")
  })
  @GetMapping(path = "/tei/{id}", produces = MediaType.TEXT_XML_VALUE)
  public String getXmlDocumentById(@PathVariable String id) {
    log.info("GET request for /tei/{id} id: {}", id);
    if (ObjectUtils.isEmpty(id)) {
      throw ExceptionFactory.getException(ExceptionType.MISSING_PARAMETER,
          "The required field 'id' is missing");
    }
    final SearchParams params = SearchParams.builder()
        .withPhrase(id)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .withRows(2)
        .build();

    List<String> teiDocs = teiService.findHspTEIDocuments(params);
    if (teiDocs == null || teiDocs.isEmpty()) {
      throw ExceptionFactory.getException(ExceptionType.NOT_FOUND,
          String.format("No tei xml document found for id %s.", id));
    }
    if (teiDocs.size() > 1) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT,
          String.format("Multiple tei xml documents found for id %s.", id));
    }
    return teiDocs.get(0);
  }
}
