/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.UpdaterTask;
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
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
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
  private static boolean          scrapeNew       = false;
  private static boolean          scrapeUnscraped = false;
  private static boolean          renameNew       = false;
  private static boolean          checkFiles      = false;

  // datasource IDs
  private static HashSet<Integer> updateMovieDs   = new HashSet<Integer>();
  private static HashSet<Integer> updateTvDs      = new HashSet<Integer>();

  /**
   * parse command line params
   * 
   * @param args
   */
  static void parseParams(String[] args) {
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
        printSyntax();
        System.exit(0);
      }
      else {
        System.out.println("ERROR: unrecognized command '" + cmd);
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
        "=== tinyMediaManager (c) 2012-2014 Manuel Laggner ===\n" +
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
   * executes all the command line tasks, one after another
   */
  static void startCommandLineTasks() {
    try {
      TmmTask task = null;
      boolean updateAvailable = false;

      if (scrapeNew || scrapeUnscraped) {
        // only do an update check when we are scraping online
        // no need for a "forced" check for just updating the datasource
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
