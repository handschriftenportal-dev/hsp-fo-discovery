package de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight;

import de.staatsbibliothek.berlin.hsp.fo.discovery.util.ListHelper;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

class ListHelperTest {
  @Test
  void givenNullValue_whenRemoveDuplicates_thenEmptyListIsReturned() {
    final List<String> result = ListHelper.removeDuplicates(null);

    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(0));
  }

  @Test
  void givenListWithDuplicates_whenRemoveDuplicates_thenResultIsDistinct() {
    final List<String> testData = List.of("a", "b", "a", "c");

    final List<String> result = ListHelper.removeDuplicates(testData);

    assertThat(result, contains("a", "b", "c"));
  }

  @Test
  void givenNumericEqualityCheckerAndAdditionMerger_whenMergingLists_thenResultIsCorrect() {
    BiPredicate<Integer, Integer> equals = (i1, i2) -> i1 == i2;
    BinaryOperator<Integer> merge = (i1, i2) -> i1 + i2;

    final List<Integer> l1 = List.of(1, 2, 3, 4, 5);
    final List<Integer> l2 = List.of(4, 5, 6);

    final List<Integer> result = ListHelper.merge(l1, l2, equals, merge);

    assertThat(result, contains(1, 2, 3, 8, 10, 6));
  }

  @Test
  void givenTrimEqualityCheckerAndSpaceAdditionMerger_whenMergingLists_thenResultIsCorrect() {
    BiPredicate<String, String> equals = (s1, s2) -> StringUtils.trim(s1).equals(StringUtils.trim(s2));
    BinaryOperator<String> merge = (s1, s2) -> s1 + " ";

    final List<String> l1 = List.of("a", "b", "c", "d", "e");
    final List<String> l2 = List.of("d", "e", "f");

    final List<String> result = ListHelper.merge(l1, l2, equals, merge);

    assertThat(result, contains("a", "b", "c", "d ", "e ", "f"));
  }
}
