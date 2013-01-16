/*
 * Copyright 2013 Manuel Laggner
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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class MovieSet.
 */
@Entity
public class MovieSet extends AbstractModelObject {

  /** The name. */
  private String      name             = "";

  /** The poster url. */
  private String      posterUrl        = "";

  /** The poster. */
  private String      poster           = "";

  private String      fanartUrl        = "";

  private String      fanart           = "";

  /** The movies. */
  private List<Movie> movies           = new ArrayList<Movie>();

  /** The movies observable. */
  @Transient
  private List<Movie> moviesObservable = ObservableCollections.observableList(movies);

  /**
   * Instantiates a new movie set. Needed for JAXB
   */
  public MovieSet() {
  }

  /**
   * Instantiates a new movie set.
   * 
   * @param name
   *          the name
   */
  public MovieSet(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  /**
   * Sets the observable cast list.
   */
  public void setObservables() {
    moviesObservable = ObservableCollections.observableList(movies);
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
   * @return the posterUrl
   */
  public String getPosterUrl() {
    return posterUrl;
  }

  /**
   * @param posterUrl
   *          the posterUrl to set
   */
  public void setPosterUrl(String newValue) {
    String oldValue = this.posterUrl;
    this.posterUrl = newValue;
    firePropertyChange("posterUrl", oldValue, newValue);
  }

  /**
   * @return the poster
   */
  public String getPoster() {
    return poster;
  }

  /**
   * @param poster
   *          the poster to set
   */
  public void setPoster(String newValue) {
    String oldValue = this.poster;
    this.poster = newValue;
    firePropertyChange("poster", oldValue, newValue);
  }

  public String getFanartUrl() {
    return fanartUrl;
  }

  public String getFanart() {
    return fanart;
  }

  public void setFanartUrl(String newValue) {
    String oldValue = this.fanartUrl;
    this.fanartUrl = newValue;
    firePropertyChange("fanartUrl", oldValue, newValue);
  }

  public void setFanart(String newValue) {
    String oldValue = this.fanart;
    this.fanart = newValue;
    firePropertyChange("fanart", oldValue, newValue);
  }

  /**
   * Sets the name.
   * 
   * @param newValue
   *          the new name
   */
  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  /**
   * Adds the movie.
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    moviesObservable.add(movie);
    saveToDb();
    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("addedMovie", null, movie);
  }

  /**
   * Removes the movie.
   * 
   * @param movie
   *          the movie
   */
  public void removeMovie(Movie movie) {
    moviesObservable.remove(movie);
    saveToDb();
    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("removedMovie", null, movie);
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public List<Movie> getMovies() {
    return moviesObservable;
  }

  /**
   * Removes the all movies.
   */
  public void removeAllMovies() {
    moviesObservable.clear();
    saveToDb();
    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("RemovedAllMovies", null, moviesObservable);
  }

  /**
   * Save to db.
   */
  public synchronized void saveToDb() {
    // update DB
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.persist(this);
    Globals.entityManager.getTransaction().commit();
  }

  /**
   * toString. used for JComboBox in movie editor
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return this.name;
    // return ToStringBuilder.reflectionToString(this,
    // ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * Gets the movie index.
   * 
   * @param movie
   *          the movie
   * @return the movie index
   */
  public int getMovieIndex(Movie movie) {
    return movies.indexOf(movie);
  }
}
