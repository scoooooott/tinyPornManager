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

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * The Class MediaArt.
 */
public class MediaArt {

  /** The Constant logger. */
  private static final Logger LOGGER = Logger.getLogger(MediaArt.class);

  private int                 tmdbId;

  /** The download url. */
  private String              downloadUrl;

  /** The provider id. */
  private String              providerId;

  /** The type. */
  private MediaArtifactType   type;

  /** The label. */
  private String              label;

  /** The season. */
  private int                 season;

  /**
   * Instantiates a new media art.
   */
  public MediaArt() {
  }

  /**
   * Gets the download url.
   * 
   * @return the download url
   */
  public String getDownloadUrl() {
    return downloadUrl;
  }

  /**
   * Sets the download url.
   * 
   * @param downloadUrl
   *          the new download url
   */
  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  /**
   * Gets the provider id.
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Sets the provider id.
   * 
   * @param providerId
   *          the new provider id
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
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
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  public void setType(MediaArtifactType type) {
    this.type = type;
  }

  /**
   * Gets the label.
   * 
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label.
   * 
   * @param label
   *          the new label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Gets the season.
   * 
   * @return the season
   */
  public int getSeason() {
    return season;
  }

  /**
   * Sets the season.
   * 
   * @param season
   *          the new season
   */
  public void setSeason(int season) {
    this.season = season;
  }

  /**
   * Gets the image is.
   * 
   * @return the image is
   */
  public InputStream getImageIS() {
    CachedUrl url;
    try {
      url = new CachedUrl(getDownloadUrl());
      return url.getInputStream();
    } catch (IOException e) {
      LOGGER.error("getImageIS", e);
    }
    return null;

  }

  public int getTmdbId() {
    return tmdbId;
  }

  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

}
