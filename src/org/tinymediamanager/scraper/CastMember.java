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

import java.util.LinkedList;
import java.util.List;

/**
 * The Class CastMember.
 */
public class CastMember {
  
  /** The id. */
  private String          id;
  
  /** The name. */
  private String          name;
  
  /** The part. */
  private String          part;
  
  /** The character. */
  private String          character;

  /** The provider data url. */
  private String          providerDataUrl;
  
  /** The type. */
  private int             type;
  
  /** The fanart. */
  private List<String>    fanart   = new LinkedList<String>();

  /** The Constant ACTOR. */
  public static final int ACTOR    = 0;
  
  /** The Constant WRITER. */
  public static final int WRITER   = 1;
  
  /** The Constant DIRECTOR. */
  public static final int DIRECTOR = 2;
  
  /** The Constant OTHER. */
  public static final int OTHER    = 99;
  
  /** The Constant ALL. */
  public static final int ALL      = 999;

  /**
   * Instantiates a new cast member.
   */
  public CastMember() {
  }

  /**
   * Instantiates a new cast member.
   *
   * @param type the type
   */
  public CastMember(int type) {
    setType(type);
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
   * @param id the new id
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
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the part.
   *
   * @return the part
   */
  public String getPart() {
    return part;
  }

  /**
   * Sets the part.
   *
   * @param part the new part
   */
  public void setPart(String part) {
    this.part = part;
  }

  /**
   * Gets the provider data url.
   *
   * @return the provider data url
   */
  public String getProviderDataUrl() {
    return providerDataUrl;
  }

  /**
   * Sets the provider data url.
   *
   * @param providerDataUrl the new provider data url
   */
  public void setProviderDataUrl(String providerDataUrl) {
    this.providerDataUrl = providerDataUrl;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public int getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Adds the fanart.
   *
   * @param url the url
   */
  public void addFanart(String url) {
    if (url != null) {
      fanart.add(url.trim());
    }
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
   * @param character the new character
   */
  public void setCharacter(String character) {
    this.character = character;
  }

}
