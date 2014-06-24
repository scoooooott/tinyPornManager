/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.awt.AWTEvent;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ui.movies.MoviePanel;

/**
 * The Class TmmWindowSaver. To save window/dialog settings (like size/position)
 * 
 * @author Manuel Laggner
 */
public class TmmWindowSaver implements AWTEventListener {
  private final static Logger   LOGGER          = LoggerFactory.getLogger(MainWindow.class);
  private final static String   PROPERTIES_FILE = "tmm_ui.prop";
  private static TmmWindowSaver instance;

  private Properties            properties;

  private TmmWindowSaver() {
    properties = new Properties();

    InputStream input = null;
    try {
      input = new FileInputStream(PROPERTIES_FILE);
      properties.load(input);
    }
    catch (FileNotFoundException e) {
    }
    catch (Exception e) {
      LOGGER.warn("unable to read window config: " + e.getMessage());
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (IOException e) {
        }
      }
    }
  }

  public static TmmWindowSaver getInstance() {
    if (instance == null) {
      instance = new TmmWindowSaver();
    }
    return instance;
  }

  private void writeProperties() {
    OutputStream output = null;
    try {
      output = new FileOutputStream(PROPERTIES_FILE);
      properties.store(output, null);
    }
    catch (IOException e) {
      LOGGER.warn("failed to store window config: " + e.getMessage());
    }
    finally {
      if (output != null) {
        try {
          output.close();
        }
        catch (IOException e) {
          LOGGER.warn("failed to store window config: " + e.getMessage());
        }
      }
    }
  }

  @Override
  public void eventDispatched(AWTEvent evt) {
    ComponentEvent cev = (ComponentEvent) evt;
    // frame = mainWindow
    if (evt.getID() == WindowEvent.WINDOW_CLOSING && cev.getComponent() instanceof JFrame) {
      JFrame frame = (JFrame) cev.getComponent();
      saveSettings(frame);
    }
    // popup dialogs
    if (evt.getID() == WindowEvent.WINDOW_CLOSED && cev.getComponent() instanceof JDialog) {
      JDialog dialog = (JDialog) cev.getComponent();
      saveSettings(dialog);
    }
  }

  /**
   * Load settings for a frame
   * 
   * @param frame
   *          the frame
   */
  public void loadSettings(JFrame frame) {
    // settings for main window
    if ("mainWindow".equals(frame.getName())) {
      // was the main window maximized?
      if (getBoolean("mainWindowMaximized")) {
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.validate();
      }
      else {
        // only set location/size if something was stored
        Rectangle rect = getWindowBounds("mainWindow");
        if (rect.width > 0) {
          frame.setBounds(rect);
          // frame.validate();
        }
        else {
          frame.setLocationRelativeTo(null);
        }
      }

      // sliders
      MainWindow mainWindow = (MainWindow) frame;
      MoviePanel moviePanel = mainWindow.getMoviePanel();
      if (getInteger("movieWindowSlider1") > 0) {
        moviePanel.getSplitPaneVertical().setDividerLocation(getInteger("movieWindowSlider1"));
      }
      if (getInteger("movieWindowSlider2") > 0) {
        moviePanel.getSplitPaneHorizontal().setDividerLocation(getInteger("movieWindowSlider2"));
      }
    }
  }

  /**
   * Load settings for a dialog
   * 
   * @param dialog
   *          the dialog
   */
  public void loadSettings(JDialog dialog) {
    if (!dialog.getName().contains("dialog")) {
      Rectangle rect = getWindowBounds(dialog.getName());
      if (rect.width > 0) {
        dialog.setBounds(rect);
      }
    }
  }

  /**
   * Save settings for a frame
   * 
   * @param frame
   *          the frame
   */
  public void saveSettings(JFrame frame) {
    // settings for main window
    if ("mainWindow".equals(frame.getName()) && frame instanceof MainWindow) {
      addParam("mainWindowMaximized", (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
      storeWindowBounds("mainWindow", frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight());

      // sliders
      MainWindow mainWindow = (MainWindow) frame;
      MoviePanel moviePanel = mainWindow.getMoviePanel();
      addParam("movieWindowSlider1", moviePanel.getSplitPaneVertical().getDividerLocation());
      addParam("movieWindowSlider2", moviePanel.getSplitPaneHorizontal().getDividerLocation());
      writeProperties();
    }
  }

  /**
   * Save settings for a dialog
   * 
   * @param dialog
   *          the dialog
   */
  public void saveSettings(JDialog dialog) {
    if (!dialog.getName().contains("dialog")) {
      storeWindowBounds(dialog.getName(), dialog.getX(), dialog.getY(), dialog.getWidth(), dialog.getHeight());
      writeProperties();
    }
  }

  public boolean getBoolean(String name) {
    boolean b = false;

    Object param = properties.get(name);

    if (param != null && param instanceof Boolean) {
      Boolean bool = (Boolean) param;
      b = bool;
    }
    else if (param != null) {
      try {
        b = Boolean.parseBoolean(param.toString());
      }
      catch (Exception e) {
      }
    }

    return b;
  }

  public int getInteger(String name) {
    int i = 0;
    Object param = properties.get(name);

    if (param != null && param instanceof Integer) {
      Integer integer = (Integer) param;
      i = integer;
    }
    else if (param != null) {
      try {
        i = Integer.parseInt(param.toString());
      }
      catch (Exception e) {
      }
    }

    return i;
  }

  private void storeWindowBounds(String name, int x, int y, int width, int height) {
    addParam(name + "X", x);
    addParam(name + "Y", y);
    addParam(name + "W", width);
    addParam(name + "H", height);
  }

  private Rectangle getWindowBounds(String name) {
    Rectangle rect = new Rectangle();

    rect.x = getInteger(name + "X");
    rect.y = getInteger(name + "Y");
    rect.width = getInteger(name + "W");
    rect.height = getInteger(name + "H");

    return rect;
  }

  private void addParam(String key, Object value) {
    if (properties.containsKey(key)) {
      properties.remove(key);
    }

    properties.put(key, value.toString());
  }
}
