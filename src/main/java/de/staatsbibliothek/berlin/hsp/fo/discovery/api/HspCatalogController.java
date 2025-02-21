package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.DisplayFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.staatsbibliothek.berlin.hsp.fo.discovery.util.Constants.*;

@RequestMapping("/catalogs")
@RestController
@Slf4j
@Tag(name = "Kataloge")
@Validated
public class HspCatalogController extends BaseEntityController<HspCatalog> {
  protected HspCatalogController(BaseService<HspCatalog> baseService, HspConfig hspConfig, HighlightConfig highlightConfig) {
    super(baseService, hspConfig, highlightConfig);
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = HspDescription.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "Catalog for the given id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no catalog was found for the given Id", responseCode = "404"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")
  })
  @GetMapping(value = {"/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public ResponseEntity<HspCatalog> getById(
      @PathVariable @NotBlank final String id,
      @Parameter(description = "Comma separated list of fields, that should be returned for the catalog", examples = {@ExampleObject(value = "id")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "displayFields", required = false) final List<String> displayFields) {
    final Result<List<HspCatalog>> result = byId(id, displayFields);
    return new ResponseEntity<>(result.getPayload()
        .get(0), HttpStatus.OK);
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "All Catalogs within the range of [start, start+rows]", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")})
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public ResponseEntity<Result<List<HspCatalog>>> getAll(
      @Parameter(description = "Comma separated list of attributes, that should be returned for the object", examples = {@ExampleObject(value = "id")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "fields", required = false)
      final List<String> fields,
      @Parameter(description = "Start index for the response documents.")
      @RequestParam(defaultValue = "0", name = "start", required = false)
      final long start,
      @Parameter(description = "Maximum number of response documents.")
      @RequestParam(defaultValue = "10", name = "rows", required = false)
      final long rows) {
      final SortField sort = SortField.PUBLISH_YEAR_DESC;
      final DisplayField[] filteredFields = DisplayFieldFilter.filterAndAddDisplaySuffix(fields);
      final BaseService.SearchParams params = BaseService.SearchParams.builder()
              .withDisplayFields(filteredFields)
              .withFilterQueries(filterConverter.convert(null, baseService.getTypeFilter()))
              .withSortPhrase(sort.getSortPhrase())
              .withRows(rows)
              .withStart(start)
              .build();
      final Result<List<HspCatalog>> result = baseService.find(params);
      return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping(value = {"/search"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(value = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "search result", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "JSON processing error", responseCode = "500")
  })
  public ResponseEntity<Result<List<HspCatalog>>> search(
      @Parameter(description = API_PARAM_DESCRIPTION_QUERY_WITH_EXTENDED, example = API_PARAM_EXAMPLE_QUERY)
      @RequestParam(name = "q") final String q,
      @Parameter(description = API_PARAM_DESCRIPTION_QUERY_FIELDS, examples = {@ExampleObject(value = "repository-search")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "qf", required = false) final List<String> qf,
      @Parameter(description = API_PARAM_DESCRIPTION_FILTER_QUERY, example = API_PARAM_EXAMPLE_FILTER_QUERY, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(name = "fq", required = false) final String fq,
      @Parameter(description = API_PARAM_DESCRIPTION_HIGHLIGHT)
      @RequestParam(name = "hl", required = false, defaultValue = "true")
      final boolean hl,
      @RequestParam(name = "start", required = false, defaultValue = "0") final long start,
      @Parameter(description = API_PARAM_DESCRIPTION_ROWS)
      @RequestParam(name = "rows", required = false, defaultValue = "10") final long rows,
      @Parameter(description = "Sort logic.", schema = @Schema(implementation = String.class, allowableValues = {"publish-year-asc", "publish-year-desc", "score-asc", "score-desc"}))
      @RequestParam(name = "sort", required = false, defaultValue = "publish-year-desc") final SortField sort) {
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withFacets(this.catalogFacetFields)
        .withFilterQueries(filterConverter.convert(fq, baseService.getTypeFilter()))
        .withHighlight(hl)
        .withPhrase(q)
        .withRows(rows)
        .withSearchFields(getSearchFieldsWithDefaults(qf))
        .withSortPhrase(sort.getSortPhrase())
        .withStart(start)
        .withStats(this.catalogStatsFields)
        .withUseSpellCorrection(true)
        .build();
    return new ResponseEntity<>(baseService.find(params), HttpStatus.OK);
  }
}