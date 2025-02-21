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
package de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.SolrConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.DiscoveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class SolrRepository implements DiscoveryRepository {

  private static final Logger logger = LoggerFactory.getLogger(SolrRepository.class);
  private final SolrClient solrClient;
  private final String coreName;

  public SolrRepository(final SolrConfig solrConfig) {
    this.solrClient = new Http2SolrClient.Builder(solrConfig.getUrl())
        .withRequestTimeout(solrConfig.getTimeout(), TimeUnit.MILLISECONDS)
        .build();
    this.coreName = solrConfig.getCore();
  }

  /**
   * Queries all documents for the given term
   *
   * @param solrParams the solr params to specify the query
   * @return the solr response
   */
  @Override
  public QueryResponse findByQuery(final SolrParams solrParams) {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Query Solr with the following params: {}", solrParams);
      }
      final QueryResponse queryResponse = this.solrClient.query(coreName, solrParams, SolrRequest.METHOD.POST);

      if (queryResponse.getException() != null) {
        throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, queryResponse.getException()
            .getMessage());
      }
      return queryResponse;
    } catch (RemoteSolrException | SolrServerException | IOException ex) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, ex.getLocalizedMessage());
    }
  }

  /**
   * Queries for all fields
   *
   * @return the response containing the field information
   */
  @Override
  public SchemaResponse.FieldsResponse getFieldInformation() {
    final SchemaRequest.Fields fieldsRequest = new SchemaRequest.Fields();
    try {
      return fieldsRequest.process(solrClient, coreName);

    } catch (IOException | SolrServerException e) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, e.getLocalizedMessage());
    }
  }

  /**
   * Queries for all field types
   *
   * @return the response containing the field type information
   */
  @Override
  public SchemaResponse.FieldTypesResponse getFieldTypeInformation() {
    final SchemaRequest.FieldTypes typesRequest = new SchemaRequest.FieldTypes();
    try {
      return typesRequest.process(solrClient, coreName);
    } catch (IOException | SolrServerException e) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, e.getLocalizedMessage());
    }
  }

  /**
   * Queries for all enum types
   *
   * @return the response containing the enum type information
   */
  @Override
  public SolrResponseBase getEnumValues() {
    final FileRequest fileRequest = new FileRequest();

    fileRequest.setFileName("enumsConfig.xml");
    try {
      return fileRequest.process(solrClient, coreName);
    } catch (IOException | SolrServerException e) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, e.getLocalizedMessage());
    }
  }
}