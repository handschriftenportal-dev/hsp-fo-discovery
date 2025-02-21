package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import lombok.Getter;

@Getter
public enum QueryParser {

  EDISMAX("edismax"),
  COMPLEX_PHASE("complexphrase");

  private final String value;

  QueryParser(final String value) {
    this.value = value;
  }
}
