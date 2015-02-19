/*
 * Copyright 2012 - 2015 Manuel Laggner
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
 * The Enum MovieNfoNaming.
 * 
 * @author Manuel Laggner
 */
public enum MovieNfoNaming {

  /** [filename].nfo (and/or VIDEO:TS.nfo in root) */
  FILENAME_NFO,
  /** movie.nfo (in root) */
  MOVIE_NFO,
  /**
   * DVD/Bluray style<br>
   * <b>this is the nfo INSIDE the disc FOLDER</b>
   */
  DISC_NFO
}
