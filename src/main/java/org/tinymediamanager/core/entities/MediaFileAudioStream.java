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

import java.util.Locale;

import org.tinymediamanager.core.AbstractModelObject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class MediaFileAudioStream
 * 
 * @author Manuel Laggner
 */
public class MediaFileAudioStream extends AbstractModelObject {
  /**
   * "channels" String was a MediaInfo 1:1 text output<br>
   * since output got changed quite hefty over different version<br>
   * we now try to parse while getting MI, and not on every access.
   */
  @Deprecated
  @JsonProperty
  private String  channels      = "";
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

  @Deprecated
  public String getChannels() {
    return channels;
  }

  /**
   * workaround for not changing the var to int.<br>
   * channels usually filled like "5.1ch" or "8 / 6". Take the higher
   * 
   * @return channels as int
   */
  @Deprecated
  public int getChannelsAsInt() {
    int highest = 0;
    if (!channels.isEmpty()) {
      try {
        String[] parts = channels.split("/");
        for (String p : parts) {
          if (p.toLowerCase(Locale.ROOT).contains("object")) {
            // "11 objects / 6 channels" - ignore objects
            continue;
          }
          p = p.replaceAll("[a-zA-Z]", ""); // remove now all characters

          int ch = 0;
          String[] c = p.split("[^0-9]"); // split on not-numbers and count all; so 5.1 -> 6
          for (String s : c) {
            if (s.matches("[0-9]+")) {
              ch += Integer.parseInt(s);
            }
          }
          if (ch > highest) {
            highest = ch;
          }
        }
      }
      catch (NumberFormatException e) {
        highest = 0;
      }
    }
    return highest;
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

  @Deprecated
  public void setChannels(String channels) {
    this.channels = channels;
  }

  public void setBitrate(int bitrate) {
    this.bitrate = bitrate;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
