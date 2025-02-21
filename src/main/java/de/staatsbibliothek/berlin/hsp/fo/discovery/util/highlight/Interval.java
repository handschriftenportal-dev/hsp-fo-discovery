package de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a numeric interval, defined by start and end point
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
class Interval {
  int start;
  int end;
}
