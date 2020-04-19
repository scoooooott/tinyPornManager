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
package org.tinymediamanager.scraper.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.util.AesUtil;

/**
 * This class is used to provide a configuration interface for scrapers
 * 
 * @author Myron Boyle, Manuel Laggner
 */
public class MediaProviderConfig {
  private static final Logger                          LOGGER        = LoggerFactory.getLogger(MediaProviderConfig.class);
  private static final String                          CONFIG_FOLDER = "data";
  private static final String                          SALT          = "3FF2EB019C627B9652257EAAD71812269851E84295370EB132882F88C0A59A76";
  private static final String                          IV            = "E17D2C8927726ACE1E7510A1BDD3D439";

  private static final AesUtil                         AES_UTIL      = new AesUtil(128, 100);

  private final Map<String, MediaProviderConfigObject> settings      = new LinkedHashMap<>();
  private final String                                 id;

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
  void loadFromDir(String folder) {
    if (settings.isEmpty()) {
      return;
    }
    Properties p = new Properties();
    Path conf = Paths.get(folder, "scraper_" + id + ".conf");
    try (InputStream stream = Files.newInputStream(conf)) {
      p.load(stream);
      LOGGER.info("load settings '{}'", conf);
      for (MediaProviderConfigObject co : settings.values()) {
        String value = p.getProperty(co.getKey());
        if (co.isEncrypt()) {
          value = decryptField(value, co.getKey());
        }
        co.setValue(value == null ? co.getDefaultValue() : value);
      }
    }
    catch (Exception e) {
      LOGGER.trace("Cannot load settings '{}' - using defaults", conf);
    }
  }

  public void save() {
    saveToDir(CONFIG_FOLDER);
  }

  /**
   * convenient method for unit testing - should not be used otherwise
   */
  void saveToDir(String folder) {
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

    Path conf = Paths.get(folder, "scraper_" + id + ".conf");
    try (OutputStream stream = Files.newOutputStream(conf)) {
      p.store(stream, "");
    }
    catch (IOException e) {
      LOGGER.warn("Cannot write settings '{}' : {}", conf, e.getMessage());
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
   * @return a map containing all config values
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
   *          the key to get the config object
   * @return the config object or an empty one if not found
   */
  public MediaProviderConfigObject getConfigObject(String key) {
    MediaProviderConfigObject co = settings.get(key);
    if (co == null) {
      LOGGER.warn("Could not get configuration object for key '{}' - key not defined!", key);
      return new MediaProviderConfigObject();
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
   *          the key for the config value to get
   * @return the value or an empty string
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
   *          the key for the config value to get
   * @return the index
   */
  public Integer getValueIndex(String key) {
    return getConfigObject(key).getValueIndex();
  }

  /**
   * If you know that this key is a boolean, use that :)<br>
   * will return NULL if it cannot be parsed as boolean
   * 
   * @param key
   *          the key for the config value to get
   * @return true|false or NULL
   */
  public Boolean getValueAsBool(String key) {
    return getConfigObject(key).getValueAsBool();
  }

  /**
   * If you know that this key is an Integer, use that :)<br>
   * will return NULL if it cannot be parsed as Integer
   *
   * @param key
   *          the key for the config value to get
   * @return the Integer or NULL
   */
  public Integer getValueAsInteger(String key) {
    return getConfigObject(key).getValueAsInteger();
  }

  /**
   * set the given value to the config (String variant)
   * 
   * @param key
   *          the to set the value for
   * @param value
   *          the value to be set
   */
  public void setValue(String key, String value) {
    MediaProviderConfigObject co = getConfigObject(key);
    if (co.isEmpty()) {
      return;
    }
    co.setValue(value);
  }

  /**
   * set the given value to the config (boolean variant)
   *
   * @param key
   *          the to set the value for
   * @param value
   *          the value to be set
   */
  public void setValue(String key, boolean value) {
    MediaProviderConfigObject co = getConfigObject(key);
    if (co.isEmpty()) {
      return;
    }
    co.setValue(value);
  }

  /**
   * set the given value to the config (Integer variant)
   *
   * @param key
   *          the to set the value for
   * @param value
   *          the value to be set
   */
  public void setValue(String key, Integer value) {
    MediaProviderConfigObject co = getConfigObject(key);
    if (co.isEmpty()) {
      return;
    }
    co.setValue(value);
  }

  /**
   * adds a boolean parameter to the configuration
   * 
   * @param key
   *          the config key
   * @param defaultValue
   *          the default value
   */
  public void addBoolean(String key, boolean defaultValue) {
    addBoolean(key, "", defaultValue);
  }

  /**
   * adds a boolean parameter to the configuration
   *
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param defaultValue
   *          the default value
   */
  public void addBoolean(String key, String keyDescription, boolean defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.BOOL);
    co.setKey(key);
    co.setKeyDescription(keyDescription);
    co.setDefaultValue(String.valueOf(defaultValue));
    co.setValue(String.valueOf(defaultValue));
    settings.put(key, co);
  }

  /**
   * adds a text parameter to the configuration
   * 
   * @param key
   *          the config key
   * @param defaultValue
   *          the default value
   */
  public void addText(String key, String defaultValue) {
    addText(key, defaultValue, false);
  }

  /**
   * adds a text parameter to the configuration
   *
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param defaultValue
   *          the default value
   */
  public void addText(String key, String keyDescription, String defaultValue) {
    addText(key, keyDescription, defaultValue, false);
  }

  /**
   * adds an encrypted text parameter to the configuration (useful for sensitive information)
   *
   * @param key
   *          the config key
   * @param defaultValue
   *          the default value
   * @param encrypt
   *          enable/disable encryption
   */
  public void addText(String key, String defaultValue, boolean encrypt) {
    addText(key, "", defaultValue, encrypt);
  }

  /**
   * adds an encrypted text parameter to the configuration (useful for sensitive information)
   *
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param defaultValue
   *          the default value
   * @param encrypt
   *          enable/disable encryption
   */
  public void addText(String key, String keyDescription, String defaultValue, boolean encrypt) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.TEXT);
    co.setKey(key);
    co.setKeyDescription(keyDescription);
    co.setDefaultValue(defaultValue);
    co.setValue(defaultValue);
    co.setEncrypt(encrypt);
    settings.put(key, co);
  }

  /**
   * adds an Integer value to the configuration
   * 
   * @param key
   *          the config key
   * @param defaultValue
   *          the default value
   */
  public void addInteger(String key, Integer defaultValue) {
    addInteger(key, "", defaultValue);
  }

  /**
   * adds an Integer value to the configuration
   * 
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param defaultValue
   *          the default value
   */
  public void addInteger(String key, String keyDescription, Integer defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.INTEGER);
    co.setKey(key);
    co.setKeyDescription(keyDescription);
    co.setDefaultValue(defaultValue.toString());
    co.setValue(defaultValue);
    settings.put(key, co);
  }

