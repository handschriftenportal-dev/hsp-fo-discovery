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
package de.staatsbibliothek.berlin.hsp.fo.discovery.ui.response;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Data
@NoArgsConstructor
public class MetaData implements Serializable {

  /**
   * UID for serializing
   */
  private static final long serialVersionUID = 1L;

  @JsonInclude(Include.NON_EMPTY)
  protected Map<String, Map<String, List<String>>> highlighting;
  
  private long numDocsFound;

  private long numGroupsFound;

  private long start;

  private long rows;

  @JsonInclude(Include.NON_EMPTY)
  private Map<String, Map<String, Long>> facets;

  @JsonInclude(Include.NON_EMPTY)
  private Map<String, Stats> stats;
  
  public static class Builder {
    protected Map<String, Map<String, List<String>>> highlighting;
    private long numGroupsFound;
    private long start;
    private long rows;
    private Map<String, Map<String, Long>> facets;
    private Map<String, Stats> stats;
    
    public Builder() {}
    
    public Builder withHighlighting(final Map<String, Map<String, List<String>>> highlighting) {
      this.highlighting = highlighting;
      return this;
    }
    
    public Builder withNumGroupsFound(final long numGroupsFound) {
      this.numGroupsFound = numGroupsFound;
      return this;
    }
    
    public Builder withStart(final long start) {
      this.start = start;
      return this;
    }
    
    public Builder withRows(final long rows) {
      this.rows = rows;
      return this;
    }
    
    public Builder withFacets(final Map<String, Map<String, Long>> facets) {
      this.facets = facets;
      return this;
    }
    
    public Builder withStats(final Map<String, Stats> stats) {
      this.stats = stats;
      return this;
    }
    
    public MetaData build() {
      MetaData ret = new MetaData();
      ret.setFacets(facets);
      ret.setHighlighting(highlighting);
      ret.setNumGroupsFound(numGroupsFound);
      ret.setRows(rows);
      ret.setStart(start);
      ret.setStats(stats);
      
      return ret;
    }
  }
}