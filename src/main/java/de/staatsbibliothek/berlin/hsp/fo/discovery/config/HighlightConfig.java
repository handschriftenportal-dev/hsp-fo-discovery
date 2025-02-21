package de.staatsbibliothek.berlin.hsp.fo.discovery.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "hsp.highlight")
@Data
@NoArgsConstructor
public class HighlightConfig {
  /* used for Apache Solr's default fragment mechanism and determines the overall fragment size */
  private int fragSize;
  /* used for custom fragment mechanism and determines the character count used to pad each highlighted term (from the left and the right) */
  private int padding;
  /* used for Apache Solr's default fragment mechanism and determines the number of fragments for each field */
  private int snippetCount;
  /* the name of the tag that is used for wrapping a highlighted term, e.g. em */
  private String tagName;
}
