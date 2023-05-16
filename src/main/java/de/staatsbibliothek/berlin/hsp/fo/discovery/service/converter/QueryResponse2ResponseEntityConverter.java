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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Stats;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.*;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchFieldStemmed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 */
@Component
@Slf4j
public class QueryResponse2ResponseEntityConverter {

  public static final String FIELD_NAME_EXCEPTION = "exception";
  public static final String FIELD_NAME_RESPONSE_HEADER = "responseHeader";
  public static final String FIELD_NAME_MISSING = "__MISSING__";
  public static final String FIELD_NAME_GROUP_ID = "group-id-display";

  private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  static {
    mapper.setDateFormat(new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH));
  }

  private QueryResponse2ResponseEntityConverter() {
  }

  private static String getHeaderValue(final QueryResponse queryResponse, final String headerName) {
    return ((NamedList<String>) queryResponse.getHeader()
        .get("params")).get(headerName);
  }

  private static Long getStartValue(final QueryResponse queryResponse) {
    return Long.parseLong(getHeaderValue(queryResponse, "start"));
  }

  private static boolean getGrouping(final QueryResponse queryResponse) {
    return Boolean.parseBoolean(Objects.requireNonNullElse(getHeaderValue(queryResponse, "group"), "false"));
  }

  private static Long getRowsValue(final QueryResponse queryResponse) {
    return Long.parseLong(getHeaderValue(queryResponse, "rows"));
  }

  /**
   * creates a response object containing a list of HspObjectGroups and the related meta data
   *
   * @param queryResponse the query response from solr
   * @return the response object
   */
  public static List<HspObjectGroup> getHSPObjectGroups(final QueryResponse queryResponse) {
    if (queryResponse.getException() != null) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST_EXCEPTION, queryResponse.getException()
          .getMessage());
    }
    return QueryResponse2ResponseEntityConverter.extractHspObjectGroups(queryResponse);
  }

  public static List<String> getHspObjectGroupIds(final QueryResponse queryResponse) {
    if (queryResponse.getException() != null) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST_EXCEPTION, queryResponse.getException()
          .getMessage());
    }
    return extractSolrDocumentIds(queryResponse);
  }

  /**
   * extracts and returns all contained HspObjectGroups from a query response
   *
   * @param queryResponse the query response from solr
   * @return the HspObjectGroup as List
   */
  public static List<HspObjectGroup> extractHspObjectGroups(final QueryResponse queryResponse) {
    final List<HspObjectGroup> ret = new ArrayList<>();

    if (queryResponse.getGroupResponse() != null) {
      final List<Group> groupList = queryResponse.getGroupResponse()
          .getValues()
          .get(0)
          .getValues();
      for (Group group : groupList) {
        final HspObjectGroup hog = extractHspObjectGroup(group.getResult());
        if (hog.getHspObject() != null) {
          ret.add(hog);
        }
      }
    }
    return ret;
  }

  private static HspObjectGroup extractHspObjectGroup(final SolrDocumentList documents) {
    HspObjectGroup hog = new HspObjectGroup();
    String type;
    for (SolrDocument solrDoc : documents) {
      type = (String) solrDoc.get(DisplayField.TYPE.getName());
      if (type != null) {
        switch (HspType.getByValue(type)) {
          case HSP_OBJECT:
            hog.setHspObject(extractHspObject(solrDoc));
            break;
          case HSP_DESCRIPTION:
          case HSP_DESCRIPTION_RETRO:
            CollectionUtils.addIgnoreNull(hog.getHspDescriptions(), extractHspDescription(solrDoc));
            break;
          case HSP_DIGITIZED:
            CollectionUtils.addIgnoreNull(hog.getHspDigitizeds(), extractHspDigitized(solrDoc));
            break;
        }
      }
    }
    return hog;
  }

  /**
   * Extracts a List of entities of type {@code T} based on a {@code QueryResponse} for an ungrouped query.
   * Will return an empty list for gruped queries
   *
   * @param queryResponse the queryResponse containing the result data in JSON format
   * @param clazz the Class the response should be mapped to
   * @param <T> the extraction's target type
   * @return a list containing the extracted result data of type T
   */
  public static <T> List<T> extract(final QueryResponse queryResponse, final Class<T> clazz) {
    return queryResponse.getResults()
        .stream()
        .map(doc -> extract(doc, clazz))
        .collect(Collectors.toList());
  }

  private static <T> T extract(final SolrDocument solrDocument, final Class<T> clazz) {
    try {
      return mapper.readValue(solrDocument.jsonStr(), clazz);
    } catch (JsonProcessingException e) {
      log.warn("error while mapping object to class {}, will skip {}: {}", clazz, e.getMessage(), e);
      return null;
    }
  }

  private static HspObject extractHspObject(final SolrDocument solrDoc) {
    return extract(solrDoc, HspObject.class);
  }

  private static HspDescription extractHspDescription(final SolrDocument solrDoc) {
    return extract(solrDoc, HspDescription.class);
  }

  private static HspDigitized extractHspDigitized(final SolrDocument solrDoc) {
    return extract(solrDoc, HspDigitized.class);
  }

  /**
   * extracts and returns the total collapsed (grouped) amount of found documents
   *
   * @param queryResponse the query response from solr
   * @return the number of found collapsed (grouped) documents
   */
  public static long extractNumFound(final QueryResponse queryResponse) {
    if (queryResponse == null) {
      return 0;
    }
    if (getGrouping(queryResponse)) {
      return queryResponse.getGroupResponse()
          .getValues()
          .get(0)
          .getNGroups();
    } else if (Objects.nonNull(queryResponse.getResults())) {
      return queryResponse.getResults()
          .getNumFound();
    }
    return 0;
  }

  /**
   * extracts and returns the first collated result from spell checking if there is any
   *
   * @param queryResponse the {@link QueryResponse} from solr
   * @return the first collated result of spell checking if there is any, {@code null} otherwise
   */
  public static String extractSpellCorrectedTerm(final QueryResponse queryResponse) {
    if (queryResponse != null && queryResponse.getSpellCheckResponse() != null) {
      return queryResponse.getSpellCheckResponse()
          .getCollatedResult();
    }
    return null;
  }

  /**
   * extracts the highlighting information from the query response an example of resulting data is
   * shown below as json representation:
   *
   * <pre>
   * {@code
   * "HOG001": {
   *   "aufbewahrungsOrt": [
   *     "consequat <em>cupidatat</em> nisi"
   *   ]
   * }
   * </pre>
   *
   * @param queryResponse the query response from solr
   * @return
   */
  public static Map<String, Map<String, List<String>>> extractHighlightingFragmentsFromQueryResponse(final QueryResponse queryResponse) {
    if (queryResponse == null || queryResponse.getResponse() == null) {
      return Map.of();
    }
    return prepareHighlightInformation(queryResponse.getHighlighting());
  }

  /**
   * extracts the faceting information from the query response and converts it into a map of maps.
   * an example of resulting data is shown below as json representation:
   *
   * <pre>
   * {@code
   * { "aufbewahrungsOrt": {
   *     "Berlin": 1,
   *     "Leipzig": 1,
   *     "Dessau": 1
   *   }
   * }
   * </pre>
   *
   * @param queryResponse the query response from solr
   * @return the faceting information
   */
  public static Map<String, Map<String, Long>> extractFacetsFromQueryResponse(final QueryResponse queryResponse) {
    if (queryResponse != null && queryResponse.getFacetFields() != null) {
      final Map<String, Map<String, Long>> ret = new HashMap<>();
      for (FacetField ff : queryResponse.getFacetFields()) {
        // null to __MISSING__ conversion could be done with per JsonSerializer annotation, but it would be a lot more complex
        Map<String, Long> items = ff.getValues()
            .stream()
            .collect(Collectors.toMap(fc -> fc.getName() == null ? FIELD_NAME_MISSING : fc.getName(), FacetField.Count::getCount));
        if (items.containsKey(FIELD_NAME_MISSING) && items.get(FIELD_NAME_MISSING) == 0) {
          items.remove(FIELD_NAME_MISSING);
        }
        ret.put(ff.getName(), items);
      }
      return ret;
    }
    return Map.of();
  }

  public static Map<String, Stats> extractStatsFromQueryResponse(final QueryResponse queryResponse) {
    if (queryResponse != null && queryResponse.getFieldStatsInfo() != null) {
      final Map<String, Stats> ret = new HashMap<>();
      for (FieldStatsInfo stat : queryResponse.getFieldStatsInfo()
          .values()) {
        ret.put(stat.getName(), new Stats((Double) stat.getMin(), (Double) stat.getMax(), stat.getCount(), stat.getMissing()));
      }
      return ret;
    }
    return Map.of();
  }

  /**
   * extracts the metadata information and returns the result
   *
   * @param queryResponse the query response from solr
   * @return the meta data
   */
  public static MetaData extractMetadata(final QueryResponse queryResponse) {
    final String spellCorrectedTerm = extractSpellCorrectedTerm(queryResponse);
    final long numFound = QueryResponse2ResponseEntityConverter.extractNumFound(queryResponse);
    final long start = getStartValue(queryResponse);
    final long rows = getRowsValue(queryResponse);
    final Map<String, Map<String, Long>> facets = extractFacetsFromQueryResponse(queryResponse);
    final Map<String, Stats> stats = extractStatsFromQueryResponse(queryResponse);
    final Map<String, Map<String, List<String>>> highlighting = extractHighlightingFragmentsFromQueryResponse(queryResponse);

    return MetaData.builder()
        .withNumFound(numFound)
        .withFacets(facets)
        .withHighlighting(highlighting)
        .withRows(rows)
        .withStart(start)
        .withStats(stats)
        .withSpellCorrectedTerm(spellCorrectedTerm)
        .build();
  }

  public static List<String> extractSolrDocumentIds(final QueryResponse queryResponse) {
    List<String> groupIds = new LinkedList<>();
    if (queryResponse != null && queryResponse.getResults() != null) {
      for (SolrDocument sd : queryResponse.getResults()) {
        groupIds.add((String) sd.getFieldValue(FIELD_NAME_GROUP_ID));
      }
    }
    return groupIds;
  }

  public static List<String> extractTeiDocumentsFromSolrResponse(final QueryResponse queryResponse) {
    List<String> result = new ArrayList<>();
    SolrDocumentList docList = queryResponse.getResults();
    if (docList == null) {
      return result;
    }
    for (SolrDocument doc : docList) {
      String teiDoc = (String) doc.getFieldValue(DisplayField.TEI_DOCUMENT.getName());
      if (teiDoc != null && !(teiDoc.trim()
          .isEmpty())) {
        result.add(teiDoc);
      }
    }
    return result;
  }

  private static Map<String, Map<String, List<String>>> prepareHighlightInformation(
      @Nullable Map<String, Map<String, List<String>>> highlighting) {
    if (highlighting == null) {
      return Map.of();
    }
    Map<String, Map<String, List<String>>> result = highlighting;
    result = removeHighlightFieldSuffix(result, SearchField.EXACT_SUFFIX);
    result = removeMultipleHighlighting(result);
    result = removeHighlightFieldSuffix(result, SearchFieldStemmed.STEMMED_SUFFIX);
    return result;
  }

  /**
   * Removes the field-suffix for exact search fields
   *
   * @param highlighting the highlighting data
   * @return the highlighting data containing the modified field names
   */
  private static Map<String, Map<String, List<String>>> removeHighlightFieldSuffix(final Map<String, Map<String, List<String>>> highlighting, final String suffix) {
    return highlighting.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(fieldName -> StringUtils.substringBefore(fieldName.getKey(), suffix), Map.Entry::getValue))));
  }

  /**
   * Removes highlighting on unstemmed fields (having the {@value SearchFieldStemmed#STEMMED_SUFFIX}), when highlighting already contains the corresponding stemmed field
   *
   * @param highlighting the highlighting information to modified
   * @return the modified highlighting information
   */
  private static Map<String, Map<String, List<String>>> removeMultipleHighlighting(Map<String, Map<String, List<String>>> highlighting) {
    return highlighting.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey()
                .endsWith(SearchFieldStemmed.STEMMED_SUFFIX) || !e.getValue()
                .containsKey(entry.getKey() + SearchFieldStemmed.STEMMED_SUFFIX))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
  }
}
