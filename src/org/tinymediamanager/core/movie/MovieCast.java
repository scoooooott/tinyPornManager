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
package org.tinymediamanager.core.movie;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class MovieCast.
 */
@Embeddable
public class MovieCast extends AbstractModelObject {

  public static final String ACTOR_DIR = ".actors";

  /**
   * The Enum CastType.
   */
  public enum CastType {

    /** The actor. */
    ACTOR
  }

  /** The name. */
  private String   name      = "";

  /** The character. */
  private String   character = "";

  /** The thumbnail. */
  private String   thumb     = "";

  /** The thumb path. */
  private String   thumbPath = "";

  /** The type. */
  private CastType type;

  /**
   * Instantiates a new movie cast.
   */
  public MovieCast() {
  }

  /**
   * Instantiates a new movie cast.
   * 
   * @param name
   *          the name
   * @param castType
   *          the cast type
   */
  public MovieCast(String name, CastType castType) {
    this.name = name;
    this.type = castType;
  }

  /**
   * Instantiates a new movie cast.
   * 
   * @param name
   *          the name
   * @param character
   *          the character
   */
  public MovieCast(String name, String character) {
    this.name = name;
    this.character = character;
    this.type = CastType.ACTOR;
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
   * Sets the type.
   * 
   * @param newValue
   *          the new type
   */
  public void setType(CastType newValue) {
    CastType oldValue = type;
    type = newValue;
    firePropertyChange("type", oldValue, newValue);
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public CastType getType() {
    return type;
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
   * Uses <code>ReflectionToStringBuilder</code> to generate a
   * <code>toString</code> for the specified object.
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
    if (!(obj instanceof MovieCast)) {
      return false;
    }

    MovieCast cast = (MovieCast) obj;

    // checks of equality
    if (StringUtils.equals(name, cast.name) && StringUtils.equals(character, cast.character) && StringUtils.equals(thumb, cast.thumb)
        && type == cast.type) {
      return true;
    }

    return false;
  }
}
