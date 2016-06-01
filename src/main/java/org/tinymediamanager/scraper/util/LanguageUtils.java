/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * This is a helper class for language related tasks
 *
 * @author Manuel Laggner
 * @since 2.0
 */
public class LanguageUtils {
  // Map of all known English/UserLocalized String to base locale, key is LOWERCASE
  public final static LinkedHashMap<String, Locale> KEY_TO_LOCALE_MAP;

  private final static Map<Locale, String>          ISO_639_2B_EXCEPTIONS;

  static {
    ISO_639_2B_EXCEPTIONS = createIso6392BExceptions();
    KEY_TO_LOCALE_MAP = generateLanguageArray();
  }

  private static Map<Locale, String> createIso6392BExceptions() {
    Map<Locale, String> exceptions = new HashMap<>();
    exceptions.put(Locale.forLanguageTag("sq"), "alb");
    exceptions.put(Locale.forLanguageTag("hy"), "arm");
    exceptions.put(Locale.forLanguageTag("eu"), "baq");
    exceptions.put(Locale.forLanguageTag("my"), "bur");
    exceptions.put(Locale.forLanguageTag("zh"), "chi");
    exceptions.put(Locale.forLanguageTag("cs"), "cze");
    exceptions.put(Locale.forLanguageTag("nl"), "dut");
    exceptions.put(Locale.forLanguageTag("fr"), "fre");
    exceptions.put(Locale.forLanguageTag("de"), "ger");
    exceptions.put(Locale.forLanguageTag("ka"), "geo");
    exceptions.put(Locale.forLanguageTag("el"), "gre");
    exceptions.put(Locale.forLanguageTag("is"), "ice");
    exceptions.put(Locale.forLanguageTag("mk"), "mac");
    exceptions.put(Locale.forLanguageTag("mi"), "mao");
    exceptions.put(Locale.forLanguageTag("ms"), "may");
    exceptions.put(Locale.forLanguageTag("fa"), "per");
    exceptions.put(Locale.forLanguageTag("ro"), "rum");
    exceptions.put(Locale.forLanguageTag("sk"), "slo");
    exceptions.put(Locale.forLanguageTag("bo"), "tib");
    exceptions.put(Locale.forLanguageTag("cy"), "wel");

    return exceptions;
  }

  private static LinkedHashMap<String, Locale> generateLanguageArray() {
    Map<String, Locale> langArray = new HashMap<>();
    LinkedHashMap<String, Locale> sortedMap = new LinkedHashMap<>();

    Locale intl = Locale.ENGLISH;
    Locale locales[] = Locale.getAvailableLocales();
    // all possible variants of language/country/prefixes/non-iso style
    for (Locale locale : locales) {
      Locale base = new Locale(locale.getLanguage()); // from all, create only the base languages
      langArray.put(base.getDisplayLanguage(intl), base);
      langArray.put(base.getDisplayLanguage(), base);
      try {
        langArray.put(base.getDisplayLanguage(intl).substring(0, 3), base); // eg German -> Ger, where iso3=deu
      }
      catch (Exception e) {
        // ignore
      }
      // ISO-639-2/T
      langArray.put(base.getISO3Language(), base);
      // ISO-639-2/B
      langArray.put(LanguageUtils.getISO3BLanguage(base), base);

      langArray.put(base.getCountry(), base);
      try {
        String c = base.getISO3Country();
        langArray.put(c, base);
      }
      catch (MissingResourceException e) {
        // tjo... not available, see javadoc
      }
    }
    for (String l : Locale.getISOLanguages()) {
      langArray.put(l, new Locale(l));
    }

    // sort
    List<String> keys = new LinkedList<>(langArray.keySet());
    Collections.sort(keys, new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        return s2.length() - s1.length();
      }
    });

    for (String key : keys) {
      if (!key.isEmpty()) {
        sortedMap.put(key.toLowerCase(), langArray.get(key));
      }
    }

    return sortedMap;
  }

  /**
   * Get the ISO 639-2/B 3 letter code
   * 
   * @param locale
   *          the locale to get the code for
   * @return the 3 letter code
   * @since 2.0
   */
  public static String getISO3BLanguage(Locale locale) {
    if (ISO_639_2B_EXCEPTIONS.containsKey(locale)) {
      return ISO_639_2B_EXCEPTIONS.get(locale);
    }
    return locale.getISO3Language();
  }

  /**
   * Get the ISO 639-2/B 3 letter code
   * 
   * @param language
   *          the 2 letter ISO code to get the 3 letter code for
   * @return the 3 letter code
   * @since 2.0
   */
  public static String getISO3BLanguage(String language) {
    return getISO3BLanguage(Locale.forLanguageTag(language));
  }

  /**
   * uses our localized language mapping table, to get the iso3 code
   *
   * @param text
   *          the language (as string) to get the iso3 code for
   * @return 3 chars or empty string
   * @since 2.0
   */
  public static String getIso3LanguageFromLocalizedString(String text) {
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase());
    if (l != null) {
      return l.getISO3Language();
    }
    return "";
  }

  /**
   * uses our localized language mapping table, to get the iso3B code
   *
   * @param text
   *          the language (as string) to get the iso3B code for
   * @return 3 chars or empty string
   * @since 2.0
   */
  public static String getIso3BLanguageFromLocalizedString(String text) {
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase());
    if (l != null) {
      return getISO3BLanguage(l);
    }
    return "";
  }

  /**
   * uses our localized language mapping table, to get the iso2 code
   *
   * @param text
   *          the language (as string) to get the iso2 code for
   * @return 2 chars or empty string
   * @since 2.0
   */
  public static String getIso2LanguageFromLocalizedString(String text) {
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase());
    if (l != null) {
      return l.getLanguage();
    }
    return "";
  }

  /**
   * uses our localized language mapping table, to get the english language name
   *
   * @param text
   *          the language (as string) to get the language name for
   * @return the english language name or empty string
   * @since 2.0
   */
  public static String getEnglishLanguageNameFromLocalizedString(String text) {
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase());
    if (l != null) {
      return l.getDisplayLanguage(Locale.ENGLISH);
    }
    return "";
  }

  /**
   * uses our localized language mapping table, to get the localized language name
   *
   * @param text
   *          the language (as string) to get the language name for
   * @return the localized language name or empty string
   * @since 2.0
   */
  public static String getLocalizedLanguageNameFromLocalizedString(String text) {
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase());
    if (l != null) {
      return l.getDisplayLanguage();
    }
    return "";
  }
}
