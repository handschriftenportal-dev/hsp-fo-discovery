package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import java.util.HashMap;
import java.util.Map;

public class MapHelper {

  private MapHelper() {}

  /**
   * Merges multiple maps of type Map<String, Long>, summing up values for common keys.
   * @param maps The maps to be merged.
   * @return A merged map containing the summed up values.
   */
  @SafeVarargs
  public static Map<String, Long> mergeMaps(Map<String, Long>... maps) {
    Map<String, Long> mergedMap = new HashMap<>();

    for (Map<String, Long> map : maps) {
      for (Map.Entry<String, Long> entry : map.entrySet()) {
        String key = entry.getKey();
        Long value = entry.getValue();
        mergedMap.put(key, mergedMap.getOrDefault(key, 0L) + value);
      }
    }

    return mergedMap;
  }
}
