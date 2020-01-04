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
package org.tinymediamanager.core;

import org.tinymediamanager.core.MediaEntityExporter.TemplateType;

/**
 * The Class MediaExporter.
 * 
 * @author Manuel Laggner
 */
public class ExportTemplate extends AbstractModelObject {
  private String       name        = "";
  private String       path        = "";
  private TemplateType type;
  private boolean      detail      = false;
  private String       description = "";
  private String       url         = "";

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public TemplateType getType() {
    return type;
  }

  public boolean isDetail() {
    return detail;
  }

  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  public void setPath(String newValue) {
    String oldValue = this.path;
    this.path = newValue;
    firePropertyChange("path", oldValue, newValue);
  }

  public void setType(TemplateType newValue) {
    TemplateType oldValue = this.type;
    this.type = newValue;
    firePropertyChange("type", oldValue, newValue);
  }

  public void setDetail(boolean newValue) {
    boolean oldValue = this.detail;
    this.detail = newValue;
    firePropertyChange("detail", oldValue, newValue);
  }

  public String getDescription() {
    return description;
  }

  public String getUrl() {
    return url;
  }

  public void setDescription(String newValue) {
    String oldValue = this.description;
    this.description = newValue;
    firePropertyChange("description", oldValue, newValue);
  }

  public void setUrl(String newValue) {
    String oldValue = this.url;
    this.url = newValue;
    firePropertyChange("url", oldValue, newValue);
  }
}
