package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Data
public final class QueryParams {
  private Set<String> fields = new HashSet<>();
  private String query;
  private List<QueryToken> queryTokens;
  private QueryType queryType = QueryType.STANDARD;

  private QueryParams() {}

  public static QueryParams withSolrQueryParams(final Query2SolrQueryConverter.SolrQueryParams solrQueryParams) {
    final QueryParams queryParams = new QueryParams();
    queryParams.query = solrQueryParams.getQuery();
    queryParams.queryTokens = solrQueryParams.getTokens();
    queryParams.queryType = solrQueryParams.getQueryType();

    return queryParams;
  }

  public String[] getFieldsArray() {
    return CollectionUtils.emptyIfNull(fields).toArray(String[]::new);
  }
}
