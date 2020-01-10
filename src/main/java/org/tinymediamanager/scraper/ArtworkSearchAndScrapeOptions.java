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

import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * the class ArtworkSearchAndScrapeOptions is used to pass data to the artwork scrapers
 * 
 * @author Manuel Laggner
 */
public class ArtworkSearchAndScrapeOptions extends MediaSearchAndScrapeOptions {
  private MediaArtwork.MediaArtworkType artworkType;
  private MediaArtwork.FanartSizes      fanartSize = MediaArtwork.FanartSizes.LARGE; // default; will be overwritten by tmm settings
  private MediaArtwork.PosterSizes      posterSize = MediaArtwork.PosterSizes.LARGE; // default; will be overwritten by tmm settings

  public ArtworkSearchAndScrapeOptions(MediaType type) {
    super(type);
  }

  protected ArtworkSearchAndScrapeOptions(ArtworkSearchAndScrapeOptions original) {
    super(original);
    this.artworkType = original.artworkType;
  }

  /**
   * get the artwork type
   * 
   * @return the artwork type
   */
  public MediaArtwork.MediaArtworkType getArtworkType() {
    return artworkType;
  }

  /**
   * set the artwork type
   * 
   * @param artworkType
   *          the artwork type
   */
  public void setArtworkType(MediaArtwork.MediaArtworkType artworkType) {
    this.artworkType = artworkType;
  }

  /**
   * get the expected fanart size
   * 
   * @return the expected fanart size
   */
  public MediaArtwork.FanartSizes getFanartSize() {
    return fanartSize;
  }

  /**
   * set the expected fanart size
   * 
   * @param fanartSize
   *          the expected fanart size
   */
  public void setFanartSize(MediaArtwork.FanartSizes fanartSize) {
    this.fanartSize = fanartSize;
  }

  /**
   * get the expected poster size
   * 
   * @return the expected poster size
   */
  public MediaArtwork.PosterSizes getPosterSize() {
    return posterSize;
  }

  /**
   * set the expected poster size
   * 
   * @param posterSize
   *          the expected poster size
   */
  public void setPosterSize(MediaArtwork.PosterSizes posterSize) {
    this.posterSize = posterSize;
  }
}
