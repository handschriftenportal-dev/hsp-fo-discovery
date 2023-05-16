/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.staatsbibliothek.berlin.hsp.fo.discovery.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StringTokenizer {
  /**
   * Tokenizes a string by using the double quotation mark as delimiter, but preserving it within the corresponding token(s)
   * @param str the String to be tokenized
   * @return a list of strings representing the tokens
   */
  public static List<String> tokenize(final String str) {
    final ArrayList<String> tokens = new ArrayList<>();
    String token = "";
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == '"' && i != 0 && str.charAt(i-1) != '\\') {
        if (token.length() > 0 && token.charAt(0) == '"') {
          token = endQuotedToken(token, tokens, str.charAt(i));
        } else {
          token = startQuotedToken(token, tokens, str.charAt(i));
        }
      } else {
        token += str.charAt(i);
        if(i == str.length() - 1) {
          token = StringUtils.trim(token);
          if(token.length() != 0) {
            tokens.add(token);
          }
        }
      }
    }
    return tokens;
  }

  private static String startQuotedToken(String token, List<String> tokens, final char currentCharacter) {
    token = StringUtils.trim(token);
    if(token.length() > 0) {
      tokens.add(token);
    }
    return String.valueOf(currentCharacter);
  }

  private static String endQuotedToken(String token, List<String> tokens, final char currentCharacter) {
    token += currentCharacter;
    if(token.length() > 2) {
      tokens.add(token);
    }
    return "";
  }
}
