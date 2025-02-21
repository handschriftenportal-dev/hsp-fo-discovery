package de.staatsbibliothek.berlin.hsp.fo.discovery.type;

import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ConfigBuilder;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SearchFieldStemmedTest {

  private static FieldProvider fieldProvider;

  @BeforeAll
  public static void setup() {
    fieldProvider = ConfigBuilder.getFieldProvider(
        List.of("settlement-search^20",
            "binding-orig-place-search",
            "fulltext-search",
            "item-text-search",
            "item-text-search-stemmed^2"),
        Collections.emptyMap());
  }

  @Test
  void whenGetNameBoostedIsCalledForBoostedField_thenFieldIsReturnedBoosted() {
    final String actualField  = fieldProvider.getFieldNameWithBoosting("settlement-search");

    assertThat(actualField, is("settlement-search^20"));
  }

  @Test
  void whenGetNameBoostedIsCalledForUnboostedField_thenFieldIsReturnedUnboosted() {
    final String actualField  = fieldProvider.getFieldNameWithBoosting("item-text-search");

    assertThat(actualField, is("item-text-search"));
  }

  @Test
  void whenGetNamesIsCalledWithBoosting_thenCorrespondingStemmedFieldIsReturnedBoosted() {
    final Optional<String> actualFields  = fieldProvider.getStemmedName("item-text-search");

    assertThat(actualFields, isPresentAndIs("item-text-search-stemmed^2"));
  }
}
