package org.tinymediamanager.scraper.util;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class LanguageUtilsTest {

  @Test
  public void localizedCountries() {
    assertEqual("", LanguageUtils.getLocalizedCountry());
    System.out.println(LanguageUtils.getLocalizedCountry("German", "dE"));

    assertEqual("Vereinigte Staaten von Amerika", LanguageUtils.getLocalizedCountryForLanguage(Locale.GERMAN, "USA", "en_US", "US"));
    assertEqual("Vereinigte Staaten von Amerika", LanguageUtils.getLocalizedCountryForLanguage(Locale.GERMANY, "USA", "en_US", "US"));
    assertEqual("Vereinigte Staaten von Amerika", LanguageUtils.getLocalizedCountryForLanguage("de", "USA", "en_US", "US"));
    assertEqual("United States", LanguageUtils.getLocalizedCountryForLanguage("en", "USA", "en_US", "US"));
    assertEqual("Etats-Unis", LanguageUtils.getLocalizedCountryForLanguage("fr", "USA", "en_US", "US"));

    assertEqual("West Germany", LanguageUtils.getLocalizedCountryForLanguage("de", "West Germany", "XWG"));
  }

  // own method to get some logging ;)
  public static void assertEqual(Object expected, Object actual) {
    try {
      Assert.assertEquals(expected, actual);
      System.out.println(expected + " - passed");
    }
    catch (AssertionError e) {
      System.err.println(expected + " - FAILED: " + e.getMessage());
      throw e;
    }
  }
}
