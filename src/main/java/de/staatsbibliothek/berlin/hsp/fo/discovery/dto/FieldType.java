package de.staatsbibliothek.berlin.hsp.fo.discovery.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Representing a field's type
 */

@Data
public class FieldType {
  public enum Type {
    BOOLEAN,
    DATE,
    ENUM,
    FLOAT,
    INTEGER,
    LONG,
    STRING,
    TEXT,
    UNKNOWN,
    YEAR
  }

  private Type type;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String[] values;
}