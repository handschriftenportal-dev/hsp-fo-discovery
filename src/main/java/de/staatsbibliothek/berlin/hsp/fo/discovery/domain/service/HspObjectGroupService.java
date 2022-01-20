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
package de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service;

import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.infrastructure.persistence.solr.IRepository;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.Result;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Service
public class HspObjectGroupService implements IHspObjectGroupService {

  private IRepository solrRepository;
  @Autowired
  public HspObjectGroupService(final IRepository repository) {
    this.solrRepository = repository;
  }

  /**
   * Queries solr for documents
   *
   * @param the search params to use
   * @return the Solr {@link QueryResponse}
   */
  @Override
  public Result<List<HspObjectGroup>> findHspObjectGroups(final AbstractParams<?> searchParams) {
    final SolrParams solrParams = SearchParams2SolrParamsConverter.convert(searchParams);
    final QueryResponse response = solrRepository.findByQuery(solrParams);

    return ResponseHelper.createResponseWithHspGroups(response, searchParams.getStart(), searchParams.getRows());
  }
  
  /**
   * Queries solr for documents
   *
   * @param the search params to use
   * @return the Solr {@link QueryResponse}
   */
  @Override
  public Result<List<String>> findHspObjectGroupIds(final AbstractParams<?> searchParams) {
    final SolrParams solrParams = SearchParams2SolrParamsConverter.convert(searchParams);
    final QueryResponse response = solrRepository.findByQuery(solrParams);

    return ResponseHelper.createResponseWithHspGroupIds(response, searchParams.getStart(), searchParams.getRows());
  }

  @Override
  public List<String> findHspDescriptionFulltext(final AbstractParams<?> searchParams) {
    final SolrParams solrParams = SearchParams2SolrParamsConverter.convert(searchParams);
    final QueryResponse response = solrRepository.findByQuery(solrParams);
    return ResponseHelper.extractHspDescriptionFulltextFromSolrResponse(response);
  }
  
  @Override
  public List<String> findHspTEIDocuments(final AbstractParams<?> searchParams) {
    final SolrParams solrParams = SearchParams2SolrParamsConverter.convert(searchParams);
    final QueryResponse response = solrRepository.findByQuery(solrParams);
    return ResponseHelper.extractTeiDocumentsFromSolrResponse(response);
  }
}
