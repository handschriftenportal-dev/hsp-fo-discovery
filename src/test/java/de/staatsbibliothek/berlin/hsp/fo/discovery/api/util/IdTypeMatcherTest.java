package de.staatsbibliothek.berlin.hsp.fo.discovery.api.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class IdTypeMatcherTest {
  @Test
  void givenValidHSPId_whenTypeMatcherIsCalled_thenHspIsReturned() {
    assertThat(IdTypeMatcher.match("HSP-01234567-89ab-cdef-0123-456789abcdef"), is(IdTypeMatcher.IdType.HSP_OBJECT));
  }

  @Test
  void givenValidAuthorityFileId_whenTypeMatcherIsCalled_thenHspIsReturned() {
    assertThat(IdTypeMatcher.match("NORM-01234567-89ab-cdef-0123-456789abcdef"), is(IdTypeMatcher.IdType.AUTHORITY_FILE));
  }

  @Test
  void givenInvalidId_whenTypeMatcherIsCalled_thenUnknownIsReturned() {
    assertThat(IdTypeMatcher.match("TEST-012345678"), is(IdTypeMatcher.IdType.UNKNOWN));
  }
}
