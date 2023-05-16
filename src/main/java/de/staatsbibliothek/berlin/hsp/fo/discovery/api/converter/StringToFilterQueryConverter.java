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
package de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.discovery.api.SearchController;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SortField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.StatField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * converts a json string to a solr conform query
 *
 * @author Lutz Helm {@literal <helm@ub.uni-leipzig.de>}
 */
public class StringToFilterQueryConverter {

  private static final Logger logger = LoggerFactory.getLogger(StringToFilterQueryConverter.class);

  private final List<String> filterFields;
  private static final Entry<String, Object> DEFAULT_TYPE_FILTER = Map.entry(
      FacetField.TYPE.getName(),
      List.of(HspType.HSP_OBJECT.getValue(), HspType.HSP_DESCRIPTION.getValue(), HspType.HSP_DESCRIPTION_RETRO.getValue())
  );

  public StringToFilterQueryConverter(final List<String> filterFields) {
    this.filterFields = filterFields;
  }

  /**
   * @param input Filter query as described in {@link SearchController#search(String, List, String, boolean, long, long, SortField)}
   *              as JSON string.
   * @return Map with individual filter queries as key and an optional filter query field name as
   * value if a tag is required for excluding this query from the facet / stats calculation
   * for the field. See <a href="https://solr.apache.org/guide/solr/latest/query-guide/faceting.html#tagging-and-excluding-filters">Solr Faceting<a/>
   */
  @SuppressWarnings("unchecked")
  public Map<String, String> convert(String input) {
    Map<String, Object> map;
    Map<String, String> filterQueries = new HashMap<>();
    final Entry<String, Object> typeFilter = DEFAULT_TYPE_FILTER;
    if (input == null) {
      map = new HashMap<>();
    } else {
      try {
        map = new ObjectMapper().readValue(input, new TypeReference<>() {
        });
      } catch (JsonProcessingException ex) {
        logger.info("Error while processing filter query {} {}: {} ", input, ex.getMessage(), ex);
        return Map.of();
      }
    }

    /* add type filter for excluding digitizeds if there is none already */
    if (!map.containsKey(typeFilter.getKey()) || map.get(typeFilter.getKey()) == null || map.get(typeFilter.getKey()) instanceof List && CollectionUtils.isEmpty((List) map.get(typeFilter.getKey()))) {
      map.put(typeFilter.getKey(), typeFilter.getValue());
    }

    for (Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() == null || entry.getKey() == null || !filterFields.contains(entry.getKey().equals("orig-date-facet") ? StatField.ORIG_DATE_FROM.getValue() : entry.getKey())) {
        continue;
      }

      if (entry.getKey().equals("orig-date-facet")) {
        String origDateFilterQuery = getFilterQueryForOrigDate((Map) entry.getValue());
        if (!StringUtils.isBlank(origDateFilterQuery)) {
          filterQueries.put(origDateFilterQuery, StatField.getTagNameForField(StatField.ORIG_DATE_FROM.getValue()));
        }
        continue;
      }

      String facetField = entry.getKey();
      String filterQuery = null;
      String tag = null;
      boolean includeMissing = false;

      if (entry.getValue() instanceof String) {
        String stringValue = (String) entry.getValue();
        if (StringUtils.isNotEmpty(stringValue)) {
          filterQuery = String.format("%s:\"%s\"", entry.getKey(), stringValue);
        }
      }

      if (entry.getValue() instanceof Map) {
        Map<String, Object> rangeFilter = (Map) entry.getValue();
        if (rangeFilter.get("from") instanceof Integer && rangeFilter.get("to") instanceof Integer) {
          int from = (int) rangeFilter.get("from");
          int to = (int) rangeFilter.get("to");
          filterQuery = String.format("%s:[%s TO %s]", facetField, from, to);
          tag = entry.getKey();
        }
        if (rangeFilter.get("missing") instanceof Boolean) {
          includeMissing = (boolean) rangeFilter.get("missing");
        }
      } else if (entry.getValue() instanceof List) {
        @SuppressWarnings({"rawtypes"}) List listValue = (List) entry.getValue();
        if (listValue.isEmpty()) {
          continue;
        }
        if (listValue.get(0) instanceof String) {
          if (listValue.contains(QueryResponse2ResponseEntityConverter.FIELD_NAME_MISSING)) {
            listValue.remove(QueryResponse2ResponseEntityConverter.FIELD_NAME_MISSING);
            includeMissing = true;
          }

          if (listValue.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(entry.getKey());
            builder.append(":("); // mood?

            Iterator<String> valueIterator = listValue.iterator();
            while (valueIterator.hasNext()) {
              String value = valueIterator.next();
              builder.append(String.format("\"%s\"", value));
              if (valueIterator.hasNext()) {
                builder.append(" OR ");
              }
            }
            builder.append(")");
            filterQuery = builder.toString();
          }

          tag = entry.getKey();
        }
        // Everything else is ignored
      }
      if (!includeMissing && StringUtils.isBlank(filterQuery)) {
        continue;
      }

      String finalFilterQuery = null;
      if (includeMissing) {
        if (StringUtils.isBlank(filterQuery)) {
          finalFilterQuery = String.format("-%s:[* TO *]", facetField);
        } else {
          finalFilterQuery = String.format("(*:* NOT %s:*) OR (%s)", facetField, filterQuery);
        }
      } else {
        finalFilterQuery = filterQuery;
      }
      filterQueries.put(finalFilterQuery, tag);
    }
    return filterQueries.isEmpty() ? null : filterQueries;
  }

  private static String getFilterQueryForOrigDate(Map<String, Object> origDateFilter) {
    Integer from = (Integer) origDateFilter.get("from");
    Integer to = (Integer) origDateFilter.get("to");
    if (from == null || to == null) {
      return null;
    }

    Boolean exact = (Boolean) origDateFilter.get("exact");
    exact = exact != null && exact;
    Boolean includeMissing = (Boolean) origDateFilter.get("missing");
    includeMissing = includeMissing != null && includeMissing;

    StringBuilder sb = new StringBuilder();
    if (exact) {
      sb.append(String.format("(%s:[%s TO *]", StatField.ORIG_DATE_FROM.getValue(), from));
      sb.append(" AND ");
      sb.append(String.format("%s:[* TO %s])", StatField.ORIG_DATE_TO.getValue(), to));
      if (includeMissing) {
        sb.append(" OR ");
        sb.append(String.format("((*:* NOT %s:*) AND (*:* NOT %s:*))", StatField.ORIG_DATE_FROM.getValue(), StatField.ORIG_DATE_TO.getValue()));
      }
    } else {
      sb.append(String.format("(%s:[%s TO *] OR (*:* NOT %s:*))", StatField.ORIG_DATE_TO.getValue(), from, StatField.ORIG_DATE_TO.getValue()));
      sb.append(" AND ");
      sb.append(String.format("(%s:[* TO %s] OR (*:* NOT %s:*))", StatField.ORIG_DATE_FROM.getValue(), to, StatField.ORIG_DATE_FROM.getValue()));
      if (!includeMissing) {
        sb.append(" AND ");
        sb.append(String.format("(%s:[* TO *] OR %s:[* TO *])", StatField.ORIG_DATE_FROM.getValue(), StatField.ORIG_DATE_TO.getValue()));
      }
    }
    return sb.toString();
  }
}
