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

/**
 * A helper class for creating custom runtime exceptions
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class ExceptionFactory {
  
  private ExceptionFactory() {};

  public static class JSONProcessingException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public JSONProcessingException(final String message) {
      super(message);
    }
  }
  
  public static class SolrNotReachableException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public SolrNotReachableException(final String message) {
      super(message);
    }
  }

  public static class FulltextNotFoundException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public FulltextNotFoundException(final String message) {
      super(message);
    }
  }

  public static class HspGroupNotFoundException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public HspGroupNotFoundException(final String message) {
      super(message);
    }
  }

  public static class TeiDocumentNotFoundException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public TeiDocumentNotFoundException(final String message) {
      super(message);
    }
  }

  public static class MissingParameterException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public MissingParameterException(final String message) {
      super(message);
    }
  }

  public static class NoUniqueResultException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public NoUniqueResultException(final String message) {
      super(message);
    }
  }

  public static class QuerySyntaxException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public QuerySyntaxException(final String message) {
      super(message);
    }
  }

  /**
   * Returns a runtime exception based on exceptionType and exceptionMessage
   * 
   * @param exType the type of the exception to be thrown
   * @param Exceptionmessage the message of the exception to be thrown
   * @return the runtime exception
   */
  public static RuntimeException getException(final ExceptionType exType, final String exMessage) {
    switch (exType) {
      case JSON_PROCESSING_ERROR:
        return new JSONProcessingException(exMessage);
      case SOLR_REQUEST_EXCEPTION:
        return new SolrNotReachableException(exMessage);
      case NO_FULLTEXT_FOUND:
        return new FulltextNotFoundException(exMessage);
      case NO_HSP_OBJECT_FOUND:
        return new HspGroupNotFoundException(exMessage);
      case NO_TEI_DOCUMENT_FOUND:
        return new TeiDocumentNotFoundException(exMessage); 
      case NO_UNIQUE_RESULT:
        return new NoUniqueResultException(exMessage);
      case MISSING_PARAMETER:
        return new MissingParameterException(exMessage);
      case WRONG_QUERY_SYNTAX:
        return new QuerySyntaxException(exMessage);
    }
    return new RuntimeException(exMessage);
  }
}
