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

import javax.persistence.Entity;

import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class MovieCast.
 */
@Entity
public class MovieCast extends AbstractModelObject {

  /**
   * The Enum CastType.
   */
  public enum CastType {

    /** The actor. */
    ACTOR
  }

  /** The name. */
  private String name;

  /** The character. */
  private String character;

  /** The type. */
  private CastType type;

  /**
   * Instantiates a new movie cast.
   */
  public MovieCast() {
    this.name = new String();
    this.character = new String();
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
    this.character = new String();
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

}
