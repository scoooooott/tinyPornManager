/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The Class MediaGenres2.
 * 
 * @author Manuel Laggner
 */
public class MediaGenres extends DynaEnum<MediaGenres> {







  //@formatter:off
  public final static MediaGenres ACTION            = new MediaGenres("ACTION", 0, "Action",                      new String[] { "nl-Actie", "cz-Akcní", "sk-Akčný" });
  public final static MediaGenres ADVENTURE         = new MediaGenres("ADVENTURE", 1, "Adventure",                new String[] { "de-Abenteuer", "nl-Avontuur", "cz-Dobrodružný", "sk-Dobrodružný" });
  public final static MediaGenres ANIMATION         = new MediaGenres("ANIMATION", 2, "Animation",                new String[] { "Anime", "de-Zeichentrick", "cz-Animovaný", "nl-Animatie", "sk-Animovaný" });
  public final static MediaGenres ANIMAL            = new MediaGenres("ANIMAL", 3, "Animal",                      new String[] { "de-Tierfilm", "cz-O zvíratech", "nl-Dieren", "sk-O zvieratách" });
  public final static MediaGenres BIOGRAPHY         = new MediaGenres("BIOGRAPHY", 4, "Biography",                new String[] { "de-Biographie", "cz-Životopisný", "nl-Biografie","sk-Životopisný" });
  public final static MediaGenres COMEDY            = new MediaGenres("COMEDY", 5, "Comedy",                      new String[] { "de-Komödie", "nl-Komedie", "cz-Komedie", "sk-Komédia" });
  public final static MediaGenres CRIME             = new MediaGenres("CRIME", 6, "Crime",                        new String[] { "de-Krimi", "nl-Misdaad", "cz-Kriminální", "sk-Kriminálny" });
  public final static MediaGenres DISASTER          = new MediaGenres("DISASTER", 7, "Disaster",                  new String[] { "de-Katastrophen", "Katastrophenfilm", "nl-Rampen", "cz-Katastrofický", "sk-Katastrofický" });
  public final static MediaGenres DOCUMENTARY       = new MediaGenres("DOCUMENTARY", 8, "Documentary",            new String[] { "de-Dokumentation", "Mondo", "cz-Dokumentární", "nl-Documentaire", " sk-Dokumentárny" });
  public final static MediaGenres DRAMA             = new MediaGenres("DRAMA", 9, "Drama",                        new String[] { "sk-Dramatický" });
  public final static MediaGenres EASTERN           = new MediaGenres("EASTERN", 10, "Eastern",                   new String[] { "cz-Východní", "nl-Oosters", "sk-Orientálny" });
  public final static MediaGenres EROTIC            = new MediaGenres("EROTIC", 11, "Erotic",                     new String[] { "de-Erotik", "Sex", "Adult", "nl-Erotiek", "cz-Erotický", "Hardcore", "sk-Erotický" });
  public final static MediaGenres FAMILY            = new MediaGenres("FAMILY", 12, "Family",                     new String[] { "Kinder-/Familienfilm", "nl-Familie", "cz-Rodinný", "de-Familienfilm", "sk-Rodinný" });
  public final static MediaGenres FAN_FILM          = new MediaGenres("FAN_FILM", 13, "Fan Film",                 new String[] { "Fan-Film", "cz-Fanouškovský", "sk-Fanúšikovský" });
  public final static MediaGenres FANTASY           = new MediaGenres("FANTASY", 14, "Fantasy",                   new String[] { "cz-Fantazy" });
  public final static MediaGenres FILM_NOIR         = new MediaGenres("FILM_NOIR", 15, "Film Noir",               new String[] { "Film-Noir", "Neo-noir" });
  public final static MediaGenres FOREIGN           = new MediaGenres("FOREIGN", 16, "Foreign",                   new String[] { "de-Ausländisch", "cz-Zahranicní", "nl-Buitenlands", "sk-Zahraničný" });
  public final static MediaGenres GAME_SHOW         = new MediaGenres("GAME_SHOW", 17, "Gameshow",                new String[] { "Game-Show", "cz-Herní Show", "nl-Spelshow" });
  public final static MediaGenres HISTORY           = new MediaGenres("HISTORY", 18, "History",                   new String[] { "de-Historienfilm", "Geschichte", "cz-Historický", "nl-Historie", "sk-Historický" });
  public final static MediaGenres HOLIDAY           = new MediaGenres("HOLIDAY", 19, "Holiday",                   new String[] { "cz-Prázdninový", "nl-Vakantie", "sk-Sviatočný" });
  public final static MediaGenres HORROR            = new MediaGenres("HORROR", 20, "Horror",                     new String[] { "Splatter", "Grusel", "sk-Horor" });
  public final static MediaGenres INDIE             = new MediaGenres("INDIE", 21, "Indie",                       new String[] { "Experimentalfilm", "Amateur", "Essayfilm", "cz-Nezávislý" });
  public final static MediaGenres MUSIC             = new MediaGenres("MUSIC", 22, "Music",                       new String[] { "de-Musikfilm", "Musik", "nl-Muziek", "cz-Hudební", "sk-Hudobný" });
  public final static MediaGenres MUSICAL           = new MediaGenres("MUSICAL", 23, "Musical",                   new String[] { "cz-Muzikál", "sk-Muzikál" });
  public final static MediaGenres MYSTERY           = new MediaGenres("MYSTERY", 24, "Mystery",                   new String[] { "cz-Mysteriózní", "nl-Mysterie", "sk-Mysteriózny" });
  public final static MediaGenres NEO_NOIR          = new MediaGenres("NEO_NOIR", 25, "Neo Noir",                 new String[] {});
  public final static MediaGenres NEWS              = new MediaGenres("NEWS", 26, "News",                         new String[] { "de-Nachrichten", "cz-Zprávodajský", "nl-Nieuws", "sk-Správy" });
  public final static MediaGenres REALITY_TV        = new MediaGenres("REALITY_TV", 27, "Reality TV",             new String[] { "Reality-TV", "sk-Reality show" });
  public final static MediaGenres ROAD_MOVIE        = new MediaGenres("ROAD_MOVIE", 28, "Road Movie",             new String[] { "Roadmovie" });
  public final static MediaGenres ROMANCE           = new MediaGenres("ROMANCE", 29, "Romance",                   new String[] { "Liebe/Romantik", "Romanze", "cz-Romantický", "Lovestory", "Liebe", "de-Romantik", "nl-Romantiek", "sk-Romantický" });
  public final static MediaGenres SCIENCE_FICTION   = new MediaGenres("SCIENCE_FICTION", 30, "Science Fiction",   new String[] { "Sci-Fi", "Science-Fiction", "Sciencefiction" });
  public final static MediaGenres SERIES            = new MediaGenres("SERIES", 31, "Series",                     new String[] { "de-Serie", "TV-Serie", "TV-Mini-Serie", "cz-Serie", "Webserie", "sk-Seriál" });
  public final static MediaGenres SHORT             = new MediaGenres("SHORT", 32, "Short",                       new String[] { "de-Kurzfilm", "z-Krátký film", "nl-Korte Film", "sk-Krátky film" });
  public final static MediaGenres SILENT_MOVIE      = new MediaGenres("SILENT_MOVIE", 33, "Silent Movie",         new String[] { "de-Stummfilm", "cz-Nemý film", "nl-Stomme Film", "sk-Nemý Film" });
  public final static MediaGenres SPORT             = new MediaGenres("SPORT", 34, "Sport",                       new String[] { "Kampfsport", "cz-Sportovní", "nl-Sport", "sk-Šport" });
  public final static MediaGenres SPORTING_EVENT    = new MediaGenres("SPORTING_EVENT", 35, "Sporting Event",     new String[] { "de-Sportereignis", "nl-Sport Evenement", "cz-Sportovní událost", "sk-Športová udalosť" });
  public final static MediaGenres SPORTS_FILM       = new MediaGenres("SPORTS_FILM", 36, "Sports Film",           new String[] { "de-Sportfilm", "Sport Film", "nl-Sport Film", "cz-Spor", "sk-Športový Film" });
  public final static MediaGenres SUSPENSE          = new MediaGenres("SUSPENSE", 37, "Suspense",                 new String[] { "de-Spannung", "nl-Spanning" });
  public final static MediaGenres TALK_SHOW         = new MediaGenres("TALK_SHOW", 38, "Talk show",               new String[] { "Talk-Show" });
  public final static MediaGenres TV_MOVIE          = new MediaGenres("TV_MOVIE", 39, "TV Movie",                 new String[] { "de-TV-Film", "TV-Pilotfilm", "cz-Televizní film", "nl-TV film", "Heimatfilm", "sk-TV film" });
  public final static MediaGenres THRILLER          = new MediaGenres("THRILLER", 40, "Thriller",                 new String[] {});
  public final static MediaGenres WAR               = new MediaGenres("WAR", 41, "War",                           new String[] { "de-Krieg", "Kriegsfilm", "nl-Oorlog", "cz-Válecný", "sk-Vojnový" });
  public final static MediaGenres WESTERN           = new MediaGenres("WESTERN", 42, "Western",                   new String[] {});
  //@formatter:on

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
  private MediaGenres(String enumName, int ordinal, String name, String[] alternateNames) {
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
    return this.getLocalizedName();
  }

  public String dump() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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
        // first 3 chars are language like "de-"
        if (notation.substring(0, 3).equalsIgnoreCase(name)) {
          return genre;
        }
        // match names without prefix
        if (notation.substring(0, 3).equalsIgnoreCase(name.substring(0, 3))) {
          return genre;
        }
      }
    }

    // dynamically create new one
    return new MediaGenres(name, values().length, name, new String[] {});
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
