package org.tinymediamanager.ui;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.WindowConfig;

public class TmmWindowSaver implements AWTEventListener {

  private static final Logger   LOGGER = Logger.getLogger(TmmWindowSaver.class);

  private static TmmWindowSaver instance;

  private Map                   framemap;

  private TmmWindowSaver() {
    framemap = new HashMap();
  }

  public static TmmWindowSaver getInstance() {
    if (instance == null) {
      instance = new TmmWindowSaver();
    }
    return instance;
  }

  @Override
  public void eventDispatched(AWTEvent evt) {
    // load settings
    if (evt.getID() == WindowEvent.WINDOW_OPENED) {
      try {
        ComponentEvent cev = (ComponentEvent) evt;
        // frame = mainWindow
        if (cev.getComponent() instanceof JFrame) {
          JFrame frame = (JFrame) cev.getComponent();
          loadSettings(frame);
        }
        // popup dialogs
        if (cev.getComponent() instanceof JDialog) {
          JDialog dialog = (JDialog) cev.getComponent();
          // loadSettings(frame);
          System.out.println(dialog.getName());
        }
      }
      catch (Exception ex) {
        LOGGER.warn("failed to restore window layout", ex);
      }
    }

    // save settings
    if (evt.getID() == WindowEvent.WINDOW_CLOSING) {
      ComponentEvent cev = (ComponentEvent) evt;
      // frame = mainWindow
      if (cev.getComponent() instanceof JFrame) {
        JFrame frame = (JFrame) cev.getComponent();
        saveSettings(frame);
      }
      // popup dialogs
      if (cev.getComponent() instanceof JDialog) {
        JDialog dialog = (JDialog) cev.getComponent();
        // loadSettings(frame);
        System.out.println(dialog.getName());
      }
      System.out.println(evt);
    }
  }

  public void loadSettings(JFrame frame) {
    WindowConfig config = Globals.settings.getWindowConfig();
    // settings for main window
    if ("mainWindow".equals(frame.getName())) {
      // only set location/size if something was stored
      if (config.isMainWindowMaximized()) {
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.validate();
      }
      else {
        if (config.getMainWindowHeight() > 0) {
          frame.setLocation(config.getMainWindowX(), config.getMainWindowY());
          frame.setSize(config.getMainWindowWidth(), config.getMainWindowHeight());
          frame.validate();
        }
      }
    }
  }

  public void saveSettings(JFrame frame) {
    WindowConfig config = Globals.settings.getWindowConfig();
    // settings for main window
    if ("mainWindow".equals(frame.getName())) {
      config.setMainWindowX(frame.getX());
      config.setMainWindowY(frame.getY());
      config.setMainWindowWidth(frame.getWidth());
      config.setMainWindowHeight(frame.getHeight());
      config.setMainWindowMaximized((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
    }
  }
}