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

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspBaseEntity;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspBaseEntityService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HspBaseEntityServiceImpl extends BaseServiceImpl implements HspBaseEntityService {
  @Override
  public Map<String, String> getTypeFilter() {
    final String types = Arrays.stream(HspType.values())
        .map(type -> String.format("\"%s\"", type.getValue()))
        .collect(Collectors.joining(" OR "));
    return Map.of("type-facet: (" + types + ")", FacetField.TYPE.getName());
  }

  @Override
  public Result<List<HspBaseEntity>> findHspBaseEntities(SearchParams searchParams) {
    final QueryResponse queryResponse = search(searchParams);
    final List<HspBaseEntity> payload = QueryResponse2ResponseEntityConverter.extract(queryResponse, HspBaseEntity.class);
    final MetaData metadata = QueryResponse2ResponseEntityConverter.extractMetadata(queryResponse);
    return new Result<>(payload, metadata);
  }
}
