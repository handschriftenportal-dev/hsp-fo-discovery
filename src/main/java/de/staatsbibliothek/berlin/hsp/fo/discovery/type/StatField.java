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
package de.staatsbibliothek.berlin.hsp.fo.discovery.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lutz Helm {@literal <helm@ub.uni-leipzig.de>}
 */
public enum StatField {

  HEIGHT("height-facet"),
  LEAVES_COUNT("leaves-count-facet"),
  ORIG_DATE_FROM("orig-date-from-facet", "orig-date-facet"),
  ORIG_DATE_TO("orig-date-to-facet", "orig-date-facet"),
  ORIG_DATE_WHEN("orig-date-when-facet"),
  PUBLISH_YEAR_FACET("publish-year-facet"),
  WIDTH("width-facet"),

  // description stats
  PUBLISH_DATE("publish-year-facet"),

  // catalog stats
  CATALOG_PUBLISH_YEAR("catalog-publish-year-facet");

  private static final Map<String, StatField> lookup = new HashMap<>();

  static {
    Arrays.stream(StatField.values())
        .forEach(sf -> lookup.put(sf.name, sf));
  }

  private final String name;
  private String tagName;

  StatField(final String name) {
    this.name = name;
  }

  StatField(final String name, String tagName) {
    this.name = name;
    this.tagName = tagName;
  }

  public String getValue() {
    return this.name;
  }

  public static String getTagNameForField(String fieldName) {
    for (StatField stat : StatField.values()) {
      if (stat.getValue()
          .equals(fieldName)) {
        return stat.tagName == null ? fieldName : stat.tagName;
      }
    }
    return null;
  }

  public static List<String> getNames() {
    return Arrays.stream(StatField.values())
        .map(StatField::getValue)
        .collect(Collectors.toList());
  }
}
