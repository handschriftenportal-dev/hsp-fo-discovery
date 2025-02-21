package de.staatsbibliothek.berlin.hsp.fo.discovery;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class FacetIntegrationTest extends AbstractBaseIntegrationTest {

  @Test
  void whenSearching_thenFormatFacetIsProvided() {
    search().jsonPath("$.metadata.facets['format-facet']").value(allOf(
        hasKey("quarto"),
        hasKey("folio"),
        hasKey("octavo"),
        hasKey(MISSING)
        ));
  }

  @Test
  void whenSearching_thenHasNotationFacetIsProvided() {
    search().jsonPath("$.metadata.facets['has-notation-facet']").value(allOf(
        hasKey("yes"),
        hasKey("no"),
        hasKey(MISSING)
    ));
  }

  @Test
  void whenSearching_thenIlluminatedFacetIsProvided() {
    search().jsonPath("$.metadata.facets['illuminated-facet']").value(allOf(
        hasKey("yes"),
        hasKey("no"),
        hasKey(MISSING)
    ));
  }

  @Test
  void whenSearching_thenLanguageFacetIsProvided() {
    search().jsonPath("$.metadata.facets['language-facet']").value(allOf(
        hasKey("la"),
        hasKey("de"),
        hasKey("fr"),
        hasKey(MISSING)
    ));
  }

  @Test
  void whenSearching_thenMaterialFacetIsProvided() {
    search().jsonPath("$.metadata.facets['material-facet']").value(allOf(
        hasKey("paper"),
        hasKey("parchment"),
        hasKey("other"),
        hasKey(MISSING)
        ));
  }


  @Test
  void whenSearching_thenObjectTypeFacetIsProvided() {
    search().jsonPath("$.metadata.facets['object-type-facet']").value(allOf(
        hasKey("codex"),
        hasKey("fragment"),
        hasKey("composite"),
        hasKey(MISSING)
        ));
  }

  @Test
  void whenSearching_thenOrigDateTypeFacetIsProvided() {
    search().jsonPath("$.metadata.facets['orig-date-type-facet']").value(allOf(
        hasKey("datable"),
        hasKey("dated"),
        hasKey(MISSING)
        ));
  }

  @Test
  void whenSearching_thenOrigPlaceFacetIsProvided() {
    search().jsonPath("$.metadata.facets['orig-place-facet']").value(allOf(
        hasKey(MISSING),
        hasKey("Deutschland"),
        hasKey("Italien"),
        hasKey("Süddeutschland")
    ));
  }

  @Test
  void whenSearching_thenRepositoryFacetIsProvided() {
    search().jsonPath("$.metadata.facets['repository-facet']").value(allOf(
        hasKey("Bayerische Staatsbibliothek"),
        hasKey("Staatsbibliothek zu Berlin"),
        hasKey("Herzog August Bibliothek Wolfenbüttel"),
        hasKey("Sächsische Landesbibliothek – Staats- und Universitätsbibliothek Dresden")
    ));
  }

  @Test
  void whenSearching_thenSettlementFacetIsProvided() {
    search().jsonPath("$.metadata.facets['settlement-facet']").value(allOf(
        hasKey("München"),
        hasKey("Berlin"),
        hasKey("Wolfenbüttel"),
        hasKey("Dresden")));
  }

  @Test
  void whenSearching_thenSettlementFacetDoesNotContainMissing() {
    search().jsonPath("$.metadata.facets['settlement-facet']").value(
        not(hasKey(MISSING)));
  }

  @Test
  void whenSearching_thenRepositoryFacetDoesNotContainMissing() {
    search().jsonPath("$.metadata.facets['repository-facet']").value(
        not(hasKey(MISSING)));
  }
}
