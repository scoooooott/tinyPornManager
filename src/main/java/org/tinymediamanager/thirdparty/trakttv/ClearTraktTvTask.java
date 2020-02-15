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

import java.util.ResourceBundle;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmTask;

/**
 * Clear your data from trakt.tv
 * 
 * @author Manuel Laggner
 */
public class ClearTraktTvTask extends TmmTask {
  private static final ResourceBundle BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control());

  private boolean                     clearMovies  = false;
  private boolean                     clearTvShows = false;

  public ClearTraktTvTask(boolean clearMovies, boolean clearTvShows) {
    super(BUNDLE.getString("trakt.clear"), 0, TaskType.BACKGROUND_TASK);
    this.clearMovies = clearMovies;
    this.clearTvShows = clearTvShows;
  }

  @Override
  protected void doInBackground() {
    TraktTv traktTV = TraktTv.getInstance();

    if (clearMovies) {
      publishState(BUNDLE.getString("trakt.clear.movies"), 0);
      traktTV.clearTraktMovies();
    }

    if (clearTvShows) {
      publishState(BUNDLE.getString("trakt.clear.tvshows"), 0);
      traktTV.clearTraktTvShows();
    }
  }
}
