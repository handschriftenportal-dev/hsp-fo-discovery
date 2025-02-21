package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

/**
 * converter for converting extended search strings into query params
 */
public interface ExtendedSearchStringToQueryParamsConverter {
  /**
   * converts the given {@code extended phrase} into {@link QueryParams}
   * @param extendedPhrase the phrase to match
   * @return {@link QueryParams} containing the converted extended Phrase
   */
  QueryParams convert(final String extendedPhrase);
}
