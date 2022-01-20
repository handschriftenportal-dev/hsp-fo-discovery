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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.valueobject.HspTypes;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response.Stats;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Component
public class ResponseHelper {

  public static final String FIELD_NAME_EXCEPTION = "exception";
  public static final String FIELD_NAME_RESPONSE_HEADER = "responseHeader";
  public static final String FIELD_NAME_PARAMS = "params";
  public static final String FIELD_NAME_ROWS = "rows";
  public static final String FIELD_NAME_MISSING = "__MISSING__";
  public static final String FIELD_NAME_GROUP_ID = "group-id-display";

  private static final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  static {
    mapper.setDateFormat(new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH));
  }

  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ResponseHelper.class);

  private ResponseHelper() {}

  /**
   * creates a response object containing a list of HspObjectGroups and the related meta data
   * 
   * @param queryResponse the query response from solr
   * @return the response object
   */
  public static Result<List<HspObjectGroup>> createResponseWithHspGroups(final QueryResponse queryResponse, final long start, final long rows) {
    if (queryResponse.getException() != null) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST_EXCEPTION,
          queryResponse.getException().getMessage());
    }
    final Result<List<HspObjectGroup>> response = new Result<>();
    final MetaData metaData = extractExtendedMetaDataFromQueryResponse(queryResponse, start, rows);
    response.setPayload(ResponseHelper.extractHspObjectGroupsFromSolrResponse(queryResponse));
    response.setMetadata(metaData);
    return response;
  }

  /**
   * creates a response object containing a single HspObjectGroup and the related meta data
   * 
   * @param queryResponse the query response from solr
   * @return the response object
   */
  public static Result<List<HspObjectGroup>> createResponseWithHspObject(final QueryResponse queryResponse) {
    if (queryResponse.getException() != null) {
      throw ExceptionFactory.getException(ExceptionType.SOLR_REQUEST_EXCEPTION,
          queryResponse.getException().getMessage());
    }
    final Result<List<HspObjectGroup>> result = new Result<>();
    final MetaData metaData = new MetaData();
    result.setPayload(List.of(ResponseHelper.extractHspObjectGroupFromSolrResponse(queryResponse)));
    metaData.setHighlighting(extractHighlightingFragmentsFromQueryResponse(queryResponse));
    result.setMetadata(metaData);
    return result;
  }
  
  public static Result<List<String>> createResponseWithHspGroupIds(final QueryResponse queryResponse, final long start, final long rows) {
    final List<String> resultData = extractSolrDocumentIds(queryResponse);
    final MetaData metaData = extractExtendedMetaDataFromQueryResponse(queryResponse, start, rows);
    Result<List<String>> result = new Result<>();
    result.setPayload(resultData);
    result.setMetadata(metaData);
    return result;
    
  }

  /**
   * extracts and returns the first HspObjectGroup from a query response
   * 
   * @param queryResponse the query response from solr
   * @return the HspObjectGroups, null if there is no HspObjectGroup
   */
  public static HspObjectGroup extractHspObjectGroupFromSolrResponse(final QueryResponse queryResponse) {
    final List<HspObjectGroup> hogs =
        ResponseHelper.extractHspObjectGroupsFromSolrResponse(queryResponse);
    if (!hogs.isEmpty()) {
      return hogs.get(0);
    } else {
      return null;
    }
  }

  /**
   * extracts and returns all contained KulturObjektDokumente from a query response
   * 
   * @param queryResponse the query response from solr
   * @return the HspObjectGroup as List
   * @throws JsonProcessingException
   * @throws JsonMappingException
   */
  public static List<HspObjectGroup> extractHspObjectGroupsFromSolrResponse(final QueryResponse queryResponse) {
    final List<HspObjectGroup> ret = new ArrayList<>();

    if(queryResponse.getGroupResponse() != null) {
      final List<Group> groupList = queryResponse.getGroupResponse().getValues().get(0).getValues();
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

        if (type.equals(HspTypes.HSP_OBJECT.getValue())) {
          hog.setHspObject(extractHspObject(solrDoc));
        } else if (type.equals(HspTypes.HSP_DESCRIPTION.getValue())) {
          final HspDescription description = extractHspDescription(solrDoc);
          if (description != null) {
            hog.getHspDescriptions().add(description);
          }
        } else if (type.equals(HspTypes.HSP_DIGITIZED.getValue())) {
          final HspDigitized digitized = extractHspDigitized(solrDoc);
          if (digitized != null) {
            hog.getHspDigitizeds().add(digitized);
          }
        }
      }
    }
    return hog;
  }

  private static HspObject extractHspObject(final SolrDocument solrDoc) {
    try {
      return mapper.readValue(solrDoc.jsonStr(), HspObject.class);
    } catch (JsonProcessingException e) {
      logger.warn("error while processing hsp object, will skip {}: {}", e.getMessage(), e);
      return null;
    }
  }

  private static HspDescription extractHspDescription(final SolrDocument solrDoc) {
    try {
      return mapper.readValue(solrDoc.jsonStr(), HspDescription.class);
    } catch (JsonProcessingException e) {
      logger.warn("error while processing hsp description, will skip {}: {}", e.getMessage(), e);
      return null;
    }
  }

  private static HspDigitized extractHspDigitized(final SolrDocument solrDoc) {
    try {
      return mapper.readValue(solrDoc.jsonStr(), HspDigitized.class);
    } catch (JsonProcessingException e) {
      logger.warn("error while processing hsp digitized, will skip {}: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Extract fulltext from field "rendering" of HSP descriptions.
   *
   * @param queryResponse query response from solr
   * @return List of renderings as String for all found descriptions
   */
  public static List<String> extractHspDescriptionFulltextFromSolrResponse(final QueryResponse queryResponse) {
    List<String> result = new ArrayList<>();
    SolrDocumentList docList = queryResponse.getResults();
    if (docList == null) {
      return result;
    }
    for (SolrDocument doc : queryResponse.getResults()) {
      String rendering = (String) doc.getFieldValue(DisplayField.RENDERING.getName());
      if (rendering != null && !(rendering.trim().isEmpty())) {
        result.add(rendering);
      }
    }
    return result;
  }

  /**
   * extracts and returns the total amount of found document groups
   * 
   * @param queryResponse the query response from solr
   * @return the number of found groups
   */
  public static long extractNumGroupsFoundFromSolrResponse(final QueryResponse queryResponse) {
    if (queryResponse == null || queryResponse.getGroupResponse() == null) {
      return 0;
    }

    return queryResponse.getGroupResponse().getValues().get(0).getNGroups();
  }

  /**
   * extracts and returns the total amount of found documents (matches)
   * 
   * @param queryResponse the query response from solr
   * @return the number of matching documents
   */
  public static long extractNumDocsFoundFromSolrResponse(final QueryResponse queryResponse) {
    if (queryResponse == null || queryResponse.getGroupResponse() == null) {
      return 0;
    }

    return queryResponse.getGroupResponse().getValues().get(0).getMatches();
  }
  
  /**
   * extracts and returns the total collapsed (grouped) amount of found documents
   * 
   * @param queryResponse the query response from solr
   * @return the number of found collapsed (grouped) documents
   */
  public static long extractNumCollapsedDocsFoundFromSolrResponse(final QueryResponse queryResponse) {
    if (queryResponse == null || queryResponse.getResults() == null) {
      return 0;
    }

    return queryResponse.getResults().getNumFound();
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
   * 
   * @return
   */
  public static Map<String, Map<String, List<String>>> extractHighlightingFragmentsFromQueryResponse(final QueryResponse queryResponse) {
    if (queryResponse == null || queryResponse.getResponse() == null) {
      return null;
    }
    final Map<String, Map<String, List<String>>> highlightings = queryResponse.getHighlighting();
    if (highlightings == null) {
      return null;
    }
    return highlightings;
  }

  /**
   * extracts the faceting information from the query response and converts it into a map of maps.
   * an an example of resulting data is shown below as json representation:
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
   * 
   * @return the faceting information
   */
  public static Map<String, Map<String, Long>> extractFacetsFromQueryResponse(final QueryResponse queryResponse) {
    if (queryResponse != null && queryResponse.getFacetFields() != null) {
      final Map<String, Map<String, Long>> ret = new HashMap<>();
      for (FacetField ff : queryResponse.getFacetFields()) {
        // null to __MISSING__ conversion could be done with per JsonSerializer annotation, but it would be a lot more complex  
        Map<String, Long> items = ff.getValues().stream()
            .collect(Collectors.toMap(fc -> fc.getName() == null ? FIELD_NAME_MISSING : fc.getName(), fc -> fc.getCount()));
        if (items.containsKey(FIELD_NAME_MISSING) &&  items.get(FIELD_NAME_MISSING) == 0) {
          items.remove(FIELD_NAME_MISSING);
        }
        ret.put(ff.getName(), items);
      }
      return ret;
    }
    return null;
  }

  public static Map<String, HspObjectGroup> extractIndexMapFromQueryResponse(final QueryResponse queryResponse) {
    final List<String> ids = extractSolrDocumentIds(queryResponse);
    final Map<String, HspObjectGroup> ret = new LinkedHashMap<>();
    ids.stream().forEach(id -> ret.put(id, null));

    return ret;
  }

  public static Map<String, Stats> extractStatsFromQueryResponse(final QueryResponse queryResponse) {
    if (queryResponse != null && queryResponse.getFieldStatsInfo() != null) {
      final Map<String, Stats> ret = new HashMap<>();
      for (FieldStatsInfo stat : queryResponse.getFieldStatsInfo().values()) {
        ret.put(stat.getName(), new Stats((Double) stat.getMin(), (Double) stat.getMax(),
            stat.getCount(), stat.getMissing()));
      }
      return ret;
    }
    return null;
  }

  /**
   * extracts the meta data information and returns the result
   * 
   * @param queryResponse the query response from solr
   * @param start the document start param
   * @param rows the document row param
   * @return the meta data
   */
  public static MetaData extractExtendedMetaDataFromQueryResponse(final QueryResponse queryResponse, final long start, final long rows) {
    final long numGroupsFound = ResponseHelper.extractNumCollapsedDocsFoundFromSolrResponse(queryResponse);
    final Map<String, Map<String, Long>> facets = extractFacetsFromQueryResponse(queryResponse);
    final Map<String, Stats> stats = extractStatsFromQueryResponse(queryResponse);
    final MetaData ret = new MetaData.Builder()
        .withNumGroupsFound(numGroupsFound)
        .withRows(rows)
        .withStart(start)
        .build();
    final Map<String, Map<String, List<String>>> highlighting = extractHighlightingFragmentsFromQueryResponse(queryResponse);
    ret.setFacets(facets);
    ret.setHighlighting(highlighting);
    ret.setStats(stats);
    return ret;
  }

  public static List<String> extractSolrDocumentIdsFromGroupResponse(final QueryResponse queryResponse) {
    List<String> groupIds = new LinkedList<>();
    if (queryResponse != null && queryResponse.getGroupResponse() != null) {
      for (Group group : queryResponse.getGroupResponse().getValues().get(0).getValues()) {
        groupIds.add(group.getGroupValue());
      }
    }
    return groupIds;
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
    for (SolrDocument doc : queryResponse.getResults()) {
      String teiDoc = (String) doc.getFieldValue(DisplayField.TEI_DOCUMENT.getName());
      if (teiDoc != null && !(teiDoc.trim().isEmpty())) {
        result.add(teiDoc);
      }
    }
    return result;
  }
}
