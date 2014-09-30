/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.scraper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The Class MediaSearchResult.
 * 
 * @author Manuel Laggner
 */
public class MediaSearchResult implements Comparable<MediaSearchResult> {
  private String              providerId;
  private String              url;
  private String              title;
  private String              year;
  private String              originalTitle;
  private String              id;
  private float               score;
  private Map<String, String> extraArgs = new HashMap<String, String>();
  private String              imdbId;
  private MediaMetadata       metadata  = null;
  private MediaType           type;
  private String              posterUrl;

  public MediaSearchResult(String providerId) {
    this.providerId = providerId;
  }

  /**
   * merges all entries from other MSR into ours, IF VALUES ARE EMPTY<br>
   * <b>needs testing!</b>
   * 
   * @param msr
   *          other MediaSerachResult
   * @return MediaSerachResult
   */
  public void mergeFrom(MediaSearchResult msr) {
    url = StringUtils.isEmpty(url) ? msr.getUrl() : url;
    title = StringUtils.isEmpty(title) ? msr.getTitle() : title;
    year = StringUtils.isEmpty(year) ? msr.getYear() : year;
    originalTitle = StringUtils.isEmpty(originalTitle) ? msr.getOriginalTitle() : originalTitle;
    id = StringUtils.isEmpty(id) ? msr.getId() : id;
    imdbId = StringUtils.isEmpty(imdbId) ? msr.getIMDBId() : imdbId;
    posterUrl = StringUtils.isEmpty(posterUrl) ? msr.getPosterUrl() : posterUrl;

    extraArgs.putAll(msr.getExtra()); // meh - add all

    if (metadata == null) {
      metadata = msr.getMediaMetadata();
    }
    else {
      metadata.mergeFrom(msr.getMediaMetadata());
    }

  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  public MediaSearchResult(String providerId, MediaType type, float score) {
    this.providerId = providerId;
    this.type = type;
    this.score = score;
  }

  public MediaSearchResult(String providerId, String id, String title, String year, float score) {
    super();
    this.providerId = providerId;
    this.id = id;
    this.title = title;
    this.year = year;
    this.score = score;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public float getScore() {
    return score;
  }

  public void setScore(float score) {
    this.score = score;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void addExtraArg(String key, String value) {
    this.extraArgs.put(key, value);
  }

  public MediaType getMediaType() {
    return type;
  }

  public void setMediaType(MediaType type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIMDBId() {
    return imdbId;
  }

  public void setIMDBId(String imdbid) {
    this.imdbId = imdbid;
  }

  public Map<String, String> getExtra() {
    return extraArgs;
  }

  /**
   * renamed to getMediaMetadata()
   */
  @Deprecated
  public MediaMetadata getMetadata() {
    return metadata;
  }

  public MediaMetadata getMediaMetadata() {
    return metadata;
  }

  public void setMetadata(MediaMetadata md) {
    this.metadata = md;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public void setPosterUrl(String posterUrl) {
    this.posterUrl = posterUrl;
  }

  @Override
  public int compareTo(MediaSearchResult arg0) {
    if (getScore() < arg0.getScore()) {
      return -1;
    }
    else if (getScore() == arg0.getScore()) {
      // same score - rank on year
      try {
        int y1 = Integer.valueOf(getYear());
        int y2 = Integer.valueOf(arg0.getYear());
        if (y1 > y2) {
          return 1;
        }
        else {
          return -1;
        }
      }
      catch (Exception e) {
        return 0;
      }
    }
    else {
      return 1;
    }
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    // return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
    // @Override
    // protected boolean accept(Field f) {
    // return super.accept(f) && !f.getName().equals("metadata");
    // }
    // }).toString();
  }
}
