package de.staatsbibliothek.berlin.hsp.fo.discovery.persistence.impl;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

/**
 * A Request for fetching file information from the Solr admin API
 */
public class FileRequest extends SolrRequest<SolrResponseBase> {

  private ModifiableSolrParams params;

  private static final String PATH = "/admin/file";
  private static final METHOD METHOD = SolrRequest.METHOD.GET;
  public FileRequest() {
    super(METHOD, PATH);
    this.params = new ModifiableSolrParams();
    this.setResponseParser(new CustomNoOpResponseParser("utf-8"));
  }

  @Override
  public String getRequestType() {
    return SolrRequestType.ADMIN.toString();
  }

  @Override
  public SolrParams getParams() {
    return params;
  }

  public void setFileName(final String fileName) {
    this.params.set("file", fileName);
  }

  @Override
  protected SolrResponseBase createResponse(SolrClient client) {
    return new SolrResponseBase();
  }
}