package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import de.staatsbibliothek.berlin.hsp.fo.discovery.config.HspConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class FieldProvider {
  public static final String SUFFIX_EXACT = "-exact";
  public static final String SUFFIX_EXACT_NO_PUNCTUATION = "-exact-no-punctuation";
  public static final String SUFFIX_STEMMED = "-stemmed";
  public static final String SUFFIX_UNSTEMMED = "-search";

  /* field names without boosting factors */
  private final Map<String, Long> boostings;
  private final Map<String, SearchField> fields;
  private final Map<String, List<String>> groups;

  private static final String FIELD_GROUP_ALL = "FIELD-GROUP-ALL";

  @Autowired
  public FieldProvider(final HspConfig hspConfig) {
    List<String> fields = hspConfig.getFields();
    this.groups = hspConfig.getGroups();
    this.fields = createBaseFieldsMap(fields);
    this.boostings = createBoostingMap(fields);
  }

  private static Map<String, Long> createBoostingMap(final List<String> fieldNames) {
    final Map<String, Long> result = new LinkedHashMap<>();
    String[] parts;
    for(String fieldName: fieldNames) {
      parts = fieldName.split("\\^", 2);
      if(parts.length == 2) {
        result.put(parts[0], Long.valueOf(parts[1]));
      }
    }
    return result;
  }

  private static Map<String, SearchField> createBaseFieldsMap(final List<String> fieldNames) {
    final Map<String, SearchField> result = new LinkedHashMap<>();
    final Pattern regEx = Pattern.compile("^([a-z]+(?:-[a-z]+){0,5}?-search).*");
    Matcher matcher;
    String name;
    for(String fn: fieldNames) {
      matcher = regEx.matcher(fn);
      if (matcher.matches()) {
        name = matcher.group(1);
        Field baseField = createFieldByFieldName(fieldNames, "^(" + name + ").*");
        Field exactField = createFieldByFieldName(fieldNames, "^(" + name + SUFFIX_EXACT + ").*");
        Field exactFieldNoPunctuation = createFieldByFieldName(fieldNames, "^(" + name + SUFFIX_EXACT_NO_PUNCTUATION + ").*");
        Field stemmedField = createFieldByFieldName(fieldNames, "^(" + name + SUFFIX_STEMMED + ").*");

        result.put(name, new SearchField(baseField, exactField, exactFieldNoPunctuation, stemmedField));
      }
    }
    return result;
  }

  private static Field createFieldByFieldName(final List<String> fieldNames, final String regEx) {
    final Pattern pattern = Pattern.compile(regEx);
    for(String fn : fieldNames) {
      Matcher matcher = pattern.matcher(fn);
      if(matcher.matches()) {
        return new Field(matcher.group(1));
      }
    }
    return null;
  }

  /**
   * Checks if a field name is valid i.e. the field name is part of the service's configuration
   * @param fieldName the field name to check for validity
   * @return {@code true} if the field name is valid, {@code false} otherwise
   */
  public boolean isValid(final String fieldName) {
    return fieldName != null && fields.containsKey(fieldName);
  }

  /**
   * removes optional trailing boosting factor (matches the pattern {@code '^\d+$')}
   * @param fieldName the field name
   * @return the field name excluding the optional boosting factor
   */
  public static String removeBoostingFactor(final String fieldName) {
    if (fieldName != null) {
      return fieldName.replaceAll("\\^\\d+$", "");
    }
    return null;
  }

  /**
   * removes optional trailing boosting factors (matches the pattern {@code '^\d+$')} from field names
   * a check to determine whether the field name is valid is not carried out
   * @param fieldNames the field names
   * @return the field names excluding the optional boosting factors
   */
  public static List<String> removeBoostingFactors(List<String> fieldNames) {
    return fieldNames.stream()
        .map(FieldProvider::removeBoostingFactor)
        .collect(Collectors.toList());
  }

  /**
   * Adds boosting factor according to the given field name, if there is any boosting
   * @param fieldName the field name
   * @return the field name plus boosting, if there is any
   */
  public String getBoosting(final String fieldName) {
    if(boostings.containsKey(fieldName) && boostings.get(fieldName) != null) {
      return String.format("^%s", boostings.get(fieldName));
    } else
      return StringUtils.EMPTY;
  }

  /**
   * Adds boosting factor according to the given field name, if there is any boosting
   * @param fieldName the field name
   * @return the field name plus boosting, if there is any
   */
  public String getFieldNameWithBoosting(final String fieldName) {
    return String.format("%s%s", fieldName, getBoosting(fieldName));
  }

  /**
   * Returns the associated field name for basic searches if one exists.
   * @param fieldName the field name to check for a basic variant
   * @return an {@code Optional} containing the field name for basic searches, {@code Optional.empty} if there is none
   */
  public Optional<String> getBasicName(final String fieldName) {
    final Field basicField = fields.get(fieldName).getBasic();
    if (basicField != null) {
      return Optional.of(getFieldNameWithBoosting(basicField.getName()));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns the associated field names for basic searches. If there is no basic equivalent for a field name in the list,
   * no item will be added to the result list
   * @param fieldNames the field names to check for a basic variant
   * @return a list containing the basic field names
   */
  public List<String> getBasicNames(final List<String> fieldNames) {
    return getNames(fieldNames, this::getBasicName);
  }

  /**
   * Returns the associated field name for exact searches if one exists.
   * @param fieldName the field name to check for an exact variant
   * @return an {@code Optional} containing the field name for exact searches, {@code Optional.empty} if there is none
   */
  public Optional<String> getExactName(final String fieldName) {
    final Field exactField = fields.get(fieldName).getExact();
    if (exactField != null) {
      return Optional.of(getFieldNameWithBoosting(exactField.getName()));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns the associated field names for exact searches. If there is no exact equivalent for a field name in the list,
   * no item will be added to the result list
   * @param fieldNames the field names to check for an exact variant
   * @return a list containing the exact field names
   */
  public List<String> getExactNames(final List<String> fieldNames) {
    return getNames(fieldNames, this::getExactName);
  }

  /**
   * Returns the associated field name for stemmed searches if one exists.
   * @param fieldName the field name to check for a stemmed variant
   * @return an {@code Optional} containing the field name for stemmed searches, {@code Optional.empty} if there is none
   */
  public Optional<String> getStemmedName(final String fieldName) {
    final Field stemmedField = fields.get(fieldName).getStemmed();
    if (stemmedField != null) {
      return Optional.of(getFieldNameWithBoosting(stemmedField.getName()));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns the associated field names for stemmed searches. If there is no stemmed equivalent for a field name in the list,
   * no item will be added to the result list
   * @param fieldNames the field names to check for a stemmed variant
   * @return a list containing the exact field names
   */
  public List<String> getStemmedNames(final List<String> fieldNames) {
    return getNames(fieldNames, this::getStemmedName);
  }

  /**
   * Returns the associated field name for stemmed searches if one exists.
   * @param fieldName the field name to check for an exact variant without punctuation
   * @return an {@code Optional} containing the field name for stemmed searches, {@code Optional.empty} if there is none
   */
  public Optional<String> getExactNoPunctuationName(final String fieldName) {
    final Field stemmedField = fields.get(fieldName).getExactNoPunctuation();
    if (stemmedField != null) {
      return Optional.of(getFieldNameWithBoosting(stemmedField.getName()));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns the associated field names for stemmed searches. If there is no stemmed equivalent for a field name in the list,
   * no item will be added to the result list
   * @param fieldNames the field names to check for a stemmed variant
   * @return a list containing the exact field names
   */
  public List<String> getExactNoPunctuationNames(final List<String> fieldNames) {
    return getNames(fieldNames, this::getExactNoPunctuationName);
  }

  private List<String> getNames(final List<String> fieldNames, Function<String, Optional<String>> mapper) {
    return fieldNames.stream()
        .filter(fields::containsKey)
        .map(mapper)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  /**
   * Returns all base (neither exact nor stemmed) field names
   * @return a list containing all base field names
   */
  public List<String> getFieldNames() {
    return this.fields.values().stream()
        .map(SearchField::getBasic)
        .filter(Objects::nonNull)
        .map(Field::getName)
        .collect(Collectors.toList());
  }

  /**
   * Returns all field names associated with a search group
   * @param groupName the name of the search group
   * @return a list containing all field names for the given search group
   */
  public List<String> getFieldNamesForGroup(final String groupName) {
    return groups.getOrDefault(groupName, Collections.emptyList());
  }

  /**
   * Returns all field names associated with the search group {@code ALL}
   * @return a list containing all field names for the search group {@code ALL}
   */
  public List<String> getFieldNamesForGroupAll() {
    return groups.getOrDefault(FIELD_GROUP_ALL, Collections.emptyList());
  }

  /**
   * Checks if a search group exists
   * @param groupName the group's name to be checked
   * @return {@code true} if the group exists, {@code false} otherwise
   */
  public boolean groupExists(final String groupName) {
    return Objects.nonNull(groupName) && groups.containsKey(groupName);
  }

  /**
   * Removes an optionally existing suffix, by removing every character after '-search'
   * @param fieldName the fieldname whose suffix should be removed
   * @return the fieldname without any character after '-search'
   */
  public static String removeOptionalSuffix(final String fieldName) {
    final Pattern pattern = Pattern.compile("^([a-z]+(?:-[a-z]+){0,5}-search).*");
    Matcher m = pattern.matcher(fieldName);
    if(m.matches()) {
      return m.group(1);
    }
    return fieldName;
  }
}