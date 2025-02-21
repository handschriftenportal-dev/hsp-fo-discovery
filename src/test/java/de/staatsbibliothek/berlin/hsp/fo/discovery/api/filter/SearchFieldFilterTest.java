package de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ConfigBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SearchFieldFilterTest {

  final SearchFieldFilter searchFieldFilter = new SearchFieldFilter(ConfigBuilder.getFieldProvider(
      List.of("orig-place-search", "binding-orig-place-search", "fulltext-search"),
      Map.of("FIELD-GROUP-ORIGIN", List.of("orig-place-search", "binding-orig-place-search"))));

  @Test
  void whenCalledWithFieldGroup_thenFieldGroupIsReplacedByFieldNames() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN");

    final List<String> filteredFields = searchFieldFilter.filter(fields);

    assertThat(filteredFields, containsInAnyOrder("orig-place-search", "binding-orig-place-search"));
  }

  @Test
  void whenCalledWithInvalidField_thenInvalidFieldIsRemoved() {
    final List<String> fields = List.of("binding-orig-place-search", "invalid-search");

    final List<String> filteredFields = searchFieldFilter.filter(fields);

    assertThat(filteredFields, containsInAnyOrder("binding-orig-place-search"));
  }

  @Test
  void whenCalledWithInvalidAndFieldGroup_thenInvalidFieldIsRemovedAndFieldGroupIsReplacedByFieldNames() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN", "invalid-search");

    final List<String> filteredFields = searchFieldFilter.filter(fields);

    assertThat(filteredFields, containsInAnyOrder("orig-place-search", "binding-orig-place-search"));
  }

  @Test
  void whenCalledWithFieldGroupAndFieldThatsPartOfThisGroup_thenFieldsAreDistinct() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN", "binding-orig-place-search");

    final List<String> filteredFields = searchFieldFilter.filter(fields);

    assertThat(filteredFields, containsInAnyOrder("orig-place-search", "binding-orig-place-search"));
  }

  @Test
  void whenCalledWithFieldGroupAndFields_thenAllFieldsAreReturned() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN", "fulltext-search");

    final List<String> filteredFields = searchFieldFilter.filter(fields);

    assertThat(filteredFields, containsInAnyOrder("orig-place-search", "binding-orig-place-search", "fulltext-search"));
  }

  @Test
  void whenCalledWithoutValidFields_theEpptyListIsReturned() {
    final List<String> fields = List.of("invalid-field");

    final List<String> filteredFields = searchFieldFilter.filter(fields);

    assertThat(filteredFields, is(empty()));
  }
}
