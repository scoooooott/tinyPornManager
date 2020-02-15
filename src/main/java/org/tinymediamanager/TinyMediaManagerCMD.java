/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import static org.tinymediamanager.TinyMediaManager.shutdownLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ExportTemplate;
import org.tinymediamanager.core.MediaEntityExporter.TemplateType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieComparator;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieRenameTask;
import org.tinymediamanager.core.movie.tasks.MovieScrapeTask;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask;
import org.tinymediamanager.core.tasks.UpdaterTask;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowComparator;
import org.tinymediamanager.core.tvshow.TvShowEpisodeScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowExporter;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowEpisodeScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The class TinyMediaManagerCMD - used for all logic for the command line tool
 * 
 * @author Manuel Laggner
 */
class TinyMediaManagerCMD {
  private static final Logger     LOGGER          = LoggerFactory.getLogger(TinyMediaManagerCMD.class);
  private static boolean          updateMovies    = false;
  private static boolean          updateTv        = false;
  private static boolean          scrapeAll       = false;
  private static boolean          scrapeNew       = false;
  private static boolean          scrapeUnscraped = false;
  private static boolean          rename          = false;
  private static boolean          dryRun          = false;
  private static boolean          checkFiles      = false;
  private static boolean          export          = false;

  // datasource IDs
  private static HashSet<Integer> updateMovieDs   = new HashSet<>();
  private static HashSet<Integer> updateTvDs      = new HashSet<>();

  private static Path             exportTemplate  = null;
  private static Path             exportDir       = null;

