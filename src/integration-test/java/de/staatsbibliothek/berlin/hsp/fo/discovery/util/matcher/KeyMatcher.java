package de.staatsbibliothek.berlin.hsp.fo.discovery.util.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Map;
import java.util.Set;

public class KeyMatcher {

  private KeyMatcher() {}

  public static Matcher<Map<String, ? extends Object>> allKeys(final Set<String> keys) {
    return new TypeSafeMatcher<>() {

      @Override
      protected boolean matchesSafely(Map<String, ?> map) {
        Set<String> mapKeys = map.keySet();
        if (mapKeys.size() != keys.size()) {
          return false;
        }
        for (String key : keys) {
          if (!mapKeys.contains(key)) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a map containing exactly ").appendValueList("", ", ", "", keys);
      }
    };
  }
}