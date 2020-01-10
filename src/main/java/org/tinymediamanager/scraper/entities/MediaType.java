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
package org.tinymediamanager.scraper.entities;

import java.util.Locale;

/**
 * The enum MediaType. This enum represents all type of media tinyMediaManager understands
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public enum MediaType {
  TV_SHOW,
  TV_EPISODE,
  MOVIE,
  MOVIE_SET,
  SUBTITLE;

  public static MediaType toMediaType(String id) {
    if (id == null) {
      return null;
    }

    id = id.toLowerCase(Locale.ROOT);
    if ("movie".equalsIgnoreCase(id) || "movies".equalsIgnoreCase(id)) {
      return MOVIE;
    }
    if ("movieSet".equalsIgnoreCase(id) || "set".equalsIgnoreCase(id) || "movie_set".equalsIgnoreCase(id)) {
      return MOVIE_SET;
    }

    if ("tv".equalsIgnoreCase(id) || "tvShow".equalsIgnoreCase(id) || "tv_show".equalsIgnoreCase(id)) {
      return TV_SHOW;
    }

    if ("episode".equalsIgnoreCase(id) || "tvEpisode".equalsIgnoreCase(id) || "tv_episode".equalsIgnoreCase(id)) {
      return TV_EPISODE;
    }

    return null;
  }
}
