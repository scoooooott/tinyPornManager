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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.ELProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieRenameTask;
import org.tinymediamanager.core.movie.tasks.MovieScrapeTask;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TmmUILogCollector;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.dialogs.WhatsNewDialog;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

import com.sun.jna.Platform;

/**
 * The Class TinyMediaManager.
 * 
 * @author Manuel Laggner
 */
public class TinyMediaManager {

  /** The Constant LOGGER. */
  private static final Logger     LOGGER          = LoggerFactory.getLogger(TinyMediaManager.class);

  private static boolean          updateMovies    = false;
  private static boolean          updateTv        = false;
  private static boolean          scrapeNew       = false;
  private static boolean          scrapeUnscraped = false;
  private static boolean          renameNew       = false;
  private static boolean          checkFiles      = false;

  // datasource IDs
  private static HashSet<Integer> updateMovieDs   = new HashSet<Integer>();
  private static HashSet<Integer> updateTvDs      = new HashSet<Integer>();

  private static void syntax() {
    // @formatter:off
    System.out.println("\n" +
        "=====================================================\n" +
        "=== tinyMediaManager (c) 2012-2013 Manuel Laggner ===\n" +
        "=====================================================\n" +
        "\n" +
        "    SYNTAX: java -jar tmm.jar <parameters>\n" +
        "\n" +
        "PARAMETERS:\n" +
        "\n" +
        "    -updateMovies        update all movie datasources and add new movies/files to DB\n" +
        "    -updateMoviesX       replace X with 1-9 - just updates a single movie datasource; ordering like GUI\n" +
        "    -updateTv            update all TvShow datasources and add new TvShows/episodes to DB\n" +
        "    -updateTvX           replace X with 1-9 - just updates a single TvShow datasource; ordering like GUI\n" +
        "    -update              update all (short for '-updateMovies -updateTv')\n" +
        "\n" +
        "    -scrapeNew           auto-scrape (force best match) new found movies/TvShows/episodes from former update(s)\n" +
        "    -scrapeUnscraped     auto-scrape (force best match) all movies, which have not yet been scraped (not for TV/episodes!)\n" +
        "    -renameNew           rename & cleanup of the new found movies/TvShows/episodes\n" +
        "\n" +
        "    -checkFiles          does a physical check, if all files in DB are existent on filesystem (might take long!)\n" +
        "\n");
    // @formatter:on
  }

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    // simple parse command line
    if (args != null && args.length > 0) {
      for (String cmd : args) {
        if (cmd.equalsIgnoreCase("-updateMovies")) {
          updateMovies = true;
        }
        else if (cmd.matches("(?)-updateMovies[1-9]")) {
          updateMovies = true;
          updateMovieDs.add(Integer.parseInt(StrgUtils.substr(cmd, "(?)-updateMovies(\\d)")));
        }
        else if (cmd.equalsIgnoreCase("-updateTv")) {
          updateTv = true;
        }
        else if (cmd.matches("(?)-updateTv[1-9]")) {
          updateTv = true;
          updateTvDs.add(Integer.parseInt(StrgUtils.substr(cmd, "(?)-updateTv(\\d)")));
        }
        else if (cmd.equalsIgnoreCase("-update")) {
          updateMovies = true;
          updateTv = true;
        }
        else if (cmd.equalsIgnoreCase("-scrapeNew")) {
          scrapeNew = true;
        }
        else if (cmd.equalsIgnoreCase("-scrapeUnscraped")) {
          scrapeUnscraped = true;
        }
        else if (cmd.equalsIgnoreCase("-checkFiles")) {
          checkFiles = true;
        }
        else if (cmd.equalsIgnoreCase("-renameNew")) {
          renameNew = true;
        }
        else if (cmd.toLowerCase().contains("help")) { // -help, --help, help ...
          syntax();
          System.exit(0);
        }
        else {
          System.out.println("ERROR: unrecognized command '" + cmd);
          syntax();
          System.exit(0);
        }
      }
      System.setProperty("java.awt.headless", "true");
    }
    else {
      // no cmd params found, but if we are headless - display syntax
      String head = System.getProperty("java.awt.headless");
      if (head != null && head.equals("true")) {
        syntax();
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
    LOGGER.info("=== tinyMediaManager (c) 2012-2013 Manuel Laggner ===");
    LOGGER.info("=====================================================");
    LOGGER.info("tmm.version      : " + ReleaseInfo.getRealVersion());

    if (Globals.isDonator()) {
      LOGGER.info("tmm.supporter    : THANKS FOR DONATING - ALL FEATURES UNLOCKED :)");
    }

    LOGGER.info("os.name          : " + System.getProperty("os.name"));
    LOGGER.info("os.version       : " + System.getProperty("os.version"));
    LOGGER.info("os.arch          : " + System.getProperty("os.arch"));
    LOGGER.info("java.version     : " + System.getProperty("java.version"));
    if (Globals.isRunningJavaWebStart()) {
      LOGGER.info("java.webstart    : true");
    }

    // initialize SWT if needed
    TmmUIHelper.init();
    if (TmmUIHelper.swt != null) {
      NativeInterface.open();
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

          // update check //////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "starting tinyMediaManager", 0);
            splash.update();
          }

          LOGGER.info("=====================================================");
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

          // set native dir (needs to be absolute)
          // String nativepath = TinyMediaManager.class.getClassLoader().getResource(".").getPath() + "native/";
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
            updateProgress(g2, "loading MediaInfo libs", 20);
            splash.update();
          }
          LOGGER.debug("Loading native mediainfo lib from: {}", nativepath);
          // load libMediainfo
          String miv = MediaInfo.version();
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
            UpgradeTasks.performUpgradeTasksAfterDatabaseLoading(oldVersion);
          }

