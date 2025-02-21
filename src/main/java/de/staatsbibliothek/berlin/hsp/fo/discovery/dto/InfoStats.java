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

package de.staatsbibliothek.berlin.hsp.fo.discovery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;


/**
 * InfoStats represents information for stats endpoint.
 * Contains information about digitalisate, beschreibungen and kulturobjekte.
 *
 */
@Builder(setterPrefix = "with")
@Data
public class InfoStats {

  @JsonProperty("kulturobjekte")
  private BaseStats kod;

  @JsonProperty("digitalisate")
  private BaseStats digitized;

  @JsonProperty("kataloge")
  private BaseStats catalog;

  @JsonProperty("beschreibungen")
  private DescriptionStats description;

  /**
   * BaseStats represents the stats every HspType exposes.
   */
  @SuperBuilder(setterPrefix = "with")
  @Data
  public static class BaseStats {
    private long all;
    private Map<String, Long> institution;
  }

  /**
   * DescriptionsStats additional stats that Descriptions expose.
   */
  @EqualsAndHashCode(callSuper = true)
  @SuperBuilder(setterPrefix = "with")
  @Data
  public static class DescriptionStats extends BaseStats {
    private long retro;
    private long extern;
    private long intern;
  }
}