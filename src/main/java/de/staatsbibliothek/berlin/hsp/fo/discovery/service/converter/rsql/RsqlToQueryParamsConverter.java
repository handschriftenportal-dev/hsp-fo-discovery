package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.ExtendedSearchStringToQueryParamsConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryParams;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class RsqlToQueryParamsConverter implements ExtendedSearchStringToQueryParamsConverter {
  private final static RSQLParser parser = new RSQLParser();
  private SolrVisitor solrVisitor;

  public RsqlToQueryParamsConverter() {}

  @Autowired
  public void setSolrVisitor(final SolrVisitor solrVisitor) {
    this.solrVisitor = solrVisitor;
  }

  @Override
  public QueryParams convert(String extendedPhrase) {
    final Set<String> fields = new HashSet<>();
    final Node node = parser.parse(extendedPhrase);
    final String query = node.accept(solrVisitor, fields);
    log.info("Converted the given search phrase:\n {} \n to:\n{}", extendedPhrase, query);
    return new QueryParams(fields, query, Collections.emptyList(), QueryType.EXTENDED);
  }
}
