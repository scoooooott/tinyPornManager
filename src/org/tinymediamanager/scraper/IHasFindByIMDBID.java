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

/**
 * The Interface IHasFindByIMDBID.
 */
public interface IHasFindByIMDBID {

  /**
   * Gets the metadata for imdb id.
   * 
   * @param imdbid
   *          the imdbid
   * @return the metadata for imdb id
   * @throws Exception
   *           the exception
   */
  public MediaMetadata getMetadataForIMDBId(String imdbid) throws Exception;

  /**
   * Search by imdb id.
   * 
   * @param imdbid
   *          the imdbid
   * @return the media search result
   * @throws Exception
   *           the exception
   */
  public MediaSearchResult searchByImdbId(String imdbid) throws Exception;
}