  private TinyMediaManagerCMD() {
    // hide the public constructor for utility classes
  }

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
          shutdownLogger();
          System.exit(0);
        }
      }
      else if (cmd.equalsIgnoreCase("-checkFiles")) {
        checkFiles = true;
      }
      else if (cmd.equalsIgnoreCase("-rename") || cmd.equalsIgnoreCase("-renameNew")) { // "new" deprecated
        rename = true;
      }

      // ************
      // ** EXPORT **
      // ************
      else if (cmd.equalsIgnoreCase("-export")) {
        try {
          i++;
          if (i == args.length || i == args.length - 1) { // export needs 2 parameters
            throw new Exception("missing parameters");
          }
          exportTemplate = Paths.get("templates", args[i]);
          if (!Files.isDirectory(exportTemplate)) {
            throw new IOException("template folder not found/accessible");
          }
          i++;
          exportDir = Paths.get(args[i]);
          export = true;
        }
        catch (Exception e) {
          System.out.println("ERROR: export failed because of: " + e.getMessage());
          printSyntax();
          shutdownLogger();
          System.exit(0);
        }
      }
      else if (cmd.toLowerCase(Locale.ROOT).contains("help")) { // -help, --help, help ...
        printSyntax();
        shutdownLogger();
        System.exit(0);
      }
      else {
        System.out.println("ERROR: unrecognized command " + cmd);
        printSyntax();
        shutdownLogger();
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
        "=== tinyMediaManager (c) 2012-2020 Manuel Laggner ===\n" +
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
        "    -updateMovies         update all movie datasources\n" +
        "    -updateMoviesX        replace X with 1-9 - just updates a single movie datasource; ordering like GUI\n" +
        "    -updateTv             update all TvShow\n" +
        "    -updateTvX            replace X with 1-9 - just updates a single TvShow datasource; ordering like GUI\n" +
        "    -update               update all (short for '-updateMovies -updateTv')\n" +
        "\n" +
        "    SCRAPE: auto-scrapes (force best match) your specified items:\n" +
        "    -scrapeNew            only NEW FOUND movies/TvShows/episodes from former update\n" +
        "    -scrapeUnscraped      all movies/TvShows/episodes, which have not yet been scraped\n" +
        "    -scrapeAll            ALL movies/TvShows/episodes, whether they have already been scraped or not\n" +
        "\n" +
        "    -rename               rename & cleanup all the movies/TvShows/episodes from former scrape command\n" +
        "    -export template dir  exports your complete movie/tv library with specified template to dir\n" +
        "    -checkFiles           does a physical check, if all files in DB are existent on filesystem (might take long!)\n" +
        "\n" +
        "\n" +
        "EXAMPLES:\n" +
        "\n" +
        "    tinyMediaManagerCMD.exe -updateMovies -updateTv3 -scrapeNew -rename\n" +
        "    tinyMediaManagerCMD.exe -scrapeUnscraped -rename\n" +
        "    tinyMediaManagerCMD.exe -export ExcelXml /user/export/movies\n" +
        "    tinyMediaManagerCMD.exe -export TvShowDetailExampleXml /user/export/tv" +
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
          task = new MovieUpdateDatasourceTask();
          task.run(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<>(MovieModuleManager.SETTINGS.getMovieDataSource());
          for (Integer i : updateMovieDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new MovieUpdateDatasourceTask(dataSources.get(i - 1));
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
        HashSet<Movie> scrape = new HashSet<>(); // no dupes
        if (scrapeNew) {
          LOGGER.info("Commandline - scraping new movies...");
          List<Movie> newMovies = MovieList.getInstance().getNewMovies();
          if (!newMovies.isEmpty()) {
            scrape.addAll(newMovies);
          }
        }
        if (scrapeUnscraped) {
          LOGGER.info("Commandline - scraping all unscraped movies...");
          List<Movie> unscrapedMovies = MovieList.getInstance().getUnscrapedMovies();
          if (!unscrapedMovies.isEmpty()) {
            scrape.addAll(unscrapedMovies);
          }
        }
        moviesToScrape.addAll(new ArrayList<>(scrape));
      }

      if (!moviesToScrape.isEmpty()) {
        MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
        List<MovieScraperMetadataConfig> config = MovieModuleManager.SETTINGS.getScraperMetadataConfig();
        options.loadDefaults();
        if (dryRun) {
          for (Movie movie : moviesToScrape) {
            LOGGER.info("DRYRUN: would have scraped {}", movie.getTitle());
          }
        }
        else {
          task = new MovieScrapeTask(moviesToScrape, true, options, config);
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
        if (!moviesToScrape.isEmpty()) {
          if (dryRun) {
            for (Movie movie : moviesToScrape) {
              LOGGER.info("DRYRUN: would have renamed {}", movie.getTitle());
            }
          }
          else {
            task = new MovieRenameTask(moviesToScrape);
            task.run(); // blocking}
          }
        }
      }

      // *****************
      // EXPORT
      // *****************
      if (export) {
        for (ExportTemplate t : MovieExporter.findTemplates(TemplateType.MOVIE)) {
          if (t.getPath().equals(exportTemplate.toAbsolutePath().toString())) {
            // ok, our template has been found under movies
            LOGGER.info("Commandline - exporting movies...");
            if (dryRun) {
              LOGGER.info("DRYRUN: would have exported ALL movies to {}", exportDir.toAbsolutePath());
            }
            else {
              MovieExporter ex = new MovieExporter(Paths.get(t.getPath()));
              List<Movie> movies = MovieList.getInstance().getMovies();
              movies.sort(new MovieComparator());
              ex.export(movies, exportDir);
            }
            break;
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
          task = new TvShowUpdateDatasourceTask();
          task.run(); // blocking
        }
        else {
          List<String> dataSources = new ArrayList<>(TvShowModuleManager.SETTINGS.getTvShowDataSource());
          for (Integer i : updateTvDs) {
            if (dataSources != null && dataSources.size() >= i - 1) {
              task = new TvShowUpdateDatasourceTask(dataSources.get(i - 1));
              task.run(); // blocking
            }
          }
        }
        LOGGER.info("Commandline - found {} TvShow(s) containing {} new episode(s)", TvShowList.getInstance().getNewTvShows().size(),
            TvShowList.getInstance().getNewEpisodes().size());
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
          if (!newTv.isEmpty()) {
            scrapeShow.addAll(newTv);
          }
          LOGGER.info("Commandline - scraping new episodes...");
          if (!newEp.isEmpty()) {
            scrapeEpisode.addAll(newEp);
          }
        }

        if (scrapeUnscraped) {
          LOGGER.info("Commandline - scraping unscraped TvShows...");
          List<TvShow> unscrapedShows = TvShowList.getInstance().getUnscrapedTvShows();
          List<TvShowEpisode> unscrapedEpisodes = TvShowList.getInstance().getUnscrapedEpisodes();
          if (!unscrapedShows.isEmpty()) {
            scrapeShow.addAll(unscrapedShows);
          }
          LOGGER.info("Commandline - scraping unscraped episodes...");
          if (!unscrapedEpisodes.isEmpty()) {
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
      List<TvShowScraperMetadataConfig> tvShowScraperMetadataConfig = TvShowModuleManager.SETTINGS.getTvShowScraperMetadataConfig();
      List<TvShowEpisodeScraperMetadataConfig> episodeScraperMetadataConfig = TvShowModuleManager.SETTINGS.getEpisodeScraperMetadataConfig();

      options.loadDefaults();
      if (!showToScrape.isEmpty()) {
        if (dryRun) {
          for (TvShow show : showToScrape) {
            LOGGER.info("DRYRUN: would have scraped show {} with {} episodes", show.getTitle(), show.getEpisodeCount());
          }
        }
        else {
          task = new TvShowScrapeTask(showToScrape, true, options, tvShowScraperMetadataConfig, episodeScraperMetadataConfig);
          task.run(); // blocking
          // wait for other tmm threads (artwork download et all)
          while (TmmTaskManager.getInstance().poolRunning()) {
            Thread.sleep(2000);
          }
        }
      }
      if (!episodeToScrape.isEmpty()) {
        if (dryRun) {
          for (TvShowEpisode ep : episodeToScrape) {
            LOGGER.info("DRYRUN: would have scraped episode {} S:{} E:{}", ep.getTvShow().getTitle(), ep.getSeason(), ep.getEpisode());
          }
        }
        else {
          TvShowEpisodeSearchAndScrapeOptions options1 = new TvShowEpisodeSearchAndScrapeOptions();
          options1.setDataFromOtherOptions(options);

          task = new TvShowEpisodeScrapeTask(episodeToScrape, options1, episodeScraperMetadataConfig);
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
        if (!showToScrape.isEmpty()) {
          if (dryRun) {
            for (TvShow show : showToScrape) {
              LOGGER.info("DRYRUN: would have renamed show {} with {} episodes", show.getTitle(), show.getEpisodeCount());
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
        if (!episodeToScrape.isEmpty()) {
          if (dryRun) {
            for (TvShowEpisode ep : episodeToScrape) {
              LOGGER.info("DRYRUN: would have renamed episode {} S:{} E:{}", ep.getTvShow().getTitle(), ep.getSeason(), ep.getEpisode());
            }
          }
          else {
            task = new TvShowRenameTask(null, episodeToScrape, true); // just rename new EPs AND root folder
            task.run(); // blocking
          }
        }
      }
      // *****************
      // EXPORT
      // *****************
      if (export) {
        for (ExportTemplate t : TvShowExporter.findTemplates(TemplateType.TV_SHOW)) {
          if (t.getPath().equals(exportTemplate.toAbsolutePath().toString())) {
            // ok, our template has been found under movies
            LOGGER.info("Commandline - exporting tv shows...");
            if (dryRun) {
              LOGGER.info("DRYRUN: would have exported ALL TV shows to {}", exportDir.toAbsolutePath());
            }
            else {
              TvShowExporter ex = new TvShowExporter(Paths.get(t.getPath()));
              List<TvShow> tvShows = TvShowList.getInstance().getTvShows();
              tvShows.sort(new TvShowComparator());
              ex.export(tvShows, exportDir);
            }
            break;
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
              LOGGER.warn("MediaFile not found! {}", mf.getFileAsPath());
              allOk = false;
            }
          }
        }
        for (TvShow s : TvShowList.getInstance().getTvShows()) {
          System.out.print(".");
          for (MediaFile mf : s.getMediaFiles()) { // show MFs
            if (!mf.exists()) {
              System.out.println();
              LOGGER.warn("MediaFile not found! {}", mf.getFileAsPath());
              allOk = false;
            }
          }
          for (TvShowEpisode episode : new ArrayList<>(s.getEpisodes())) {
            for (MediaFile mf : episode.getMediaFiles()) { // episode MFs
              if (!mf.exists()) {
                System.out.println();
                LOGGER.warn("MediaFile not found! {}", mf.getFileAsPath());
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
