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

import java.awt.AWTEvent;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.WindowConfig;
import org.tinymediamanager.ui.movies.MoviePanel;

/**
 * The Class TmmWindowSaver.
 */
public class TmmWindowSaver implements AWTEventListener {

  /** The Constant LOGGER. */
  private static final Logger   LOGGER = Logger.getLogger(TmmWindowSaver.class);

  /** The instance. */
  private static TmmWindowSaver instance;

  /**
   * Instantiates a new tmm window saver.
   */
  private TmmWindowSaver() {
  }

  /**
   * Gets the single instance of TmmWindowSaver.
   * 
   * @return single instance of TmmWindowSaver
   */
  public static TmmWindowSaver getInstance() {
    if (instance == null) {
      instance = new TmmWindowSaver();
    }
    return instance;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.AWTEventListener#eventDispatched(java.awt.AWTEvent)
   */
  @Override
  public void eventDispatched(AWTEvent evt) {
    // // load settings
    // if (evt.getID() == WindowEvent.WINDOW_OPENED) {
    // try {
    // ComponentEvent cev = (ComponentEvent) evt;
    // // frame = mainWindow
    // if (cev.getComponent() instanceof JFrame) {
    // JFrame frame = (JFrame) cev.getComponent();
    // loadSettings(frame);
    // }
    // // popup dialogs
    // if (cev.getComponent() instanceof JDialog) {
    // JDialog dialog = (JDialog) cev.getComponent();
    // loadSettings(dialog);
    // }
    // }
    // catch (Exception ex) {
    // LOGGER.warn("failed to restore window layout", ex);
    // }
    // }

    // save settings
    // if (evt.getID() == WindowEvent.WINDOW_CLOSING) {

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
   * Load settings.
   * 
   * @param frame
   *          the frame
   */
  public static void loadSettings(JFrame frame) {
    WindowConfig config = Globals.settings.getWindowConfig();
    // settings for main window
    if ("mainWindow".equals(frame.getName())) {
      // was the main window maximized?
      if (config.getBoolean("mainWindowMaximized")) {
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.validate();
      }
      else {
        // only set location/size if something was stored
        Rectangle rect = config.getWindowBounds("mainWindow");
        if (rect.width > 0) {
          frame.setBounds(rect);
          // frame.validate();
        }
      }

      // sliders
      MainWindow mainWindow = (MainWindow) frame;
      MoviePanel moviePanel = mainWindow.getMoviePanel();
      if (config.getInteger("movieWindowSlider1") > 0) {
        moviePanel.getSplitPaneVertical().setDividerLocation(config.getInteger("movieWindowSlider1"));
      }
      if (config.getInteger("movieWindowSlider2") > 0) {
        moviePanel.getSplitPaneHorizontal().setDividerLocation(config.getInteger("movieWindowSlider2"));
      }
    }
  }

  /**
   * Load settings.
   * 
   * @param dialog
   *          the dialog
   */
  public static void loadSettings(JDialog dialog) {
    WindowConfig config = Globals.settings.getWindowConfig();
    if (!dialog.getName().contains("dialog")) {
      Rectangle rect = config.getWindowBounds(dialog.getName());
      if (rect.width > 0) {
        dialog.setBounds(rect);
      }
    }
  }

  /**
   * Save settings.
   * 
   * @param frame
   *          the frame
   */
  public void saveSettings(JFrame frame) {
    WindowConfig config = Globals.settings.getWindowConfig();
    // settings for main window
    if ("mainWindow".equals(frame.getName()) && frame instanceof MainWindow) {
      config.addParam("mainWindowMaximized", (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
      config.storeWindowBounds("mainWindow", frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight());

      // sliders
      MainWindow mainWindow = (MainWindow) frame;
      MoviePanel moviePanel = mainWindow.getMoviePanel();
      config.addParam("movieWindowSlider1", moviePanel.getSplitPaneVertical().getDividerLocation());
      config.addParam("movieWindowSlider2", moviePanel.getSplitPaneHorizontal().getDividerLocation());
    }
  }

  /**
   * Save settings.
   * 
   * @param dialog
   *          the dialog
   */
  public void saveSettings(JDialog dialog) {
    WindowConfig config = Globals.settings.getWindowConfig();
    if (!dialog.getName().contains("dialog")) {
      config.storeWindowBounds(dialog.getName(), dialog.getX(), dialog.getY(), dialog.getWidth(), dialog.getHeight());
    }
  }
}