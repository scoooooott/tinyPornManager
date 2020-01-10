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
package org.tinymediamanager.ui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.TmmProperties;

/**
 * The Class TmmWindowSaver. To save window/dialog settings (like size/position)
 * 
 * @author Manuel Laggner
 */
public class TmmWindowSaver implements AWTEventListener {
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
    if (!Globals.settings.isStoreWindowPreferences()) {
      // at least display the main frame centered
      if ("mainWindow".equals(frame.getName())) {
        frame.setLocationRelativeTo(null);
      }
      return;
    }

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

      // splitpane divider
      MainWindow mainWindow = (MainWindow) frame;
      if (properties.getPropertyAsInteger("splitPaneDividerLocation") > 0) {
        mainWindow.getSplitPane().setDividerLocation(properties.getPropertyAsInteger("splitPaneDividerLocation"));
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
    if (!Globals.settings.isStoreWindowPreferences()) {
      dialog.pack();
      dialog.setLocationRelativeTo(dialog.getParent());
      return;
    }

    if (!dialog.getName().contains("dialog")) {
      Rectangle rect = getWindowBounds(dialog.getName());
      if (rect.width > 0 && getVirtualBounds().contains(rect)) {
        dialog.setBounds(rect);
      }
      else {
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getParent());
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
    if (!Globals.settings.isStoreWindowPreferences()) {
      return;
    }

    // settings for main window
    if ("mainWindow".equals(frame.getName()) && frame instanceof MainWindow) {
      addParam("mainWindowMaximized", (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
      storeWindowBounds("mainWindow", frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight());

      MainWindow mainWindow = (MainWindow) frame;
      addParam("splitPaneDividerLocation", mainWindow.getSplitPane().getDividerLocation());
    }
  }

  /**
   * Save settings for a dialog
   * 
   * @param dialog
   *          the dialog
   */
  public void saveSettings(JDialog dialog) {
    if (!Globals.settings.isStoreWindowPreferences()) {
      return;
    }

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

  private Rectangle getVirtualBounds() {
    Rectangle bounds = new Rectangle(0, 0, 0, 0);
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice lstGDs[] = ge.getScreenDevices();
    for (GraphicsDevice gd : lstGDs) {
      bounds.add(gd.getDefaultConfiguration().getBounds());
    }
    return bounds;
  }
}
