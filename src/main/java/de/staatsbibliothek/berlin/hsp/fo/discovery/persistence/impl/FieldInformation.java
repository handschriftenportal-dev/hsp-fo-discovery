package de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the information for a Solr field.
 * @see <a href="https://solr.apache.org/guide/solr/latest/indexing-guide/fields.html">Fields</a>
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class FieldInformation {
  @JsonAlias("multiValued")
  private boolean isMultiValued;
  private boolean required;
  private String name;
  private String type;
  private String values;
}