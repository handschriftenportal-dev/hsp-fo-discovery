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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HspDigitized extends HspBaseEntity {

  @JsonProperty("digitization-date-display")
  private Date digitizationDate;

  @JsonProperty("digitization-institution-display")
  private String digitizationPlace;

  @JsonProperty("digitization-settlement-display")
  private String digitizationOrganization;

  @JsonProperty("external-uri-display")
  private String externalUriDisplay;

  @JsonProperty("issuing-date-display")
  private Date issuingDate;

  @JsonProperty("kod-id-display")
  private String kodIdDisplay;

  @JsonProperty("manifest-uri-display")
  private String manifestURI;

  @JsonProperty("subtype-display")
  private String subtype;

  @JsonProperty("thumbnail-uri-display")
  private String thumbnailURI;
}
