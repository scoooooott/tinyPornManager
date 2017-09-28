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
package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The class ApiResourceBundle. To provide a ResourceBundle for API tests (where the official tmm ResourceBundle is not available)
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class ApiResourceBundle extends ResourceBundle {
  private static ResourceBundle INSTANCE = new ApiResourceBundle();

  @SuppressWarnings("unchecked")
  public static ResourceBundle getResourceBundle() {
    try {
      Class<Control> clazz = (Class<Control>) Class.forName("org.tinymediamanager.ui.UTF8Control");
      return ResourceBundle.getBundle("messages", clazz.newInstance());
    }
    catch (Exception e) {
      return INSTANCE;
    }
  }

  @SuppressWarnings("unchecked")
  public static ResourceBundle getResourceBundle(Locale loc) {
    try {
      Class<Control> clazz = (Class<Control>) Class.forName("org.tinymediamanager.ui.UTF8Control");
      return ResourceBundle.getBundle("messages", loc, clazz.newInstance());
    }
    catch (Exception e) {
      return INSTANCE;
    }
  }

  @Override
  protected Object handleGetObject(String key) {
    return "";
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(new ArrayList<String>(0));
  }
}
