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
package de.staatsbibliothek.berlin.hsp.fo.discovery.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class HspObject extends HspBaseEntity {

  public HspObject(final String id, final String type) {
    super(id, type);
  }

  @JsonProperty("dimensions-display")
  private String[] dimensions;

  @JsonProperty("format-display")
  private String[] format;

  @JsonProperty("former-ms-identifier-display")
  private String[] formerMsIdentifier;

  @JsonProperty("has-notation-display")
  private String hasNotation;

  @JsonProperty("idno-display")
  private String idno;

  @JsonProperty("illuminated-display")
  private String illuminated;

  @JsonProperty("language-display")
  private String[] language;

  private Date lastModified;

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

  @JsonProperty("orig-place-authority-file-display")
  private String[] origPlaceAuthorityFile;

  @JsonProperty("persistent-url-display")
  private String purl;

  @JsonProperty("repository-authority-file-display")
  private String[] repositoryAuthorityFile;

  @JsonProperty("repository-display")
  private String repository;

  @JsonProperty("settlement-authority-file-display")
  private String[] settlementAuthorityFile;

  @JsonProperty("settlement-display")
  private String settlement;

  @JsonProperty("status-display")
  private String status;

  @JsonProperty("title-display")
  private String title;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE MMM dd HH:mm:ss zzz YYYY", locale = "en")
  @JsonProperty("last-modified-display")
  public void setLastModified(final Date lastModified) {
    this.lastModified = lastModified;
  }

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-dd")
  @JsonProperty("last-modified")
  public Date getLastModified() {
    return lastModified;
  }
}
