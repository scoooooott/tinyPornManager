/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UpdaterTask;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieRenameTask;
import org.tinymediamanager.core.movie.tasks.MovieScrapeTask;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask2;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowEpisodeScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask2;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The class TinyMediaManagerCMD - used for all logic for the command line tool
 * 
 * @author Manuel Laggner
 */
public class TinyMediaManagerCMD {
  private static final Logger     LOGGER          = LoggerFactory.getLogger(TinyMediaManagerCMD.class);
  private static boolean          updateMovies    = false;
  private static boolean          updateTv        = false;
  private static boolean          scrapeAll       = false;
  private static boolean          scrapeNew       = false;
  private static boolean          scrapeUnscraped = false;
  private static boolean          rename          = false;
  private static boolean          dryRun          = false;
  private static boolean          checkFiles      = false;

  // datasource IDs
  private static HashSet<Integer> updateMovieDs   = new HashSet<>();
  private static HashSet<Integer> updateTvDs      = new HashSet<>();

  /**
   * parse command line params
   * 
   * @param args
   *          an array of params to parse
   */
  static void parseParams(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String cmd = args[i];

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
      else if (cmd.equalsIgnoreCase("-scrapeAll")) {
        scrapeAll = true;
      }
      else if (cmd.equalsIgnoreCase("-scrapeUnscraped")) {
        scrapeUnscraped = true;
      }
      else if (cmd.equalsIgnoreCase("-dryRun")) {
        dryRun = true;
        if (args.length == 1) {
          // haahaa - we specified dryRun as only argument
          printSyntax();
          System.exit(0);
        }
      }
      else if (cmd.equalsIgnoreCase("-checkFiles")) {
        checkFiles = true;
      }
      else if (cmd.equalsIgnoreCase("-rename") || cmd.equalsIgnoreCase("-renameNew")) { // "new" deprecated
        rename = true;
      }
      else if (cmd.equalsIgnoreCase("-config")) {
        i++;
        if (i == args.length) { // config is last parameter
          System.out.println("ERROR: config not specified!");
          printSyntax();
          System.exit(0);
        }
        String file = args[i];
        if (Files.exists(Paths.get("data", file))) { // only check in default data path?
          // load custom settings
          Settings.getInstance("data", file);
        }
        else {
          System.out.println("ERROR: config file not found! " + file);
          printSyntax();
          System.exit(0);
        }
      }
      else if (cmd.toLowerCase(Locale.ROOT).contains("help")) { // -help, --help, help ...
        printSyntax();
        System.exit(0);
      }
      else {
        System.out.println("ERROR: unrecognized command " + cmd);
        printSyntax();
        System.exit(0);
      }
    }
  }

  /**
   * print the syntax to command line
   */
  static void printSyntax() {
    // @formatter:off
    System.out.println("\n" +
        "=====================================================\n" +
        "=== tinyMediaManager (c) 2012-2016 Manuel Laggner ===\n" +
        "=====================================================\n" +
        "\n" +
        "SYNTAX:    Windows:   tinyMediaManagerCMD.exe <parameters>\n" +
        "           Linux:   ./tinyMediaManagerCMD.sh  <parameters>\n" +
        "\n" +
        "\n" +
        "PARAMETERS:\n" +
        "\n" +
        "    UPDATE: Will scan your folders, and adds all found items to database\n" +
        "            Keeps an internal list of 'new' items (for this run only!)\n" +
        "\n" +
        "    -updateMovies        update all movie datasources\n" +
        "    -updateMoviesX       replace X with 1-9 - just updates a single movie datasource; ordering like GUI\n" +
        "    -updateTv            update all TvShow\n" +
        "    -updateTvX           replace X with 1-9 - just updates a single TvShow datasource; ordering like GUI\n" +
        "    -update              update all (short for '-updateMovies -updateTv')\n" +
        "\n" +
        "    SCRAPE: auto-scrapes (force best match) your specified items:\n" +
        "    -scrapeNew           only NEW FOUND movies/TvShows/episodes from former update\n" +
        "    -scrapeUnscraped     all movies/TvShows/episodes, which have not yet been scraped\n" +
        "    -scrapeAll           ALL movies/TvShows/episodes, whether they have already been scraped or not\n" +
        "\n" +
        "    -rename              rename & cleanup all the movies/TvShows/episodes from former scrape command\n" +
        "    -config file.xml     specify an alternative configuration xml file\n" +
        "    -checkFiles          does a physical check, if all files in DB are existent on filesystem (might take long!)\n" +
        "\n" +
        "\n" +
        "EXAMPLES:\n" +
        "\n" +
        "    tinyMediaManagerCMD.exe -updateMovies -updateTv3 -scrapeNew -rename\n" +
        "    tinyMediaManagerCMD.exe -scrapeUnscraped -rename\n" +
        "\n");
    // @formatter:on
  }

  /**
   * executes all the command line tasks, one after another
   */
  static void startCommandLineTasks() {
    try {
      TmmTask task = null;
      boolean updateAvailable = false;

      if (scrapeNew || scrapeUnscraped || scrapeAll) {
        // only do an update check when we are scraping online
        // no need for a "forced" check for just updating the datasource
        Utils.trackEvent("cmd");

        final SwingWorker<Boolean, Void> updateWorker = new UpdaterTask();
        updateWorker.run();
        updateAvailable = updateWorker.get(); // blocking
        if (updateAvailable) {
          LOGGER.warn("There's a new TMM update available!");
          LOGGER.warn("Please update to remove waiting time ;)");
          for (int i = 20; i > 0; i--) {
            System.out.print(i + "..");
            Thread.sleep(1000);
          }
          System.out.println("0");
        }
      }

      // @formatter:off
      // ███╗   ███╗ ██████╗ ██╗   ██╗██╗███████╗███████╗
      // ████╗ ████║██╔═══██╗██║   ██║██║██╔════╝██╔════╝
      // ██╔████╔██║██║   ██║██║   ██║██║█████╗  ███████╗
      // ██║╚██╔╝██║██║   ██║╚██╗ ██╔╝██║██╔══╝  ╚════██║
      // ██║ ╚═╝ ██║╚██████╔╝ ╚████╔╝ ██║███████╗███████║
      // ╚═╝     ╚═╝ ╚═════╝   ╚═══╝  ╚═╝╚══════╝╚══════╝
      // @formatter:on

      // *****************
      // UPDATE
      // *****************
      if (updateMovies) {
        LOGGER.info("Commandline - updating movies...");
        if (updateMovieDs.isEmpty()) {
          task = new MovieUpdateDatasourceTask2();
          task.run(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<>(MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource());
          for (Integer i : updateMovieDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new MovieUpdateDatasourceTask2(dataSources.get(i - 1));
              task.run(); // blocking
            }
          }
        }
        LOGGER.info("Found " + MovieList.getInstance().getNewMovies().size() + " new movies");
      }

      // *****************
      // SCRAPE
      // *****************
      List<Movie> moviesToScrape = new ArrayList<>();
      if (scrapeAll) {
        LOGGER.info("Commandline - scraping ALL movies...");
        if (MovieList.getInstance().getMovieCount() > 0) {
          moviesToScrape = MovieList.getInstance().getMovies();
        }
      }
      else {
        HashSet<Movie> scrape = new HashSet<Movie>(); // no dupes
        if (scrapeNew) {
          LOGGER.info("Commandline - scraping new movies...");
          List<Movie> newMovies = MovieList.getInstance().getNewMovies();
          if (newMovies.size() > 0) {
            scrape.addAll(newMovies);
          }
        }
        if (scrapeUnscraped) {
          LOGGER.info("Commandline - scraping all unscraped movies...");
          List<Movie> unscrapedMovies = MovieList.getInstance().getUnscrapedMovies();
          if (unscrapedMovies.size() > 0) {
            scrape.addAll(unscrapedMovies);
          }
        }
        moviesToScrape.addAll(new ArrayList<Movie>(scrape));
      }

      if (moviesToScrape.size() > 0) {
        MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
        options.loadDefaults();
        if (dryRun) {
          for (Movie movie : moviesToScrape) {
            LOGGER.info("DRYRUN: would have scraped " + movie.getTitle());
          }
        }
        else {
          task = new MovieScrapeTask(moviesToScrape, true, options);
          task.run(); // blocking
          // wait for other tmm threads (artwork download et all)
          while (TmmTaskManager.getInstance().poolRunning()) {
            Thread.sleep(2000);
          }
        }
      }

      // *****************
      // RENAME
      // *****************
      if (rename) {
        LOGGER.info("Commandline - rename & cleanup movies...");
        if (moviesToScrape.size() > 0) {
          if (dryRun) {
            for (Movie movie : moviesToScrape) {
              LOGGER.info("DRYRUN: would have renamed " + movie.getTitle());
            }
          }
          else {
            task = new MovieRenameTask(moviesToScrape);
            task.run(); // blocking}
          }
        }
      }

      // @formatter:off
      //  ████████╗██╗   ██╗███████╗██╗  ██╗ ██████╗ ██╗    ██╗███████╗
      //  ╚══██╔══╝██║   ██║██╔════╝██║  ██║██╔═══██╗██║    ██║██╔════╝
      //     ██║   ██║   ██║███████╗███████║██║   ██║██║ █╗ ██║███████╗
      //     ██║   ╚██╗ ██╔╝╚════██║██╔══██║██║   ██║██║███╗██║╚════██║
      //     ██║    ╚████╔╝ ███████║██║  ██║╚██████╔╝╚███╔███╔╝███████║
      //     ╚═╝     ╚═══╝  ╚══════╝╚═╝  ╚═╝ ╚═════╝  ╚══╝╚══╝ ╚══════╝
      // @formatter:on

      // *****************
      // UPDATE
      // *****************
      if (updateTv) {
        LOGGER.info("Commandline - updating TvShows and episodes...");
        if (updateTvDs.isEmpty()) {
          task = new TvShowUpdateDatasourceTask2();
          task.run(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<>(TvShowModuleManager.SETTINGS.getTvShowDataSource());
          for (Integer i : updateTvDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new TvShowUpdateDatasourceTask2(dataSources.get(i - 1));
              task.run(); // blocking
            }
          }
        }
        LOGGER.info("Commandline - found " + TvShowList.getInstance().getNewTvShows().size() + " TvShow(s) containing "
            + TvShowList.getInstance().getNewEpisodes().size() + " new episode(s)");
      }

      // *****************
      // prepare shows/episodes for scrape
      // *****************
      List<TvShow> showToScrape = new ArrayList<>();
      List<TvShowEpisode> episodeToScrape = new ArrayList<>();
      if (scrapeAll) {
        LOGGER.info("Commandline - scraping ALL TvShows...");
        if (TvShowList.getInstance().getTvShowCount() > 0) {
          showToScrape = TvShowList.getInstance().getTvShows();
          episodeToScrape.clear(); // scraping complete show
        }
      }
      else {
        HashSet<TvShow> scrapeShow = new HashSet<>(); // no dupes
        HashSet<TvShowEpisode> scrapeEpisode = new HashSet<>(); // no dupes

        if (scrapeNew) {
          List<TvShow> newTv = TvShowList.getInstance().getNewTvShows();
          List<TvShowEpisode> newEp = TvShowList.getInstance().getNewEpisodes();
          LOGGER.info("Commandline - scraping new TvShows...");
          if (newTv.size() > 0) {
            scrapeShow.addAll(newTv);
          }
          LOGGER.info("Commandline - scraping new episodes...");
          if (newEp.size() > 0) {
            scrapeEpisode.addAll(newEp);
          }
        }

        if (scrapeUnscraped) {
          LOGGER.info("Commandline - scraping unscraped TvShows...");
          List<TvShow> unscrapedShows = TvShowList.getInstance().getUnscrapedTvShows();
          List<TvShowEpisode> unscrapedEpisodes = TvShowList.getInstance().getUnscrapedEpisodes();
          if (unscrapedShows.size() > 0) {
            scrapeShow.addAll(unscrapedShows);
          }
          LOGGER.info("Commandline - scraping unscraped episodes...");
          if (unscrapedEpisodes.size() > 0) {
            scrapeEpisode.addAll(unscrapedEpisodes);
          }
        }

        // if we scrape already the whole show, no need to scrape dedicated episodes for it
        HashSet<TvShowEpisode> removedEpisode = new HashSet<>(); // no dupes
        for (TvShowEpisode ep : scrapeEpisode) {
          if (scrapeShow.contains(ep.getTvShow())) {
            removedEpisode.add(ep);
          }
        }
        scrapeEpisode.removeAll(removedEpisode);
        showToScrape = new ArrayList<>(scrapeShow);
        episodeToScrape = new ArrayList<>(scrapeEpisode);
      }

      // *****************
      // do the scrape
      // *****************
      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.loadDefaults();
      if (showToScrape.size() > 0) {
        if (dryRun) {
          for (TvShow show : showToScrape) {
            LOGGER.info("DRYRUN: would have scraped show " + show.getTitle() + " with " + show.getEpisodeCount() + " episodes");
          }
        }
        else {
          task = new TvShowScrapeTask(showToScrape, true, options);
          task.run(); // blocking
          // wait for other tmm threads (artwork download et all)
          while (TmmTaskManager.getInstance().poolRunning()) {
            Thread.sleep(2000);
          }
        }
      }
      if (episodeToScrape.size() > 0) {
        if (dryRun) {
          for (TvShowEpisode ep : episodeToScrape) {
            LOGGER.info("DRYRUN: would have scraped episode " + ep.getTvShow().getTitle() + " S:" + ep.getSeason() + " E:" + ep.getEpisode());
          }
        }
        else {
          task = new TvShowEpisodeScrapeTask(episodeToScrape, options.getMetadataScraper());
          task.run(); // blocking
          // wait for other tmm threads (artwork download et all)
          while (TmmTaskManager.getInstance().poolRunning()) {
            Thread.sleep(2000);
          }
        }
      }

      // *****************
      // RENAME
      // *****************
      if (rename) {
        LOGGER.info("Commandline - rename & cleanup new shows...");
        if (showToScrape.size() > 0) {
          if (dryRun) {
            for (TvShow show : showToScrape) {
              LOGGER.info("DRYRUN: would have renamed show " + show.getTitle() + " with " + show.getEpisodeCount() + " episodes");
            }
          }
          else {
            task = new TvShowRenameTask(showToScrape, null, true);
            task.run(); // blocking
            // wait for other tmm threads (artwork download et all)
            while (TmmTaskManager.getInstance().poolRunning()) {
              Thread.sleep(2000);
            }
          }
        }
        LOGGER.info("Commandline - rename & cleanup new episodes...");
        if (episodeToScrape.size() > 0) {
          if (dryRun) {
            for (TvShowEpisode ep : episodeToScrape) {
              LOGGER.info("DRYRUN: would have renamed episode " + ep.getTvShow().getTitle() + " S:" + ep.getSeason() + " E:" + ep.getEpisode());
            }
          }
          else {
            task = new TvShowRenameTask(null, episodeToScrape, true); // just rename new EPs AND root folder
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
              LOGGER.warn("MediaFile not found! " + mf.getFileAsPath());
              allOk = false;
            }
          }
        }
        for (TvShow s : TvShowList.getInstance().getTvShows()) {
          System.out.print(".");
          for (MediaFile mf : s.getMediaFiles()) { // show MFs
            if (!mf.exists()) {
              System.out.println();
              LOGGER.warn("MediaFile not found! " + mf.getFileAsPath());
              allOk = false;
            }
          }
          for (TvShowEpisode episode : new ArrayList<>(s.getEpisodes())) {
            for (MediaFile mf : episode.getMediaFiles()) { // episode MFs
              if (!mf.exists()) {
                System.out.println();
                LOGGER.warn("MediaFile not found! " + mf.getFileAsPath());
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

      if (updateAvailable) {
        LOGGER.warn("=====================================================");
        LOGGER.warn("There's a new TMM version available! Please update!");
        LOGGER.warn("=====================================================");
      }
    }
    catch (Exception e) {
      LOGGER.error("Error executing command line task!", e);
    }
  }
}
