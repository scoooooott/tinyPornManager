/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import org.tinymediamanager.core.AbstractModelObject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class MediaFileAudioStream
 * 
 * @author Manuel Laggner
 */
public class MediaFileAudioStream extends AbstractModelObject {
  @JsonProperty
  private int     audioChannels = 0;
  @JsonProperty
  private String  codec         = "";
  @JsonProperty
  private int     bitrate       = 0;
  @JsonProperty
  private String  language      = "";
  @JsonProperty
  private boolean defaultStream = false;

  public MediaFileAudioStream() {
  }

  public String getCodec() {
    return codec;
  }

  public int getAudioChannels() {
    return audioChannels;
  }

  public void setAudioChannels(int audiochannels) {
    this.audioChannels = audiochannels;
  }

  public boolean isDefaultStream() {
    return defaultStream;
  }

  public void setDefaultStream(boolean defaultStream) {
    this.defaultStream = defaultStream;
  }

  public int getBitrate() {
    return bitrate;
  }

  public String getBitrateInKbps() {
    return bitrate > 0 ? bitrate + " kbps" : "";
  }

  public String getLanguage() {
    return language;
  }

  public void setCodec(String codec) {
    this.codec = codec;
  }

  public void setBitrate(int bitrate) {
    this.bitrate = bitrate;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
