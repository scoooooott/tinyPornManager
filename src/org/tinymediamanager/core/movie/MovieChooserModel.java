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

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;

/**
 * The Class MovieChooserModel.
 */
public class MovieChooserModel extends AbstractModelObject {

  /** The Constant logger. */
  private static final Logger    LOGGER  = Logger.getLogger(MovieChooserModel.class);

  /** The metadata provider. */
  private IMediaMetadataProvider metadataProvider;

  /** The result. */
  private MediaSearchResult      result;

  /** The metadata. */
  private MediaMetadata          metadata;

  /** The name. */
  private String                 name;

  /** The overview. */
  private String                 overview;

  /** The year. */
  private String                 year;

  /** The combined name. */
  private String                 combinedName;

  /** The poster url. */
  private String                 posterUrl;

  /** The scraped. */
  private boolean                scraped = false;

  /* new scraper logic */
  /**
   * Instantiates a new movie chooser model.
   * 
   * @param metadataProvider
   *          the metadata provider
   * @param result
   *          the result
   */
  public MovieChooserModel(IMediaMetadataProvider metadataProvider, MediaSearchResult result) {
    this.metadataProvider = metadataProvider;
    this.result = result;
    // name
    setName(result.getTitle());
    // year
    setYear(result.getYear());
    // combined name (name (year))
    setCombinedName();
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  public void setName(String name) {
    String oldValue = this.name;
    this.name = name;
    firePropertyChange("name", oldValue, name);
  }

  /**
   * Sets the overview.
   * 
   * @param overview
   *          the new overview
   */
  public void setOverview(String overview) {
    String oldValue = this.overview;
    this.overview = overview;
    firePropertyChange("overview", oldValue, overview);
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the overview.
   * 
   * @return the overview
   */
  public String getOverview() {
    // if (metadata == null) {
    // scrapeMetaData();
    // }
    return overview;
  }

  /**
   * Gets the poster url.
   * 
   * @return the poster url
   */
  public String getPosterUrl() {
    return posterUrl;
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange("posterUrl", oldValue, newValue);
  }

  /**
   * Gets the year.
   * 
   * @return the year
   */
  public String getYear() {
    return year;
  }

  /**
   * Sets the year.
   * 
   * @param year
   *          the new year
   */
  public void setYear(String year) {
    String oldValue = this.year;
    this.year = year;
    firePropertyChange("year", oldValue, year);
  }

  /**
   * Sets the combined name.
   */
  public void setCombinedName() {
    String oldValue = this.combinedName;
    this.combinedName = getName() + " (" + getYear() + ")";
    firePropertyChange("combinedName", oldValue, this.combinedName);
  }

  /**
   * Gets the combined name.
   * 
   * @return the combined name
   */
  public String getCombinedName() {
    return combinedName;
  }

  /**
   * Scrape meta data.
   */
  public void scrapeMetaData() {
    try {
      metadata = metadataProvider.getMetaData(result);
      setOverview(metadata.getPlot());

      // poster for preview
      List<MediaArt> mediaArt = metadata.getFanart();
      for (MediaArt art : mediaArt) {
        if (art.getType() == MediaArtifactType.POSTER) {
          setPosterUrl(art.getDownloadUrl());

          break;
        }
      }

      scraped = true;

    }
    catch (IOException e) {
      LOGGER.error("scrapeMedia", e);
    }
    catch (Exception e) {
      LOGGER.error("scrapeMedia", e);
    }

  }

  /**
   * Gets the metadata.
   * 
   * @return the metadata
   */
  public MediaMetadata getMetadata() {
    // if (metadata == null) {
    // scrapeMetaData();
    // }
    return metadata;
  }

  /**
   * Checks if is scraped.
   * 
   * @return true, if is scraped
   */
  public boolean isScraped() {
    return scraped;
  }

}
