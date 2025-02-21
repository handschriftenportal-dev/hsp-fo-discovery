package de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl;

import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * An extension of Solr {@ref NoOpResponseParser}, as it seems not be possible to extract the encoding exactly in the default implementation.
 * Therefore, it is set explicitly in the {@ref processResponse} method
 */

public class CustomNoOpResponseParser extends NoOpResponseParser {

  /* The encoding that should be used for processing the response */
  private String encoding;

  public CustomNoOpResponseParser(final String encoding) {
    this.encoding = encoding;
  }

  @Override
  public NamedList<Object> processResponse(InputStream body, String encoding) {
    try {
      encoding = encoding != null ? encoding : this.encoding;
      StringWriter writer = new StringWriter();
      new InputStreamReader(body, encoding).transferTo(writer);
      String output = writer.toString();
      NamedList<Object> list = new NamedList<>();
      list.add("response", output);
      return list;
    } catch (IOException e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "parsing error", e);
    }
  }
}
