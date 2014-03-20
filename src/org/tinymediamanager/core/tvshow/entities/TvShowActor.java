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
package org.tinymediamanager.core.tvshow.entities;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class TvShowActor.
 * 
 * @author Manuel Laggner
 */
@Embeddable
public class TvShowActor extends AbstractModelObject {
  public static final String ACTOR_DIR = ".actors";

  private String             name      = "";
  private String             character = "";
  private String             thumb     = "";
  private String             thumbPath = "";

  public TvShowActor() {
  }

  public TvShowActor(String name) {
    this.name = name;
  }

  public TvShowActor(String name, String character) {
    this.name = name;
    this.character = character;
  }

  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange("name", oldValue, newValue);
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
    firePropertyChange("name", oldValue, newValue);
  }

  public String getThumb() {
    return thumb;
  }

  public void setThumb(String newValue) {
    String oldValue = this.thumb;
    thumb = newValue;
    firePropertyChange("thumb", oldValue, newValue);
  }

  public String getThumbPath() {
    return thumbPath;
  }

  public void setThumbPath(String newValue) {
    String oldValue = this.thumbPath;
    thumbPath = newValue;
    firePropertyChange("thumbPath", oldValue, newValue);
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
    if (!(obj instanceof TvShowActor)) {
      return false;
    }

    TvShowActor cast = (TvShowActor) obj;

    // checks of equality
    if (StringUtils.equals(name, cast.name) && StringUtils.equals(character, cast.character) && StringUtils.equals(thumb, cast.thumb)) {
      return true;
    }

    return false;
  }
}
