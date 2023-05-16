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

import java.util.Arrays;
import java.util.EnumMap;

public enum SearchFieldStemmed {
  ACCOMPANYING_MATERIAL(SearchField.ACCOMPANYING_MATERIAL),
  BINDING(SearchField.BINDING),
  BINDING_ORIG_PLACE(SearchField.BINDING_ORIG_PLACE),
  BOOKLET(SearchField.BOOKLET),
  COLLECTION(SearchField.COLLECTION),
  DECORATION(SearchField.DECORATION),
  EXPLICIT(SearchField.EXPLICIT),
  FRAGMENT(SearchField.FRAGMENT),
  FULLTEXT(SearchField.FULLTEXT),
  HISTORY(SearchField.HISTORY),
  INCIPIT(SearchField.INCIPIT),
  ITEM_ICONOGRAPHY(SearchField.ITEM_ICONOGRAPHY),
  ITEM_MUSIC(SearchField.ITEM_MUSIC),
  ITEM_TEXT(SearchField.ITEM_TEXT),
  LANGUAGE(SearchField.LANGUAGE, 5),
  MATERIAL(SearchField.MATERIAL, 5),
  ORIG_DATE_WHEN(SearchField.ORIG_DATE_WHEN),
  ORIG_PLACE(SearchField.ORIG_PLACE, 20),
  PHYSICAL_DESCRIPTION(SearchField.PHYSICAL_DESCRIPTION),
  REPOSITORY(SearchField.REPOSITORY, 20),
  SETTLEMENT(SearchField.SETTLEMENT, 20),
  TITLE(SearchField.TITLE, 100),
  WORK_TITLE(SearchField.WORK_TITLE, 100);

  private final SearchField baseField;
  private final String fieldName;
  private Integer boost;

  public static final String STEMMED_SUFFIX = "-stemmed";

  private static final EnumMap<SearchField, SearchFieldStemmed> baseLookup = new EnumMap<>(SearchField.class);

  static {
    Arrays.stream(SearchFieldStemmed.values())
          .forEach(sf -> baseLookup.put(sf.baseField, sf));
  }

  SearchFieldStemmed(final SearchField searchField) {
    this.baseField = searchField;
    this.fieldName = searchField.getName() + STEMMED_SUFFIX;
  }

  SearchFieldStemmed(final SearchField searchField, final Integer boost) {
    this.baseField = searchField;
    this.fieldName = searchField.getName() + STEMMED_SUFFIX;
    this.boost = boost;
  }

  public String getName() {
    return this.fieldName;
  }

  public String getNameBoosted() {
    if (boost != null) {
      return String.format("%s^%s", fieldName, boost);
    }
    return getName();
  }

  public static String[] getNames(final SearchField[] baseFields, final boolean boosted) {
    return Arrays.stream(baseFields)
                 .filter(baseLookup::containsKey)
                 .map(sf -> boosted ? baseLookup.get(sf)
                                                .getNameBoosted() : baseLookup.get(sf).fieldName)
                 .toArray(String[]::new);
  }
}
