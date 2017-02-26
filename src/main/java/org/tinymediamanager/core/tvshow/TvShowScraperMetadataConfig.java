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

  private boolean title         = true;
  private boolean plot          = true;
  private boolean rating        = true;
  private boolean runtime       = true;
  private boolean year          = true;
  private boolean aired         = true;
  private boolean status        = true;
  private boolean certification = true;
  private boolean cast          = true;
  private boolean genres        = true;
  private boolean artwork       = true;
  private boolean episodes      = true;

  /**
   * default constructor - true for all fields
   */
  public TvShowScraperMetadataConfig() {
  }

  /**
   * custom constructor - set all fields to the given value
   *
   * @param value
   *          the value to set all fields to
   */
  public TvShowScraperMetadataConfig(boolean value) {
    title = value;
    plot = value;
    rating = value;
    runtime = value;
    year = value;
    aired = value;
    status = value;
    certification = value;
    cast = value;
    genres = value;
    artwork = value;
    episodes = value;
  }

  public boolean isTitle() {
    return title;
  }

  public boolean isPlot() {
    return plot;
  }

  public boolean isRating() {
    return rating;
  }

  public boolean isRuntime() {
    return runtime;
  }

  public boolean isYear() {
    return year;
  }

  public boolean isCertification() {
    return certification;
  }

  public boolean isCast() {
    return cast;
  }

  public boolean isGenres() {
    return genres;
  }

  public boolean isArtwork() {
    return artwork;
  }

  public void setTitle(boolean newValue) {
    boolean oldValue = this.title;
    this.title = newValue;
    firePropertyChange("title", oldValue, newValue);
  }

  public void setPlot(boolean newValue) {
    boolean oldValue = this.plot;
    this.plot = newValue;
    firePropertyChange("plot", oldValue, newValue);
  }

  public void setRating(boolean rating) {
    this.rating = rating;
  }

  public void setRuntime(boolean newValue) {
    boolean oldValue = this.runtime;
    this.runtime = newValue;
    firePropertyChange("runtime", oldValue, newValue);
  }

  public void setYear(boolean newValue) {
    boolean oldValue = this.year;
    this.year = newValue;
    firePropertyChange("year", oldValue, newValue);
  }

  public void setCertification(boolean newValue) {
    boolean oldValue = this.certification;
    this.certification = newValue;
    firePropertyChange("certification", oldValue, newValue);
  }

  public void setCast(boolean newValue) {
    boolean oldValue = this.cast;
    this.cast = newValue;
    firePropertyChange("cast", oldValue, newValue);
  }

  public void setGenres(boolean newValue) {
    boolean oldValue = this.genres;
    this.genres = newValue;
    firePropertyChange("genres", oldValue, newValue);
  }

  public void setArtwork(boolean newValue) {
    boolean oldValue = this.artwork;
    this.artwork = newValue;
    firePropertyChange("artwork", oldValue, newValue);
  }

  public boolean isEpisodes() {
    return episodes;
  }

  public void setEpisodes(boolean newValue) {
    boolean oldValue = this.episodes;
    this.episodes = newValue;
    firePropertyChange("episodes", oldValue, newValue);
  }

  public boolean isAired() {
    return aired;
  }

  public void setAired(boolean newValue) {
    boolean oldValue = this.aired;
    this.aired = newValue;
    firePropertyChange("aired", oldValue, newValue);
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean newValue) {
    boolean oldValue = this.status;
    this.status = newValue;
    firePropertyChange("status", oldValue, newValue);
  }
}
