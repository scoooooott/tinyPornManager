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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.util.AesUtil;

/**
 * This class is used to provide a configuration interface for scrapers
 * 
 * @author Myron Boyle
 */
public class MediaProviderConfig {
  private static final Logger                        LOGGER        = LoggerFactory.getLogger(MediaProviderConfig.class);
  private static final String                        CONFIG_FOLDER = "data";
  private static final String                        SALT          = "3FF2EB019C627B9652257EAAD71812269851E84295370EB132882F88C0A59A76";
  private static final String                        IV            = "E17D2C8927726ACE1E7510A1BDD3D439";

  private static final AesUtil                       AES_UTIL      = new AesUtil(128, 100);

  private HashMap<String, MediaProviderConfigObject> settings      = new HashMap<>();
  private String                                     id            = "";

  public MediaProviderConfig(MediaProviderInfo mpi) {
    this.id = mpi.getId();
  }

  public void load() {
    loadFromDir(CONFIG_FOLDER);
  }

  /**
   * convenient method for unit testing - should not be used otherwise
   */
  @Deprecated
  public void loadFromDir(String folder) {
    if (settings.isEmpty()) {
      return;
    }
    Properties p = new Properties();
    File conf = new File(folder, "scraper_" + id + ".conf");
    try {
      p.load(new FileInputStream(conf));
      for (MediaProviderConfigObject co : settings.values()) {
        String value = p.getProperty(co.getKey());
        if (co.isEncrypt()) {
          value = decryptField(value, co.getKey());
        }
        co.setValue(value == null ? co.getDefaultValue() : value);
      }
    }
    catch (Exception e) {
      LOGGER.warn("Cannot load settings '" + conf + "' - using defaults");
    }
  }

  public void save() {
    saveToDir(CONFIG_FOLDER);
  }

  /**
   * convenient method for unit testing - should not be used otherwise
   */
  @Deprecated
  public void saveToDir(String folder) {
    if (settings.isEmpty()) {
      return;
    }
    Properties p = new Properties();
    for (MediaProviderConfigObject co : settings.values()) {
      String value = co.getValue();
      if (co.isEncrypt()) {
        value = encryptField(value, co.getKey());
      }
      p.setProperty(co.getKey(), value);
    }
    File conf = new File(folder, "scraper_" + id + ".conf");
    try {
      p.store(new FileOutputStream(conf), "");
    }
    catch (IOException e) {
      LOGGER.warn("Cannot write settings " + conf);
    }
  }

  /**
   * indicate whether a config is available or not
   * 
   * @return true/false
   */
  public boolean hasConfig() {
    return !settings.isEmpty();
  }

  public Set<String> getAllEntries() {
    return settings.keySet();
  }

  public Map<String, MediaProviderConfigObject> getConfigObjects() {
    return settings;
  }

  /**
   * gets the config value as string<br>
   * You might want to parse it to boolean if it is true|false<br>
   * You might get a number if it was setup to return the index
   * 
   * @param key
   * @return
   */
  public String getValue(String key) {
    MediaProviderConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not get value for key '" + key + "' - key not defined!");
      return "";
    }
    return co.getValue();
  }

  /**
   * If you know that this key is a boolean, use that :)<br>
   * will return NULL if it cannot be parsed as boolean
   * 
   * @param key
   * @return true|false or NULL
   */
  public Boolean getValueAsBool(String key) {
    Boolean bool = null;
    String val = getValue(key);
    if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) { // always false when unparseable :/
      bool = Boolean.valueOf(val);
    }
    else {
      LOGGER.warn("This is not a Boolean '" + key + "=" + val + "' - returning NULL");
    }
    return bool;
  }

  public void setValue(String key, String value) {
    MediaProviderConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not set '" + key + "=" + value + "' - key not defined!");
      return;
    }
    if (co.getPossibleValues().size() > 0 && !co.getPossibleValues().contains(value)) {
      // possible values set, but ours isn't in? just return...
      LOGGER.warn("Could not set '" + key + "=" + value + "' - not in defined range!");
      return;
    }
    co.setValue(value);
  }

  public void setValue(String key, boolean value) {
    MediaProviderConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not set '" + key + "=" + value + "' - key not defined!");
      return;
    }
    co.setValue(String.valueOf(value));
  }

  public void addBoolean(String key, boolean defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setKey(key);
    co.setValue(String.valueOf(defaultValue));
    co.setDefaultValue(String.valueOf(defaultValue));
    co.setType(MediaProviderConfigObject.ConfigType.BOOL);
    settings.put(key, co);
  }

  public void addText(String key, String defaultValue) {
    addText(key, defaultValue, false);
  }

  public void addText(String key, String defaultValue, boolean encrypt) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setKey(key);
    co.setValue(defaultValue);
    co.setDefaultValue(defaultValue);
    co.setType(MediaProviderConfigObject.ConfigType.TEXT);
    co.setEncrypt(true);
    settings.put(key, co);
  }

  public void addSelect(String key, String[] possibleValues, String defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setKey(key);
    co.setType(MediaProviderConfigObject.ConfigType.SELECT);
    for (String s : possibleValues) {
      co.addPossibleValues(s);
    }
    if (!co.possibleValues.contains(defaultValue)) {
      LOGGER.warn("Will not set defaultValue '" + key + "=" + defaultValue + "' - since it is not in the list of possible values!");
    }
    else {
      co.setDefaultValue(defaultValue);
      co.setValue(defaultValue);
    }
    settings.put(key, co);
  }

  public void addSelectIndex(String key, String[] possibleValues, String defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setKey(key);
    co.setReturnListAsInt(true);
    co.setType(MediaProviderConfigObject.ConfigType.SELECT_INDEX);
    for (String s : possibleValues) {
      co.addPossibleValues(s);
    }
    if (!co.possibleValues.contains(defaultValue)) {
      LOGGER.warn("Will not set defaultValue '" + key + "=" + defaultValue + "' - since it is not in the list of possible values!");
    }
    else {
      co.setDefaultValue(defaultValue);
      co.setValue(defaultValue);
    }
    settings.put(key, co);
  }

  private static String encryptField(String value, String key) {
    return AES_UTIL.encrypt(SALT, IV, key, value);
  }

  private static String decryptField(String value, String key) {
    return AES_UTIL.decrypt(SALT, IV, key, value);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
