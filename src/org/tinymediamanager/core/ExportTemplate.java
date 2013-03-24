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
package org.tinymediamanager.core;

import org.tinymediamanager.core.movie.MovieExporter.TemplateType;

/**
 * The Class MediaExporter.
 * 
 * @author Manuel Laggner
 */
public class ExportTemplate extends AbstractModelObject {

  /** The name. */
  private String       name        = "";

  /** The path. */
  private String       path        = "";

  /** The type. */
  private TemplateType type;

  /** The detail. */
  private boolean      detail      = false;

  /** The description. */
  private String       description = "";

  /** The url. */
  private String       url         = "";

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the path.
   * 
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public TemplateType getType() {
    return type;
  }

  /**
   * Checks if is detail.
   * 
   * @return true, if is detail
   */
  public boolean isDetail() {
    return detail;
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
   * Sets the path.
   * 
   * @param newValue
   *          the new path
   */
  public void setPath(String newValue) {
    String oldValue = this.path;
    this.path = newValue;
    firePropertyChange("path", oldValue, newValue);
  }

  /**
   * Sets the type.
   * 
   * @param newValue
   *          the new type
   */
  public void setType(TemplateType newValue) {
    TemplateType oldValue = this.type;
    this.type = newValue;
    firePropertyChange("type", oldValue, newValue);
  }

  /**
   * Sets the detail.
   * 
   * @param newValue
   *          the new detail
   */
  public void setDetail(boolean newValue) {
    boolean oldValue = this.detail;
    this.detail = newValue;
    firePropertyChange("detail", oldValue, newValue);
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
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the description.
   * 
   * @param newValue
   *          the new description
   */
  public void setDescription(String newValue) {
    String oldValue = this.description;
    this.description = newValue;
    firePropertyChange("description", oldValue, newValue);
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

}
