package de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Field;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.FieldType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.DiscoveryRepository;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl.FieldTypeInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.InfoService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.EnumInformation;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.EnumsConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InfoServiceImpl implements InfoService {
  protected DiscoveryRepository discoveryRepository;
  protected FieldProvider fieldProvider;
  private static final String FIELD_TYPE_YEAR = "year";

  @Autowired
  public InfoServiceImpl(final DiscoveryRepository discoveryRepository, final FieldProvider fieldProvider) {
    this.discoveryRepository = discoveryRepository;
    this.fieldProvider = fieldProvider;
  }

  /**
   * Get all available search fields, including its type
   * @return a {@link List} containing the {@link Field}s
   */
  @Override
  public List<Field> getFields() {
    List<FieldInformation> fieldInformationList = getFieldInformation();
    fieldInformationList = filterFieldInformation(fieldInformationList);
    final List<FieldTypeInformation> fieldTypeInformationList = getFieldTypeInformation();
    final EnumsConfig enumsConfig = getEnumInformation();

    return mergeFieldsAndFieldTypes(fieldInformationList, fieldTypeInformationList, enumsConfig);
  }

  /**
   * Filters the given {@code fieldInformationList} for search fields
   * @param fieldInformationList the {@link FieldInformation}s to filter
   * @return a {link List} containing the filtered {@link FieldInformation} items
   */
  private List<FieldInformation> filterFieldInformation(final List<FieldInformation> fieldInformationList) {
    return fieldInformationList.stream()
        .filter(fi -> fi.getName().endsWith(FieldProvider.SUFFIX_UNSTEMMED))
        .filter(fti -> fieldProvider.getFieldNames().contains(fti.getName()))
        .toList();
  }

  /**
   * Get all available fields
   * @return a {@link List} containing the {@link FieldInformation}
   */
  private List<FieldInformation> getFieldInformation() {
    final SchemaResponse.FieldsResponse fieldsResponse = discoveryRepository.getFieldInformation();
    return QueryResponse2ResponseEntityConverter.extractFieldInformation(fieldsResponse);
  }

  /**
   * Get all available field types
   * @return a {@link List} containing the {@link FieldTypeInformation}
   */
  private List<FieldTypeInformation> getFieldTypeInformation() {
    final SchemaResponse.FieldTypesResponse fieldTypesResponse = discoveryRepository.getFieldTypeInformation();
    return QueryResponse2ResponseEntityConverter.extractFieldTypeInformation(fieldTypesResponse);
  }

  /**
   * Get the {@link EnumsConfig}, containing all available enum values
   * @return the {@link EnumsConfig}
   */
  private EnumsConfig getEnumInformation() {
    SolrResponseBase fileResponse = discoveryRepository.getEnumValues();
    return QueryResponse2ResponseEntityConverter.extractEnumConfig(fileResponse);
  }

  /**
   * Merges a {@link List} of {@link FieldInformation} items and a {@link List} of {@link FieldTypeInformation} by the field's type name.
   * If the type is an enum, the result is enriched with the permitted values
   * @param fieldInformationList a {@link List} containing the {@link FieldInformation} items to merge
   * @param fieldTypeInformationList a {@link List} containing the {@link FieldTypeInformation} items to merge
   * @param enumsConfig containing the enum information
   * @return a {@link List} containing the resulting {@link Field}s
   */
  private List<Field> mergeFieldsAndFieldTypes(final List<FieldInformation> fieldInformationList, final List<FieldTypeInformation> fieldTypeInformationList, final EnumsConfig enumsConfig) {
    final List<Field> result = new LinkedList<>();
    final Map<String, FieldTypeInformation> fieldTypesMap = fieldTypeInformationList.stream()
        .collect(Collectors.toMap(FieldTypeInformation::getName, f -> f));
    for(FieldInformation fieldInformation : fieldInformationList) {
      final FieldTypeInformation fieldTypeInformation = fieldTypesMap.get(fieldInformation.getType());
      final EnumInformation enumInformation = enumsConfig.getByName(fieldTypeInformation.getEnumName());
      result.add(getFieldFromFieldInformation(fieldInformation, fieldTypeInformation, enumInformation));
    }

    return result;
  }


  /**
   * Merges a {@link FieldInformation} and a {@link FieldTypeInformation}
   * If the type is an enum, the result is enriched with the permitted values
   * @param fieldInformation a {@link List} containing the {@link FieldInformation} items to merge
   * @param fieldTypeInformation a {@link List} containing the {@link FieldTypeInformation} items to merge
   * @param enumInformation containing the enum information, can be {@code null}, if there is none
   * @return the corresponding {@link Field} instance
   */
  private static Field getFieldFromFieldInformation(final FieldInformation fieldInformation, final FieldTypeInformation fieldTypeInformation, final EnumInformation enumInformation) {
    final Field field = new Field();
    field.setName(fieldInformation.getName());
    field.setRequired(fieldInformation.isRequired());
    field.setType(getTypeFromSolrType(fieldTypeInformation.getClazz(), fieldTypeInformation.getName()));
    if(enumInformation != null) {
      field.setValues(enumInformation.getValue());
    }
    return field;
  }

  /**
   * Maps a Solr type's class name to a {@link FieldType.Type}
   * @param solrType the Solr type represented by its class name
   * @return the corresponding {@link FieldType.Type}
   */
  private static FieldType.Type getTypeFromSolrType(final String solrType, final String typeName) {
    return switch (solrType) {
      case "solr.BoolField" -> FieldType.Type.BOOLEAN;
      case "solr.DatePointField" -> FieldType.Type.DATE;
      case "solr.FloatPointField" -> FieldType.Type.FLOAT;
      case "solr.EnumFieldType" -> FieldType.Type.ENUM;
      case "solr.StrField" -> FieldType.Type.STRING;
      case "solr.IntPointField" -> FIELD_TYPE_YEAR.equals(typeName) ? FieldType.Type.YEAR : FieldType.Type.INTEGER;
      case "solr.LongPointField" -> FieldType.Type.LONG;
      case "solr.TextField", "org.apache.solr.schema.TextField" -> FieldType.Type.TEXT;
      default -> FieldType.Type.UNKNOWN;
    };
  }
}
