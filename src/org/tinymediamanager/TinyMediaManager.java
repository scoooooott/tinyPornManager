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

package org.tinymediamanager;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.ELProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.License;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TmmUILogCollector;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.dialogs.MessageDialog;
import org.tinymediamanager.ui.dialogs.WhatsNewDialog;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

import com.sun.jna.Platform;

/**
 * The Class TinyMediaManager.
 * 
 * @author Manuel Laggner
 */
public class TinyMediaManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(TinyMediaManager.class);

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    // simple parse command line
    if (args != null && args.length > 0) {
      TinyMediaManagerCMD.parseParams(args);

      System.setProperty("java.awt.headless", "true");
    }
    else {
      // no cmd params found, but if we are headless - display syntax
      String head = System.getProperty("java.awt.headless");
      if (head != null && head.equals("true")) {
        TinyMediaManagerCMD.printSyntax();
        System.exit(0);
      }
    }

    // check if we have write permissions to this folder
    try {
      RandomAccessFile f = new RandomAccessFile("access.test", "rw");
      f.close();
      FileUtils.deleteQuietly(new File("access.test"));
    }
    catch (Exception e2) {
      String msg = "Cannot write to TMM directory, have no rights - exiting.";
      if (!GraphicsEnvironment.isHeadless()) {
        JOptionPane.showMessageDialog(null, msg);
      }
      else {
        System.out.println(msg);
      }
      System.exit(1);
    }

    LOGGER.info("=====================================================");
    LOGGER.info("=== tinyMediaManager (c) 2012-2014 Manuel Laggner ===");
    LOGGER.info("=====================================================");
    LOGGER.info("tmm.version      : " + ReleaseInfo.getRealVersion());

    if (Globals.isDonator()) {
      LOGGER.info("tmm.supporter    : THANKS FOR DONATING - ALL FEATURES UNLOCKED :)");
    }

    LOGGER.info("os.name          : " + System.getProperty("os.name"));
    LOGGER.info("os.version       : " + System.getProperty("os.version"));
    LOGGER.info("os.arch          : " + System.getProperty("os.arch"));
    LOGGER.trace("network.id       : " + License.getMac());
    LOGGER.info("java.version     : " + System.getProperty("java.version"));
    if (Globals.isRunningJavaWebStart()) {
      LOGGER.info("java.webstart    : true");
    }

    // initialize SWT if found
    try {
      TmmUIHelper.init();
      LOGGER.info("java.swt         : true");
      if (TmmUIHelper.swt != null) {
        NativeInterface.open();
      }
    }
    catch (ClassNotFoundException e2) {
      LOGGER.info("java.swt         : false");
    }

    // START character encoding debug
    debugCharacterEncoding("default encoding : ");
    System.setProperty("file.encoding", "UTF-8");
    System.setProperty("sun.jnu.encoding", "UTF-8");
    Field charset;
    try {
      // we cannot (re)set the properties while running inside JVM
      // so we trick it to reread it by setting them to null ;)
      charset = Charset.class.getDeclaredField("defaultCharset");
      charset.setAccessible(true);
      charset.set(null, null);
    }
    catch (Exception e) {
      LOGGER.warn("Error resetting to UTF-8", e);
    }
    debugCharacterEncoding("set encoding to  : ");
    // END character encoding debug

    // set GUI default language
    Locale.setDefault(Utils.getLocaleFromLanguage(Globals.settings.getLanguage()));
    LOGGER.info("System language  : " + System.getProperty("user.language") + "_" + System.getProperty("user.country"));
    LOGGER.info("GUI language     : " + Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
    LOGGER.info("Scraper language : " + MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
    LOGGER.info("TV Scraper lang  : " + Globals.settings.getTvShowSettings().getScraperLanguage());

    // start EDT
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        boolean newVersion = !Globals.settings.isCurrentVersion();
        try {
          Thread.setDefaultUncaughtExceptionHandler(new Log4jBackstop());
          if (!GraphicsEnvironment.isHeadless()) {
            Thread.currentThread().setName("main");
          }
          else {
            Thread.currentThread().setName("headless");
            LOGGER.debug("starting without GUI...");
          }
          Toolkit tk = Toolkit.getDefaultToolkit();
          tk.addAWTEventListener(TmmWindowSaver.getInstance(), AWTEvent.WINDOW_EVENT_MASK);
          if (!GraphicsEnvironment.isHeadless()) {
            setLookAndFeel();
          }
          doStartupTasks();

          // suppress logging messages from betterbeansbinding
          org.jdesktop.beansbinding.util.logging.Logger.getLogger(ELProperty.class.getName()).setLevel(Level.SEVERE);

          // init ui logger
          TmmUILogCollector.init();

          // init splash
          SplashScreen splash = null;
          if (!GraphicsEnvironment.isHeadless()) {
            splash = SplashScreen.getSplashScreen();
          }
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

          LOGGER.info("=====================================================");
          if (g2 != null) {
            updateProgress(g2, "starting tinyMediaManager", 0);
            splash.update();
          }
          LOGGER.info("starting tinyMediaManager");

          // convert old database
          // UpgradeTasks.convertDatabase(); // we need to check the exceptions before we can activate this

          // upgrade check
          String oldVersion = Globals.settings.getVersion();
          if (newVersion) {
            UpgradeTasks.performUpgradeTasksBeforeDatabaseLoading(oldVersion); // do the upgrade tasks for the old version
            Globals.settings.setCurrentVersion();
            Globals.settings.saveSettings();
          }

          // proxy settings
          if (Globals.settings.useProxy()) {
            LOGGER.info("setting proxy");
            Globals.settings.setProxy();
          }

          // MediaInfo /////////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading MediaInfo libs", 20);
            splash.update();
          }

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

          String miv = "";
          // need that, since we cannot try and reload/unload a Class
          // MI does not load over UNC, so copy to temp
          if (System.getProperty("user.dir", "").startsWith("\\\\")) {
            LOGGER.debug("We're on a network UNC path!");
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmm");
            File nativeDir = new File(tmpDir, nativepath);
            FileUtils.copyDirectory(new File(nativepath), nativeDir); // same structure

            System.setProperty("jna.library.path", nativeDir.getAbsolutePath());
            LOGGER.debug("Loading native mediainfo lib from: {}", nativeDir.getAbsolutePath());
            miv = MediaInfo.version(); // load class
          }
          else {
            System.setProperty("jna.library.path", nativepath);
            LOGGER.debug("Loading native mediainfo lib from: {}", nativepath);
            miv = MediaInfo.version(); // load class
          }

          if (!StringUtils.isEmpty(miv)) {
            LOGGER.info("Using " + miv);
          }
          else {
            LOGGER.error("could not load MediaInfo!");
          }

          // load modules //////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading movie module", 30);
            splash.update();
          }
          TmmModuleManager.getInstance().startUp();
          TmmModuleManager.getInstance().registerModule(MovieModuleManager.getInstance());
          TmmModuleManager.getInstance().enableModule(MovieModuleManager.getInstance());

          if (g2 != null) {
            updateProgress(g2, "loading TV show module", 40);
            splash.update();
          }

          TmmModuleManager.getInstance().registerModule(TvShowModuleManager.getInstance());
          TmmModuleManager.getInstance().enableModule(TvShowModuleManager.getInstance());

          // if (g2 != null) {
          // updateProgress(g2, "loading plugins", 50);
          // splash.update();
          // }
          // PluginManager.getInstance(); // just instantiate static

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

          // do upgrade tasks after database loading
          if (newVersion) {
            if (g2 != null) {
              updateProgress(g2, "upgrading database to new version", 60);
              splash.update();
            }
            UpgradeTasks.performUpgradeTasksAfterDatabaseLoading(oldVersion);
          }

          // clean cache ////////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "cleaning cache", 70);
            splash.update();
          }
          CachedUrl.cleanupCache();

          // launch application ////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading ui", 80);
            splash.update();
          }
          if (!GraphicsEnvironment.isHeadless()) {
            MainWindow window = new MainWindow("tinyMediaManager / " + ReleaseInfo.getRealVersion());

            // finished ////////////////////////////////////////////////////
            if (g2 != null) {
              updateProgress(g2, "finished starting :)", 100);
              splash.update();
            }

            // write a random number to file, to identify this instance (for
            // updater, tracking, whatsoever)
            Utils.trackEvent("startup");

            TmmWindowSaver.getInstance().loadSettings(window);
            window.setVisible(true);

            // show changelog
            if (newVersion) {
              showChangelog();
            }
          }
          else {
            TinyMediaManagerCMD.startCommandLineTasks();
            // wait for other tmm threads (artwork download et all)
            while (TmmTaskManager.getInstance().poolRunning()) {
              Thread.sleep(2000);
            }

            LOGGER.info("bye bye");
            // MainWindows.shutdown()
            try {
              // send shutdown signal
              TmmTaskManager.getInstance().shutdown();
              // save unsaved settings
              Globals.settings.saveSettings();
              // hard kill
              TmmTaskManager.getInstance().shutdownNow();
              // close database connection
              TmmModuleManager.getInstance().shutDown();
            }
            catch (Exception ex) {
              LOGGER.warn(ex.getMessage());
            }
            System.exit(0);
          }
        }
        catch (javax.persistence.PersistenceException e) {
          LOGGER.error("PersistenceException", e);
          if (!GraphicsEnvironment.isHeadless()) {
            MessageDialog.showExceptionWindow(e);
            System.exit(1);
          }
        }
        catch (Exception e) {
          LOGGER.error("Exception while start of tmm", e);
          if (!GraphicsEnvironment.isHeadless()) {
            MessageDialog.showExceptionWindow(e);
            System.exit(1);
          }
        }
      }

      /**
       * Update progress on splash screen.
       * 
       * @param text
       *          the text
       */
      private void updateProgress(Graphics2D g2, String text, int progress) {
        Object oldAAValue = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(20, 200, 480, 305);
        g2.setPaintMode();

        g2.setColor(new Color(51, 153, 255));
        g2.fillRect(22, 272, 452 * progress / 100, 21);

        g2.setColor(Color.black);
        g2.drawString(text + "...", 23, 310);
        int l = g2.getFontMetrics().stringWidth(ReleaseInfo.getRealVersion()); // bound right
        g2.drawString(ReleaseInfo.getRealVersion(), 480 - l, 325);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAAValue);
        LOGGER.debug("Startup (" + progress + "%) " + text);
      }

      /**
       * Sets the look and feel.
       * 
       * @throws Exception
       *           the exception
       */
      private void setLookAndFeel() throws Exception {
        // get font settings
        String fontFamily = Globals.settings.getFontFamily();
        try {
          // sanity check
          fontFamily = Font.decode(fontFamily).getFamily();
        }
        catch (Exception e) {
          fontFamily = "Dialog";
        }

        int fontSize = Globals.settings.getFontSize();
        if (fontSize < 12) {
          fontSize = 12;
        }

        String fontString = fontFamily + " " + fontSize;

        // Get the native look and feel class name
        // String laf = UIManager.getSystemLookAndFeelClassName();
        Properties props = new Properties();
        props.setProperty("controlTextFont", fontString);
        props.setProperty("systemTextFont", fontString);
        props.setProperty("userTextFont", fontString);
        props.setProperty("menuTextFont", fontString);
        // props.setProperty("windowTitleFont", "Dialog bold 20");

        fontSize = Math.round((float) (fontSize * 0.833));
        fontString = fontFamily + " " + fontSize;

        props.setProperty("subTextFont", fontString);
        props.setProperty("backgroundColor", "237 237 237");
        props.setProperty("menuBackgroundColor", "237 237 237");
        props.setProperty("controlBackgroundColor", "237 237 237");
        props.setProperty("menuColorLight", "237 237 237");
        props.setProperty("menuColorDark", "237 237 237");
        props.setProperty("toolbarColorLight", "237 237 237");
        props.setProperty("toolbarColorDark", "237 237 237");
        props.setProperty("tooltipBackgroundColor", "255 255 255");
        props.put("windowDecoration", "system");
        props.put("logoString", "");

        // Get the look and feel class name
        com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme(props);
        String laf = "com.jtattoo.plaf.luna.LunaLookAndFeel";

        // Install the look and feel
        UIManager.setLookAndFeel(laf);
      }

      /**
       * Does some tasks at startup
       */
      private void doStartupTasks() {
        // rename downloaded files
        UpgradeTasks.renameDownloadedFiles();

        // check if a .desktop file exists
        if (Platform.isLinux()) {
          File desktop = new File(TmmOsUtils.DESKTOP_FILE);
          if (!desktop.exists()) {
            TmmOsUtils.createDesktopFileForLinux(desktop);
          }
        }

        // do a DB backup, and keep last 15 copies
        File db = new File(Constants.DB);
        Utils.createBackupFile(db);
        Utils.deleteOldBackupFile(db, 15);
      }

      private void showChangelog() {
        // read the changelog
        try {
          final String changelog = FileUtils.readFileToString(new File("changelog.txt"));
          if (StringUtils.isNotBlank(changelog)) {
            EventQueue.invokeLater(new Runnable() {
              @Override
              public void run() {
                WhatsNewDialog dialog = new WhatsNewDialog(changelog);
                dialog.pack();
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setModalityType(ModalityType.APPLICATION_MODAL);
                dialog.setVisible(true);
              }
            });
          }
        }
        catch (IOException e) {
          // no file found
          LOGGER.warn(e.getMessage());
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
  private static void debugCharacterEncoding(String text) {
    String defaultCharacterEncoding = System.getProperty("file.encoding");
    byte[] bArray = { 'w' };
    InputStream is = new ByteArrayInputStream(bArray);
    InputStreamReader reader = new InputStreamReader(is);
    LOGGER.info(text + defaultCharacterEncoding + " | " + reader.getEncoding() + " | " + Charset.defaultCharset());
  }
}
