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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class JsonResponseBuilder {
  
  private final ObjectMapper objectMapper;
  private final HighlightConfig highlightConfig;
  private static final String FIELD_NAME_PAYLOAD = "payload";
  private static final String FIELD_NAME_METADATA = "metadata";

  public JsonResponseBuilder() {
    objectMapper = JsonMapper.builder()
        .configure(JsonWriteFeature.QUOTE_FIELD_NAMES, false)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .build();
    highlightConfig = new HighlightConfig(250, 100, 3, "em");
  }
  
  public <T> String getJson(final T obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }
  
  
  public <T> String getJsonResult(Result<T> result) throws JsonProcessingException {
    return objectMapper.writeValueAsString(result);
  }
  
  public String getJsonResponse(final QueryResponse solrResponse,
      final boolean multiExpected) {
    return getJsonResponse(solrResponse, multiExpected, 0, 10);
  }

  public String getJsonResponse(final QueryResponse solrResponse,
      final boolean multiExpected, final long start, final long rows) {
    final ObjectNode on = objectMapper.createObjectNode();
    final MetaData metaData;

    if (multiExpected) {
      final long numFound = solrResponse.getResults() != null ? solrResponse.getResults().size(): 0;
      final Map<String, Map<String, List<String>>> highlighting =
          QueryResponse2ResponseEntityConverter.extractHighlightingFromQueryResponse(solrResponse, highlightConfig);
      metaData = MetaData.builder()
          .withNumFound(numFound)
          .withRows(rows)
          .withStart(start)
          .withHighlighting(highlighting)
          .build();

      on.putPOJO(FIELD_NAME_PAYLOAD, QueryResponse2ResponseEntityConverter.extractHspObjectGroups(solrResponse));
    } else { 
      metaData = new MetaData();
      on.putPOJO(FIELD_NAME_PAYLOAD, QueryResponse2ResponseEntityConverter.extractHspObjectGroups(solrResponse).get(0));
    }
    on.putPOJO(FIELD_NAME_METADATA, metaData);

    return on.toString();
  }
}
