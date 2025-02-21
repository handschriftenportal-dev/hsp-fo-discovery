package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter.StringToFilterQueryConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.StatField;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class BaseController<T> {

  protected final BaseService<T> baseService;
  protected final HspConfig hspConfig;
  protected final List<String> defaultFacetFields;
  protected final List<String> catalogFacetFields;
  protected final HighlightConfig highlightConfig;
  protected final StringToFilterQueryConverter filterConverter;
  protected final List<String> statsFields;
  protected final List<String> catalogStatsFields;

  protected BaseController(final BaseService<T> baseService, final HspConfig hspConfig, final HighlightConfig highlightConfig) {
    final List<String> combinedFacetFields = new ArrayList<>();
    this.baseService = baseService;
    this.highlightConfig = highlightConfig;
    this.hspConfig = hspConfig;
    this.defaultFacetFields = ListUtils.intersection(this.hspConfig.getDefaultFacets(), FacetField.getNames()).stream().sorted().collect(Collectors.toList());
    this.catalogFacetFields = ListUtils.intersection(this.hspConfig.getCatalogFacets(), FacetField.getNames()).stream().sorted().collect(Collectors.toList());
    this.statsFields = ListUtils.intersection(this.hspConfig.getStats(), StatField.getNames());
    this.catalogStatsFields = ListUtils.intersection(this.hspConfig.getCatalogStats(), StatField.getNames());

    combinedFacetFields.addAll(defaultFacetFields);
    combinedFacetFields.addAll(catalogFacetFields);
    combinedFacetFields.addAll(statsFields);
    combinedFacetFields.addAll(catalogStatsFields);

    this.filterConverter = new StringToFilterQueryConverter(combinedFacetFields);
  }
}
