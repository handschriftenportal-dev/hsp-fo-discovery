package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.adapter.GNDEntity;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("authority-files")
@Tag(name = "Authority Files")
@Validated
public class AuthorityFileController {

  private final AuthorityFileService authorityFileService;

  @Autowired
  public AuthorityFileController(final AuthorityFileService authorityFileService) {
    this.authorityFileService = authorityFileService;
  }

  @ApiResponses(value = {
      @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), description = "The authority file for the given Id", responseCode = "200"),
      @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE), description = "If no authority file was found for the given Id", responseCode = "404")})
  @GetMapping(value = {"/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GNDEntity> getById(@PathVariable @NotBlank final String id) {
    final GNDEntity[] result = authorityFileService.findById(id, GNDEntity[].class);

    if (ArrayUtils.isEmpty(result)) {
      throw ExceptionFactory.getException(ExceptionType.NOT_FOUND, String.format("No authority file with id %s found.", id));
    }
    if (result.length > 1) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT, String.format("Multiple authority files with id %s found.", id));
    }
    return new ResponseEntity<>(result[0], HttpStatus.OK);
  }
}