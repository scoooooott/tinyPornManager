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

import java.util.HashMap;
import java.util.Map;

/**
 * The Class SearchQuery.
 * 
 * @author Manuel Laggner
 */
public class MediaSearchOptions {

  /**
   * The Enum SearchOption.
   * 
   * @author Manuel Laggner
   */
  public enum SearchParam {
    /** The query. */
    QUERY,
    /** The title. */
    TITLE,
    /** The year. */
    YEAR,
    /** The imdbId. */
    IMDBID,
    /** The tmdbId. */
    TMDBID,
    /** The season. */
    SEASON,
    /** The episode. */
    EPISODE,
  };

  /** The searchoptions. */
  private Map<SearchParam, String> options = new HashMap<SearchParam, String>();

  /** The type. */
  private MediaType                type;

  /**
   * Instantiates a new search query.
   */
  public MediaSearchOptions() {
  }

  /**
   * Instantiates a new search query.
   * 
   * @param type
   *          the type
   * @param title
   *          the title
   */
  public MediaSearchOptions(MediaType type, String title) {
    this(type, SearchParam.TITLE, title);
  }

  /**
   * Instantiates a new search query.
   * 
   * @param type
   *          the type
   * @param field
   *          the field
   * @param value
   *          the value
   */
  public MediaSearchOptions(MediaType type, SearchParam field, String value) {
    this.type = type;
    set(field, value);
  }

  /**
   * Gets the media type.
   * 
   * @return the media type
   */
  public MediaType getMediaType() {
    return type;
  }

  /**
   * Sets the media type.
   * 
   * @param type
   *          the new media type
   */
  public void setMediaType(MediaType type) {
    this.type = type;
  }

  /**
   * Sets the option field.
   * 
   * @param field
   *          the field
   * @param value
   *          the value
   */
  public void set(SearchParam field, String value) {
    options.put(field, value);
  }

  /**
   * Gets a specific field.
   * 
   * @param field
   *          the field
   * @return the string
   */
  public String get(SearchParam field) {
    return options.get(field);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
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
