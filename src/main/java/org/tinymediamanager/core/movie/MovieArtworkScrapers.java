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
package org.tinymediamanager.core.movie;

/**
 * The enum MovieArtworkScrapers. The sources where we can scrape our artwork from
 * 
 * @author Manuel Laggner
 */
public enum MovieArtworkScrapers {
  TMDB("The Movie Database"), FANART_TV("Fanart.tv");

  private String title;

  private MovieArtworkScrapers(String title) {
    this.title = title;
  }

  public String toString() {
    return this.title;
  }
}
