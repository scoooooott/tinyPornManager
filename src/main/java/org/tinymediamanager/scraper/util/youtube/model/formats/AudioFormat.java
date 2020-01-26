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

import com.fasterxml.jackson.databind.JsonNode;
import org.tinymediamanager.scraper.util.youtube.YoutubeHelper;
import org.tinymediamanager.scraper.util.youtube.model.Itag;
import org.tinymediamanager.scraper.util.youtube.model.quality.AudioQuality;

/**
 * AudioFormat
 *
 * @author Wolfgang Janes
 */
public class AudioFormat extends Format {

  private final Integer audioSampleRate;
  private final Integer averageBitrate;
  private final AudioQuality audioQuality;

  public AudioFormat(JsonNode json, Itag itag) {
    super(json, itag);
    audioSampleRate = YoutubeHelper.getInt(json, "getAudioSampleRate");
    averageBitrate = YoutubeHelper.getInt(json, "averageBitrate");

    AudioQuality audioQuality = null;
    if (json.has("audioQuality")) {
      String[] split = json.get("audioQuality").asText().split("_");
      String quality = split[split.length - 1].toLowerCase();
      try {
        audioQuality = AudioQuality.valueOf(quality);
      } catch (IllegalArgumentException ignore) {}
    }
    this.audioQuality = audioQuality;
  }

  @Override
  public String type() {
    return "audio";
  }

  public AudioQuality audioQuality() {
    return audioQuality != null ? audioQuality : itag.audioQuality();
  }

  public Integer getAudioSampleBitrate() {
    return audioSampleRate;
  }

  public Integer getAverageBitrate() {
    return averageBitrate;
  }
}
