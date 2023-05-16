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
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspBaseEntity;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspBaseEntityService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@Slf4j
@Tag(name = "HspBaseEntities", description = "Can be either a Kulturobjektdokument, Beschreibung or Digitalisat")
@Validated
public class HspBaseEntityController {
  private final HspBaseEntityService hspBaseEntityService;

  public HspBaseEntityController(final HspBaseEntityService hspBaseEntityService) {
    this.hspBaseEntityService = hspBaseEntityService;
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The HspBaseEntity for the given id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")
  })
  @GetMapping(value = "/any/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HspBaseEntity> getById(@PathVariable @NotEmpty final String id) {
    log.info("GET request for /any/{id} id: {}", id);
    final BaseService.SearchParams searchParams = BaseService.SearchParams.builder()
        .withDisplayFields(ArrayUtils.toArray(DisplayField.ID, DisplayField.TYPE, DisplayField.GROUP_ID))
        .withPhrase(id)
        .withRows(2)
        .withSearchFields(ArrayUtils.toArray(SearchField.ID))
        .build();
    final Result<List<HspBaseEntity>> result = hspBaseEntityService.findHspBaseEntities(searchParams);

    if (result.getPayload().isEmpty()) {
      throw ExceptionFactory.getException(ExceptionType.NOT_FOUND, String.format("No HspBaseEntity with id %s found.", id));
    }
    if (result.getPayload().size() > 1) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT, String.format("Multiple HspBaseEntity for id %s found", id));
    }
    return new ResponseEntity<>(result.getPayload().get(0), HttpStatus.OK);
  }
}