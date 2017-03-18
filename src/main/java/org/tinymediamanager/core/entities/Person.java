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
package org.tinymediamanager.core.entities;

import static org.tinymediamanager.core.Constants.CHARACTER;
import static org.tinymediamanager.core.Constants.NAME;
import static org.tinymediamanager.core.Constants.ROLE;
import static org.tinymediamanager.core.Constants.THUMB;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.util.UrlUtil;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Actor. This class represents actors/cast
 * 
 * @author Manuel Laggner
 */
public abstract class Person extends AbstractModelObject {
  public static final String        ACTOR_DIR  = ".actors";

  @JsonProperty
  private String                    name       = "";
  @JsonProperty
  private String                    character  = "";
  @JsonProperty
  private String                    thumbUrl   = "";
  @Deprecated
  @JsonProperty
  private String                    thumbPath  = "";              // MF like
  @JsonProperty
  private String                    entityRoot = "";              // movie or TV show/episode root
  @JsonProperty
  private int                       ordering   = 0;
  @JsonProperty
  private String                    role       = "";
  @JsonProperty
  protected HashMap<String, Object> ids        = new HashMap<>(0);

  public Person() {
  }

  public Person(String name) {
    this.name = name;
  }

  public Person(String name, String character) {
    this.name = name;
    this.character = character;
  }

  public void setName(String newValue) {
    // FIXME: check renaming of thumb!!!!
    String oldValue = name;
    name = newValue;
    firePropertyChange(NAME, oldValue, newValue);
  }

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
   * Absolute path on filesystem;<br>
   * constructed out of entityRoot+actorsfolder+cleanname.ext
   * 
   * @return path or NULL
   */
  @Deprecated
  public Path getStoragePath() {
    if (StringUtils.isEmpty(entityRoot) || StringUtils.isEmpty(name)) {
      return null;
    }
    return Paths.get(entityRoot, ACTOR_DIR, getNameForStorage()).toAbsolutePath();
  }

  public String getCharacter() {
    return character;
  }

  public void setCharacter(String newValue) {
    String oldValue = character;
    character = newValue;
    firePropertyChange(CHARACTER, oldValue, newValue);
  }

  public String getRole() {
    return role;
  }

  public void setRole(String newValue) {
    String oldValue = role;
    role = newValue;
    firePropertyChange(ROLE, oldValue, newValue);
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setThumbUrl(String newValue) {
    String oldValue = this.thumbUrl;
    thumbUrl = newValue;
    firePropertyChange(THUMB, oldValue, newValue);
  }

  @Deprecated
  public String getThumbPath() {
    return thumbPath;
  }

  @Deprecated
  public void setThumbPath(String thumbPath) {
    this.thumbPath = thumbPath;
  }

  /**
   * The root folder of entity (either movie / tv show/episode path)
   * 
   * @return
   */
  @Deprecated
  public String getEntityRoot() {
    return entityRoot;
  }

  /**
   * The root folder of entity (either movie / tv show/episode path)
   * 
   * @param entityRoot
   */
  @Deprecated
  public void setEntityRoot(String entityRoot) {
    this.entityRoot = entityRoot;
  }

  @Deprecated
  public void setEntityRoot(Path entityRoot) {
    if (entityRoot != null) {
      this.entityRoot = entityRoot.toString();
    }
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
    if (StringUtils.equals(name, cast.name) && StringUtils.equals(character, cast.character) && StringUtils.equals(thumbUrl, cast.thumbUrl)) {
      return true;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).append(character).append(thumbUrl).build();
  }
}
