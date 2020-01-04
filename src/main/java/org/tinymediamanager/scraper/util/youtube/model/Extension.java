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
package org.tinymediamanager.scraper.util.youtube.model;

/**
 * Holds the possible Extension of a Youtube Media File
 *
 * @author Wolfgang Janes
 */
public enum Extension {

  MP4("mp4"),
  WEBM("webm"),
  THREEGP("3gp"),
  FLV("flv"),
  HLS("hls"),
  M4A("m4a"),
  UNKNOWN("UNKNOWN");

  private String text;

  Extension(String text) {
    this.text = text;
  }

  public String getText() {
    return this.text;
  }

  public static Extension getExtension(String text) {
    for (Extension v : Extension.values()) {
      if (v.text.equalsIgnoreCase(text)) {
        return v;
      }
    }
    return null;
  }

}
