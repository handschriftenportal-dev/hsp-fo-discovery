package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents all variants of a searchable field. These are:
 * basic (used for standard fuzzy searches)
 * exact (used for exact searches)
 *  * exact (used for exact searches by ignoring any kind of interpunctuation)
 * stemmed (used for stemmed searches)
 */
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Data
@NoArgsConstructor
public class SearchField {
  private Field basic;
  private Field exact;
  private Field exactNoPunctuation;
  private Field stemmed;
}
