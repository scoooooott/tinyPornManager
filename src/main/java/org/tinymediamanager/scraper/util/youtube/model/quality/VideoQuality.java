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
package org.tinymediamanager.scraper.util.youtube.model.quality;

/**
 * {@link VideoQuality} represents groups of video qualities
 */
public enum VideoQuality {

  UNKNOWN("UNKNOWN"),
  HIGHRES("3072p"),
  HD_2880("2880p"),
  HD_2160("2160p"),
  HD_1440("1440p"),
  HD_1080("1080p"),
  HD_720("720p"),
  LARGE("480p"),
  MEDIUM("360p"),
  SMALL("240p"),
  TINY("144p"),
  NO_VIDEO("NO_VIDEO");

  private String text;

  VideoQuality(String text) {
    this.text = text;
  }

  public String getText() {
    return this.text;
  }

  public static VideoQuality getVideoQuality(String text) {
    for (VideoQuality v : VideoQuality.values()) {
      if (v.text.equalsIgnoreCase(text)) {
        return v;
      }
    }
    throw new IllegalArgumentException("No constant with text: " + text + "found");
  }

}
