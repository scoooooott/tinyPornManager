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
package org.tinymediamanager.core.movie.connector;

import java.nio.file.Path;

/**
 * The Enum MovieConnectors.
 * 
 * @author Manuel Laggner
 */
public enum MovieConnectors {
  KODI("Kodi"),
  XBMC("Kodi / XBMC < v16"),
  MP("MediaPortal (legacy)"),
  MP_MP("MediaPortal - Moving Pictures"),
  MP_MV("MediaPortal - MyVideo");

  private String title;

  MovieConnectors(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return this.title;
  }

  /**
   * checks, if current NFO file is a valid XML<br>
   * (by casting to all known XML formats)
   * 
   * @param nfo
   *          the path to the NFO
   * @return true/false
   */
  public static boolean isValidNFO(Path nfo) {
    try {
      MovieNfoParser movieNfoParser = MovieNfoParser.parseNfo(nfo);
      return movieNfoParser.isValidNfo();
    }
    catch (Exception e) {
      return false;
    }
  }
}
