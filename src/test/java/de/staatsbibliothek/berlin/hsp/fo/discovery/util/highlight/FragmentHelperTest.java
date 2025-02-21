package de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

class FragmentHelperTest {

  private static final String TAG_NAME = "em";

  @Test
  void givenHighlightedTextWithPrecedingText_whenGeneratingHighlightFragments_thenPrecedingTextIsAdded() {
    final String highlightedText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut <em>labore</em>";

    final String[] result = FragmentHelper.fragmentHighlightInformation(highlightedText, TAG_NAME, 25);

    assertThat(result, arrayContainingInAnyOrder("tempor invidunt ut <em>labore</em>"));
  }

  @Test
  void givenHighlightedTextWithShortPrecedingText_whenGeneratingHighlightFragments_onlyPrecedingTextIsAdded() {
    final String highlightedText = "<em>Lorem</em> ipsum dolor sit <em>amet</em>";

    final String[] result = FragmentHelper.fragmentHighlightInformation(highlightedText, TAG_NAME, 7);

    assertThat(result, arrayContainingInAnyOrder("<em>Lorem</em> ipsum", "sit <em>amet</em>"));
  }

  @Test
  void bla() {
    final String highlightedText = "<em>Lorem ipsum dolor</em> sit amet, <em>consetetur sadipscing elitr</em>, sed diam nonumy eirmod tempor invidunt ut labore et dolore";

    final String[] result = FragmentHelper.fragmentHighlightInformation(highlightedText, TAG_NAME, 0);

    assertThat(result, arrayContainingInAnyOrder("<em>Lorem ipsum dolor</em>", "<em>consetetur sadipscing elitr</em>"));
  }
}
