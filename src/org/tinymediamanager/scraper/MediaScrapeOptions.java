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
package org.tinymediamanager.scraper;

import java.util.HashMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

/**
 * The Class MediaScrapeOptions.
 * 
 * @author Manuel Laggner
 */
public class MediaScrapeOptions {

  /** The result. */
  private MediaSearchResult       result;

  /** The metadata. */
  private MediaMetadata           metadata;

  /** The imdb id. */
  private String                  imdbId      = "";

  /** The tmdb id. */
  private int                     tmdbId      = 0;

  /** The ids. */
  private HashMap<String, String> ids         = new HashMap<String, String>();

  /** The type. */
  private MediaType               type;

  /** The artwork type. */
  private MediaArtworkType        artworkType = MediaArtworkType.ALL;

  /**
   * Instantiates a new media scrape options.
   */
  public MediaScrapeOptions() {

  }

  /**
   * Gets the result.
   * 
   * @return the result
   */
  public MediaSearchResult getResult() {
    return result;
  }

  /**
   * Sets the result.
   * 
   * @param result
   *          the new result
   */
  public void setResult(MediaSearchResult result) {
    this.result = result;
  }

  /**
   * Gets the id.
   * 
   * @param key
   *          the key
   * @return the id
   */
  public String getId(String key) {
    return ids.get(key);
  }

  /**
   * Sets the id.
   * 
   * @param key
   *          the key
   * @param id
   *          the new id
   */
  public void setId(String key, String id) {
    this.ids.put(key, id);
  }

  /**
   * Gets the imdb id.
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    return imdbId;
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Sets the imdb id.
   * 
   * @param imdbId
   *          the new imdb id
   */
  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param tmdbId
   *          the new tmdb id
   */
  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  /**
   * Gets the artwork type.
   * 
   * @return the artwork type
   */
  public MediaArtworkType getArtworkType() {
    return artworkType;
  }

  /**
   * Sets the artwork type.
   * 
   * @param artworkType
   *          the new artwork type
   */
  public void setArtworkType(MediaArtworkType artworkType) {
    this.artworkType = artworkType;
  }

  /**
   * Gets the metadata.
   * 
   * @return the metadata
   */
  public MediaMetadata getMetadata() {
    return metadata;
  }

  /**
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   */
  public void setMetadata(MediaMetadata metadata) {
    this.metadata = metadata;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public MediaType getType() {
    return type;
  }

  /**
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  public void setType(MediaType type) {
    this.type = type;
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
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
