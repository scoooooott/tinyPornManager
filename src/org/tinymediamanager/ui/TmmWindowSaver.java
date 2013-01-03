package org.tinymediamanager.ui;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
    }
  }

  public void loadSettings(JFrame frame) {
    WindowConfig config = Globals.settings.getWindowConfig();
    // settings for main window
    if ("mainWindow".equals(frame.getName())) {
      // was the main window maximized?
      if (config.isMainWindowMaximized()) {
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.validate();
      }
      else {
        // only set location/size if something was stored
        if (config.getMainWindowHeight() > 0) {
          frame.setLocation(config.getMainWindowX(), config.getMainWindowY());
          frame.setSize(config.getMainWindowWidth(), config.getMainWindowHeight());
          frame.validate();
        }
      }

      System.out.println(config.getParam("mainMain"));

      // sliders
      MainWindow mainWindow = (MainWindow) frame;
      MoviePanel moviePanel = mainWindow.getMoviePanel();
      if (config.getMovieWindowSlider1Position() > 0) {
        moviePanel.getSplitPaneVertical().setDividerLocation(config.getMovieWindowSlider1Position());
      }
      if (config.getMovieWindowSlider2Position() > 0) {
        moviePanel.getSplitPaneHorizontal().setDividerLocation(config.getMovieWindowSlider2Position());
      }
    }
  }

  public void loadSettings(JPanel panel) {
    WindowConfig config = Globals.settings.getWindowConfig();
  }

  public void saveSettings(JFrame frame) {
    WindowConfig config = Globals.settings.getWindowConfig();
    // settings for main window
    if ("mainWindow".equals(frame.getName()) && frame instanceof MainWindow) {
      config.setMainWindowX(frame.getX());
      config.setMainWindowY(frame.getY());
      config.setMainWindowWidth(frame.getWidth());
      config.setMainWindowHeight(frame.getHeight());
      config.setMainWindowMaximized((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);

      config.addParam("mainMain", Integer.valueOf(frame.getX()));

      // sliders
      MainWindow mainWindow = (MainWindow) frame;
      MoviePanel moviePanel = mainWindow.getMoviePanel();
      config.setMovieWindowSlider1Position(moviePanel.getSplitPaneVertical().getDividerLocation());
      config.setMovieWindowSlider2Position(moviePanel.getSplitPaneHorizontal().getDividerLocation());
    }
  }
}