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
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * rest controller providing search functions for domain entities
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@RestController
@Slf4j
@Tag(name = "Kulturobjektdokumente with references")
@Validated
public class HspObjectGroupController extends BaseController {
  private final HspObjectGroupService searchService;

  @Autowired
  protected HspObjectGroupController(HspObjectGroupService searchService) {
    this.searchService = searchService;
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The HspObjectGroup for the given id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no HspObjectGroup was found for the given Id", responseCode = "404"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If the result is not unique", responseCode = "500")
  })
  @GetMapping(value = { "/hspobjects/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Result<HspObjectGroup>> findById(@NotBlank @PathVariable final String id) {
    log.info("GET request for /hspobjects/{id} id: {}", id);

    final SearchParams params = SearchParams.builder()
        .withGrouping(true)
        .withRows(Integer.MAX_VALUE)
        .withPhrase(id)
        .withSearchFields(ArrayUtils.toArray(SearchField.GROUP_ID))
        .withStart(0)
        .build();

    Result<List<HspObjectGroup>> result = searchService.findHspObjectGroups(params);

    if (result.getPayload().size() == 0 ) {
      throw ExceptionFactory.getException(ExceptionType.NOT_FOUND,
          String.format("No hsp object with id %s found.", id));
    }
    if (result.getPayload().size() > 1 ) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT,
          String.format("Multiple objects for id %s found.", id));
    }

    //ToDo replace with: return new ResponseEntity<>(result.getPayload().get(0), HttpStatus.OK);
    return new ResponseEntity<>(new Result<>(result.getPayload().get(0)), HttpStatus.OK);
  }
}
