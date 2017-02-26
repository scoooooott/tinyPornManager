/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * The enum MovieTrailerQuality
 * 
 * @author Manuel Laggner
 */
public enum MovieTrailerQuality {

  //@formatter:off
  SD("SD", Arrays.asList("SD", "480p", "360p", "225p", "180p", "135p", "90p")), 
  HD_720("720p", Arrays.asList("HD", "720p", "720")), 
  HD_1080("1080p", Arrays.asList("HD", "1080p", "1080"));  
  // @formatter:on

  private String       displayText;
  private List<String> possibleQualities;

  private MovieTrailerQuality(String text, List<String> qualities) {
    this.displayText = text;
    this.possibleQualities = qualities;
  }

  public boolean containsQuality(String quality) {
    if (StringUtils.isBlank(quality)) {
      return false;
    }
    for (String qual : possibleQualities) {
      if (quality.equalsIgnoreCase(qual)) {
        return true;
      }
    }
    return false;
  }

  /**
   * parse out the matching MovieTrailerQuality for the given string
   * 
   * @param quality
   *          the given string
   * @return the found quality or SD as fallback
   */
  public static MovieTrailerQuality getMovieTrailerQuality(String quality) {
    for (MovieTrailerQuality q : MovieTrailerQuality.values()) {
      if (q.containsQuality(quality)) {
        return q;
      }
    }
    return SD;
  }

  @Override
  public String toString() {
    return this.displayText;
  }
}
