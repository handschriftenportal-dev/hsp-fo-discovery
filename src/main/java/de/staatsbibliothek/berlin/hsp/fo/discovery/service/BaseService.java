/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.staatsbibliothek.berlin.hsp.fo.discovery.service;

import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.SearchField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import lombok.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Map;

public interface BaseService {
  MetaData getMetaData(SearchParams searchParams);
  Map<String, String> getTypeFilter();

  /**
   * Encapsulates parameters
   *
   * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
   */
  @AllArgsConstructor
  @Data
  @Builder(setterPrefix = "with")
  @NoArgsConstructor
  class SearchParams {
    //ToDo default values can be moved to solr config
    private boolean collapse;
    private DisplayField[] displayFields;
    private List<String> facets;
    @Builder.Default
    private List<String> facetTermsExcluded = List.of(HspType.HSP_DIGITIZED.getValue());
    @Builder.Default
    @Getter(AccessLevel.NONE)
    private boolean includeMissingFacet = true;
    private Map<String, String> filterQueries;
    private boolean grouping;
    private boolean highlight;
    private SearchField[] highlightFields;
    private String highlightPhrase;
    private String highlightQuery;
    private int highlightSnippetCount;
    private String phrase;
    private String query;
    @Builder.Default
    private final QueryOperator queryOperator = QueryOperator.AND;
    private long rows;
    @Getter(AccessLevel.NONE)
    private SearchField[] searchFields;
    private String sortPhrase;
    private boolean spellcheck;
    private long start;
    private List<String> stats;
    public static final String QUERY_PARSER = "edismax";

    public SearchField[] getSearchFields() {
      if (ArrayUtils.isNotEmpty(this.searchFields)) {
        return this.searchFields;
      } else {
        return SearchField.values();
      }
    }

    public enum QueryOperator {
      AND,
      OR
    }

    public boolean includeMissingFacet() {
      return includeMissingFacet;
    }
  }
}
