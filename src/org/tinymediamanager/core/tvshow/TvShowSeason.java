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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;

/**
 * The Class TvShowSeason.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeason extends AbstractModelObject {
  private int                    season   = -1;
  private TvShow                 tvShow;
  private List<TvShowEpisode>    episodes = new ArrayList<TvShowEpisode>(0);
  private PropertyChangeListener listener;

  public TvShowSeason(int season, TvShow tvShow) {
    this.season = season;
    this.tvShow = tvShow;
    listener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof TvShowEpisode && MEDIA_FILES.equals(evt.getPropertyName())) {
          firePropertyChange(MEDIA_FILES, null, evt.getNewValue());
        }
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
    episodes.add(episode);
    episode.addPropertyChangeListener(listener);
    firePropertyChange(ADDED_EPISODE, null, episodes);
  }

  public List<TvShowEpisode> getEpisodes() {
    return episodes;
  }

  public void setPoster(File newValue) {
    String oldValue = tvShow.getSeasonPoster(season);
    tvShow.setSeasonPoster(season, newValue);
    firePropertyChange(POSTER, oldValue, newValue);
    for (TvShowEpisode episode : episodes) {
      episode.setPosterChanged();
    }
  }

  public void clearPoster() {
    tvShow.clearSeasonPoster(season);
    firePropertyChange(POSTER, null, "");
  }

  public String getPoster() {
    return tvShow.getSeasonPoster(season);
  }

  public Dimension getPosterSize() {
    return tvShow.getSeasonPosterSize(season);
  }

  public void setPosterUrl(String newValue) {
    String oldValue = tvShow.getSeasonPosterUrl(season);
    tvShow.setSeasonPosterUrl(season, newValue);
    firePropertyChange(POSTER_URL, oldValue, newValue);
  }

  public String getPosterUrl() {
    return tvShow.getSeasonPosterUrl(season);
  }

  public List<MediaFile> getMediaFiles() {
    ArrayList<MediaFile> mfs = new ArrayList<MediaFile>();
    Set<MediaFile> unique = new LinkedHashSet<MediaFile>(mfs);
    for (int i = 0; i < episodes.size(); i++) {
      unique.addAll(new ArrayList<MediaFile>(episodes.get(i).getMediaFiles()));
    }
    mfs.addAll(unique);
    return mfs;
  }

  public List<MediaFile> getMediaFiles(MediaFileType type) {
    ArrayList<MediaFile> mfs = new ArrayList<MediaFile>();
    Set<MediaFile> unique = new LinkedHashSet<MediaFile>(mfs);
    for (int i = 0; i < episodes.size(); i++) {
      unique.addAll(episodes.get(i).getMediaFiles(type));
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
}
