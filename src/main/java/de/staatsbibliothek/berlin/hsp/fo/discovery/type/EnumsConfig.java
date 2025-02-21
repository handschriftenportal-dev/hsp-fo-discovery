package de.staatsbibliothek.berlin.hsp.fo.discovery.type;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Represents the collected information to all enum field types
 */
@Data
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class EnumsConfig {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "enum")
  EnumInformation[] enumInformation;

  /**
   * Find {@link EnumInformation} based on its name
   * @param name the enum's name to look for
   * @return the {@link EnumInformation} that corresponds to the given name, {@code null} otherwise
   */
  public EnumInformation getByName(final String name) {
    if(StringUtils.isEmpty(name)) {
      return null;
    }
    return CollectionUtils.emptyIfNull(List.of(enumInformation))
        .stream()
        .filter(item -> name.equals(item.getName()))
        .findFirst()
        .orElse(null);
  }
}
