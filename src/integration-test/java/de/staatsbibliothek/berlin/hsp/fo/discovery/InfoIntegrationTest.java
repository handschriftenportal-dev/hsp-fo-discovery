package de.staatsbibliothek.berlin.hsp.fo.discovery;

import org.junit.jupiter.api.Test;

class InfoIntegrationTest extends AbstractBaseIntegrationTest {

  @Test
  void whenRequestingFieldInfo_thenIdSearchIsContained() {
    get("/info/fields").jsonPath("$.[?(@.name=='id-search')].type").isEqualTo("STRING");
  }
}