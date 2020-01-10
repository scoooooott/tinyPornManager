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
package org.tinymediamanager.core.movie;

import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.scraper.DynaEnum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This enum represents all different types of movie editions
 *
 * @author Manuel Laggner
 */
public class MovieEdition extends DynaEnum<MovieEdition> {
  private static final Comparator<MovieEdition> COMPARATOR         = new MovieEditionComparator();

  public static final MovieEdition              NONE               = new MovieEdition("NONE", 0, "", "");
  public static final MovieEdition              DIRECTORS_CUT      = new MovieEdition("DIRECTORS_CUT", 1, "Director's Cut",
      ".Director.?s.(Cut|Edition|Version)");
  public static final MovieEdition              EXTENDED_EDITION   = new MovieEdition("EXTENDED_EDITION", 2, "Extended Edition",
      ".Extended.(Cut|Edition|Version)?");
  public static final MovieEdition              THEATRICAL_EDITION = new MovieEdition("THEATRICAL_EDITION", 3, "Theatrical Edition",
      ".Theatrical.(Cut|Edition|Version)?");
  public static final MovieEdition              UNRATED            = new MovieEdition("UNRATED", 4, "Unrated", ".Unrated.(Cut|Edition|Version)?");
  public static final MovieEdition              UNCUT              = new MovieEdition("UNCUT", 5, "Uncut", ".Uncut.(Cut|Edition|Version)?");
  public static final MovieEdition              IMAX               = new MovieEdition("IMAX", 6, "IMAX", "^(IMAX|.*?.IMAX).(Cut|Edition|Version)?");
  public static final MovieEdition              SPECIAL_EDITION    = new MovieEdition("SPECIAL_EDITION", 7, "Special Edition",
      ".(Special|Remastered|Collectors|Ultimate|Final).(Cut|Edition|Version)");

  private String                                title;
  private Pattern                               pattern;

  private MovieEdition(String enumName, int ordinal, String title, String pattern) {
    super(enumName, ordinal);
    this.title = title;
    if (StringUtils.isBlank(pattern)) {
      this.pattern = null;
    }
    else {
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    addElement();
  }

  @Override
  public String toString() {
    return title;
  }

  public String getTitle() {
    return title;
  }

  @JsonValue
  public String getName() {
    return name();
  }

  /**
   * get all movie editions
   *
   * @return an array of all movie editions
   */
  public static MovieEdition[] values() {
    MovieEdition[] movieEditions = values(MovieEdition.class);
    Arrays.sort(movieEditions, COMPARATOR);
    return movieEditions;
  }

  /**
   * Parse the given string for an appropriate movie edition (via name & regexp)
   *
   * @param stringToParse
   *          the string to parse out the movie edition
   * @return the found edition
   */
  public static MovieEdition getMovieEditionFromString(String stringToParse) {
    for (MovieEdition edition : MovieEdition.values()) {
      if (edition.name().equalsIgnoreCase(stringToParse)) {
        return edition;
      }

      if (edition.pattern != null) {
        Matcher matcher = edition.pattern.matcher(stringToParse);
        if (matcher.find()) {
          return edition;
        }
      }
    }

    return NONE;
  }

  /**
   * Gets the right movie edition for the given string.
   *
   * @param name
   *          the name
   * @return the movie edition
   */
  @JsonCreator
  public static MovieEdition getMovieEdition(String name) {
    for (MovieEdition edition : values()) {
      // check if the "enum" name matches
      if (edition.name().equals(name)) {
        return edition;
      }
      // check if the printable name matches
      if (edition.title.equalsIgnoreCase(name)) {
        return edition;
      }
    }

    // dynamically create new one
    return new MovieEdition(name, values().length, name, "");
  }

  /**
   * Comparator for sorting our MovieEditions in a localized fashion
   */
  public static class MovieEditionComparator implements Comparator<MovieEdition> {
    private RuleBasedCollator stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

    @Override
    public int compare(MovieEdition o1, MovieEdition o2) {
      // toString is localized name
      if (o1.toString() == null && o2.toString() == null) {
        return 0;
      }
      if (o1.toString() == null) {
        return 1;
      }
      if (o2.toString() == null) {
        return -1;
      }
      return stringCollator.compare(o1.toString().toLowerCase(Locale.ROOT), o2.toString().toLowerCase(Locale.ROOT));
    }
  }

  /**
   * add a new DynaEnumEventListener. This listener will be informed if any new value has been added
   *
   * @param listener
   *          the new listener to be added
   */
  public static void addListener(DynaEnumEventListener listener) {
    addListener(MovieEdition.class, listener);
  }

  /**
   * remove the given DynaEnumEventListener
   *
   * @param listener
   *          the listener to be removed
   */
  public static void removeListener(DynaEnumEventListener listener) {
    removeListener(MovieEdition.class, listener);
  }
}
