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
  ACTION("Action"),
  /** The adventure. */
  ADVENTURE("Adventure"),
  /** The animation. */
  ANIMATION("Animation"),
  /** The comedy. */
  COMEDY("Comedy"),
  /** The crime. */
  CRIME("Crime"),
  /** The disaster. */
  DISASTER("Disaster"),
  /** The documentary. */
  DOCUMENTARY("Documentary"),
  /** The drama. */
  DRAMA("Drama"),
  /** The eastern. */
  EASTERN("Eastern"),
  /** The erotic. */
  EROTIC("Erotic"),
  /** The family. */
  FAMILY("Family"),
  /** The fan film. */
  FAN_FILM("Fan Film"),
  /** The fantasy. */
  FANTASY("Fantasy"),
  /** The film noir. */
  FILM_NOIR("Film Noir"),
  /** The foreign. */
  FOREIGN("Foreign"),
  /** The history. */
  HISTORY("History"),
  /** The holiday. */
  HOLIDAY("Holiday"),
  /** The horror. */
  HORROR("Horror"),
  /** The indie. */
  INDIE("Indie"),
  /** The music. */
  MUSIC("Music"),
  /** The musical. */
  MUSICAL("Musical"),
  /** The mystery. */
  MYSTERY("Mystery"),
  /** The neo noir. */
  NEO_NOIR("Neo Noir"),
  /** The road movie. */
  ROAD_MOVIE("Road Movie"),
  /** The romance. */
  ROMANCE("Romance"),
  /** The science fiction. */
  SCIENCE_FICTION("Science Fiction"),
  /** The short. */
  SHORT("Short"),
  /** The sport. */
  SPORT("Sport"),
  /** The sporting event. */
  SPORTING_EVENT("Sporting Event"),
  /** The sports film. */
  SPORTS_FILM("Sports Film"),
  /** The suspense. */
  SUSPENSE("Suspense"),
  /** The tv movie. */
  TV_MOVIE("TV Movie"),
  /** The thriller. */
  THRILLER("Thriller"),
  /** The war. */
  WAR("War"),
  /** The western. */
  WESTERN("Western");

  /** The name. */
  private String name;

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
  private MediaGenres(String name) {
    this.name = name;
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
      if (genre.name.equals(name)) {
        return genre;
      }
    }
    return null;
  }
}
