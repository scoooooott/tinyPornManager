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
public class MediaFileSubtitle extends MediaStreamInfo implements Comparable<MediaFileSubtitle> {

  /**
   * https://www.3playmedia.com/2017/06/19/whats-the-difference-subtitles-for-the-deaf-and-hard-of-hearing-sdh-v-closed-captions/<br>
   * https://www.reddit.com/r/kodi/comments/4daoa1/subtitles_can_someone_explain_the_difference/d1pa9lf?utm_source=share&utm_medium=web2x
   */

  @JsonProperty
  @Deprecated
  public boolean forced        = false;
  @JsonProperty
  @Deprecated
  public boolean defaultStream = false;

  public MediaFileSubtitle() {
  }

  @Override
  public int compareTo(MediaFileSubtitle mf2) {
    return this.getLanguage().compareTo(mf2.getLanguage());
  }

  @Override
  public String toString() {
    return this.getLanguage();
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

    MediaFileSubtitle that = (MediaFileSubtitle) o;

    return forced == that.forced && defaultStream == that.defaultStream;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), forced, defaultStream);
  }
}
