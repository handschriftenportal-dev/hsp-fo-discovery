package de.staatsbibliothek.berlin.hsp.fo.discovery;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("integration")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = HspFoDiscoveryApplication.class)
public class AbstractBaseIntegrationTest {

  protected static final String MISSING = "__MISSING__";

  protected WebTestClient webTestClient;

  @Autowired
  public void setWebTestClient(final WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  protected final WebTestClient.BodyContentSpec get(final String URI) {
    return webTestClient.get().uri(URI)
        .exchange()
        .expectStatus().is2xxSuccessful()
        .expectBody();
  }

  protected final WebTestClient.BodyContentSpec search(final String phrase) {
    final String URI = String.format("/hspobjects/search?q=%s&rows=0", phrase);
    return get(URI);
  }

  protected final WebTestClient.BodyContentSpec search() {
    return search("*");
  }
}
