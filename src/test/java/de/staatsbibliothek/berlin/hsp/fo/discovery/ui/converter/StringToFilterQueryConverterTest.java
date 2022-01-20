/*
 * MIT License
 *
 * Copyright (c) 2021 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.staatsbibliothek.berlin.hsp.fo.discovery.ui.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Lutz Helm {@literal <helm@ub.uni-leipzig.de>}
 *
 */
class StringToFilterQueryConverterTest {

  private static final String DEFAULT_TYPE_FILTER =
      "type-facet:(\"hsp:object\" OR \"hsp:description\")";

  private static StringToFilterQueryConverter converter;
  
  @BeforeAll
  static void init() {
    final List<String> facets = Arrays.asList("foo", "foobar", "baz", "orig-date-from-facet", "orig-date-to-facet", "type-facet");
    converter = new StringToFilterQueryConverter(facets);
  }

  @Test
  void whenCalledWithEmptyObject_onlyDefaultFilterIsReturned() throws Exception {
    Map<String, String> onlyDefaultFilter = converter.convert("{}");
    assertThat(onlyDefaultFilter, aMapWithSize(1));
    assertThat(onlyDefaultFilter.containsKey(DEFAULT_TYPE_FILTER), is(true));
  }

  @Test
  void whenCalledWithStrings_simpleEqualsIsReturned() throws Exception {
    Map<String, String> filters =
        converter.convert("{ \"foo\": \"bar\", \"foobar\": \"baz\" }");
    assertThat(filters, aMapWithSize(3));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
    assertThat(filters, hasKey("foo:\"bar\""));
    assertThat(filters, hasKey("foobar:\"baz\""));
  }

  @Test
  void whenCalledWithEmptyStringOrNull_keyIsOmitted() throws Exception {
    Map<String, String> filters =
        converter.convert("{ \"foo\": \"bar\", \"foobar\": \"\", \"baz\": null }");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("foo:\"bar\""));
  }

  @Test
  void whenCalledWithArrayOfStrings_queryWithOrIsReturnedIfNecessary() throws Exception {
    Map<String, String> filterWithoutValueButDefaultFilter =
        converter.convert("{ \"foo\": [] }");
    assertThat(filterWithoutValueButDefaultFilter, aMapWithSize(1));
    assertThat(filterWithoutValueButDefaultFilter, hasKey(DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithOneValue =
        converter.convert("{ \"foo\": [\"bar\"] }");
    assertThat(filterWithOneValue, aMapWithSize(2));
    assertThat(filterWithOneValue, hasKey("foo:(\"bar\")"));
    assertThat(filterWithOneValue, hasKey(DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithThreeValues =
        converter.convert("{ \"foo\": [\"bar\", \"foobar\", \"baz\"] }");
    assertThat(filterWithThreeValues, aMapWithSize(2));
    assertThat(filterWithThreeValues, hasKey("foo:(\"bar\" OR \"foobar\" OR \"baz\")"));
    assertThat(filterWithThreeValues, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithArrayOfIntegers_rangeQueryIsReturned() throws Exception {
    Map<String, String> filterWithoutValueButTypeFilter =
        converter.convert("{ \"foo\": [] }");
    assertThat(filterWithoutValueButTypeFilter, aMapWithSize(1));
    assertThat(filterWithoutValueButTypeFilter, hasKey(DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithMissingBound =
        converter.convert("{ \"foo\": [1234] }");
    assertThat(filterWithMissingBound, aMapWithSize(1));
    assertThat(filterWithMissingBound, hasKey(DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithValidRange =
        converter.convert("{ \"foo\": { \"from\": 1010, \"to\":1234 }}");
    assertThat(filterWithValidRange, aMapWithSize(2));
    assertThat(filterWithValidRange, hasKey("foo:[1010 TO 1234]"));
  }

  @Test
  void whenCalledWithMixedValues_expectedQueriesAreReturned() throws Exception {
    Map<String, String> filters = converter.convert(
        "{ \"foo\": \"bar\", \"foobar\": [\"foo\", \"bar\", \"baz\"], \"baz\": { \"from\": 1010, \"to\": 1234 }}");
    assertThat(filters, aMapWithSize(4));
    assertThat(filters, hasKey("foo:\"bar\""));
    assertThat(filters, hasKey("foobar:(\"foo\" OR \"bar\" OR \"baz\")"));
    assertThat(filters, hasKey("baz:[1010 TO 1234]"));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithTypeFilter_defaultTypeFilterIsNotAdded() throws Exception {
    Map<String, String> filters = converter.convert("{ \"type-facet\": \"bar\" }");
    assertThat(filters, aMapWithSize(1));
    assertThat(filters, hasKey("type-facet:\"bar\""));
  }

  @Test
  void whenCalledWithoutTypeFilter_defaultTypeFilterIsAdded() throws Exception {
    Map<String, String> filters = converter.convert("{ \"foo\": \"bar\" }");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("foo:\"bar\""));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithMissingOptionAndMultivalue_complexQueryIsReturned() throws Exception {
    Map<String, String> filters =
        converter.convert("{ \"foo\": [\"bar\", \"baz\", \"__MISSING__\"] }");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("(*:* NOT foo:*) OR (foo:(\"bar\" OR \"baz\"))"));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithMissingOption_queryForEmptyIsReturned() throws Exception {
    Map<String, String> filters =
        converter.convert("{ \"foo\": [\"__MISSING__\"] }");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("-foo:[* TO *]"));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacet_CorrectQueryIsReturned() throws Exception {
    String expectedQuery =
        "(orig-date-to-facet:[1234 TO *] OR (*:* NOT orig-date-to-facet:*)) AND (orig-date-from-facet:[* TO 1337] OR (*:* NOT orig-date-from-facet:*)) AND (orig-date-from-facet:[* TO *] OR orig-date-to-facet:[* TO *])";

    Map<String, String> filters = converter
        .convert("{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337 }}");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));

    filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"exact\": false, \"missing\": false }}");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacetWithMissingOption_CorrectQueryIsReturned() throws Exception {
    String expectedQuery =
        "(orig-date-to-facet:[1234 TO *] OR (*:* NOT orig-date-to-facet:*)) AND (orig-date-from-facet:[* TO 1337] OR (*:* NOT orig-date-from-facet:*))";

    Map<String, String> filters = converter
        .convert("{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"missing\": true }}");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));

    filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"missing\": true, \"exact\": false }}");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacetWithExactOption_CorrectQueryIsReturned() throws Exception {
    String expectedQuery = "(orig-date-from-facet:[1234 TO *] AND orig-date-to-facet:[* TO 1337])";

    Map<String, String> filters = converter
        .convert("{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"exact\": true }}");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));

    filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"missing\": false, \"exact\": true }}");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacetWithExactAndMissingOption_CorrectQueryIsReturned()
      throws Exception {
    Map<String, String> filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"exact\": true, \"missing\": true }}");
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(
        "(orig-date-from-facet:[1234 TO *] AND orig-date-to-facet:[* TO 1337]) OR ((*:* NOT orig-date-from-facet:*) AND (*:* NOT orig-date-to-facet:*))"));
    assertThat(filters, hasKey(DEFAULT_TYPE_FILTER));
  }
}
