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

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.DisplayFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspDescriptionService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Slf4j
@RestController
@Tag(name = "Beschreibungen")
@Validated
public class HspDescriptionController extends BaseController {
  private final HspDescriptionService hspDescriptionService;

  public HspDescriptionController(@Autowired final HspDescriptionService hspDescriptionService) {
    this.hspDescriptionService = hspDescriptionService;
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = HspDescription.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "Description for the given id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no description was found for the given Id", responseCode = "404"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")})
  @GetMapping(value = {"/descriptions/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public ResponseEntity<HspDescription> getById(
      @PathVariable @NotEmpty final String id,
      @Parameter(description = "Comma separated list of fields, that should be returned for the description", examples = {@ExampleObject(value = "id")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "fields", required = false)
      final List<String> fields) {
    log.info("GET request for /descriptions/{id} id: {}", id);

    final DisplayField[] filteredFields = DisplayFieldFilter.filterAndAddDisplaySuffix(fields);
    final SearchParams params = SearchParams.builder()
                                            .withDisplayFields(filteredFields)
                                            .withRows(2)
                                            .withPhrase(id)
                                            .withSearchFields(ArrayUtils.toArray(SearchField.ID))
                                            .build();

    final Result<List<HspDescription>> result = hspDescriptionService.findDescriptions(params);

    if (result.getPayload().isEmpty()) {
      throw ExceptionFactory.getException(ExceptionType.NOT_FOUND, String.format("No description with id %s found.", id));
    }
    if (result.getPayload().size() > 1) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT, String.format("Multiple descriptions for id %s found", id));
    }
    return new ResponseEntity<>(result.getPayload().get(0), HttpStatus.OK);
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The description for the given id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")
  })
  @GetMapping(path = "/descriptions", produces = MediaType.APPLICATION_JSON_VALUE)
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public ResponseEntity<Result<List<HspDescription>>> getAll(
      @Parameter(description = "Comma separated list of attributes, that should be returned for the object", examples = {@ExampleObject(value = "id")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "fields", required = false) final List<String> fields,
      @Parameter(
          description = "Start index for the response documents."
      )
      @RequestParam(
          defaultValue = "0",
          name = "start",
          required = false
      ) final long start,
      @Parameter(
          description = "Maximum number of response documents."
      )
      @RequestParam(
          defaultValue = "10",
          name = "rows",
          required = false
      ) final long rows) {
    log.info("GET request for /descriptions start: {}, rows: {}, number of fields: {}", start, rows, fields);

    final DisplayField[] filteredFields = DisplayFieldFilter.filterAndAddDisplaySuffix(fields);
    final SearchParams params = SearchParams.builder()
                                            .withDisplayFields(filteredFields)
                                            .withRows(rows)
                                            .withStart(start)
                                            .build();
    final Result<List<HspDescription>> result = hspDescriptionService.findDescriptions(params);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
