/*
 * Copyright 2012 - 2020 Manuel Laggner
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * the class {@link MediaSearchAndScrapeOptions} is used to pass all needed parameters to the scrapers
 *
 * @author Manuel Laggner
 * @since 3.1
 */
public abstract class MediaSearchAndScrapeOptions {
  // common fields
  protected MediaType           type;
  protected Map<String, Object> ids              = new HashMap<>();
  protected MediaLanguages      language;

  // search related fields
  protected String              searchQuery      = "";
  protected int                 searchYear       = -1;

  // scrape related fields
  protected MediaScraper        metadataScraper;
  protected List<MediaScraper>  artworkScrapers  = new ArrayList<>();
  protected List<MediaScraper>  trailerScrapers  = new ArrayList<>();
  protected List<MediaScraper>  subtitleScrapers = new ArrayList<>();

  // helper fields to pass data around in the search&scrape process
  protected MediaSearchResult   searchResult;
  protected MediaMetadata       metadata;

  protected MediaSearchAndScrapeOptions(MediaType type) {
    this.type = type;
  }

  /**
   * copy constructor
   * 
   * @param original
   *          the original to copy
   */
  protected MediaSearchAndScrapeOptions(MediaSearchAndScrapeOptions original) {
    this.type = original.type;
    setDataFromOtherOptions(original);
  }

  /**
   * set the data from another {@link MediaSearchAndScrapeOptions}
   * 
   * @param original
   *          the original
   */
  public void setDataFromOtherOptions(MediaSearchAndScrapeOptions original) {
    this.ids.putAll(original.ids);
    this.language = original.language;
    this.searchQuery = original.searchQuery;
    this.searchYear = original.searchYear;
    this.searchResult = original.searchResult;
    this.metadata = original.metadata;

    this.metadataScraper = original.metadataScraper;
    this.artworkScrapers.addAll(original.artworkScrapers);
    this.trailerScrapers.addAll(original.trailerScrapers);
    this.subtitleScrapers.addAll(original.subtitleScrapers);
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
   * get the language to scrape
   *
   * @return the language to scrape
   */
  public MediaLanguages getLanguage() {
    return language;
  }

  /**
   * set the language to scrape
   *
   * @param language
   *          the language to scrape
   */
  public void setLanguage(MediaLanguages language) {
    this.language = language;
  }

  /**
   * Get the search query
   *
   * @return the search query
   */
  public String getSearchQuery() {
    return searchQuery;
  }

  /**
   * Set the search searchQuery
   *
   * @param searchQuery
   *          the search query
   */
  public void setSearchQuery(String searchQuery) {
    this.searchQuery = searchQuery;
  }

  /**
   * Get the search year
   *
   * @return the search year or -1 if none set
   */
  public int getSearchYear() {
    return searchYear;
  }

  /**
   * Set the search year
   *
   * @param searchYear
   *          the search year
   */
  public void setSearchYear(int searchYear) {
    this.searchYear = searchYear;
  }

  /**
   * get all given ids
   * 
   * @return the map with all ids
   */
  public Map<String, Object> getIds() {
    return ids;
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
          // nothing to be done here
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
    return id;
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
   * Get a previous found search result
   *
   * @return the search result if available or null
   */
  public MediaSearchResult getSearchResult() {
    return searchResult;
  }

  /**
   * Set a search result if available
   *
   * @param searchResult
   *          the search result
   */
  public void setSearchResult(MediaSearchResult searchResult) {
    this.searchResult = searchResult;
  }

  /**
   * Get any previous scraped meta data (either explicitly set or from a search result)
   *
   * @return the meta data or null
   */
  public MediaMetadata getMetadata() {
    if (metadata != null) {
      return metadata;
    }
    else if (searchResult != null) {
      return searchResult.getMediaMetadata();
    }
    return null;
  }

  /**
   * explicitly set a meta data
   * 
   * @param metadata
   *          the meta data to set
   */
  public void setMetadata(MediaMetadata metadata) {
    this.metadata = metadata;
  }

  /**
   * Gets the metadata scraper.
   *
   * @return the metadata scraper
   */
  public MediaScraper getMetadataScraper() {
    return metadataScraper;
  }

  /**
   * Gets the artwork scrapers.
   *
   * @return the artwork scrapers
   */
  public List<MediaScraper> getArtworkScrapers() {
    return artworkScrapers;
  }

  /**
   * Gets the trailer scrapers.
   *
   * @return the trailer scrapers
   */
  public List<MediaScraper> getTrailerScrapers() {
    return trailerScrapers;
  }

  /**
   * Gets the subtitle scrapers.
   *
   * @return the subtitle scrapers
   */
  public List<MediaScraper> getSubtitleScrapers() {
    return subtitleScrapers;
  }

  /**
   * Sets the metadata scraper.
   *
   * @param metadataScraper
   *          the new metadata scraper
   */
  public void setMetadataScraper(MediaScraper metadataScraper) {
    this.metadataScraper = metadataScraper;
  }

  /**
   * Set the artwork scrapers.
   *
   * @param artworkScrapers
   *          the artwork scrapers
   */
  public void setArtworkScraper(List<MediaScraper> artworkScrapers) {
    this.artworkScrapers.clear();
    this.artworkScrapers.addAll(artworkScrapers);
  }

  /**
   * Set the trailer scrapers.
   *
   * @param trailerScraper
   *          the trailer scrapers
   */
  public void setTrailerScraper(List<MediaScraper> trailerScraper) {
    this.trailerScrapers.clear();
    this.trailerScrapers.addAll(trailerScraper);
  }

  /**
   * Set the subtitle scrapers.
   *
   * @param subtitleScraper
   *          the subtitle scrapers
   */
  public void setSubtitleScraper(List<MediaScraper> subtitleScraper) {
    this.subtitleScrapers.clear();
    this.subtitleScrapers.addAll(subtitleScraper);
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
        return super.accept(f) && !"metadata".equals(f.getName());
      }
    }).toString();
  }
}
