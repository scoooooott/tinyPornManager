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

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Trailer.
 * 
 * @author Manuel Laggner
 */
public class MediaTrailer extends AbstractModelObject implements Comparable<MediaTrailer> {
  private static final Logger LOGGER   = LoggerFactory.getLogger(MediaTrailer.class);

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
  private Date                date     = null;

  public String getName() {
    return name;
  }

  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = StrgUtils.getNonNullString(newValue);
    firePropertyChange("name", oldValue, newValue);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String newValue) {
    String oldValue = this.url;
    this.url = StrgUtils.getNonNullString(newValue);
    firePropertyChange("url", oldValue, newValue);
  }

  public String getQuality() {
    return quality;
  }

  public void setQuality(String newValue) {
    String oldValue = this.quality;
    this.quality = StrgUtils.getNonNullString(newValue);
    firePropertyChange("quality", oldValue, newValue);
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String newValue) {
    String oldValue = this.provider;
    this.provider = StrgUtils.getNonNullString(newValue);
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

  public Date getDate() {
    return date;
  }

  /**
   * convenient method to set the date (parsed from string).
   */
  public void setDate(String newValue) {
    try {
      setDate(StrgUtils.parseDate(newValue));
    }
    catch (ParseException e) {
      LOGGER.trace("could not parse date: {}", e.getMessage());
    }
  }

  public void setDate(Date newValue) {
    Date oldValue = this.date;
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

  @Override
  public boolean equals(Object mt2) {
    if (mt2 instanceof MediaTrailer) {
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

  /**
   * the comparator QualityComparator is used to sort the trailers on their quality to take the "best" one
   */
  public static class QualityComparator implements Comparator<MediaTrailer> {
    @Override
    public int compare(MediaTrailer o1, MediaTrailer o2) {
      int quality1 = 0;
      int quality2 = 0;
      try {
        quality1 = Integer.parseInt(o1.quality.replace("p", ""));
      }
      catch (Exception ignored) {
        // no need to log here
      }
      try {
        quality2 = Integer.parseInt(o2.quality.replace("p", ""));
      }
      catch (Exception ignored) {
        // no need to log here
      }
      return quality2 - quality1;
    }
  }
}
