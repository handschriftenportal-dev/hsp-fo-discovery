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
package de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HspObject extends HspBase {

  @JsonProperty("dimensions-display")
  private String dimensions;

  @JsonProperty("format-display")
  private String format;

  @JsonProperty("has-notation-display")
  private String hasNotation;

  @JsonProperty("idno-display")
  private String idno;

  @JsonProperty("illuminated-display")
  private boolean illuminated;

  @JsonProperty("language-display")
  private String[] language;

  @JsonProperty("leaves-count-display")
  private String leavesCount;

  @JsonProperty("material-display")
  private String material;

  @JsonProperty("object-type-display")
  private String objectType;

  @JsonProperty("orig-date-lang-display")
  private String[] origDate;

  @JsonProperty("orig-place-display")
  private String origPlace;

  @JsonProperty("repository-display")
  private String repository;

  @JsonProperty("settlement-display")
  private String settlement;

  @JsonProperty("status-display")
  private String status;

  @JsonProperty("title-display")
  private String title;
}
