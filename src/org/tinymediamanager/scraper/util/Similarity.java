/*
 * Copyright 2012 Manuel Laggner
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

import org.apache.log4j.Logger;

/**
 * String Similarity taken from:
 * http://www.catalysoft.com/articles/StrikeAMatch.html
 * 
 * @author seans
 * 
 */
public class Similarity {

  /** The instance. */
  private static Similarity   instance = new Similarity();

  /** The Constant log. */
  private static final Logger LOGGER   = Logger.getLogger(Similarity.class);

  /**
   * Letter pairs.
   * 
   * @param str
   *          the str
   * @return an array of adjacent letter pairs contained in the input string
   */
  private String[] letterPairs(String str) {

    int numPairs = str.length() - 1;
    // address an issue where str is ""
    if (numPairs < 0)
      numPairs = 0;

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
  @SuppressWarnings("unchecked")
  private ArrayList wordLetterPairs(String str) {

    ArrayList allPairs = new ArrayList();

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
  @SuppressWarnings("unchecked")
  public float compareStrings(String str1, String str2) {
    if (str1 == null || str2 == null)
      return 0.0f;
    if (str1.toUpperCase().equals(str2.toUpperCase())) {
      return 1.0f;
    }

    try {
      ArrayList pairs1 = wordLetterPairs(str1.toUpperCase());
      ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());

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
      if (Float.isNaN(score))
        score = 0;
      if (score == 1.0f) {
        // exception case... for some reason, "Batman Begins" ==
        // "Batman Begins 2"
        // for the lack of a better test...
        if (str1.toUpperCase().equals(str2.toUpperCase())) {
          return score;
        }
        else {
          LOGGER.warn("Adjusted the perfect score to " + 0.90 + " for " + str1 + " and " + str2 + " because they are not equal.");
          // adjust the score, because only 2 strings should be equal.
          score = 0.90f;
        }
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(String.format("Similarity Score: [%s][%s]=[%s]", str1, str2, score));
      }
      return score;
    }
    catch (Exception e) {
      LOGGER.debug("Exception in compareStrings str1 = " + str1 + " str12 = " + str2);
      return (float) 0.0;
    }
  }

  /**
   * Gets the single instance of Similarity.
   * 
   * @return single instance of Similarity
   */
  public static Similarity getInstance() {
    return instance;
  }
}
