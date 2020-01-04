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
package org.tinymediamanager.scraper;

/**
 * ENUM for all scrapers
 *
 * @author Myron Boyle
 */
public enum ScraperType {
  MOVIE,
  TV_SHOW,
  MOVIE_SET,
  MOVIE_ARTWORK,
  TV_SHOW_ARTWORK,
  MOVIE_TRAILER,
  TVSHOW_TRAILER,
  SUBTITLE, // TMM Types (representing an interfaces)
  ALBUM,
  ARTIST,
  MUSICVIDEO,
  LIBRARY // XBMC ones (currently w/o TMM interfaces)
}
