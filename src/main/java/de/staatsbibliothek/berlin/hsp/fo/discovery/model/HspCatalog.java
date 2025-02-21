package de.staatsbibliothek.berlin.hsp.fo.discovery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HspCatalog extends HspBaseEntity {
  @JsonProperty("author-display")
  public String[] author;

  @JsonProperty("editor-display")
  public String[] editor;

  @JsonProperty("manifest-uri-display")
  public String manifestUri;

  @JsonProperty("publisher-display")
  public String[] publisher;

  @JsonProperty("publish-year-display")
  public int publishYear;

  @JsonProperty("thumbnail-uri-display")
  public String thumbnailUri;

  @JsonProperty("title-display")
  public String title;

  public HspCatalog(final String id, final String type) {
    super(id, type);
  }
}