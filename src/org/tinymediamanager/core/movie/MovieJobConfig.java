package org.tinymediamanager.core.movie;

public class MovieJobConfig {

  public static int FORCE_BEST_MATCH        = 0;
  public static int CHOOSE_MOVIE            = 1;
  public static int CHOOSE_IMAGES           = 2;
  public static int CHOOSE_MOVIE_AND_IMAGES = 3;

  private Movie     movieToScrape;
  private int       scrapeSetting;

  public MovieJobConfig(Movie movie, int setting) {
    movieToScrape = movie;
    scrapeSetting = setting;
  }

  public int getScrapeSetting() {
    return scrapeSetting;
  }

  public Movie getMovie() {
    return movieToScrape;
  }

}
