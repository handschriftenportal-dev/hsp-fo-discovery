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

package de.staatsbibliothek.berlin.hsp.fo.discovery.testutil;

import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.discovery.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.MetaData;

import java.util.List;
import java.util.Map;

public class TestDataProvider {

  public TestDataProvider() {
  }

  public static HspObjectGroup getTestData() {
    final HspObjectGroup hspObjectGroup;
    final MetaData metaData;

    hspObjectGroup = new HspObjectGroup();
    final HspObject hspObject = new HspObject();
    hspObject.setId("hspObjectId123");
    hspObject.setGroupId("hspObjectId123");
    hspObject.setRepository("Wolfenbüttel");
    hspObject.setSettlement("Wolfenbüttel");
    hspObject.setIdno("hspObjectIdno123");
    hspObject.setType("hsp:object");
    hspObjectGroup.setHspObject(hspObject);

    final HspDescription hspDescription = new HspDescription();
    hspDescription.setId("hspDescription123");
    hspDescription.setGroupId(hspObject.getGroupId());
    hspDescription.setRepository(hspObject.getRepository());
    hspDescription.setSettlement(hspObject.getSettlement());
    hspDescription.setIdno("hspDescriptionIdno123");
    hspDescription.setType("hsp:description");
    hspObjectGroup.setHspDescriptions(List.of(hspDescription));

    HspObjectForTeiDocTests teiDocumentObject = new HspObjectForTeiDocTests();
    teiDocumentObject.setId(hspObject.getId());
    teiDocumentObject.setGroupId(hspObject.getGroupId());
    teiDocumentObject.setRepository(hspObject.getRepository());
    teiDocumentObject.setSettlement(hspObject.getSettlement());
    teiDocumentObject.setIdno(hspObject.getIdno());
    teiDocumentObject.setType(hspObject.getType());
    teiDocumentObject.setTeiDocument("tei-content");

    return hspObjectGroup;
  }

  public static MetaData getMetadata() {
    return MetaData.builder().withHighlighting(Map.of()).withFacets(Map.of()).withRows(10).withStats(Map.of()).build();
  }
}
