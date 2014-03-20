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
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.*;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class MovieProducer. This class is representing a movie producer
 * 
 * @author Manuel Laggner
 */
@Embeddable
public class MovieProducer extends AbstractModelObject {
  public static final String ACTOR_DIR = ".actors";

  private String             name      = "";
  private String             role      = "";
  private String             thumbUrl  = "";
  private String             thumbPath = "";

  public MovieProducer() {
  }

  public MovieProducer(String name) {
    this.name = name;
  }

  public MovieProducer(String name, String character) {
    this.name = name;
    this.role = character;
  }

  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange(NAME, oldValue, newValue);
  }

  public String getName() {
    return name;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String newValue) {
    String oldValue = role;
    role = newValue;
    firePropertyChange(ROLE, oldValue, newValue);
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setThumbUrl(String newValue) {
    String oldValue = this.thumbUrl;
    thumbUrl = newValue;
    firePropertyChange(THUMB_URL, oldValue, newValue);
  }

  public String getThumbPath() {
    return thumbPath;
  }

  public void setThumbPath(String newValue) {
    String oldValue = this.thumbPath;
    thumbPath = newValue;
    firePropertyChange(THUMB_PATH, oldValue, newValue);
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
  public boolean equals(Object obj) {
    if (!(obj instanceof MovieProducer)) {
      return false;
    }

    MovieProducer cast = (MovieProducer) obj;

    // checks of equality
    if (StringUtils.equals(name, cast.name) && StringUtils.equals(role, cast.role) && StringUtils.equals(thumbUrl, cast.thumbUrl)) {
      return true;
    }

    return false;
  }
}
