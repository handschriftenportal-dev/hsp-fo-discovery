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
package de.staatsbibliothek.berlin.hsp.fo.discovery.model;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
class DeserializerTest {

  private static final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Test
  void testHspBaseDeserialization() throws Exception {
    final String baseJson =
        "{\"id-display\": \"TestId\", \"group-id-display\":\"TestGroupId\", \"type-display\": \"hsp:base\"}";
    final HspBaseEntity hspBaseEntity = mapper.readValue(baseJson, HspBaseEntity.class);

    assertThat(hspBaseEntity.id, is("TestId"));
    assertThat(hspBaseEntity.groupId, is("TestGroupId"));
    assertThat(hspBaseEntity.type, is("hsp:base"));
  }

  @Test
  void testHspBaseSerialization() throws Exception {
    final HspBaseEntity hspBaseEntity = new HspBaseEntity("TestGroupId", "TestId", "hsp:base");

    final String jsonHspBase = mapper.writeValueAsString(hspBaseEntity);
    final TreeNode hspBaseNode = mapper.readTree(jsonHspBase);

    assertThat(hspBaseNode.get("group-id"), instanceOf(TextNode.class));
    assertThat(((TextNode) hspBaseNode.get("group-id")).asText(), is(equalTo("TestGroupId")));

    assertThat(hspBaseNode.get("id"), instanceOf(TextNode.class));
    assertThat(((TextNode) hspBaseNode.get("id")).asText(), is(equalTo("TestId")));

    assertThat(hspBaseNode.get("type"), instanceOf(TextNode.class));
    assertThat(((TextNode) hspBaseNode.get("type")).asText(), is(equalTo("hsp:base")));
  }
}
