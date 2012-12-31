/*
 * Copyright 2012 Manuel Laggner
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

import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

public class MediaScrapeOptions {

  private MediaSearchResult result;
  private MediaMetadata     metadata;
  private String            imdbId      = "";
  private int               tmdbId      = 0;

  private MediaArtworkType  artworkType = MediaArtworkType.ALL;

  public MediaScrapeOptions() {

  }

  public MediaSearchResult getResult() {
    return result;
  }

  public void setResult(MediaSearchResult result) {
    this.result = result;
  }

  public String getImdbId() {
    return imdbId;
  }

  public int getTmdbId() {
    return tmdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  public MediaArtworkType getArtworkType() {
    return artworkType;
  }

  public void setArtworkType(MediaArtworkType artworkType) {
    this.artworkType = artworkType;
  }

  public MediaMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(MediaMetadata metadata) {
    this.metadata = metadata;
  }

}
