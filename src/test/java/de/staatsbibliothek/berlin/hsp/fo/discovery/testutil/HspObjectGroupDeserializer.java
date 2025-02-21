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
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDigitized;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class HspObjectGroupDeserializer extends StdDeserializer<SolrDocumentList> {

  @Serial
  private static final long serialVersionUID = 1L;
  final static ObjectMapper objectMapper = new ObjectMapper();

  protected HspObjectGroupDeserializer(Class<?> vc) {
    super(vc);
  }

  public HspObjectGroupDeserializer() {
    this(HashMap.class);
  }

  @Override
  public SolrDocumentList deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    final TreeNode tn = p.readValueAsTree();

    SolrDocumentList solrDocList = new SolrDocumentList();

    if (tn.get("hspObject") != null) {
      solrDocList.add(new SolrDocument(
          objectMapper.readValue(tn.get("hspObject").toString(), HashMap.class)));
    }

    if (tn.get("hspDescriptions") != null) {
      HspDescription[] descs = objectMapper.readValue(tn.get("hspDescriptions").toString(),
          HspDescription[].class);
      for (HspDescription desc : descs) {
        solrDocList.add(objectMapper.convertValue(desc, SolrDocument.class));
      }
    }
    if (tn.get("hspDigitizeds") != null) {
      HspDigitized[] digits =
          objectMapper.readValue(tn.get("hspDigitizeds").toString(), HspDigitized[].class);
      for (HspDigitized digit : digits) {
        solrDocList.add(objectMapper.convertValue(digit, SolrDocument.class));
      }
    }

    return solrDocList;
  }
}
