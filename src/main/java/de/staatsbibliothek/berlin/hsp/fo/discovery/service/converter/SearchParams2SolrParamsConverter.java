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
package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import cz.jirutka.rsql.parser.RSQLParserException;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SortField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.StatField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight.DOMHelper;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Component
public class SearchParams2SolrParamsConverter {
  private static final String FIELD_NAME_DEF_TYPE = "defType";
  private static final String FIELD_NAME_FACET = "facet";
  private static final String FIELD_NAME_FACET_EXCLUDE = "facet.excludeTerms";
  private static final String FIELD_NAME_FACET_FIELD = "facet.field";
  private static final String FIELD_NAME_FACET_LIMIT = "facet.limit";
  private static final String FIELD_NAME_FACET_MISSING = "facet.missing";
  private static final String FIELD_NAME_FACET_MIN_COUNT = "facet.mincount";
  private static final String FIELD_NAME_FACET_METHOD = "facet.method";
  private static final String FIELD_NAME_FACET_SORT = "facet.sort";
  private static final String FIELD_NAME_FIELDS = "fl";
  private static final String FIELD_NAME_FILTER_QUERY = "fq";
  private static final String FIELD_NAME_OPERATOR = "q.op";
  private static final String FIELD_NAME_QUERY = "q";
  private static final String FIELD_NAME_QUERY_FIELDS = "qf";
  private static final String FIELD_NAME_STATS = "stats";
  private static final String FIELD_NAME_STATS_FIELD = "stats.field";
  private static final String FIELD_NAME_SORT = "sort";
  private static final String FIELD_NAME_SPELLCHECK = "spellcheck";
  private static final String FIELD_NAME_SPELLCHECK_QUERY = "spellcheck.q";
  private static final String FIELD_NAME_USER_FIELDS = "uf";

  private static final String FILTER_TAG_PREFIX = "solr_fq_";
  private static final int GROUP_LIMIT = 100;
  private static final List<String> HIGHLIGHT_FIELDS_IGNORE = List.of("group-id-search");

  private ExtendedSearchStringToQueryParamsConverter extendedSearchConverter;
  private FieldProvider fieldProvider;
  private Query2SolrQueryConverter query2SolrQueryConverter;

  @Autowired
  public void setFieldProvider(final FieldProvider fieldProvider) {
    this.fieldProvider = fieldProvider;
  }

  @Autowired
  public void setQuery2SolrQueryConverter(final Query2SolrQueryConverter query2SolrQueryConverter) {
    this.query2SolrQueryConverter = query2SolrQueryConverter;
  }

  @Autowired
  public void setExtendedSearchStringToQueryParamsConverter(final ExtendedSearchStringToQueryParamsConverter extendedSearchConverter) {
    this.extendedSearchConverter = extendedSearchConverter;
  }

  /**
   * Returns solr params based on the given values
   *
   * @param params the params used to build the solr query params with
   * @return the solr params
   */
  public SolrParams convert(final BaseService.SearchParams params, final HighlightConfig highlightConfig) {
    final EnhancedModifiableSolrParams solrParams = new EnhancedModifiableSolrParams();
    final List<String> fieldNames = params.getSearchFields() == null ? fieldProvider.getFieldNames() : params.getSearchFields();
    final QueryParams queryParams = getQueryParams(params.getPhrase(), params.getPhraseExtended(), params.getQuery(), fieldNames);

    solrParams.set(FIELD_NAME_QUERY, queryParams.getQuery());
    solrParams.set(FIELD_NAME_QUERY_FIELDS, queryParams.getFieldsArray());
    solrParams.set(FIELD_NAME_DEF_TYPE, BaseService.SearchParams.QUERY_PARSER);
    solrParams.set(FIELD_NAME_USER_FIELDS, "* _query_");
    solrParams.set(FIELD_NAME_FIELDS, DisplayField.getNames(params.getDisplayFields()));
    if (SortField.isValid(params.getSortPhrase())) {
      solrParams.set(FIELD_NAME_SORT, params.getSortPhrase());
    }
    solrParams.set(FIELD_NAME_OPERATOR, params.getQueryOperator().toString());
    enrichWithSpellchecking(solrParams, queryParams);
    enrichWithMetaFields(solrParams, params.getStart(), params.getRows());
    enrichWithFilterQuery(solrParams, params.getFilterQueries());
    enrichWithHighlighting(params, solrParams, highlightConfig.getTagName(), highlightConfig.getSnippetCount());
    enrichWithFacets(solrParams, params);
    enrichWithStats(solrParams, params.getStats());
    enrichWithCollapse(solrParams, params.isCollapse());
    enrichWithGrouping(solrParams, params.isGrouping());

    /* prepare highlighting by re-writing them to the source params */
    params.setHighlightQuery(queryParams.getQuery());
    params.setHighlightFields(FieldProvider.removeBoostingFactors(queryParams.getFields().stream().toList()));
    params.setHighlightQueryType(queryParams.getQueryType());

    return solrParams;
  }

