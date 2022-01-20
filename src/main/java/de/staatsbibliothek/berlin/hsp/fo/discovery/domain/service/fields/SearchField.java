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
package de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 * 
 */
public enum SearchField {
  
  ACCOMPANYING_MATERIAL("accompanying-material-search"),
  BINDING("binding-search"),
  BINDING_ORIG_PLACE("binding-orig-place-search"),
  BOOKLET("booklet-search"),
  COLLECTION("collection-search"),
  DECORATION("decoration-search"),
  EXPLICIT("explicit-search"),
  FORMAT("format-search", Boost.P3),
  FRAGMENT("fragment-search"),
  FULLTEXT("fulltext-search"),
  GROUP_ID("group-id-search"),
  HISTORY("history-search"),
  ID("id-search", Boost.P1),
  IDNO("idno-search", Boost.P1),
  IDNO_ALT("idno-alternative-search"),
  INCIPIT("incipit-search"),
  ITEM_ICONOGRAPHY("item-iconography-search"),
  ITEM_MUSIC("item-music-search"),
  ITEM_TEXT("item-text-search"),
  LANGUAGE("language-search", Boost.P3),
  MATERIAL("material-search", Boost.P3),
  MS_IDENTIFIER("ms-identifier-search"),
  OBJECT_TYPE("object-type-search", Boost.P3),
  ORIG_DATE_WHEN("orig-date-when-search"),
  ORIG_PLACE("orig-place-search", Boost.P2),
  PERSON("person-search"),
  PHYSICAL_DESCRIPTION("physical-description-search"),
  REPOSITORY("repository-search", Boost.P2),
  SETTLEMENT("settlement-search", Boost.P2),
  STATUS("status-search", Boost.P3),
  TITLE("title-search", Boost.P1),
  WORK_TITLE("work-title-search"),

  // description fields
  /* disabled as it seems not to be possible to highlight date fields */
  /* DESC_PUBLISH_DATE("desc-publish-date-search"), */
  DESC_AUTHORS("desc-author-search");

  private static Map<String, SearchField> lookup = new HashMap<>();

  static {
    Arrays.stream(SearchField.values()).forEach(sf -> lookup.put(sf.name, sf));
  }

  private String name;
  private Float boost;

  private SearchField(final String name) {
    this.name = name;
  }

  private SearchField(final String name, final Float boost) {
    this(name);
    this.boost = boost;
  }

  public String getName() {
    return this.name;
  }

  public Float getBoost() {
    return this.boost;
  }

  public static String[] getNames() {
    return lookup.keySet().toArray(String[]::new);
  }

  public static String[] getNamesBoosted() {
    return Arrays.stream(SearchField.values())
        .map(v -> v.getName() + (v.getBoost() != null ? "^" + v.getBoost() : ""))
        .toArray(String[]::new);
  }

  public static String getNamesConcated() {
    return String.join(" ", getNames());
  }

  public static String getNamesBoostedConcated() {
    return String.join(" ", getNamesBoosted());
  }

  public static SearchField getByName(final String name) {
    return lookup.get(name);
  }

  public static List<SearchField> getByNames(final String[] names) {
    return Arrays.stream(names).map(name -> getByName(name)).collect(Collectors.toList());
  }

  public static String[] getByNamesBoosted(final String[] names) {
    return Arrays.stream(names)
        .map(name -> name + (getByName(name) != null && getByName(name).getBoost() != null
            ? "^" + getByName(name).getBoost()
            : ""))
        .toArray(String[]::new);
  }

  public static class Boost {
    private Boost() {}

    public static final Float P1 = 10.0f;
    public static final Float P2 = 7.0f;
    public static final Float P3 = 5.0f;
  }
}
