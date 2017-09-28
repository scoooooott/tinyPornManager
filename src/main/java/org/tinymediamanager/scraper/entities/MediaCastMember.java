/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.scraper.entities;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * This class contains the data for a cast member and its role
 * 
 * @author Manuel Laggner
 * @since 1.0
 * 
 */
public class MediaCastMember {
  /**
   * An enum for holding all different cast member types.
   * 
   * @author Manuel Laggner
   * @since 1.0
   * 
   */
  public enum CastType {
    ACTOR,
    WRITER,
    DIRECTOR,
    OTHER,
    ALL,
    PRODUCER
  }

  private String       id;
  private String       name;
  private String       character;
  private String       part;
  private String       providerDataUrl;
  private String       imageUrl;
  private CastType     type;
  private List<String> fanart = new LinkedList<>();

  public MediaCastMember() {
  }

  public MediaCastMember(CastType type) {
    setType(type);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = StrgUtils.getNonNullString(name);
  }

  /**
   * Gets the part/role this cast member has.
   * 
   * @return the part
   */
  public String getPart() {
    return part;
  }

  /**
   * Sets the part/role the cast member has
   * 
   * @param part
   *          the new part
   */
  public void setPart(String part) {
    this.part = StrgUtils.getNonNullString(part);
  }

  public String getProviderDataUrl() {
    return providerDataUrl;
  }

  public void setProviderDataUrl(String providerDataUrl) {
    this.providerDataUrl = StrgUtils.getNonNullString(providerDataUrl);
  }

  public CastType getType() {
    return type;
  }

  public void setType(CastType type) {
    this.type = type;
  }

  public void addFanart(String url) {
    fanart.add(StrgUtils.getNonNullString(url).trim());
  }

  /**
   * Gets the character name of this cast member (if it is an actor).
   * 
   * @return the character name
   */
  public String getCharacter() {
    return character;
  }

  /**
   * Sets the character name of this cast member (if it is an actor).
   * 
   * @param character
   *          the character name
   */
  public void setCharacter(String character) {
    this.character = StrgUtils.getNonNullString(character);
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = StrgUtils.getNonNullString(imageUrl);
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
