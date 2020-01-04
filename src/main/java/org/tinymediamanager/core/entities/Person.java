/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.core.entities;

import static org.tinymediamanager.core.Constants.NAME;
import static org.tinymediamanager.core.Constants.ROLE;
import static org.tinymediamanager.core.Constants.THUMB;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.UrlUtil;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Actor. This class represents actors/cast
 * 
 * @author Manuel Laggner
 */
public class Person extends AbstractModelObject {
  public static final String ACTOR_DIR = ".actors";

  public enum Type {
    ACTOR,
    DIRECTOR,
    WRITER,
    PRODUCER,
    OTHER
  }

  @JsonProperty
  private Type                type       = Type.OTHER;
  @JsonProperty
  private String              name       = "";
  @JsonProperty
  private String              role       = "";
  @JsonProperty
  private String              thumbUrl   = "";
  @JsonProperty
  private String              profileUrl = "";
  @JsonProperty
  private Map<String, Object> ids        = null;

  /**
   * JSON constructor - please do not use
   */
  public Person() {
  }

  public Person(Type type) {
    this.type = type;
  }

  public Person(Type type, String name) {
    this(type);
    setName(name);
  }

  public Person(Type type, String name, String role) {
    this(type, name);
    setRole(role);
  }

  public Person(Type type, String name, String role, String thumbUrl) {
    this(type, name, role);
    setThumbUrl(thumbUrl);
  }

  public Person(Type type, String name, String role, String thumbUrl, String profileUrl) {
    this(type, name, role, thumbUrl);
    setProfileUrl(profileUrl);
  }

  /**
   * copy constructor
   * 
   * @param source
   *          the source to be copied
   */
  public Person(Person source) {
    this.type = source.type;
    this.name = source.name;
    this.role = source.role;
    this.thumbUrl = source.thumbUrl;
    this.profileUrl = source.profileUrl;

    if (source.ids != null && !source.ids.isEmpty()) {
      this.ids = new HashMap<>(source.ids);
    }
  }

  /**
   * set the type of that person
   * 
   * @param type
   *          the type of that person
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * get the type of that person
   * 
   * @return the type of that person
   */
  public Type getType() {
    return type;
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
    if (this.ids == null) {
      this.ids = new HashMap<>(0);
    }

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
   * @return the id for this key
   */
  public Object getId(String key) {
    if (this.ids == null) {
      return null;
    }

    return ids.get(key);
  }

  /**
   * get all ID for this object. These are the IDs from the various scraper
   *
   * @return a map of all IDs
   */
  public Map<String, Object> getIds() {
    if (this.ids == null) {
      return Collections.emptyMap();
    }

    return ids;
  }

  /**
   * set the name of that person
   * 
   * @param newValue
   *          the new name
   */
  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = StrgUtils.getNonNullString(newValue);
    firePropertyChange(NAME, oldValue, newValue);
  }

  /**
   * get the name of that person
   * 
   * @return the actual name of that person
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the actor name in a storage-able format (without special characters)
   * 
   * @return the <i>cleaned</i> name for storing
   */
  public String getNameForStorage() {
    String n = name.replace(" ", "_");
    n = n.replaceAll("([\"\\\\:<>|/?*])", "");
    String ext = UrlUtil.getExtension(this.thumbUrl);
    if (ext.isEmpty()) {
      ext = "jpg";
    }
    return n + "." + ext;
  }

  /**
   * get the role of that person (character for actors, role for other ones)
   * 
   * @return the actual role
   */
  public String getRole() {
    return role;
  }

  /**
   * set the role for that person (character for actors, role for other ones)
   * 
   * @param newValue
   *          the role to be set
   */
  public void setRole(String newValue) {
    String oldValue = this.role;
    this.role = StrgUtils.getNonNullString(newValue);
    firePropertyChange(ROLE, oldValue, newValue);
  }

  /**
   * get the thumb url of that person (or an empty string)
   * 
   * @return the thumb url or an empty string
   */
  public String getThumbUrl() {
    return thumbUrl;
  }

  /**
   * set the thumb url for that person
   * 
   * @param newValue
   *          the new thumb url
   */
  public void setThumbUrl(String newValue) {
    String oldValue = this.thumbUrl;
    thumbUrl = StrgUtils.getNonNullString(newValue);
    firePropertyChange(THUMB, oldValue, newValue);
  }

  /**
   * get the profile url of that person (or an empty string)
   * 
   * @return the profile url or an empty string
   */
  public String getProfileUrl() {
    return profileUrl;
  }

  /**
   * set the profile url of that person
   * 
   * @param newValue
   *          the profile url
   */
  public void setProfileUrl(String newValue) {
    String oldValue = this.profileUrl;
    this.profileUrl = StrgUtils.getNonNullString(newValue);
    firePropertyChange("profileUrl", oldValue, newValue);
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
    if (!(obj instanceof Person)) {
      return false;
    }

    Person cast = (Person) obj;

    // checks of equality
    if (StringUtils.equals(name, cast.name) && StringUtils.equals(role, cast.role) && StringUtils.equals(thumbUrl, cast.thumbUrl)
        && StringUtils.equals(profileUrl, cast.profileUrl)) {
      return true;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).append(role).append(thumbUrl).build();
  }
}
