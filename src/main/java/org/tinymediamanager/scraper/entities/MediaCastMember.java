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

import java.util.HashMap;
import java.util.Map;

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

  private String                    name       = "";
  private String                    character  = "";
  private String                    part       = "";
  private String                    imageUrl   = "";
  private String                    profileUrl = "";
  private CastType                  type;

  protected HashMap<String, Object> ids        = new HashMap<>(0);

  public MediaCastMember() {
  }

  public MediaCastMember(CastType type) {
    setType(type);
  }

  /**
   * set the given ID; if the value is zero/"" or null, the key is removed from the existing keys
   *
   * @param key
   *          the ID-key
   * @param value
   *          the ID-value
   */
  public void setId(String key, Object value) {
    // remove ID, if empty/0/null
    // if we only skipped it, the existing entry will stay although someone changed it to empty.
    String v = String.valueOf(value);
    if ("".equals(v) || "0".equals(v) || "null".equals(v)) {
      ids.remove(key);
    }
    else {
      ids.put(key, value);
    }
  }

  /**
   * get the given id
   *
   * @param key
   *          the ID-key
   * @return
   */
  public Object getId(String key) {
    return ids.get(key);
  }

  /**
   * get all ID for this object. These are the IDs from the various scraper
   *
   * @return a map of all IDs
   */
  public Map<String, Object> getIds() {
    return ids;
  }

  /**
   * get the name for that cast member
   * 
   * @return the name of that cast member
   */
  public String getName() {
    return name;
  }

  /**
   * set the name of that cast member
   * 
   * @param name
   *          the name of that cast member
   */
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

  /**
   * get the type of the cast member
   * 
   * @return the cast member type
   */
  public CastType getType() {
    return type;
  }

  /**
   * set the cast member type
   * 
   * @param type
   *          the cast member type
   */
  public void setType(CastType type) {
    this.type = type;
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

  /**
   * get the image url of that cast member
   * 
   * @return the image url or an empty string
   */
  public String getImageUrl() {
    return imageUrl;
  }

  /**
   * set the image url of that cast member
   * 
   * @param imageUrl
   *          the image url
   */
  public void setImageUrl(String imageUrl) {
    this.imageUrl = StrgUtils.getNonNullString(imageUrl);
  }

  /**
   * get the profile url of that cast member
   * 
   * @return the profile url or an empty string
   */
  public String getProfileUrl() {
    return profileUrl;
  }

  /**
   * set the profile url of that cast member
   * 
   * @param profileUrl
   *          the profile url
   */
  public void setProfileUrl(String profileUrl) {
    this.profileUrl = profileUrl;
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
