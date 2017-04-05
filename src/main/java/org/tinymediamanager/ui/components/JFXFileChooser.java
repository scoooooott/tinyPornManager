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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * show a file chooser by using the JavaFX file chooser
 * 
 * @author Manuel Laggner
 */
public class JFXFileChooser {
  private static final Logger LOGGER = LoggerFactory.getLogger(JFXFileChooser.class);

  private Class<?>            clazz;
  private Object              fileChooser;

  public JFXFileChooser() throws Exception {
    clazz = Class.forName("javafx.stage.FileChooser");
    fileChooser = clazz.newInstance();
  }

  public File showOpenDialog() {
    try {
      Method method = clazz.getMethod("showOpenDialog", Class.forName("javafx.stage.Window"));
      Object obj = method.invoke(fileChooser, new Object[] { null });
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

  @SuppressWarnings("unchecked")
  public List<File> showOpenMultipleDialog() {
    try {
      List<File> files = new ArrayList<>();

      Method method = clazz.getMethod("showOpenMultipleDialog", Class.forName("javafx.stage.Window"));
      Object objs = method.invoke(fileChooser, new Object[] { null });
      if (objs instanceof List) {
        files.addAll((Collection<? extends File>) objs);
        return files;
      }
      return null;
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
      return null;
    }
  }

  public File showSaveDialog() {
    try {
      Method method = clazz.getMethod("showSaveDialog", Class.forName("javafx.stage.Window"));
      Object obj = method.invoke(fileChooser, new Object[] { null });
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
      method.invoke(fileChooser, dir);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }
  }

  public void setInitialFileName(String name) {
    try {
      Method method = clazz.getMethod("setInitialFileName", String.class);
      method.invoke(fileChooser, name);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }
  }

  public void setTitle(String name) {
    try {
      Method method = clazz.getMethod("setTitle", String.class);
      method.invoke(fileChooser, name);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }
  }

  public String getTitle() {
    try {
      Method method = clazz.getMethod("getTitle");
      return (String) method.invoke(fileChooser);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
      return "";
    }
  }

  public File getInitialDirectory() {
    try {
      Method method = clazz.getMethod("getInitialDirectory");
      return (File) method.invoke(fileChooser);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public void addExtensionFilter(String description, List<String> ext) {
    try {
      Method method = clazz.getMethod("getExtensionFilters");
      Object obj = Class.forName("javafx.collections.ObservableList").cast(method.invoke(fileChooser));
      Object filter = Class.forName("javafx.stage.FileChooser$ExtensionFilter").getConstructor(String.class, List.class).newInstance(description,
          ext);
      ((List<Object>) obj).add(filter);
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }
  }
}
