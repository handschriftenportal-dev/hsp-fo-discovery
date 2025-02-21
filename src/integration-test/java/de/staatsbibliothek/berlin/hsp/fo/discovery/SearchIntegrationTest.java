package de.staatsbibliothek.berlin.hsp.fo.discovery;

import org.junit.jupiter.api.Test;

public class SearchIntegrationTest extends AbstractBaseIntegrationTest {
  @Test
  void givenMisspelledTerm_whenSearching_thenSpellCorrectionIsApplied() {
        search("Birlin")
        .jsonPath("$.metadata.spellCorrectedTerm").isEqualTo("berlin");
  }
}
