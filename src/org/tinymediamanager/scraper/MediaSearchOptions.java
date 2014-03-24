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

import java.util.HashMap;
import java.util.Map;

/**
 * The Class SearchQuery. Pass arguments to the scraper searches
 * 
 * @author Manuel Laggner
 */
public class MediaSearchOptions {
  public enum SearchParam {
    QUERY, TITLE, YEAR, IMDBID, TMDBID, SEASON, EPISODE, LANGUAGE, COUNTRY, COLLECTION_INFO, IMDB_FOREIGN_LANGUAGE
  };

  private Map<SearchParam, String> options = new HashMap<SearchParam, String>();
  private MediaType                type;

  public MediaSearchOptions(MediaType type) {
    this.type = type;
  }

  public MediaSearchOptions(MediaType type, String title) {
    this(type, SearchParam.TITLE, title);
  }

  public MediaSearchOptions(MediaType type, SearchParam field, String value) {
    this.type = type;
    set(field, value);
  }

  public MediaType getMediaType() {
    return type;
  }

  public void setMediaType(MediaType type) {
    this.type = type;
  }

  public void set(SearchParam field, String value) {
    options.put(field, value);
  }

  public String get(SearchParam field) {
    return options.get(field);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("SearchQuery; Type: ").append(type.name()).append("; ");
    ;
    for (SearchParam k : options.keySet()) {
      sb.append(k.name()).append(":").append(options.get(k)).append(";");
    }
    return sb.toString();
  }
}
