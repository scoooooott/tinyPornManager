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
package org.tinymediamanager.core.movie.connector;

/**
 * The Enum MovieConnectors.
 * 
 * @author Manuel Laggner
 */
public enum MovieConnectors {

  /** The xbmc. */
  XBMC("XBMC"),
  /** The mp. */
  MP("MediaPortal");

  /** The title. */
  private String title;

  /**
   * Instantiates a new movie connectors.
   * 
   * @param title
   *          the title
   */
  private MovieConnectors(String title) {
    this.title = title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  public String toString() {
    return this.title;
  }

}
