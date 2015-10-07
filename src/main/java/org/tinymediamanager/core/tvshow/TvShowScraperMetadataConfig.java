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
package org.tinymediamanager.core.tvshow;

import javax.xml.bind.annotation.XmlRootElement;

import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class TvShowScraperMetadataConfig.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "TvShowScraperMetadata")
public class TvShowScraperMetadataConfig extends AbstractModelObject {

  /** The title. */
  private boolean title         = true;

  /** The plot. */
  private boolean plot          = true;

  /** The rating. */
  private boolean rating        = true;

  /** The runtime. */
  private boolean runtime       = true;

  /** The year. */
  private boolean year          = true;

  /** The aired. */
  private boolean aired         = true;

  /** The status. */
  private boolean status        = true;

  /** The certification. */
  private boolean certification = true;

  /** The cast. */
  private boolean cast          = true;

  /** The genres. */
  private boolean genres        = true;

  /** The artwork. */
  private boolean artwork       = true;

  /** The episodes. */
  private boolean episodes      = true;

  /**
   * Instantiates a new scraper metadata config.
   */
  public TvShowScraperMetadataConfig() {
  }

  /**
   * Checks if is title.
   * 
   * @return true, if is title
   */
  public boolean isTitle() {
    return title;
  }

  /**
   * Checks if is plot.
   * 
   * @return true, if is plot
   */
  public boolean isPlot() {
    return plot;
  }

  /**
   * Checks if is rating.
   * 
   * @return true, if is rating
   */
  public boolean isRating() {
    return rating;
  }

  /**
   * Checks if is runtime.
   * 
   * @return true, if is runtime
   */
  public boolean isRuntime() {
    return runtime;
  }

  /**
   * Checks if is year.
   * 
   * @return true, if is year
   */
  public boolean isYear() {
    return year;
  }

  /**
   * Checks if is certification.
   * 
   * @return true, if is certification
   */
  public boolean isCertification() {
    return certification;
  }

  /**
   * Checks if is cast.
   * 
   * @return true, if is cast
   */
  public boolean isCast() {
    return cast;
  }

  /**
   * Checks if is genres.
   * 
   * @return true, if is genres
   */
  public boolean isGenres() {
    return genres;
  }

  /**
   * Checks if is artwork.
   * 
   * @return true, if is artwork
   */
  public boolean isArtwork() {
    return artwork;
  }

  /**
   * Sets the title.
   * 
   * @param newValue
   *          the new title
   */
  public void setTitle(boolean newValue) {
    boolean oldValue = this.title;
    this.title = newValue;
    firePropertyChange("title", oldValue, newValue);
  }

  /**
   * Sets the plot.
   * 
   * @param newValue
   *          the new plot
   */
  public void setPlot(boolean newValue) {
    boolean oldValue = this.plot;
    this.plot = newValue;
    firePropertyChange("plot", oldValue, newValue);
  }

  /**
   * Sets the rating.
   * 
   * @param rating
   *          the new rating
   */
  public void setRating(boolean rating) {
    this.rating = rating;
  }

  /**
   * Sets the runtime.
   * 
   * @param newValue
   *          the new runtime
   */
  public void setRuntime(boolean newValue) {
    boolean oldValue = this.runtime;
    this.runtime = newValue;
    firePropertyChange("runtime", oldValue, newValue);
  }

  /**
   * Sets the year.
   * 
   * @param newValue
   *          the new year
   */
  public void setYear(boolean newValue) {
    boolean oldValue = this.year;
    this.year = newValue;
    firePropertyChange("year", oldValue, newValue);
  }

  /**
   * Sets the certification.
   * 
   * @param newValue
   *          the new certification
   */
  public void setCertification(boolean newValue) {
    boolean oldValue = this.certification;
    this.certification = newValue;
    firePropertyChange("certification", oldValue, newValue);
  }

  /**
   * Sets the cast.
   * 
   * @param newValue
   *          the new cast
   */
  public void setCast(boolean newValue) {
    boolean oldValue = this.cast;
    this.cast = newValue;
    firePropertyChange("cast", oldValue, newValue);
  }

  /**
   * Sets the genres.
   * 
   * @param newValue
   *          the new genres
   */
  public void setGenres(boolean newValue) {
    boolean oldValue = this.genres;
    this.genres = newValue;
    firePropertyChange("genres", oldValue, newValue);
  }

  /**
   * Sets the artwork.
   * 
   * @param newValue
   *          the new artwork
   */
  public void setArtwork(boolean newValue) {
    boolean oldValue = this.artwork;
    this.artwork = newValue;
    firePropertyChange("artwork", oldValue, newValue);
  }

  /**
   * Checks if is episodes.
   * 
   * @return true, if is episodes
   */
  public boolean isEpisodes() {
    return episodes;
  }

  /**
   * Sets the episodes.
   * 
   * @param newValue
   *          the new episodes
   */
  public void setEpisodes(boolean newValue) {
    boolean oldValue = this.episodes;
    this.episodes = newValue;
    firePropertyChange("episodes", oldValue, newValue);
  }

  /**
   * Checks if is aired.
   * 
   * @return true, if is aired
   */
  public boolean isAired() {
    return aired;
  }

  /**
   * Sets the aired.
   * 
   * @param newValue
   *          the new aired
   */
  public void setAired(boolean newValue) {
    boolean oldValue = this.aired;
    this.aired = newValue;
    firePropertyChange("aired", oldValue, newValue);
  }

  /**
   * Checks if is status.
   * 
   * @return true, if is status
   */
  public boolean isStatus() {
    return status;
  }

  /**
   * Sets the status.
   * 
   * @param newValue
   *          the new status
   */
  public void setStatus(boolean newValue) {
    boolean oldValue = this.status;
    this.status = newValue;
    firePropertyChange("status", oldValue, newValue);
  }

}
