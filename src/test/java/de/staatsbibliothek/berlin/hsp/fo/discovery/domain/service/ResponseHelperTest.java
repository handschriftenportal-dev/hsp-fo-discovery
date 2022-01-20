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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.Test;

import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.entity.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.domain.valueobject.HspTypes;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.SolrResponse;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
class ResponseHelperTest {
  private HspObjectGroup hspObjectGroup;

  public ResponseHelperTest() {

    this.hspObjectGroup = new HspObjectGroup();
    final HspObject hspObject = new HspObject();
    hspObject.setType(HspTypes.HSP_OBJECT.getValue());
    hspObjectGroup.setHspObject(hspObject);

    final HspDescription hspDescription = new HspDescription();
    hspDescription.setType(HspTypes.HSP_DESCRIPTION.getValue());
    hspObjectGroup.setHspDescriptions(Arrays.asList(hspDescription));

    final HspDigitized hspDigitized = new HspDigitized();
    hspDigitized.setType(HspTypes.HSP_DIGITIZED.getValue());
    hspObjectGroup.setHspDigitizeds(Arrays.asList(hspDigitized));
  }

  @Test
  void testExtractNumDocsFoundFromSolrResponse() throws Exception {
    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();
    assertThat(ResponseHelper.extractNumDocsFoundFromSolrResponse(queryResponse), is(2L));
  }

  @Test
  void testExtractNumGroupsFoundFromSolrResponse() throws Exception {
    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();
    assertThat(ResponseHelper.extractNumGroupsFoundFromSolrResponse(queryResponse), is(1L));
  }

  @Test
  void testExtractHspDigitized() throws Exception {
    HspDigitized digitized = hspObjectGroup.getHspDigitizeds().get(0);
    digitized.setDigitizationDate(new Date(4, 9, 2020));
    digitized.setDigitizationOrganization("UBL");
    digitized.setDigitizationPlace("Leipzig");
    digitized.setIssuingDate(new Date(3, 9, 2020));
    digitized.setSubtype("komplettVomOriginal");
    digitized.setManifestURI("http://manifest.uri");
    digitized.setThumbnailURI("http://thumbnail.uri");

    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();
    final HspObjectGroup hspObjectGroup =
        ResponseHelper.extractHspObjectGroupFromSolrResponse(queryResponse);

    assertThat(hspObjectGroup, notNullValue());
    final List<HspDigitized> hspDigitizeds = hspObjectGroup.getHspDigitizeds();
    assertThat(hspDigitizeds, notNullValue());
    assertThat(hspDigitizeds, hasSize(1));
    assertThat(digitized, is(hspDigitizeds.get(0)));
  }

  @Test
  void testExtractHspDescription() throws Exception {
    final HspDescription desc = hspObjectGroup.getHspDescriptions().get(0);
    desc.setGroupId("groupId123");
    desc.setId("id456");
    desc.setType(HspTypes.HSP_DESCRIPTION.getValue());
    desc.setAuthors(new String[] {"Katrin Sturm"});

    final QueryResponse queryResponse = new SolrResponse.Builder()
        .withHspObjectGroups(hspObjectGroup)
        .build()
        .convert();

    final HspObjectGroup hspObjectGroup =
        ResponseHelper.extractHspObjectGroupFromSolrResponse(queryResponse);

    assertThat(hspObjectGroup, notNullValue());
    final List<HspDescription> hspDescriptions = hspObjectGroup.getHspDescriptions();
    assertThat(hspDescriptions, notNullValue());
    assertThat(hspDescriptions, hasSize(1));
    assertThat(desc, is(hspDescriptions.get(0)));
  }
}
