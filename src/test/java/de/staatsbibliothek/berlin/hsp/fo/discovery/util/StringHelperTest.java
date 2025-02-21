package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StringHelperTest {

  @Test
  void givenContextThatCannotBeSplit_whenLeftBoundaryIsCalculated_thenEndPositionIsReturned() {
    final String text = "LoremLorem Ipsum";

    final int result = StringHelper.leftBoundary(text, 0, 11);

    assertThat(result, is(0));
  }

  @Test
  void givenContextThatCannotBeSplit_whenRightBoundaryIsCalculated_thenStartPositionIsReturned() {
    final String text = "Ipsum DolorDolor";

    final int result = StringHelper.rightBoundary(text, 5, 16);

    assertThat(result, is(16));
  }

  @Test
  void givenMultipleDividerChars_whenLeftBoundaryIsCalculated_thenStartPositionOfTheNextWordIsReturned() {
    final String text = " (;;..)Lorem Ipsum";

    final int result = StringHelper.leftBoundary(text, 2, 13);

    assertThat(result, is(7));
  }

  @Test
  void givenMultipleDividerChars_whenRightBoundaryIsCalculated_thenEndPositionOfThePreviousWordIsReturned() {
    final String text = "Ipsum Dolor (;;..)";

    final int result = StringHelper.rightBoundary(text, 5, 16);

    assertThat(result, is(11));
  }
  @Test
  void givenBoostedString_whenRemovingBoosting_thenBoostingIsRemoved() {
    final String str = "repository-search^100";

    final String result = StringHelper.removeBoosting(str);

    assertThat(result, is("repository-search"));
  }

  @Test
  void givenUnboostedString_whenRemovingBoosting_StringRemainsTheSame() {
    final String str = "repository-search";

    final String result = StringHelper.removeBoosting(str);

    assertThat(result, is("repository-search"));
  }
}
