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

/**
 * Each value of this enum represents a conjunction of one or more search fields
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 */
public enum FieldGroup {

  BASIC("FIELD-GROUP-BASIC", Arrays.asList(
      SearchField.FORMAT,
      SearchField.IDNO,
      SearchField.LANGUAGE,
      SearchField.MATERIAL,
      SearchField.OBJECT_TYPE,
      SearchField.ORIG_PLACE,
      SearchField.REPOSITORY,
      SearchField.SETTLEMENT,
      SearchField.STATUS,
      SearchField.TITLE)),
  FULLTEXT("FIELD-GROUP-FULLTEXT",Arrays.asList(
      SearchField.ACCOMPANYING_MATERIAL,
      SearchField.BINDING,
      SearchField.BOOKLET,
      SearchField.DECORATION,
      SearchField.FRAGMENT,
      SearchField.FULLTEXT,
      SearchField.HISTORY,
      SearchField.ITEM_ICONOGRAPHY,
      SearchField.ITEM_MUSIC,
      SearchField.ITEM_TEXT,
      SearchField.PHYSICAL_DESCRIPTION
      )),
  ORIGIN("FIELD-GROUP-ORIGIN", Arrays.asList(
      SearchField.BINDING_ORIG_PLACE,
      SearchField.ORIG_PLACE
      )),
  PERSON("FIELD-GROUP-PERSON", Arrays.asList(
      SearchField.PERSON
      )),
  SIGNATURE("FIELD-GROUP-SIGNATURE", Arrays.asList(
      SearchField.COLLECTION,
      SearchField.IDNO,
      SearchField.IDNO_ALT,
      SearchField.REPOSITORY,
      SearchField.SETTLEMENT
      ));

  final String name;
  
  final List<SearchField> fields;

  private FieldGroup(final String value, List<SearchField> fields) {
    this.name = value;
    this.fields = fields;
  }
  
  private static Map<String, FieldGroup> lookup = new HashMap<>();
  
  static {
    Arrays.stream(FieldGroup.values()).forEach(fg -> lookup.put(fg.name, fg));
  }
  
  public static FieldGroup getByName(final String name) {
    return lookup.get(name);
  }
  
  public List<SearchField> getFields() {
    return this.fields;
  }
}