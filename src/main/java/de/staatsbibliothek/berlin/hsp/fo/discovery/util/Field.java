package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import lombok.Data;

@Data
public class Field {
  private String name;

  public Field(final String name) {
    this.name = name;
  }
}
