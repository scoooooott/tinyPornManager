/*
 * Copyright 2012 - 2015 Manuel Laggner
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
 * The Class MovieActor. This class represents the movie actors
 * 
 * @author Manuel Laggner
 */
@Embeddable
public class MovieActor extends AbstractModelObject {
  public static final String ACTOR_DIR = ".actors";

  private String             name      = "";
  private String             character = "";
  private String             thumbUrl  = "";
  private String             thumbPath = "";

  public MovieActor() {
  }

  public MovieActor(String name) {
    this.name = name;
  }

  public MovieActor(String name, String character) {
    this.name = name;
    this.character = character;
  }

  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange(NAME, oldValue, newValue);
  }

  public String getName() {
    return name;
  }

  public String getCharacter() {
    return character;
  }

  public void setCharacter(String newValue) {
    String oldValue = character;
    character = newValue;
    firePropertyChange(CHARACTER, oldValue, newValue);
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setThumbUrl(String newValue) {
    String oldValue = this.thumbUrl;
    thumbUrl = newValue;
    firePropertyChange(THUMB, oldValue, newValue);
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
    if (!(obj instanceof MovieActor)) {
      return false;
    }

    MovieActor cast = (MovieActor) obj;

    // checks of equality
    if (StringUtils.equals(name, cast.name) && StringUtils.equals(character, cast.character) && StringUtils.equals(thumbUrl, cast.thumbUrl)) {
      return true;
    }

    return false;
  }
}
