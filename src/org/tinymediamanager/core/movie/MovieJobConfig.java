/*
 * Copyright 2012 Manuel Laggner
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

/**
 * The Class MovieJobConfig.
 */
public class MovieJobConfig {

  /** The force best match. */
  public static int FORCE_BEST_MATCH        = 0;
  
  /** The choose movie. */
  public static int CHOOSE_MOVIE            = 1;
  
  /** The choose images. */
  public static int CHOOSE_IMAGES           = 2;
  
  /** The choose movie and images. */
  public static int CHOOSE_MOVIE_AND_IMAGES = 3;

  /** The movie to scrape. */
  private Movie     movieToScrape;
  
  /** The scrape setting. */
  private int       scrapeSetting;

  /**
   * Instantiates a new movie job config.
   *
   * @param movie the movie
   * @param setting the setting
   */
  public MovieJobConfig(Movie movie, int setting) {
    movieToScrape = movie;
    scrapeSetting = setting;
  }

  /**
   * Gets the scrape setting.
   *
   * @return the scrape setting
   */
  public int getScrapeSetting() {
    return scrapeSetting;
  }

  /**
   * Gets the movie.
   *
   * @return the movie
   */
  public Movie getMovie() {
    return movieToScrape;
  }

}
