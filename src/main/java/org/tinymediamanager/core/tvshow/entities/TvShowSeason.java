/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.core.tvshow.entities;

import static org.tinymediamanager.core.Constants.ADDED_EPISODE;
import static org.tinymediamanager.core.Constants.BANNER;
import static org.tinymediamanager.core.Constants.BANNER_URL;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.POSTER;
import static org.tinymediamanager.core.Constants.POSTER_URL;
import static org.tinymediamanager.core.Constants.REMOVED_EPISODE;
import static org.tinymediamanager.core.Constants.THUMB;
import static org.tinymediamanager.core.Constants.THUMB_URL;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

/**
 * The Class TvShowSeason.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeason extends AbstractModelObject implements Comparable<TvShowSeason> {
  private int                    season      = -1;
  private TvShow                 tvShow;
  private List<TvShowEpisode>    episodes    = new CopyOnWriteArrayList<>();
  private Date                   lastWatched = null;
  private PropertyChangeListener listener;

  public TvShowSeason(int season, TvShow tvShow) {
    this.season = season;
    this.tvShow = tvShow;
    listener = evt -> {
      if (evt.getSource() instanceof TvShowEpisode && MEDIA_FILES.equals(evt.getPropertyName())) {
        firePropertyChange(MEDIA_FILES, null, evt.getNewValue());
      }
    };
  }

  public int getSeason() {
    return season;
  }

  public TvShow getTvShow() {
    return tvShow;
  }

  public void addEpisode(TvShowEpisode episode) {
    // when adding a new episode, check:
    for (TvShowEpisode e : episodes) {
      // - if that is a dummy episode; do not add it if a the real episode is available
      if (episode.isDummy() && episode.getEpisode() == e.getEpisode() && episode.getSeason() == e.getSeason()) {
        return;
      }
      // - if that is a real episode; remove the corresponding dummy episode if available
      if (!episode.isDummy() && e.isDummy() && episode.getEpisode() == e.getEpisode() && episode.getSeason() == e.getSeason()) {
        tvShow.removeEpisode(e);
      }
    }

    episodes.add(episode);
    episodes.sort(TvShowEpisode::compareTo);
    episode.addPropertyChangeListener(listener);
    firePropertyChange(ADDED_EPISODE, null, episodes);
  }

  public void removeEpisode(TvShowEpisode episode) {
    episodes.remove(episode);
    episode.removePropertyChangeListener(listener);
    firePropertyChange(REMOVED_EPISODE, null, episodes);
  }

  public List<TvShowEpisode> getEpisodes() {
    List<TvShowEpisode> episodes = new ArrayList<>();
    for (TvShowEpisode episode : this.episodes) {
      if (!episode.isDummy()) {
        episodes.add(episode);
      }
    }
    return episodes;
  }

  /**
   * Checks if all episodes are watched.
   *
   * @return true, if all episodes are watched
   */
  public boolean isWatched() {
    boolean watched = true;

    for (TvShowEpisode episode : episodes) {
      if (!episode.isWatched()) {
        watched = false;
        break;
      }
    }

    return watched;
  }

  public boolean isDummy() {
    for (TvShowEpisode episode : episodes) {
      if (!episode.isDummy()) {
        return false;
      }
    }
    return true;
  }

  public List<TvShowEpisode> getEpisodesForDisplay() {
    return episodes;
  }

  public void setArtwork(Path newValue, MediaArtworkType artworkType) {
    String oldValue = getArtworkFilename(artworkType);
    tvShow.setSeasonArtwork(season, artworkType, newValue);

    switch (artworkType) {
      case SEASON_POSTER:
        firePropertyChange(POSTER, oldValue, newValue);
        break;

      case SEASON_BANNER:
        firePropertyChange(BANNER, oldValue, newValue);
        break;

      case SEASON_THUMB:
        firePropertyChange(THUMB, oldValue, newValue);
        break;

      default:
        return;
    }

    for (TvShowEpisode episode : episodes) {
      episode.setSeasonArtworkChanged(artworkType);
    }
  }

  public void clearArtwork(MediaArtworkType artworkType) {
    tvShow.clearSeasonArtwork(season, artworkType);

    switch (artworkType) {
      case SEASON_POSTER:
        firePropertyChange(POSTER, null, "");
        break;

      case SEASON_BANNER:
        firePropertyChange(BANNER, null, "");
        break;

      case SEASON_THUMB:
        firePropertyChange(THUMB, null, "");
        break;
    }
  }

  public String getArtworkFilename(MediaArtworkType type) {
    return tvShow.getSeasonArtwork(season, type);
  }

  public Dimension getArtworkSize(MediaArtworkType type) {
    return tvShow.getSeasonArtworkSize(season, type);
  }

  public void setArtworkUrl(String newValue, MediaArtworkType artworkType) {
    String oldValue = getArtworkUrl(artworkType);
    tvShow.setSeasonArtworkUrl(season, newValue, artworkType);

    switch (artworkType) {
      case SEASON_POSTER:
        firePropertyChange(POSTER_URL, oldValue, newValue);
        break;

      case SEASON_BANNER:
        firePropertyChange(BANNER_URL, oldValue, newValue);
        break;

      case SEASON_THUMB:
        firePropertyChange(THUMB_URL, oldValue, newValue);
        break;

      default:
        return;
    }

    for (TvShowEpisode episode : episodes) {
      episode.setSeasonArtworkChanged(artworkType);
    }
  }

  public String getArtworkUrl(MediaArtworkType type) {
    return tvShow.getSeasonArtworkUrl(season, type);
  }

  public void downloadArtwork(MediaArtworkType artworkType) {
    tvShow.downloadSeasonArtwork(season, artworkType);
  }

  public List<MediaFile> getMediaFiles() {
    ArrayList<MediaFile> mfs = new ArrayList<>();
    Set<MediaFile> unique = new LinkedHashSet<>(mfs);
      for (TvShowEpisode episode : episodes) {
          unique.addAll(new ArrayList<>(episode.getMediaFiles()));
      }
    mfs.addAll(unique);
    return mfs;
  }

  public List<MediaFile> getMediaFiles(MediaFileType type) {
    ArrayList<MediaFile> mfs = new ArrayList<>();
    Set<MediaFile> unique = new LinkedHashSet<>(mfs);
      for (TvShowEpisode episode : episodes) {
          unique.addAll(episode.getMediaFiles(type));
      }
    mfs.addAll(unique);
    return mfs;
  }

  public boolean isNewlyAdded() {
    for (TvShowEpisode episode : episodes) {
      if (episode.isNewlyAdded()) {
        return true;
      }
    }
    return false;
  }

  public Date getLastWatched() {
    return lastWatched;
  }

  public void setLastWatched(Date lastWatched) {
    this.lastWatched = lastWatched;
  }

  @Override
  public int compareTo(TvShowSeason o) {
    if (getTvShow() != o.getTvShow()) {
      return getTvShow().getTitle().compareTo(o.getTvShow().getTitle());
    }
    return Integer.compare(getSeason(), o.getSeason());
  }
}
