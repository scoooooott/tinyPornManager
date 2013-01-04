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

public class TmmWindowSaver implements AWTEventListener {

  private static final Logger   LOGGER = Logger.getLogger(TmmWindowSaver.class);

  private static TmmWindowSaver instance;

  private TmmWindowSaver() {
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
          loadSettings(dialog);
        }
      }
      catch (Exception ex) {
        LOGGER.warn("failed to restore window layout", ex);
      }
    }

    // save settings
    if (evt.getID() == WindowEvent.WINDOW_CLOSING) {
      ComponentEvent cev = (ComponentEvent) evt;
      System.out.println(cev);
      // frame = mainWindow
      if (cev.getComponent() instanceof JFrame) {
        JFrame frame = (JFrame) cev.getComponent();
        saveSettings(frame);
      }
      // popup dialogs
      if (cev.getComponent() instanceof JDialog) {
        JDialog dialog = (JDialog) cev.getComponent();
        saveSettings(dialog);
      }
    }
  }

  public void loadSettings(JFrame frame) {
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
          frame.validate();
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

  public void loadSettings(JDialog dialog) {
    WindowConfig config = Globals.settings.getWindowConfig();
    if (!dialog.getName().contains("dialog")) {
      Rectangle rect = config.getWindowBounds(dialog.getName());
      if (rect.width > 0) {
        dialog.setBounds(rect);
      }
    }
  }

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

  public void saveSettings(JDialog dialog) {
    WindowConfig config = Globals.settings.getWindowConfig();
    if (!dialog.getName().contains("dialog")) {
      config.storeWindowBounds("movieChooser", dialog.getX(), dialog.getY(), dialog.getWidth(), dialog.getHeight());
    }
  }
}