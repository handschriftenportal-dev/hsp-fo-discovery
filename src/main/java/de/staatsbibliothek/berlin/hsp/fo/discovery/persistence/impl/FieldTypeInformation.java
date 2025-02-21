package de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the information for a Solr field type.
 * @see <a href="https://solr.apache.org/guide/solr/latest/indexing-guide/field-type-definitions-and-properties.html">Field Type Definitions and Properties</a>
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class FieldTypeInformation {
  @JsonProperty("class")
  private String clazz;
  private String enumName;
  private String name;
}