  private static void enrichWithMetaFields(final ModifiableSolrParams solrParams, final long start, final long rows) {
    solrParams.set("start", Long.toString(start));
    solrParams.set("rows", Long.toString(rows));
  }

  private void enrichWithHighlighting(final BaseService.SearchParams sourceParams, final EnhancedModifiableSolrParams targetParams, final String tagName, final int snippetCount) {
    if (sourceParams.isHighlight()) {
      targetParams.set("hl", "on");
      targetParams.set("hl.q", sourceParams.getHighlightQuery());
      targetParams.set("hl.fl", gatherHighlightFields(sourceParams));
      targetParams.set("hl.qparser", BaseService.SearchParams.QUERY_PARSER);
      targetParams.set("hl.highlightMultiTerm", true);
      targetParams.set("hl.snippets", snippetCount);
      targetParams.set("hl.maxAnalyzedChars", Integer.MAX_VALUE - 1);
      targetParams.set("hl.mergeContiguous", true);
      targetParams.set(HighlightParams.METHOD, "original");
      targetParams.set(HighlightParams.TAG_PRE, DOMHelper.getOpeningTag(tagName));
      targetParams.set(HighlightParams.TAG_POST, DOMHelper.getClosingTag(tagName));
      targetParams.set(HighlightParams.FRAGSIZE, 0);
      targetParams.set(HighlightParams.FIELD_MATCH, true);

      /* necessary for enabling highlight querying */
      if(!CollectionUtils.isEmpty(sourceParams.getHighlightFields())) {
        targetParams.addIfAbsent(FIELD_NAME_QUERY_FIELDS, sourceParams.getHighlightFields().toArray(new String[0]));
      }
    }
  }

  private String[] gatherHighlightFields(final BaseService.SearchParams sourceParams) {
    final List<String> fields;
    if(CollectionUtils.isEmpty(sourceParams.getHighlightFields())) {
      fields = sourceParams.getSearchFields();
    } else {
      fields = sourceParams.getHighlightFields();
    }

    return fields.stream()
        .filter(f -> !HIGHLIGHT_FIELDS_IGNORE.contains(f))
        .toArray(String[]::new);
  }

  private void enrichWithGrouping(final ModifiableSolrParams solrParams, final boolean grouping) {
    if (grouping) {
      solrParams.set("group", true);
      solrParams.set("group.field", fieldProvider.getBasicName("group-id-search").get());
      solrParams.set("group.limit", GROUP_LIMIT);
      solrParams.set("group.ngroups", true);
    }
  }

  private void enrichWithCollapse(final ModifiableSolrParams solrParams, final boolean collapse) {
    if (collapse) {
      solrParams.add(FIELD_NAME_FILTER_QUERY, String.format("{!collapse field=%s}", fieldProvider.getBasicName("group-id-search").get()));
    }
  }

  private static void enrichWithFacets(final ModifiableSolrParams targetParams, final BaseService.SearchParams sourceParams) {
    targetParams.add(FIELD_NAME_FACET_EXCLUDE, sourceParams.getFacetTermsExcluded() == null ? "" : String.join(" ", sourceParams.getFacetTermsExcluded()));

    if (!CollectionUtils.isEmpty(sourceParams.getFacets())) {
      targetParams.add(FIELD_NAME_FACET, "true");
      targetParams.add(FIELD_NAME_FACET_LIMIT, "-1");
      targetParams.add(FIELD_NAME_FACET_METHOD, "enum");
      targetParams.add(FIELD_NAME_FACET_MIN_COUNT, String.valueOf(sourceParams.getFacetMinCount()));
      targetParams.add(FIELD_NAME_FACET_MISSING, String.valueOf(sourceParams.includeMissingFacet()));
      targetParams.add(FIELD_NAME_FACET_SORT, "count");
      sourceParams.getFacets().forEach(
          /* add the facet and append the solr suffix */
          facet -> targetParams.add(FIELD_NAME_FACET_FIELD, createLocalParameters("ex", FILTER_TAG_PREFIX + facet) + facet));
    }
  }

  private static void enrichWithStats(final ModifiableSolrParams solrParams, final List<String> stats) {
    if (stats != null && !stats.isEmpty()) {
      solrParams.add(FIELD_NAME_STATS, "true");
      stats.forEach(stat -> solrParams.add(FIELD_NAME_STATS_FIELD, createLocalParameters("ex", FILTER_TAG_PREFIX + StatField.getTagNameForField(stat), "min", "true", "max", "true", "count", "true", "missing", "true") + stat));
    }
  }

