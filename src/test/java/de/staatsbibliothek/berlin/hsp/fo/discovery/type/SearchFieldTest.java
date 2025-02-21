package de.staatsbibliothek.berlin.hsp.fo.discovery.type;

import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ConfigBuilder;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SearchFieldTest {

  private static FieldProvider fieldProvider;

  @BeforeAll
  public static void setup() {
    fieldProvider = ConfigBuilder.getFieldProvider(
        List.of("object-type-search",
            "repository-search",
            "repository-search-exact^100",
            "settlement-search",
            "settlement-search-exact^100",
            "status-search"),
        Collections.emptyMap());
  }

  @Test
  void whenGetExactNamesIsCalledOnTextField_thenFieldNameIsConvertedToExactFieldName() {
    final Optional<String> result = fieldProvider.getExactName("settlement-search");
    assertThat(result, isPresentAndIs("settlement-search-exact^100"));
  }
  @Test
  void whenGetExactNamesIsCalledOnNonTextField_thenFieldNameIsEmpty() {
    final Optional<String> result = fieldProvider.getExactName("status-search");
    assertThat(result, isEmpty());
  }

  @Test
  void whenGetExactNamesIsCalledOnTextFields_thenFieldNamesAreConvertedToExactSearchNames() {
    final List<String> result = fieldProvider.getExactNames(List.of("repository-search", "settlement-search"));
    assertThat(result, containsInAnyOrder("settlement-search-exact^100", "repository-search-exact^100"));
  }

  @Test
  void whenGetExactNamesIsCalledOnNonTextFields_thenNoFieldsAreReturned() {
    final List<String> result = fieldProvider.getExactNames(List.of("status-search", "object-type-search"));
    assertThat(result, is(empty()));
  }

  @Test
  void whenGetNamesBoostedIsCalledAndIsExactSearchIsTrue_thenFieldNamesAreConverted() {
    final List<String> result = fieldProvider.getExactNames(List.of("settlement-search", "repository-search"));
    assertThat(result, containsInAnyOrder("settlement-search-exact^100", "repository-search-exact^100"));
  }
}
