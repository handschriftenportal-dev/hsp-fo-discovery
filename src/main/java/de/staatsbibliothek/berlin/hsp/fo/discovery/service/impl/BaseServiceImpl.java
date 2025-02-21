package de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.DiscoveryRepository;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.SearchParams2SolrParamsConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.SpellcheckHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseServiceImpl<T> implements BaseService<T> {

  protected DiscoveryRepository discoveryRepository;
  protected HighlightConfig highlightConfig;
  protected final Map<String, Object> typeFilter;
  private final Class<T> genericType;
  private SearchParams2SolrParamsConverter searchParams2SolrParamsConverter;

  @Autowired
  public void setHighlightConfig(final HighlightConfig highlightConfig) {
    this.highlightConfig = highlightConfig;
  }

  @Autowired
  public void setSolrRepository(final DiscoveryRepository discoveryRepository) {
    this.discoveryRepository = discoveryRepository;
  }

  @Autowired
  public void setSearchParams2SolrParamsConverter(final SearchParams2SolrParamsConverter searchParams2SolrParamsConverter) {
    this.searchParams2SolrParamsConverter = searchParams2SolrParamsConverter;
  }

  public BaseServiceImpl(final Map<String, Object> typeFilter, final Class<T> clazz) {
    this.genericType = clazz;
    this.typeFilter = typeFilter;
  }

  @Override
  public Result<List<T>> find(SearchParams searchParams) {
    QueryResponse queryResponse = search(searchParams);
    Result<List<T>> result = extractResult(queryResponse);

    if (searchParams.getRows() > 0 && CollectionUtils.isEmpty(result.getPayload()) && searchParams.useSpellCorrection()) {
      final String spellCorrectedTerm = getSpellCorrection(result, searchParams);
      if(StringUtils.isNotEmpty(spellCorrectedTerm)) {
        searchParams.setPhrase(spellCorrectedTerm);
        // be careful to avoid recursion
        searchParams.useSpellCorrection(false);
        result = find(searchParams);
      }
    }
    return result;
  }

  @Override
  public Map<String, Object> getTypeFilter() {
    return typeFilter;
  }

  protected String getSpellCorrection(final Result result, final SearchParams searchParams) {
    if(result != null && result.getMetadata() != null && StringUtils.isNotEmpty(result.getMetadata().getSpellCorrectedTerm())) {
      return SpellcheckHelper.applySpellCorrection(searchParams.getPhrase(), result.getMetadata().getSpellCorrectedTerm());
    }
    return StringUtils.EMPTY;
  }

  @Override
  public String getEntityName() {
    return this.genericType.getSimpleName();
  }

  protected Result<List<T>> extractResult(final QueryResponse queryResponse) {
    List<T> payload = QueryResponse2ResponseEntityConverter.extract(queryResponse, genericType);
    MetaData metadata = QueryResponse2ResponseEntityConverter.extractMetadata(queryResponse, highlightConfig);
    return new Result<>(payload, metadata);
  }

  @Override
  public MetaData findMetaData(final SearchParams searchParams) {
    final QueryResponse response = search(searchParams);
    return QueryResponse2ResponseEntityConverter.extractMetadata(response, highlightConfig);
  }

  protected QueryResponse search(final SearchParams searchParams) {
    final SolrParams solrParams = searchParams2SolrParamsConverter.convert(searchParams, highlightConfig);
    return discoveryRepository.findByQuery(solrParams);
  }

  /**
   * Generates a filter query for the given {@code facetField} and {@code values}
   * @param facetField the field that the filter query should be applied to
   * @param values the values that the {@code facetField} should match1
   * @return a map containing the generated filter query as its first entry
   */
  public static Map<String, String> generateFilter(final FacetField facetField, final List<String> values) {
    if(CollectionUtils.isEmpty(values)) {
      return Collections.emptyMap();
    }
    final String concatenated = values.stream().map(v -> String.format("\"%s\"", v)).collect(Collectors.joining(" OR "));
    return Map.of(String.format("%s:(%s)", facetField.getName(), concatenated), facetField.getName());
  }

  /**
   * Generates multiple filter queries for the given map of entries containing {@code facetField} and {@code values}
   * @param values a map within each entry represents the facetField and its associated values
   * @return a map containing the generated filter queries
   */
  public static Map<String, String> generateFilters(final Map<FacetField, List<String>> values) {
    if(CollectionUtils.isEmpty(values.entrySet())) {
      return Collections.emptyMap();
    }
    final Map<String, String> result = new HashMap<>();

    for(Map.Entry<FacetField, List<String>> entry : values.entrySet()) {
      result.putAll(generateFilter(entry.getKey(), entry.getValue()));
    }
    return result;
  }
}
