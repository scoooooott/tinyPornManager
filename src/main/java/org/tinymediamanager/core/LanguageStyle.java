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
package org.tinymediamanager.core;

import java.util.ResourceBundle;

import org.tinymediamanager.scraper.util.LanguageUtils;

/**
 * The enum LanguageStyle is used for the different language notations
 * 
 * @author Manuel Laggner
 */
public enum LanguageStyle {
  ISO2,
  ISO3T,
  ISO3B,
  LANG_EN,
  LANG_LOCALIZED;

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  @Override
  public String toString() {
    try {
      return BUNDLE.getString("Settings.renamer.language." + name());
    }
    catch (Exception e) {
      return name();
    }
  }

  /**
   * Get the appropriate language code for the given language and the chosen style
   * 
   * @param language
   *          the language to get code for
   * @param style
   *          the style
   * @return the language code
   */
  public static String getLanguageCodeForStyle(String language, LanguageStyle style) {
    switch (style) {
      case ISO2:
        return LanguageUtils.getIso2LanguageFromLocalizedString(language);

      case ISO3T:
        return LanguageUtils.getIso3LanguageFromLocalizedString(language);

      case ISO3B:
        return LanguageUtils.getIso3BLanguageFromLocalizedString(language);

      case LANG_EN:
        return LanguageUtils.getEnglishLanguageNameFromLocalizedString(language);

      case LANG_LOCALIZED:
        return LanguageUtils.getLocalizedLanguageNameFromLocalizedString(language);

      default:
        return language;
    }
  }
}
