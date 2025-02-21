package de.staatsbibliothek.berlin.hsp.fo.discovery.type;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * Represents the information to an enum field type
 */
@Data
public class EnumInformation {
  @JacksonXmlProperty(isAttribute = true)
  private String name;

  @JacksonXmlElementWrapper(useWrapping = false)
  private String[] value;
}
