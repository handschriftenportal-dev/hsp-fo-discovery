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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SortField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.StatField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.valueobject.HspTypes;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class SearchParams2SolrParamsConverter {

  private static final String FIELD_NAME_FACET = "facet";
  private static final String FIELD_NAME_FACET_FIELD = "facet.field";
  private static final String FIELD_NAME_FACET_MINCOUNT = "facet.mincount";
  private static final String FIELD_NAME_FACET_EXCLUDE = "facet.excludeTerms";
  private static final String FIELD_NAME_FACET_MISSING = "facet.missing";
  private static final String FIELD_NAME_FIELDS = "fl";
  private static final String FIELD_NAME_FILTER_QUERY = "fq";
  private static final String FIELD_NAME_QUERY = "q";
  private static final String FIELD_NAME_QUERY_FIELDS = "qf";
  private static final String FIELD_NAME_STATS = "stats";
  private static final String FIELD_NAME_STATS_FIELD = "stats.field";
  private static final String FIELD_NAME_SORT = "sort";

  private static final String FIELD_NAME_DEF_TYPE = "defType";

  private static final String FILTER_TAG_PREFIX = "solr_fq_";
  
  /**
   * Returns solr params based on the given values
   *
   * @param params the params used to build the solr query params with
   * @return the solr params
   */
  public static SolrParams convert(final AbstractParams<?> params) {
    final ModifiableSolrParams solrParams = new ModifiableSolrParams();

    solrParams.set(FIELD_NAME_DEF_TYPE, params.getQueryParser().getSolrName());
    // ToDo request only required fields e. g. only group-id for search requests
    solrParams.set(FIELD_NAME_FIELDS, DisplayField.getNames());
    solrParams.set(FIELD_NAME_QUERY, params.getQuery());
    if (SortField.isValid(params.getSortPhrase())) {
      solrParams.set(FIELD_NAME_SORT, params.getSortPhrase());
    }

    /* used for term search and highlighting */
    if (QueryParser.EDISMAX.equals(params.getQueryParser()) || QueryParser.EDISMAX.equals(params.getHighlightQueryParser())) {
      solrParams.set(FIELD_NAME_QUERY_FIELDS, SearchField.getByNamesBoosted(params.getSearchFields()));
    }
    enrichWithMetaFields(solrParams, params.getStart(), params.getRows());
    enrichtWithFilterQuery(solrParams, params.getFilterQueries());
    enrichWithHighlighting(solrParams, params);
    enrichWithFacets(solrParams, params.getFacets());
    enrichWithStats(solrParams, params.getStats());
    enrichWithCollapse(solrParams, params.isCollapse(), SearchField.GROUP_ID.getName());
    enrichWithGrouping(solrParams, params.isGrouping(), SearchField.GROUP_ID.getName(), 100);
    return solrParams;
  }
  
  private static void enrichWithMetaFields(final ModifiableSolrParams solrParams, final long start, final long rows) {
    solrParams.set("start", Long.toString(start));
    solrParams.set("rows", Long.toString(rows));
  }

  private static void enrichWithHighlighting(final ModifiableSolrParams solrParams, final AbstractParams<?> params) {
    if (params.isHighlight()) {
      solrParams.set("hl", "on");
      solrParams.set("hl.q", params.getHighlightQuery());
      solrParams.set("hl.fl", params.getHighlightFields());
      solrParams.set("hl.qparser", params.getHighlightQueryParser().getSolrName());
      solrParams.set("hl.highlightMultiTerm", true);
      solrParams.set("hl.snippets", params.getHighlightSnippetCount());
      solrParams.set("hl.maxAnalyzedChars", Integer.MAX_VALUE);
      solrParams.set("hl.mergeContiguous", true);
    }
  }

  private static void enrichWithGrouping(final ModifiableSolrParams solrParams, final boolean grouping, final String groupField, final int groupLimit) {
    if (grouping) {
      solrParams.set("group", true);
      solrParams.set("group.field", groupField);
      solrParams.set("group.limit", groupLimit);
      solrParams.set("group.ngroups", true);
    }
  }

  private static void enrichWithCollapse(final ModifiableSolrParams solrParams, final boolean collapse, final String collapseField) {
    if(collapse) {
      solrParams.add(FIELD_NAME_FILTER_QUERY, String.format("{!collapse field=%s}", collapseField));
    }
  }

  private static void enrichWithFacets(final ModifiableSolrParams solrParams, final List<String> facets) {
    solrParams.add(FIELD_NAME_FACET_EXCLUDE, HspTypes.HSP_DIGITIZED.getValue());
    if (facets != null && !facets.isEmpty()) {
      solrParams.add(FIELD_NAME_FACET, "true");
      solrParams.add(FIELD_NAME_FACET_MINCOUNT, "1");
      solrParams.add(FIELD_NAME_FACET_MISSING, "true");
      facets.stream().forEach(
          /* add the facet and append the solr suffix */
          facet -> solrParams.add(FIELD_NAME_FACET_FIELD,
              createLocalParameters("ex", FILTER_TAG_PREFIX + facet) + facet));
    }
  }

  private static void enrichWithStats(final ModifiableSolrParams solrParams, final List<String> stats) {
    if (stats != null && !stats.isEmpty()) {
      solrParams.add(FIELD_NAME_STATS, "true");
      stats.stream()
          .forEach(stat -> solrParams.add(FIELD_NAME_STATS_FIELD,
              createLocalParameters("ex", FILTER_TAG_PREFIX + StatField.getTagNameForField(stat),
                  "min", "true", "max", "true", "count", "true", "missing", "true") + stat));
    }
  }

  private static void enrichtWithFilterQuery(final ModifiableSolrParams solrParams, final Map<String, String> filterQueries) {
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
}
