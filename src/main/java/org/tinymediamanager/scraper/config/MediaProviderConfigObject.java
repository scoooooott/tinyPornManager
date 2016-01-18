/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.scraper.config;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This clas is used for holding a config setting
 * 
 * @author Myron Boyle
 */
public class MediaProviderConfigObject {
  public enum ConfigType {
    TEXT, BOOL, SELECT, SELECT_INDEX
  }

  private static final Logger LOGGER          = LoggerFactory.getLogger(MediaProviderConfigObject.class);

  String                      key             = "";
  String                      value           = "";
  String                      defaultValue    = "";
  boolean                     returnListAsInt = false;
  boolean                     encrypt         = false;
  boolean                     visible         = true;
  ConfigType                  type            = ConfigType.TEXT;
  ArrayList<String>           possibleValues  = new ArrayList<String>();

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public boolean isEmpty() {
    return key.isEmpty();
  }

  /**
   * gets the configured value, or the default one
   * 
   * @return
   */
  public String getValue() {
    String ret = "";
    switch (type) {
      case SELECT:
        ret = getValueAsString();
        break;
      case SELECT_INDEX:
        Integer i = getValueIndex();
        ret = (i == null || i < 0) ? "" : String.valueOf(i);
        break;
      case BOOL:
        return String.valueOf(getValueAsBool());
      case TEXT:
      default:
        return this.value;
    }
    return ret;
  }

  public String getValueAsString() {
    if (type == ConfigType.SELECT && !possibleValues.contains(this.value)) {
      LOGGER.warn("Could not get value for key '" + this.key + "' - not in range; returning default " + defaultValue);
      return this.defaultValue;
    }
    return this.value;
  }

  public Boolean getValueAsBool() {
    Boolean bool = null;
    if (type != ConfigType.BOOL) {
      LOGGER.warn("This is not a boolean '" + key + "=" + value + "' - returning NULL ");
      return null;
    }
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { // always false when unparseable :/
      bool = Boolean.valueOf(value);
    }
    else {
      LOGGER.warn("This is not a Boolean '" + key + "=" + value + "' - returning default " + defaultValue);
      bool = Boolean.valueOf(defaultValue);
    }
    return bool;
  }

  public Integer getValueIndex() {
    // FIXME: Index is just stored in value? return 1:1 ?!? no example found yet...
    Integer ret = null;
    if (type != ConfigType.SELECT && type != ConfigType.SELECT_INDEX) {
      LOGGER.warn("This is not a selectbox '" + key + "=" + value + "' - returning NULL ");
      return null;
    }
    ret = possibleValues.indexOf(value);
    if (ret == -1) {
      ret = possibleValues.indexOf(defaultValue);
      if (ret == -1) {
        ret = null;
      }
      LOGGER.warn("Could not get index for '" + key + "=" + value + "' - not in defined range! returning default " + ret);
    }
    return ret;
  }

  public void setValue(String value) {
    if (type == ConfigType.SELECT && !possibleValues.contains(value)) {
      // possible values set, but ours isn't in? just return...
      LOGGER.warn("Could not set '" + key + "=" + value + "' - not in defined range!");
      return;
    }
    this.value = value;
  }

  public void setValue(boolean value) {
    if (type != ConfigType.BOOL) {
      LOGGER.warn("This is not a boolean configuration object - seeting keep current value");
    }
    else {
      this.value = String.valueOf(value);
    }
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    if (type == ConfigType.SELECT && !possibleValues.contains(defaultValue)) {
      LOGGER.warn("Will not set defaultValue '" + key + "=" + defaultValue + "' - since it is not in the list of possible values!");
    }
    else {
      this.defaultValue = defaultValue;
    }
  }

  public boolean isReturnListAsInt() {
    return returnListAsInt;
  }

  public void setReturnListAsInt(boolean returnListAsInt) {
    this.returnListAsInt = returnListAsInt;
  }

  public ArrayList<String> getPossibleValues() {
    return possibleValues;
  }

  public void setPossibleValues(ArrayList<String> possibleValues) {
    this.possibleValues = possibleValues;
  }

  public void addPossibleValues(String possibleValue) {
    this.possibleValues.add(possibleValue);
  }

  public ConfigType getType() {
    return type;
  }

  public void setType(ConfigType type) {
    this.type = type;
  }

  public boolean isEncrypt() {
    return encrypt;
  }

  public void setEncrypt(boolean encrypt) {
    this.encrypt = encrypt;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * option can be "hidden" in GUI, but be still an option!
   * 
   * @return
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * option can be "hidden" in GUI, but be still an option!
   * 
   * @param visible
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
