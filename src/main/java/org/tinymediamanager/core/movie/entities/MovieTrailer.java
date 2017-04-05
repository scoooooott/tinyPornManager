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
package org.tinymediamanager.core.movie.entities;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.entities.MediaTrailer;
import org.tinymediamanager.scraper.util.YoutubeLinkExtractor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Trailer.
 * 
 * @author Manuel Laggner
 */
public class MovieTrailer extends AbstractModelObject implements Comparable<MovieTrailer> {
  private static final Logger LOGGER   = LoggerFactory.getLogger(MovieTrailer.class);

  @JsonProperty
  private String              name     = "";
  @JsonProperty
  private String              url      = "";
  @JsonProperty
  private String              quality  = "";
  @JsonProperty
  private String              provider = "";
  @JsonProperty
  private Boolean             inNfo    = Boolean.FALSE;
  @JsonProperty
  private String              date     = "";

  public MovieTrailer() {
  }

  /**
   * create a MovieTrailer object from a given MovieTrailer instance
   * 
   * @param mediaTrailer
   *          the MediaTrailer instance
   */
  public MovieTrailer(MediaTrailer mediaTrailer) {
    if (mediaTrailer != null) {
      name = mediaTrailer.getName();
      url = mediaTrailer.getUrl();
      quality = mediaTrailer.getQuality();
      provider = mediaTrailer.getProvider();
      date = mediaTrailer.getDate();
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String newValue) {
    String oldValue = this.url;
    this.url = newValue;
    firePropertyChange("url", oldValue, newValue);
  }

  public String getQuality() {
    return quality;
  }

  public void setQuality(String newValue) {
    String oldValue = this.quality;
    this.quality = newValue;
    firePropertyChange("quality", oldValue, newValue);
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String newValue) {
    String oldValue = this.provider;
    this.provider = newValue;
    firePropertyChange("provider", oldValue, newValue);
  }

  public Boolean getInNfo() {
    return inNfo;
  }

  public void setInNfo(Boolean newValue) {
    if (this.url.startsWith("file")) {
      // local trailers never in url
      newValue = false;
    }
    Boolean oldValue = this.inNfo;
    this.inNfo = newValue;
    firePropertyChange("inNfo", oldValue, newValue);
  }

  public String getDate() {
    return date;
  }

  public void setDate(String newValue) {
    String oldValue = this.date;
    this.date = newValue;
    firePropertyChange("date", oldValue, newValue);
  }

  @Override
  public String toString() {
    return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
      @Override
      protected boolean accept(Field f) {
        return super.accept(f) && !f.getName().equals("propertyChangeSupport");
      }
    }).toString();
  }

  /**
   * gets the real download url - provider based implementation
   * 
   * @return real url
   */
  public String getDownloadUrl() {
    String url = getUrl();

    if ("youtube".equalsIgnoreCase(getProvider())) {
      try {
        YoutubeLinkExtractor yt = new YoutubeLinkExtractor(url);
        url = yt.extractVideoUrl();
      }
      catch (IOException e) {
        LOGGER.error("Error extracting Youtube url: " + e.getMessage());
      }
      catch (InterruptedException e) {
      }
    }

    return url;
  }

  @Override
  public boolean equals(Object mt2) {
    if ((mt2 != null) && (mt2 instanceof MovieTrailer)) {
      return compareTo((MovieTrailer) mt2) == 0;
    }
    return false;
  }

  @Override
  public int compareTo(MovieTrailer mt2) {
    return this.getUrl().compareTo(mt2.getUrl());
  }

  @Override
  public int hashCode() {
    return this.getUrl().hashCode();
  }

  /**
   * the comparator QualityComparator is used to sort the trailers on their quality to take the "best" one
   */
  public static class QualityComparator implements Comparator<MovieTrailer> {
    @Override
    public int compare(MovieTrailer o1, MovieTrailer o2) {
      int quality1 = 0;
      int quality2 = 0;
      try {
        quality1 = Integer.parseInt(o1.quality.replace("p", ""));
      }
      catch (Exception e) {
      }
      try {
        quality2 = Integer.parseInt(o2.quality.replace("p", ""));
      }
      catch (Exception e) {
      }
      return quality2 - quality1;
    }
  }
}
