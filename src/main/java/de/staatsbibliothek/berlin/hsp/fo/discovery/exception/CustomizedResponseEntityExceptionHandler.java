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
package de.staatsbibliothek.berlin.hsp.fo.discovery.exception;

import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 */
@ControllerAdvice
@RestController
@Slf4j
public class CustomizedResponseEntityExceptionHandler {

  @ExceptionHandler({
      AuthorityFileServiceException.class
  })
  public final ResponseEntity<String> handleAuthorityFileException(final RuntimeException ex) {
    log.error("Error while fetching authority file ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({
      ExtendedSearchException.class
  })
  public final ResponseEntity<String> handleExtendedSearchException(final RuntimeException ex) {
    log.error("Syntax error in extended search expression ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({
      InvalidParamException.class
  })
  public final ResponseEntity<String> handleInvalidParamException(final RuntimeException ex) {
    log.error("Invalid parameter ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({
      MappingException.class,
  })
  public final ResponseEntity<String> handleMappingException(final Exception ex) {
    log.warn("Error while mapping data ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({
      MissingPathVariableException.class,
  })
  public final ResponseEntity<String> handleMissingPathVariableException(final Exception ex) {
    log.warn("Path variable is missing ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({
      NotFoundException.class
  })
  public final ResponseEntity<String> handleNotFoundException(final Exception ex) {
    log.warn("Resource not found ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({
      NoUniqueResultException.class
  })
  public final ResponseEntity<String> handleNoUniqueException(final Exception ex) {
    log.warn("Identifier for requested resource not unique ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({
      SolrNotReachableException.class
  })
  public final ResponseEntity<String> handleSolrNotReachableException(final Exception ex) {
    log.warn("Error while requesting Solr ", ex);
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({
      Exception.class
  })
  public final ResponseEntity<String> handleUncaughtException(final RuntimeException ex) {
    log.error("An unknown error occurred ", ex);
    return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
  }
}