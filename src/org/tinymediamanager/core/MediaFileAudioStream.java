/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import javax.persistence.Embeddable;

/**
 * The class MediaFileAudioStream
 * 
 * @author Manuel Laggner
 */
@Embeddable
public class MediaFileAudioStream extends AbstractModelObject {

  private String codec    = "";
  private String channels = "";
  private int    bitrate  = 0;
  private String language = "";

  public MediaFileAudioStream() {
  }

  public String getCodec() {
    return codec;
  }

  public String getChannels() {
    return channels;
  }

  /**
   * workaround for not changing the var to int.<br>
   * channels usually filled like "6ch".
   * 
   * @return channels as int
   */
  public int getChannelsAsInt() {
    int ch = 0;
    if (!channels.isEmpty()) {
      try {
        String[] c = channels.split("[^0-9]"); // split on not-numbers and count all; so 5.1 -> 6
        for (String s : c) {
          if (s.matches("[0-9]+")) {
            ch += Integer.parseInt(s);
          }
        }
      }
      catch (NumberFormatException e) {
        ch = 0;
      }
    }
    return ch;
  }

  public int getBitrate() {
    return bitrate;
  }

  public String getBitrateInKbps() {
    return bitrate + " kbps";
  }

  public String getLanguage() {
    return language;
  }

  public void setCodec(String codec) {
    this.codec = codec;
  }

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
