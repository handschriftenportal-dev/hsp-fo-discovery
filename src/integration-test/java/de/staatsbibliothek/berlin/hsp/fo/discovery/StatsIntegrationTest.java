package de.staatsbibliothek.berlin.hsp.fo.discovery;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.staatsbibliothek.berlin.hsp.fo.discovery.util.matcher.KeyMatcher.allKeys;

public class StatsIntegrationTest extends AbstractBaseIntegrationTest {
  protected static final Set<String> STATS_KEYS = Set.of("min", "max", "count", "missing");

  @Test
  void whenSearching_thenHeightStatsAreProvided() {
    search().jsonPath("$.metadata.stats['height-facet']").value(allKeys(STATS_KEYS));
  }

  @Test
  void whenSearching_thenLeavesCountStatsAreProvided() {
    search().jsonPath("$.metadata.stats['leaves-count-facet']").value(allKeys(STATS_KEYS));
  }

  @Test
  void whenSearching_thenOrigDateFromStatsAreProvided() {
    search().jsonPath("$.metadata.stats['orig-date-from-facet']").value(allKeys(STATS_KEYS));
  }

  @Test
  void whenSearching_thenOrigDateToStatsAreProvided() {
    search().jsonPath("$.metadata.stats['orig-date-to-facet']").value(allKeys(STATS_KEYS));
  }

  @Test
  void whenSearching_thenOrigDateWhenStatsAreProvided() {
    search().jsonPath("$.metadata.stats['orig-date-when-facet']").value(allKeys(STATS_KEYS));
  }

  @Test
  void whenSearching_thenWidthStatsAreProvided() {
    search().jsonPath("$.metadata.stats['width-facet']").value(allKeys(STATS_KEYS));
  }
}