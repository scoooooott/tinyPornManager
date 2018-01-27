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
package org.tinymediamanager.scraper;

import java.io.File;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * The class SubtitleSearchOptions. Pass arguments to the scraper searches
 * 
 * @author Manuel Laggner
 * @since 2.0
 */
public class SubtitleSearchOptions extends MediaSearchOptions {
  private int  season  = -1;
  private int  episode = -1;

  private File file;

  public SubtitleSearchOptions() {
    super(MediaType.SUBTITLE);
  }

  public SubtitleSearchOptions(File file) {
    super(MediaType.SUBTITLE);
    this.file = file;
  }

  public SubtitleSearchOptions(String query) {
    super(MediaType.SUBTITLE, query);
  }

  public SubtitleSearchOptions(File file, String query) {
    super(MediaType.SUBTITLE, query);
    this.file = file;
  }

  /**
   * Get the season
   *
   * @return the season or -1 if none set
   */
  public int getSeason() {
    return season;
  }

  /**
   * Set the season
   *
   * @param season
   *          the season
   */
  public void setSeason(int season) {
    this.season = season;
  }

  /**
   * Get the episode
   *
   * @return the episode or -1 if none set
   */
  public int getEpisode() {
    return episode;
  }

  /**
   * Set the episode
   *
   * @param episode
   *          the episode
   */
  public void setEpisode(int episode) {
    this.episode = episode;
  }

  /**
   * Get the file for subtitle scraping
   *
   * @return the file
   */
  public File getFile() {
    return file;
  }

  /**
   * Set the file for subtitle scraping
   * 
   * @param file
   *          the file for creating hashes
   */
  public void setFile(File file) {
    this.file = file;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
