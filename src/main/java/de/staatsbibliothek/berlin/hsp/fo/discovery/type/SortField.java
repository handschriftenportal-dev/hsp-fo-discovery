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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public enum SortField {

  MS_IDENTIFIER_ASC("ms-identifier-sort", "asc"),
  MS_IDENTIFIER_DESC("ms-identifier-sort", "desc"),
  ORIG_DATE_ASC("orig-date-from-sort", "asc"),
  ORIG_DATE_DESC("orig-date-to-sort", "desc"),
  PUBLISH_YEAR_ASC("publish-year-sort", "asc"),
  PUBLISH_YEAR_DESC("publish-year-sort", "desc"),
  SCORE_DESC("score", "desc");

  private static final Map<String, SortField> lookup = new HashMap<>();

  static {
    Arrays.stream(SortField.values()).forEach(sf -> lookup.put(sf.getSortPhrase(), sf));
  }

  private final String name;
  private final String direction;

  SortField(final String name, final String direction) {
    this.name = name;
    this.direction = direction;
  }

  public String getName() {
    return this.name;
  }

  public String getDirection() {
    return this.direction;
  }

  public String getSortPhrase() {
    return this.getName() + " " + this.getDirection();
  }

  public static boolean isValid(String sortPhrase) {
    return StringUtils.isNotEmpty(sortPhrase) && lookup.containsKey(sortPhrase);
  }
}
