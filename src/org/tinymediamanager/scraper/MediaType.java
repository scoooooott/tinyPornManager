/*
 * Copyright 2012 - 2013 Manuel Laggner
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

  /** The tv show. */
  TV_SHOW,

  /** The tv episode. */
  TV_EPISODE,

  /** The movie. */
  MOVIE;

  /**
   * To media type.
   * 
   * @param id
   *          the id
   * @return the media type
   */
  public static MediaType toMediaType(String id) {
    if (id == null)
      return null;

    id = id.toLowerCase();
    if ("movie".equalsIgnoreCase(id) || "movies".equalsIgnoreCase(id)) {
      return MOVIE;
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