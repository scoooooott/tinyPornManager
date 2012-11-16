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
  ADVENTURE("Adventure", new String[] {}),
  /** The animation. */
  ANIMATION("Animation", new String[] {}),
  /** The Biography. */
  BIOGRAPHY("Biography", new String[] {}),
  /** The comedy. */
  COMEDY("Comedy", new String[] {}),
  /** The crime. */
  CRIME("Crime", new String[] {}),
  /** The disaster. */
  DISASTER("Disaster", new String[] {}),
  /** The documentary. */
  DOCUMENTARY("Documentary", new String[] {}),
  /** The drama. */
  DRAMA("Drama", new String[] {}),
  /** The eastern. */
  EASTERN("Eastern", new String[] {}),
  /** The erotic. */
  EROTIC("Erotic", new String[] {}),
  /** The family. */
  FAMILY("Family", new String[] {}),
  /** The fan film. */
  FAN_FILM("Fan Film", new String[] {}),
  /** The fantasy. */
  FANTASY("Fantasy", new String[] {}),
  /** The film noir. */
  FILM_NOIR("Film Noir", new String[] { "Film-Noir" }),
  /** The foreign. */
  FOREIGN("Foreign", new String[] {}),
  /** The game show. */
  GAME_SHOW("Gameshow", new String[] { "Game-Show" }),
  /** The history. */
  HISTORY("History", new String[] {}),
  /** The holiday. */
  HOLIDAY("Holiday", new String[] {}),
  /** The horror. */
  HORROR("Horror", new String[] {}),
  /** The indie. */
  INDIE("Indie", new String[] {}),
  /** The music. */
  MUSIC("Music", new String[] {}),
  /** The musical. */
  MUSICAL("Musical", new String[] {}),
  /** The mystery. */
  MYSTERY("Mystery", new String[] {}),
  /** The neo noir. */
  NEO_NOIR("Neo Noir", new String[] {}),
  /** The news. */
  NEWS("News", new String[] {}),
  /** The reality tv. */
  REALITY_TV("Reality TV", new String[] { "Reality-TV" }),
  /** The road movie. */
  ROAD_MOVIE("Road Movie", new String[] {}),
  /** The romance. */
  ROMANCE("Romance", new String[] {}),
  /** The science fiction. */
  SCIENCE_FICTION("Science Fiction", new String[] { "Sci-Fi" }),
  /** The short. */
  SHORT("Short", new String[] {}),
  /** The sport. */
  SPORT("Sport", new String[] {}),
  /** The sporting event. */
  SPORTING_EVENT("Sporting Event", new String[] {}),
  /** The sports film. */
  SPORTS_FILM("Sports Film", new String[] {}),
  /** The suspense. */
  SUSPENSE("Suspense", new String[] {}),
  /** The talk show. */
  TALK_SHOW("Talk show", new String[] { "Talk-Show" }),
  /** The tv movie. */
  TV_MOVIE("TV Movie", new String[] {}),
  /** The thriller. */
  THRILLER("Thriller", new String[] {}),
  /** The war. */
  WAR("War", new String[] {}),
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
