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
package org.tinymediamanager.scraper.tmdb;

import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

/**
 * The Class TmdbArtwork.
 */
public class TmdbArtwork {

  /**
   * The Enum PosterSizes.
   */
  public enum PosterSizes {
    /** The original. */
    original,
    /** The w500. */
    w500,
    /** The w342. */
    w342,
    /** The w185. */
    w185,
    /** The w154. */
    w154,
    /** The w92. */
    w92
  }

  /**
   * The Enum FanartSizes.
   */
  public enum FanartSizes {
    /** The original. */
    original,
    /** The w1280. */
    w1280,
    /** The w780. */
    w780,
    /** The w300. */
    w300
  }

  /** The type. */
  private MediaArtworkType type;

  /** The base url. */
  private String           baseUrl;

  /** The file path. */
  private String           filePath;

  private int              width;

  private int              height;

  /**
   * Instantiates a new tmdb artwork.
   * 
   * @param type
   *          the type
   * @param baseUrl
   *          the base url
   * @param filePath
   *          the file path
   */
  public TmdbArtwork(MediaArtworkType type, String baseUrl, String filePath) {
    this.type = type;
    this.baseUrl = baseUrl;
    this.filePath = filePath;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public MediaArtworkType getType() {
    return type;
  }

  /**
   * Gets the base url.
   * 
   * @return the base url
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Gets the file path.
   * 
   * @return the file path
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * Gets the url for small artwork.
   * 
   * @return the url for small artwork
   */
  public String getUrlForSmallArtwork() {
    String url = null;

    switch (type) {
      case POSTER:
        url = baseUrl + "w154" + filePath;
        break;

      case BACKGROUND:
        url = baseUrl + "w300" + filePath;
        break;
    }

    return url;

  }

  /**
   * Gets the url for medium artwork.
   * 
   * @return the url for medium artwork
   */
  public String getUrlForMediumArtwork() {
    String url = null;

    switch (type) {
      case POSTER:
        url = baseUrl + "w342" + filePath;
        break;

      case BACKGROUND:
        url = baseUrl + "w780" + filePath;
        break;
    }

    return url;

  }

  /**
   * Gets the url for original artwork.
   * 
   * @return the url for original artwork
   */
  public String getUrlForOriginalArtwork() {
    String url = baseUrl + "original" + filePath;
    return url;

  }

  /**
   * Gets the url for special artwork.
   * 
   * @param size
   *          the size
   * @return the url for special artwork
   */
  public String getUrlForSpecialArtwork(String size) {
    String url = baseUrl + size + filePath;
    return url;
  }

  /**
   * Gets the url for special artwork.
   * 
   * @param size
   *          the size
   * @return the url for special artwork
   */
  public String getUrlForSpecialArtwork(PosterSizes size) {
    return getUrlForSpecialArtwork(size.name());
  }

  /**
   * Gets the url for special artwork.
   * 
   * @param size
   *          the size
   * @return the url for special artwork
   */
  public String getUrlForSpecialArtwork(FanartSizes size) {
    return getUrlForSpecialArtwork(size.name());
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
}
