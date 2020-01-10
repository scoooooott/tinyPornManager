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
package org.tinymediamanager.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;

/**
 * the class TmmProperties is used to store several UI related settings
 *
 * @author Manuel Laggner
 */
public class TmmProperties {
  private static final Logger  LOGGER          = LoggerFactory.getLogger(TmmProperties.class);
  private static final String  PROPERTIES_FILE = "tmm.prop";
  private static TmmProperties instance;

  private Properties           properties;

  private TmmProperties() {
    properties = new Properties();

    properties = new Properties();

    try (InputStream input = new FileInputStream(new File(Globals.settings.getSettingsFolder(), PROPERTIES_FILE))) {
      properties.load(input);
    }
    catch (FileNotFoundException ignored) {
    }
    catch (Exception e) {
      LOGGER.warn("unable to read properties file: {}", e.getMessage());
    }
  }

  /**
   * the an instance of this class
   * 
   * @return an instance of this class
   */
  public synchronized static TmmProperties getInstance() {
    if (instance == null) {
      instance = new TmmProperties();
    }
    return instance;
  }

  private void writeProperties() {
    OutputStream output = null;
    try {
      output = new FileOutputStream(new File(Settings.getInstance().getSettingsFolder(), PROPERTIES_FILE));
      Properties tmp = new Properties() {
        private static final long serialVersionUID = 1L;

        @Override
        public synchronized Enumeration<Object> keys() {
          return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
      };
      tmp.putAll(properties);
      tmp.store(output, null);
    }
    catch (IOException e) {
      LOGGER.warn("failed to store properties file: {}", e.getMessage());
    }
    finally {
      if (output != null) {
        try {
          output.close();
        }
        catch (IOException e) {
          LOGGER.warn("failed to store properties file: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * put a key/value pair into the properties file
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void putProperty(String key, String value) {
    if (properties.containsKey(key)) {
      properties.remove(key);
    }

    properties.put(key, value);
    writeProperties();
  }

  /**
   * get the value for the given key
   * 
   * @param key
   *          the key to search the value for
   * @return the value or null
   */
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * get the value as Boolean<br>
   * if the value is not available or not parseable, this will return {@literal Boolean.FALSE}
   *
   * @param key
   *          the key to search the value for
   * @return true or false
   */
  public Boolean getPropertyAsBoolean(String key) {
    String value = properties.getProperty(key);
    if (StringUtils.isBlank(value)) {
      return Boolean.FALSE;
    }

    try {
      return Boolean.parseBoolean(value);
    }
    catch (Exception ignored) {
    }

    return Boolean.FALSE;
  }

  /**
   * get the value as Integer<br>
   * if the value is not available or not parseable, this will return zero
   * 
   * @param key
   *          the key to search the value for
   * @return the value or zero
   */
  public Integer getPropertyAsInteger(String key) {
    String value = properties.getProperty(key);
    if (StringUtils.isBlank(value)) {
      return 0;
    }

    try {
      return Integer.parseInt(value);
    }
    catch (Exception ignored) {
    }

    return 0;
  }
}
