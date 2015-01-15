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
package org.tinymediamanager.scraper;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The Class CastMember.
 * 
 * @author Manuel Laggner
 */
public class MediaCastMember {

  /**
   * The Enum CastType.
   * 
   * @author Manuel Laggner
   */
  public enum CastType {
    ACTOR, WRITER, DIRECTOR, OTHER, ALL, PRODUCER
  }

  private String       id;
  private String       name;
  private String       character;
  private String       part;
  private String       providerDataUrl;
  private String       imageUrl;
  private CastType     type;
  private List<String> fanart = new LinkedList<String>();

  /**
   * Instantiates a new cast member.
   */
  public MediaCastMember() {
  }

  /**
   * Instantiates a new cast member.
   * 
   * @param type
   *          the type
   */
  public MediaCastMember(CastType type) {
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
   * @param id
   *          the new id
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
   * @param name
   *          the new name
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
   * @param part
   *          the new part
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
   * @param providerDataUrl
   *          the new provider data url
   */
  public void setProviderDataUrl(String providerDataUrl) {
    this.providerDataUrl = providerDataUrl;
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
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  public void setType(CastType type) {
    this.type = type;
  }

  /**
   * Adds the fanart.
   * 
   * @param url
   *          the url
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
   * @param character
   *          the new character
   */
  public void setCharacter(String character) {
    this.character = character;
  }

  /**
   * Gets the image url.
   * 
   * @return the image url
   */
  public String getImageUrl() {
    return imageUrl;
  }

  /**
   * Sets the image url.
   * 
   * @param imageUrl
   *          the new image url
   */
  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
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
}
