package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.TEIService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/tei")
@RestController
@Tag(name = "TEI", description = "TEI for KODs and Descriptions")
public class TEIController extends BaseEntityController<String> {

  @Autowired
  public TEIController(final TEIService baseService, final HspConfig hspConfig, final HighlightConfig highlightConfig) {
    super(baseService, hspConfig, highlightConfig);
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
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If the result is not unique", responseCode = "500")})
  @GetMapping(path = "/{id}", produces = MediaType.TEXT_XML_VALUE)
  public String getXmlDocumentById(@NotBlank @PathVariable String id) {
    final Result<List<String>> result = byId(id, new DisplayField[]{ DisplayField.TEI_DOCUMENT });
    return result.getPayload()
        .get(0);
  }
}
