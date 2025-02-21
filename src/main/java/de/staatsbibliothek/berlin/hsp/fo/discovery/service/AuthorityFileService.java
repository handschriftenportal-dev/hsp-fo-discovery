package de.staatsbibliothek.berlin.hsp.fo.discovery.service;

public interface AuthorityFileService {
  <T> T findById(final String id, Class<T> clazz);
}
