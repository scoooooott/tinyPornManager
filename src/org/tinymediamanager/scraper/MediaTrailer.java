/*
 * Copyright 2012 Manuel Laggner
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

import javax.persistence.Entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;

// TODO: Auto-generated Javadoc
/**
 * The Class Trailer.
 */
@Entity
public class MediaTrailer extends AbstractModelObject {

  /** The name. */
  private String  name     = "";

  /** The url. */
  private String  url      = "";

  /** The quality. */
  private String  quality  = "";

  /** The provider. */
  private String  provider = "";

  /** The inNfo. */
  private Boolean inNfo    = Boolean.FALSE;

  /** The size (as string). */
  private String  size     = "";

  /** The date (as string). */
  private String  date     = "";

  /**
   * Instantiates a new trailer.
   */
  public MediaTrailer() {
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param newValue
   *          the new name
   */
  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url.
   * 
   * @param newValue
   *          the new url
   */
  public void setUrl(String newValue) {
    String oldValue = this.url;
    this.url = newValue;
    firePropertyChange("url", oldValue, newValue);
  }

  /**
   * Gets the quality.
   * 
   * @return the quality
   */
  public String getQuality() {
    return quality;
  }

  /**
   * Sets the quality.
   * 
   * @param newValue
   *          the new quality
   */
  public void setQuality(String newValue) {
    String oldValue = this.quality;
    this.quality = newValue;
    firePropertyChange("quality", oldValue, newValue);
  }

  /**
   * Gets the provider.
   * 
   * @return the provider
   */
  public String getProvider() {
    return provider;
  }

  /**
   * Sets the provider.
   * 
   * @param newValue
   *          the new provider
   */
  public void setProvider(String newValue) {
    String oldValue = this.provider;
    this.provider = newValue;
    firePropertyChange("provider", oldValue, newValue);
  }

  /**
   * Gets the in nfo.
   * 
   * @return the in nfo
   */
  public Boolean getInNfo() {
    return inNfo;
  }

  /**
   * Sets the in nfo.
   * 
   * @param newValue
   *          the new in nfo
   */
  public void setInNfo(Boolean newValue) {
    Boolean oldValue = this.inNfo;
    this.inNfo = newValue;
    firePropertyChange("inNfo", oldValue, newValue);
  }

  /**
   * Gets the size.
   * 
   * @return the size
   */
  public String getSize() {
    return size;
  }

  /**
   * Gets the date.
   * 
   * @return the date
   */
  public String getDate() {
    return date;
  }

  /**
   * Sets the size.
   * 
   * @param newValue
   *          the new size
   */
  public void setSize(String newValue) {
    String oldValue = this.size;
    this.size = newValue;
    firePropertyChange("size", oldValue, newValue);
  }

  /**
   * Sets the date.
   * 
   * @param newValue
   *          the new date
   */
  public void setDate(String newValue) {
    String oldValue = this.date;
    this.date = newValue;
    firePropertyChange("date", oldValue, newValue);
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a
   * <code>toString</code> for the specified object.
   * </p>
   * 
   * @param object
   *          the Object to be output
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
