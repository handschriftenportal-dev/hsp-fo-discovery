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

/**
 * A helper class for creating custom runtime exceptions
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class ExceptionFactory {
  
  private ExceptionFactory() {}

  public static class AuthorityFileServiceException extends RuntimeException {
    public AuthorityFileServiceException(final String message) {
      super(message);
    }
  }

  public static class ExtendedSearchException extends RuntimeException {
    public ExtendedSearchException(final String message) {
      super(message);
    }
  }

  public static class InvalidParamException extends RuntimeException {
    public InvalidParamException(final String message) {
      super(message);
    }
  }

  public static class MappingException extends RuntimeException {
    public MappingException(final String message) {
      super(message);
    }
  }

  public static class NotFoundException extends RuntimeException {
    public NotFoundException(final String message) {
      super(message);
    }
  }

  public static class NoUniqueResultException extends RuntimeException {
    public NoUniqueResultException(final String message) {
      super(message);
    }
  }

  public static class SolrNotReachableException extends RuntimeException {
    public SolrNotReachableException(final String message) {
      super(message);
    }
  }

  /**
   * Returns a runtime exception based on exceptionType and exceptionMessage
   * 
   * @param exType the type of the exception to be thrown
   * @param exMessage the message of the exception to be thrown
   * @return the runtime exception
   */
  public static RuntimeException getException(final ExceptionType exType, final String exMessage) {
    return switch (exType) {
      case AUTHORITY_FILE_EXCEPTION -> new AuthorityFileServiceException(exMessage);
      case EXTENDED_SEARCH -> new ExtendedSearchException(exMessage);
      case INVALID_PARAM -> new InvalidParamException(exMessage);
      case MAPPING -> new MappingException(exMessage);
      case NOT_FOUND -> new NotFoundException(exMessage);
      case NO_UNIQUE_RESULT -> new NoUniqueResultException(exMessage);
      case SOLR_REQUEST -> new SolrNotReachableException(exMessage);
    };
  }
}
