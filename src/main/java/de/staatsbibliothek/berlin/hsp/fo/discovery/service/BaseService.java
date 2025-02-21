package de.staatsbibliothek.berlin.hsp.fo.discovery.service;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

public interface BaseService<T> {
  MetaData findMetaData(SearchParams searchParams);
  Map<String, Object> getTypeFilter();
  Result<List<T>> find(final SearchParams searchParams);
  String getEntityName();

  /**
   * Encapsulates parameters
   */
  @AllArgsConstructor
  @Data
  @Builder(setterPrefix = "with", toBuilder = true)
  @NoArgsConstructor
  class SearchParams {
    //ToDo default values can be moved to solr config
    private boolean collapse;
    private DisplayField[] displayFields;
    private List<String> facets;
    @Builder.Default
    private long facetMinCount = 0;
    @Builder.Default
    private List<String> facetTermsExcluded = List.of(HspType.HSP_DIGITIZED.getValue());
    @Builder.Default
    @Getter(AccessLevel.NONE)
    private boolean includeMissingFacet = true;
    private Map<String, String> filterQueries;
    private boolean grouping;
    private boolean highlight;
    private List<String> highlightFields;
    private String highlightQuery;
    private QueryType highlightQueryType;
    private String phrase;
    private String phraseExtended;
    private String query;
    @Builder.Default
    private final QueryOperator queryOperator = QueryOperator.AND;
    private long rows;
    private List<String> searchFields;
    private String sortPhrase;
    @Accessors(fluent = true)
    private boolean useSpellCorrection;
    private long start;
    private List<String> stats;
    public static final String QUERY_PARSER = "edismax";

    public enum QueryOperator {
      AND,
      OR
    }

    public boolean includeMissingFacet() {
      return includeMissingFacet;
    }
  }
}
