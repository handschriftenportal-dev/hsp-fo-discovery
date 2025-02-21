package de.staatsbibliothek.berlin.hsp.fo.discovery.api;

import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.DisplayFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.api.filter.SearchFieldFilter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HighlightConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.DisplayField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.util.FieldProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Validated
public abstract class BaseEntityController<T> extends BaseController<T> {

  protected SearchFieldFilter searchFieldFilter;
  protected FieldProvider fieldProvider;

  protected BaseEntityController(final BaseService<T> baseService, final HspConfig hspConfig, final HighlightConfig highlightConfig) {
    super(baseService, hspConfig, highlightConfig);
  }

  @Autowired
  public void setSearchFieldFilter(final SearchFieldFilter searchFieldFilter) {
    this.searchFieldFilter = searchFieldFilter;
  }

  @Autowired
  public void setFieldProvider(final FieldProvider fieldProvider) {
    this.fieldProvider = fieldProvider;
  }

  protected Result<List<T>> byId(@NotBlank final String id, final List<String> fields) {
    final DisplayField[] filteredFields = DisplayFieldFilter.filterAndAddDisplaySuffix(fields);
    return byId(id, filteredFields);
  }

  protected Result<List<T>> byId(@NotBlank final String id, final DisplayField[] displayFields) {
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withDisplayFields(displayFields)
        .withRows(2)
        .withPhrase(id)
        .withSearchFields(List.of("id-search"))
        .build();

    final Result<List<T>> result = baseService.find(params);
    checkIdResult(result, id);
    return result;
  }

  protected void checkIdResult(final Result<List<T>> result, final String id) {
    if (result.getPayload().isEmpty()) {
      throw ExceptionFactory.getException(ExceptionType.NOT_FOUND, String.format("No %s with id %s found.", baseService.getEntityName(), id));
    }
    if (result.getPayload().size() > 1) {
      throw ExceptionFactory.getException(ExceptionType.NO_UNIQUE_RESULT, String.format("Multiple %ss for id %s found.", baseService.getEntityName(), id));
    }
  }

  protected Result<List<T>> all(final DisplayField[] fields, final long start, final long rows) {
    final BaseService.SearchParams params = BaseService.SearchParams.builder()
        .withDisplayFields(fields)
        .withFilterQueries(filterConverter.convert(null, baseService.getTypeFilter()))
        .withRows(rows)
        .withStart(start)
        .build();
    return baseService.find(params);
  }

  protected List<String> getSearchFieldsWithDefaults(final List<String> fieldNames) {
    final List<String> filteredFieldNames = searchFieldFilter.filter(fieldNames);
    if (filteredFieldNames.isEmpty()) {
      return fieldProvider.getFieldNamesForGroupAll();
    }
    return filteredFieldNames;
  }
}
