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

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.StringToFilterQueryConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.SearchFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.SearchService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SortField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.StatField;
import io.swagger.v3.oas.annotations.Hidden;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams.QueryOperator.OR;

@Slf4j
@RestController
@Tag(name = "Search")
public class SearchController extends BaseController {
  private final SearchService searchService;
  private final List<String> facetFields;
  private final List<String> statsFields;
  private final StringToFilterQueryConverter filterConverter;
  protected HspConfig hspConfig;

  @Autowired
  public SearchController(final SearchService searchService, final HspConfig hspConfig) {
    this.searchService = searchService;
    this.hspConfig = hspConfig;
    this.facetFields = ListUtils.intersection(this.hspConfig.getFacets(), FacetField.getNames());
    this.statsFields = ListUtils.intersection(this.hspConfig.getStats(), StatField.getNames());
    this.filterConverter = new StringToFilterQueryConverter(ListUtils.sum(this.facetFields, this.statsFields));
  }

  /**
   * @param q     a query in RSQL syntax (see <a href=
   *              "https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>).
   *              Fields in the RSQL query may be any Solr search fields.
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
  @ApiResponses(value = {@ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "search result", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "JSON processing error", responseCode = "500")})
  @ConditionalOnExpression("false")
  @GetMapping(path = "/search/extended", produces = MediaType.APPLICATION_JSON_VALUE)
  @Hidden
  ResponseEntity<String> searchExtended(
      @Parameter(description = "a query in RSQL syntax (see <a href=\n" + " \"https://github.com/jirutka/rsql-parser\">https://github.com/jirutka/rsql-parser</a>).\n" + " Fields in the RSQL query may be any Solr search fields.", example = "settlement-search==\"Wolfenbüttel\";repository-search==\"Herzog August Bibliothek\"")
      @RequestParam(name = "q") final String q,
      @Parameter(description = "A filter query as JSON object. The JSON object key correspond with facet field names.\n" + " Entry values may have two forms: <br>\n" + " - For multivalued string filters, JSON values are string arrays, e.g. {" + " \"settlement-facet\": [\"Berlin\", \"Leipzig\"] }<br>\n" + " - For range filters, JSON values are object containing minimum and maximum value as well\n" + " as a flag for including documents without this filter value, e.g. { \"width-facet\": {" + " \"from\": 10, \"to\": 20, \"missing\": false }} <br>\n\n" + " For origin dates, the filter query is a special case of the range filter: <br>\n" + " - name / key is \"orig-date-facet\", filter queries include the two fields\n" + " orig-date-from-facet and orig-date-to-facet<br>\n" + " - filter has an addition boolean field \"exact\" that indicates that both\n" + " \"orig-date-from-facet\" and \"orig-date-to-facet\" of a document have values between \"from\"\n" + " and \"to\"", example = "{ \"settlement-facet\": [\"Wolfenbüttel\", \"Berlin\"], \"repository-facet\": [\"Herzog August Bibliothek\"] }")
      @RequestParam(name = "fq", required = false) final String fq,
      @Parameter(description = "Indicates whether the term should be highlighted in the response documents.")
      @RequestParam(name = "hl", required = false, defaultValue = "true")
      final boolean hl,
      @Parameter(description = "Start index for the response documents.")
      @RequestParam(name = "start", required = false, defaultValue = "0")
      final long start,
      @Parameter(description = "Maximum number of response documents.")
      @RequestParam(name = "rows", required = false, defaultValue = "10")
      final long rows,
      @Parameter(description = "Sort logic.", schema = @Schema(implementation = String.class, allowableValues = {"ms-identifier-asc, ms-identifier-desc, orig-date-asc, orig-date-desc, score-desc"}))
      @RequestParam(name = "sort", required = false, defaultValue = "score-desc")
      final SortField sort) {
    //ToDo implement
    return new ResponseEntity<>("", HttpStatus.OK);
  }

  /**
   * @param q                        The term to be searched for. This can be either a single term,
   *                                 that can be wildcarded by using '?' (representing exact one character)
   *                                 or '*' representing zero or more sequential characters,
   * @param qf                       a comma separated list of field names, that should be considered when searching for {@code q}
   * @param fq                       A filter query as JSON object. The JSON object key correspond with facet field names.
   *                                 Entry values may have two forms: <br>
   *                                 - For multivalued string filters, JSON values are string arrays, e.g. {
   *                                 "settlement-facet": ["Berlin", "Leipzig"] }<br>
   *                                 - For range filters, JSON values are object containing minimum and maximum value as well
   *                                 as a flag for including documents without this filter value, e.g. { "width-facet": {
   *                                 "from": 10, "to": 20, "missing": false }} <br>
   *                                 For origin dates, the filter query is a special case of the range filter: <br>
   *                                 - name / key is "orig-date-facet", filter queries include the two fields
   *                                 orig-date-from-facet and orig-date-to-facet<br>
   *                                 - filter has an addition boolean field "exact" that indicates that both
   *                                 "orig-date-from-facet" and "orig-date-to-facet" of a document have values between "from"
   *                                 and "to"
   * @param hl                       Indicates whether the term should be highlighted in the response documents.
   * @param start                    Start index for the response documents.
   * @param rows                     Maximum number of response documents.
   * @param sort                     describes how to order the result. Might be one of {@code SortField}
   * @return A list containing all matching documents with their respective.
   */
  @GetMapping(value = {"/hspobjects", "/search"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(value = {@ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "search result", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "JSON processing error", responseCode = "500")})
  public ResponseEntity<Result<List<HspObjectGroup>>> search(
      @Parameter(description = "The term to be searched for. This can be either a single term," + "that can be wildcarded by using '?' (representing exact one character)" + " or '*' representing zero or more sequential characters.", example = "\"\\\"Herzo? August Biblioth*\\\"\"")
      @RequestParam(name = "q") final String q,
      @Parameter(description = "Comma separated list of fields, on which the search should be performed on", examples = {@ExampleObject(value = "repository-search")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "qf", required = false)
      final List<String> qf,
      @Parameter(description = "A filter query as JSON object. The JSON object key correspond with facet field names.\n" + " Entry values may have two forms: <br>\n" + " - For multivalued string filters, JSON values are string arrays, e.g. {" + " \"settlement-facet\": [\"Berlin\", \"Leipzig\"] }<br>\n" + " - For range filters, JSON values are object containing minimum and maximum value as well\n" + " as a flag for including documents without this filter value, e.g. { \"width-facet\": {" + " \"from\": 10, \"to\": 20, \"missing\": false }} <br>\n\n" + " For origin dates, the filter query is a special case of the range filter: <br>\n" + " - name / key is \"orig-date-facet\", filter queries include the two fields\n" + " orig-date-from-facet and orig-date-to-facet<br>\n" + " - filter has an addition boolean field \"exact\" that indicates that both\n" + " \"orig-date-from-facet\" and \"orig-date-to-facet\" of a document have values between \"from\"\n" + " and \"to\"", example = "{ \"settlement-facet\": [\"Wolfenbüttel\", \"Berlin\"], \"repository-facet\": [\"Herzog August Bibliothek Wolfenbüttel\"] }")
      @RequestParam(name = "fq", required = false) final String fq,
      @Parameter(description = "Indicates whether the term should be highlighted in the response documents.")
      @RequestParam(name = "hl", required = false, defaultValue = "true")
      final boolean hl,
      @Parameter(description = "Start index for the response documents.")
      @RequestParam(name = "start", required = false, defaultValue = "0")
      final long start,
      @Parameter(description = "Maximum number of response documents.")
      @RequestParam(name = "rows", required = false, defaultValue = "10")
      final long rows,
      @Parameter(description = "Sort logic.", schema = @Schema(implementation = String.class, allowableValues = {"ms-identifier-asc, ms-identifier-desc, orig-date-asc, orig-date-desc, score-desc"}))
      @RequestParam(name = "sort", required = false, defaultValue = "score-desc")
      final SortField sort) {
    log.info("GET request for /hspobjects with params q: {}, qf: {} fq: {}, hl: {}, start: {}, rows: {}, sort: {}", q, qf, fq, hl, start, rows, sort);

    final SearchField[] filteredFields = SearchFieldFilter.filter(qf);
    final Result<List<HspObjectGroup>> result = performSearch(q, filteredFields, fq, hl, start, rows, sort);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  private Result<List<HspObjectGroup>> performSearch(final String query, final SearchField[] searchFields, final String filterQuery, final boolean hl, final long start, final long rows, final SortField sort) {
    final SearchParams params = SearchParams.builder()
        .withCollapse(true)
        .withFacets(this.facetFields)
        .withFilterQueries(filterConverter.convert(filterQuery))
        .withHighlightSnippetCount(hspConfig.getSnippetCount())
        .withPhrase(query)
        .withRows(rows)
        .withSearchFields(searchFields)
        .withSortPhrase(sort.getSortPhrase())
        .withSpellcheck(true)
        .withStart(start)
        .withStats(this.statsFields)
        .build();

    Result<List<String>> result = searchService.findHspObjectGroupIds(params);

    if (CollectionUtils.isEmpty(result.getPayload()) && result.getMetadata() != null && result.getMetadata()
        .getSpellCorrectedTerm() != null) {
      params.setPhrase(result.getMetadata()
          .getSpellCorrectedTerm());
      result = searchService.findHspObjectGroupIds(params);
    }

    if (CollectionUtils.isNotEmpty(result.getPayload())) {
      final SearchParams compParams = getGroupCompletionParamsBySearchParams(params, result.getPayload(), hl);
      final Result<List<HspObjectGroup>> completeResult = searchService.findHspObjectGroups(compParams);
      return mergeResults(result, completeResult);
    }
    return new Result(List.of(), result.getMetadata());
  }

  private Result<List<HspObjectGroup>> mergeResults(final Result<List<String>> resultWithFacets, final Result<List<HspObjectGroup>> resultWithGroupData) {
    final Map<String, HspObjectGroup> mappedResultData = mapById(resultWithGroupData.getPayload());

    /* filter for object groups that are part of resultWithGroupData i.e. that ones who got a HspObject */
    final List<HspObjectGroup> resultData = resultWithFacets.getPayload()
        .stream()
        .filter(mappedResultData::containsKey)
        .map(mappedResultData::get)
        .collect(Collectors.toList());
    final long resultNumFound = resultWithFacets.getMetadata()
        .getNumFound();
    final long resultStart = resultWithFacets.getMetadata()
        .getStart();
    final long resultRows = resultWithFacets.getMetadata()
        .getRows();
    final MetaData resultMetaData = MetaData.builder()
        .withNumFound(resultNumFound)
        .withRows(resultRows)
        .withStart(resultStart)
        .build();
    resultMetaData.setFacets(Map.copyOf(resultWithFacets.getMetadata()
        .getFacets()));
    resultMetaData.setStats(Map.copyOf(resultWithFacets.getMetadata()
        .getStats()));
    resultMetaData.setHighlighting(Map.copyOf(resultWithGroupData.getMetadata()
        .getHighlighting()));

    return new Result<>(resultData, resultMetaData);
  }

  private Map<String, HspObjectGroup> mapById(List<HspObjectGroup> items) {
    return items.stream()
        .collect(Collectors.toMap(HspObjectGroup::getGroupId, item -> item));
  }

  private SearchParams getGroupCompletionParamsBySearchParams(final SearchParams sourceParams, final Collection<String> groupIds, final boolean highlight) {
    final SearchParams targetParams = SearchParams.builder()
        .withGrouping(true)
        .withQuery(SearchField.GROUP_ID.getName() + ":(" + String.join(" ", groupIds) + ")")
        .withQueryOperator(OR)
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(sourceParams.getSearchFields())
        .withStart(0)
        .build();
    /* prepare highlighting */
    if (highlight) {
      targetParams.setHighlight(true);
      targetParams.setHighlightFields(sourceParams.getSearchFields());
      targetParams.setHighlightPhrase(sourceParams.getPhrase());
      targetParams.setHighlightQuery(sourceParams.getQuery());
      targetParams.setHighlightSnippetCount(hspConfig.getSnippetCount());
    }
    return targetParams;
  }
}
