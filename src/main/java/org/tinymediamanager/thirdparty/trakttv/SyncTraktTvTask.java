/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.thirdparty.trakttv;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.tvshow.entities.TvShow;

/**
 * Sync your data with trakt.tv
 * 
 * @author Manuel Laggner
 */
public class SyncTraktTvTask extends TmmTask {
  private static final ResourceBundle BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control());

  private boolean                     syncMovies         = false;
  private boolean                     syncMoviesWatched  = false;
  private boolean                     syncTvShows        = false;
  private boolean                     syncTvShowsWatched = false;
  private List<Movie>                 movies             = new ArrayList<>();
  private List<TvShow>                tvShows            = new ArrayList<>();

  public SyncTraktTvTask(boolean syncMovies, boolean syncMoviesWatched, boolean syncTvShow, boolean syncTvShowWatched) {
    super(BUNDLE.getString("trakt.sync"), 0, TaskType.BACKGROUND_TASK);
    this.syncMovies = syncMovies;
    this.syncMoviesWatched = syncMoviesWatched;
    this.syncTvShows = syncTvShow;
    this.syncTvShowsWatched = syncTvShowWatched;
  }

  public SyncTraktTvTask(List<Movie> movies, List<TvShow> tvShows) {
    super(BUNDLE.getString("trakt.sync"), 0, TaskType.BACKGROUND_TASK);
    if (movies != null && !movies.isEmpty()) {
      this.syncMovies = true;
      this.syncMoviesWatched = true;
      this.movies.addAll(movies);
    }
    if (tvShows != null && !tvShows.isEmpty()) {
      this.syncTvShows = true;
      this.syncTvShowsWatched = true;
      this.tvShows.addAll(tvShows);
    }
  }

  @Override
  protected void doInBackground() {
    TraktTv traktTV = TraktTv.getInstance();

    if (syncMovies) {
      publishState(BUNDLE.getString("trakt.sync.movie"), 0);
      if (movies.isEmpty()) {
        traktTV.syncTraktMovieCollection();
      }
      else {
        traktTV.syncTraktMovieCollection(movies);
      }
    }

    if (syncMoviesWatched) {
      publishState(BUNDLE.getString("trakt.sync.moviewatched"), 0);
      if (movies.isEmpty()) {
        traktTV.syncTraktMovieWatched();
      }
      else {
        traktTV.syncTraktMovieWatched(movies);
      }
    }

    if (syncTvShows) {
      publishState(BUNDLE.getString("trakt.sync.tvshow"), 0);
      if (tvShows.isEmpty()) {
        traktTV.syncTraktTvShowCollection();
      }
      else {
        traktTV.syncTraktTvShowCollection(tvShows);
      }
    }

    if (syncTvShowsWatched) {
      publishState(BUNDLE.getString("trakt.sync.tvshowwatched"), 0);
      if (tvShows.isEmpty()) {
        traktTV.syncTraktTvShowWatched();
      }
      else {
        traktTV.syncTraktTvShowWatched(tvShows);
      }
    }
  }
}
