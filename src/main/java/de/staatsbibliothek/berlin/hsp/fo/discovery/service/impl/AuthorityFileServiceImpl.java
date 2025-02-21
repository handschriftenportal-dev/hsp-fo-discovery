package de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionFactory;
import de.staatsbibliothek.berlin.hsp.fo.discovery.exception.ExceptionType;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.adapter.GraphQLAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthorityFileServiceImpl implements AuthorityFileService {

  private static final String QUERY = ("query findGNDEntityFactsByIds($ids: [String]) {"
      + "  findGNDEntityFactsByIds(ids: $ids) {"
      + "    gndId: gndIdentifier"
      + "    id"
      + "    identifier {"
      + "      text"
      + "      type"
      + "      url"
      + "    }"
      + "    preferredName"
      + "    typeName"
      + "    variantName {"
      + "      name"
      + "      languageCode"
      + "    }"
      + "  }"
      + "}").replaceAll("\\p{javaSpaceChar}{2,}", " ");
  //@formatter:on

  private final GraphQLAdapter graphQLAdapter;

  private static final String RESULT_PATH = "findGNDEntityFactsByIds";
  private static final String VARIABLE_ID = "ids";

  @Autowired
  public AuthorityFileServiceImpl(final GraphQLAdapter graphQlService) {
    this.graphQLAdapter = graphQlService;
  }

  /**
   * find an authority file by a given id or name
   * @param idOrName the authority file's id or name
   * @return an instance authority-file {@code T} that matches the id or name
   */
  @Override
  public <T> T findById(final String idOrName, Class<T> clazz) {
    try {
      final Map<String, Object> variables = new HashMap<>();
      variables.put(VARIABLE_ID, idOrName);
      return graphQLAdapter.find(QUERY, variables, null, RESULT_PATH, clazz);
    } catch (GraphQlTransportException e) {
      log.error("Error while fetching GNDEntity with id {}", idOrName, e);
      throw ExceptionFactory.getException(ExceptionType.AUTHORITY_FILE_EXCEPTION, "Error while fetching GNDEntity with id " + idOrName);
    }
  }
}
