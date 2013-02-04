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

package org.tinymediamanager.core.movie;

/**
 * The Enum MoviePosterNaming.
 */
public enum MoviePosterNaming {

  /** [moviename]-poster.png */
  MOVIENAME_POSTER_PNG,
  /** [moviename]-poster.jpg */
  MOVIENAME_POSTER_JPG,
  /** [moviename]-poster.tbn */
  @Deprecated
  MOVIENAME_POSTER_TBN,

  /** [filename].png */
  FILENAME_PNG,
  /** [filename].jpg */
  FILENAME_JPG,
  /** [filename].tbn */
  @Deprecated
  FILENAME_TBN,

  /** [filename]-poster.png */
  FILENAME_POSTER_PNG,
  /** [filename]-poster.jpg */
  FILENAME_POSTER_JPG,
  /** [filename]-poster.tbn */
  @Deprecated
  FILENAME_POSTER_TBN,

  /** movie.png */
  MOVIE_PNG,
  /** movie.jpg */
  MOVIE_JPG,
  /** movie.tbn */
  @Deprecated
  MOVIE_TBN,

  /** poster.png */
  POSTER_PNG,
  /** poster.jpg */
  POSTER_JPG,
  /** poster.tbn */
  @Deprecated
  POSTER_TBN,

  /** folder.png */
  FOLDER_PNG,
  /** folder.jpg */
  FOLDER_JPG,
  /** folder.tbn */
  @Deprecated
  FOLDER_TBN;
}
