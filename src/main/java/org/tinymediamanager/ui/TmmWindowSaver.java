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
package org.tinymediamanager.ui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.ui.movies.MoviePanel;

/**
 * The Class TmmWindowSaver. To save window/dialog settings (like size/position)
 * 
 * @author Manuel Laggner
 */
public class TmmWindowSaver implements AWTEventListener {
  private final static Logger   LOGGER = LoggerFactory.getLogger(TmmWindowSaver.class);
  private static TmmWindowSaver instance;

  private final TmmProperties   properties;

  private TmmWindowSaver() {
    properties = TmmProperties.getInstance();
  }

  /**
   * get an instance of this class
   * 
   * @return an instance of this class
   */
  public synchronized static TmmWindowSaver getInstance() {
    if (instance == null) {
      instance = new TmmWindowSaver();
    }
    return instance;
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
      if (properties.getPropertyAsBoolean("mainWindowMaximized")) {
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
      if (properties.getPropertyAsInteger("movieWindowSlider1") > 0) {
        moviePanel.getSplitPaneVertical().setDividerLocation(properties.getPropertyAsInteger("movieWindowSlider1"));
      }
      if (properties.getPropertyAsInteger("movieWindowSlider2") > 0) {
        moviePanel.getSplitPaneHorizontal().setDividerLocation(properties.getPropertyAsInteger("movieWindowSlider2"));
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
      else {
        dialog.pack();
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
    }
  }

  private void storeWindowBounds(String name, int x, int y, int width, int height) {
    addParam(name + "X", x);
    addParam(name + "Y", y);
    addParam(name + "W", width);
    addParam(name + "H", height);
  }

  private Rectangle getWindowBounds(String name) {
    Rectangle rect = new Rectangle();

    rect.x = properties.getPropertyAsInteger(name + "X");
    rect.y = properties.getPropertyAsInteger(name + "Y");
    rect.width = properties.getPropertyAsInteger(name + "W");
    rect.height = properties.getPropertyAsInteger(name + "H");

    // check if the stored sizes fit the actual screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // screen insets / taskbar
    if (MainWindow.getActiveInstance() != null) {
      Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(MainWindow.getActiveInstance().getGraphicsConfiguration());
      if ((rect.x + rect.width) > (screenSize.getWidth() - scnMax.left - scnMax.right)) {
        rect.x = scnMax.left;
        rect.width = (int) screenSize.getWidth() - scnMax.right;
      }

      if ((rect.y + rect.height) > (screenSize.getHeight() - scnMax.top - scnMax.bottom)) {
        rect.y = scnMax.top;
        rect.height = (int) screenSize.getHeight() - scnMax.bottom;
      }
    }

    return rect;
  }

  private void addParam(String key, Object value) {
    properties.putProperty(key, value.toString());
  }
}
