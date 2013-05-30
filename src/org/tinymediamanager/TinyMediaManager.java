/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.ELProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TmmWindowSaver;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

import com.sun.jna.Platform;

/**
 * The Class TinyMediaManager.
 * 
 * @author Manuel Laggner
 */
public class TinyMediaManager {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TinyMediaManager.class); ;

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    // initialize SWT if needed
    TmmUIHelper.init();
    if (TmmUIHelper.swt != null) {
      NativeInterface.open();
    }

    // START character encoding debug
    debugCharacterEncoding();
    System.setProperty("file.encoding", "UTF-8");
    Field charset;
    try {
      // we cannot (re)set the properties while running inside JVM
      // so we trick it to reread it by setting them to null ;)
      charset = Charset.class.getDeclaredField("defaultCharset");
      charset.setAccessible(true);
      charset.set(null, null);
    }
    catch (NoSuchFieldException e1) {
      LOGGER.warn("Error resetting to UTF-8");
    }
    catch (SecurityException e1) {
      LOGGER.warn("Error resetting to UTF-8");
    }
    catch (IllegalArgumentException e) {
      LOGGER.warn("Error resetting to UTF-8");
    }
    catch (IllegalAccessException e) {
      LOGGER.warn("Error resetting to UTF-8");
    }
    debugCharacterEncoding();
    // END character encoding debug

    // start EDT
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          Thread.setDefaultUncaughtExceptionHandler(new Log4jBackstop());
          Thread.currentThread().setName("main");

          Toolkit tk = Toolkit.getDefaultToolkit();
          tk.addAWTEventListener(TmmWindowSaver.getInstance(), AWTEvent.WINDOW_EVENT_MASK);
          setLookAndFeel();
          doStartupTasks();

          // after 5 secs of beeing idle, the threads are removed till 0; see Globals
          Globals.executor.allowCoreThreadTimeOut(true);

          // suppress logging messages from betterbeansbinding
          org.jdesktop.beansbinding.util.logging.Logger.getLogger(ELProperty.class.getName()).setLevel(Level.SEVERE);

          // upgrade check
          if (!Globals.settings.isCurrentVersion()) {
            JOptionPane.showMessageDialog(null, "The configuration format changed in this update.\nPlease check your settings!");
            doUpgradeTasks(Globals.settings.getVersion()); // do the upgrade tasks for the old version
            Globals.settings.writeDefaultSettings(); // write current default
          }

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

          // update check //////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "update check", 10);
            splash.update();
          }

          LOGGER.info("starting tinyMediaManager");
          LOGGER.info("default encoding " + System.getProperty("file.encoding"));

          // initialize database //////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "initialize database", 20);
            splash.update();
          }

          LOGGER.info("initialize database");
          Globals.startDatabase();
          LOGGER.debug("database opened");

          // proxy settings
          if (Globals.settings.useProxy()) {
            LOGGER.info("setting proxy");
            Globals.settings.setProxy();
          }

          // load database //////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading movies", 30);
            splash.update();
          }

          MovieList movieList = MovieList.getInstance();
          movieList.loadMoviesFromDatabase();

          TvShowList tvShowList = TvShowList.getInstance();
          tvShowList.loadTvShowsFromDatabase();

          // set native dir (needs to be absolute)
          // String nativepath =
          // TinyMediaManager.class.getClassLoader().getResource(".").getPath()
          // + "native/";
          String nativepath = "native/";
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
          MainWindow window = new MainWindow("tinyMediaManager / " + ReleaseInfo.getVersion() + " - " + ReleaseInfo.getBuild());

          // finished ////////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "finished starting", 100);
            splash.update();
          }

          // write a random number to file, to identify this instance (for
          // updater, tracking, whatsoever)
          Utils.trackEvent("startup");

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
        // props.setProperty("tooltipBackgroundColor", "237 237 237");
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
      private void doUpgradeTasks(String version) {

        if (version.isEmpty()) {
          // upgrade from alpha/beta to "TV Show" 2.0 format
          // happens only once
          JOptionPane
              .showMessageDialog(null,
                  "And since you are upgrading to a complete new version, we need to cleanup/delete the complete database this time.\nWe're sorry for that.");
          FileUtils.deleteQuietly(new File("tmm.odb"));

          // upgrade from alpha - delete unneeded files
          FileUtils.deleteQuietly(new File("lib/jackson-core-lgpl.jar"));
          FileUtils.deleteQuietly(new File("lib/jackson-core-lgpl.jarv"));
          FileUtils.deleteQuietly(new File("lib/jackson-mapper-lgpl.jar"));
          FileUtils.deleteQuietly(new File("lib/jackson-mapper-lgpl.jarv"));

          // check really old alpha version
          FileUtils.deleteQuietly(new File("lib/beansbinding-1.2.1.jar"));
        }
        else if (version.equals("2.0")) {
          // do something to upgrade to 2.1/3.0
        }
      }

      /**
       * Does some tasks at startup
       */
      private void doStartupTasks() {
        // self updater
        File file = new File("getdown-new.jar");
        if (file.exists() && file.length() > 100000) {
          File cur = new File("getdown.jar");
          if (file.length() != cur.length() || !cur.exists()) {
            try {
              FileUtils.copyFile(file, cur);
            }
            catch (IOException e) {
              LOGGER.error("Could not update the updater!");
            }
          }
        }

        // check if a .desktop file exists
        if (Platform.isLinux()) {
          File desktop = new File("tinyMediaManager.desktop");
          if (!desktop.exists()) {
            // create .desktop
            String path = this.getClass().getClassLoader().getResource(".").getPath();
            StringBuilder sb = new StringBuilder("[Desktop Entry]\n");
            sb.append("Type=Application\n");
            sb.append("Name=tinyMediaManager\n");
            sb.append("Path=");
            sb.append(path);
            sb.append("\n");
            sb.append("Exec=/bin/sh \"");
            sb.append(path);
            sb.append("tinyMediaManager.sh\"\n");
            sb.append("Icon=");
            sb.append(path);
            sb.append("tmm.png\n");
            sb.append("Categories=Application;Multimedia;");
            FileWriter writer;
            try {
              writer = new FileWriter(desktop);
              writer.write(sb.toString());
              writer.close();
              desktop.setExecutable(true);
            }
            catch (IOException e) {
              LOGGER.warn(e.getMessage());
            }
          }
        }
      }
    });

    if (TmmUIHelper.swt != null) {
      NativeInterface.runEventPump();
    }
  }

  /**
   * debug various JVM character settings
   */
  private static void debugCharacterEncoding() {
    String defaultCharacterEncoding = System.getProperty("file.encoding");
    LOGGER.debug("defaultCharacterEncoding by property: " + defaultCharacterEncoding);
    byte[] bArray = { 'w' };
    InputStream is = new ByteArrayInputStream(bArray);
    InputStreamReader reader = new InputStreamReader(is);
    LOGGER.debug("defaultCharacterEncoding by code: " + reader.getEncoding());
    LOGGER.debug("defaultCharacterEncoding by charSet: " + Charset.defaultCharset());
  }
}
