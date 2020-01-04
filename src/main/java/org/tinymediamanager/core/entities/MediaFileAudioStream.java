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
package org.tinymediamanager.core.entities;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class MediaFileAudioStream
 * 
 * @author Manuel Laggner
 */
public class MediaFileAudioStream extends MediaStreamInfo {
  @JsonProperty
  private int    audioChannels = 0;
  @JsonProperty
  private int    bitrate       = 0;
  @JsonProperty
  @Deprecated
  public boolean defaultStream = false;

  public MediaFileAudioStream() {
  }

  public int getAudioChannels() {
    return audioChannels;
  }

  public void setAudioChannels(int audiochannels) {
    this.audioChannels = audiochannels;
  }

  public int getBitrate() {
    return bitrate;
  }

  public String getBitrateInKbps() {
    return bitrate > 0 ? bitrate + " kbps" : "";
  }

  public void setBitrate(int bitrate) {
    this.bitrate = bitrate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    MediaFileAudioStream that = (MediaFileAudioStream) o;

    return audioChannels == that.audioChannels && bitrate == that.bitrate && defaultStream == that.defaultStream;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), audioChannels, bitrate, defaultStream);
  }
}
