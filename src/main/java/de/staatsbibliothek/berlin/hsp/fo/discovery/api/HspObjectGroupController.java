/*
 * MIT License
 *
 * Copyright (c) 2021 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.DisplayFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SortField;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.staatsbibliothek.berlin.hsp.fo.discovery.util.Constants.*;

/**
 * rest controller providing search functions for domain entities
 **/

@RequestMapping("/hspobjects")
@RestController
@Tag(name = "Kulturobjektdokumente mit Referenzen")
@Validated
public class HspObjectGroupController extends BaseEntityController<HspObjectGroup> {

  @Autowired
  public HspObjectGroupController(final BaseService<HspObjectGroup> baseService, final HspConfig hspConfig, final HighlightConfig highlightConfig) {
    super(baseService, hspConfig, highlightConfig);
  }

  @ApiResponses(value = {@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "All HspObjectGroups within the range of [start, start+rows]", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")})
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Result<List<HspObjectGroup>>> getAll(
      @Parameter(description = "Comma separated list of fields, that should be returned for the object", examples = {@ExampleObject(value = "id")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "fields", required = false)
      final List<String> fields,
      @Parameter(description = "Start index for the response documents.")
      @RequestParam(name = "start", required = false, defaultValue = "0")
      final long start,
      @Parameter(description = "Maximum number of response documents.")
      @RequestParam(name = "rows", required = false, defaultValue = "10")
      final long rows) {

    final DisplayField[] filteredFields = DisplayFieldFilter.filterAndAddDisplaySuffix(fields);
    final SearchParams params = SearchParams.builder()
        .withDisplayFields(filteredFields)
        .withRows(rows)
        .withStart(start)
        .build();
    final Result<List<HspObjectGroup>> result = baseService.find(params);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiResponses(value = {@ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The HspObjectGroup for the given id", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no HspObjectGroup was found for the given Id", responseCode = "404"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If the result is not unique", responseCode = "500")})
  @GetMapping(value = {"/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Result<HspObjectGroup>> findById(
      @PathVariable @NotBlank final String id) {
    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withRows(2)
        .withPhrase(id)
        .withSearchFields(List.of("id-search"))
        .build();

    Result<List<HspObjectGroup>> result = baseService.find(params);
    checkIdResult(result, id);
    return new ResponseEntity<>(new Result<>(result.getPayload()
        .get(0)), HttpStatus.OK);
  }

  /**
   * @param q     The term to be searched for. This can be either a single term,
   *              that can be wild-carded by using '?' (representing exact one character)
   *              or '*' representing zero or more sequential characters, or a search query in RSQL syntax. For further information,
   *    *         see <a href="https://github.com/jirutka/rsql-parser">RSQL specification</a>
   * @param qf    a comma separated list of field names, that should be considered when searching for {@code q}
   * @param fq    A filter query as JSON object. The JSON object key correspond with facet field names.
   *              Entry values may have two forms: <br>
   *              - For multivalued string filters, JSON values are string arrays, e.g. {
   *              "settlement-facet": ["Berlin", "Leipzig"] }<br>
   *              - For range filters, JSON values are object containing minimum and maximum value as well
   *              as a flag for including documents without this filter value, e.g. { "width-facet": {
   *              "from": 10, "to": 20, "missing": false }} <br>
   *              For origin dates, the filter query is a special case of the range filter: <br>
   *              - name / key is "orig-date-facet", filter queries include the two fields
   *              orig-date-from-facet and orig-date-to-facet<br>
   *              - filter has an addition boolean field "exact" that indicates that both
   *              "orig-date-from-facet" and "orig-date-to-facet" of a document have values between "from"
   *              and "to"
   * @param hl    Indicates whether the term should be highlighted in the response documents.
   * @param start Start index for the response documents.
   * @param rows  Maximum number of response documents.
   * @param sort  describes how to order the result. Might be one of {@code SortField}
   * @return A list containing all matching documents with their respective.
   */
  @GetMapping(value = {"/search"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(value = {@ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "search result", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "JSON processing error", responseCode = "500")})
  public ResponseEntity<Result<List<HspObjectGroup>>> search(
      @Parameter(description = API_PARAM_DESCRIPTION_QUERY_WITH_EXTENDED, example = API_PARAM_EXAMPLE_QUERY)
      @RequestParam(name = "q") final String q,
      @Parameter(description = API_PARAM_DESCRIPTION_QUERY_FIELDS, examples = {@ExampleObject(value = "repository-search")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "qf", required = false)
      final List<String> qf,
      @Parameter(description = API_PARAM_DESCRIPTION_FILTER_QUERY, example = API_PARAM_EXAMPLE_FILTER_QUERY)
      @RequestParam(name = "fq", required = false) final String fq,
      @Parameter(description = API_PARAM_DESCRIPTION_HIGHLIGHT)
      @RequestParam(name = "hl", required = false, defaultValue = "true")
      final boolean hl, @Parameter(description = API_PARAM_DESCRIPTION_START)
      @RequestParam(name = "start", required = false, defaultValue = "0")
      final long start, @Parameter(description = API_PARAM_DESCRIPTION_ROWS)
      @RequestParam(name = "rows", required = false, defaultValue = "10")
      final long rows,
      @Parameter(description = "Sort logic.", schema = @Schema(implementation = String.class, allowableValues = {"ms-identifier-asc, ms-identifier-desc, orig-date-asc, orig-date-desc, score-desc"}))
      @RequestParam(name = "sort", required = false, defaultValue = "score-desc")
      final SortField sort,
      @Parameter(description = "Whether this is an extended search query or not")
      @RequestParam(name = "isExtended", required = false, defaultValue = "false")
      final boolean isExtended) {
    final Result<List<HspObjectGroup>> result = performSearch(q, getSearchFieldsWithDefaults(qf), fq, hl, start, rows, sort, isExtended);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  private Result<List<HspObjectGroup>> performSearch(final String query, final List<String> searchFields, final String filterQuery, final boolean hl, final long start, final long rows, final SortField sort, boolean isExact) {
    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFacets(this.defaultFacetFields)
        .withFilterQueries(filterConverter.convert(filterQuery, baseService.getTypeFilter()))
        .withHighlight(hl)
        .withRows(rows)
        .withSearchFields(searchFields)
        .withSortPhrase(sort.getSortPhrase())
        .withStart(start)
        .withStats(this.statsFields)
        .withUseSpellCorrection(true)
        .build();

    if(!isExact) {
      params.setPhrase(query);
    } else {
      params.setPhraseExtended(query);
    }
    return baseService.find(params);
  }
}
