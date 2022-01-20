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
package de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.service.fields.SearchField;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Abstract parameter class for querying {@link IHspObjectGroupService}
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractParams<T extends AbstractParams<T>>  {

  protected boolean collapse = false;
  private List<String> facets;
  private Map<String, String> filterQueries;
  protected boolean grouping = false;
  private boolean highlight;
  private String[] highlightFields;
  private String highlightQuery;
  private QueryParser highlightQueryParser;
  private int highlightSnippetCount;
  @Getter(AccessLevel.NONE)
  protected String queryOrSearchTerm;
  @Getter(AccessLevel.NONE)
  private QueryParser queryParser;
  private long rows;
  @Getter(AccessLevel.NONE)
  protected String[] searchFields;
  private String sortPhrase;
  private long start;
  private List<String> stats;

  public String[] getFields() {
    return this.searchFields == null || ArrayUtils.isEmpty(this.searchFields) ? SearchField.getNames() : this.searchFields;
  }

  public abstract String getQuery();

  public abstract QueryParser getQueryParser();
  
  public abstract String[] getSearchFields();
  
  public static abstract class AbstractBuilder<T> {

    private boolean collapse;
    private List<String> facets;
    private Map<String, String> filterQueries;
    private boolean grouping;
    private boolean highlight;
    private String[] highlightFields;
    private QueryParser highlightQueryParser;
    private String highlightQuery;
    private int highlightSnippetCount;
    private String queryOrSearchTerm;
    private long rows;
    protected String[] searchFields;
    private String sortPhrase;
    private long start;
    private List<String> stats;

    public AbstractBuilder<T> withCollapse(final boolean collapse) {
      this.collapse = collapse;
      return this;
    }

    public AbstractBuilder<T> withFacets(final List<String> facets) {
      this.facets = facets;
      return this;
    }

    public AbstractBuilder<T> withFilterQueries(Map<String, String> filterQueries) {
      this.filterQueries = filterQueries;
      return this;
    }

    public AbstractBuilder<T> withGrouping(final boolean grouping) {
      this.grouping = grouping;
      return this;
    }

    public AbstractBuilder<T> withHighlight(final boolean highlight) {
      this.highlight = highlight;
      return this;
    }

    public AbstractBuilder<T> withHighlightFields(final String... highlightFields) {
      this.highlightFields = highlightFields;
      return this;
    }

    public AbstractBuilder<T> withHighlightQuery(final String highlightQuery) {
      this.highlightQuery = highlightQuery;
      return this;
    }

    public AbstractBuilder<T> withHighlightQueryParser(final QueryParser highlightQueryParser) {
      this.highlightQueryParser = highlightQueryParser;
      return this;
    }
    
    public AbstractBuilder<T> withHightlightSnippetCount(final int highlightSnippetCount) {
      this.highlightSnippetCount = highlightSnippetCount;
      return this;
    }

    public AbstractBuilder<T> withQueryOrSearchTerm(final String queryOrSearchTerm) {
      this.queryOrSearchTerm = queryOrSearchTerm;
      return this;
    }

    public AbstractBuilder<T> withRows(final long rows) {
      this.rows = rows;
      return this;
    }

    public AbstractBuilder<T> withSearchFields(final String... searchFields) {
      this.searchFields = searchFields;
      return this;
    }

    public AbstractBuilder<T> withSortPhrase(final String sortPhrase) {
      this.sortPhrase = sortPhrase;
      return this;
    }

    public AbstractBuilder<T> withStart(final long start) {
      this.start = start;
      return this;
    }

    public AbstractBuilder<T> withStats(final List<String> stats) {
      this.stats = stats;
      return this;
    }

    public abstract T build();
  }

  protected AbstractParams(final AbstractBuilder<T> builder) {
    this.collapse               = builder.collapse;
    this.facets                 = builder.facets;
    this.filterQueries          = builder.filterQueries;
    this.grouping               = builder.grouping;
    this.highlight              = builder.highlight;
    this.highlightFields        = builder.highlightFields;
    this.highlightQuery         = builder.highlightQuery;
    this.highlightQueryParser   = builder.highlightQueryParser;
    this.highlightSnippetCount  = builder.highlightSnippetCount;
    this.queryOrSearchTerm      = builder.queryOrSearchTerm;
    this.rows                   = builder.rows;
    this.searchFields           = builder.searchFields;
    this.sortPhrase             = builder.sortPhrase;
    this.start                  = builder.start;
    this.stats                  = builder.stats; 
  }
}
