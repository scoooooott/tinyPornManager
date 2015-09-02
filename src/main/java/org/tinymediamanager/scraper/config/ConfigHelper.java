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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.util.AesUtil;

/**
 * The class ConfigHelper is a helper class for loading and storing scraper
 * config files via reflection
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public final class ConfigHelper {
  private static final String CONFIG_FILE = "scraper_{id}.conf";
  private static final String SALT        = "3FF2EB019C627B9652257EAAD71812269851E84295370EB132882F88C0A59A76";
  private static final String IV          = "E17D2C8927726ACE1E7510A1BDD3D439";

  private static final AesUtil AES_UTIL = new AesUtil(128, 100);

  /**
   * load the properties file specified via the parameter filename and assign
   * the values via reflection to the object config.
   * 
   * @param mediaProviderInfo
   *          the media provider info from the media provider requesting to load
   *          the config
   * @param config
   *          the object to store the configuration via reflection
   */
  public static void loadConfig(MediaProviderInfo mediaProviderInfo, Object config) {
    String filename = CONFIG_FILE.replace("{id}", mediaProviderInfo.getId());
    // load properties
    Properties properties = new Properties();
    InputStream input = null;
    try {
      input = new FileInputStream(filename);
      properties.load(input);
    }
    catch (Exception ignored) {
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (IOException ignored) {
        }
      }
    }

    // and now try to load these settings
    for (Field field : config.getClass().getFields()) {
      try {
        field.setAccessible(true);
        ScraperSetting annotation = field.getAnnotation(ScraperSetting.class);
        if (annotation == null) {
          continue;
        }
        
        String propValue = properties.getProperty(field.getName());
        if(propValue == null){
          continue;
        }
        
        Method method = null;
        try {
          method = field.getType().getMethod("valueOf", String.class);
        }
        catch (Exception ignored) {
        }
        if (method != null) {
          if (annotation.encrypt()) {
            field.set(config, method.invoke(null, decryptField(propValue, annotation.encryptionKey())));
          }
          else {
            field.set(config, method.invoke(null, propValue));
          }
        }
        else if (field.getType() == String.class) {
          if (annotation.encrypt()) {
            field.set(config, decryptField(properties.getProperty(field.getName()), annotation.encryptionKey()));
          }
          else {
            field.set(config, properties.getProperty(field.getName()));
          }
        }
      }
      catch (Exception ignored) {
      }
    }
  }

  /**
   * write the config properties file specified with the parameter filename. The
   * values to be stored are determined and read via reflection
   * 
   * @param mediaProviderInfo
   *          the media provider info from the media provider requesting to save
   *          the config
   * @param config
   *          the object to extract the config from
   */
  public static void saveConfig(MediaProviderInfo mediaProviderInfo, Object config) {
    String filename = CONFIG_FILE.replace("{id}", mediaProviderInfo.getId());
    // save properties
    Properties properties = new Properties();

    for (Field field : config.getClass().getFields()) {
      try {
        field.setAccessible(true);
        ScraperSetting annotation = field.getAnnotation(ScraperSetting.class);
        if (annotation == null) {
          continue;
        }

        if (annotation.encrypt()) {
          properties.setProperty(field.getName(), encryptField(field.get(config).toString(), annotation.encryptionKey()));
        }
        else {
          properties.setProperty(field.getName(), field.get(config).toString());
        }
      }
      catch (Exception ignored) {
      }
    }

    OutputStream output = null;
    try {
      output = new FileOutputStream(filename);
      properties.store(output, null);
      // properties.store(output, null);
    }
    catch (Exception ignored) {
    }
    finally {
      if (output != null) {
        try {
          output.close();
        }
        catch (Exception ignored) {
        }
      }
    }
  }

  /**
   * Convert the config object into a map containing the fields (mainly used for
   * representation in a UI)
   * 
   * @param config
   *          the object containing the configuration
   * @return a map containing the field/value pairs
   */
  public static Map<String, Object> getConfigElementsAsMap(Object config) {
    Map<String, Object> configMap = new HashMap<>();
    for (Field field : config.getClass().getFields()) {
      try {
        field.setAccessible(true);
        ScraperSetting annotation = field.getAnnotation(ScraperSetting.class);
        if (annotation == null) {
          continue;
        }
        configMap.put(field.getName(), field.get(config));
      }
      catch (Exception ignored) {
        ignored.printStackTrace();
      }
    }
    return configMap;
  }

  /**
   * Convert the config map (field/value pairs) to the config class (mainly used
   * after changing the config in a UI)
   * 
   * @param configMap
   *          the map containing the new config field/value pairs
   * @param config
   *          the object containing the configuration
   */
  public static void setConfigElementsFromMap(Map<String, Object> configMap, Object config) {
    // and now try to load these settings
    for (Field field : config.getClass().getFields()) {
      try {
        field.setAccessible(true);
        ScraperSetting annotation = field.getAnnotation(ScraperSetting.class);
        if (annotation == null) {
          continue;
        }
        field.set(config, configMap.get(field.getName()));
      }
      catch (Exception ignored) {
      }
    }
  }

  private static String encryptField(String value, String key) {
    return AES_UTIL.encrypt(SALT, IV, key, value);
  }

  private static String decryptField(String value, String key) {
    return AES_UTIL.decrypt(SALT, IV, key, value);
  }
}