  private static void enrichWithFilterQuery(final ModifiableSolrParams solrParams, final Map<String, String> filterQueries) {
    if (filterQueries != null) {
      for (Entry<String, String> fq : filterQueries.entrySet()) {
        if (StringUtils.isNotEmpty(fq.getKey())) {
          String tag = StringUtils.isEmpty(fq.getValue()) ? "" : createLocalParameters("tag", FILTER_TAG_PREFIX + fq.getValue());
          solrParams.add(FIELD_NAME_FILTER_QUERY, tag + fq.getKey());
        }
      }
    }
  }

  private static void enrichWithSpellchecking(final ModifiableSolrParams modifiableSolrParams, final QueryParams queryParams) {
      final String correctableTerms = queryParams.getQueryTokens().stream()
          .filter(qt -> TokenType.STANDARD.equals(qt.getType()))
          .map(QueryToken::getToken)
          .collect(Collectors.joining(" "));
      if(StringUtils.isNotEmpty(correctableTerms)) {
        modifiableSolrParams.set(FIELD_NAME_SPELLCHECK, true);
        modifiableSolrParams.set(FIELD_NAME_SPELLCHECK_QUERY, correctableTerms);
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

  /**
   * Collects all necessary information for building a query
   * @return the {@code QueryParams}
   */
  private QueryParams getQueryParams(final String phrase, final String extendedPhrase, final String query, final List<String> searchFields) {
    /* used for group completion */
    if(StringUtils.isNotEmpty(query)) {
      return getQueryParamsPreparedQuery(query, searchFields, List.of());
    } else if (StringUtils.isEmpty(phrase) && StringUtils.isEmpty(extendedPhrase)) {
      return getQueryParamsEmptySearch(searchFields);
    } else if(StringUtils.isNotEmpty(phrase)) {
      return getQueryParamsDefault(phrase, searchFields);
    } else {
      return getQueryParamsExtended(extendedPhrase);
    }
  }

  private QueryParams getQueryParamsDefault(final String phrase, final List<String> fields) {
    final Query2SolrQueryConverter.SolrQueryParams solrQueryParams = query2SolrQueryConverter.convert(phrase, fields);
    final QueryParams result = QueryParams.withSolrQueryParams(solrQueryParams);
    result.setFields(getSearchFields(result.getQueryType(), fields));

    return result;
  }

  private static QueryParams getQueryParamsPreparedQuery(final String query, final List<String> searchFields, final List<QueryToken> tokens) {
      return new QueryParams(Set.copyOf(searchFields), query, tokens, QueryType.STANDARD);
    }

  private QueryParams getQueryParamsExtended(final String extendedPhrase) {
    try {
      return extendedSearchConverter.convert(extendedPhrase);
    } catch(RSQLParserException ex) {
      throw ExceptionFactory.getException(ExceptionType.EXTENDED_SEARCH, ex.getMessage());
    }
  }

  private QueryParams getQueryParamsEmptySearch(final List<String> fields) {
    return new QueryParams(getSearchFields(QueryType.STANDARD, fields), "*:*", Collections.emptyList(), QueryType.STANDARD);
  }

  private Set<String> getSearchFields(final QueryType queryType, final List<String> fields) {
    List<String> ret = new LinkedList<>();
    switch (queryType) {
      case STANDARD -> {
        ret.addAll(fieldProvider.getBasicNames(fields));
        ret.addAll(fieldProvider.getStemmedNames(fields));
      }
      case MIXED -> {
        ret.addAll(fieldProvider.getBasicNames(fields));
        ret.addAll(fieldProvider.getExactNames(fields));
        ret.addAll(fieldProvider.getExactNoPunctuationNames(fields));
        ret.addAll(fieldProvider.getStemmedNames(fields));
      }
      case EXACT -> {
        ret.addAll(fieldProvider.getExactNames(fields));
        ret.addAll(fieldProvider.getExactNoPunctuationNames(fields));
      }
    }

    return Set.copyOf(ret);
  }

  /**
   * A subclass of {@link ModifiableSolrParams} that provides additional utility methods.
   */
  private class EnhancedModifiableSolrParams extends ModifiableSolrParams {

    /**
     * Adds the specified values to the parameter only if they are not already present.
     *
     * @param name the name of the parameter
     * @param values the values to add if absent
     */
    public void addIfAbsent(final String name, final String... values) {
      if (name == null || values == null || values.length == 0) {
        return;
      }

      final String[] existingValues = this.getParams(name);
      if (existingValues == null) {
        this.add(name, values);
      } else {
        String[] toAdd = ArrayUtils.removeElements(values, existingValues);
        if (toAdd.length > 0) {
          this.add(name, toAdd);
        }
      }
    }
  }
}
