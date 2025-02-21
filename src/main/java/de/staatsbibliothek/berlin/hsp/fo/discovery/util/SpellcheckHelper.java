package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.QueryToken;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.StringTokenToQueryTokenConverter;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.converter.TokenType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static de.staatsbibliothek.berlin.hsp.fo.discovery.util.StringHelper.countWords;

/**
 * As it is only possible to perform a spell correction on fuzzy components of a search phrase,
 * this helper class provides a convenient way for applying the spell corrected fuzzy components
 * to the original search phrase.
 */
public class SpellcheckHelper {

  private SpellcheckHelper() {}

  /**
   * Applies spell correction on a search phrase
   *
   * @param phrase the original search phrase
   * @param correctedPhrase the spell corrected one
   * @return the spell corrected phrase
   */
  public static String applySpellCorrection(final String phrase, final String correctedPhrase) {
    if(StringUtils.isEmpty(phrase) || StringUtils.isEmpty(correctedPhrase)) {
      return phrase;
    }

    final List<String> correctedPhraseWords = new ArrayList<>(List.of(correctedPhrase.split(" ")));
    final List<QueryToken> queryTokens = StringTokenToQueryTokenConverter.convert(phrase);

    return applySpellCorrectionOnStandardTokens(queryTokens, correctedPhraseWords);
  }


  /**
   * Replaces each {@code queryToken} of {@code STANDARD} type by consuming the corresponding number of words
   * @param queryTokens the tokens to be spell corrected
   * @param spellCorrectedTokens the spell corrected {@code STANDARD} tokens
   * @return the spell corrected search phrase
   */
  private static String applySpellCorrectionOnStandardTokens(final List<QueryToken> queryTokens, List<String> spellCorrectedTokens) {
    List<String> resultItems = new ArrayList<>();
    for (QueryToken queryToken : queryTokens) {

      // spell correction is only available for standard (fuzzy) tokens (sub phrases)
      if (TokenType.STANDARD.equals(queryToken.getType())) {

        // extract spell corrected words of the length the token
        int wordCount = countWords(queryToken.getToken());
        List<String> correctedToken = spellCorrectedTokens.subList(0, wordCount);
        resultItems.addAll(correctedToken);
        correctedToken.clear();

        // for other token types use the original token
      } else {
        resultItems.add(queryToken.getToken());
      }
    }
    return String.join(" ", resultItems);
  }
}