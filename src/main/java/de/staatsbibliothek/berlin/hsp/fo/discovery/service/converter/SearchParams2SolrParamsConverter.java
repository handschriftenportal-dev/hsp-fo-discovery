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
package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class SearchParams2SolrParamsConverter {

  private static final String FIELD_NAME_DEF_TYPE = "defType";
  private static final String FIELD_NAME_FACET = "facet";
  private static final String FIELD_NAME_FACET_FIELD = "facet.field";
  private static final String FIELD_NAME_FACET_MINCOUNT = "facet.mincount";
  private static final String FIELD_NAME_FACET_EXCLUDE = "facet.excludeTerms";
  private static final String FIELD_NAME_FACET_MISSING = "facet.missing";
  private static final String FIELD_NAME_FACET_LIMIT = "facet.limit";
  private static final String FIELD_NAME_FIELDS = "fl";
  private static final String FIELD_NAME_FILTER_QUERY = "fq";
  private static final String FIELD_NAME_OPERATOR = "q.op";
  private static final String FIELD_NAME_QUERY = "q";
  private static final String FIELD_NAME_QUERY_FIELDS = "qf";
  private static final String FIELD_NAME_STATS = "stats";
  private static final String FIELD_NAME_STATS_FIELD = "stats.field";
  private static final String FIELD_NAME_SORT = "sort";
  private static final String FIELD_NAME_SPELLCHECK = "spellcheck";
  private static final String FIELD_NAME_SPELLCHECK_query = "spellcheck.q";
  private static final String FIELD_NAME_USER_FIELDS = "uf";

  private static final String FILTER_TAG_PREFIX = "solr_fq_";

  private static final int GROUP_LIMIT = 100;

  private static final List<String> HIGHLIGHT_FIELDS_BLACKLIST = List.of(SearchField.GROUP_ID.getName());

  private SearchParams2SolrParamsConverter() {
  }

  /**
   * Returns solr params based on the given values
   *
   * @param params the params used to build the solr query params with
   * @return the solr params
   */
  public static SolrParams convert(final BaseService.SearchParams params) {
    final ModifiableSolrParams solrParams = new ModifiableSolrParams();
    final QueryParams queryParams = getQueryParams(params.getPhrase(), params.getQuery(), params.getSearchFields(), true);

    solrParams.set(FIELD_NAME_QUERY, queryParams.getQuery());
    solrParams.set(FIELD_NAME_SPELLCHECK_query, queryParams.getQuery());
    solrParams.set(FIELD_NAME_QUERY_FIELDS, queryParams.getFields());
    solrParams.set(FIELD_NAME_DEF_TYPE, BaseService.SearchParams.QUERY_PARSER);
    solrParams.set(FIELD_NAME_USER_FIELDS, "* _query_");
    solrParams.set(FIELD_NAME_FIELDS, DisplayField.getNames(params.getDisplayFields()));
    if (SortField.isValid(params.getSortPhrase())) {
      solrParams.set(FIELD_NAME_SORT, params.getSortPhrase());
    }
    solrParams.set(FIELD_NAME_OPERATOR, params.getQueryOperator().toString());
    solrParams.set(FIELD_NAME_SPELLCHECK, params.isSpellcheck());
    enrichWithMetaFields(solrParams, params.getStart(), params.getRows());
    enrichWithFilterQuery(solrParams, params.getFilterQueries());
    enrichWithHighlighting(solrParams, params);
    enrichWithFacets(solrParams, params.getFacets(), params.getFacetTermsExcluded(), params.includeMissingFacet());
    enrichWithStats(solrParams, params.getStats());
    enrichWithCollapse(solrParams, params.isCollapse());
    enrichWithGrouping(solrParams, params.isGrouping());
    return solrParams;
  }

  private static void enrichWithMetaFields(final ModifiableSolrParams solrParams, final long start, final long rows) {
    solrParams.set("start", Long.toString(start));
    solrParams.set("rows", Long.toString(rows));
  }

  private static void enrichWithHighlighting(final ModifiableSolrParams solrParams, final BaseService.SearchParams params) {
    if (params.isHighlight()) {
      final QueryParams queryParams = getQueryParams(params.getHighlightPhrase(), params.getHighlightQuery(), params.getHighlightFields(), false);

      solrParams.set("hl", "on");
      solrParams.set("hl.q", queryParams.getQuery());
      solrParams.set("hl.fl", Arrays.stream(queryParams.getFields()).filter(f -> !HIGHLIGHT_FIELDS_BLACKLIST.contains(f)).toArray(String[]::new));
      solrParams.set("hl.qparser", BaseService.SearchParams.QUERY_PARSER);
      solrParams.set("hl.highlightMultiTerm", true);
      solrParams.set("hl.snippets", params.getHighlightSnippetCount());
      solrParams.set("hl.maxAnalyzedChars", Integer.MAX_VALUE - 1);
      solrParams.set("hl.mergeContiguous", true);
      /* necessary for enabling highlight querying */
      solrParams.add(FIELD_NAME_QUERY_FIELDS, queryParams.getFields());
    }
  }

  private static void enrichWithGrouping(final ModifiableSolrParams solrParams, final boolean grouping) {
    if (grouping) {
      solrParams.set("group", true);
      solrParams.set("group.field", SearchField.GROUP_ID.getName());
      solrParams.set("group.limit", GROUP_LIMIT);
      solrParams.set("group.ngroups", true);
    }
  }

  private static void enrichWithCollapse(final ModifiableSolrParams solrParams, final boolean collapse) {
    if(collapse) {
      solrParams.add(FIELD_NAME_FILTER_QUERY, String.format("{!collapse field=%s}", SearchField.GROUP_ID.getName()));
    }
  }

  private static void enrichWithFacets(final ModifiableSolrParams solrParams, final List<String> facets, final List<String> termsExcluded, final boolean withFacetMissing) {
    solrParams.add(FIELD_NAME_FACET_EXCLUDE, termsExcluded == null ? "" : String.join(" ", termsExcluded));

    if (!CollectionUtils.isEmpty(facets)) {
      solrParams.add(FIELD_NAME_FACET, "true");
      solrParams.add(FIELD_NAME_FACET_MINCOUNT, "1");
      solrParams.add(FIELD_NAME_FACET_MISSING, String.valueOf(withFacetMissing));
      solrParams.add(FIELD_NAME_FACET_LIMIT, "-1");
      facets.forEach(
          /* add the facet and append the solr suffix */
          facet -> solrParams.add(FIELD_NAME_FACET_FIELD,
              createLocalParameters("ex", FILTER_TAG_PREFIX + facet) + facet));
    }
  }

  private static void enrichWithStats(final ModifiableSolrParams solrParams, final List<String> stats) {
    if (stats != null && !stats.isEmpty()) {
      solrParams.add(FIELD_NAME_STATS, "true");
      stats.forEach(stat -> solrParams.add(FIELD_NAME_STATS_FIELD,
              createLocalParameters("ex", FILTER_TAG_PREFIX + StatField.getTagNameForField(stat),
                  "min", "true", "max", "true", "count", "true", "missing", "true") + stat));
    }
  }

  private static void enrichWithFilterQuery(final ModifiableSolrParams solrParams, final Map<String, String> filterQueries) {
    if (filterQueries != null) {
      for (Entry<String, String> fq : filterQueries.entrySet()) {
        if (StringUtils.isNotEmpty(fq.getKey())) {
          String tag = StringUtils.isEmpty(fq.getValue()) ? ""
              : createLocalParameters("tag", FILTER_TAG_PREFIX + fq.getValue());
          solrParams.add(FIELD_NAME_FILTER_QUERY, tag + fq.getKey());
        }
      }
    }
  }

  private static String createLocalParameters(String... params) {
    if (params == null || params.length == 0 || params.length % 2 != 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("{!");
    for (int i = 0; i < params.length; i++) {
      sb.append(params[i]);
      if (i % 2 == 0) {
        sb.append("=");
      } else if (i + 1 < params.length) {
        sb.append(" ");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  private static QueryParams getQueryParams(final String phrase, final String query, final SearchField[] fields, final boolean boost) {
    final QueryParams result = new QueryParams();

    if(query == null) {
      if(StringUtils.isNotBlank(phrase)) {
        final String convertedPhrase = Query2SolrQueryConverter.convert(phrase, fields);
        result.setQuery(convertedPhrase);
      } else {
        result.setQuery("*:*");
      }
    }
    else {
      result.setQuery(query);
    }
    final String[] searchFields = getSearchFields(phrase, query, fields, boost);
    result.setFields(searchFields);
    return result;
  }

  private static String[] getSearchFields(final String phrase, final String query, final SearchField[] fields, final boolean boost) {
    final boolean isExactSearch = query == null && Query2SolrQueryConverter.isQuoted(phrase);
    String[] ret = SearchField.getNames(fields, isExactSearch, boost);

    if(!isExactSearch) {
      String[] stemmed = SearchFieldStemmed.getNames(fields, boost);
      return ArrayUtils.addAll(ret, stemmed);
    }
    return ret;
  }

  @Data
  @NoArgsConstructor
  private static final class QueryParams {
    private String[] fields;
    private String query;
  }
}
