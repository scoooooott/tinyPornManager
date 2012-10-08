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

import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.PosterSizes;

/**
 * The Class TmdbArtwork.
 */
public class TmdbArtwork {

  /** The type. */
  private MediaArtifactType type;

  /** The base url. */
  private String baseUrl;

  /** The file path. */
  private String filePath;

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
  public TmdbArtwork(MediaArtifactType type, String baseUrl, String filePath) {
    this.type = type;
    this.baseUrl = baseUrl;
    this.filePath = filePath;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public MediaArtifactType getType() {
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
}
