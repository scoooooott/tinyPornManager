/**
 * 
 */
package org.tinymediamanager.scraper;

/**
 * @author manuel
 * 
 */
public enum MediaGenres {
  /** The action. */
  ACTION("Action", new String[] {}),
  /** The adventure. */
  ADVENTURE("Adventure", new String[] { "Abenteuer" }),
  /** The animation. */
  ANIMATION("Animation", new String[] { "Anime", "Zeichentrick" }),
  /** The animal. */
  ANIMAL("Animal", new String[] { "Tierfilm" }),
  /** The Biography. */
  BIOGRAPHY("Biography", new String[] { "Biographie" }),
  /** The comedy. */
  COMEDY("Comedy", new String[] { "Komödie" }),
  /** The crime. */
  CRIME("Crime", new String[] { "Krimi" }),
  /** The disaster. */
  DISASTER("Disaster", new String[] { "Katastrophen", "Katastrophenfilm" }),
  /** The documentary. */
  DOCUMENTARY("Documentary", new String[] { "Dokumentation", "Mondo" }),
  /** The drama. */
  DRAMA("Drama", new String[] {}),
  /** The eastern. */
  EASTERN("Eastern", new String[] {}),
  /** The erotic. */
  EROTIC("Erotic", new String[] { "Erotik", "Sex", "Adult" }),
  /** The family. */
  FAMILY("Family", new String[] { "Kinder-/Familienfilm", "Familie" }),
  /** The fan film. */
  FAN_FILM("Fan Film", new String[] { "Fan-Film" }),
  /** The fantasy. */
  FANTASY("Fantasy", new String[] {}),
  /** The film noir. */
  FILM_NOIR("Film Noir", new String[] { "Film-Noir", "Neo-noir" }),
  /** The foreign. */
  FOREIGN("Foreign", new String[] {}),
  /** The game show. */
  GAME_SHOW("Gameshow", new String[] { "Game-Show" }),
  /** The history. */
  HISTORY("History", new String[] { "Historienfilm", "Geschichte", "Historie" }),
  /** The holiday. */
  HOLIDAY("Holiday", new String[] {}),
  /** The horror. */
  HORROR("Horror", new String[] { "Splatter", "Grusel" }),
  /** The indie. */
  INDIE("Indie", new String[] { "Experimentalfilm", "Amateur" }),
  /** The music. */
  MUSIC("Music", new String[] { "Musikfilm", "Musik" }),
  /** The musical. */
  MUSICAL("Musical", new String[] {}),
  /** The mystery. */
  MYSTERY("Mystery", new String[] {}),
  /** The neo noir. */
  NEO_NOIR("Neo Noir", new String[] {}),
  /** The news. */
  NEWS("News", new String[] { "Nachrichten" }),
  /** The reality tv. */
  REALITY_TV("Reality TV", new String[] { "Reality-TV" }),
  /** The road movie. */
  ROAD_MOVIE("Road Movie", new String[] {}),
  /** The romance. */
  ROMANCE("Romance", new String[] { "Liebe/Romantik", "Romanze", "Lovestory" }),
  /** The science fiction. */
  SCIENCE_FICTION("Science Fiction", new String[] { "Sci-Fi", "Science-Fiction" }),
  /** The (tv) series. */
  SERIES("Series", new String[] { "Serie", "TV-Serie", "TV-Mini-Serie" }),
  /** The short. */
  SHORT("Short", new String[] { "Kurzfilm" }),
  /** The silent ones. */
  SILENT_MOVIE("Silent Movie", new String[] { "Stummfilm" }),
  /** The sport. */
  SPORT("Sport", new String[] { "Kampfsport" }),
  /** The sporting event. */
  SPORTING_EVENT("Sporting Event", new String[] { "Sportereignis" }),
  /** The sports film. */
  SPORTS_FILM("Sports Film", new String[] { "Sportfilm", "Sport Film" }),
  /** The suspense. */
  SUSPENSE("Suspense", new String[] {}),
  /** The talk show. */
  TALK_SHOW("Talk show", new String[] { "Talk-Show" }),
  /** The tv movie. */
  TV_MOVIE("TV Movie", new String[] { "TV-Film", "TV-Pilotfilm" }),
  /** The thriller. */
  THRILLER("Thriller", new String[] {}),
  /** The war. */
  WAR("War", new String[] { "Krieg", "Kriegsfilm" }),
  /** The western. */
  WESTERN("Western", new String[] {});

  /** The name. */
  private String   name;
  private String[] alternateNames;

  /**
   * Instantiates a new genres.
   */
  private MediaGenres() {
    this.name = "";
  }

  /**
   * Instantiates a new genres.
   * 
   * @param name
   *          the name
   */
  private MediaGenres(String name, String[] alternateNames) {
    this.name = name;
    this.alternateNames = alternateNames;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  public String toString() {
    return this.name;
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
      // check if the name matches
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
    return null;
  }
}
