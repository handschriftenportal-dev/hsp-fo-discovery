package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ConfigBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FieldProviderTest {

  @Test
  void givenBoostedExactField_whenGetExactFieldIsCalled_thenResultIsCorrect() {
    final FieldProvider fieldProvider = ConfigBuilder.getFieldProvider(List.of("field-search^10", "field-search-exact^5"), Collections.EMPTY_MAP);

    final Optional<String> exactFieldName = fieldProvider.getExactName("field-search");

    assertThat(exactFieldName, isPresentAndIs("field-search-exact^5"));
  }

  @Test
  void givenBoostedStemmedField_whenGetStemmedFieldIsCalled_thenResultIsCorrect() {
    final FieldProvider fieldProvider = ConfigBuilder.getFieldProvider(List.of("field-search^10", "field-search-stemmed^5"), Collections.EMPTY_MAP);

    final Optional<String> exactFieldName = fieldProvider.getStemmedName("field-search");

    assertThat(exactFieldName, isPresentAndIs("field-search-stemmed^5"));
  }

  @Test
  void givenFieldNameWithoutSuffix_whenRemovingOptionalSuffix_thenFieldNameRemainsTheSame() {
    final String fieldName = "test-search";

    final String result = FieldProvider.removeOptionalSuffix(fieldName);

    assertThat(result, is("test-search"));
  }

  @Test
  void givenFieldNameWithSuffix_whenRemovingOptionalSuffix_thenFieldNameSuffixIsRemoved() {
    final String fieldName = "test-search-suffix";

    final String result = FieldProvider.removeOptionalSuffix(fieldName);

    assertThat(result, is("test-search"));
  }
}
