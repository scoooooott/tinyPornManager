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

package org.tinymediamanager.ui.movies;

import static org.tinymediamanager.core.Constants.TRAKT;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.Converter;
import org.tinymediamanager.scraper.MediaMetadata;

/**
 * The class {@link MovieOtherIdsConverter} is used to display the other ids in a string format
 * 
 * @author Manuel Laggner
 */
public class MovieOtherIdsConverter extends Converter<Map<String, Object>, String> {

  @Override
  public String convertForward(Map<String, Object> arg0) {
    String otherIds = "";

    for (Map.Entry<String, Object> entry : arg0.entrySet()) {
      switch (entry.getKey()) {
        case MediaMetadata.IMDB:
        case MediaMetadata.TMDB:
        case TRAKT:
          // already in UI - skip
          continue;

        case "tmdbId":
        case "imdbId":
        case "traktId":
          // legacy format
          continue;

        case MediaMetadata.TMDB_SET:
          // not needed
          continue;

        default:
          if (StringUtils.isNotBlank(otherIds)) {
            otherIds += "; ";
          }
          otherIds += entry.getKey() + ": " + entry.getValue();
      }
    }

    return otherIds;
  }

  @Override
  public Map<String, Object> convertReverse(String arg0) {
    return null;
  }
}
