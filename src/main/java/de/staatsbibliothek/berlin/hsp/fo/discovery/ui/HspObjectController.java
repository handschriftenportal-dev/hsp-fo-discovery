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
package de.staatsbibliothek.berlin.hsp.fo.discovery.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.ExceptionFactory.QuerySyntaxException;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.AbstractParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.IHspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.QuerySearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.TermSearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SortField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.StatField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.converter.StringToFilterQueryConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.converter.StringToQueryConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.Result;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * rest controller providing search functions for domain entities
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@RestController
@Tag(name = "search", description = "the search API")
public class HspObjectController {

  private static final Logger logger = LoggerFactory.getLogger(HspObjectController.class);

  private IHspObjectGroupService searchService;

  private StringToFilterQueryConverter filterConverter;

  private ObjectMapper mapper;
  
  private List<String> facetFields;
  private List<String> statsFields;
  
  private HspConfig hspConfig;

  @Autowired
  public HspObjectController(final IHspObjectGroupService solrService, final HspConfig hspConfig) {
    this.searchService = solrService;
    this.mapper = new ObjectMapper();
    this.hspConfig = hspConfig;
    this.facetFields = ListUtils.intersection(hspConfig.getFacets(), FacetField.getNames());
    this.statsFields = ListUtils.intersection(hspConfig.getStats(), StatField.getNames());
    this.filterConverter = new StringToFilterQueryConverter(ListUtils.sum(this.facetFields, this.statsFields));
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The HspObjectGroup for the given id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no HspObjectGroup was found for the given Id", responseCode = "404"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If the result is not unique", responseCode = "500")
  })
  @GetMapping(path = "/hspobjects/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public ResponseEntity<String> findById(@PathVariable(
      required = false) final String id) {
    logger.info("GET request for /hspobjects/{id} id: {}", id);

    if (ObjectUtils.isEmpty(id)) {
      throw ExceptionFactory.getException(ExceptionType.MISSING_PARAMETER,
          "The required field 'id' is missing");
    }

    final QuerySearchParams params = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withRows(Integer.MAX_VALUE)
        .withQueryOrSearchTerm(SearchField.GROUP_ID.getName() + ":" + id)
        .withSearchFields(SearchField.GROUP_ID.getName())
        .withStart(0)
        .build();

    Result<List<HspObjectGroup>> result = searchService.findHspObjectGroups(params);

    if (result.getPayload().size() == 0 ) {
      throw ExceptionFactory.getException(ExceptionType.NO_HSP_OBJECT_FOUND,
          String.format("No hsp object with id %s found.", id));
    }
    return new ResponseEntity<String>(toJson(new Result<HspObjectGroup>(result.getPayload().get(0))), HttpStatus.OK);
  }

  @GetMapping(path = "/hspobjects/")
  @Hidden
  @Order(Ordered.LOWEST_PRECEDENCE)
  public ResponseEntity<String> findById() {
    return findById(null);
  }

  /**
   * @param q A simple search term or a query in RSQL syntax (see <a href=
   *        "https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>).
   *        Fields in the RSQL query may be any Solr search fields.
   * @param fq A filter query as JSON object. The JSON object key correspond with facet field names.
   *        Entry values may have two forms: <br>
   *        - For multivalued string filters, JSON values are string arrays, e.g. {
   *        "settlement-facet": ["Berlin", "Leipzig"] }<br>
   *        - For range filters, JSON values are object containing minimum and maximum value as well
   *        as a flag for including documents without this filter value, e.g. { "width-facet": {
   *        "from": 10, "to": 20, "missing": false }} <br>
   *        For origin dates, the filter query is a special case of the range filter: <br>
   *        - name / key is "orig-date-facet", filter queries include the two fields
   *        orig-date-from-facet and orig-date-to-facet<br>
   *        - filter has an addition boolean field "exact" that indicates that both
   *        "orig-date-from-facet" and "orig-date-to-facet" of a document have values between "from"
   *        and "to"
   * @param hl Indicates whether the term should be highlighted in the response documents.
   * @param start Start index for the response documents.
   * @param rows Maximum number of response documents.
   * @return A list containing all matching documents with their respective.
   */
  @GetMapping(path = "/hspobjects", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(value = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "search result", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "JSON processing error", responseCode = "500")
  })
  public ResponseEntity<String> findByQuery(
      @Parameter(
          description = "A simple search term or a query in RSQL syntax (see <a href=\n"
          + " \"https://github.com/jirutka/rsql-parser\">https://github.com/jirutka/rsql-parser</a>).\n"
          + " Fields in the RSQL query may be any Solr search fields.",
          example = "settlement-search==\"Wolfenbüttel\";repository-search==\"Herzog August Bibliothek\""
      )
      @RequestParam(
          name = "q",
          required = true
      ) final String q,
      @Parameter(
          description = "A filter query as JSON object. The JSON object key correspond with facet field names.\n"
          + " Entry values may have two forms: <br>\n"
          + " - For multivalued string filters, JSON values are string arrays, e.g. {"
          + " \"settlement-facet\": [\"Berlin\", \"Leipzig\"] }<br>\n"
          + " - For range filters, JSON values are object containing minimum and maximum value as well\n"
          + " as a flag for including documents without this filter value, e.g. { \"width-facet\": {"
          + " \"from\": 10, \"to\": 20, \"missing\": false }} <br>\n\n"
          + " For origin dates, the filter query is a special case of the range filter: <br>\n"
          + " - name / key is \"orig-date-facet\", filter queries include the two fields\n"
          + " orig-date-from-facet and orig-date-to-facet<br>\n"
          + " - filter has an addition boolean field \"exact\" that indicates that both\n"
          + " \"orig-date-from-facet\" and \"orig-date-to-facet\" of a document have values between \"from\"\n"
          + " and \"to\"",
          example = "{ \"settlement-facet\": [\"Wolfenbüttel\", \"Berlin\"], \"repository-facet\": [\"Herzog August Bibliothek\"] }")
      @RequestParam(
          name = "fq",
          required = false
      ) final String fq,
      @Parameter(
          description = "Indicates whether the term should be highlighted in the response documents.")
      @RequestParam(
          name = "hl",
          required = false,
          defaultValue = "true"
      ) final boolean hl,
      @Parameter(
          description = "Start index for the response documents."
      )
      @RequestParam(
          name = "start",
          required = false,
          defaultValue = "0"
      ) final long start,
      @Parameter(
          description = "Maximum number of response documents."
      )
      @RequestParam(
          name = "rows",
          required = false,
          defaultValue = "10"
      ) final long rows,
      @Parameter(
          description = "Sort logic.",
          schema = @Schema(implementation = String.class, allowableValues = {"ms-identifier-asc, ms-identifier-desc, orig-date-asc, orig-date-desc, score-desc"})
      )
      @RequestParam(
          name = "sort",
          required = false,
          defaultValue = "score-desc"
      ) final SortField sort
  ) {
    logger.info(
        "GET request for /hspobjects with params q: {}, fq: {}, hl: {}, start: {}, rows: {}, sort: {}", q, fq,
        hl, start, rows, sort);

    Set<String> searchFields = new HashSet<>();
    Result<List<String>> result;
    AbstractParams<?> params;
    String query;
    try {
      query = StringToQueryConverter.convert(q, searchFields);
      params = new QuerySearchParams();
    } catch (QuerySyntaxException ex) {
      query = q;
      params = new TermSearchParams();
    }
    params.setCollapse(true);
    params.setFacets(this.facetFields);
    params.setFilterQueries(filterConverter.convert(fq));
    params.setHighlightSnippetCount(hspConfig.getSnippetCount());
    params.setQueryOrSearchTerm(query);
    params.setRows(rows);
    params.setSearchFields(searchFields.toArray(new String[searchFields.size()]));
    params.setSortPhrase(sort.getSortPhrase());
    params.setStart(start);
    params.setStats(this.statsFields);
    result = searchService.findHspObjectGroupIds(params);
    if (result.getPayload() != null && result.getPayload().size() > 0) {
      final QuerySearchParams compParams = getGroupCompletionParamsBySearchParams(params, result.getPayload(), hl);
      final Result<List<HspObjectGroup>> completeResult = searchService.findHspObjectGroups(compParams);
      final Result<List<HspObjectGroup>> mergedResult = mergeResults(result, completeResult);
      return new ResponseEntity<String>(toJson(mergedResult), HttpStatus.OK);
    } else {
      return new ResponseEntity<String>(toJson(new Result<>(new ArrayList<>(), result.getMetadata())), HttpStatus.OK);
    }
  }

  /**
   * Get HTML rendering of HSP description TEI fulltext.
   *
   * @param id HSP description id
   * @return HTML rendering of TEI fulltext for consumption in HSP webapp.
   */
  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_XML_VALUE), description = "the rendered HTML", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no TEI was found for the given Id to render", responseCode = "404"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If the result is not unique", responseCode = "500")
  })
  @GetMapping(path = "/hspfulltext/{id}", produces = MediaType.TEXT_HTML_VALUE)
  public String getFulltextByDescriptionId(@PathVariable String id) {
    logger.info("GET request for /hspfulltext/{id} id: {}", id);
    if (ObjectUtils.isEmpty(id)) {
      throw ExceptionFactory.getException(ExceptionType.MISSING_PARAMETER,
          "The required field 'id' is missing");
    }
    final TermSearchParams params = new TermSearchParams.Builder()
        .withQueryOrSearchTerm(String.format("\"%s\"", id))
        .withRows(2)
        .withSearchFields(SearchField.ID.getName())
        .build();
    final List<String> renderings = searchService.findHspDescriptionFulltext(params);
    if (CollectionUtils.isEmpty(renderings)) {
      throw ExceptionFactory.getException(ExceptionType.NO_FULLTEXT_FOUND,
          String.format("No fulltext found for description id %s.", id));
    }
    if (renderings.size() > 1) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT,
          String.format("Multiple fulltext found for description id %s.", id));
    }
    return renderings.get(0);
  }

  /**
   * Get the original TEI XML document.
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
    logger.info("GET request for /tei/{id} id: {}", id);
    if (ObjectUtils.isEmpty(id)) {
      throw ExceptionFactory.getException(ExceptionType.MISSING_PARAMETER,
          "The required field 'id' is missing");
    }
    final TermSearchParams params = new TermSearchParams.Builder()
        .withQueryOrSearchTerm(String.format("\"%s\"", id))
        .withSearchFields(SearchField.ID.getName())
        .withRows(2)
        .build();

    List<String> teiDocs = searchService.findHspTEIDocuments(params);
    if (teiDocs == null || teiDocs.isEmpty()) {
      throw ExceptionFactory.getException(ExceptionType.NO_TEI_DOCUMENT_FOUND,
          String.format("No tei xml document found for id %s.", id));
    }
    if (teiDocs.size() > 1) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT,
          String.format("Multiple tei xml documents found for id %s.", id));
    }
    return teiDocs.get(0);
  }

  @GetMapping(path = "/tei/", produces = MediaType.TEXT_XML_VALUE)
  @Hidden
  @Order(Ordered.LOWEST_PRECEDENCE)
  public String getXmlDocumentById() {
    throw ExceptionFactory.getException(ExceptionType.MISSING_PARAMETER,
        "The required field 'id' is missing");
  }
  
  private QuerySearchParams getGroupCompletionParamsBySearchParams(final AbstractParams<?> sourceParams, final Collection<String> groupIds, final boolean highlight) {
    final QuerySearchParams targetParams = new QuerySearchParams.Builder()
        .withGrouping(true)
        .withQueryOrSearchTerm(SearchField.GROUP_ID.getName() + ":(" + String.join(" ", groupIds) + ")")
        .withRows(Integer.MAX_VALUE)
        .withSearchFields(sourceParams.getSearchFields())
        .withStart(0)
        .build();
    /* prepare highlighting */
    if(highlight) {
      targetParams.setHighlight(true);
      targetParams.setHighlightFields(sourceParams.getSearchFields());
      targetParams.setHighlightQueryParser(sourceParams.getQueryParser());
      targetParams.setHighlightQuery(sourceParams.getQuery());
      targetParams.setHighlightSnippetCount(hspConfig.getSnippetCount());
    }
    return targetParams;
  }
  
  private String toJson(final Result<?> re) {
    try {
      return mapper.writeValueAsString(re);
    } catch (JsonProcessingException e) {
      throw ExceptionFactory.getException(ExceptionType.JSON_PROCESSING_ERROR, "Error while writing result JSON.");
    }
  }
  
  private Result<List<HspObjectGroup>> mergeResults(final Result<List<String>> resultWithFacets, final Result<List<HspObjectGroup>> resultWithGroupData) {
    final Map<String, HspObjectGroup> mappedResultData = mapById(resultWithGroupData.getPayload());
    
    /* filter for object groups that are part of resultWithGroupData i. e. that ones who got a HspObject */
    final List<HspObjectGroup> resultData = resultWithFacets.getPayload().stream()
      .filter(key -> mappedResultData.containsKey(key))
      .map(key -> mappedResultData.get(key))
      .collect(Collectors.toList());
    final long resultNumGroupsFound = resultWithFacets.getMetadata().getNumGroupsFound();
    final long resultStart = resultWithFacets.getMetadata().getStart();
    final long resultRows = resultWithFacets.getMetadata().getRows();
    final MetaData resultMetaData = new MetaData.Builder()
        .withNumGroupsFound(resultNumGroupsFound)
        .withRows(resultRows)
        .withStart(resultStart)
        .build();
    resultMetaData.setFacets(Map.copyOf(resultWithFacets.getMetadata().getFacets()));
    resultMetaData.setStats(Map.copyOf(resultWithFacets.getMetadata().getStats()));
    resultMetaData.setHighlighting(Map.copyOf(resultWithGroupData.getMetadata().getHighlighting()));
    
    return new Result<List<HspObjectGroup>>(resultData, resultMetaData);
  }
  
  private Map<String, HspObjectGroup> mapById(List<HspObjectGroup> items) {
    return items.stream()
      .collect(Collectors.toMap(item -> item.getGroupId(), item -> item));
  }
}
