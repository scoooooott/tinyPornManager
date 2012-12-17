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

import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class Trailer.
 */
@Entity
public class Trailer extends AbstractModelObject {

  /** The name. */
  private String  name     = "";

  /** The url. */
  private String  url      = "";

  /** The quality. */
  private String  quality  = "";

  /** The provider. */
  private String  provider = "";

  /** The inNfo. */
  private Boolean inNfo    = false;

  /**
   * Instantiates a new trailer.
   */
  public Trailer() {
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
   * @param name
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
   * @param url
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
   * @param quality
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
   * @param provider
   *          the new provider
   */
  public void setProvider(String newValue) {
    String oldValue = this.provider;
    this.provider = newValue;
    firePropertyChange("provider", oldValue, newValue);
  }

  public Boolean getInNfo() {
    return inNfo;
  }

  public void setInNfo(Boolean newValue) {
    Boolean oldValue = this.inNfo;
    this.inNfo = newValue;
    firePropertyChange("inNfo", oldValue, newValue);
  }

}
