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
package de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler {

  @ExceptionHandler(ExceptionFactory.NoUniqueResultException.class)
  public final ResponseEntity<String> handleJSONProcessingErrorException(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
  @ExceptionHandler(ExceptionFactory.SolrNotReachableException.class)
  public final ResponseEntity<String> handleSolrNotReachableExceptions(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ExceptionFactory.HspGroupNotFoundException.class)
  public final ResponseEntity<String> handleNoHspGroupFoundExceptions(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ExceptionFactory.MissingParameterException.class)
  public final ResponseEntity<String> handleMissingParameterExceptions(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ExceptionFactory.QuerySyntaxException.class)
  public final ResponseEntity<String> handleWrongQuerySyntaxEception(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ExceptionFactory.FulltextNotFoundException.class)
  public final ResponseEntity<String> handleFulltextNotFoundException(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ExceptionFactory.TeiDocumentNotFoundException.class)
  public final ResponseEntity<String> handleTeiDocumentNotFoundException(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ExceptionFactory.JSONProcessingException.class)
  public final ResponseEntity<String> handleNoUniqueResultException(final Exception ex,
      final WebRequest request) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
