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
package de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

/**
 * 
 * @author Lutz Helm {@literal <helm@ub.uni-leipzig.de>}
 *
 */
class StringToFilterQueryConverterTest {
  private static final Map<String, Object> DEFAULT_TYPE_FILTER = Map.of("type-facet", List.of("hsp:object", "hsp:description", "hsp:description_retro"));
  private static final String CONVERTED_DEFAULT_TYPE_FILTER = "type-facet:(\"hsp:object\" OR \"hsp:description\" OR \"hsp:description_retro\")";
  private static StringToFilterQueryConverter converter;
  
  @BeforeAll
  static void init() {
    final List<String> facets = Arrays.asList("foo", "foobar", "baz", "orig-date-from-facet", "orig-date-to-facet", "type-facet");
    converter = new StringToFilterQueryConverter(facets);
  }

  @Test
  void whenCalledWithEmptyObject_thenOnlyDefaultFilterIsReturned() {
    Map<String, String> onlyDefaultFilter = converter.convert("{}", DEFAULT_TYPE_FILTER);
    assertThat(onlyDefaultFilter, aMapWithSize(1));
    assertThat(onlyDefaultFilter.containsKey(CONVERTED_DEFAULT_TYPE_FILTER), is(true));
  }

  @Test
  void whenCalledWithStrings_thenSimpleEqualsIsReturned() {
    Map<String, String> filters =
        converter.convert("{ \"foo\": \"bar\", \"foobar\": \"baz\" }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(3));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
    assertThat(filters, hasKey("foo:\"bar\""));
    assertThat(filters, hasKey("foobar:\"baz\""));
  }

  @Test
  void whenCalledWithEmptyStringOrNull_thenKeyIsOmitted() {
    Map<String, String> filters =
        converter.convert("{ \"foo\": \"bar\", \"foobar\": \"\", \"baz\": null }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("foo:\"bar\""));
  }

