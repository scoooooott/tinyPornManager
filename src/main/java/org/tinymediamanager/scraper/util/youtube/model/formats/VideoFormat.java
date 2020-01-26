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
package org.tinymediamanager.scraper.util.youtube.model.formats;

import org.tinymediamanager.scraper.util.youtube.YoutubeHelper;
import org.tinymediamanager.scraper.util.youtube.model.Itag;
import org.tinymediamanager.scraper.util.youtube.model.quality.VideoQuality;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Video Format
 *
 * @author Wolfgang Janes
 */
public class VideoFormat extends Format {

  private final Integer fps;
  private final String  qualityLabel;
  private final Integer width;
  private final Integer height;
  private final VideoQuality videoQuality;

  public VideoFormat(JsonNode json, Itag itag) {
    super(json, itag);
    fps = YoutubeHelper.getInt(json, "fps");
    qualityLabel = YoutubeHelper.getString(json, "qualityLabel");
    if (json.has("size")) {
      String[] split = YoutubeHelper.getString(json, "size").split("x");
      width = Integer.parseInt(split[0]);
      height = Integer.parseInt(split[1]);
    }
    else {
      width = YoutubeHelper.getInt(json, "width");
      height = YoutubeHelper.getInt(json, "height");
    }

    VideoQuality videoQuality = null;
    if (json.has("quality")) {
      try {
        videoQuality = VideoQuality.valueOf(json.get("quality").asText());
      } catch (IllegalArgumentException ignore) {
      }
    }
    this.videoQuality = videoQuality;

  }

  @Override
  public String type() {
    return "video";
  }

  public int fps() {
    return fps;
  }

  public VideoQuality videoQuality() {
    return videoQuality != null ? videoQuality : itag.videoQuality();
  }

  public String qualityLabel() {
    return qualityLabel;
  }

  public Integer width() {
    return width;
  }

  public Integer height() {
    return height;
  }

}
