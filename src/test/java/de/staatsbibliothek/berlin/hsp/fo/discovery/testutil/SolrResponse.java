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
package de.staatsbibliothek.berlin.hsp.fo.discovery.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class SolrResponse {

  private static final String FIELD_NAME_RESPONSE = "response";
  private static final String FIELD_NAME_HIGHLIGHTING = "highlighting";
  private static final String FIELD_NAME_PARAMS = "params";
  private static final String FIELD_NAME_FACET_INFO = "facet_counts";
  private static final String FIELD_NAME_FACET_FIELDS = "facet_fields";
  private static final String HEADER_NAME_GROUP = "group";

  private final NamedList<String> exception;
  private final NamedList<NamedList<Number>> facets;
  private final SimpleOrderedMap<NamedList<List<String>>> highlightingFragments;
  private final HspObjectGroup[] hspObjectGroups;
  private final HspObject[] hspObjects;
  private final SimpleOrderedMap<Object> params;
  private final TestHspObject[] teiDocumentObjects;
  private static final ObjectMapper mapper;
  
  static {
    mapper = new ObjectMapper();
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(SolrDocumentList.class, new HspObjectGroupDeserializer());
    SolrResponse.mapper.registerModule(simpleModule);
  }

  private SolrResponse(final Builder builder) {
    this.exception = builder.exception;
    this.facets = builder.facets;
    this.highlightingFragments = builder.highlightingFragments;
    this.hspObjectGroups = builder.hspObjectGroups;
    this.hspObjects = builder.hspObjects;
    this.params = builder.params;
    this.teiDocumentObjects = builder.teiDocumentObjects;
  }
  
  public static final class Builder {
    private NamedList<String> exception;
    private NamedList<NamedList<Number>> facets;
    private SimpleOrderedMap<NamedList<List<String>>> highlightingFragments;
    private HspObjectGroup[] hspObjectGroups;
    private HspObject[] hspObjects;
    private final SimpleOrderedMap<Object> params;
    private TestHspObject[] teiDocumentObjects;
    
    public Builder() {
      this.params = new SimpleOrderedMap<>();
    }
    
    public SolrResponse build() {
      return new SolrResponse(this);
    }
    
    public Builder withHspObjectGroups(final HspObjectGroup... hspObjectGroup) {
      this.hspObjectGroups = hspObjectGroup;
      return this;
    }
    
    public Builder withTeiDocumentObjects(TestHspObject... teiDocumentObjects) {
      this.teiDocumentObjects = teiDocumentObjects;
      return this;
    }

    public Builder withFacets(final NamedList<NamedList<Number>> facets) {
      this.facets = facets;
      return this;
    }
    
    public Builder withHighlightingInformation(final String HSPGroupID, final NamedList<List<String>> highlightingFragments) {
      this.highlightingFragments = new SimpleOrderedMap<>();
      this.highlightingFragments.add(HSPGroupID, highlightingFragments);
      return this;
    }
    
    public Builder withSolrException(final String message) {
      this.exception = new NamedList<>(Map.of("msg", message));
      return this;
    }

    public Builder withSortParam(final String sortParam) {
      this.params.add("sort", sortParam);
      return this;
    }

    public Builder withAdditionalParams(final Map<String, Object> params) {
      this.params.addAll(params);
      return this;
    }

    public Builder withHspObjects(final HspObject... hspObjects) {
      this.hspObjects = hspObjects;
      return this;
    }
  }

  //ToDo refactor
  public QueryResponse convert() throws JsonProcessingException {
    final QueryResponse response = new QueryResponse();
    final NamedList<Object> responseItems = new NamedList<>();
    final SolrDocumentList documentList = new SolrDocumentList();
    final NamedList<Object> responseHeaders = new NamedList<>();
    
    if (this.hspObjectGroups != null && this.hspObjectGroups.length > 0) {
      this.params.add(HEADER_NAME_GROUP, "true");
      final SimpleOrderedMap<Object> fieldGroups = new SimpleOrderedMap<>();
      final NamedList<Object> groupedInfo = new NamedList<>();
      final ArrayList<Object> groups = new ArrayList<>();
      documentList.setNumFound(this.hspObjectGroups.length);
      int matches = 0;

      for (HspObjectGroup hspObjectGroup : this.hspObjectGroups) {
        // serialize and deserialize the object to make things a bit more comfortable
        final String tmpJson = mapper.writeValueAsString(hspObjectGroup);
        final SolrDocumentList docList = mapper.readValue(tmpJson, SolrDocumentList.class);
        // dirty hack: fix technical fields

        docList.forEach(this::manipulateTechnicalFields);

        final SimpleOrderedMap<Object> grpMap = new SimpleOrderedMap<>();
        grpMap.add("doclist", docList);
        grpMap.add("groupValue", hspObjectGroup.getHspObject().getId());

        groups.add(grpMap);
        matches += 1 + (hspObjectGroup.getHspDescriptions() == null ? 0
            : hspObjectGroup.getHspDescriptions().size());
      }

      fieldGroups.add("matches", matches);
      fieldGroups.add("ngroups", this.hspObjectGroups.length);
      fieldGroups.add("groups", groups);

      groupedInfo.add("groupId", fieldGroups);
      responseItems.add("grouped", groupedInfo);
    } else {
      this.params.add(HEADER_NAME_GROUP, "false");
      if (this.teiDocumentObjects != null && this.teiDocumentObjects.length > 0) {
        for (TestHspObject obj : this.teiDocumentObjects) {
          documentList.add(toSolrDocument(obj));
        }
      } else if (this.hspObjects != null) {
        for (HspObject obj : this.hspObjects) {
          final SolrDocument doc = toSolrDocument(obj);
          manipulateTechnicalFields(doc);
          documentList.add(doc);
        }
      }
      documentList.setNumFound(documentList.size());
    }
    responseHeaders.add(FIELD_NAME_PARAMS, this.params);
    responseItems.add(FIELD_NAME_RESPONSE, documentList);
    responseItems.add(QueryResponse2ResponseEntityConverter.FIELD_NAME_RESPONSE_HEADER, responseHeaders);

    if (this.highlightingFragments != null && this.highlightingFragments.size() > 0) {
      responseItems.add(FIELD_NAME_HIGHLIGHTING, this.highlightingFragments);
    }

    if (this.facets != null && this.facets.size() > 0) {

      final NamedList<Object> facet_info = new NamedList<>();
      final NamedList<NamedList<Number>> facetFields = new NamedList<>();
      for (Map.Entry<String, NamedList<Number>> facet : this.facets) {
        facetFields.add(facet.getKey(), facet.getValue());
      }
      facet_info.add(FIELD_NAME_FACET_FIELDS, facetFields);
      responseItems.add(FIELD_NAME_FACET_INFO, facet_info);
    }

    responseItems.add(QueryResponse2ResponseEntityConverter.FIELD_NAME_EXCEPTION, exception);
    response.setResponse(responseItems);
    return response;
  }
  
  public void manipulateTechnicalFields(final SolrDocument doc) {
    doc.put("id-display", doc.remove("id"));
    doc.put("group-id-display", doc.remove("group-id"));
    doc.put("type-display", doc.remove("type"));
  }
  
  public SolrDocument toSolrDocument(final Object obj) throws JsonProcessingException {
    final String tmpJson = mapper.writeValueAsString(obj);
    return mapper.readValue(tmpJson, SolrDocument.class);
  }
}
