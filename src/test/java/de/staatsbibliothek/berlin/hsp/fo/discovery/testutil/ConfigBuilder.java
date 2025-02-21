package de.staatsbibliothek.berlin.hsp.fo.discovery.testutil;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;

import java.util.List;
import java.util.Map;

public class ConfigBuilder {

  public static HspConfig getHSPConfig(final List<String> fields, final Map<String, List<String>> groups) {
    final HspConfig hspConfig = new HspConfig();
    hspConfig.setFields(fields);
    hspConfig.setGroups(groups);

    return hspConfig;
  }

  public static FieldProvider getFieldProvider(final List<String> fields, final Map<String, List<String>> groups) {
    return new FieldProvider(getHSPConfig(fields, groups));
  }
}
