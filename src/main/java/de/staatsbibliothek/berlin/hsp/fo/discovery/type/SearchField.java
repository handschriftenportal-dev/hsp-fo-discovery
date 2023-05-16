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

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Enum providing all searchable fields
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 * 
 */
public enum SearchField {

  ACCOMPANYING_MATERIAL("accompanying-material-search", true),
  BINDING("binding-search", true),
  BINDING_ORIG_PLACE("binding-orig-place-search", true),
  BOOKLET("booklet-search", true),
  COLLECTION("collection-search", true),
  DECORATION("decoration-search", true),
  EXPLICIT("explicit-search", true),
  FORMAT("format-search", 10),
  FRAGMENT("fragment-search", true),
  FULLTEXT("fulltext-search", true),
  GROUP_ID("group-id-search"),
  HISTORY("history-search", true),
  ID("id-search", 10),
  IDNO("idno-search", 1000, true),
  IDNO_ALT("idno-alternative-search", 500, true),
  INCIPIT("incipit-search", true),
  ITEM_ICONOGRAPHY("item-iconography-search", true),
  ITEM_MUSIC("item-music-search", true),
  ITEM_TEXT("item-text-search", true),
  LANGUAGE("language-search", 10, true),
  MATERIAL("material-search", 10, true),
  MS_IDENTIFIER("ms-identifier-search", 1000, true),
  OBJECT_TYPE("object-type-search", 10),
  ORIG_DATE_WHEN("orig-date-when-search", true),
  ORIG_PLACE("orig-place-search", 100, true),
  PERSON_AUTHOR("person-author-search", true),
  PERSON_BOOKBINDER("person-bookbinder-search", true),
  PERSON_COMMISSIONED_BY("person-commissioned-by-search", true),
  PERSON_CONSERVATOR("person-conservator-search", true),
  PERSON_ILLUMINATOR("person-illuminator-search", true),
  PERSON_MENTIONED_IN("person-mentioned-in-search", true),
  PERSON_OTHER("person-other-search", true),
  PERSON_PREVIOUS_OWNER("person-previous-owner-search", true),
  PERSON_SCRIBE("person-scribe-search", true),
  PERSON_TRANSLATOR("person-translator-search", true),
  PHYSICAL_DESCRIPTION("physical-description-search", true),
  REPOSITORY("repository-search", 100, true),
  SETTLEMENT("settlement-search", 100, true),
  STATUS("status-search", 10),
  TITLE("title-search", 200, true),
  WORK_TITLE("work-title-search", true),

  // description fields
  /* disabled as it seems not to be possible to highlight date fields */
  /* DESC_PUBLISH_DATE("desc-publish-date-search"), */
  DESC_AUTHORS("desc-author-search");

  private static final Map<String, SearchField> lookup = new HashMap<>();
  public static final String EXACT_SUFFIX = "-exact";

  static {
    Arrays.stream(SearchField.values()).forEach(sf -> lookup.put(sf.name, sf));
  }

  private final String name;
  private Integer boost;
  private boolean supportsExactSearch;

  SearchField(final String name) {
    this.name = name;
  }

  SearchField(final String name, final Integer boost) {
    this(name);
    this.boost = boost;
  }

  SearchField(final String name, final boolean supportsExactSearch) {
    this(name);
    this.supportsExactSearch = supportsExactSearch;
  }

  SearchField(final String name, final Integer boost, final boolean supportsExactSearch) {
    this(name, boost);
    this.supportsExactSearch = supportsExactSearch;
  }

  /**
   * returns the (solr field) name
   * @return the field's name in solr
   */
  public String getName() {
    return getName(false);
  }

  /**
   * returns the (solr field) name
   * @return the field's name in solr
   */
  public String getName(final boolean isExactSearch) {
    if(isExactSearch && supportsExactSearch) {
      return getNameForExactSearch(this.name);
    }
    return this.name;
  }

  /**
   * returns the boosting factor, if there is any
   * @return the boost factor
   */
  public Integer getBoost() {
    return this.boost;
  }

  /**
   * returns all (solr field) names of all searchable fields
   * @param isExactSearch whether to use the exact search field names or not
   * @return the names
   */
  public static String[] getNames(final SearchField[] searchFields, final boolean isExactSearch) {
    return Arrays.stream(searchFields).map(sf-> sf.getName(isExactSearch)).toArray(String[]::new);
  }

  /**
   * Get a {@code SearchField} by its field name
   * @param name the name of the search field
   * @return the {@code SearchField}
   */
  public static SearchField getByName(final String name) {
    return lookup.get(name);
  }

  /**
   * Get an {@code Array} of field names by a {@code List} of {@code SearchField}s
   * @param searchFields the
   * @param isExactSearch whether to use the exact search field names or not
   * @return the field names
   */
  public static String[] getNames(final SearchField[] searchFields, final boolean isExactSearch, final boolean boost) {
    return boost ? getNamesBoosted(searchFields, isExactSearch) : getNames(searchFields, isExactSearch);
  }

  /**
   * Get boosted field names
   * @param names the names to be boosted
   * @param isExactSearch whether this is a multi term expansion search or not
   * @return the boosted field names
   */
  public static String[] getNamesBoosted(final SearchField[] names, final boolean isExactSearch) {
    return Arrays.stream(names)
        .map(name -> (name.getName(isExactSearch)) + (name.getBoost() != null
            ? "^" + name.getBoost()
            : ""))
        .toArray(String[]::new);
  }


  /**
   * Converts the given field name {@code name} to the corresponding exact search field name, if possible
   * @param name the field name to convert
   * @return the exact search field name
   */
  public static String getNameForExactSearch(final String name) {
    if(getByName(name) != null && getByName(name).supportsExactSearch) {
      return String.format("%s%s", name, EXACT_SUFFIX);
    }
    return name;
  }

  /**
   * Converts the given field names {@code names} to the corresponding exact search field names, if possible
   * @param names the field names to convert
   * @return the exact search field names
   */
  public static String[] getNamesForExactSearch(final SearchField[] names) {
    if(names != null) {
      return Arrays.stream(names).map(name -> name.getName(true)).toArray(String[]::new);
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }
}
