package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

public class ListHelper {

  private ListHelper() {}

  /**
   * Removes all duplicate items of the given list {@code items}
   * If @{code items} is {@code null}, an empty {@code List} is returned
   * @param items the list from which duplicates should be removed
   * @return the resulting {@code List}
   * @param <T> the type of elements in this list
   */
  public static <T> List<T> removeDuplicates(@Nullable final List<T> items) {
    return CollectionUtils.emptyIfNull(items)
        .stream()
        .distinct()
        .toList();
  }

  /**
   * Merge two lists. Checks equality by using the given {@code equals} function
   * If two items are equal, the items will be merged by using the given {@code merge} function.
   * Otherwise, it will be added unmodified to the result list.
   *
   * @param list First {@code List} of type {@code T} that should be merged
   * @param anotherList Second {@code List} of type {@code T} that should be merged
   * @param equals function that is used for checking two items for equality
   * @param merge function for merging two items that are equal
   * @return A {@code List} of type {@code T} containing the merged lists.
   * @param <T> the type of elements in this list
   */
  public static <T> List<T> merge(@Nullable final List<T> list, @Nullable List<T> anotherList, final BiPredicate<T, T> equals, final BinaryOperator<T> merge) {
    final List<T> l1 = removeDuplicates(list);
    final List<T> l2 = removeDuplicates(anotherList);

    // check conditions for premature return
    if (l1.isEmpty() && l2.isEmpty()) {
      return Collections.emptyList();
    } else if (l1.isEmpty()) {
      return new ArrayList<>(l2);
    } else if (l2.isEmpty()) {
      return new ArrayList<>(l1);
    }

    List<T> res = new ArrayList<>(Math.max(l1.size(), l2.size()));

    // compare all items in l1 with items in l2
    for (T element1 : l1) {
      boolean merged = false;
      for (T element2 : l2) {
        if (equals.test(element1, element2)) {
          res.add(merge.apply(element1, element2));
          merged = true;
          break;
        }
      }
      if (!merged) {
        res.add(element1);
      }
    }

    // finally compare items in l2 with items in l1
    for (T element2 : l2) {
      boolean found = false;
      for (T element1 : l1) {
        if (equals.test(element1, element2)) {
          found = true;
          break;
        }
      }
      if(!found) {
        res.add(element2);
      }
    }

    return res;
  }
}
