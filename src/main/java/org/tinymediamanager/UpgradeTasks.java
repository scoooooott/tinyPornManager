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

import static org.tinymediamanager.core.MediaFileType.TRAILER;
import static org.tinymediamanager.core.MediaFileType.VIDEO;
import static org.tinymediamanager.core.Utils.deleteFileSafely;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Platform;

/**
 * The class UpdateTasks. To perform needed update tasks
 *
 * @author Manuel Laggner / Myron Boyle
 */
public class UpgradeTasks {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeTasks.class);

  public static void performUpgradeTasksBeforeDatabaseLoading(String oldVersion) {
    String v = "" + oldVersion;
    if (StringUtils.isBlank(v)) {
      v = "3"; // set version for other updates
    }

    // ****************************************************
    // PLEASE MAKE THIS TO RUN MULTIPLE TIMES WITHOUT ERROR
    // NEEDED FOR NIGHTLY SNAPSHOTS ET ALL
    // SVN BUILD IS ALSO CONSIDERED AS LOWER !!!
    // ****************************************************

    // upgrade to v3 (OR DO THIS IF WE ARE INSIDE IDE)
    // if (StrgUtils.compareVersion(v, "3") < 0) {
    // LOGGER.info("Performing upgrade tasks to version 3");
    // }

    // migrate image cache to hex folders
    if (StrgUtils.compareVersion(v, "3.0.5") < 0) {
      LOGGER.info("Performing upgrade tasks to version 3.0.5");

      // clear all files from /cache (except the all subfolders)
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(Globals.CACHE_FOLDER))) {
        for (Path path : stream) {
          if (!path.toFile().isDirectory()) {
            Utils.deleteFileSafely(path);
          }
        }
      }
      catch (Exception e) {
        LOGGER.warn("could not clean up cache folder - {}", e.getMessage());
      }
    }

    // move old plugins to the backup folder
    if (StrgUtils.compareVersion(v, "3.1") < 0) {
      LOGGER.info("Performing upgrade tasks to version 3.1");

      Path pluginFolder = Paths.get("plugins");

      // clear all files from /cache (except the all subfolders)
      if (Files.exists(pluginFolder) && pluginFolder.toFile().isDirectory()) {
        try {
          Path backupFolder = Paths.get(Globals.BACKUP_FOLDER);
          Utils.moveDirectorySafe(pluginFolder, backupFolder.resolve(pluginFolder.getFileName()));
        }
        catch (Exception e) {
          LOGGER.warn("could not movie plugins folder to the backup folder: {}", e.getMessage());
        }
      }
    }

    // delete nfd on osx
    if (StrgUtils.compareVersion(v, "3.1.2") < 0) {
      LOGGER.info("Performing upgrade tasks to version 3.1");

      if (SystemUtils.IS_OS_MAC) {
        Utils.deleteFileSafely(Paths.get("native", "mac", "liblwjgl.dylib"));
        Utils.deleteFileSafely(Paths.get("native", "mac", "liblwjgl_nfd.dylib"));
      }
    }

    // adopt space substitution settings for TV shows
    if (StrgUtils.compareVersion(v, "3.1.5") < 0) {
      LOGGER.info("Performing upgrade tasks to version 3.1");

      try {
        ObjectMapper mapper = new ObjectMapper();
        Map settingsMap = mapper.readValue(new File("data/tvShows.json"), Map.class);

        Boolean renamerSpaceSubstitution = (Boolean) settingsMap.get("renamerSpaceSubstitution");
        String renamerSpaceReplacement = (String) settingsMap.get("renamerSpaceReplacement");
        if (renamerSpaceSubstitution != null && renamerSpaceReplacement != null) {
          TvShowSettings.getInstance().setRenamerFilenameSpaceSubstitution(renamerSpaceSubstitution);
          TvShowSettings.getInstance().setRenamerFilenameSpaceReplacement(renamerSpaceReplacement);
          TvShowSettings.getInstance().setRenamerSeasonPathnameSpaceSubstitution(renamerSpaceSubstitution);
          TvShowSettings.getInstance().setRenamerSeasonPathnameSpaceReplacement(renamerSpaceReplacement);
          TvShowSettings.getInstance().setRenamerShowPathnameSpaceSubstitution(renamerSpaceSubstitution);
          TvShowSettings.getInstance().setRenamerShowPathnameSpaceReplacement(renamerSpaceReplacement);
          TvShowSettings.getInstance().saveSettings();
        }
      }
      catch (Exception e) {
        LOGGER.warn("could not adopt space substitution settings: {}", e.getMessage());
      }

    }
  }

  /**
   * performs some upgrade tasks from one version to another<br>
   * <b>make sure, this upgrade can run multiple times (= needed for nightlies!!!)
   *
   * @param oldVersion
   *          our current version
   */
  public static void performUpgradeTasksAfterDatabaseLoading(String oldVersion) {
    MovieList movieList = MovieList.getInstance();
    TvShowList tvShowList = TvShowList.getInstance();

    String v = "" + oldVersion;

    if (StringUtils.isBlank(v)) {
      v = "3"; // set version for other updates
    }

    // ****************************************************
    // PLEASE MAKE THIS TO RUN MULTIPLE TIMES WITHOUT ERROR
    // NEEDED FOR NIGHTLY SNAPSHOTS ET ALL
    // GIT BUILD IS ALSO CONSIDERED AS LOWER !!!
    // ****************************************************

    // upgrade to v3.0
    if (StrgUtils.compareVersion(v, "3.0.0") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3");
      // clean old style backup files
      ArrayList<Path> al = new ArrayList<>();

      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Globals.BACKUP_FOLDER))) {
        for (Path path : directoryStream) {
          if (path.getFileName().toString().matches("movies\\.db\\.\\d{4}\\-\\d{2}\\-\\d{2}\\.zip")
              || path.getFileName().toString().matches("tvshows\\.db\\.\\d{4}\\-\\d{2}\\-\\d{2}\\.zip")) {
            al.add(path);
          }
        }
      }
      catch (IOException ignored) {
      }

      for (Path path : al) {
        deleteFileSafely(path);
      }

      // has been expanded to space
      if (MovieSettings.getInstance().getRenamerColonReplacement().equals("")) {
        MovieSettings.getInstance().setRenamerColonReplacement(" ");
        MovieSettings.getInstance().saveSettings();
      }
      if (TvShowSettings.getInstance().getRenamerColonReplacement().equals("")) {
        TvShowSettings.getInstance().setRenamerColonReplacement(" ");
        TvShowSettings.getInstance().saveSettings();
      }
    }

    // upgrade to v3.0.1
    if (StrgUtils.compareVersion(v, "3.0.1") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3.0.1");
      // remove the tvShowSeason id from TV shows
      for (TvShow tvShow : TvShowList.getInstance().getTvShows()) {
        if (tvShow.getIds().containsKey("tvShowSeason")) {
          tvShow.removeId("tvShowSeason");
          tvShow.saveToDb();
        }
      }

      // remove "http://thetvdb.com/banners/" artwork urls from episodes
      for (TvShow tvShow : TvShowList.getInstance().getTvShows()) {
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          if (episode.getArtworkUrl(MediaFileType.THUMB).equals("http://thetvdb.com/banners/")) {
            episode.setArtworkUrl("", MediaFileType.THUMB);
            episode.saveToDb();
          }
        }
      }
    }

    // upgrade to v3.0.2
    if (StrgUtils.compareVersion(v, "3.0.2") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3.0.2");
      // set episode year
      for (TvShow tvShow : TvShowList.getInstance().getTvShows()) {
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          if (episode.getYear() == 0 && episode.getFirstAired() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(episode.getFirstAired());
            episode.setYear(calendar.get(Calendar.YEAR));
            episode.saveToDb();
          }
        }
      }
    }

    // add stream flags for old booleans
    if (StrgUtils.compareVersion(v, "3.0.3") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3.0.3");

      for (Movie movie : MovieList.getInstance().getMovies()) {
        boolean dirty = false;
        for (MediaFile mf : movie.getMediaFiles()) {
          for (MediaFileAudioStream as : mf.getAudioStreams()) {
            // the IS method checks already for new field
            if (as.defaultStream && !as.isDefaultStream()) {
              as.setDefaultStream(true);
              dirty = true;
            }
          }
          for (MediaFileSubtitle sub : mf.getSubtitles()) {
            // the IS method checks already for new field
            if (sub.defaultStream && !sub.isDefaultStream()) {
              sub.setDefaultStream(true);
              dirty = true;
            }
            if (sub.forced && !sub.isForced()) {
              sub.setForced(true);
              dirty = true;
            }
          }
        }
        if (dirty) {
          movie.saveToDb();
        }
      }

      for (TvShow tvShow : TvShowList.getInstance().getTvShows()) {
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          boolean dirty = false;
          for (MediaFile mf : episode.getMediaFiles()) {
            for (MediaFileAudioStream as : mf.getAudioStreams()) {
              // the IS method checks already for new field
              if (as.defaultStream && !as.isDefaultStream()) {
                as.setDefaultStream(true);
                dirty = true;
              }
            }
            for (MediaFileSubtitle sub : mf.getSubtitles()) {
              // the IS method checks already for new field
              if (sub.defaultStream && !sub.isDefaultStream()) {
                sub.setDefaultStream(true);
                dirty = true;
              }
              if (sub.forced && !sub.isForced()) {
                sub.setForced(true);
                dirty = true;
              }
            }
          }
          if (dirty) {
            episode.saveToDb();
          }
        }
      }
    }

    // migrate image cache to hex folders
    if (StrgUtils.compareVersion(v, "3.0.4") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3.0.4");
      ImageCache.migrate();

      // change unknown file extension to regex
      // look if there is any regexp in the list to avoid double upgrade in devel mode
      boolean alreadyMigrated = false;
      for (String entry : Settings.getInstance().getCleanupFileType()) {
        if (entry.endsWith("$")) {
          alreadyMigrated = true;
          break;
        }
      }

      if (!alreadyMigrated) {
        List<String> newEntries = new ArrayList<>();
        for (String entry : Settings.getInstance().getCleanupFileType()) {
          newEntries.add(entry + "$");
        }
        Settings.getInstance().setCleanupFileTypes(newEntries);
      }
    }

    if (StrgUtils.compareVersion(v, "3.1") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3.1");

      for (Movie movie : MovieList.getInstance().getMovies()) {
        boolean dirty = false;
        for (MediaFile mf : movie.getMediaFiles()) {
          if (mf.isHDR() && StringUtils.isBlank(mf.getHdrFormat())) {
            mf.setHdrFormat("HDR10");
            dirty = true;
          }
          if ("iso".equalsIgnoreCase(mf.getExtension()) && !mf.isISO()) {
            mf.setIsISO(true);
            dirty = true;
          }
        }
        if (dirty) {
          movie.saveToDb();
        }
      }

      for (TvShow tvShow : TvShowList.getInstance().getTvShows()) {
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          boolean dirty = false;
          for (MediaFile mf : episode.getMediaFiles()) {
            if (mf.isHDR() && StringUtils.isBlank(mf.getHdrFormat())) {
              mf.setHdrFormat("HDR10");
              dirty = true;
            }
            if ("iso".equalsIgnoreCase(mf.getExtension()) && !mf.isISO()) {
              mf.setIsISO(true);
              dirty = true;
            }
          }
          if (dirty) {
            episode.saveToDb();
          }
        }
      }
    }

    if (StrgUtils.compareVersion(v, "3.1.3") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3.1.3");

      // remove legacy imdbId
      for (Movie movie : MovieList.getInstance().getMovies()) {
        movie.setId("imdbId", null);
      }

      // convert movie set ids from tmdb to tmdbSet
      for (MovieSet movieSet : MovieList.getInstance().getMovieSetList()) {
        if (movieSet.getId(Constants.TMDB) != null) {
          // do not overwrite any existing new one
          if (movieSet.getId(Constants.TMDB_SET) == null) {
            movieSet.setId(Constants.TMDB_SET, movieSet.getId(Constants.TMDB));
          }
          // remove the old one
          movieSet.setId(Constants.TMDB, null);
          movieSet.saveToDb();
        }
      }
    }

    if (StrgUtils.compareVersion(v, "3.1.6") < 0) {
      LOGGER.info("Performing database upgrade tasks to version 3.1.6");

      for (Movie movie : MovieList.getInstance().getMovies()) {
        boolean dirty = false;
        for (MediaFile mediaFile : movie.getMediaFiles(VIDEO, TRAILER)) {
          switch (mediaFile.getVideoCodec().toLowerCase(Locale.ROOT)) {
            case "hevc":
            case "x265":
              mediaFile.setVideoCodec("h265");
              dirty = true;
              break;

          }
        }
        if (dirty) {
          movie.saveToDb();
        }
      }

      for (TvShow tvShow : TvShowList.getInstance().getTvShows()) {
        boolean dirty = false;
        for (MediaFile mediaFile : tvShow.getMediaFiles(VIDEO, TRAILER)) {
          switch (mediaFile.getVideoCodec().toLowerCase(Locale.ROOT)) {
            case "hevc":
            case "x265":
              mediaFile.setVideoCodec("h265");
              dirty = true;
              break;

          }
        }
        if (dirty) {
          tvShow.saveToDb();
        }

        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          dirty = false;
          for (MediaFile mediaFile : episode.getMediaFiles(VIDEO, TRAILER)) {
            switch (mediaFile.getVideoCodec().toLowerCase(Locale.ROOT)) {
              case "hevc":
              case "x265":
                mediaFile.setVideoCodec("h265");
                dirty = true;
                break;

            }
          }
          if (dirty) {
            episode.saveToDb();
          }
        }
      }
    }
  }

  /**
   * rename downloaded files (getdown.jar, ...)
   */
  public static void renameDownloadedFiles() {
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

    // exe launchers
    if (Platform.isWindows()) {
      file = new File("tinyMediaManager.new");
      if (file.exists() && file.length() > 10000 && file.length() < 100000) {
        File cur = new File("tinyMediaManager.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update tmm!");
        }
      }
      file = new File("tinyMediaManagerUpd.new");
      if (file.exists() && file.length() > 10000 && file.length() < 100000) {
        File cur = new File("tinyMediaManagerUpd.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update the updater!");
        }
      }
      file = new File("tinyMediaManagerCMD.new");
      if (file.exists() && file.length() > 10000 && file.length() < 100000) {
        File cur = new File("tinyMediaManagerCMD.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update CMD TMM!");
        }
      }
    }

    // OSX launcher
    if (Platform.isMac()) {
      file = new File("JavaApplicationStub.new");
      if (file.exists() && file.length() > 0) {
        File cur = new File("../../MacOS/JavaApplicationStub");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update JavaApplicationStub");
        }
      }
    }

    // OSX Info.plist
    if (Platform.isMac()) {
      file = new File("Info.plist");
      if (file.exists() && file.length() > 0) {
        File cur = new File("../../Info.plist");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update JavaApplicationStub");
        }
      }
    }

    // OSX tmm.icns
    if (Platform.isMac()) {
      file = new File("tmm.icns");
      if (file.exists() && file.length() > 0) {
        File cur = new File("../tmm.icns");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update tmm.icns");
        }
      }
    }
  }
}
