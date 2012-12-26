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
package org.tinymediamanager.core;

import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class ScraperMetadataConfig.
 */
@XmlRootElement(name = "ScraperMetadata")
public class ScraperMetadataConfig extends AbstractModelObject {

  /** The title. */
  private boolean title         = true;

  /** The original title. */
  private boolean originalTitle = true;

  /** The tagline. */
  private boolean tagline       = true;

  /** The plot. */
  private boolean plot          = true;

  /** The rating. */
  private boolean rating        = true;

  /** The runtime. */
  private boolean runtime       = true;

  /** The year. */
  private boolean year          = true;

  /** The certification. */
  private boolean certification = true;

  /** The cast. */
  private boolean cast          = true;

  /** The genres. */
  private boolean genres        = true;

  /** The artwork. */
  private boolean artwork       = true;

  /** The trailer. */
  private boolean trailer       = true;

  /**
   * Instantiates a new scraper metadata config.
   */
  public ScraperMetadataConfig() {

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
   * Checks if is original title.
   * 
   * @return true, if is original title
   */
  public boolean isOriginalTitle() {
    return originalTitle;
  }

  /**
   * Checks if is tagline.
   * 
   * @return true, if is tagline
   */
  public boolean isTagline() {
    return tagline;
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
   * Checks if is trailer.
   * 
   * @return true, if is trailer
   */
  public boolean isTrailer() {
    return trailer;
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
   * Sets the original title.
   * 
   * @param newValue
   *          the new original title
   */
  public void setOriginalTitle(boolean newValue) {
    boolean oldValue = this.originalTitle;
    this.originalTitle = newValue;
    firePropertyChange("originalTitle", oldValue, newValue);
  }

  /**
   * Sets the tagline.
   * 
   * @param newValue
   *          the new tagline
   */
  public void setTagline(boolean newValue) {
    boolean oldValue = this.tagline;
    this.tagline = newValue;
    firePropertyChange("tagline", oldValue, newValue);
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
   * Sets the trailer.
   * 
   * @param newValue
   *          the new trailer
   */
  public void setTrailer(boolean newValue) {
    boolean oldValue = this.trailer;
    this.trailer = newValue;
    firePropertyChange("trailer", oldValue, newValue);
  }

}
