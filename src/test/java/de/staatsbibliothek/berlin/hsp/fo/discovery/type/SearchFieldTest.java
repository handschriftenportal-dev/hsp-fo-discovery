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

package de.staatsbibliothek.berlin.hsp.fo.discovery.type;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SearchFieldTest {

  @Test
  void whenGetExactNamesIsCalledOnTextField_thenFieldNameIsConvertedToExactFieldName() {
    final String result = SearchField.getNameForExactSearch("settlement-search");
    assertThat(result, is("settlement-search-exact"));
  }
  @Test
  void whenGetExactNamesIsCalledOnNonTextField_thenFieldNameIsNotConverted() {
    final String result = SearchField.getNameForExactSearch("status-search");
    assertThat(result, is("status-search"));
  }

  @Test
  void whenGetExactNamesIsCalledOnTextFields_thenFieldNamesAreConvertedToExactSearchNames() {
    final String[] result = SearchField.getNamesForExactSearch(Arrays.array(SearchField.SETTLEMENT, SearchField.REPOSITORY));
    assertThat(result, arrayWithSize(2));
    assertThat(result, arrayContainingInAnyOrder("settlement-search-exact", "repository-search-exact"));
  }

  @Test
  void whenGetExactNamesIsCalledOnNonTextFields_thenFieldNamesAreNotConverted() {
    final String[] result = SearchField.getNamesForExactSearch(Arrays.array(SearchField.STATUS, SearchField.OBJECT_TYPE));
    assertThat(result, arrayWithSize(2));
    assertThat(result, arrayContainingInAnyOrder("status-search", "object-type-search"));
  }

  @Test
  void whenGetNamesBoostedIsCalledAndIsExactSearchIsFalse_thenFieldNamesAreNotConverted() {
    final String[] result = SearchField.getNamesBoosted(Arrays.array(SearchField.SETTLEMENT), false);
    assertThat(result, arrayWithSize(1));
    assertThat(result, arrayContainingInAnyOrder("settlement-search^100"));
  }

  @Test
  void whenGetNamesBoostedIsCalledOnTextFieldsAndIsExactSearchIsFalse_thenFieldNamesAreNotConverted() {
    final String[] result = SearchField.getNamesBoosted(Arrays.array(SearchField.SETTLEMENT), false);
    assertThat(result, arrayWithSize(1));
    assertThat(result, arrayContainingInAnyOrder("settlement-search^100"));
  }

  @Test
  void whenGetNamesBoostedIsCalledAndIsExactSearchIsTrue_thenFieldNamesAreConverted() {
    final String[] result = SearchField.getNamesBoosted(Arrays.array(SearchField.SETTLEMENT, SearchField.REPOSITORY), true);
    assertThat(result, arrayWithSize(2));
    assertThat(result, arrayContainingInAnyOrder("settlement-search-exact^100", "repository-search-exact^100"));
  }

  @Test
  void whenGetNamesBoostedIsCalledOnNonTextFieldsAndIsExactSearchIsTrue_thenFieldNamesAreNotConverted() {
    final String[] result = SearchField.getNamesBoosted(Arrays.array(SearchField.STATUS, SearchField.OBJECT_TYPE), true);
    assertThat(result, arrayWithSize(2));
    assertThat(result, arrayContainingInAnyOrder("status-search^10", "object-type-search^10"));
  }

  @Test
  void whenGetNameIsCalledOnTextFieldAndIsExactSearchIsTrue_thenFieldNameIsConverted() {
    final String result = SearchField.SETTLEMENT.getName(true);

    assertThat(result, is("settlement-search-exact"));
  }

  @Test
  void whenGetNameIsCalledOnNonTextFieldAndIsExactSearchIsTrue_thenFieldNameIsNotConverted() {
    final String result = SearchField.STATUS.getName(true);

    assertThat(result, is("status-search"));
  }

  @Test
  void whenGetNameIsCalledOnTextFieldAndIsExactSearchIsFalse_thenFieldNameIsNotConverted() {
    final String result = SearchField.SETTLEMENT.getName(false);

    assertThat(result, is("settlement-search"));
  }

  @Test
  void whenGetNameIsCalledOnNonTextFieldAndIsExactSearchIsFalse_thenFieldNameIsNotConverted() {
    final String result = SearchField.STATUS.getName(false);

    assertThat(result, is("status-search"));
  }
}
