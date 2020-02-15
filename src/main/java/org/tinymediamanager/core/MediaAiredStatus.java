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

package org.tinymediamanager.core;

import java.util.ResourceBundle;

/**
 * this enum is used to set different aired states
 *
 * @author Manuel Laggner
 */
public enum MediaAiredStatus {
  UNKNOWN("Unknown", new String[] { "" }),
  CONTINUING("Continuing", new String[] { "Continuing", "returning series" }),
  ENDED("Ended", new String[] { "Ended" });

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());
  private String                      name;
  private String[]                    possibleNotations;

  MediaAiredStatus(String name, String[] possibleNotations) {
    this.name = name;
    this.possibleNotations = possibleNotations;
  }

  /**
   * find the corresponding status for the given text
   * 
   * @param text
   *          the text to find a status for
   * @return the status or UNKNOWN
   */
  public static MediaAiredStatus findAiredStatus(String text) {
    for (MediaAiredStatus status : MediaAiredStatus.values()) {
      for (String notation : status.possibleNotations) {
        if (notation.equalsIgnoreCase(text)) {
          return status;
        }
      }
    }

    return UNKNOWN;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    try {
      return BUNDLE.getString("MediaAiredStatus." + name());
    }
    catch (Exception ignored) {
      // fallback
      return this.name;
    }
  }
}
