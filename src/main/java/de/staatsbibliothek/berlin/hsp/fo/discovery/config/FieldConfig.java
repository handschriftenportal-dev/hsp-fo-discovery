package de.staatsbibliothek.berlin.hsp.fo.discovery.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "hsp.fields")
@Data
@NoArgsConstructor
public class FieldConfig {
  private List<String> fields;
}