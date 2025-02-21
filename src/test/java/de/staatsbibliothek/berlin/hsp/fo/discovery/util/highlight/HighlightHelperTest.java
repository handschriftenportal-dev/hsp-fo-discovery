package de.staatsbibliothek.berlin.hsp.fo.discovery.util.highlight;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class HighlightHelperTest {

  private static final String TAG_NAME = "em";

  @Test
  void whenMergeHighlightFragmentsWithoutSpaceInBetween_thenNoSpacesAreAdded() {
    final String highlightedText = "<em>foo</em><em>bar</em>";

    final String result = HighlightHelper.mergeContiguousElements(highlightedText, TAG_NAME);

    assertThat(result, is("<em>foobar</em>"));
  }

  @Test
  void whenMergeHighlightFragments_thenOtherElementsArePreserved() {
    final String highlightedText = "<em>foo</em><p>bar</p><em>baz</em>";

    final String result = HighlightHelper.mergeContiguousElements(highlightedText, TAG_NAME);

    assertThat(result, is("<em>foo</em><p>bar</p><em>baz</em>"));
  }

  @Test
  void whenMergingHighlightInformation_thenOverlappingHighlightFragmentsAreMerged() {
    final List<String> highlightList  = List.of("Lorem <em>ipsum</em> dolor sit amet, consetetur sadipscing <em>elitr, sed diam</em>");
    final List<String> anotherHighlightList = List.of("<em>Lorem</em> ipsum dolor sit amet, <em>consetetur sadipscing elitr, sed</em> diam");

    final List<String> result = HighlightHelper.mergeHighlights(highlightList, anotherHighlightList, TAG_NAME);

    assertThat(result, contains("<em>Lorem ipsum</em> dolor sit amet, <em>consetetur sadipscing elitr, sed diam</em>"));
  }

  @Test
  void givenHighlightingInformationWithNonHighlightingTag_whenMergingHighlightInformation_thenResultStillContainsNonHighlightingTag() {
    final String highlightedText = "<em><pre>Lorem</pre></em> ipsum dolor sit amet, consetetur sadipscing <em>elitr, sed diam</em>";
    final String anotherHighlightedText = "<em><pre>Lorem</pre></em> ipsum dolor sit amet, <em>consetetur sadipscing elitr, sed</em> diam";

    final List<String> result = HighlightHelper.mergeHighlights(List.of(highlightedText), List.of(anotherHighlightedText), TAG_NAME);

    assertThat(result, containsInAnyOrder("<em><pre>Lorem</pre></em> ipsum dolor sit amet, <em>consetetur sadipscing elitr, sed diam</em>"));
  }

  @Test
  void givenHighlightedTextWithSpecialChars_whenMergingHighlights_thenResultIsCorrect() {
    final String highlightedText = "Oesterley,   Gesta <em>Romanorum</em>, 1872, S. 589, Nr. 183, germ. 15 und S. 103 Nr. 25, 40; zu 3.: Zitat aus Beda Venerabilis , Historia ecclesiastica, Liber 1, Caput 1; Druck: PL, Bd. 95, Sp. 23; anschließend kurzes Exzerpt";
    final String anotherHighlightedText = "Oesterley,   Gesta Romanorum, 1872, S. 589, Nr. 183, germ. 15 und S. 103 Nr. 25, 40; zu 3.: Zitat aus Beda <em>Venerabilis</em> , Historia ecclesiastica, Liber 1, Caput 1; Druck: PL, Bd. 95, Sp. 23; anschließend kurzes Exzerpt";

    final List<String> result = HighlightHelper.mergeHighlights(List.of(highlightedText), List.of(anotherHighlightedText), TAG_NAME);

    assertThat(result, contains("Oesterley,   Gesta <em>Romanorum</em>, 1872, S. 589, Nr. 183, germ. 15 und S. 103 Nr. 25, 40; zu 3.: Zitat aus Beda <em>Venerabilis</em> , Historia ecclesiastica, Liber 1, Caput 1; Druck: PL, Bd. 95, Sp. 23; anschließend kurzes Exzerpt"));
  }

  @Test
  void givenHighlightedTextWithTag_whenMergingHighlightedTerms_thenResultIsCorrect() {
    final String highlightedText = "<em>Johannes</em> <Damascenus>";

    final String result = HighlightHelper.mergeContiguousElements(highlightedText, TAG_NAME);

    assertThat(result, is("<em>Johannes</em> <Damascenus>"));
  }

  @Test
  void givenMultiValuedHighlights_whenMerging_thenHighlightsAreMerged() {
    final List<String> highlightList = List.of("<em>Universitätsbibliothek</em> Leipzig and other <em>information</em>", "<em>Universitätsbibliothek</em> Chemnitz", "<em>Universitätsbibliothek</em> Greifswald", "Universitätsbibliothek Paderborn");
    final List<String> anotherHighlightList = List.of("<em>Universitätsbibliothek</em> Leipzig and other information", "Universitätsbibliothek <em>Chemnitz</em>", "<em>Universitätsbibliothek</em> Greifswald", "Universitätsbibliothek Paderborn", "Universitätsbibliothek Tübingen");

    final List<String> result = HighlightHelper.mergeHighlights(highlightList, anotherHighlightList, TAG_NAME);

    assertThat(result, containsInAnyOrder("<em>Universitätsbibliothek</em> Leipzig and other <em>information</em>", "<em>Universitätsbibliothek Chemnitz</em>", "<em>Universitätsbibliothek</em> Greifswald", "Universitätsbibliothek Paderborn", "Universitätsbibliothek Tübingen"));
  }

  @Test
  void givenMultiValuedHighlightsAndEmptyHighlights_whenMerging_thenHighlightsAreMerged() {
    final List<String> highlightList = List.of("<em>Universitätsbibliothek</em> Leipzig and other <em>information</em>", "<em>Universitätsbibliothek</em> Chemnitz", "<em>Universitätsbibliothek</em> Greifswald", "Universitätsbibliothek Paderborn");
    final List<String> anotherHighlightList = List.of();

    final List<String> result = HighlightHelper.mergeHighlights(highlightList, anotherHighlightList, TAG_NAME);

    assertThat(result, contains(highlightList.toArray()));
  }
}