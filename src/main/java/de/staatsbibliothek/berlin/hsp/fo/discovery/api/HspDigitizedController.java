package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.DisplayFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.staatsbibliothek.berlin.hsp.fo.discovery.util.Constants.*;

@RequestMapping("/digitizeds")
@RestController
@Tag(name = "Digitalisate")
public class HspDigitizedController extends BaseEntityController<HspDigitized> {
  @Autowired
  public HspDigitizedController(BaseService<HspDigitized> baseService, HspConfig hspConfig, HighlightConfig highlightConfig) {
    super(baseService, hspConfig, highlightConfig);
  }

  @ApiResponses(value = {@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "All Digitizeds within the range of [start, start+rows]", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")})
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Result<List<HspDigitized>>> getAll(
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
    final Result<List<HspDigitized>> result =  all(filteredFields, start, rows);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiResponses(value = {@ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The Digitized for the given id", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "No KOD was found for the given Id", responseCode = "404"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "Something went terribly wrong", responseCode = "500")})
  @GetMapping(value = {"/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HspDigitized> getById(
      @PathVariable @NotBlank final String id,
      @Parameter(description = "Comma separated list of attributes, that should be returned for the object", examples = {@ExampleObject(value = "id")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "fields", required = false)
      final List<String> fields) {
    final Result<List<HspDigitized>> result = byId(id, fields);
    return new ResponseEntity<>(result.getPayload()
        .get(0), HttpStatus.OK);
  }

  @GetMapping(value = {"/search"}, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses(value = {@ApiResponse(content = @Content(schema = @Schema(implementation = Result.class), mediaType = MediaType.APPLICATION_JSON_VALUE), description = "search result", responseCode = "200"), @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "JSON processing error", responseCode = "500")})
  public ResponseEntity<Result<List<HspDigitized>>> search(
      @Parameter(description = "A search term or phrase. Only manifest URIs are supported currently.", example = "https://content.staatsbibliothek-berlin.de/dc/835110419/manifest")
      @RequestParam(name = "q") final String q,
      @Parameter(description = API_PARAM_DESCRIPTION_QUERY_FIELDS, examples = { @ExampleObject(value = "manifest-uri-search")}, explode = Explode.TRUE, style = ParameterStyle.SIMPLE)
      @RequestParam(defaultValue = "", name = "qf", required = false)
      final List<String> qf,
      @Parameter(description = API_PARAM_DESCRIPTION_START)
      @RequestParam(name = "start", required = false, defaultValue = "0")
      final long start, @Parameter(description = API_PARAM_DESCRIPTION_ROWS)
      @RequestParam(name = "rows", required = false, defaultValue = "10")
      final long rows) {
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withFacets(this.defaultFacetFields)
        .withPhrase(q)
        .withRows(rows)
        .withSearchFields(getSearchFieldsWithDefaults(qf))
        .withStart(start)
        .withStats(this.statsFields)
        .build();
    return new ResponseEntity<>(baseService.find(params), HttpStatus.OK);
  }
}
