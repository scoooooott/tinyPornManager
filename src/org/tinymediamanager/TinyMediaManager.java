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

package org.tinymediamanager;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.io.File;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;

import com.sun.jna.Platform;

/**
 * The Class TinyMediaManager.
 */
public class TinyMediaManager {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(TinyMediaManager.class); ;

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          Toolkit tk = Toolkit.getDefaultToolkit();
          tk.addAWTEventListener(TmmWindowSaver.getInstance(), AWTEvent.WINDOW_EVENT_MASK);

          // set look and feel
          setLookAndFeel();

          // check old version
          File file = new File("lib/beansbinding-1.2.1.jar");
          if (file.exists()) {
            JOptionPane.showMessageDialog(null, "Update from Alpha is not supported. Please download the actual version");
            return;
          }

          doUpgradeTasks();

          // init splash
          SplashScreen splash = SplashScreen.getSplashScreen();
          Graphics2D g2 = null;
          if (splash != null) {
            g2 = splash.createGraphics();
            if (g2 != null) {
              Font font = new Font("Dialog", Font.PLAIN, 14);
              g2.setFont(font);
            }
            else {
              LOGGER.debug("got no graphics from splash");
            }
          }
          else {
            LOGGER.debug("no splash found");
          }
          long timeStart = System.currentTimeMillis();

          // update check //////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "update check", 10);
            splash.update();
          }
          // write a random number to file, to identify this instance (for
          // updater, tracking, whatsoever)
          Utils.trackEvent("startup");

          LOGGER.debug("starting tinyMediaManager");
          LOGGER.debug("default encoding " + System.getProperty("file.encoding"));

          // initialize database //////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "initialize database", 20);
            splash.update();
          }

          LOGGER.debug("initialize database");
          Globals.startDatabase();
          LOGGER.debug("database opened");

          // proxy settings
          if (Globals.settings.useProxy()) {
            LOGGER.debug("setting proxy");
            Globals.settings.setProxy();
          }

          // load database //////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading movies", 30);
            splash.update();
          }

          MovieList movieList = MovieList.getInstance();
          movieList.loadMoviesFromDatabase();

          // set native dir (needs to be absolute)
          String nativepath = TinyMediaManager.class.getClassLoader().getResource(".").getPath() + "native/";
          if (Platform.isWindows()) {
            nativepath += "windows-";
          }
          else if (Platform.isLinux()) {
            nativepath += "linux-";
          }
          else if (Platform.isMac()) {
            nativepath += "mac-";
          }
          nativepath += System.getProperty("os.arch");
          System.setProperty("jna.library.path", nativepath);

          // MediaInfo /////////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading MediaInfo libs", 50);
            splash.update();
          }
          LOGGER.debug("Loading native mediainfo lib from: " + nativepath);
          // load libMediainfo
          String miv = MediaInfo.version();
          if (!StringUtils.isEmpty(miv)) {
            LOGGER.info("Using " + miv);
          }
          else {
            LOGGER.error("could not load MediaInfo!");
          }

          // VLC /////////////////////////////////////////////////////////
          // // try to initialize VLC native libs
          // if (g2 != null) {
          // updateProgress(g2, "loading VLC libs", 60);
          // splash.update();
          // }
          // try {
          // // add -Dvlcj.log=DEBUG to VM arguments
          // new NativeDiscovery().discover();
          // Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(),
          // LibVlc.class);
          // LOGGER.info("VLC: native libraries found and loaded :)");
          // }
          // catch (UnsatisfiedLinkError ule) {
          // LOGGER.warn("VLC: " + ule.getMessage().trim());
          // }

          // clean cache ////////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading movies", 80);
            splash.update();
          }
          CachedUrl.cleanupCache();

          // launch application ////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading ui", 90);
            splash.update();
          }
          long timeEnd = System.currentTimeMillis();
          if ((timeEnd - timeStart) > 3000) {
            try {
              Thread.sleep(3000 - (timeEnd - timeStart));
            }
            catch (Exception e) {
            }
          }
          MainWindow window = new MainWindow("tinyMediaManager / " + ReleaseInfo.getVersion() + " - " + ReleaseInfo.getBuild());

          // finished ////////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "finished starting", 100);
            splash.update();
          }

          TmmWindowSaver.loadSettings(window);
          window.setVisible(true);

        }
        catch (javax.persistence.PersistenceException e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
        }
        catch (Exception e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
          LOGGER.error("start of tmm", e);
        }
      }

      /**
       * Update progress on splash screen.
       * 
       * @param text
       *          the text
       */
      private void updateProgress(Graphics2D g2, String text, int progress) {
        // LOGGER.debug("graphics found");
        Object oldAAValue = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(20, 200, 480, 305);
        g2.setPaintMode();
        g2.setColor(Color.WHITE);
        g2.drawString(text + "...", 20, 295);
        g2.fillRect(20, 300, 460 * progress / 100, 10);
        g2.drawString(ReleaseInfo.getVersion() + " " + ReleaseInfo.getBuild(), 430, 325);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAAValue);
      }

      /**
       * Sets the look and feel.
       * 
       * @throws Exception
       *           the exception
       */
      private void setLookAndFeel() throws Exception {
        // Get the native look and feel class name
        // String laf = UIManager.getSystemLookAndFeelClassName();
        Properties props = new Properties();
        props.setProperty("controlTextFont", "Dialog 12");
        props.setProperty("systemTextFont", "Dialog 12");
        props.setProperty("userTextFont", "Dialog 12");
        props.setProperty("menuTextFont", "Dialog 12");
        props.setProperty("windowTitleFont", "Dialog bold 12");
        props.setProperty("subTextFont", "Dialog 10");
        props.setProperty("backgroundColor", "237 237 237");
        props.setProperty("menuBackgroundColor", "237 237 237");
        props.setProperty("menuColorLight", "237 237 237");
        props.setProperty("menuColorDark", "237 237 237");
        props.setProperty("toolbarColorLight", "237 237 237");
        props.setProperty("toolbarColorDark", "237 237 237");
        props.put("windowDecoration", "system");
        props.put("logoString", "");

        // Get the look and feel class name
        com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme(props);
        String laf = "com.jtattoo.plaf.luna.LunaLookAndFeel";

        // Install the look and feel
        UIManager.setLookAndFeel(laf);
      }

      /**
       * does upgrade tasks, such as deleting old libs
       */
      private void doUpgradeTasks() {
        File file = new File("lib/jackson-core-lgpl.jar");
        if (file.exists()) {
          FileUtils.deleteQuietly(file);
        }
        file = new File("lib/jackson-core-lgpl.jarv");
        if (file.exists()) {
          FileUtils.deleteQuietly(file);
        }
        file = new File("lib/jackson-mapper-lgpl.jar");
        if (file.exists()) {
          FileUtils.deleteQuietly(file);
        }
        file = new File("lib/jackson-mapper-lgpl.jarv");
        if (file.exists()) {
          FileUtils.deleteQuietly(file);
        }
      }
    });
  }
}
