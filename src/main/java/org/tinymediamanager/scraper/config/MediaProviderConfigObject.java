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
  ConfigType                  type            = ConfigType.TEXT;
  ArrayList<String>           possibleValues  = new ArrayList<String>();

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  /**
   * gets the configured value, or the index of the value
   * 
   * @return
   */
  public String getValue() {
    if (this.returnListAsInt) {
      if (!possibleValues.contains(this.value)) {
        LOGGER.warn("Could not get INT value for key '" + this.key + "' - not in range!");
        return "";
      }
      else {
        return String.valueOf(possibleValues.indexOf(value));
      }
    }
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
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
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
