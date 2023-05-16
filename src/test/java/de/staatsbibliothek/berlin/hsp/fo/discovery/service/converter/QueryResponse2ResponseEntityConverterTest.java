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
package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.SolrResponse;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
class QueryResponse2ResponseEntityConverterTest {
  private final HspObjectGroup hspObjectGroup;

  public QueryResponse2ResponseEntityConverterTest() {

    this.hspObjectGroup = new HspObjectGroup();
    final HspObject hspObject = new HspObject();
    hspObject.setType(HspType.HSP_OBJECT.getValue());
    hspObjectGroup.setHspObject(hspObject);

    final HspDescription hspDescription = new HspDescription();
    hspDescription.setType(HspType.HSP_DESCRIPTION.getValue());
    hspObjectGroup.setHspDescriptions(List.of(hspDescription));

    final HspDigitized hspDigitized = new HspDigitized();
    hspDigitized.setType(HspType.HSP_DIGITIZED.getValue());
    hspObjectGroup.setHspDigitizeds(List.of(hspDigitized));
  }

  @Test
  void whenExtractingNumFound_thenValueIsCorrect() throws Exception {
    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjects(hspObjectGroup.getHspObject(), hspObjectGroup.getHspObject())
        .build()
        .convert();
    assertThat(QueryResponse2ResponseEntityConverter.extractNumFound(queryResponse), is(2L));
  }

  @Test
  void whenExtractingNumFoundFromNullResponse_thenValueIsNull() {
    assertThat(QueryResponse2ResponseEntityConverter.extractNumFound(null), is(0L));
  }

  @Test
  void whenExtractingNumFoundFromGroupedResponse_thenValueIsCorrect() throws Exception {
    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();
    assertThat(QueryResponse2ResponseEntityConverter.extractNumFound(queryResponse), is(1L));
  }

  @Test
  void whenExtractingNumFoundFromResponse_thenValueIsCorrect() throws Exception {
    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjects(hspObjectGroup.getHspObject())
        .build()
        .convert();
    assertThat(QueryResponse2ResponseEntityConverter.extractNumFound(queryResponse), is(1L));
  }

  @Test
  void whenExtractingSpellCorrectedTerm_thenValueIsCorrect() throws Exception {
    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();
    assertThat(QueryResponse2ResponseEntityConverter.extractNumFound(queryResponse), is(1L));
  }

  @Test
  void testExtractHspDigitized() throws Exception {
    final Calendar cal = Calendar.getInstance();
    cal.set(4, Calendar.SEPTEMBER, 2020);
    HspDigitized digitized = hspObjectGroup.getHspDigitizeds().get(0);
    digitized.setDigitizationDate(cal.getTime());
    digitized.setDigitizationOrganization("UBL");
    digitized.setDigitizationPlace("Leipzig");
    digitized.setIssuingDate(cal.getTime());
    digitized.setSubtype("komplettVomOriginal");
    digitized.setManifestURI("http://manifest.uri");
    digitized.setThumbnailURI("http://thumbnail.uri");

    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();
    final HspObjectGroup hspObjectGroup =
        QueryResponse2ResponseEntityConverter.extractHspObjectGroups(queryResponse).get(0);

    assertThat(hspObjectGroup, notNullValue());
    final List<HspDigitized> HSPDigitalCopy = hspObjectGroup.getHspDigitizeds();
    assertThat(HSPDigitalCopy, notNullValue());
    assertThat(HSPDigitalCopy, hasSize(1));
    assertThat(digitized, is(HSPDigitalCopy.get(0)));
  }

  @Test
  void testExtractHspDescription() throws Exception {
    final HspDescription desc = hspObjectGroup.getHspDescriptions().get(0);
    desc.setGroupId("groupId123");
    desc.setId("id456");
    desc.setType(HspType.HSP_DESCRIPTION.getValue());
    desc.setAuthors(new String[] {"Katrin Sturm"});

    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();

    final HspObjectGroup hspObjectGroup =
        QueryResponse2ResponseEntityConverter.extractHspObjectGroups(queryResponse).get(0);

    assertThat(hspObjectGroup, notNullValue());
    final List<HspDescription> hspDescriptions = hspObjectGroup.getHspDescriptions();
    assertThat(hspDescriptions, notNullValue());
    assertThat(hspDescriptions, hasSize(1));
    assertThat(desc, is(hspDescriptions.get(0)));
  }

  @Test
  void whenHighlightingContainsUnstemmedAndStemmedField_thenUnstemmedHighlightingIsRemoved() throws Exception {
    final NamedList<List<String>> mockedHighlighting = new NamedList<>();
    mockedHighlighting.add("settlement-search", List.of("highlightedTerm"));
    mockedHighlighting.add("settlement-search-stemmed", List.of("highlightedTermStemmed"));

    final QueryResponse mockedResponse = new SolrResponse.Builder()
        .withHighlightingInformation("testID", mockedHighlighting)
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();

    final Map<String, Map<String, List<String>>> actualHighlighting = QueryResponse2ResponseEntityConverter.extractHighlightingFragmentsFromQueryResponse(mockedResponse);

    assertThat(actualHighlighting.get("testID"), hasEntry("settlement-search", List.of("highlightedTermStemmed")));
  }

  @Test
  void whenHighlightingContainsStemmedFieldOnly_thenStemmedFieldIsNotRemovedAndSuffixIsRemoved() throws Exception {
    final NamedList<List<String>> mockedHighlighting = new NamedList<>();
    mockedHighlighting.add("settlement-search-stemmed", List.of("highlightedTermStemmed"));

    final QueryResponse mockedResponse = new SolrResponse.Builder()
        .withHighlightingInformation("testID", mockedHighlighting)
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();

    final Map<String, Map<String, List<String>>> actualHighlighting = QueryResponse2ResponseEntityConverter.extractHighlightingFragmentsFromQueryResponse(mockedResponse);

    assertThat(actualHighlighting.get("testID"), hasEntry("settlement-search", List.of("highlightedTermStemmed")));
  }
}
