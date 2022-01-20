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
package de.staatsbibliothek.berlin.hsp.fo.discovery.infrastructure.persistence.solr;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.ExceptionType;

/**
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Repository
public class SolrRepository implements IRepository{

  private static final Logger logger = LoggerFactory.getLogger(SolrRepository.class);

  private final SolrClient solrClient;
  private final SolrConfig solrConfig;

  public SolrRepository(final SolrConfig solrConfig) {
    this.solrConfig = solrConfig;
    this.solrClient = new HttpSolrClient.Builder(solrConfig.getUrl()).build();
  }

  /**
   * Queries Solr for the given term
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

      final QueryResponse queryResponse = this.solrClient.query(solrConfig.getCore(), solrParams);

      if (queryResponse.getException() != null) {
        throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST_EXCEPTION,
            queryResponse.getException().getMessage());
      }
      return queryResponse;
    } catch (RemoteSolrException | SolrServerException | IOException ex) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST_EXCEPTION,
          ex.getLocalizedMessage());
    }
  }
}
