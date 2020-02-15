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
package org.tinymediamanager.ui.tvshows;

import java.util.ResourceBundle;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;

/**
 * The class TvShowEpisodeChooserModel
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeChooserModel extends AbstractModelObject {
  private static final ResourceBundle           BUNDLE      = ResourceBundle.getBundle("messages", new UTF8Control());
  public static final TvShowEpisodeChooserModel emptyResult = new TvShowEpisodeChooserModel();

  private MediaScraper                          mediaScraper;
  private MediaMetadata                         mediaMetadata;
  private String                                title       = "";
  private String                                overview    = "";
  private int                                   season      = -1;
  private int                                   episode     = -1;

  public TvShowEpisodeChooserModel(MediaScraper mediaScraper, MediaMetadata episode) {
    this.mediaScraper = mediaScraper;
    this.mediaMetadata = episode;

    setTitle(episode.getTitle());
    setOverview(mediaMetadata.getPlot());
    setSeason(mediaMetadata.getSeasonNumber());
    setEpisode(mediaMetadata.getEpisodeNumber());
  }

  private TvShowEpisodeChooserModel() {
    setTitle(BUNDLE.getString("chooser.nothingfound"));
  }

  public void setTitle(String title) {
    String oldValue = this.title;
    this.title = title;
    firePropertyChange("title", oldValue, title);
  }

  public void setOverview(String overview) {
    String oldValue = this.overview;
    this.overview = overview;
    firePropertyChange("overview", oldValue, overview);
  }

  public void setSeason(int season) {
    int oldValue = this.season;
    this.season = season;
    firePropertyChange("season", oldValue, season);
  }

  public void setEpisode(int episode) {
    int oldValue = this.episode;
    this.episode = episode;
    firePropertyChange("episode", oldValue, episode);
  }

  public String getTitle() {
    return title;
  }

  public String getOverview() {
    return overview;
  }

  public int getSeason() {
    return season;
  }

  public int getEpisode() {
    return episode;
  }

  public MediaMetadata getMediaMetadata() {
    return mediaMetadata;
  }
}
