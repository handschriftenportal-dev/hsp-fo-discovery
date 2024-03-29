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

package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.CustomizedResponseEntityExceptionHandler;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.SolrConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.JsonResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
@ActiveProfiles("integration")
@SpringBootTest(classes = {
    AbstractRestControllerTest.TestConfiguration.class,
    AbstractRestControllerTest.SolrTestConfiguration.class
})

public abstract class AbstractRestControllerTest {
  protected MockMvc mockMvc;
  protected JsonResponseBuilder jsonResponseBuilder;

  @EnableConfigurationProperties(HspConfig.class)
  public static class TestConfiguration {}

  @EnableConfigurationProperties(SolrConfig.class)
  public static class SolrTestConfiguration {}

  @Autowired
  public AbstractRestControllerTest() {
    this.jsonResponseBuilder = new JsonResponseBuilder();
  }

  @BeforeEach
  void setup() {
    this.mockMvc = buildSystem();
  }

  protected MockMvc buildSystem() {
    return MockMvcBuilders
        .standaloneSetup(getControllerToTest(), new CustomizedResponseEntityExceptionHandler())
        .setConversionService(addConverter(new FormattingConversionService()))
        .build();
  }

  public abstract Object getControllerToTest();

  public FormattingConversionService addConverter(final FormattingConversionService conversionService) {
    return conversionService;
  }
}
