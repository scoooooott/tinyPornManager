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

/**
 * The class ConfigHelper is a helper class for loading and storing scraper
 * config files via reflection
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public final class ConfigHelper {

  /**
   * load the properties file specified via the parameter filename and assign
   * the values via reflection to the object config.
   * 
   * @param filename
   *          the file name of the config properties file
   * @param config
   *          the object to store the configuration via reflection
   */
  public static void loadConfig(String filename, Object config) {
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
        Method method = field.getType().getMethod("valueOf", String.class);
        if (method != null) {
          field.set(config, method.invoke(null, properties.getProperty(field.getName())));
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
   * @param filename
   *          the file name to store the config to
   * @param config
   *          the object to extract the config from
   */
  public static void saveConfig(String filename, Object config) {
    // save properties
    Properties properties = new Properties();

    for (Field field : config.getClass().getFields()) {
      try {
        field.setAccessible(true);
        properties.setProperty(field.getName(), field.get(config).toString());
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
        field.set(config, configMap.get(field.getName()));
      }
      catch (Exception ignored) {
      }
    }
  }
}
