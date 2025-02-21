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
package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.SolrResponse;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class QueryResponse2ResponseEntityConverterTest {
  private final HspObjectGroup hspObjectGroup;
  private final HighlightConfig highlightConfig;

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

    highlightConfig = new HighlightConfig(250, 100, 3, "em");
  }

  @Nested
  @DisplayName("Misc")
  class Misc {

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
  }

  @Nested
  @DisplayName("Entities")
  class Entities {
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
      desc.setAuthors(new String[]{"Katrin Sturm"});

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
  }

  @Nested
  @DisplayName("Highlight")
  class Highlight {
    @Test
    void whenHighlightingContainsContiguousFragments_thenFragmentsAreMerged() throws Exception {
      final NamedList<List<String>> mockedHighlighting = new NamedList<>();
      mockedHighlighting.add("settlement-search", List.of("<em>foo</em> <em>bar</em> baz"));

      final QueryResponse mockedResponse = new SolrResponse.Builder()
          .withHighlightingInformation("testID", mockedHighlighting)
          .withHspObjectGroups(hspObjectGroup)
          .build()
          .convert();

      final Map<String, Map<String, List<String>>> actualHighlighting = QueryResponse2ResponseEntityConverter.extractHighlightingFromQueryResponse(mockedResponse, highlightConfig);

      assertThat(actualHighlighting.get("testID"), hasEntry("settlement-search", List.of("<em>foo bar</em> baz")));
    }

    @Test
    void whenHighlightingContainsStemmedFieldOnly_thenStemmedFieldIsNotRemovedAndSuffixIsRemoved() throws Exception {
      final NamedList<List<String>> mockedHighlighting = new NamedList<>();
      mockedHighlighting.add("settlement-search-stemmed", List.of("<em>highlightedTermStemmed</em>"));

      final QueryResponse mockedResponse = new SolrResponse.Builder()
          .withHighlightingInformation("testID", mockedHighlighting)
          .withHspObjectGroups(hspObjectGroup)
          .build()
          .convert();

      final Map<String, Map<String, List<String>>> actualHighlighting = QueryResponse2ResponseEntityConverter.extractHighlightingFromQueryResponse(mockedResponse, highlightConfig);

      assertThat(actualHighlighting.get("testID"), hasEntry("settlement-search", List.of("<em>highlightedTermStemmed</em>")));
    }

    @Test
    void givenHighlightingWithExactFieldAndExactNoPunctuationField_whenExtractingHighlighting_thenFieldsAreMerged() throws Exception {
      final NamedList<List<String>> mockedHighlighting = new NamedList<>();
      mockedHighlighting.add("settlement-search-exact", List.of("<em>highlightedTermExact</em>", "<em>one</em> two"));
      mockedHighlighting.add("settlement-search-exact-no-punctuation", List.of("<em>highlightedTermExactNoPunctuation</em>", "one <em>two</em>"));

      final QueryResponse mockedResponse = new SolrResponse.Builder()
          .withHighlightingInformation("testID", mockedHighlighting)
          .withHspObjectGroups(hspObjectGroup)
          .build()
          .convert();

      final Map<String, Map<String, List<String>>> actualHighlighting = QueryResponse2ResponseEntityConverter.extractHighlightingFromQueryResponse(mockedResponse, highlightConfig);

      assertThat(actualHighlighting.get("testID"), hasEntry("settlement-search", List.of("<em>highlightedTermExactNoPunctuation</em>", "<em>one two</em>", "<em>highlightedTermExact</em>")));
    }

    @Test
    void whenHighlightingContainsUnstemmedAndStemmedField_thenStemmedHighlightingIsRemoved() throws Exception {
      final NamedList<List<String>> mockedHighlighting = new NamedList<>();
      mockedHighlighting.add("settlement-search", List.of("<em>term</em> stemmed term"));
      mockedHighlighting.add("settlement-search-stemmed", List.of("term <em>stemmed term</em>"));

      final QueryResponse mockedResponse = new SolrResponse.Builder()
          .withHighlightingInformation("testID", mockedHighlighting)
          .withHspObjectGroups(hspObjectGroup)
          .build()
          .convert();

      final Map<String, Map<String, List<String>>> actualHighlighting = QueryResponse2ResponseEntityConverter.extractHighlightingFromQueryResponse(mockedResponse, highlightConfig);

      assertThat(actualHighlighting.get("testID"), hasEntry("settlement-search", List.of("<em>term stemmed term</em>")));
    }

    @Test
    void givenHighlightsOnDifferentFieldShapes_whenExtractingHighlights_thenFieldsAreMerged() throws JsonProcessingException {
      final NamedList<List<String>> mockedHighlighting = new NamedList<>();
      mockedHighlighting.add("settlement-search",         List.of("<em>Lorem ipsum</em> dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore"));
      mockedHighlighting.add("settlement-search-stemmed", List.of("<em>Lorem ipsum dolor</em> sit amet, <em>consetetur sadipscing elitr</em>, sed diam nonumy eirmod tempor invidunt ut labore et dolore"));
      mockedHighlighting.add("settlement-search-exact", List.of("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore"));
      mockedHighlighting.add("settlement-search-exact-no-punctuation", List.of("Lorem <em>ipsum</em> dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut <em>labore</em> et dolore"));

      final QueryResponse mockedResponse = new SolrResponse.Builder()
          .withHighlightingInformation("testID", mockedHighlighting)
          .withHspObjectGroups(hspObjectGroup)
          .build()
          .convert();

      final Map<String, Map<String, List<String>>> actualHighlighting = QueryResponse2ResponseEntityConverter.extractHighlightingFromQueryResponse(mockedResponse, new HighlightConfig(0, 100, 0, "em"));

      assertThat(actualHighlighting.get("testID"), hasEntry("settlement-search", List.of("<em>Lorem ipsum dolor</em> sit amet, <em>consetetur sadipscing elitr</em>, sed diam nonumy eirmod tempor invidunt ut <em>labore</em> et dolore")));
      assertThat(actualHighlighting.get("testID"), not(hasKey("settlement-search-stemmed")));
      assertThat(actualHighlighting.get("testID"), not(hasKey("settlement-search-exact")));
      assertThat(actualHighlighting.get("testID"), not(hasKey("settlement-search-exact-no-punctuation")));
    }
  }


  @Nested
  @DisplayName("Facets")
  class Facets {
    @Test
    void givenResponseWithDifferentFacetCounts_whenExtractingFacets_thenFacetsAreOrderedDescendingAccordingToItsCount() throws Exception {
      final NamedList<Number> facetItems = new NamedList(new Map.Entry[] {
          new AbstractMap.SimpleEntry<>("xyz", 1),
          new AbstractMap.SimpleEntry("abc", 0),
          new AbstractMap.SimpleEntry("def", 9),
      });
      final NamedList<NamedList<Number>> facets = new NamedList<>();
      facets.add("field", facetItems);
      final QueryResponse response = new SolrResponse.Builder()
          .withFacets(facets)
          .build()
          .convert();

      Map<String, Map<String, Long>> result = QueryResponse2ResponseEntityConverter.extractFacetsFromQueryResponse(response);

      assertThat(result.get("field"), aMapWithSize(3));
      assertThat(result.get("field").keySet().toArray()[0], is("def"));
      assertThat(result.get("field").keySet().toArray()[1], is("xyz"));
      assertThat(result.get("field").keySet().toArray()[2], is("abc"));
    }

    @Test
    void givenResponseWithTrailingNullNameNonZeroCountFacet_whenExtractingFacets_then__Missing__IsAddedAtTheBeginning() throws Exception {
      final NamedList<Number> facetItems = new NamedList<Number>(new Map.Entry[] {
          new AbstractMap.SimpleEntry(null, 1),
          new AbstractMap.SimpleEntry("test", 2)
      });
      final NamedList<NamedList<Number>> facets = new NamedList<>();
      facets.add("field", facetItems);
      final QueryResponse response = new SolrResponse.Builder()
          .withFacets(facets)
          .build()
          .convert();

      Map<String, Map<String, Long>> result = QueryResponse2ResponseEntityConverter.extractFacetsFromQueryResponse(response);

      assertThat(result, aMapWithSize(1));
      assertThat(result, hasKey("field"));
      assertThat(result.get("field"), aMapWithSize(2));
      assertThat(result.get("field").keySet().toArray()[0], is("__MISSING__"));
      assertThat(result.get("field").get("__MISSING__"), is(1L));
      assertThat(result.get("field").keySet().toArray()[1], is("test"));
      assertThat(result.get("field").get("test"), is(2L));
    }

    @Test
    void givenResponseWithNullNameZeroCountFacet_whenExtracting_then__Missing__ZeroCountFacetIsRemoved() throws Exception {
      final NamedList<Number> facetItems = new NamedList(new Map.Entry[] { new AbstractMap.SimpleEntry<>(null, 0), new AbstractMap.SimpleEntry("test", 1)});
      final NamedList<NamedList<Number>> facets = new NamedList<>();
      facets.add("field", facetItems);
      final QueryResponse response = new SolrResponse.Builder()
          .withFacets(facets)
          .build()
          .convert();

      Map<String, Map<String, Long>> result = QueryResponse2ResponseEntityConverter.extractFacetsFromQueryResponse(response);

      assertThat(result, aMapWithSize(1));
      assertThat(result, hasKey("field"));
      assertThat(result.get("field"), aMapWithSize(1));
      assertThat(result.get("field"), hasEntry("test", 1L));
    }
  }
}
