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
 */
public class SearchQuery {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /*
   * we are going to add IMDBID and ID to the search query remove metadata id
   * and series id since they are basically the same provider will no longer
   * have search by id, just a searchQUery, and then the provider can determine
   * how to search, etc.
   */
  /**
   * The Enum Field.
   */
  public enum Field {
    /** The query. */
    QUERY,
    /** The raw title. */
    RAW_TITLE,
    /** The clean title. */
    CLEAN_TITLE,
    /** The season. */
    SEASON,
    /** The episode. */
    EPISODE,
    /** The disc. */
    DISC,
    /** The episode title. */
    EPISODE_TITLE,
    /** The episode date. */
    EPISODE_DATE,
    /** The year. */
    YEAR,
    /** The file. */
    FILE,
    /** The url. */
    URL,
    /** The provider. */
    PROVIDER,
    /** The id. */
    ID
  };

  /** The fields. */
  private Map<Field, String> fields = new HashMap<Field, String>();

  /** The type. */
  private MediaType          type   = MediaType.MOVIE;

  /**
   * Instantiates a new search query.
   */
  public SearchQuery() {
    // empty
  }

  /**
   * Instantiates a new search query.
   * 
   * @param query
   *          the query
   */
  public SearchQuery(SearchQuery query) {
    this.type = query.getMediaType();
    for (Field f : query.fields.keySet()) {
      fields.put(f, query.get(f));
    }
  }

  /**
   * Instantiates a new search query.
   * 
   * @param type
   *          the type
   * @param title
   *          the title
   */
  public SearchQuery(MediaType type, String title) {
    this(type, Field.RAW_TITLE, title);
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
  public SearchQuery(MediaType type, Field field, String value) {
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
   *          the type
   * @return the search query
   */
  public SearchQuery setMediaType(MediaType type) {
    this.type = type;
    return this;
  }

  /**
   * Sets the.
   * 
   * @param field
   *          the field
   * @param value
   *          the value
   * @return the search query
   */
  public SearchQuery set(Field field, String value) {
    fields.put(field, value);
    return this;
  }

  /**
   * Gets the.
   * 
   * @param field
   *          the field
   * @return the string
   */
  public String get(Field field) {
    return fields.get(field);
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
    for (Field k : fields.keySet()) {
      sb.append(k.name()).append(":").append(fields.get(k)).append(";");
    }
    return sb.toString();
  }

  /**
   * Copy.
   * 
   * @param q
   *          the q
   * @return the search query
   */
  public static SearchQuery copy(SearchQuery q) {
    return new SearchQuery(q);
  }
}
