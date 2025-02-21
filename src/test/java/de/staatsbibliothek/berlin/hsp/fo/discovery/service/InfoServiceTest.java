package de.staatsbibliothek.berlin.hsp.fo.discovery.service;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Field;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.FieldType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.DiscoveryRepository;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldTypeInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.InfoServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.testutil.ResponseBuilder;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@ExtendWith(SpringExtension.class)
class InfoServiceTest  {

  @Mock
  DiscoveryRepository discoveryRepository;

  @Mock
  FieldProvider fieldProvider;

  @InjectMocks
  private InfoServiceImpl infoService;

  @Test
  void whenGetFieldsIsCalled_thenOnlyEnabledSearchFieldsAreReturned() {
    List<FieldInformation> fieldInformationList = List.of(
        new FieldInformation(false, false, "test-search", "text_general", null),
        new FieldInformation(false, false, "id-search", "text_general", null)
    );
    final SchemaResponse.FieldsResponse fieldResponse = ResponseBuilder.getFieldsResponse(fieldInformationList);

    List<FieldTypeInformation> fieldTypeInformationList = List.of(
        new FieldTypeInformation("solr.TextField", null, "text_general")
    );
    final SchemaResponse.FieldTypesResponse fieldTypesResponse = ResponseBuilder.getFieldsTypeResponse(fieldTypeInformationList);
    final SolrResponseBase fileResponse = ResponseBuilder.getFileResponse("<?xml version=\"1.0\" ?><enumsConfig></enumsConfig>");

    Mockito.when(this.fieldProvider.getFieldNames()).thenReturn(List.of("id-search"));
    Mockito.when(this.discoveryRepository.getFieldInformation()).thenReturn(fieldResponse);
    Mockito.when(this.discoveryRepository.getFieldTypeInformation()).thenReturn(fieldTypesResponse);
    Mockito.when(this.discoveryRepository.getEnumValues()).thenReturn(fileResponse);

    final List<Field> fields = infoService.getFields();

    assertThat(fields, containsInAnyOrder(new Field("id-search", false, FieldType.Type.TEXT, null)));
  }
}