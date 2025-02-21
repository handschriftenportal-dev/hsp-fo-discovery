package de.staatsbibliothek.berlin.hsp.fo.discovery.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "hsp.groups")
@Data
@NoArgsConstructor
public class FieldGroupConfig {
  private Map<String, List<String>> fieldGroups;
}
