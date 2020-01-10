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

import java.util.Objects;

import org.tinymediamanager.scraper.util.youtube.YoutubeHelper;
import org.tinymediamanager.scraper.util.youtube.model.Extension;
import org.tinymediamanager.scraper.util.youtube.model.Itag;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The Format class
 *
 * @author Wolfgang Janes
 */
public abstract class Format {
  protected final Itag    itag;
  private final String    url;
  private final String    mimeType;
  private final Extension extension;
  private final Integer   bitrate;
  private final Long      contentLength;
  private final Long      lastModified;

  protected Format(JsonNode json, Itag itag) {
    this.itag = itag;

    url = Objects.requireNonNull(YoutubeHelper.getString(json, "url")).replace("\\u0026", "&");
    mimeType = YoutubeHelper.getString(json, "mimeType");
    bitrate = YoutubeHelper.getInt(json, "bitrate");
    contentLength = Long.valueOf(Objects.requireNonNull(YoutubeHelper.getString(json, "contentLength")));
    lastModified = Long.valueOf(Objects.requireNonNull(YoutubeHelper.getString(json, "lastModified")));

    if (Objects.requireNonNull(mimeType).contains(Extension.MP4.getText())) {
      extension = Extension.MP4;
    }
    else if (mimeType.contains(Extension.WEBM.getText())) {
      extension = Extension.WEBM;
    }
    else if (mimeType.contains(Extension.FLV.getText())) {
      extension = Extension.FLV;
    }
    else if (mimeType.contains(Extension.HLS.getText())) {
      extension = Extension.HLS;
    }
    else if (mimeType.contains(Extension.THREEGP.getText())) {
      extension = Extension.THREEGP;
    }
    else if (mimeType.contains(Extension.M4A.getText())) {
      extension = Extension.MP4;
    }
    else {
      extension = Extension.UNKNOWN;
    }

  }

  public abstract String type();

  public Itag itag() {
    return itag;
  }

  public Integer bitrate() {
    return bitrate;
  }

  public String mimeType() {
    return mimeType;
  }

  public String url() {
    return url;
  }

  public Long contentLength() {
    return contentLength;
  }

  public long lastModified() {
    return lastModified;
  }

  public Extension extension() {
    return extension;
  }
}
