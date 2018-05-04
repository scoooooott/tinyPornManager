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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.ApiResourceBundle;
import org.tinymediamanager.scraper.DynaEnum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The class/dynaenum MediaGenres. This class stores all default known genres along with some different parsing informations
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class MediaGenres extends DynaEnum<MediaGenres> {
  private final static Comparator<MediaGenres> COMPARATOR      = new MediaGenres.MediaGenresComparator();

  public final static MediaGenres              ACTION          = new MediaGenres("ACTION", 0, "Action");
  public final static MediaGenres              ADVENTURE       = new MediaGenres("ADVENTURE", 1, "Adventure");
  public final static MediaGenres              ANIMATION       = new MediaGenres("ANIMATION", 2, "Animation");
  public final static MediaGenres              ANIME           = new MediaGenres("ANIME", -1, "Anime");
  public final static MediaGenres              ANIMAL          = new MediaGenres("ANIMAL", 3, "Animal");
  public final static MediaGenres              BIOGRAPHY       = new MediaGenres("BIOGRAPHY", 4, "Biography");
  public final static MediaGenres              COMEDY          = new MediaGenres("COMEDY", 5, "Comedy");
  public final static MediaGenres              CRIME           = new MediaGenres("CRIME", 6, "Crime");
  public final static MediaGenres              DISASTER        = new MediaGenres("DISASTER", 7, "Disaster");
  public final static MediaGenres              DOCUMENTARY     = new MediaGenres("DOCUMENTARY", 8, "Documentary",
      new String[] { "Home and Garden", "Food" });
  public final static MediaGenres              DRAMA           = new MediaGenres("DRAMA", 9, "Drama");
  public final static MediaGenres              EASTERN         = new MediaGenres("EASTERN", 10, "Eastern");
  public final static MediaGenres              EROTIC          = new MediaGenres("EROTIC", 11, "Erotic");
  public final static MediaGenres              FAMILY          = new MediaGenres("FAMILY", 12, "Family", new String[] { "Children" });
  public final static MediaGenres              FAN_FILM        = new MediaGenres("FAN_FILM", 13, "Fan Film");
  public final static MediaGenres              FANTASY         = new MediaGenres("FANTASY", 14, "Fantasy");
  public final static MediaGenres              FILM_NOIR       = new MediaGenres("FILM_NOIR", 15, "Film Noir");
  public final static MediaGenres              FOREIGN         = new MediaGenres("FOREIGN", 16, "Foreign");
  public final static MediaGenres              GAME_SHOW       = new MediaGenres("GAME_SHOW", 17, "Gameshow", new String[] { "Game Show" });
  public final static MediaGenres              HISTORY         = new MediaGenres("HISTORY", 18, "History");
  public final static MediaGenres              HOLIDAY         = new MediaGenres("HOLIDAY", 19, "Holiday", new String[] { "Travel" });
  public final static MediaGenres              HORROR          = new MediaGenres("HORROR", 20, "Horror");
  public final static MediaGenres              INDIE           = new MediaGenres("INDIE", 21, "Indie", new String[] { "Special Interest" });
  public final static MediaGenres              MUSIC           = new MediaGenres("MUSIC", 22, "Music");
  public final static MediaGenres              MUSICAL         = new MediaGenres("MUSICAL", 23, "Musical");
  public final static MediaGenres              MYSTERY         = new MediaGenres("MYSTERY", 24, "Mystery");
  public final static MediaGenres              NEO_NOIR        = new MediaGenres("NEO_NOIR", 25, "Neo Noir");
  public final static MediaGenres              NEWS            = new MediaGenres("NEWS", 26, "News");
  public final static MediaGenres              REALITY_TV      = new MediaGenres("REALITY_TV", 27, "Reality TV", new String[] { "Reality" });
  public final static MediaGenres              ROAD_MOVIE      = new MediaGenres("ROAD_MOVIE", 28, "Road Movie");
  public final static MediaGenres              ROMANCE         = new MediaGenres("ROMANCE", 29, "Romance");
  public final static MediaGenres              SCIENCE_FICTION = new MediaGenres("SCIENCE_FICTION", 30, "Science Fiction",
      new String[] { "Sci-Fi", "Science-Fiction" });
  public final static MediaGenres              SERIES          = new MediaGenres("SERIES", 31, "Series", new String[] { "Soap", "Mini-Series" });
  public final static MediaGenres              SHORT           = new MediaGenres("SHORT", 32, "Short");
  public final static MediaGenres              SILENT_MOVIE    = new MediaGenres("SILENT_MOVIE", 33, "Silent Movie");
  public final static MediaGenres              SPORT           = new MediaGenres("SPORT", 34, "Sport");
  public final static MediaGenres              SPORTING_EVENT  = new MediaGenres("SPORTING_EVENT", 35, "Sporting Event");
  public final static MediaGenres              SPORTS_FILM     = new MediaGenres("SPORTS_FILM", 36, "Sports Film");
  public final static MediaGenres              SUSPENSE        = new MediaGenres("SUSPENSE", 37, "Suspense");
  public final static MediaGenres              TALK_SHOW       = new MediaGenres("TALK_SHOW", 38, "Talk show");
  public final static MediaGenres              TV_MOVIE        = new MediaGenres("TV_MOVIE", 39, "TV Movie");
  public final static MediaGenres              THRILLER        = new MediaGenres("THRILLER", 40, "Thriller");
  public final static MediaGenres              WAR             = new MediaGenres("WAR", 41, "War");
  public final static MediaGenres              WESTERN         = new MediaGenres("WESTERN", 42, "Western");

  private String                               name;
  private String[]                             alternateNames;

  /**
   * Instantiates a new genres.
   * 
   * @param enumName
   *          the enum name
   * @param ordinal
   *          the ordinal
   * @param name
   *          the name
   */
  private MediaGenres(String enumName, int ordinal, String name) {
    super(enumName, ordinal);
    this.name = name;
    this.alternateNames = loadAlternateNames(enumName);

    addElement();
  }

  /**
   * Instantiates a new genres.
   *
   * @param enumName
   *          the enum name
   * @param ordinal
   *          the ordinal
   * @param name
   *          the name
   * @param alternates
   *          extra alternate names
   */
  private MediaGenres(String enumName, int ordinal, String name, String[] alternates) {
    super(enumName, ordinal);
    this.name = name;
    this.alternateNames = ArrayUtils.addAll(loadAlternateNames(enumName), alternates);

    addElement();
  }

  @Override
  public String toString() {
    return getLocalizedName();
  }

  public String dump() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * get the (english) name of this genre
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  @JsonValue
  public String getEnumName() {
    return name();
  }

  /**
   * Iterates over all found languages and gets the "alternative name" of specified property
   * 
   * @param propName
   *          the property
   * @return array of alternate names
   */
  public static String[] loadAlternateNames(String propName) {
    ArrayList<String> alt = new ArrayList<>();
    for (Locale loc : getLanguages()) {
      if (loc == null || loc.getLanguage().equals("en")) {
        // English not needed, since it's in default properties
        // and for invalid languages (like NB) it will be null
        continue;
      }
      ResourceBundle b = ApiResourceBundle.getResourceBundle(loc);
      try {
        alt.add(loc.getLanguage() + "-" + b.getString("Genres." + propName)); // just genres
      }
      catch (Exception e) {
        // not found or localized - ignore
      }
    }
    return alt.toArray(new String[alt.size()]);
  }

  /**
   * get all available Languages. Here we use reflection to get rid of the dependency to the tmm core. If org.tinymediamanager.core.Utils is not in
   * our classpath, we only use en as available language
   * 
   * @return all available languages
   */
  @SuppressWarnings("unchecked")
  private static List<Locale> getLanguages() {
    try {
      Class<?> clazz = Class.forName("org.tinymediamanager.core.Utils");
      Object obj = clazz.newInstance();

      Method method = clazz.getDeclaredMethod("getLanguages");
      if (method.getReturnType() == List.class) {
        return (List<Locale>) method.invoke(obj);
      }
    }
    catch (Exception ignored) {
    }
    return Arrays.asList(new Locale("en", "US"));
  }

  /**
   * All the localized MediaGenres values, alphabetically sorted.
   * 
   * @return the media genres2[]
   */
  public static MediaGenres[] values() {
    MediaGenres[] mg = values(MediaGenres.class);
    Arrays.sort(mg, COMPARATOR);
    return mg;
  }

  /**
   * Gets the genre.
   * 
   * @param name
   *          the name
   * @return the genre
   */
  @JsonCreator
  public synchronized static MediaGenres getGenre(String name) {
    String cleanedName = name.replaceAll("[._-]", " ");
    for (MediaGenres genre : values()) {
      // check if the "enum" name matches
      if (genre.name().equals(name)) {
        return genre;
      }
      // check if the printable name matches
      if (genre.name.equalsIgnoreCase(name)) {
        return genre;
      }
      if (genre.name.equalsIgnoreCase(cleanedName)) {
        return genre;
      }
      // check if one of the possible names matches
      for (String notation : genre.alternateNames) {
        if (notation.equalsIgnoreCase(name)) {
          return genre;
        }
        if (notation.length() > 3) {
          // first 3 chars are language like "de-"
          if (notation.substring(3).equalsIgnoreCase(name)) {
            return genre;
          }

          if (name.length() > 3) {
            // match both names without prefix
            if (notation.substring(3).equalsIgnoreCase(name.substring(3))) {
              return genre;
            }
          }
        }
      }
    }

    // dynamically create new one
    return new MediaGenres(name, values().length, name);
  }

  /**
   * Gets the genre name with default Locale<br>
   * or just name if not found<br>
   * eg: Name = "de-Abenteuer"
   * 
   * @return the localized genre
   */
  public String getLocalizedName() {
    return getLocalizedName(Locale.getDefault());
  }

  /**
   * Gets the genre name with given Locale<br>
   * or just name if not found<br>
   * eg: Name = "de-Abenteuer"
   *
   * @return the localized genre
   */
  public String getLocalizedName(Locale locale) {
    String lang = locale.getLanguage() + "-";
    for (String notation : alternateNames) {
      if (notation.startsWith(lang)) {
        return notation.substring(3);
      }
    }
    return name;
  }

  /**
   * Comparator for sorting our MediaGenres in a localized fashion
   */
  public static class MediaGenresComparator implements Comparator<MediaGenres> {
    @Override
    public int compare(MediaGenres o1, MediaGenres o2) {
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
      return o1.toString().compareTo(o2.toString());
    }
  }

  /**
   * add a new DynaEnumEventListener. This listener will be informed if any new value has been added
   *
   * @param listener
   *          the new listener to be added
   */
  public static void addListener(DynaEnumEventListener listener) {
    addListener(MediaGenres.class, listener);
  }

  /**
   * remove the given DynaEnumEventListener
   *
   * @param listener
   *          the listener to be removed
   */
  public static void removeListener(DynaEnumEventListener listener) {
    removeListener(MediaGenres.class, listener);
  }
}
