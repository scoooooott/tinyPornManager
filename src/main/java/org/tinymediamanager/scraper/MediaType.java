/*
 * Copyright 2012 - 2014 Manuel Laggner
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

/**
 * The Enum MediaType.
 * 
 * @author Manuel Laggner
 */
public enum MediaType {
  TV_SHOW, TV_EPISODE, MOVIE, MOVIE_SET;

  public static MediaType toMediaType(String id) {
    if (id == null)
      return null;

    id = id.toLowerCase();
    if ("movie".equalsIgnoreCase(id) || "movies".equalsIgnoreCase(id)) {
      return MOVIE;
    }
    if ("movieSet".equalsIgnoreCase(id) || "set".equalsIgnoreCase(id)) {
      return MOVIE_SET;
    }

    if ("tv".equalsIgnoreCase(id) || "tvShow".equalsIgnoreCase(id)) {
      return TV_SHOW;
    }

    if ("episode".equalsIgnoreCase(id) || "tvEpisode".equalsIgnoreCase(id)) {
      return TV_EPISODE;
    }

    return null;
  }
}