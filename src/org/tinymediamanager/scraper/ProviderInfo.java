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

/**
 * The Class ProviderInfo.
 */
public class ProviderInfo {
  /** The icon url. */
  private String id, name, description, iconUrl;

  /**
   * Instantiates a new provider info.
   */
  public ProviderInfo() {
  }

  /**
   * Instantiates a new provider info.
   * 
   * @param id
   *          the id
   * @param name
   *          the name
   * @param description
   *          the description
   * @param iconUrl
   *          the icon url
   */
  public ProviderInfo(String id, String name, String description, String iconUrl) {
    super();
    this.id = id;
    this.name = name;
    this.description = description;
    this.iconUrl = iconUrl;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   * 
   * @param id
   *          the new id
   */
  public void setId(String id) {
    this.id = id;
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
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   * 
   * @param description
   *          the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the icon url.
   * 
   * @return the icon url
   */
  public String getIconUrl() {
    return iconUrl;
  }

  /**
   * Sets the icon url.
   * 
   * @param iconUrl
   *          the new icon url
   */
  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }
}
