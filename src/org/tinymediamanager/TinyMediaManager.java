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
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.ELProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.tasks.MovieRenameTask;
import org.tinymediamanager.core.movie.tasks.MovieScrapeTask;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmSwingWorker;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TmmUILogCollector;
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

    LOGGER.info("os.name          : " + System.getProperty("os.name"));
    LOGGER.info("os.version       : " + System.getProperty("os.version"));
    LOGGER.info("os.arch          : " + System.getProperty("os.arch"));
    LOGGER.info("java.version     : " + System.getProperty("java.version"));

    // initialize SWT if needed
    TmmUIHelper.init();
    if (TmmUIHelper.swt != null) {
      NativeInterface.open();
    }

    // START character encoding debug
    debugCharacterEncoding("default encoding : ");
    System.setProperty("file.encoding", "UTF-8");
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
    LOGGER.info("System language  : " + System.getProperty("user.language") + "_" + System.getProperty("user.language"));
    LOGGER.info("GUI language     : " + Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
    LOGGER.info("Scraper language : " + Globals.settings.getMovieSettings().getScraperLanguage());
    LOGGER.info("TV Scraper lang  : " + Globals.settings.getTvShowSettings().getScraperLanguage());

    // start EDT
    EventQueue.invokeLater(new Runnable() {
      public void run() {
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

          // after 5 secs of beeing idle, the threads are removed till 0; see Globals
          Globals.executor.allowCoreThreadTimeOut(true);

          // suppress logging messages from betterbeansbinding
          org.jdesktop.beansbinding.util.logging.Logger.getLogger(ELProperty.class.getName()).setLevel(Level.SEVERE);

          // init ui logger
          TmmUILogCollector.init();

          // upgrade check
          if (!Globals.settings.isCurrentVersion()) {
            if (!GraphicsEnvironment.isHeadless()) {
              JOptionPane.showMessageDialog(null, "The configuration format changed in this update.\nPlease check your settings!");
            }
            doUpgradeTasks(Globals.settings.getVersion()); // do the upgrade tasks for the old version
            Globals.settings.writeDefaultSettings(); // write current default
          }

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
            updateProgress(g2, "update check", 10);
            splash.update();
          }

          LOGGER.info("=====================================================");
          LOGGER.info("starting tinyMediaManager");

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

            TmmWindowSaver.loadSettings(window);
            window.setVisible(true);
          }
          else {
            startCommandLineTasks();
            // wait for other tmm threads (artwork download et all)
            while (Globals.poolRunning()) {
              Thread.sleep(2000);
            }

            LOGGER.info("bye bye");
            // MainWindows.shutdown()
            try {
              // send shutdown signal
              Globals.executor.shutdown();
              // save unsaved settings
              Globals.settings.saveSettings();
              // close database connection
              Globals.shutdownDatabase();
              // wait a bit for threads to finish (if any)
              Globals.executor.awaitTermination(2, TimeUnit.SECONDS);
              // hard kill
              Globals.executor.shutdownNow();
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
          FileUtils.deleteQuietly(new File(Constants.DB));

          // upgrade from alpha - delete unneeded files
          FileUtils.deleteQuietly(new File("lib/jackson-core-lgpl.jar"));
          FileUtils.deleteQuietly(new File("lib/jackson-core-lgpl.jarv"));
          FileUtils.deleteQuietly(new File("lib/jackson-mapper-lgpl.jar"));
          FileUtils.deleteQuietly(new File("lib/jackson-mapper-lgpl.jarv"));

          // check really old alpha version
          FileUtils.deleteQuietly(new File("lib/beansbinding-1.2.1.jar"));
          FileUtils.deleteQuietly(new File("lib/beansbinding.jar"));
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
            // String path = this.getClass().getClassLoader().getResource(".").getPath();

            // get the path in a safe way
            String path = new File(TinyMediaManager.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
            try {
              path = URLDecoder.decode(path, "UTF-8");
            }
            catch (UnsupportedEncodingException e1) {
            }
            StringBuilder sb = new StringBuilder("[Desktop Entry]\n");
            sb.append("Type=Application\n");
            sb.append("Name=tinyMediaManager\n");
            sb.append("Path=");
            sb.append(path);
            sb.append("\n");
            sb.append("Exec=/bin/sh \"");
            sb.append(path);
            sb.append("/tinyMediaManager.sh\"\n");
            sb.append("Icon=");
            sb.append(path);
            sb.append("/tmm.png\n");
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

        // do a DB backup, and keep last 15 copies
        File db = new File(Constants.DB);
        Utils.createBackupFile(db);
        Utils.deleteOldBackupFile(db, 15);
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
      TmmSwingWorker task = null;

      // update movies //////////////////////////////////////////////
      if (updateMovies) {
        LOGGER.info("Commandline - updating movies...");
        if (updateMovieDs.isEmpty()) {
          task = new MovieUpdateDatasourceTask();
          task.execute();
          task.get(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<String>(Globals.settings.getMovieSettings().getMovieDataSource());
          for (Integer i : updateMovieDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new MovieUpdateDatasourceTask(dataSources.get(i - 1));
              task.execute();
              task.get(); // blocking
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
            task.execute();
            task.get(); // blocking

            // wait for other tmm threads (artwork download et all)
            while (Globals.poolRunning()) {
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
            task.execute();
            task.get(); // blocking
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
          task.execute();
          task.get(); // blocking

          // wait for other tmm threads (artwork download et all)
          while (Globals.poolRunning()) {
            Thread.sleep(2000);
          }
        }
        if (renameNew) {
          LOGGER.info("Commandline - rename & cleanup new movies...");
          if (unscrapedMovies.size() > 0) {
            task = new MovieRenameTask(unscrapedMovies);
            task.execute();
            task.get(); // blocking
          }
        }
      }

      // update TvShows //////////////////////////////////////////////
      if (updateTv) {
        LOGGER.info("Commandline - updating TvShows and episodes...");
        if (updateTvDs.isEmpty()) {
          task = new TvShowUpdateDatasourceTask();
          task.execute();
          task.get(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<String>(Globals.settings.getTvShowSettings().getTvShowDataSource());
          for (Integer i : updateTvDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new TvShowUpdateDatasourceTask(dataSources.get(i - 1));
              task.execute();
              task.get(); // blocking
            }
          }
        }
        List<TvShow> newTv = TvShowList.getInstance().getNewTvShows();
        List<TvShowEpisode> newEp = TvShowList.getInstance().getNewEpisodes();

        if (scrapeNew) {
          LOGGER.info("Commandline - scraping new TvShows...");
          // TODO: scrape only if unscraped?!
          if (newTv.size() > 0) {
            TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
            options.loadDefaults();
            task = new TvShowScrapeTask(newTv, true, options);
            task.execute();
            task.get(); // blocking
          }
          else {
            LOGGER.info("No new TvShows/episodes found to scrape - skipping");
          }
        }

        if (renameNew) {
          LOGGER.info("Commandline - rename & cleanup new episodes...");
          if (newTv.size() > 0 && newEp.size() > 0) {
            task = new TvShowRenameTask(null, newEp, true); // just rename new EPs AND root folder
            task.execute();
            task.get(); // blocking
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
