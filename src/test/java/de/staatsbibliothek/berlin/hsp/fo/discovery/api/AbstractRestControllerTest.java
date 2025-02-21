package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.HspFoDiscoveryTestApplication;
import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.SearchFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.*;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.CustomizedResponseEntityExceptionHandler;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.JsonResponseBuilder;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    HspFoDiscoveryTestApplication.class,
    AbstractRestControllerTest.FieldConfiguration.class,
    AbstractRestControllerTest.FieldGroupConfiguration.class,
    AbstractRestControllerTest.HighlightConfiguration.class,
    AbstractRestControllerTest.HspConfiguration.class,
    AbstractRestControllerTest.SolrTestConfiguration.class
})
public abstract class AbstractRestControllerTest {
  protected MockMvc mockMvc;
  protected JsonResponseBuilder jsonResponseBuilder;
  protected HspConfig config;

  @Autowired
  protected FieldProvider fieldProvider;

  @Autowired
  protected SearchFieldFilter searchFieldFilter;

  @EnableConfigurationProperties(HspConfig.class)
  public static class HspConfiguration {}

  @EnableConfigurationProperties(SolrConfig.class)
  public static class SolrTestConfiguration {}

  @EnableConfigurationProperties(HighlightConfig.class)
  public static class HighlightConfiguration {}

  @EnableConfigurationProperties(FieldConfig.class)
  public static class FieldConfiguration {}

  @EnableConfigurationProperties(FieldGroupConfig.class)
  public static class FieldGroupConfiguration {}

  public AbstractRestControllerTest() {
    this.jsonResponseBuilder = new JsonResponseBuilder();
  }

  @BeforeEach
  void setup() {
    this.mockMvc = buildSystem();
  }

  protected MockMvc buildSystem() {
    final Object controller = getControllerToTest();
    if(controller instanceof BaseEntityController) {
      ((BaseEntityController<?>)controller).setSearchFieldFilter(searchFieldFilter);
    }
    return MockMvcBuilders
        .standaloneSetup(controller, new CustomizedResponseEntityExceptionHandler())
        .setConversionService(addConverter(new FormattingConversionService()))
        .build();
  }

  public abstract Object getControllerToTest();

  public FormattingConversionService addConverter(final FormattingConversionService conversionService) {
    return conversionService;
  }
}
