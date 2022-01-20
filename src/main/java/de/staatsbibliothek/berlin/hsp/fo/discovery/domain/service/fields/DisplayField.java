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
import java.util.Map;

/**
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public enum DisplayField {
  DIMENSIONS("dimensions-display"),
  FORMAT("format-display"),
  GROUPID("group-id-display"),
  HAS_NOTATION("has-notation-display"),
  ID("id-display"),
  IDNO("idno-display"),
  ILLUMINTAED("illuminated-display"),
  LANGUAGE("language-display"),
  LEAVES_COUNT("leaves-count-display"),
  MATERIAL("material-display"),
  OBJECT_TYPE("object-type-display"),
  ORIG_DATE_DATED("orig-date-dated-display"),
  ORIG_DATE_LANG("orig-date-lang-display"),
  ORIG_PLACE("orig-place-display"),
  RENDERING("rendering-display"),
  REPOSITORY("repository-display"),
  SETTLEMENT("settlement-display"),
  STATUS("status-display"),
  TEI_DOCUMENT("tei-document-display"),
  TITLE("title-display"),
  TYPE("type-display"),

  // description fields

  DESC_AUTHORS("desc-author-display"),
  DESC_PUBLISHING_DATE("desc-publish-date-display"),

  // digitized fields
  DIGITIZATION_DATE("digitization-date-display"),
  DIGITIZATION_PLACE("digitization-place-display"),
  DIGITIZATION_ORGANISATION("digitization-organization-display"),
  EXTERNAL_URI("external-uri-display"),
  ISSUIING_DATE("issuing-date-display"),
  MANIFEST_URI("manifest-uri-display"),
  SUBTYPE("subtype-display"),
  THUMBNAIL_URI("thumbnail-uri-display");

  private String name;

  private static Map<String, DisplayField> lookup = new HashMap<>();

  static {
    Arrays.stream(DisplayField.values()).forEach(df -> lookup.put(df.getName(), df));
  }

  private DisplayField(final String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static String[] getNames() {
    return lookup.keySet().toArray(String[]::new);
  }

  public static String getNamesConcated() {
    return String.join(" ", getNames());
  }

  public static DisplayField getByName(final String fieldName) {
    return lookup.get(fieldName);
  }
}
