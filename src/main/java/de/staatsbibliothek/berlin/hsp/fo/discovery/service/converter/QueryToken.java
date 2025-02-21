package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * information about a query's token (term), containing the token (term) itself and the type of the token
 */
@AllArgsConstructor
@Data
public class QueryToken {
  private String token;
  private TokenType type;
}
