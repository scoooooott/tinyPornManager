/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MediaGenres2.
 * 
 * @author Manuel Laggner
 */
public class MediaGenres extends DynaEnum<MediaGenres> {

  public final static MediaGenres ACTION          = new MediaGenres("ACTION", 0, "Action");
  public final static MediaGenres ADVENTURE       = new MediaGenres("ADVENTURE", 1, "Adventure");
  public final static MediaGenres ANIMATION       = new MediaGenres("ANIMATION", 2, "Animation");
  public final static MediaGenres ANIME           = new MediaGenres("ANIME", -1, "Anime");
  public final static MediaGenres ANIMAL          = new MediaGenres("ANIMAL", 3, "Animal");
  public final static MediaGenres BIOGRAPHY       = new MediaGenres("BIOGRAPHY", 4, "Biography");
  public final static MediaGenres COMEDY          = new MediaGenres("COMEDY", 5, "Comedy");
  public final static MediaGenres CRIME           = new MediaGenres("CRIME", 6, "Crime");
  public final static MediaGenres DISASTER        = new MediaGenres("DISASTER", 7, "Disaster");
  public final static MediaGenres DOCUMENTARY     = new MediaGenres("DOCUMENTARY", 8, "Documentary");
  public final static MediaGenres DRAMA           = new MediaGenres("DRAMA", 9, "Drama");
  public final static MediaGenres EASTERN         = new MediaGenres("EASTERN", 10, "Eastern");
  public final static MediaGenres EROTIC          = new MediaGenres("EROTIC", 11, "Erotic");
  public final static MediaGenres FAMILY          = new MediaGenres("FAMILY", 12, "Family");
  public final static MediaGenres FAN_FILM        = new MediaGenres("FAN_FILM", 13, "Fan Film");
  public final static MediaGenres FANTASY         = new MediaGenres("FANTASY", 14, "Fantasy");
  public final static MediaGenres FILM_NOIR       = new MediaGenres("FILM_NOIR", 15, "Film Noir");
  public final static MediaGenres FOREIGN         = new MediaGenres("FOREIGN", 16, "Foreign");
  public final static MediaGenres GAME_SHOW       = new MediaGenres("GAME_SHOW", 17, "Gameshow");
  public final static MediaGenres HISTORY         = new MediaGenres("HISTORY", 18, "History");
  public final static MediaGenres HOLIDAY         = new MediaGenres("HOLIDAY", 19, "Holiday");
  public final static MediaGenres HORROR          = new MediaGenres("HORROR", 20, "Horror");
  public final static MediaGenres INDIE           = new MediaGenres("INDIE", 21, "Indie");
  public final static MediaGenres MUSIC           = new MediaGenres("MUSIC", 22, "Music");
  public final static MediaGenres MUSICAL         = new MediaGenres("MUSICAL", 23, "Musical");
  public final static MediaGenres MYSTERY         = new MediaGenres("MYSTERY", 24, "Mystery");
  public final static MediaGenres NEO_NOIR        = new MediaGenres("NEO_NOIR", 25, "Neo Noir");
  public final static MediaGenres NEWS            = new MediaGenres("NEWS", 26, "News");
  public final static MediaGenres REALITY_TV      = new MediaGenres("REALITY_TV", 27, "Reality TV");
  public final static MediaGenres ROAD_MOVIE      = new MediaGenres("ROAD_MOVIE", 28, "Road Movie");
  public final static MediaGenres ROMANCE         = new MediaGenres("ROMANCE", 29, "Romance");
  public final static MediaGenres SCIENCE_FICTION = new MediaGenres("SCIENCE_FICTION", 30, "Science Fiction");
  public final static MediaGenres SERIES          = new MediaGenres("SERIES", 31, "Series");
  public final static MediaGenres SHORT           = new MediaGenres("SHORT", 32, "Short");
  public final static MediaGenres SILENT_MOVIE    = new MediaGenres("SILENT_MOVIE", 33, "Silent Movie");
  public final static MediaGenres SPORT           = new MediaGenres("SPORT", 34, "Sport");
  public final static MediaGenres SPORTING_EVENT  = new MediaGenres("SPORTING_EVENT", 35, "Sporting Event");
  public final static MediaGenres SPORTS_FILM     = new MediaGenres("SPORTS_FILM", 36, "Sports Film");
  public final static MediaGenres SUSPENSE        = new MediaGenres("SUSPENSE", 37, "Suspense");
  public final static MediaGenres TALK_SHOW       = new MediaGenres("TALK_SHOW", 38, "Talk show");
  public final static MediaGenres TV_MOVIE        = new MediaGenres("TV_MOVIE", 39, "TV Movie");
  public final static MediaGenres THRILLER        = new MediaGenres("THRILLER", 40, "Thriller");
  public final static MediaGenres WAR             = new MediaGenres("WAR", 41, "War");
  public final static MediaGenres WESTERN         = new MediaGenres("WESTERN", 42, "Western");

  /** The name. */
  private String                  name;

  /** The alternate names. */
  private String[]                alternateNames;

  /**
   * Instantiates a new genres.
   * 
   * @param enumName
   *          the enum name
   * @param ordinal
   *          the ordinal
   * @param name
   *          the name
   * @param alternateNames
   *          the alternate names
   */
  private MediaGenres(String enumName, int ordinal, String name) {
    super(enumName, ordinal);
    this.name = name;
    // System.out.println(enumName + " - " + name);
    this.alternateNames = loadAlternateNames(enumName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.DynaEnum#toString()
   */
  public String toString() {
    return this.getLocalizedName();
  }

  public String dump() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * Iterates ofer all found languages ang gets the "alternative name" of specified property
   * 
   * @param propName
   *          the property
   * @return array of alternate names
   */
  public static String[] loadAlternateNames(String propName) {
    ArrayList<String> alt = new ArrayList<String>();
    for (Locale loc : Utils.getLanguages()) {
      if (loc == null || loc.getLanguage().equals("en")) {
        // English not needed, since it's in default properties
        // and for invalid languages (like NB) it will be null
        continue;
      }
      ResourceBundle b = ResourceBundle.getBundle("messages", loc, new UTF8Control()); //$NON-NLS-1$
      try {
        // System.out.println(" " + loc.getLanguage() + "-" + b.getString("Genres." + propName));
        alt.add(loc.getLanguage() + "-" + b.getString("Genres." + propName)); // just genres
      }
      catch (Exception e) {
        // not found or localized - ignore
      }
    }
    return alt.toArray(new String[alt.size()]);
  }

  /**
   * All the localized MediaGenres values, alphabetically sorted.
   * 
   * @return the media genres2[]
   */
  public static MediaGenres[] values() {
    Comparator<MediaGenres> comp = new MediaGenres.MediaGenresComparator();
    MediaGenres[] mg = values(MediaGenres.class);
    Arrays.sort(mg, comp);
    return mg;
  }

  /**
   * Gets the genre.
   * 
   * @param name
   *          the name
   * @return the genre
   */
  public static MediaGenres getGenre(String name) {
    for (MediaGenres genre : values()) {
      // check if the "enum" name matches
      if (genre.name().equals(name)) {
        return genre;
      }
      // check if the printable name matches
      if (genre.name.equalsIgnoreCase(name)) {
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
    String lang = Locale.getDefault().getLanguage() + "-";
    for (String notation : this.alternateNames) {
      if (notation.startsWith(lang)) {
        return notation.substring(3);
      }
    }
    return name;
  }

  /**
   * Comparator for sorting our MediaGenres in a localized fashion
   * 
   * @author Myron
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

}
