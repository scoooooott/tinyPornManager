package org.tinymediamanager.scraper.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.SorensenDice;

public class ITSimilarityTest {

  // as stated on http://www.catalysoft.com/articles/StrikeAMatch.html
  // the implemented algorithm is already known as Dice's Coefficient.
  // reference impl here (+ one optimized)
  // https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Dice%27s_coefficient#Java

  @Test
  public void testSimilarity() {
    System.out.println(padRight("", 50) + "Ours\t\tAlt1\t\tAlt2\t\tNorm. Lev\tJaro Winkler\tCosine\t\tJaccard\t\tSorensen Dice\tCosine2");
    compareAlgs("revenant", "The Revenant - Der Rückkehrer");
    compareAlgs("revenant", "A World Unseen: The Revenant");
    // OUTCOME:
    // Our impl differs only 0.02 - and sees the second better. (same as a2)
    // Alt1 differ 0.06, and sees the first better.
    compareAlgs("revenant", "Der Rückkehrer - The Revenant"); // a1 higher that first, ours/a2 lower

    compareAlgs("revenant", "Le Revenant"); // ours:0.43 to first, alt:0.39
    compareAlgs("revenant", "Un revenant"); // ours:0.43 to first, alt:0.39
    compareAlgs("revenant", "Revenant - Sie kommen in der Nacht"); // ours:0.02 to first, alt:0.11

    compareAlgs("Un Revenant", "Le Revenant");
    compareAlgs("rEVENANT", "Revenant"); // 1.0
    compareAlgs("RRR", "RRRRRRR"); // not 1.0 (a1 fails - can this happen?)
    compareAlgs("R", "RRRRRRR"); // 0.0

    compareAlgs("Godfather", "The Godfather");
  }

