/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Utility class fo UTF8 resource bundles<br>
 * See http://stackoverflow.com/questions/4428689/why-is-text-in-swedish-from-a-
 * resource-bundle-showing-up-as-gibberish
 * 
 * @author Myron Boyle
 * @author BalusC
 */
public class UTF8Control extends Control {
  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException,
      InstantiationException, IOException {
    // The below is a copy of the default implementation.
    String bundleName = toBundleName(baseName, locale);
    String resourceName = toResourceName(bundleName, "properties");
    ResourceBundle bundle = null;
    InputStream stream = null;
    if (reload) {
      URL url = loader.getResource(resourceName);
      if (url != null) {
        URLConnection connection = url.openConnection();
        if (connection != null) {
          connection.setUseCaches(false);
          stream = connection.getInputStream();
        }
      }
    }
    else {
      stream = loader.getResourceAsStream(resourceName);
    }
    if (stream != null) {
      try {
        // Only this line is changed to make it to read properties files as
        // UTF-8.
        bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
      }
      finally {
        stream.close();
      }
    }
    return bundle;
  }
}