          // clean cache ////////////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "cleaning cache", 80);
            splash.update();
          }
          CachedUrl.cleanupCache();

          // launch application ////////////////////////////////////////////
          if (g2 != null) {
            updateProgress(g2, "loading ui", 90);
            splash.update();
          }
          if (!GraphicsEnvironment.isHeadless()) {
            MainWindow window = new MainWindow("tinyMediaManager / " + ReleaseInfo.getRealVersion());

            // finished ////////////////////////////////////////////////////
            if (g2 != null) {
              updateProgress(g2, "finished starting", 100);
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
            startCommandLineTasks();
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
          }
        }
        catch (javax.persistence.PersistenceException e) {
          if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, e.getMessage());
          }
          LOGGER.error("PersistenceException", e);
        }
        catch (Exception e) {
          if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, e.getMessage());
          }
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
        if (Platform.isWindows()) {
          file = new File("tinyMediaManager.new");
          if (file.exists() && file.length() > 10000 && file.length() < 50000) {
            File cur = new File("tinyMediaManager.exe");
            if (file.length() != cur.length() || !cur.exists()) {
              try {
                FileUtils.copyFile(file, cur);
              }
              catch (IOException e) {
                LOGGER.error("Could not update the updater!");
              }
            }
          }
        }
        if (Platform.isWindows()) {
          file = new File("tinyMediaManagerCMD.new");
          if (file.exists() && file.length() > 10000 && file.length() < 50000) {
            File cur = new File("tinyMediaManagerCMD.exe");
            if (file.length() != cur.length() || !cur.exists()) {
              try {
                FileUtils.copyFile(file, cur);
              }
              catch (IOException e) {
                LOGGER.error("Could not update the updater!");
              }
            }
          }
        }

        // check if a .desktop file exists
        if (Platform.isLinux()) {
          File desktop = new File("tinyMediaManager.desktop");
          if (!desktop.exists()) {
            // create .desktop
            // String path = this.getClass().getClassLoader().getResource(".").getPath();

            // get the path in a safe way
            String path = new File(TinyMediaManager.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
            try {
              path = URLDecoder.decode(path, "UTF-8");
            }
            catch (UnsupportedEncodingException e1) {
              path = URLDecoder.decode(path);
            }
            StringBuilder sb = new StringBuilder(60);
            sb.append("[Desktop Entry]\n");
            sb.append("Type=Application\n");
            sb.append("Name=tinyMediaManager\n");
            sb.append("Path=");
            sb.append(path);
            sb.append('\n');
            sb.append("Exec=/bin/sh \"");
            sb.append(path);
            sb.append("/tinyMediaManager.sh\"\n");
            sb.append("Icon=");
            sb.append(path);
            sb.append("/tmm.png\n");
            sb.append("Categories=Application;Multimedia;");
            FileWriterWithEncoding writer;
            try {
              writer = new FileWriterWithEncoding(desktop, "UTF-8");
              writer.write(sb.toString());
              writer.close();
              desktop.setExecutable(true);
            }
            catch (IOException e) {
              LOGGER.warn(e.getMessage());
            }
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

  /**
   * executes all the command line tasks, one after another
   */
  private static void startCommandLineTasks() {
    try {
      TmmTask task = null;

      // update movies //////////////////////////////////////////////
      if (updateMovies) {
        LOGGER.info("Commandline - updating movies...");
        if (updateMovieDs.isEmpty()) {
          task = new MovieUpdateDatasourceTask();
          task.run(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<String>(MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource());
          for (Integer i : updateMovieDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new MovieUpdateDatasourceTask(dataSources.get(i - 1));
              task.run(); // blocking
            }
          }
        }
        List<Movie> newMovies = MovieList.getInstance().getNewMovies();

        if (scrapeNew) {
          LOGGER.info("Commandline - scraping new movies...");
          if (newMovies.size() > 0) {
            MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
            options.loadDefaults();
            task = new MovieScrapeTask(newMovies, true, options);
            task.run(); // blocking

            // wait for other tmm threads (artwork download et all)
            while (TmmTaskManager.getInstance().poolRunning()) {
              Thread.sleep(2000);
            }
          }
          else {
            LOGGER.info("No new movies found to scrape - skipping");
          }
        }

        if (renameNew) {
          LOGGER.info("Commandline - rename & cleanup new movies...");
          if (newMovies.size() > 0) {
            task = new MovieRenameTask(newMovies);
            task.run(); // blocking
          }
        }
      }
      if (scrapeUnscraped) {
        LOGGER.info("Commandline - scraping all unscraped movies...");
        List<Movie> unscrapedMovies = MovieList.getInstance().getUnscrapedMovies();
        if (unscrapedMovies.size() > 0) {
          MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
          options.loadDefaults();
          task = new MovieScrapeTask(unscrapedMovies, true, options);
          task.run(); // blocking

          // wait for other tmm threads (artwork download et all)
          while (TmmTaskManager.getInstance().poolRunning()) {
            Thread.sleep(2000);
          }
        }
        if (renameNew) {
          LOGGER.info("Commandline - rename & cleanup new movies...");
          if (unscrapedMovies.size() > 0) {
            task = new MovieRenameTask(unscrapedMovies);
            task.run(); // blocking
          }
        }
      }

      // update TvShows //////////////////////////////////////////////
      if (updateTv) {
        LOGGER.info("Commandline - updating TvShows and episodes...");
        if (updateTvDs.isEmpty()) {
          task = new TvShowUpdateDatasourceTask();
          task.run(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<String>(Globals.settings.getTvShowSettings().getTvShowDataSource());
          for (Integer i : updateTvDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new TvShowUpdateDatasourceTask(dataSources.get(i - 1));
              task.run(); // blocking
            }
          }
        }
        List<TvShow> newTv = TvShowList.getInstance().getNewTvShows();
        List<TvShowEpisode> newEp = TvShowList.getInstance().getNewEpisodes();
        LOGGER.info("Commandline - found " + newTv.size() + " TvShow(s) containing " + newEp.size() + " new episode(s)");

        if (scrapeNew) {
          LOGGER.info("Commandline - scraping new TvShows...");
          // TODO: scrape only if unscraped?!
          if (newTv.size() > 0) {
            TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
            options.loadDefaults();
            task = new TvShowScrapeTask(newTv, true, options);
            task.run(); // blocking
          }
          else {
            LOGGER.info("No new TvShows/episodes found to scrape - skipping");
          }
        }

        if (renameNew) {
          LOGGER.info("Commandline - rename & cleanup new episodes...");
          if (newEp.size() > 0) {
            task = new TvShowRenameTask(null, newEp, true); // just rename new EPs AND root folder
            task.run(); // blocking
          }
        }
      }

      if (checkFiles) {
        boolean allOk = true;
        // check db
        LOGGER.info("Check all files if existing");
        for (Movie m : MovieList.getInstance().getMovies()) {
          System.out.print(".");
          for (MediaFile mf : m.getMediaFiles()) {
            if (!mf.exists()) {
              System.out.println();
              LOGGER.warn("MediaFile not found! " + mf.getFile().getAbsolutePath());
              allOk = false;
            }
          }
        }
        for (TvShow s : TvShowList.getInstance().getTvShows()) {
          System.out.print(".");
          for (MediaFile mf : s.getMediaFiles()) { // show MFs
            if (!mf.exists()) {
              System.out.println();
              LOGGER.warn("MediaFile not found! " + mf.getFile().getAbsolutePath());
              allOk = false;
            }
          }
          for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(s.getEpisodes())) {
            for (MediaFile mf : episode.getMediaFiles()) { // episode MFs
              if (!mf.exists()) {
                System.out.println();
                LOGGER.warn("MediaFile not found! " + mf.getFile().getAbsolutePath());
                allOk = false;
              }
            }
          }
        }
        System.out.println();
        if (allOk) {
          LOGGER.info("no problems found - everything ok :)");
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Error executing command line task!", e);
    }
  }
}
