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
package de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.Query2SolrQueryConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryParser;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService.SearchParams.QueryOperator.OR;

@Service
public class HspObjectGroupServiceImpl extends BaseServiceImpl<HspObjectGroup> implements HspObjectGroupService {

  public HspObjectGroupServiceImpl() {
    super(Map.of(FacetField.TYPE.getName(), List.of(
        HspType.HSP_OBJECT.getValue(),
        HspType.HSP_DESCRIPTION.getValue(),
        HspType.HSP_DESCRIPTION_RETRO.getValue())
    ), HspObjectGroup.class);
  }

  private static Result<List<HspObjectGroup>> mergeResults(final Result<List<String>> resultWithFacets, final Result<List<HspObjectGroup>> resultWithGroupData) {
    final Map<String, HspObjectGroup> mappedResultData = mapById(resultWithGroupData.getPayload());

    /* filter for object groups that are part of resultWithGroupData i.e. that ones who got a HspObject */
    final List<HspObjectGroup> resultData = resultWithFacets.getPayload()
        .stream()
        .filter(mappedResultData::containsKey)
        .map(mappedResultData::get)
        .toList();

    final MetaData resultMetaData = resultWithFacets.getMetadata()
        .toBuilder()
        .withHighlighting(Map.copyOf(resultWithGroupData.getMetadata()
            .getHighlighting()))
        .build();

    return new Result<>(resultData, resultMetaData);
  }

  private static Map<String, HspObjectGroup> mapById(List<HspObjectGroup> items) {
    return items.stream()
        .collect(Collectors.toMap(HspObjectGroup::getGroupId, item -> item));
  }

  public static SearchParams getGroupCompletionParamsBySearchParams(final SearchParams sourceParams, final Collection<String> groupIds) {
    final String query = Query2SolrQueryConverter.createEmbeddedQueryWithQueryParser(QueryParser.EDISMAX, "group-id-search" + ":(" + String.join(" ", groupIds) + ")");
    return SearchParams.builder()
        .withGrouping(true)
        .withHighlight(sourceParams.isHighlight())
        .withHighlightFields(sourceParams.getHighlightFields())
        .withHighlightQuery(sourceParams.getHighlightQuery())
        .withHighlightQueryType(sourceParams.getHighlightQueryType())
        .withQuery(query)
        .withQueryOperator(OR)
        .withRows(Integer.MAX_VALUE)
        // ToDo original search fields need to be set for highlighting. Could be done somewhere else
        .withSearchFields(sourceParams.getSearchFields())
        .withStart(0)
        .build();
  }

  /**
   * Queries solr for documents
   *
   * @param searchParams the search params to use
   * @return a {@link Result} containing the matching {@link HspObjectGroup}s
   */
  @Override
  public Result<List<HspObjectGroup>> find(final SearchParams searchParams) {
    final boolean useHighlighting = searchParams.isHighlight();
    searchParams.setHighlight(false);
    Result<List<String>> result = findHspObjectGroupIds(searchParams);

    if (searchParams.getRows() > 0 && CollectionUtils.isEmpty(result.getPayload()) && searchParams.useSpellCorrection()) {
      final String spellCorrectedTerm = getSpellCorrection(result, searchParams);
      if(StringUtils.isNotEmpty(spellCorrectedTerm)) {
        searchParams.setPhrase(spellCorrectedTerm);
        searchParams.useSpellCorrection(false);
        result = findHspObjectGroupIds(searchParams);
      }
    }

    if (CollectionUtils.isNotEmpty(result.getPayload())) {
      searchParams.setHighlight(useHighlighting);
      final SearchParams compParams = getGroupCompletionParamsBySearchParams(searchParams, result.getPayload());
      final QueryResponse response = search(compParams);
      final List<HspObjectGroup> payload = QueryResponse2ResponseEntityConverter.getHSPObjectGroups(response);
      final MetaData metaData = QueryResponse2ResponseEntityConverter.extractMetadata(response, highlightConfig);
      return mergeResults(result, new Result<>(payload, metaData));
    }
    return new Result<>(List.of(), result.getMetadata());
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
    final MetaData metaData = QueryResponse2ResponseEntityConverter.extractMetadata(response, highlightConfig);

    return new Result<>(payload, metaData);
  }
}