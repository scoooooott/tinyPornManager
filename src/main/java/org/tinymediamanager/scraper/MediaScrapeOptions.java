/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * This class is used to set the scrape options for scraping.
 * 
 * @author Manuel Laggner
 * @since 2.1
 * 
 */
public class MediaScrapeOptions {
  private MediaSearchResult   result;
  private MediaMetadata       metadata;
  private Map<String, Object> ids         = new HashMap<>();
  private MediaType           type;
  private MediaArtworkType    artworkType = MediaArtworkType.ALL;
  private Locale              language    = Locale.getDefault();
  private CountryCode         country     = CountryCode.getDefault();
  private FanartSizes         fanartSize  = FanartSizes.MEDIUM;      // default; will be overwritten by tmm settings
  private PosterSizes         posterSize  = PosterSizes.MEDIUM;      // default; will be overwritten by tmm settings

  public MediaScrapeOptions(MediaType type) {
    this.type = type;
  }

  /**
   * Get a previous found search result
   * 
   * @return the search result if available or null
   */
  public MediaSearchResult getResult() {
    return result;
  }

  /**
   * Set a search result if available
   * 
   * @param result
   *          the search result
   */
  public void setResult(MediaSearchResult result) {
    this.result = result;
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
    return getIdAsIntOrDefault(providerId, 0);
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
    int id = getIdAsInt(MediaMetadata.TMDB);
    if (id == 0) {
      id = getIdAsIntOrDefault("tmdbId", 0);
    }
    return id;
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
   * Get the preferred artwork type
   *
   * @return the artwork type
   */
  public MediaArtworkType getArtworkType() {
    return artworkType;
  }

  /**
   * Set the preferred artwork type
   * 
   * @param artworkType
   *          the artwork type
   */
  public void setArtworkType(MediaArtworkType artworkType) {
    this.artworkType = artworkType;
  }

  /**
   * Get any previous scraped meta data
   * 
   * @return the meta data or null
   */
  public MediaMetadata getMetadata() {
    return metadata;
  }

  /**
   * Set any existing meta data
   *
   * @param metadata
   *          the meta data
   */
  public void setMetadata(MediaMetadata metadata) {
    this.metadata = metadata;
  }

  /**
   * Get the media type
   *
   * @return the media type
   */
  public MediaType getType() {
    return type;
  }

  /**
   * Get the desired language for the scrape
   * 
   * @return the language
   */
  public Locale getLanguage() {
    return language;
  }

  /**
   * Set the desired language for the scrape
   * 
   * @param language
   *          language
   */
  public void setLanguage(Locale language) {
    this.language = language;
  }

  /**
   * Get the desired country for scraping
   *
   * @return the country
   */
  public CountryCode getCountry() {
    return country;
  }

  /**
   * Set the desired country for scraping
   *
   * @param country
   *          the country
   */
  public void setCountry(CountryCode country) {
    this.country = country;
  }

  /**
   * Get the desired fanart size for scraping
   *
   * @return the desired fanart size
   */
  public FanartSizes getFanartSize() {
    return fanartSize;
  }

  /**
   * Get the desired poster size for scraping
   * 
   * @return the desired poster size
   */
  public PosterSizes getPosterSize() {
    return posterSize;
  }

  /**
   * Set the desired fanart size for scraping
   * 
   * @param fanartSize
   *          the desired fanart size
   */
  public void setFanartSize(FanartSizes fanartSize) {
    this.fanartSize = fanartSize;
  }

  /**
   * Set the desired poster size for scraping
   * 
   * @param posterSize
   *          the desired poster size
   */
  public void setPosterSize(PosterSizes posterSize) {
    this.posterSize = posterSize;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
      @Override
      protected boolean accept(Field f) {
        return super.accept(f) && !f.getName().equals("metadata");
      }
    }).toString();
  }
}
