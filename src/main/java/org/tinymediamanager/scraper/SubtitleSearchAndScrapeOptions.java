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

package org.tinymediamanager.scraper;

import java.io.File;

import org.tinymediamanager.scraper.entities.MediaType;

/**
 * the class SubtitleSearchAndScrapeOptions is used to pass data to the artwork scrapers
 * 
 * @author Manuel Laggner
 */
public class SubtitleSearchAndScrapeOptions extends MediaSearchAndScrapeOptions {
  private int  season  = -1;
  private int  episode = -1;

  private File file;

  public SubtitleSearchAndScrapeOptions(MediaType type) {
    super(type);
  }

  protected SubtitleSearchAndScrapeOptions(SubtitleSearchAndScrapeOptions original) {
    super(original);
    this.season = original.season;
    this.episode = original.episode;
    this.file = original.file;
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
}
