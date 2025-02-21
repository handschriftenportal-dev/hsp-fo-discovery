package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SpellcheckHelperTest {
  @Test
  void givenFuzzySearchAndSpellCorrection_whenSearching_thenResultEqualsSpellCorrection() {
    final String searchPhrase = "Bärlin Leibzig";
    final String spellCorrection = "berlin leipzig";

    final String spellCorrectedSearchPhrase = SpellcheckHelper.applySpellCorrection(searchPhrase, spellCorrection);

    assertThat(spellCorrectedSearchPhrase, is("berlin leipzig"));
  }

  @Test
  void givenMixedSearchAndSpellCorrection_whenSearching_thenFuzzyPartsAreSpellCorrected() {
    final String searchPhrase = "Bärlin \"*bibliothek*\" Leibzig";
    final String spellCorrection = "berlin leipzig";

    final String spellCorrectedSearchPhrase = SpellcheckHelper.applySpellCorrection(searchPhrase, spellCorrection);

    assertThat(spellCorrectedSearchPhrase, is("berlin \"*bibliothek*\" leipzig"));
  }

  @Test
  void givenExactSearchWithoutSpellCorrection_whenSearching_thenSearchPhraseRemainsUnchanged() {
    final String searchPhrase = "\"*bibliothek*\" \"Leipzig\"";
    final String spellCorrection = "";

    final String spellCorrectedSearchPhrase = SpellcheckHelper.applySpellCorrection(searchPhrase, spellCorrection);

    assertThat(spellCorrectedSearchPhrase, is("\"*bibliothek*\" \"Leipzig\""));
  }

  @Test
  void givenEmptySearchWithoutSpellCorrection_whenSearching_thenResultIsEmpty() {
    final String searchPhrase = "";
    final String spellCorrection = "";

    final String spellCorrectedSearchPhrase = SpellcheckHelper.applySpellCorrection(searchPhrase, spellCorrection);

    assertThat(spellCorrectedSearchPhrase, is(""));
  }
}
