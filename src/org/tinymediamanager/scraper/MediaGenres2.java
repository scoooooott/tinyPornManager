/*
 * Copyright 2012-2013 Manuel Laggner
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

/**
 * The Class MediaGenres2.
 * 
 * @author manuel
 */
public class MediaGenres2 extends DynaEnum<MediaGenres2> {
  /** The action. */
  public final static MediaGenres2 ACTION          = new MediaGenres2("ACTION", 0, "Action", new String[] {});
  /** The adventure. */
  public final static MediaGenres2 ADVENTURE       = new MediaGenres2("ADVENTURE", 1, "Adventure", new String[] { "Abenteuer" });
  /** The animation. */
  public final static MediaGenres2 ANIMATION       = new MediaGenres2("ANIMATION", 2, "Animation", new String[] { "Anime", "Zeichentrick" });
  /** The animal. */
  public final static MediaGenres2 ANIMAL          = new MediaGenres2("ANIMAL", 3, "Animal", new String[] { "Tierfilm" });
  /** The Biography. */
  public final static MediaGenres2 BIOGRAPHY       = new MediaGenres2("BIOGRAPHY", 4, "Biography", new String[] { "Biographie" });
  /** The comedy. */
  public final static MediaGenres2 COMEDY          = new MediaGenres2("COMEDY", 5, "Comedy", new String[] { "Komödie" });
  /** The crime. */
  public final static MediaGenres2 CRIME           = new MediaGenres2("CRIME", 6, "Crime", new String[] { "Krimi" });
  /** The disaster. */
  public final static MediaGenres2 DISASTER        = new MediaGenres2("DISASTER", 7, "Disaster", new String[] { "Katastrophen", "Katastrophenfilm" });
  /** The documentary. */
  public final static MediaGenres2 DOCUMENTARY     = new MediaGenres2("DOCUMENTARY", 8, "Documentary", new String[] { "Dokumentation", "Mondo" });
  /** The drama. */
  public final static MediaGenres2 DRAMA           = new MediaGenres2("DRAMA", 9, "Drama", new String[] {});
  /** The eastern. */
  public final static MediaGenres2 EASTERN         = new MediaGenres2("EASTERN", 10, "Eastern", new String[] {});
  /** The erotic. */
  public final static MediaGenres2 EROTIC          = new MediaGenres2("EROTIC", 11, "Erotic", new String[] { "Erotik", "Sex", "Adult" });
  /** The family. */
  public final static MediaGenres2 FAMILY          = new MediaGenres2("FAMILY", 12, "Family", new String[] { "Kinder-/Familienfilm", "Familie" });
  /** The fan film. */
  public final static MediaGenres2 FAN_FILM        = new MediaGenres2("FAN_FILM", 13, "Fan Film", new String[] { "Fan-Film" });
  /** The fantasy. */
  public final static MediaGenres2 FANTASY         = new MediaGenres2("FANTASY", 14, "Fantasy", new String[] {});
  /** The film noir. */
  public final static MediaGenres2 FILM_NOIR       = new MediaGenres2("FILM_NOIR", 15, "Film Noir", new String[] { "Film-Noir", "Neo-noir" });
  /** The foreign. */
  public final static MediaGenres2 FOREIGN         = new MediaGenres2("FOREIGN", 16, "Foreign", new String[] {});
  /** The game show. */
  public final static MediaGenres2 GAME_SHOW       = new MediaGenres2("GAME_SHOW", 17, "Gameshow", new String[] { "Game-Show" });
  /** The history. */
  public final static MediaGenres2 HISTORY         = new MediaGenres2("HISTORY", 18, "History", new String[] { "Historienfilm", "Geschichte",
      "Historie"                                  });
  /** The holiday. */
  public final static MediaGenres2 HOLIDAY         = new MediaGenres2("HOLIDAY", 19, "Holiday", new String[] {});
  /** The horror. */
  public final static MediaGenres2 HORROR          = new MediaGenres2("HORROR", 20, "Horror", new String[] { "Splatter", "Grusel" });
  /** The indie. */
  public final static MediaGenres2 INDIE           = new MediaGenres2("INDIE", 21, "Indie", new String[] { "Experimentalfilm", "Amateur" });
  /** The music. */
  public final static MediaGenres2 MUSIC           = new MediaGenres2("MUSIC", 22, "Music", new String[] { "Musikfilm", "Musik" });
  /** The musical. */
  public final static MediaGenres2 MUSICAL         = new MediaGenres2("MUSICAL", 23, "Musical", new String[] {});
  /** The mystery. */
  public final static MediaGenres2 MYSTERY         = new MediaGenres2("MYSTERY", 24, "Mystery", new String[] {});
  /** The neo noir. */
  public final static MediaGenres2 NEO_NOIR        = new MediaGenres2("NEO_NOIR", 25, "Neo Noir", new String[] {});
  /** The news. */
  public final static MediaGenres2 NEWS            = new MediaGenres2("NEWS", 26, "News", new String[] { "Nachrichten" });
  /** The reality tv. */
  public final static MediaGenres2 REALITY_TV      = new MediaGenres2("REALITY_TV", 27, "Reality TV", new String[] { "Reality-TV" });
  /** The road movie. */
  public final static MediaGenres2 ROAD_MOVIE      = new MediaGenres2("ROAD_MOVIE", 28, "Road Movie", new String[] {});
  /** The romance. */
  public final static MediaGenres2 ROMANCE         = new MediaGenres2("ROMANCE", 29, "Romance", new String[] { "Liebe/Romantik", "Romanze",
      "Lovestory"                                 });
  /** The science fiction. */
  public final static MediaGenres2 SCIENCE_FICTION = new MediaGenres2("SCIENCE_FICTION", 30, "Science Fiction", new String[] { "Sci-Fi",
      "Science-Fiction"                           });
  /** The = new MediaGenres2(tv) series. */
  public final static MediaGenres2 SERIES          = new MediaGenres2("SERIES", 31, "Series", new String[] { "Serie", "TV-Serie", "TV-Mini-Serie" });
  /** The short. */
  public final static MediaGenres2 SHORT           = new MediaGenres2("SHORT", 32, "Short", new String[] { "Kurzfilm" });
  /** The silent ones. */
  public final static MediaGenres2 SILENT_MOVIE    = new MediaGenres2("SILENT_MOVIE", 33, "Silent Movie", new String[] { "Stummfilm" });
  /** The sport. */
  public final static MediaGenres2 SPORT           = new MediaGenres2("SPORT", 34, "Sport", new String[] { "Kampfsport" });
  /** The sporting event. */
  public final static MediaGenres2 SPORTING_EVENT  = new MediaGenres2("SPORTING_EVENT", 35, "Sporting Event", new String[] { "Sportereignis" });
  /** The sports film. */
  public final static MediaGenres2 SPORTS_FILM     = new MediaGenres2("SPORTS_FILM", 36, "Sports Film", new String[] { "Sportfilm", "Sport Film" });
  /** The suspense. */
  public final static MediaGenres2 SUSPENSE        = new MediaGenres2("SUSPENSE", 37, "Suspense", new String[] {});
  /** The talk show. */
  public final static MediaGenres2 TALK_SHOW       = new MediaGenres2("TALK_SHOW", 38, "Talk show", new String[] { "Talk-Show" });
  /** The tv movie. */
  public final static MediaGenres2 TV_MOVIE        = new MediaGenres2("TV_MOVIE", 39, "TV Movie", new String[] { "TV-Film", "TV-Pilotfilm" });
  /** The thriller. */
  public final static MediaGenres2 THRILLER        = new MediaGenres2("THRILLER", 40, "Thriller", new String[] {});
  /** The war. */
  public final static MediaGenres2 WAR             = new MediaGenres2("WAR", 41, "War", new String[] { "Krieg", "Kriegsfilm" });
  /** The western. */
  public final static MediaGenres2 WESTERN         = new MediaGenres2("WESTERN", 42, "Western", new String[] {});

  /** The name. */
  private String                   name;

  /** The alternate names. */
  private String[]                 alternateNames;

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
  private MediaGenres2(String enumName, int ordinal, String name, String[] alternateNames) {
    super(enumName, ordinal);
    this.name = name;
    this.alternateNames = alternateNames;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.DynaEnum#toString()
   */
  public String toString() {
    return this.name;
  }

  /**
   * Values.
   * 
   * @return the media genres2[]
   */
  public static MediaGenres2[] values() {
    return values(MediaGenres2.class);
  }

  /**
   * Gets the genre.
   * 
   * @param name
   *          the name
   * @return the genre
   */
  public static MediaGenres2 getGenre(String name) {
    for (MediaGenres2 genre : values()) {
      // check if the "enum" name matches
      if (genre.name().equals(name)) {
        return genre;
      }
      // check if the printable name matches
      if (genre.name.equals(name)) {
        return genre;
      }
      // check if one of the possible names matches
      for (String notation : genre.alternateNames) {
        if (notation.equalsIgnoreCase(name)) {
          return genre;
        }
      }
    }

    // dynamically create new one
    return new MediaGenres2(name, values().length, name, new String[] {});
  }
}