  /**
   * adds a value selection to the configuration (Array version)
   * 
   * @param key
   *          the config key
   * @param possibleValues
   *          an array of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelect(String key, String[] possibleValues, String defaultValue) {
    addSelect(key, "", possibleValues, defaultValue);
  }

  /**
   * adds a value selection to the configuration (Array version)
   *
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param possibleValues
   *          an array of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelect(String key, String keyDescription, String[] possibleValues, String defaultValue) {
    addSelect(key, keyDescription, Arrays.asList(possibleValues), defaultValue);
  }

  /**
   * adds a value selection to the configuration (List version)
   *
   * @param key
   *          the config key
   * @param possibleValues
   *          a list of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelect(String key, List<String> possibleValues, String defaultValue) {
    addSelect(key, "", possibleValues, defaultValue);
  }

  /**
   * adds a value selection to the configuration (List version)
   *
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param possibleValues
   *          a list of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelect(String key, String keyDescription, List<String> possibleValues, String defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.SELECT);
    co.setKey(key);
    co.setKeyDescription(keyDescription);
    for (String s : possibleValues) {
      co.addPossibleValues(s);
    }
    co.setDefaultValue(defaultValue);
    co.setValue(defaultValue);
    settings.put(key, co);
  }

  /**
   * adds a value selection (via index) to the configuration (Array version)
   *
   * @param key
   *          the config key
   * @param possibleValues
   *          an array of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelectIndex(String key, String[] possibleValues, String defaultValue) {
    addSelectIndex(key, "", possibleValues, defaultValue);
  }

  /**
   * adds a value selection (via index) to the configuration (Array version)
   *
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param possibleValues
   *          an array of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelectIndex(String key, String keyDescription, String[] possibleValues, String defaultValue) {
    addSelectIndex(key, keyDescription, Arrays.asList(possibleValues), defaultValue);
  }

  /**
   * adds a value selection (via index) to the configuration (List version)
   *
   * @param key
   *          the config key
   * @param possibleValues
   *          a list of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelectIndex(String key, List<String> possibleValues, String defaultValue) {
    addSelectIndex(key, "", possibleValues, defaultValue);
  }

  /**
   * adds a value selection (via index) to the configuration (List version)
   *
   * @param key
   *          the config key
   * @param keyDescription
   *          the key description
   * @param possibleValues
   *          a list of possible values
   * @param defaultValue
   *          the default value
   */
  public void addSelectIndex(String key, String keyDescription, List<String> possibleValues, String defaultValue) {
    MediaProviderConfigObject co = new MediaProviderConfigObject();
    co.setType(MediaProviderConfigObject.ConfigType.SELECT_INDEX);
    co.setKey(key);
    co.setKeyDescription(keyDescription);
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
