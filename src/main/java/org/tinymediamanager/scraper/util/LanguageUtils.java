/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This is a helper class for language related tasks
 *
 * @author Manuel Laggner
 * @since 2.0
 */
public class LanguageUtils {
  // Map of all known English/UserLocalized String to base locale, key is LOWERCASE
  public final static LinkedHashMap<String, Locale> KEY_TO_LOCALE_MAP;
  public final static LinkedHashMap<String, Locale> KEY_TO_COUNTRY_LOCALE_MAP;

  private final static Map<Locale, String>          ISO_639_2B_EXCEPTIONS;

  private static final Pattern                      localePattern    = Pattern.compile("messages_(.{2})_?(.{2}){0,1}\\.properties",
      Pattern.CASE_INSENSITIVE);
  private static List<Locale>                       availableLocales = new ArrayList<>();

  static {
    ISO_639_2B_EXCEPTIONS = createIso6392BExceptions();
    KEY_TO_LOCALE_MAP = generateLanguageArray();
    KEY_TO_COUNTRY_LOCALE_MAP = generateCountryArray();
  }

  private LanguageUtils() {
    // hide the public constructor for utility classes
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

    // all possible variants of language/prefixes/non-iso style
    for (String langu : Locale.getISOLanguages()) {
      Locale base = new Locale(langu); // from all, create only the base languages

      // first put the name in the default locale
      langArray.putIfAbsent(base.getDisplayLanguage(), base);
      // second in english
      langArray.putIfAbsent(base.getDisplayLanguage(intl), base);
      try {
        langArray.putIfAbsent(base.getDisplayLanguage(intl).substring(0, 3), base); // eg German -> Ger, where iso3=deu
      }
      catch (Exception ignore) {
        // nothing to be done here
      }

      // and afterwards in all other languages
      for (String displayLangu : Locale.getISOLanguages()) {
        try {
          String alternativeLanguage = base.getDisplayLanguage(new Locale(displayLangu));
          if (!alternativeLanguage.isEmpty()) {
            langArray.putIfAbsent(alternativeLanguage, base);
          }
        }
        catch (Exception ignored) {
          // nothing to be done here
        }
      }

      // ISO-639-2/T
      langArray.putIfAbsent(base.getISO3Language(), base);
      // ISO-639-2/B
      langArray.putIfAbsent(LanguageUtils.getISO3BLanguage(base), base);
      // ISO 639-1
      langArray.putIfAbsent(langu, base);
    }

    // also sort in all languge tags from available locales
    for (Locale locale : Locale.getAvailableLocales()) {
      Locale base = new Locale(locale.getLanguage());
      langArray.putIfAbsent(locale.toLanguageTag(), base);
    }

    // sort from long to short
    List<String> keys = new LinkedList<>(langArray.keySet());
    Collections.sort(keys, (s1, s2) -> s2.length() - s1.length());

    // all lowercase (!)
    for (String key : keys) {
      if (!key.isEmpty()) {
        sortedMap.put(key.toLowerCase(Locale.ROOT), langArray.get(key));
      }
    }

    return sortedMap;
  }

  private static LinkedHashMap<String, Locale> generateCountryArray() {
    Map<String, Locale> langArray = new HashMap<>();
    LinkedHashMap<String, Locale> sortedMap = new LinkedHashMap<>();
    Locale intl = Locale.ENGLISH;

    for (String cc : Locale.getISOCountries()) {
      Locale l = new Locale("", cc);
      langArray.put(l.getDisplayCountry(), l); // localized name
      langArray.put(l.getDisplayCountry(intl), l); // english name

      // and afterwards in all other languages
      for (String displayLangu : Locale.getISOLanguages()) {
        try {
          String alternativeLanguage = l.getDisplayCountry(new Locale(displayLangu));
          if (!alternativeLanguage.isEmpty()) {
            langArray.putIfAbsent(alternativeLanguage, l);
          }
        }
        catch (Exception ignored) {
          // nothing to be done here
        }
      }

      langArray.put(l.getCountry().toLowerCase(Locale.ROOT), l); // country code 2 char - lowercase to overwrite possible language key (!)
      langArray.put(l.getISO3Country().toLowerCase(Locale.ROOT), l); // country code 3 char - lowercase to overwrite possible language key (!)
    }

    // sort from long to short
    List<String> keys = new LinkedList<>(langArray.keySet());
    Collections.sort(keys, (s1, s2) -> s2.length() - s1.length());

    // all lowercase (!)
    for (String key : keys) {
      if (!key.isEmpty()) {
        sortedMap.put(key.toLowerCase(Locale.ROOT), langArray.get(key));
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
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase(Locale.ROOT));
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
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase(Locale.ROOT));
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
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase(Locale.ROOT));
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
    Locale l = KEY_TO_LOCALE_MAP.get(text.toLowerCase(Locale.ROOT));
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
   * @return the localized language name or the untranslated string 1:1
   * @since 2.0
   */
  public static String getLocalizedLanguageNameFromLocalizedString(String text) {
    return getLocalizedLanguageNameFromLocalizedString(Locale.getDefault(), text);
  }

  /**
   * uses our localized language mapping table, to get the localized language name in given language
   *
   * @param language
   *          the locale to which we translate the language (as string) to get the language name for
   * @param text
   *          the language (as string) to get the language name for
   * @return the localized language name or empty string
   * @since 2.0
   */
  public static String getLocalizedLanguageNameFromLocalizedString(Locale language, String... text) {
    String ret = "";
    if (language == null) {
      language = Locale.getDefault();
    }
    for (String s : text) {
      Locale l = KEY_TO_LOCALE_MAP.get(s.toLowerCase(Locale.ROOT));
      if (l != null) {
        ret = l.getDisplayLanguage(language); // auto fallback to english
        if (!ret.isEmpty()) {
          break;
        }
      }
    }
    if (ret.isEmpty() && text.length > 0) {
      ret = text[0]; // cannot translate - just take first param 1:1
    }
    return ret;
  }

