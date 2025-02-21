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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Stats;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldTypeInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.EnumsConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight.FragmentHelper;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight.HighlightHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.schema.FieldTypeRepresentation;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Converts Solr {@link QueryResponse}s to DTOs
 *
 */
@Component
@Slf4j
public class QueryResponse2ResponseEntityConverter {

  public static final String FIELD_NAME_EXCEPTION = "exception";
  public static final String FIELD_NAME_RESPONSE_HEADER = "responseHeader";
  public static final String FIELD_NAME_MISSING = "__MISSING__";
  public static final String FIELD_NAME_GROUP_ID = "group-id-display";

  private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final XmlMapper xmlMapper = new XmlMapper();

  static {
    mapper.setDateFormat(new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH));
  }

  private QueryResponse2ResponseEntityConverter() {
  }

  private static Optional<String> getHeaderValue(final QueryResponse queryResponse, final String headerName) {
    Object params = queryResponse.getHeader().get("params");
    if (params instanceof NamedList<?> namedList && namedList.get(headerName) instanceof String) {
      return Optional.of((String) namedList.get(headerName));
    }
    log.debug("Unable to extract header value: {}", headerName);
    return Optional.empty();
  }

  private static Long getStartValue(final QueryResponse queryResponse) {
    return Long.parseLong(getHeaderValue(queryResponse, "start").orElse("0"));
  }

  private static boolean getGrouping(final QueryResponse queryResponse) {
    return Boolean.parseBoolean(getHeaderValue(queryResponse, "group").orElse("false"));
  }

  private static Long getRowsValue(final QueryResponse queryResponse) {
    return Long.parseLong(getHeaderValue(queryResponse, "rows").orElse("0"));
  }

  /**
   * creates a response object containing a list of HspObjectGroups and the related metadata
   *
   * @param queryResponse the query response from solr
   * @return the response object
   */
  public static List<HspObjectGroup> getHSPObjectGroups(final QueryResponse queryResponse) {
    if (queryResponse.getException() != null) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, queryResponse.getException()
          .getMessage());
    }
    return QueryResponse2ResponseEntityConverter.extractHspObjectGroups(queryResponse);
  }

  public static List<String> getHspObjectGroupIds(final QueryResponse queryResponse) {
    if (queryResponse.getException() != null) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, queryResponse.getException()
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
   * Will return an empty list for grouped queries
   *
   * @param queryResponse the queryResponse containing the result data in JSON format
   * @param clazz         the Class the response should be mapped to
   * @param <T>           the extraction's target type
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
      log.warn("Error while mapping object to class {}, will skip.", clazz, e);
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
    long result = 0;
    if (queryResponse != null) {
      if (getGrouping(queryResponse)) {
        result = queryResponse.getGroupResponse()
            .getValues()
            .get(0)
            .getNGroups();
      } else if (Objects.nonNull(queryResponse.getResults())) {
        result = queryResponse.getResults()
            .getNumFound();
      }
    }
    return result;
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
   * "KOD-ID": {
   *   "aufbewahrungsOrt": [
   *     "consequat <em>cupidatat</em> nisi"
   *   ]
   * }
   * </pre>
   *
   * @param queryResponse the query response from solr
   * @return
   */
  public static Map<String, Map<String, List<String>>> extractHighlightingFromQueryResponse(final QueryResponse queryResponse, final HighlightConfig highlightConfig) {
    if (queryResponse == null || queryResponse.getResponse() == null) {
      return Map.of();
    }
    return prepareHighlightInformation(queryResponse.getHighlighting(), highlightConfig);
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
    if(queryResponse == null || queryResponse.getFacetFields() == null) {
      return Collections.emptyMap();
    }

    /* if the item's name is null, sort to the end, otherwise use the item's count to decide its position */
    final Comparator<Count> comp = (o1, o2) -> {
      if(o1.getName() == null && o2.getName() != null) {
        return 1;
      }
      if(o2.getName() == null && o1.getName() != null) {
        return -1;
      }
      return Long.compare(o1.getCount(), o2.getCount());
    };

    final Map<String, Map<String, Long>> ret = new HashMap<>();
    for (FacetField ff : queryResponse.getFacetFields()) {
      Map<String, Long> items = ff.getValues()
          .stream()
          .filter(v -> v.getName() != null || v.getCount() != 0)
          .sorted(comp.reversed())
          // null to __MISSING__ conversion could be done with per JsonSerializer annotation, but it would be a lot more complex
          .collect(Collectors.toMap(fc -> fc.getName() == null ? FIELD_NAME_MISSING : fc.getName(), FacetField.Count::getCount, (v1, v2) -> v2, LinkedHashMap::new));
      ret.put(ff.getName(), items);
    }
    return ret;
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
  public static MetaData extractMetadata(final QueryResponse queryResponse, final HighlightConfig highlightConfig) {
    final String spellCorrectedTerm = extractSpellCorrectedTerm(queryResponse);
    final long numFound = QueryResponse2ResponseEntityConverter.extractNumFound(queryResponse);
    final long start = getStartValue(queryResponse);
    final long rows = getRowsValue(queryResponse);
    final Map<String, Map<String, Long>> facets = extractFacetsFromQueryResponse(queryResponse);
    final Map<String, Stats> stats = extractStatsFromQueryResponse(queryResponse);
    final Map<String, Map<String, List<String>>> highlighting = extractHighlightingFromQueryResponse(queryResponse, highlightConfig);

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

  /**
   * Extracts a list of {@link FieldInformation} from a {@link SchemaResponse.FieldsResponse}
   * @param fieldsResponse containing the information to map
   * @return a {@link List} of {@link FieldInformation} based on the {@code fieldResponse}'s information
   */
  public static List<FieldInformation> extractFieldInformation(SchemaResponse.FieldsResponse fieldsResponse) {
    final List<FieldInformation> result = new LinkedList<>();
    for(Map<String, Object> field: fieldsResponse.getFields()) {
      FieldInformation fieldInformation = extractFieldInformation(field);
      result.add(fieldInformation);
    }

    return result;
  }

  /**
   * Extracts a list of {@link FieldTypeInformation} from a {@link SchemaResponse.FieldTypesResponse}
   * @param fieldTypesResponse containing the information to map
   * @return a {@link List} of {@link FieldTypeInformation} based on the {@code fieldTypesResponse}'s information
   */
  public static List<FieldTypeInformation> extractFieldTypeInformation(SchemaResponse.FieldTypesResponse fieldTypesResponse) {
    final List<FieldTypeInformation> result = new LinkedList<>();
    for(FieldTypeRepresentation fieldTypeRepresentation: fieldTypesResponse.getFieldTypes()) {
      FieldTypeInformation fieldTypeInformation = extractFieldTypeInformation(fieldTypeRepresentation.getAttributes());
      result.add(fieldTypeInformation);
    }

    return result;
  }

  public static EnumsConfig extractEnumConfig(final SolrResponseBase fileResponse) {
    final String responseXml = (String) fileResponse.getResponse().get("response");
    try {
      return xmlMapper.readValue(responseXml, EnumsConfig.class);
    }
    catch(JsonProcessingException e) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST, e.getMessage());
    }
  }

  /**
   * Creates an instance of {@link FieldInformation} based on an attributes map
   * @param fieldMap the map containing the attribut's information
   * @return the {@code FieldInformation}
   */
  private static FieldInformation extractFieldInformation(final Map<String, Object> fieldMap) {
    return mapper.convertValue(fieldMap, FieldInformation.class);
  }

  /**
   * Extracts a field type's attributes
   * @param fieldTypeMap a {@link Map} containing the field type's attributes
   * @return {@link FieldInformation} instance containing the extracted information
   */
  private static FieldTypeInformation extractFieldTypeInformation(final Map<String, Object> fieldTypeMap) {
    return mapper.convertValue(fieldTypeMap, FieldTypeInformation.class);
  }

  private static Map<String, Map<String, List<String>>> prepareHighlightInformation(
      @Nullable Map<String, Map<String, List<String>>> highlighting, final HighlightConfig highlightConfig) {
    if (highlighting == null) {
      return Map.of();
    }

    return highlighting.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                  Map<String, List<String>> hl = e.getValue();
                  hl = mergeHighlightFields(hl, highlightConfig.getTagName());
                  hl = mergeHighlightedTerms(hl, highlightConfig.getTagName());
                  hl = createFragments(hl, highlightConfig.getTagName(), highlightConfig.getPadding());
                  return hl;
                }
            )
        );
  }

  private static Map<String, List<String>> mergeHighlightFields(final Map<String, List<String>> highlightInfo, final String tagName) {
    return highlightInfo.entrySet().stream().collect(Collectors.toMap(
        k -> FieldProvider.removeOptionalSuffix(k.getKey()),
        Map.Entry::getValue,
        (hl1, hl2) -> HighlightHelper.mergeHighlights(hl1, hl2, tagName)
        ));
  }

  /**
   * Merges all occurrences of contiguous highlight fragments (wrapped by <em></em>)
   * highlight data might look like this (as JSON representation)
   * <pre>
   * {@code
   * "repository-search": [
   *   "consequat <em>cupidatat</em> nisi"
   * ]
   * </pre>
   *
   * @param highlighting the highlighting information squeezed into some maps
   * @return the highlighting information containing the merged fragments
   */
  private static Map<String, List<String>> mergeHighlightedTerms(final Map<String, List<String>> highlighting, final String tagName) {
    return highlighting.entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            hl -> hl.getValue().stream().map(v -> HighlightHelper.mergeContiguousElements(v, tagName)).collect(Collectors.toList())
        ));
  }

  private static Map<String, List<String>> createFragments(Map<String, List<String>> highlighting, final String tagName, final int padding) {
    return highlighting.entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            hl -> hl.getValue().stream().map(v -> FragmentHelper.fragmentHighlightInformation(v, tagName, padding)).flatMap(Stream::of).collect(Collectors.toList())
        ));
  }
}
