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

import org.apache.commons.lang3.StringUtils;

import lombok.NoArgsConstructor;

/**
 * Parameter class for querying {@link IHspObjectGroupService} using a query and {@link QueryParser.LUCENE}
 *
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
@NoArgsConstructor
public class QuerySearchParams extends AbstractParams<QuerySearchParams> {

  @Override
  public String getQuery() {
    return StringUtils.isNotBlank(queryOrSearchTerm) ? queryOrSearchTerm : "*:*";
  }
  
  @Override
  public QueryParser getQueryParser() {
    return QueryParser.LUCENE;
  }

  @Override
  public String[] getSearchFields() {
    return searchFields;
  }
  
  public static class Builder extends AbstractBuilder<QuerySearchParams> {
    @Override
    public QuerySearchParams build() {
      return new QuerySearchParams(this);
    }
  }

  private QuerySearchParams(final Builder builder) {
    super(builder);
  }
}
