/*
 * Copyright 2012-2013 Manuel Laggner
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

import static org.tinymediamanager.core.Constants.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.tinymediamanager.Globals;

/**
 * The Class MediaEntity.
 * 
 * @author Manuel Laggner
 */
@MappedSuperclass
// @Entity
public abstract class MediaEntity extends AbstractModelObject {

  /** The id. */
  @Id
  @GeneratedValue
  protected Long                    id;

  /** The ids to store the ID from several metadataproviders. */
  protected HashMap<String, Object> ids               = new HashMap<String, Object>();

  /** The title. */
  protected String                  title             = "";

  /** The original title. */
  protected String                  originalTitle     = "";

  /** The year. */
  protected String                  year              = "";

  /** The overview. */
  protected String                  plot              = "";

  /** The rating. */
  protected float                   rating            = 0f;

  /** The path. */
  protected String                  path              = "";

  /** The fanart url. */
  protected String                  fanartUrl         = "";

  /** The fanart. */
  protected String                  fanart            = "";

  /** The poster url. */
  protected String                  posterUrl         = "";

  /** The poster. */
  protected String                  poster            = "";

  /** The banner url. */
  protected String                  bannerUrl         = "";

  /** The banner. */
  protected String                  banner            = "";

  /** The date added. */
  protected Date                    dateAdded         = new Date();

  /** The production company. */
  protected String                  productionCompany = "";

  /** The scraped. */
  protected boolean                 scraped           = false;

  /** The duplicate flag. */
  @Transient
  private boolean                   duplicate         = false;

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * Gets the fanart url.
   * 
   * @return the fanart url
   */
  public String getFanartUrl() {
    return fanartUrl;
  }

  /**
   * Gets the path to the fanart.
   * 
   * @return the fanart
   */
  abstract public String getFanart();

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the original title.
   * 
   * @return the original title
   */
  public String getOriginalTitle() {
    return originalTitle;
  }

  /**
   * Gets the plot.
   * 
   * @return the plot
   */
  public String getPlot() {
    return plot;
  }

  /**
   * Gets the path.
   * 
   * @return the path
   */
  public String getPath() {
    return path;
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
   * Gets the path to the poster.
   * 
   * @return the poster
   */
  abstract public String getPoster();

  /**
   * Gets the banner url.
   * 
   * @return the banner url
   */
  public String getBannerUrl() {
    return bannerUrl;
  }

  /**
   * Gets the banner.
   * 
   * @return the banner
   */
  abstract public String getBanner();

  /**
   * Gets the rating.
   * 
   * @return the rating
   */
  public float getRating() {
    return rating;
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
   * Sets the id.
   * 
   * @param id
   *          the new id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Sets the title.
   * 
   * @param newValue
   *          the new title
   */
  public void setTitle(String newValue) {
    String oldValue = title;
    title = newValue;
    firePropertyChange(TITLE, oldValue, newValue);
  }

  /**
   * Sets the original title.
   * 
   * @param newValue
   *          the new original title
   */
  public void setOriginalTitle(String newValue) {
    String oldValue = originalTitle;
    originalTitle = newValue;
    firePropertyChange(ORIGINAL_TITLE, oldValue, newValue);
  }

  /**
   * Sets the plot.
   * 
   * @param newValue
   *          the new plot
   */
  public void setPlot(String newValue) {
    String oldValue = plot;
    plot = newValue;
    firePropertyChange(PLOT, oldValue, newValue);
  }

  /**
   * Sets the path.
   * 
   * @param newValue
   *          the new path
   */
  public void setPath(String newValue) {
    String oldValue = path;
    path = newValue;
    firePropertyChange(PATH, oldValue, newValue);
  }

  /**
   * Sets the rating.
   * 
   * @param newValue
   *          the new rating
   */
  public void setRating(float newValue) {
    float oldValue = rating;
    rating = newValue;
    firePropertyChange(RATING, oldValue, newValue);
  }

  /**
   * Sets the year.
   * 
   * @param newValue
   *          the new year
   */
  public void setYear(String newValue) {
    String oldValue = year;
    year = newValue;
    firePropertyChange(YEAR, oldValue, newValue);
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
    firePropertyChange(POSTER_URL, oldValue, newValue);
  }

  /**
   * Sets the poster.
   * 
   * @param poster
   *          the new poster
   */
  abstract public void setPoster(String poster);

  /**
   * Sets the banner url.
   * 
   * @param newValue
   *          the new banner url
   */
  public void setBannerUrl(String newValue) {
    String oldValue = bannerUrl;
    bannerUrl = newValue;
    firePropertyChange(BANNER_URL, oldValue, newValue);
  }

  /**
   * Sets the banner.
   * 
   * @param banner
   *          the new banner
   */
  abstract public void setBanner(String banner);

  /**
   * Sets the fanart url.
   * 
   * @param newValue
   *          the new fanart url
   */
  public void setFanartUrl(String newValue) {
    String oldValue = fanartUrl;
    fanartUrl = newValue;
    firePropertyChange(FANART_URL, oldValue, newValue);
  }

  /**
   * Sets the fanart.
   * 
   * @param fanart
   *          the new fanart
   */
  abstract public void setFanart(String fanart);

  /**
   * Gets the date added.
   * 
   * @return the date added
   */
  public Date getDateAdded() {
    return dateAdded;
  }

  /**
   * Gets the date added as string.
   * 
   * @return the date added as string
   */
  public String getDateAddedAsString() {
    if (dateAdded == null) {
      return "";
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    return sdf.format(dateAdded);
  }

  /**
   * Sets the date added.
   * 
   * @param newValue
   *          the new date added
   */
  public void setDateAdded(Date newValue) {
    Date oldValue = this.dateAdded;
    this.dateAdded = newValue;
    firePropertyChange(DATE_ADDED, oldValue, newValue);
    firePropertyChange(DATE_ADDED_AS_STRING, oldValue, newValue);
  }

  /**
   * Gets the production company.
   * 
   * @return the production company
   */
  public String getProductionCompany() {
    return productionCompany;
  }

  /**
   * Sets the production company.
   * 
   * @param newValue
   *          the new production company
   */
  public void setProductionCompany(String newValue) {
    String oldValue = this.productionCompany;
    this.productionCompany = newValue;
    firePropertyChange(PRODUCTION_COMPANY, oldValue, newValue);
  }

  /**
   * Sets the scraped.
   * 
   * @param newValue
   *          the new scraped
   */
  protected void setScraped(boolean newValue) {
    this.scraped = newValue;
    firePropertyChange(SCRAPED, false, newValue);
  }

  /**
   * checks if this movie has been scraped.
   * 
   * @return isScraped
   */
  public boolean isScraped() {
    return scraped;
  }

  /**
   * Sets the duplicate.
   */
  public void setDuplicate() {
    this.duplicate = true;
  }

  /**
   * Clear duplicate.
   */
  public void clearDuplicate() {
    this.duplicate = false;
  }

  /**
   * Checks if is duplicate.
   * 
   * @return true, if is duplicate
   */
  public boolean isDuplicate() {
    return this.duplicate;
  }

  /**
   * Sets the id.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void setId(String key, Object value) {
    ids.put(key, value);
  }

  /**
   * Gets the id.
   * 
   * @param key
   *          the key
   * @return the id
   */
  public Object getId(String key) {
    return ids.get(key);
  }

  /**
   * Save to db.
   */
  public synchronized void saveToDb() {
    // update DB
    synchronized (Globals.entityManager) {
      Globals.entityManager.getTransaction().begin();
      Globals.entityManager.persist(this);
      Globals.entityManager.getTransaction().commit();
    }
  }
}
