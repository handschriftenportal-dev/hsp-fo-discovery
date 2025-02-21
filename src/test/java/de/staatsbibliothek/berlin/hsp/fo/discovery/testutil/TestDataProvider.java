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

    TestHspObject teiDocumentObject = new TestHspObject();
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
