/*
* Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.CHARACTER;
import static org.tinymediamanager.core.Constants.NAME;
import static org.tinymediamanager.core.Constants.THUMB;
import static org.tinymediamanager.core.Constants.THUMB_PATH;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.AbstractModelObject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class MovieActor. This class represents the movie actors
 * 
 * @author Manuel Laggner
 */
public class MovieActor extends AbstractModelObject {
  public static final String ACTOR_DIR = ".actors";

  @JsonProperty
  private String             name      = "";
  @JsonProperty
  private String             character = "";
  @JsonProperty
  private String             thumbUrl  = "";
  @JsonProperty
  private String             thumbPath = "";

  public MovieActor() {
  }

  public MovieActor(String name) {
    this.name = name;
  }

  public MovieActor(String name, String character) {
    this.name = name;
    this.character = character;
  }

  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange(NAME, oldValue, newValue);
  }

  public String getName() {
    return name;
  }

  /**
   * Gets the actor name in a storageable format (without special characters)
   * 
   * @return the <i>cleaned</i> name for storing
   */
  public String getNameForStorage() {
    return name.replace(" ", "_").replaceAll("([\"\\\\:<>|/?*])", "");
  }

  public String getCharacter() {
    return character;
  }

  public void setCharacter(String newValue) {
    String oldValue = character;
    character = newValue;
    firePropertyChange(CHARACTER, oldValue, newValue);
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setThumbUrl(String newValue) {
    String oldValue = this.thumbUrl;
    thumbUrl = newValue;
    firePropertyChange(THUMB, oldValue, newValue);
  }

  public String getThumbPath() {
    return thumbPath;
  }

  public void setThumbPath(String newValue) {
    String oldValue = this.thumbPath;
    thumbPath = newValue;
    firePropertyChange(THUMB_PATH, oldValue, newValue);
  }

  /**
   * path is always absolute - change it here back to the movie root
   * 
   * @param movieDir
   *          the movie root dir
   * @return true, if path was changed<br>
   *         false, when same
   */
  public boolean updateThumbRoot(String movieDir) {
    String oldValue = this.thumbPath;
    String newValue = movieDir + File.separator + thumbPath.substring(thumbPath.indexOf(ACTOR_DIR));
    if (oldValue.equals(newValue)) {
      return false;
    }
    else {
      this.thumbPath = newValue;
      firePropertyChange(THUMB_PATH, oldValue, newValue);
      return true;
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
    if (!(obj instanceof MovieActor)) {
      return false;
    }

    MovieActor cast = (MovieActor) obj;

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
