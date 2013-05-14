/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import static org.tinymediamanager.core.Constants.*;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class TvShowSeason.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeason extends AbstractModelObject {
  /** The season. */
  private int                 season   = -1;

  /** The tv show. */
  private TvShow              tvShow;

  /** The episodes. */
  private List<TvShowEpisode> episodes = ObservableCollections.observableList(new ArrayList<TvShowEpisode>());

  /**
   * Instantiates a new tv show season.
   * 
   * @param season
   *          the season
   * @param tvShow
   *          the tv show
   */
  public TvShowSeason(int season, TvShow tvShow) {
    this.season = season;
    this.tvShow = tvShow;
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
   * Gets the tv show.
   * 
   * @return the tv show
   */
  public TvShow getTvShow() {
    return tvShow;
  }

  /**
   * Adds the episode.
   * 
   * @param episode
   *          the episode
   */
  public void addEpisode(TvShowEpisode episode) {
    episodes.add(episode);
    firePropertyChange(ADDED_EPISODE, null, episodes);
  }

  /**
   * Gets the episodes.
   * 
   * @return the episodes
   */
  public List<TvShowEpisode> getEpisodes() {
    return episodes;
  }

  /**
   * Sets the poster.
   * 
   * @param newValue
   *          the new poster
   */
  public void setPoster(String newValue) {
    String oldValue = tvShow.getSeasonPoster(season);
    tvShow.setSeasonPoster(season, newValue);
    firePropertyChange(POSTER, oldValue, newValue);
  }

  /**
   * Gets the poster.
   * 
   * @return the poster
   */
  public String getPoster() {
    return tvShow.getSeasonPoster(season);
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  public void setPosterUrl(String newValue) {
    String oldValue = tvShow.getSeasonPosterUrl(season);
    tvShow.setSeasonPosterUrl(season, newValue);
    firePropertyChange(POSTER_URL, oldValue, newValue);
  }

  /**
   * Gets the poster url.
   * 
   * @return the poster url
   */
  public String getPosterUrl() {
    return tvShow.getSeasonPosterUrl(season);
  }
}
