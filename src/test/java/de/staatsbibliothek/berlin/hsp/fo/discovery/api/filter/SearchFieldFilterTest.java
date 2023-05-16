/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;

class SearchFieldFilterTest {
  @Test
  void whenCalledWithFieldGroup_thenFieldGroupIsReplacedByFieldNames() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN");

    final SearchField[] filteredFields = SearchFieldFilter.filter(fields);

    assertThat(filteredFields, arrayContainingInAnyOrder(SearchField.ORIG_PLACE, SearchField.BINDING_ORIG_PLACE));
  }

  @Test
  void whenCalledWithInvalidField_thenInvalidFieldIsRemoved() {
    final List<String> fields = List.of("binding-orig-place-search", "invalid-search");

    final SearchField[] filteredFields = SearchFieldFilter.filter(fields);

    assertThat(filteredFields, arrayContainingInAnyOrder(SearchField.BINDING_ORIG_PLACE));
  }

  @Test
  void whenCalledWithInvalidAndFieldGroup_thenInvalidFieldIsRemovedAndFeldGroupIsReplacedByFieldNames() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN", "invalid-search");

    final SearchField[] filteredFields = SearchFieldFilter.filter(fields);

    assertThat(filteredFields, arrayContainingInAnyOrder(SearchField.ORIG_PLACE, SearchField.BINDING_ORIG_PLACE));
  }

  @Test
  void whenCalledWithFieldGroupAndFieldThatsPartOfThisGroup_thenFieldsAreDistinct() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN", "binding-orig-place-search");

    final SearchField[] filteredFields = SearchFieldFilter.filter(fields);

    assertThat(filteredFields, arrayWithSize(2));
    assertThat(filteredFields, arrayContainingInAnyOrder(SearchField.ORIG_PLACE, SearchField.BINDING_ORIG_PLACE));
  }

  @Test
  void whenCalledWithFieldGroupAndFields_thenAllFieldsAreReturned() {
    final List<String> fields = List.of("FIELD-GROUP-ORIGIN", "fulltext-search");

    final SearchField[] filteredFields = SearchFieldFilter.filter(fields);

    assertThat(filteredFields, arrayWithSize(3));
    assertThat(filteredFields, arrayContainingInAnyOrder(SearchField.ORIG_PLACE, SearchField.BINDING_ORIG_PLACE, SearchField.FULLTEXT));
  }

  @Test
  void whenCalledWithoutValidFields_theEpptyListIsReturned() {
    final List<String> fields = List.of("invalid-field");

    final SearchField[] filteredFields = SearchFieldFilter.filter(fields);

    assertThat(filteredFields, arrayWithSize(0));
  }
}
