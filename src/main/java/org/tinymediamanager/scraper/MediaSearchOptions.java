/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * The Class MediaSearchOptions. Pass arguments to the scraper searches
 * 
 * @author Manuel Laggner
 * @since 2.0
 */
public class MediaSearchOptions {
  protected MediaType   type;

  protected String      query    = "";
  protected int         year     = 0;
  protected String      imdbId   = "";
  protected int         tmdbId   = 0;
  protected Locale      language = Locale.getDefault();
  protected CountryCode country  = CountryCode.getDefault();

  public MediaSearchOptions(MediaType type) {
    this.type = type;
  }

  public MediaSearchOptions(MediaType type, String query) {
    this.type = type;
    this.query = query;
  }

  /**
   * Get the media type for this options
   *
   * @return the media type
   */
  public MediaType getMediaType() {
    return type;
  }

  /**
   * Get the search query
   *
   * @return the search query
   */
  public String getQuery() {
    return query;
  }

  /**
   * Set the search query
   *
   * @param query
   *          the search query
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * Get the year
   *
   * @return the year or 0 if none set
   */
  public int getYear() {
    return year;
  }

  /**
   * Set the year
   *
   * @param year
   *          the year
   */
  public void setYear(int year) {
    this.year = year;
  }

  /**
   * Get the IMDB Id
   *
   * @return the IMDB Id
   */
  public String getImdbId() {
    return imdbId;
  }

  /**
   * Set the IMDB Id
   *
   * @param imdbId
   *          the IMDB Id
   */
  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  /**
   * Get the TMDB Id
   *
   * @return the TMDB Id
   */
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Set the TMDB Id
   *
   * @param tmdbId
   *          the TMDB Id
   */
  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  /**
   * Get the language for the search
   *
   * @return a locale holding the right language
   */
  public Locale getLanguage() {
    return language;
  }

  /**
   * Set the language for the search
   *
   * @param language
   *          a locale for the right language
   */
  public void setLanguage(Locale language) {
    this.language = language;
  }

  /**
   * Get the CountryCode for the search
   *
   * @return the CountryCode
   */
  public CountryCode getCountry() {
    return country;
  }

  /**
   * Set the CountryCode for the search
   *
   * @param country
   *          the CountryCode
   */
  public void setCountry(CountryCode country) {
    this.country = country;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
