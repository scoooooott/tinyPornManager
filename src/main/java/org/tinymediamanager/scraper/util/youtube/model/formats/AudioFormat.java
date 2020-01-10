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
import org.tinymediamanager.scraper.util.youtube.model.quality.AudioQuality;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * AudioFormat
 *
 * @author Wolfgang Janes
 */
public class AudioFormat extends Format {

  private final Integer audioSampleRate;

  public AudioFormat(JsonNode json, Itag itag) {
    super(json, itag);
    audioSampleRate = YoutubeHelper.getInt(json, "audio_sample_rate");
  }

  @Override
  public String type() {
    return "audio";
  }

  public AudioQuality audioQuality() {
    return itag.audioQuality();
  }

  public Integer audioSampleRate() {
    return audioSampleRate;
  }
}
