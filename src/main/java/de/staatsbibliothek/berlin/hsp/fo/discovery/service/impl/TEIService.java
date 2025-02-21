package de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.discovery.dto.Result;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryResponse2ResponseEntityConverter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TEIService extends BaseServiceImpl<String> {

  public TEIService() {
    super(Collections.emptyMap(), String.class);
  }

  @Override
  public Result<List<String>> find(final SearchParams searchParams) {
    final QueryResponse response = search(searchParams);
    return new Result<>(QueryResponse2ResponseEntityConverter.extractTeiDocumentsFromSolrResponse(response));
  }
}