  private void compareAlgs(String s1, String s2) {
    float f = Similarity.compareStrings(s1, s2);
    double a1 = ITSimilarityTest.diceCoefficient(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));
    double a2 = ITSimilarityTest.diceCoefficientOptimized(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));

    NormalizedLevenshtein levenshtein = new NormalizedLevenshtein();
    double a3 = levenshtein.similarity(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));

    JaroWinkler jaroWinkler = new JaroWinkler();
    double a4 = jaroWinkler.similarity(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));

    Cosine cosine = new Cosine();
    double a5 = cosine.similarity(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));

    Jaccard jaccard = new Jaccard();
    double a6 = jaccard.similarity(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));

    SorensenDice sorensenDice = new SorensenDice();
    double a7 = sorensenDice.similarity(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));

    double a8 = cosineSimilarity(s1.toLowerCase(Locale.ROOT), s2.toLowerCase(Locale.ROOT));

    System.out.println(padRight(s1 + " | " + s2, 50)
        + String.format("%.2f\t\t%.2f\t\t%.2f\t\t%.2f\t\t%.2f\t\t\t%.2f\t\t%.2f\t\t%.2f\t\t\t%.2f", f, a1, a2, a3, a4, a5, a6, a7, a8));
  }

  // Note that this implementation is case-sensitive!
  public static double diceCoefficient(String s1, String s2) {
    Set<String> nx = new HashSet<String>();
    Set<String> ny = new HashSet<String>();

    for (int i = 0; i < s1.length() - 1; i++) {
      char x1 = s1.charAt(i);
      char x2 = s1.charAt(i + 1);
      String tmp = "" + x1 + x2;
      nx.add(tmp);
    }
    for (int j = 0; j < s2.length() - 1; j++) {
      char y1 = s2.charAt(j);
      char y2 = s2.charAt(j + 1);
      String tmp = "" + y1 + y2;
      ny.add(tmp);
    }

    Set<String> intersection = new HashSet<String>(nx);
    intersection.retainAll(ny);
    double totcombigrams = intersection.size();

    return (2 * totcombigrams) / (nx.size() + ny.size());
  }

  /**
   * Here's an optimized version of the dice coefficient calculation. It takes advantage of the fact that a bigram of 2 chars can be stored in 1 int,
   * and applies a matching algorithm of O(n*log(n)) instead of O(n*n).
   * 
   * <p>
   * Note that, at the time of writing, this implementation differs from the other implementations on this page. Where the other algorithms
   * incorrectly store the generated bigrams in a set (discarding duplicates), this implementation actually treats multiple occurrences of a bigram as
   * unique. The correctness of this behavior is most easily seen when getting the similarity between "GG" and "GGGGGGGG", which should obviously not
   * be 1.
   * 
   * @param s
   *          The first string
   * @param t
   *          The second String
   * @return The dice coefficient between the two input strings. Returns 0 if one or both of the strings are {@code null}. Also returns 0 if one or
   *         both of the strings contain less than 2 characters and are not equal.
   * @author Jelle Fresen
   */
  public static double diceCoefficientOptimized(String s, String t) {
    // Verifying the input:
    if (s == null || t == null)
      return 0;
    // Quick check to catch identical objects:
    if (s.equals(t))
      return 1;
    // avoid exception for single character searches
    if (s.length() < 2 || t.length() < 2)
      return 0;

    // Create the bigrams for string s:
    final int n = s.length() - 1;
    final int[] sPairs = new int[n];
    for (int i = 0; i <= n; i++)
      if (i == 0)
        sPairs[i] = s.charAt(i) << 16;
      else if (i == n)
        sPairs[i - 1] |= s.charAt(i);
      else
        sPairs[i] = (sPairs[i - 1] |= s.charAt(i)) << 16;

    // Create the bigrams for string t:
    final int m = t.length() - 1;
    final int[] tPairs = new int[m];
    for (int i = 0; i <= m; i++)
      if (i == 0)
        tPairs[i] = t.charAt(i) << 16;
      else if (i == m)
        tPairs[i - 1] |= t.charAt(i);
      else
        tPairs[i] = (tPairs[i - 1] |= t.charAt(i)) << 16;

    // Sort the bigram lists:
    Arrays.sort(sPairs);
    Arrays.sort(tPairs);

    // Count the matches:
    int matches = 0, i = 0, j = 0;
    while (i < n && j < m) {
      if (sPairs[i] == tPairs[j]) {
        matches += 2;
        i++;
        j++;
      }
      else if (sPairs[i] < tPairs[j])
        i++;
      else
        j++;
    }
    return (double) matches / (n + m);
  }

  /**
   * @param terms
   *          values to analyze
   * @return a map containing unique terms and their frequency
   */
  public static Map<String, Integer> getTermFrequencyMap(String[] terms) {
    Map<String, Integer> termFrequencyMap = new HashMap<>();
    for (String term : terms) {
      Integer n = termFrequencyMap.get(term);
      n = (n == null) ? 1 : ++n;
      termFrequencyMap.put(term, n);
    }
    return termFrequencyMap;
  }

  /**
   * @param text1
   * @param text2
   * @return cosine similarity of text1 and text2
   */
  public static double cosineSimilarity(String text1, String text2) {
    // Get vectors
    Map<String, Integer> a = getTermFrequencyMap(text1.split("\\W+"));
    Map<String, Integer> b = getTermFrequencyMap(text2.split("\\W+"));

    // Get unique words from both sequences
    HashSet<String> intersection = new HashSet<>(a.keySet());
    intersection.retainAll(b.keySet());

    double dotProduct = 0, magnitudeA = 0, magnitudeB = 0;

    // Calculate dot product
    for (String item : intersection) {
      dotProduct += a.get(item) * b.get(item);
    }

    // Calculate magnitude a
    for (String k : a.keySet()) {
      magnitudeA += Math.pow(a.get(k), 2);
    }

    // Calculate magnitude b
    for (String k : b.keySet()) {
      magnitudeB += Math.pow(b.get(k), 2);
    }

    // return cosine similarity
    return dotProduct / Math.sqrt(magnitudeA * magnitudeB);
  }

  public static String padRight(String s, int n) {
    return String.format("%1$-" + n + "s", s);
  }
}
