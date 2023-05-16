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

import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Primary
@Service
public class HspObjectGroupServiceImpl extends BaseServiceImpl implements HspObjectGroupService {

  /**
   * Queries solr for documents
   *
   * @param searchParams the search params to use
   * @return a {@link Result} containing the matching {@link HspObjectGroup}s
   */
  @Override
  public Result<List<HspObjectGroup>> findHspObjectGroups(final SearchParams searchParams) {
    final QueryResponse response = search(searchParams);
    final List<HspObjectGroup> payload = QueryResponse2ResponseEntityConverter.getHSPObjectGroups(response);
    final MetaData metaData = QueryResponse2ResponseEntityConverter.extractMetadata(response);

    return new Result<>(payload, metaData);
  }

  /**
   * Queries solr for documents
   *
   * @param searchParams the search params to use
   * @return a {@link Result} containing the matching {@link HspObjectGroup}'s IDs
   */
  @Override
  public Result<List<String>> findHspObjectGroupIds(final SearchParams searchParams) {
    final QueryResponse response = search(searchParams);
    final List<String> payload = QueryResponse2ResponseEntityConverter.getHspObjectGroupIds(response);
    final MetaData metaData = QueryResponse2ResponseEntityConverter.extractMetadata(response);

    return new Result<>(payload, metaData);
  }

  @Override
  public Map<String, String> getTypeFilter() {
    return Collections.emptyMap();
  }
}
