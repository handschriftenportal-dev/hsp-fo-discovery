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

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 */
public enum HspType {
  HSP_AUTHORITY_FILE("hsp:authority-file"),
  HSP_CATALOG("hsp:catalog"),
  HSP_DESCRIPTION("hsp:description"),
  HSP_DESCRIPTION_RETRO("hsp:description_retro"),
  HSP_DIGITIZED("hsp:digitized"),
  HSP_OBJECT("hsp:object");

  private static final Map<String, HspType> lookup = new HashMap<>();

  static {
    Arrays.stream(HspType.values())
        .forEach(type -> lookup.put(type.getValue(), type));
  }

  @Getter
  private final String value;

  HspType(final String value) {
    this.value = value;
  }

  public static HspType getByValue(final String fieldName) {
    return lookup.get(fieldName);
  }
}