package de.staatsbibliothek.berlin.hsp.fo.discovery.service.adapter;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class GraphQLAdapter {
  private final HttpGraphQlClient httpGraphQlClient;
  private final CloseableHttpAsyncClient closeableHttpAsyncClient;

  public GraphQLAdapter(final WebClient.Builder webClientBuilder,
                        final CloseableHttpAsyncClient closeableHttpAsyncClient,
                        @Value("${authority-file.id}") final String serviceName,
                        @Value("${authority-file.path}") final String path,
                        @Value("${authority-file.port:#{null}}") final Integer port,
                        @Value("${authority-file.protocol}") final String protocol) {
    this.closeableHttpAsyncClient = closeableHttpAsyncClient;
    this.httpGraphQlClient = configure(webClientBuilder, serviceName, path, port, protocol);
  }

  private HttpGraphQlClient configure(final WebClient.Builder webClientBuilder, final String serviceName, final String path, final Integer port, final String protocol) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(serviceName)
        .path(path);

    if(port != null) {
      builder.port(port);
    }
    final String baseUri = builder.build().toUriString();

    closeableHttpAsyncClient.start();
    WebClient webClient = webClientBuilder
        .baseUrl(baseUri)
        .clientConnector(new HttpComponentsClientHttpConnector(closeableHttpAsyncClient))
        .build();
    return HttpGraphQlClient.builder(webClient).build();
  }

  /**
   * finds one or many entities of the given {@code clazz}
   * @param query the graphQL query
   * @param variables the graphQL variables
   * @param operation the graphQL operation name
   * @param resultPath the path to the result data
   * @param clazz the clazz the result should be mapped to
   * @return the found entity resp. entities (therefore the clazz needs to be an array, for example String[].class)
   * @param <T> the generic type
   */
  public <T> T find(final String query, final Map<String, Object> variables, final String operation, final String resultPath, final Class<T> clazz) {
    return httpGraphQlClient
        .document(query)
        .variables(variables)
        .operationName(operation)
        .retrieve(resultPath)
        .toEntity(clazz)
        .block();
  }
}