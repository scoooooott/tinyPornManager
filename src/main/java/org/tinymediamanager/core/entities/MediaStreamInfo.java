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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.tinymediamanager.core.AbstractModelObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MediaStreamInfo extends AbstractModelObject {

  /**
   * https://github.com/xbmc/xbmc/blob/master/xbmc/cores/VideoPlayer/Interface/StreamInfo.h
   */
  public enum Flags {
    // FLAG_NONE, // just empty
    FLAG_DEFAULT,
    FLAG_DUB,
    FLAG_ORIGINAL,
    FLAG_COMMENT,
    FLAG_LYRICS,
    FLAG_KARAOKE,
    FLAG_FORCED,
    FLAG_HEARING_IMPAIRED,
    FLAG_VISUAL_IMPAIRED
  }

  @JsonProperty
  protected String     codec       = "";
  @JsonProperty
  protected String     language    = "";
  @JsonProperty
  protected Set<Flags> streamFlags = EnumSet.noneOf(Flags.class);

  public String getCodec() {
    return codec;
  }

  public void setCodec(String codec) {
    this.codec = codec;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public boolean has(Flags flag) {
    return streamFlags.contains(flag);
  }

  public void set(Flags... flags) {
    streamFlags.addAll(Arrays.asList(flags));
  }

  public void remove(Flags... flags) {
    for (Flags f : flags) {
      streamFlags.remove(f);
    }
  }

  public boolean isDefaultStream() {
    return streamFlags.contains(Flags.FLAG_DEFAULT);
  }

  public void setDefaultStream(boolean defaultStream) {
    if (defaultStream) {
      streamFlags.add(Flags.FLAG_DEFAULT);
    }
    else {
      streamFlags.remove(Flags.FLAG_DEFAULT);
    }
  }

  public boolean isForced() {
    return streamFlags.contains(Flags.FLAG_FORCED);
  }

  public void setForced(boolean forced) {
    if (forced) {
      streamFlags.add(Flags.FLAG_FORCED);
    }
    else {
      streamFlags.remove(Flags.FLAG_FORCED);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result + ((streamFlags == null) ? 0 : streamFlags.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    MediaStreamInfo other = (MediaStreamInfo) obj;
    if (language == null) {
      if (other.language != null)
        return false;
    }
    else if (!language.equals(other.language)) {
      return false;
    }
    if (streamFlags == null) {
      if (other.streamFlags != null) {
        return false;
      }
    }
    else if (!streamFlags.equals(other.streamFlags)) {
      return false;
    }

    return true;
  }
}
