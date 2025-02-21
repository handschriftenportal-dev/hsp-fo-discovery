package de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DisplayFieldFilter {

  private DisplayFieldFilter() {
  }

  public static DisplayField[] filterAndAddDisplaySuffix(final List<String> searchFields) {
    final Set<DisplayField> fields = new HashSet<>();

    for (String sf : searchFields) {
      CollectionUtils.addIgnoreNull(fields, DisplayField.getByName(sf + "-display"));
    }
    return fields.toArray(DisplayField[]::new);
  }
}
