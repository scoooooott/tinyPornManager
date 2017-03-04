/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * This enum represents all different types of movie editions
 *
 * @author Manuel Laggner
 */
public enum MovieEdition {
  NONE("", ""),
  DIRECTORS_CUT("Director's Cut", ".Director.?s.(Cut|Edition|Version)"),
  EXTENDED_EDITION("Extended Edition", ".Extended.(Cut|Edition|Version)?"),
  THEATRICAL_EDITION("Theatrical Edition", ".Theatrical.(Cut|Edition|Version)?"),
  UNRATED("Unrated", ".Unrated.(Cut|Edition|Version)?"),
  UNCUT("Uncut", ".Uncut.(Cut|Edition|Version)?"),
  IMAX("IMAX", "IMAX.(Cut|Edition|Version)?"),
  SPECIAL_EDITION("Special Edition", ".(Special|Remastered|Collectors|Ultimate).(Cut|Edition|Version)");

  private String  title;
  private Pattern pattern;

  MovieEdition(String title, String pattern) {
    this.title = title;
    if (StringUtils.isBlank(pattern)) {
      this.pattern = null;
    }
    else {
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
  }

  public String getTitle() {
    return title;
  }

  public String toString() {
    return title;
  }

  /**
   * Get the right movie edition for the given string
   * 
   * @param stringToParse
   *          the string to parse out the movie edition
   * @return the found edition
   */
  public static MovieEdition getMovieEditionFromString(String stringToParse) {
    MovieEdition foundEdition = NONE;

    for (MovieEdition edition : MovieEdition.values()) {
      if (edition == NONE) {
        continue;
      }

      if (edition.name().equalsIgnoreCase(stringToParse)) {
        foundEdition = edition;
        break;
      }

      if (edition.title.equalsIgnoreCase(stringToParse)) {
        foundEdition = edition;
        break;
      }

      Matcher matcher = edition.pattern.matcher(stringToParse);
      if (matcher.find()) {
        foundEdition = edition;
        break;
      }
    }

    return foundEdition;
  }
}