  @Test
  void whenCalledWithArrayOfStrings_thenQueryWithOrIsReturnedIfNecessary() {
    Map<String, String> filterWithoutValueButDefaultFilter =
        converter.convert("{ \"foo\": [] }", DEFAULT_TYPE_FILTER);
    assertThat(filterWithoutValueButDefaultFilter, aMapWithSize(1));
    assertThat(filterWithoutValueButDefaultFilter, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithOneValue =
        converter.convert("{ \"foo\": [\"bar\"] }", DEFAULT_TYPE_FILTER);
    assertThat(filterWithOneValue, aMapWithSize(2));
    assertThat(filterWithOneValue, hasKey("foo:(\"bar\")"));
    assertThat(filterWithOneValue, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithThreeValues =
        converter.convert("{ \"foo\": [\"bar\", \"foobar\", \"baz\"] }", DEFAULT_TYPE_FILTER);
    assertThat(filterWithThreeValues, aMapWithSize(2));
    assertThat(filterWithThreeValues, hasKey("foo:(\"bar\" OR \"foobar\" OR \"baz\")"));
    assertThat(filterWithThreeValues, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithArrayOfIntegers_thenRangeQueryIsReturned() {
    Map<String, String> filterWithoutValueButTypeFilter =
        converter.convert("{ \"foo\": [] }", DEFAULT_TYPE_FILTER);
    assertThat(filterWithoutValueButTypeFilter, aMapWithSize(1));
    assertThat(filterWithoutValueButTypeFilter, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithMissingBound =
        converter.convert("{ \"foo\": [1234] }", DEFAULT_TYPE_FILTER);
    assertThat(filterWithMissingBound, aMapWithSize(1));
    assertThat(filterWithMissingBound, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));

    Map<String, String> filterWithValidRange =
        converter.convert("{ \"foo\": { \"from\": 1010, \"to\":1234 }}", DEFAULT_TYPE_FILTER);
    assertThat(filterWithValidRange, aMapWithSize(2));
    assertThat(filterWithValidRange, hasKey("foo:[1010 TO 1234]"));
  }

  @Test
  void givenArrayOfDecimals_whenConverting_thenRangeQueryIsReturned() {
    Map<String, String> filterWithDecimals = converter.convert("{ \"foo\": { \"from\": 1010.0, \"to\":1234.0 }}", DEFAULT_TYPE_FILTER);

    assertThat(filterWithDecimals, aMapWithSize(2));
    assertThat(filterWithDecimals, hasKey("foo:[1010.0 TO 1234.0]"));
  }

  @Test
  void whenCalledWithMixedValues_expectedQueriesAreReturned() {
    Map<String, String> filters = converter.convert(
        "{ \"foo\": \"bar\", \"foobar\": [\"foo\", \"bar\", \"baz\"], \"baz\": { \"from\": 1010, \"to\": 1234 }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(4));
    assertThat(filters, hasKey("foo:\"bar\""));
    assertThat(filters, hasKey("foobar:(\"foo\" OR \"bar\" OR \"baz\")"));
    assertThat(filters, hasKey("baz:[1010 TO 1234]"));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithTypeFilter_thenDefaultTypeFilterIsNotAdded() {
    Map<String, String> filters = converter.convert("{ \"type-facet\": \"bar\" }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(1));
    assertThat(filters, hasKey("type-facet:\"bar\""));
  }

  @Test
  void whenCalledWithoutTypeFilter_thenDefaultTypeFilterIsAdded() {
    Map<String, String> filters = converter.convert("{ \"foo\": \"bar\" }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("foo:\"bar\""));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithMissingOptionAndMultiValue_thenComplexQueryIsReturned() {
    Map<String, String> filters =
        converter.convert("{ \"foo\": [\"bar\", \"baz\", \"__MISSING__\"] }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("(*:* NOT foo:*) OR (foo:(\"bar\" OR \"baz\"))"));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithMissingOption_thenQueryForEmptyIsReturned() {
    Map<String, String> filters =
        converter.convert("{ \"foo\": [\"__MISSING__\"] }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("-foo:[* TO *]"));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacet_thenCorrectQueryIsReturned() {
    String expectedQuery =
        "(orig-date-to-facet:[1234 TO *] OR (*:* NOT orig-date-to-facet:*)) AND (orig-date-from-facet:[* TO 1337] OR (*:* NOT orig-date-from-facet:*)) AND (orig-date-from-facet:[* TO *] OR orig-date-to-facet:[* TO *])";

    Map<String, String> filters = converter
        .convert("{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337 }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));

    filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"exact\": false, \"missing\": false }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacetWithMissingOption_thenCorrectQueryIsReturned() {
    String expectedQuery =
        "(orig-date-to-facet:[1234 TO *] OR (*:* NOT orig-date-to-facet:*)) AND (orig-date-from-facet:[* TO 1337] OR (*:* NOT orig-date-from-facet:*))";

    Map<String, String> filters = converter
        .convert("{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"missing\": true }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));

    filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"missing\": true, \"exact\": false }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacetWithExactOption_thenCorrectQueryIsReturned() {
    String expectedQuery = "(orig-date-from-facet:[1234 TO *] AND orig-date-to-facet:[* TO 1337])";

    Map<String, String> filters = converter
        .convert("{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"exact\": true }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));

    filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"missing\": false, \"exact\": true }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(expectedQuery));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void whenCalledWithOrigDateFacetWithExactAndMissingOption_thenCorrectQueryIsReturned() {
    Map<String, String> filters = converter.convert(
        "{ \"orig-date-facet\": {\"from\": 1234 , \"to\": 1337, \"exact\": true, \"missing\": true }}", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey(
        "(orig-date-from-facet:[1234 TO *] AND orig-date-to-facet:[* TO 1337]) OR ((*:* NOT orig-date-from-facet:*) AND (*:* NOT orig-date-to-facet:*))"));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void givenBooleanValue_whenConverting_thenCorrectQueryIsReturned() {
    Map<String, String> filters = converter.convert("{ \"foo\": true }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("foo:true"));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }

  @Test
  void givenBooleanValueInArray_whenConverting_thenCorrectQueryIsReturned() {
    Map<String, String> filters = converter.convert("{ \"foo\": [ true ] }", DEFAULT_TYPE_FILTER);
    assertThat(filters, aMapWithSize(2));
    assertThat(filters, hasKey("foo:(true)"));
    assertThat(filters, hasKey(CONVERTED_DEFAULT_TYPE_FILTER));
  }
}
