package de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SearchFieldFilter {

  private final FieldProvider fieldProvider;
  @Autowired
  public SearchFieldFilter(final FieldProvider fieldProvider) {
    this.fieldProvider = fieldProvider;
  }

  public List<String> filter(final List<String> searchFields) {
  final List<String> fields = new ArrayList<>();

    for(String sf : searchFields) {
      if(fieldProvider.groupExists(sf)) {
        fields.addAll(fieldProvider.getFieldNamesForGroup(sf));
      } else if(fieldProvider.isValid(sf)) {
        fields.add(sf);
      } else {
        log.warn("the provided name {} did not match a known field name or group name and will be ignored", sf);
      }
    }
    return fields.stream()
        .distinct()
        .collect(Collectors.toList());
  }
}
