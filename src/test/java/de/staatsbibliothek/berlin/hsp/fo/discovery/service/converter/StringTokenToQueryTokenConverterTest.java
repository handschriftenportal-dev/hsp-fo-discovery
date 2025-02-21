package de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class StringTokenToQueryTokenConverterTest {
  @Test
  void whenStringTokenIsUnquoted_thenStandardQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("abc");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("abc"));
    assertThat(actualQueryTokens.get(0).getType(), is(TokenType.STANDARD));
  }

  @Test
  void whenStringTokenIsUnquotedAndContainsEscapedQuotationMark_thenStandardQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\\\"abc\\\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\\\"abc\\\""));
    assertThat(actualQueryTokens.get(0).getType(), is(TokenType.STANDARD));
  }

  @Test
  void whenStringTokenIsUnquotedAndContainsWildcardCharacter_thenStandardQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("ab*c");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("ab*c"));
    assertThat(actualQueryTokens.get(0).getType(), is(TokenType.STANDARD));
  }

  @Test
  void whenStringTokenIsQuoted_thenExactQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\"abc\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\"abc\""));
    assertThat(actualQueryTokens.get(0).getType(), is(TokenType.EXACT));
  }

  @Test
  void whenStringTokenIsQuotedAndContainsWildcardCharacter_thenComplexQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\"ab*c\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\"ab*c\""));
    assertThat(actualQueryTokens.get(0).getType(), is(TokenType.COMPLEX));
  }

  @Test
  void whenStringTokenIsQuotedAndContainsEscapedWildcardCharacter_thenExactQueryTokenIsReturned() {
    final List<String> stringTokens = List.of("\"ab\\*c\"");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(1));
    assertThat(actualQueryTokens.get(0).getToken(), is("\"ab\\*c\""));
    assertThat(actualQueryTokens.get(0).getType(), is(TokenType.EXACT));
  }

  @Test
  void whenMultipleStringTokensArePassed_thenExactQueryTokensAreReturned() {
    final List<String> stringTokens = List.of("\"ab\\*c\"", "\"ab*c\"", "ab*c");

    final List<QueryToken> actualQueryTokens = StringTokenToQueryTokenConverter.convert(stringTokens);

    assertThat(actualQueryTokens, hasSize(3));

    assertThat(actualQueryTokens.get(0).getToken(), is("\"ab\\*c\""));
    assertThat(actualQueryTokens.get(0).getType(), is(TokenType.EXACT));

    assertThat(actualQueryTokens.get(1).getToken(), is("\"ab*c\""));
    assertThat(actualQueryTokens.get(1).getType(), is(TokenType.COMPLEX));

    assertThat(actualQueryTokens.get(2).getToken(), is("ab*c"));
    assertThat(actualQueryTokens.get(2).getType(), is(TokenType.STANDARD));
  }
}
