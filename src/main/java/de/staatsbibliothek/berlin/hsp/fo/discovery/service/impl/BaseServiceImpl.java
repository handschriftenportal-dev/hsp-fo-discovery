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

package de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.SearchParams2SolrParamsConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.Repository;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import org.apache.commons.collections4.MapUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseServiceImpl implements BaseService {

  protected Repository solrRepository;

  @Autowired
  public void setSolrRepository(final Repository repository) {
    this.solrRepository = repository;
  }

  @Override
  public MetaData getMetaData(final SearchParams searchParams) {
    final QueryResponse response = search(searchParams);
    return QueryResponse2ResponseEntityConverter.extractMetadata(response);
  }

  protected QueryResponse search(final SearchParams searchParams) {
    mergeFilterQuery(searchParams);
    final SolrParams solrParams = SearchParams2SolrParamsConverter.convert(searchParams);
    return solrRepository.findByQuery(solrParams);
  }

  protected void mergeFilterQuery(final SearchParams searchParams) {
    if(MapUtils.isEmpty(searchParams.getFilterQueries())) {
      searchParams.setFilterQueries(getTypeFilter());
    } else if(!searchParams.getFilterQueries().containsKey(FacetField.TYPE.getName())) {
      searchParams.getFilterQueries().putAll(getTypeFilter());
    }
  }
}
