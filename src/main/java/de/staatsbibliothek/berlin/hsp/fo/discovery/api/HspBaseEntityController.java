package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.util.IdTypeMatcher;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspBaseEntity;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/any")
@RestController
@Tag(name = "Any", description = "Can be either a Kulturobjektdokument, Beschreibung or Digitalisat")
@Validated
public class HspBaseEntityController extends BaseEntityController<HspBaseEntity> {

  private final AuthorityFileService authorityFileService;

  @Autowired
  public HspBaseEntityController(BaseService<HspBaseEntity> baseService, final AuthorityFileService authorityFileService, final HspConfig hspConfig, final HighlightConfig highlightConfig) {
    super(baseService, hspConfig, highlightConfig);
    this.authorityFileService = authorityFileService;
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The HspBaseEntity for the given id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If something went terribly wrong", responseCode = "500")
  })
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HspBaseEntity> getById(@PathVariable @NotBlank final String id) {
    final IdTypeMatcher.IdType idType = IdTypeMatcher.match(id);
    switch (idType) {
      case HSP_OBJECT:
        final Result<List<HspBaseEntity>> entities = byId(id, new DisplayField[]{DisplayField.ID, DisplayField.TYPE, DisplayField.GROUP_ID});
        return new ResponseEntity<>(entities.getPayload().getFirst(), HttpStatus.OK);
      case AUTHORITY_FILE:
        final HspBaseEntity[] result = authorityFileService.findById(id, HspBaseEntity[].class);
        checkIdResult(new Result<>(List.of(result)), id);
        result[0].setType(HspType.HSP_AUTHORITY_FILE.getValue());
        return new ResponseEntity<>(result[0], HttpStatus.OK);
      case UNKNOWN:
        throw ExceptionFactory.getException(ExceptionType.INVALID_PARAM, "Type for the given id not found");
    }
    return null;
  }
}