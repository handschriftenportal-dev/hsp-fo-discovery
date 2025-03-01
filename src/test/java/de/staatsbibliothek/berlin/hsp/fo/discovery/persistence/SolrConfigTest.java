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
package de.staatsbibliothek.berlin.hsp.fo.discovery.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Map;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.SolrConfig;
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
@SpringBootTest(classes = SolrConfigTest.TestConfiguration.class)
class SolrConfigTest extends AbstractConfigTest {

  @Autowired
  private SolrConfig solrConfig;

  public SolrConfigTest() throws IOException {
    super("integration");
  }

  @EnableConfigurationProperties(SolrConfig.class)
  public static class TestConfiguration {
  }

  /**
   * Tests if the solr configuration is populated correctly
   */
  @Test
  void testSolrConfiguration() {
    @SuppressWarnings("unchecked")
    final Map<String, Object> solrProps = (Map<String, Object>) getProfileValue("solr", null);

    final String expectedCore = (String) getProfileValue("core", solrProps);
    final String expectedHost = (String) getProfileValue("host", solrProps);
    final String expectedUrl = expectedHost + "/solr";

    assertThat(solrConfig.getCore(), is(expectedCore));
    assertThat(solrConfig.getHost(), is(expectedHost));
    assertThat(solrConfig.getUrl(), is(expectedUrl));
  }
}
