/*
 * MIT License
 *
 * Copyright (c) 2021 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.staatsbibliothek.berlin.hsp.fo.discovery.api.converter;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 
 * @author Richard Groszer {@literal <richard.grosser@uni-leipzig.de>}
 *
 */
class StringToSortPhraseConverterTest {

    private static final String DEFAULT_SORT_PHRASE = "score desc";
    
    private StringToSortPhraseConverter converter;
    
    public StringToSortPhraseConverterTest() {
      converter = new StringToSortPhraseConverter();
    }

    @Test
    void whenCalledWithIdAscString_idAscPhraseIsReturned() throws Exception {
      String sortPhrase = converter.convert("ms-identifier-asc").getSortPhrase();
      assertThat(sortPhrase, is("ms-identifier-sort asc"));
    }
    
    @Test
    void whenCalledWithIdDescString_idDescPhraseIsReturned() throws Exception {
      String sortPhrase = converter.convert("ms-identifier-desc").getSortPhrase();
      assertThat(sortPhrase, is("ms-identifier-sort desc"));
    }

    @Test
    void whenCalledWithOrigDateAscString_origDateFromAscPhraseIsReturned() throws Exception {
      String sortPhrase = converter.convert("orig-date-asc").getSortPhrase();
      assertThat(sortPhrase, is("orig-date-from-sort asc"));
    }

    @Test
    void whenCalledWithOrigDateDescString_origDateToDescPhraseIsReturned() throws Exception {
      String sortPhrase = converter.convert("orig-date-desc").getSortPhrase();
      assertThat(sortPhrase, is("orig-date-to-sort desc"));
    }

  @Test
  void whenCalledWithPublisherDateDescString_publisherDescPhraseIsReturned() throws Exception {
    String sortPhrase = converter.convert("publish-year-desc").getSortPhrase();
    assertThat(sortPhrase, is("publish-year-sort desc"));
  }

    @Test
    void whenCalledWithScoreDescString_defaultSortPhraseIsReturned() throws Exception {
      String sortPhrase = converter.convert("score-desc").getSortPhrase();
      assertThat(sortPhrase, is(DEFAULT_SORT_PHRASE));
    }
    
    @Test
    void whenCalledWithEmptyString_defaultSortPhraseIsReturned() throws Exception {
      String sortPhrase = converter.convert("").getSortPhrase();
      assertThat(sortPhrase, is(DEFAULT_SORT_PHRASE));
    }
    
    @Test
    void whenCalledWithInvalidString_defaultSortPhraseIsReturned() throws Exception {
      String sortPhrase = converter.convert("insured-value-desc").getSortPhrase();
      assertThat(sortPhrase, is(DEFAULT_SORT_PHRASE));
    }
}
