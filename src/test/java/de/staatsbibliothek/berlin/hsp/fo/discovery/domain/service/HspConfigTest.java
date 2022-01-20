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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import de.staatsbibliothek.berlin.hsp.fo.discovery.AbstractConfigTest;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@ActiveProfiles("integration")
@SpringBootTest(classes = HspConfigTest.TestConfiguration.class)
class HspConfigTest extends AbstractConfigTest {

  @Autowired
  private HspConfig hspConfig;

  public HspConfigTest() throws IOException {
    super("integration");
  }

  @EnableConfigurationProperties(HspConfig.class)
  public static class TestConfiguration {
  }

  @Test
  @SuppressWarnings("unchecked")
  void testHspConfiguration() {
    final Map<String, Object> hspProps = (Map<String, Object>) getProfileValue("hsp", null);
    final List<String> expectedFacets = (List<String>) getProfileValue("facets", hspProps);
    final Map<String, Object> fragmentProps =
        (Map<String, Object>) getProfileValue("highlight", hspProps);
    
    final int expectedSnippetCount = (int) getProfileValue("snippetCount", fragmentProps);
    
    assertThat(hspConfig.getFacets().size(), is(expectedFacets.size()));
    for (int i = 0; i < hspConfig.getFacets().size(); i++) {
      assertThat(hspConfig.getFacets().get(i), is(expectedFacets.get(i)));
    }
    assertThat(hspConfig.getSnippetCount(), is(expectedSnippetCount));
  }

}
