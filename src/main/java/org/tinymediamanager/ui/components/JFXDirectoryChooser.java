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
package org.tinymediamanager.ui.components;

import java.io.File;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * show a directory chooser by using the JavaFX directory chooser
 * 
 * @author Manuel Laggner
 */
public class JFXDirectoryChooser {
  private static final Logger LOGGER = LoggerFactory.getLogger(JFXDirectoryChooser.class);

  private Class<?>            clazz;
  private Object              directoryChooser;

  public JFXDirectoryChooser() throws Exception {
    clazz = Class.forName("javafx.stage.DirectoryChooser");
    directoryChooser = clazz.newInstance();
  }

  public File showDialog() {
    try {
      Method method = clazz.getMethod("showDialog", Class.forName("javafx.stage.Window"));
      Object obj = method.invoke(directoryChooser, new Object[] { null });
      if (obj instanceof File) {
        return (File) obj;
      }
      return null;
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
      return null;
    }
  }

  public void setInitialDirectory(File dir) {
    try {
      Method method = clazz.getMethod("setInitialDirectory", File.class);
      method.invoke(directoryChooser, dir);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }
  }

  public void setTitle(String name) {
    try {
      Method method = clazz.getMethod("setTitle", String.class);
      method.invoke(directoryChooser, name);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }
  }
}
