package de.staatsbibliothek.berlin.hsp.fo.discovery.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldTypeInformation;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.util.NamedList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseBuilder {

  private static final ObjectMapper objectMapper =  new ObjectMapper();

  /**
   * Generates a {@code FieldsResponse} based on the given {@code List} of {@code FieldInformation}
   * @param fieldInformationList the {@code List} of {@code FieldInformation}
   * @return the generated {@code FieldsResponse}
   */
  public static SchemaResponse.FieldsResponse getFieldsResponse(List<FieldInformation> fieldInformationList) {
    SchemaResponse.FieldsResponse response = new SchemaResponse.FieldsResponse();
    final List<NamedList<Object>> fields = fieldInformationList.stream()
        .map(ResponseBuilder::toMap)
        .collect(Collectors.toList());
    NamedList<Object> nl = new NamedList<>();
    nl.add("fields", fields);
    response.setResponse(nl);

    return response;
  }

  /**
   * Generates a {@code FieldsTypeResponse} based on the given {@code List} of {@code FieldInformation}
   * @param fieldTypeInformationList the {@code List} of {@code FieldTypeInformation}
   * @return the generated {@code FieldsResponse}
   */
  public static SchemaResponse.FieldTypesResponse getFieldsTypeResponse(List<FieldTypeInformation> fieldTypeInformationList) {
    SchemaResponse.FieldTypesResponse response = new SchemaResponse.FieldTypesResponse();
    final List<NamedList<Object>> fields = fieldTypeInformationList.stream()
        .map(ResponseBuilder::toMap)
        .collect(Collectors.toList());
    NamedList<Object> nl = new NamedList<>();
    nl.add("fieldTypes", fields);
    response.setResponse(nl);

    return response;
  }

  /**
   * Generates a {@code SolrResponseBase} based on the given {@code responseBody}
   * @param responseBody the response's the body
   * @return the generated {@code SolrResponseBase}
   */
  public static SolrResponseBase getFileResponse(final String responseBody) {
    SolrResponseBase response = new SolrResponseBase();
    response.setResponse(new NamedList<>(Map.of("response", responseBody)));
    return response;
  }

  /**
   * Converts an object to {@code NamedList} representation
   * @param object the object to match
   * @return the resulting {@code NamedList} representation
   */
  protected static NamedList<Object> toMap(final Object object) {
    Map<String, Object> resultMap = objectMapper
        .convertValue(object, new TypeReference<>() {});
    return toNamedList(resultMap);
  }

  /**
   * Converts a {@code Map} to a {NamedList}
   * @param map the {@code Map} to be converted into a {@code NamedList}
   * @return the resulting {@code NamedList}
   */
  protected static NamedList<Object> toNamedList(Map<String, Object> map) {
    return new NamedList<>(map);
  }
}
