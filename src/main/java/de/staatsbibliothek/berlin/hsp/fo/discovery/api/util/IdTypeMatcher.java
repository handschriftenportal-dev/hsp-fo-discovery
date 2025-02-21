package de.staatsbibliothek.berlin.hsp.fo.discovery.api.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class IdTypeMatcher {

  private static final Pattern HSP_PATTERN = Pattern.compile("^HSP-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
  private static final Pattern AUTHORITY_FILE_PATTERN = Pattern.compile("^NORM-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

  /**
   * Determines the type of an id by its pattern
   * @param id the id to match
   * @return the id's type
   */
  public static IdType match(final String id) {
    if (HSP_PATTERN.matcher(id).matches()) {
      return IdType.HSP_OBJECT;
    } else if (AUTHORITY_FILE_PATTERN.matcher(id).matches()) {
      return IdType.AUTHORITY_FILE;
    } else {
      return IdType.UNKNOWN;
    }
  }

  public enum IdType {
    AUTHORITY_FILE,
    HSP_OBJECT,
    UNKNOWN
  }
}
