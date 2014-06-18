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
  SD("SD", Arrays.asList("480p")), 
  HD_720("720p", Arrays.asList("HD", "720p")), 
  HD_1080("1080p", Arrays.asList("HD", "1080p"));  // @formatter:on

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

  @Override
  public String toString() {
    return this.displayText;
  }
}