  /**
   * tries to get local (JVM language) COUNTRY name for given parameters/variants
   * 
   * @param countries
   *          all possible names or iso codes
   * @return localized country name, or first country param 1:1 if we cannot translate
   */
  public static String getLocalizedCountry(String... countries) {
    return getLocalizedCountryForLanguage(Locale.getDefault().getLanguage(), countries);
  }

  /**
   * tries to get localized COUNTRY name (in given language) for given parameters/variants
   * 
   * @param countries
   *          all possible names or iso codes
   * @return localized country name, or first country param 1:1 if we cannot translate
   */
  public static String getLocalizedCountryForLanguage(String language, String... countries) {
    // KEY_TO_LOCALE_MAP is correct here, we want to get the language locale!!!
    return getLocalizedCountryForLanguage(KEY_TO_LOCALE_MAP.get(language.toLowerCase(Locale.ROOT)), countries);
  }

  /**
   * tries to get localized COUNTRY name (in given language) for given parameters/variants
   * 
   * @param countries
   *          all possible names or iso codes
   * @return localized country name, or first country param 1:1 if we cannot translate
   */
  public static String getLocalizedCountryForLanguage(Locale language, String... countries) {
    String ret = "";
    if (language == null) {
      language = Locale.getDefault();
    }
    for (String c : countries) {
      Locale l = KEY_TO_COUNTRY_LOCALE_MAP.get(c.toLowerCase(Locale.ROOT));
      if (l != null) {
        ret = l.getDisplayCountry(language); // auto fallback to english
        if (!ret.isEmpty()) {
          break;
        }
      }
    }
    if (ret.isEmpty() && countries.length > 0) {
      ret = countries[0]; // cannot translate - just take first param 1:1
    }
    return ret;
  }

  /**
   * checks whether the given string matches or ends with the given language
   * 
   * @param string
   *          the string to check
   * @param language
   *          the language to check
   * @return true/false
   */
  public static boolean doesStringEndWithLanguage(String string, String language) {
    return string.equalsIgnoreCase(language) || string.matches("(?i).*[ _.-]+" + Pattern.quote(language) + "$");
  }

  /**
   * returns a list of all available GUI languages
   * 
   * @return List of Locales
   */
  public static List<Locale> getLanguages() {
    if (!availableLocales.isEmpty()) {
      // do not return the original list to avoid external manipulation
      return new ArrayList<>(availableLocales);
    }

    availableLocales.add(getLocaleFromLanguage(Locale.ENGLISH.getLanguage()));
    try {
      // list all properties files from the classpath
      InputStream is = LanguageUtils.class.getResourceAsStream("/");
      if (is != null) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String resource;
        while ((resource = br.readLine()) != null) {
          parseLocaleFromFilename(resource);
        }
      }
      else {
        // we may be in a .jar file
        CodeSource src = LanguageUtils.class.getProtectionDomain().getCodeSource();
        if (src != null) {
          URL jar = src.getLocation();
          try (InputStream jarInputStream = jar.openStream(); ZipInputStream zip = new ZipInputStream(jarInputStream)) {
            while (true) {
              ZipEntry e = zip.getNextEntry();
              if (e == null) {
                break;
              }
              parseLocaleFromFilename(e.getName());
            }
          }
        }
      }
    }
    catch (Exception ignore) {
      // ignore
    }

    // do not return the original list to avoid external manipulation
    return new ArrayList<>(availableLocales);
  }

  private static void parseLocaleFromFilename(String filename) {
    Matcher matcher = localePattern.matcher(filename);
    if (matcher.matches()) {
      Locale myloc;

      String language = matcher.group(1);
      String country = matcher.group(2);

      if (country != null) {
        // found language & country
        myloc = new Locale(language, country);
      }
      else {
        // found only language
        myloc = getLocaleFromLanguage(language);
      }
      if (myloc != null && !availableLocales.contains(myloc)) {
        availableLocales.add(myloc);
      }
    }
  }

  /**
   * Gets a correct Locale (language + country) from given language.
   * 
   * @param language
   *          as 2char
   * @return Locale
   */
  public static Locale getLocaleFromLanguage(String language) {
    if (StringUtils.isBlank(language)) {
      return Locale.getDefault();
    }
    // do we have a newer locale settings style?
    if (language.length() > 2) {
      try {
        return LocaleUtils.toLocale(language);
      }
      catch (Exception e) {
        // Whoopsie. try to fix string....
        if (language.matches("^\\w\\w_\\w\\w.*")) {
          return LocaleUtils.toLocale(language.substring(0, 5));
        }
      }
    }
    if (language.equalsIgnoreCase("en")) {
      return new Locale("en", "US"); // don't mess around; at least fixtate this
    }
    Locale l = null;
    List<Locale> countries = LocaleUtils.countriesByLanguage(language.toLowerCase(Locale.ROOT));
    for (Locale locale : countries) {
      if (locale.getCountry().equalsIgnoreCase(language)) {
        // map to main countries; de->de_DE (and not de_CH)
        l = locale;
      }
    }
    if (l == null && !countries.isEmpty()) {
      // well, take the first one
      l = countries.get(0);
    }

    return l;
  }

}
