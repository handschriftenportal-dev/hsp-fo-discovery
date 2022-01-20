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
package de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.Fixtures;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
class HspDescriptionSetRenderingTest {

  private static final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Test
  void testRenderingDeserializer() throws Exception {
    HspDescription withoutRendering =
        mapper.readValue(Fixtures.DESCRIPTION_WITHOUT_RENDERING, HspDescription.class);
    assertThat(withoutRendering.isFulltextAvailable(), is(false));

    HspDescription withEmptyRendering =
        mapper.readValue(Fixtures.DESCRIPTION_WITH_EMPTY_RENDERING, HspDescription.class);
    assertThat(withEmptyRendering.isFulltextAvailable(), is(false));

    HspDescription withErroneousRendering =
        mapper.readValue(Fixtures.DESCRIPTION_WITH_ERRONEOUS_RENDERING, HspDescription.class);
    assertThat(withErroneousRendering.isFulltextAvailable(), is(false));

    HspDescription withRendering =
        mapper.readValue(Fixtures.DESCRIPTION_WITH_RENDERING, HspDescription.class);
    assertThat(withRendering.isFulltextAvailable(), is(true));
  }

  @Test
  void testHspBaseDeserialization() throws Exception {
    final String baseJson =
        "{\"id-display\": \"TestId\", \"group-id-display\":\"TestGroupId\", \"type-display\": \"hsp:base\"}";
    final HspBase hspBase = mapper.readValue(baseJson, HspBase.class);

    assertThat(hspBase.id, is("TestId"));
    assertThat(hspBase.groupId, is("TestGroupId"));
    assertThat(hspBase.type, is("hsp:base"));
  }

  @Test
  void testHspBaseSerialization() throws Exception {
    final HspBase hspBase = new HspBase("TestGroupId", "TestId", "hsp:base");

    final String jsonHspBase = mapper.writeValueAsString(hspBase);
    final TreeNode hspBaseNode = mapper.readTree(jsonHspBase);

    assertThat(hspBaseNode.get("group-id"), instanceOf(TextNode.class));
    assertThat(((TextNode) hspBaseNode.get("group-id")).asText(), is(equalTo("TestGroupId")));

    assertThat(hspBaseNode.get("id"), instanceOf(TextNode.class));
    assertThat(((TextNode) hspBaseNode.get("id")).asText(), is(equalTo("TestId")));

    assertThat(hspBaseNode.get("type"), instanceOf(TextNode.class));
    assertThat(((TextNode) hspBaseNode.get("type")).asText(), is(equalTo("hsp:base")));
  }
}
