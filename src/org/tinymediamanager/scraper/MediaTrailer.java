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
package org.tinymediamanager.scraper;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The Class Trailer.
 * 
 * @author Manuel Laggner
 */
public class MediaTrailer implements Comparable<MediaTrailer> {
  private String name     = "";
  private String url      = "";
  private String quality  = "";
  private String provider = "";
  private String size     = "";
  private String date     = "";

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url
   *          the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the quality
   */
  public String getQuality() {
    return quality;
  }

  /**
   * @param quality
   *          the quality to set
   */
  public void setQuality(String quality) {
    this.quality = quality;
  }

  /**
   * @return the provider
   */
  public String getProvider() {
    return provider;
  }

  /**
   * @param provider
   *          the provider to set
   */
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * @return the size
   */
  public String getSize() {
    return size;
  }

  /**
   * @param size
   *          the size to set
   */
  public void setSize(String size) {
    this.size = size;
  }

  /**
   * @return the date
   */
  public String getDate() {
    return date;
  }

  /**
   * @param date
   *          the date to set
   */
  public void setDate(String date) {
    this.date = date;
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
  }

  @Override
  public boolean equals(Object mt2) {
    if ((mt2 != null) && (mt2 instanceof MediaTrailer)) {
      return compareTo((MediaTrailer) mt2) == 0;
    }
    return false;
  }

  @Override
  public int compareTo(MediaTrailer mt2) {
    return this.getUrl().compareTo(mt2.getUrl());
  }

  @Override
  public int hashCode() {
    return this.getUrl().hashCode();
  }
}
