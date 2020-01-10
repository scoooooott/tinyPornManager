/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper.util;

import java.util.ArrayList;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * String Similarity taken from: http://www.catalysoft.com/articles/StrikeAMatch.html
 * 
 * @author seans
 * 
 */
public class Similarity {
  private static final Logger LOGGER = LoggerFactory.getLogger(Similarity.class);

  /**
   * Letter pairs.
   * 
   * @param str
   *          the str
   * @return an array of adjacent letter pairs contained in the input string
   */
  private static String[] letterPairs(String str) {
    if (str.length() == 1) {
      // fill up to min 2 chars
      str += " ";
    }

    int numPairs = str.length() - 1;
    // address an issue where str is ""
    if (numPairs < 0) {
      numPairs = 0;
    }
    String[] pairs = new String[numPairs];
    for (int i = 0; i < numPairs; i++) {
      pairs[i] = str.substring(i, i + 2);
    }
    return pairs;
  }

  /**
   * Word letter pairs.
   * 
   * @param str
   *          the str
   * @return an ArrayList of 2-character Strings.
   */
  private static ArrayList<String> wordLetterPairs(String str) {

    ArrayList<String> allPairs = new ArrayList<>();
    // Tokenize the string and put the tokens/words into an array
    String[] words = str.split("\\s");
    // For each word
    for (int w = 0; w < words.length; w++) {
      // Find the pairs of characters
      String[] pairsInWord = letterPairs(words[w]);
      for (int p = 0; p < pairsInWord.length; p++) {
        allPairs.add(pairsInWord[p]);
      }
    }

    return allPairs;
  }

  /**
   * Compare strings.
   * 
   * @param str1
   *          the str1
   * @param str2
   *          the str2
   * @return lexical similarity value in the range [0,1]
   */
  public static float compareStrings(String str1, String str2) {
    if (str1 == null || str2 == null) {
      return 0.0f;
    }
    if (str1.equalsIgnoreCase(str2)) {
      return 1.0f;
    }

    try {
      ArrayList<String> pairs1 = wordLetterPairs(str1.toUpperCase(Locale.ROOT));
      ArrayList<String> pairs2 = wordLetterPairs(str2.toUpperCase(Locale.ROOT));

      int intersection = 0;
      int union = pairs1.size() + pairs2.size();
      for (int i = 0; i < pairs1.size(); i++) {
        Object pair1 = pairs1.get(i);
        for (int j = 0; j < pairs2.size(); j++) {
          Object pair2 = pairs2.get(j);
          if (pair1.equals(pair2)) {
            intersection++;
            pairs2.remove(j);
            break;
          }
        }
      }

      float score = (float) (2.0 * intersection) / union;
      if (Float.isNaN(score)) {
        score = 0;
      }
      // do not downgrade score, b/c we skip duplicate 100% matches in task
      // and we had the bug, that 0.9 is lower then the second match, where it
      // took the wrong movie
      // and if the 2 results get 99% there's also a chance of takeing the wrong
      // one
      //
      // if (score == 1.0f) {
      // // exception case... for some reason, "Batman Begins" ==
      // // "Batman Begins 2"
      // // for the lack of a better test...
      // if (str1.equalsIgnoreCase(str2)) {
      // return score;
      // }
      // else {
      // LOGGER.warn("Adjusted the perfect score to " + 0.90 + " for " + str1 +
      // " and " + str2 + " because they are not equal.");
      // // adjust the score, because only 2 strings should be equal.
      // score = 0.90f;
      // }
      // }

      return score;
    }
    catch (Exception e) {
      LOGGER.warn("Exception in compareStrings str1 = " + str1 + " str12 = " + str2);
      return (float) 0.0;
    }
  }
}
