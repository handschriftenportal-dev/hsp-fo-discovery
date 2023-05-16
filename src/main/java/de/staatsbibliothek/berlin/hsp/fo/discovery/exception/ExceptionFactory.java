/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
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

  public static class ConfigurationException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public ConfigurationException(final String message) {
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

  public static class NotFoundException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public NotFoundException(final String message) {
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

  public static class ProcessingException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public ProcessingException(final String message) {
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

  public static class WrongParameterException extends RuntimeException {

    /**
     * id for serializing the object
     */
    private static final long serialVersionUID = 1L;

    public WrongParameterException(final String message) {
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
    switch (exType) {
      case CONFIGURATION:
        return new ConfigurationException(exMessage);
      case PROCESSING:
        return new ProcessingException(exMessage);
      case SOLR_REQUEST_EXCEPTION:
        return new SolrNotReachableException(exMessage);
      case NOT_FOUND:
        return new NotFoundException(exMessage);
      case NO_UNIQUE_RESULT:
        return new NoUniqueResultException(exMessage);
      case MISSING_PARAMETER:
        return new MissingParameterException(exMessage);
      case WRONG_QUERY_SYNTAX:
        return new QuerySyntaxException(exMessage);
      case WRONG_PARAMETER:
        return new WrongParameterException(exMessage);
    }
    return new RuntimeException(exMessage);
  }
}
