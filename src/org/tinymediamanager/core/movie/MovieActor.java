/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class MovieCast.
 * 
 * @author Manuel Laggner
 */
@Embeddable
public class MovieActor extends AbstractModelObject {

  /** The Constant ACTOR_DIR. */
  public static final String ACTOR_DIR = ".actors";

  /** The name. */
  private String             name      = "";

  /** The character. */
  private String             character = "";

  /** The thumbnail. */
  private String             thumb     = "";

  /** The thumb path. */
  private String             thumbPath = "";

  /**
   * Instantiates a new movie actor.
   */
  public MovieActor() {
  }

  /**
   * Instantiates a new movie actor.
   * 
   * @param name
   *          the name
   */
  public MovieActor(String name) {
    this.name = name;
  }

  /**
   * Instantiates a new movie actor.
   * 
   * @param name
   *          the name
   * @param character
   *          the character
   */
  public MovieActor(String name, String character) {
    this.name = name;
    this.character = character;
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
    firePropertyChange("name", oldValue, newValue);
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
   * Gets the character.
   * 
   * @return the character
   */
  public String getCharacter() {
    return character;
  }

  /**
   * Sets the character.
   * 
   * @param newValue
   *          the new character
   */
  public void setCharacter(String newValue) {
    String oldValue = character;
    character = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  /**
   * Gets the thumb.
   * 
   * @return the thumb
   */
  public String getThumb() {
    return thumb;
  }

  /**
   * Sets the thumb.
   * 
   * @param newValue
   *          the new thumb
   */
  public void setThumb(String newValue) {
    String oldValue = this.thumb;
    thumb = newValue;
    firePropertyChange("thumb", oldValue, newValue);
  }

  /**
   * Gets the thumb path.
   * 
   * @return the thumb path
   */
  public String getThumbPath() {
    return thumbPath;
  }

  /**
   * Sets the thumb path.
   * 
   * @param newValue
   *          the new thumb path
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof MovieActor)) {
      return false;
    }

    MovieActor cast = (MovieActor) obj;

    // checks of equality
    if (StringUtils.equals(name, cast.name) && StringUtils.equals(character, cast.character) && StringUtils.equals(thumb, cast.thumb)) {
      return true;
    }

    return false;
  }
}
