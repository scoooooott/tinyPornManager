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
import java.util.TreeMap;

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

  private TreeMap<String, MediaProviderConfigObject> settings      = new TreeMap<>();
  private String                                     id            = "";

  public MediaProviderConfig(MediaProviderInfo mpi) {
    this.id = mpi.getId();
  }

  /**
   * loads config from settings file<br>
   * Should be called right after defining the configuration objects!
   */
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

  public Map<String, MediaProviderConfigObject> getConfigObjects() {
    return settings;
  }

  /**
   * convenient method, to return a key=value map of all config entries
   * 
   * @return
   */
  public Map<String, String> getConfigKeyValuePairs() {
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, MediaProviderConfigObject> entry : this.settings.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getValue());
    }
    return result;
  }

  /**
   * returns a config object (or an empty one if not found)
   * 
   * @param key
   * @return
   */
  public MediaProviderConfigObject getConfigObject(String key) {
    MediaProviderConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not get confiuguration object for key '" + key + "' - key not defined!");
      return new MediaProviderConfigObject(); // FIXME: better NULL than empty?
    }
    return co;
  }

  /**
   * gets the config value as string (or the default)<br>
   * You might want to parse it to boolean if it is true|false<br>
   * You might get a number if it was setup to return the index<br>
   * might return an empty string!
   * 
   * @param key
   * @return
   */
  public String getValue(String key) {
    return getConfigObject(key).getValue();
  }

  /**
   * gets the config value as index<br>
   * works only on select boxes<br>
   * might return NULL if not found/parseable
   * 
   * @param key
   * @return
   */
  public Integer getValueIndex(String key) {
    return getConfigObject(key).getValueIndex();
  }

  /**
   * If you know that this key is a boolean, use that :)<br>
   * will return NULL if it cannot be parsed as boolean
   * 
   * @param key
   * @return true|false or NULL
   */
  public Boolean getValueAsBool(String key) {
    return getConfigObject(key).getValueAsBool();
  }

  public void setValue(String key, String value) {
    MediaProviderConfigObject co = getConfigObject(key);
    if (co.isEmpty()) {
      return;
    }
    co.setValue(value);
  }

  public void setValue(String key, boolean value) {
    MediaProviderConfigObject co = getConfigObject(key);
    if (co.isEmpty()) {
      return;
    }
    co.setValue(value);
  }

  public void addBoolean(String key, boolean defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.BOOL);
    co.setKey(key);
    co.setDefaultValue(String.valueOf(defaultValue));
    co.setValue(String.valueOf(defaultValue));
    settings.put(key, co);
  }

  public void addText(String key, String defaultValue) {
    addText(key, defaultValue, false);
  }

  public void addText(String key, String defaultValue, boolean encrypt) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.TEXT);
    co.setKey(key);
    co.setDefaultValue(defaultValue);
    co.setValue(defaultValue);
    co.setEncrypt(encrypt);
    settings.put(key, co);
  }

  public void addSelect(String key, String[] possibleValues, String defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.SELECT);
    co.setKey(key);
    for (String s : possibleValues) {
      co.addPossibleValues(s);
    }
    co.setDefaultValue(defaultValue);
    co.setValue(defaultValue);
    settings.put(key, co);
  }

  public void addSelectIndex(String key, String[] possibleValues, String defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.SELECT_INDEX);
    co.setKey(key);
    co.setReturnListAsInt(true);
    for (String s : possibleValues) {
      co.addPossibleValues(s);
    }
    co.setDefaultValue(defaultValue);
    co.setValue(defaultValue);
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
