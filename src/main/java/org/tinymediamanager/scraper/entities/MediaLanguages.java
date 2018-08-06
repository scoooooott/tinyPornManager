/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.scraper.entities;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.scraper.util.LanguageUtils;

/**
 * The Enum MediaLanguages. All languages we support for scraping
 * 
 * @author Manuel Laggner
 * @since 1.0
 * 
 */
public enum MediaLanguages {

  //@formatter:off
  al("Shqip"),
  ar("العَرَبِيَّة"),
  bg("български език"),
  cs("Český"),
  de("Deutsch"),
  da("Dansk"),
  el("Ελληνικά"),
  en("English"),
  es("Español"),
  et("eesti"),
  fa("فارسی"),
  fi("Suomi"),
  fr("Française"),
  he("עברית"),
  hr("hrvatski jezik"),
  hu("Magyar"),
  hy("Հայերեն"),
  it("Italiano"),
  ja("日本語"),
  ka("ქართული"),
  ko("한국어"),
  nl("Nederlands, Vlaams"),
  no("Norsk"),
  pl("Polski"),
  pt("Português"),
  pt_BR("Português (Brasil)"),
  ro("Română"),
  ru("Русский"),
  sl("Slovenčina"),
  sk("Slovenščina"),
  sr("српски језик"),
  sv("Svenska"),
  tr("Türkçe"),
  uk("Українська"),
  zh("华语");
  //@formatter:on

  private String                                   title;
  private String                                   displayTitle;

  private static final Map<String, MediaLanguages> lookup = prepareLookup();

  private static Map<String, MediaLanguages> prepareLookup() {
    Map<String, MediaLanguages> mlMap = new HashMap<>();
    for (MediaLanguages lang : MediaLanguages.values()) {
      mlMap.put(lang.getTitle(), lang);
      mlMap.put(lang.name(), lang);
    }
    return mlMap;
  }

  /**
   * Get MediaLanguage by Title
   *
   * @param title
   *          the title/name of the language
   * @return the MediaLanguages Enum Object.
   */
  public static MediaLanguages get(String title) {
    MediaLanguages entry = lookup.get(title);

    // if the entry is null (maybe localized name) try to load it via our language helper
    if (entry == null) {
      entry = lookup.get(LanguageUtils.getIso2LanguageFromLocalizedString(title));
    }

    // if the entry is still null (should not occur), take EN
    if (entry == null) {
      entry = MediaLanguages.en;
    }

    return entry;
  }

  MediaLanguages(String title) {
    this.title = title;
    Locale locale = Locale.forLanguageTag(name());
    if (locale != null && StringUtils.isNotBlank(locale.getDisplayLanguage()) && !name().equals(locale.getDisplayLanguage())) {
      this.displayTitle = locale.getDisplayLanguage();
    }
    else {
      this.displayTitle = title;
    }
  }

  /**
   * return the title
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * return the first 2 letters which is the language part
   * 
   * @return the language
   */
  public String getLanguage() {
    return name().substring(0, 2);
  }

  @Override
  public String toString() {
    return displayTitle;
  }
}
