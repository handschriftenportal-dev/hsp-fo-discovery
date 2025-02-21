package de.staatsbibliothek.berlin.hsp.fo.discovery.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representing a field including its type
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Field {
  private String name;
  private boolean required;
  private FieldType.Type type;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String[] values;
}
