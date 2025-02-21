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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public enum FacetField {
  AUTHOR("author-facet"),
  DESCRIBED_OBJECT("described-object-facet"),
  DESCRIPTION_STATUS("desc-status-facet"),
  DIGITIZED_IIIF_OBJECT("digitized-iiif-object-facet"),
  DIGITIZED_OBJECT("digitized-object-facet"),
  FORMAT("format-facet"),
  GROUP_ID("group-id-facet"),
  HAS_NOTATION("has-notation-facet"),
  ILLUMINATED("illuminated-facet"),
  LANGUAGE("language-facet"),
  MATERIAL("material-facet"),
  OBJECT_TYPE("object-type-facet"),
  ORIG_DATE_TYPE("orig-date-type-facet"),
  ORIG_PLACE("orig-place-facet"),
  PUBLISHER("publisher-facet"),
  REPOSITORY("repository-facet"),
  REPOSITORY_ID("repository-id-facet"),
  SETTLEMENT("settlement-facet"),
  STATUS("status-facet"),
  TITLE("title-facet"),
  TYPE("type-facet"),
  // catalog facets
  CATALOG_AUTHOR("catalog-author-facet"),
  CATALOG_PUBLISH_YEAR("catalog-publish-year-facet"),
  CATALOG_PUBLISHER_FACET("catalog-publisher-facet"),
  CATALOG_REPOSITORY_FACET("catalog-repository-facet");

  private final String name;

  private static final Map<String, FacetField> lookup = new HashMap<>();

  static {
    Arrays.stream(FacetField.values()).forEach(df -> lookup.put(df.getName(), df));
  }

  FacetField(final String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static List<String> getNames() {
    return new ArrayList<>(lookup.keySet());
  }
}
