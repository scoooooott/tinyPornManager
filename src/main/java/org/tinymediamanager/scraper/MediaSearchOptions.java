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
package org.tinymediamanager.scraper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
  protected MediaType             type;

  private String                  query    = "";
  private int                     year     = 0;
  private HashMap<String, Object> ids      = new HashMap<>();
  private Locale                  language = Locale.getDefault();
  private CountryCode             country  = CountryCode.getDefault();

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
   * Get the id for the given provider id as String
   *
   * @param providerId
   *          the provider Id
   * @return the id as String or null
   */
  public String getIdAsString(String providerId) {
    Object id = ids.get(providerId);
    if (id != null) {
      return String.valueOf(id);
    }

    return null;
  }

  /**
   * Get the id for the given provider id as Integer
   *
   * @param providerId
   *          the provider Id
   * @return the id as Integer or null
   */
  public Integer getIdAsInteger(String providerId) {
    Object id = ids.get(providerId);
    if (id != null) {
      if (id instanceof Integer) {
        return (Integer) id;
      }
      if (id instanceof String)
        try {
          return Integer.parseInt((String) id);
        }
        catch (Exception ignored) {
        }
    }

    return null;
  }

  /**
   * Get the id for the given provider id as int
   *
   * @param providerId
   *          the provider Id
   * @return the id as int or 0
   */
  public int getIdAsInt(String providerId) {
    Integer id = getIdAsInteger(providerId);
    if (id == null) {
      return 0;
    }
    return id.intValue();
  }

  /**
   * Get the id for the given provider id as int or the chosen default value
   *
   * @param providerId
   *          the provider Id
   * @return the id as int or the default value
   */
  public int getIdAsIntOrDefault(String providerId, int defaultValue) {
    Integer id = getIdAsInteger(providerId);
    if (id == null) {
      return defaultValue;
    }
    return id.intValue();
  }

  /**
   * Set an media id for a provider id
   *
   * @param providerId
   *          the provider id
   * @param id
   *          the media id
   */
  public void setId(String providerId, String id) {
    ids.put(providerId, id);
  }

  /**
   * set die provider ids for the search
   */
  public void setIds(Map<String, Object> newIds) {
    ids.clear();
    ids.putAll(newIds);
  }

  /**
   * Get the imdb id - just a convenience method to get the Id for the provider imdb
   *
   * @return the imdbid or an empty string
   */
  public String getImdbId() {
    Object obj = ids.get(MediaMetadata.IMDB);
    if (obj == null) {
      // legacy
      obj = ids.get("imdbId");
      if (obj == null) {
        return "";
      }
    }
    return obj.toString();
  }

  /**
   * Get the tmdb id - just a convenience method to get the Id for the provider tmdb
   *
   * @return the tmdbid or 0
   */
  public int getTmdbId() {
    Integer id = getIdAsInteger(MediaMetadata.TMDB);
    if (id == null || id == 0) {
      id = getIdAsInteger("tmdbId");
    }
    if (id != null) {
      return id;
    }
    return 0;
  }

  /**
   * Set the imdb id - just a convenience method to set the Id for the provider imdb
   *
   * @param imdbId
   *          the imdb id
   */
  public void setImdbId(String imdbId) {
    ids.put(MediaMetadata.IMDB, imdbId);
  }

  /**
   * Set the itdb id - just a convenience method to set the Id for the provider tmdb
   *
   * @param tmdbId
   *          the tmdb id
   */
  public void setTmdbId(int tmdbId) {
    ids.put(MediaMetadata.TMDB, tmdbId);
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
