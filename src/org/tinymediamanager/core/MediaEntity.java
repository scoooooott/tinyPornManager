/*
 * Copyright 2012-2013 Manuel Laggner
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

import static org.tinymediamanager.core.Constants.*;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * The Class MediaEntity.
 */

@MappedSuperclass
public abstract class MediaEntity extends AbstractModelObject {

  /** The id. */
  @Id
  @GeneratedValue
  protected Long   id;

  /** The name. */
  protected String name         = "";

  /** The original name. */
  protected String originalName = "";

  /** The overview. */
  protected String overview     = "";

  /** The path. */
  protected String path         = "";

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public Long getId() {
    return id;
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
   * Gets the original name.
   * 
   * @return the original name
   */
  public String getOriginalName() {
    return originalName;
  }

  /**
   * Gets the overview.
   * 
   * @return the overview
   */
  public String getOverview() {
    return overview;
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
   * Gets the name for ui.
   * 
   * @return the name for ui
   */
  abstract public String getNameForUi();

  /**
   * Sets the id.
   * 
   * @param id
   *          the new id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Sets the name.
   * 
   * @param newValue
   *          the new name
   */
  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange(NAME, oldValue, newValue);
    firePropertyChange(NAME_FOR_UI, oldValue, newValue);
  }

  /**
   * Sets the original name.
   * 
   * @param newValue
   *          the new original name
   */
  public void setOriginalName(String newValue) {
    String oldValue = originalName;
    originalName = newValue;
    firePropertyChange(ORIGINAL_NAME, oldValue, newValue);
  }

  /**
   * Sets the overview.
   * 
   * @param newValue
   *          the new overview
   */
  public void setOverview(String newValue) {
    String oldValue = overview;
    overview = newValue;
    firePropertyChange(OVERVIEW, oldValue, newValue);
  }

  /**
   * Sets the path.
   * 
   * @param newValue
   *          the new path
   */
  public void setPath(String newValue) {
    String oldValue = path;
    path = newValue;
    firePropertyChange(PATH, oldValue, newValue);
  }
}
