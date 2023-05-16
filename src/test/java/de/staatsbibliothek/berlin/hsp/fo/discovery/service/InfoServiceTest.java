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

import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class InfoServiceTest {

  @Test
  void whenGetItemCountIsCalledWithMissingMetaData_thenZeroIsReturned() {
    final long actualItemCount = InfoService.getItemCount(null, Collections.emptyList());

    assertThat(actualItemCount, is(0L));
  }

  @Test
  void whenGetItemCountIsCalledWithEmptyTypeFacet_thenZeroIsReturned() {
    final MetaData metaData = MetaData.builder()
        .withFacets(Map.of())
        .build();

    final long actualItemCount = InfoService.getItemCount(metaData, Collections.emptyList());

    assertThat(actualItemCount, is(0L));
  }

  @Test
  void whenGetItemCountIsCalled_thenItemsAreCountCorrectly() {
    final MetaData metaData = MetaData.builder()
        .withFacets(Map.of("type-facet", Map.of("hsp:object", 9L, "hsp:description", 3L, "hsp:foo", 1L)))
        .build();

    final long actualItemCount = InfoService.getItemCount(metaData, List.of(HspType.HSP_OBJECT, HspType.HSP_DESCRIPTION));

    assertThat(actualItemCount, is(12L));
  }
}
