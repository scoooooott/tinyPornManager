/*
 * Copyright 2012 - 2017 Manuel Laggner
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
public class MediaFileSubtitle extends AbstractModelObject implements Comparable<MediaFileSubtitle> {
  @JsonProperty
  private String  codec    = "";
  @JsonProperty
  private String  language = "";
  @JsonProperty
  private boolean forced   = false;

  public MediaFileSubtitle() {
  }

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

  public boolean isForced() {
    return forced;
  }

  public void setForced(boolean forced) {
    this.forced = forced;
  }

  @Override
  public boolean equals(Object mf2) {
    if ((mf2 != null) && (mf2 instanceof MediaFileSubtitle)) {
      return compareTo((MediaFileSubtitle) mf2) == 0;
    }
    return false;
  }

  @Override
  public int compareTo(MediaFileSubtitle mf2) {
    return this.getLanguage().compareTo(mf2.getLanguage());
  }

  @Override
  public int hashCode() {
    return this.language.hashCode();
  }

  @Override
  public String toString() {
    return this.getLanguage();
  }
}
