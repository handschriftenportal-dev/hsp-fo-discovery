package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Field;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.InfoService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequestMapping("/info")
@RestController
@Tag(name = "Info")
public class InfoController {
  protected final BaseService<Void> baseService;
  protected final InfoService infoService;
  protected final String componentName;
  protected final String componentDescription;
  protected final String componentVersion;

  public InfoController(final BaseService<Void> baseService,
                        final InfoService infoService,
                        @Value("${info.component}") final String componentName,
                        @Value("${info.description}") final String componentDescription,
                        @Value("${info.version}") final String componentVersion) {
    this.baseService = baseService;
    this.infoService = infoService;
    this.componentName = componentName;
    this.componentDescription = componentDescription;
    this.componentVersion = componentVersion;
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "some general information about the service and its provided data", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getInfo() {
    final SearchParams searchParams = SearchParams.builder()
        .withRows(0)
        .withFacets(List.of(FacetField.TYPE.getName()))
        .withFacetTermsExcluded(Collections.emptyList())
        .withIncludeMissingFacet(false)
        .build();

    final MetaData metaData = baseService.findMetaData(searchParams);
    final Map<String, Long> itemCounts = metaData.getFacets()
        .get(FacetField.TYPE.getName());

    final JSONObject response = new JSONObject();
    response.put("component", componentName);
    response.put("description", componentDescription);
    response.put("version", componentVersion);
    response.put("items", itemCounts);
    return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "all available search fields", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")
  })
  @GetMapping(value = {"/fields"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Field>> getFields() {
    final List<Field> result = infoService.getFields();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}