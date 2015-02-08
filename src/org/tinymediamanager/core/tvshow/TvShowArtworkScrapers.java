/*
 * Copyright 2012 - 2015 Manuel Laggner
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

/**
 * The Enum TvShowArtworkScrapers.
 * 
 * @author Manuel Laggner
 */
public enum TvShowArtworkScrapers {
  TVDB("The TV Database"), ANIDB("AniDB"), FANART_TV("Fanart.tv");

  private String title;

  /**
   * Instantiates a new movie scrapers.
   * 
   * @param title
   *          the title
   */
  private TvShowArtworkScrapers(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return this.title;
  }
}
