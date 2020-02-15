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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Utility class fo UTF8 resource bundles<br>
 * See http://stackoverflow.com/questions/4428689/why-is-text-in-swedish-from-a- resource-bundle-showing-up-as-gibberish
 * 
 * @author Myron Boyle
 * @author BalusC
 */
public class UTF8Control extends Control {
  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
      throws IllegalAccessException, InstantiationException, IOException {
    // The below is a copy of the default implementation.
    String bundleName = toBundleName(baseName, locale);
    ResourceBundle bundle = null;
    if (format.equals("java.class")) {
      try {
        Class<? extends ResourceBundle> bundleClass = (Class<? extends ResourceBundle>) loader.loadClass(bundleName);

        // If the class isn't a ResourceBundle subclass, throw a
        // ClassCastException.
        if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
          bundle = bundleClass.newInstance();
        }
        else {
          throw new ClassCastException(bundleClass.getName() + " cannot be cast to ResourceBundle");
        }
      }
      catch (ClassNotFoundException ignored) {
      }
    }
    else if (format.equals("java.properties")) {
      final String resourceName = toResourceName0(bundleName, "properties");
      if (resourceName == null) {
        return bundle;
      }
      final ClassLoader classLoader = loader;
      final boolean reloadFlag = reload;
      InputStream stream = null;
      try {
        stream = AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>) () -> {
          InputStream is = null;
          if (reloadFlag) {
            URL url = classLoader.getResource(resourceName);
            if (url != null) {
              URLConnection connection = url.openConnection();
              if (connection != null) {
                // Disable caches to get fresh data for reloading.
                connection.setUseCaches(false);
                is = connection.getInputStream();
              }
            }
          }
          else {
            is = classLoader.getResourceAsStream(resourceName);
          }
          return is;
        });
      }
      catch (PrivilegedActionException e) {
        throw (IOException) e.getException();
      }

      if (stream != null) {
        try {
          // Only this line is changed to make it to read properties files as UTF-8.
          // bundle = new PropertyResourceBundle(stream);
          bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
        }
        finally {
          stream.close();
        }
      }
    }
    else {
      throw new IllegalArgumentException("unknown format: " + format);
    }

    return bundle;
  }

  private String toResourceName0(String bundleName, String suffix) {
    // application protocol check
    if (bundleName.contains("://")) {
      return null;
    }
    else {
      return toResourceName(bundleName, suffix);
    }
  }
}
