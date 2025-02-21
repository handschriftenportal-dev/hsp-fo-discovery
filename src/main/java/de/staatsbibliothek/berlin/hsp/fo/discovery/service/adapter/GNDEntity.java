package de.staatsbibliothek.berlin.hsp.fo.discovery.service.adapter;

import lombok.*;

/**
 * entity class for mapping authority files
 */
@AllArgsConstructor
@Builder(setterPrefix = "with", access = AccessLevel.PUBLIC)
@Data
@NoArgsConstructor
public class GNDEntity {

  private String gndId;

  private String id;

  private Identifier[] identifier;

  private String preferredName;

  private String typeName;

  private Variant[] variantName;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Variant {

    private String name;
    private String languageCode;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString(includeFieldNames = false)
  public static class Identifier {

    private String text;

    private String type;

    private String url;
  }
}
