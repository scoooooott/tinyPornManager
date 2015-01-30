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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

import com.sun.jna.Platform;

/**
 * The class UpdateTasks. To perform needed update tasks
 * 
 * @author Manuel Laggner
 */
public class UpgradeTasks {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeTasks.class);

  public static void performUpgradeTasksBeforeDatabaseLoading(String oldVersion) {
    String v = "" + oldVersion;

    if (StringUtils.isBlank(v)) {
      LOGGER.info("Performing upgrade tasks to version 2.0");
      // upgrade from alpha/beta to "TV Show" 2.0 format
      // happens only once
      JOptionPane.showMessageDialog(null,
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
      v = "2.0"; // set version for other updates
    }
  }

  public static void performUpgradeTasksAfterDatabaseLoading(String oldVersion) {
    MovieList movieList = MovieList.getInstance();
    TvShowList tvShowList = TvShowList.getInstance();
    String v = "" + oldVersion;

    if (StringUtils.isBlank(v)) {
      v = "2.0"; // set version for other updates
    }

    if (compareVersion(v, "2.5") < 0) {
      LOGGER.info("Performing upgrade tasks to version 2.5");
      // upgrade tasks for movies; added with 2.5;
      EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (Movie movie : movieList.getMovies()) {
        movie.findActorImages();
        movie.saveToDb();
      }
      entityManager.getTransaction().commit();
    }

    if (compareVersion(v, "2.5.2") < 0) {
      LOGGER.info("Performing upgrade tasks to version 2.5.2");
      // clean tmdb id
      EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (Movie movie : movieList.getMovies()) {
        if (movie.getId(Constants.TMDBID) != null && movie.getId(Constants.TMDBID) instanceof String) {
          try {
            Integer tmdbid = Integer.parseInt((String) movie.getId(Constants.TMDBID));
            movie.setTmdbId(tmdbid);
            movie.saveToDb();
          }
          catch (Exception e) {
          }
        }
      }
      entityManager.getTransaction().commit();
    }

    if (compareVersion(v, "2.5.3") < 0) {
      LOGGER.info("Performing upgrade tasks to version 2.5.3");
      // upgrade tasks for trailers; remove extension from quality
      EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (Movie movie : movieList.getMovies()) {
        for (MovieTrailer trailer : movie.getTrailers()) {
          // 720p (mp4)
          String quality = trailer.getQuality().split(" ")[0];
          trailer.setQuality(quality);
        }
        movie.saveToDb();
      }
      entityManager.getTransaction().commit();

      // upgrade tasks for tv show episodes -> clean the path
      entityManager = TvShowModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (TvShow tvShow : tvShowList.getTvShows()) {
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          List<MediaFile> videos = episode.getMediaFiles(MediaFileType.VIDEO);
          if (videos.size() > 0) {
            MediaFile mf = videos.get(0);
            if (!mf.getPath().equals(episode.getPath())) {
              episode.setPath(mf.getPath());
              episode.saveToDb();
            }
          }
        }
      }
      entityManager.getTransaction().commit();
    }

    if (compareVersion(v, "2.5.4") < 0) {
      LOGGER.info("Performing upgrade tasks to version 2.5.4");
      // repair missing datasources
      EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (Movie movie : movieList.getMovies()) {
        if (StringUtils.isBlank(movie.getDataSource())) {
          for (String ds : MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource()) {
            if (movie.getPath().startsWith(ds)) {
              movie.setDataSource(ds);
              break;
            }
          }
        }
        // remove MacOS ignore MFs (borrowed from UDS)
        List<MediaFile> mediaFiles = new ArrayList<MediaFile>(movie.getMediaFiles());
        for (MediaFile mf : mediaFiles) {
          if (mf.getFilename().startsWith("._")) { // remove MacOS ignore files
            movie.removeFromMediaFiles(mf);
          }
        }
      }
      entityManager.getTransaction().commit();

      entityManager = TvShowModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (TvShow show : tvShowList.getTvShows()) {
        if (StringUtils.isBlank(show.getDataSource())) {
          for (String ds : Globals.settings.getTvShowSettings().getTvShowDataSource()) {
            if (show.getPath().startsWith(ds)) {
              show.setDataSource(ds);
              break;
            }
          }
        }
        // remove MacOS ignore MFs (borrowed from UDS)
        List<MediaFile> mediaFiles = new ArrayList<MediaFile>(show.getMediaFiles());
        for (MediaFile mf : mediaFiles) {
          if (mf.getFilename().startsWith("._")) { // remove MacOS ignore files
            show.removeFromMediaFiles(mf);
          }
        }
        List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>(show.getEpisodes());
        for (TvShowEpisode episode : episodes) {
          mediaFiles = new ArrayList<MediaFile>(episode.getMediaFiles());
          for (MediaFile mf : mediaFiles) {
            if (mf.getFilename().startsWith("._")) { // remove MacOS ignore files
              episode.removeFromMediaFiles(mf);
            }
          }
          // lets have a look if there is at least one video file for this episode
          List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
          if (mfs.size() == 0) {
            show.removeEpisode(episode);
          }
        }
      }
      entityManager.getTransaction().commit();
    }

    if (compareVersion(v, "2.6") < 0) {
      // THUMBS are getting EXTRATHUMBS
      LOGGER.info("Performing upgrade tasks to version 2.6");
      EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (Movie movie : movieList.getMovies()) {
        for (MediaFile mf : movie.getMediaFiles(MediaFileType.THUMB)) {
          mf.setType(MediaFileType.EXTRATHUMB);
        }
      }
      entityManager.getTransaction().commit();
    }

    if (compareVersion(v, "2.6.1") < 0) {
      LOGGER.info("Performing upgrade tasks to version 2.6.1");
      // change the old TVDB Id to the new one
      EntityManager entityManager = TvShowModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (TvShow show : tvShowList.getTvShows()) {
        Object obj = show.getId("tvdb");
        if (obj != null) {
          show.setId(Constants.TVDBID, obj);
          show.removeId("tvdb");
        }
      }
      entityManager.getTransaction().commit();
    }

    if (compareVersion(v, "2.6.6") < 0) {
      LOGGER.info("Performing upgrade tasks to version 2.6.6");
      if (SystemUtils.IS_OS_LINUX) {
        TmmOsUtils.createDesktopFileForLinux(new File(TmmOsUtils.DESKTOP_FILE));
      }

      // upgrade ALL String languages to ISO3
      EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
      entityManager.getTransaction().begin();
      for (Movie movie : movieList.getMovies()) {
        for (MediaFile mf : movie.getMediaFiles()) {
          for (MediaFileAudioStream mfa : mf.getAudioStreams()) {
            if (!StringUtils.isEmpty(mfa.getLanguage())) {
              mfa.setLanguage(Utils.getIso3LanguageFromLocalizedString(mfa.getLanguage()));
            }
          }
          for (MediaFileSubtitle mfs : mf.getSubtitles()) {
            if (!StringUtils.isEmpty(mfs.getLanguage())) {
              mfs.setLanguage(Utils.getIso3LanguageFromLocalizedString(mfs.getLanguage()));
            }
          }
        }
      }
      entityManager.getTransaction().commit();

      // TV Shows
      entityManager.getTransaction().begin();
      for (TvShow show : tvShowList.getTvShows()) {
        // show MFs
        for (MediaFile mf : show.getMediaFiles()) {
          for (MediaFileAudioStream mfa : mf.getAudioStreams()) {
            if (!StringUtils.isEmpty(mfa.getLanguage())) {
              mfa.setLanguage(Utils.getIso3LanguageFromLocalizedString(mfa.getLanguage()));
            }
          }
          for (MediaFileSubtitle mfs : mf.getSubtitles()) {
            if (!StringUtils.isEmpty(mfs.getLanguage())) {
              mfs.setLanguage(Utils.getIso3LanguageFromLocalizedString(mfs.getLanguage()));
            }
          }
        }
        // Episode MFs
        for (TvShowEpisode episode : show.getEpisodes()) {
          for (MediaFile mf : episode.getMediaFiles()) {
            for (MediaFileAudioStream mfa : mf.getAudioStreams()) {
              if (!StringUtils.isEmpty(mfa.getLanguage())) {
                mfa.setLanguage(Utils.getIso3LanguageFromLocalizedString(mfa.getLanguage()));
              }
            }
            for (MediaFileSubtitle mfs : mf.getSubtitles()) {
              if (!StringUtils.isEmpty(mfs.getLanguage())) {
                mfs.setLanguage(Utils.getIso3LanguageFromLocalizedString(mfs.getLanguage()));
              }
            }
          }
        }
      }
      entityManager.getTransaction().commit();
    }

  }

  /**
   * compares the given version (v1) against another one (v2)
   * 
   * @param v1
   * @param v2
   * @return < 0 if v1 is lower<br>
   *         > 0 if v1 is higher<br>
   *         = 0 if equal
   */
  private static int compareVersion(String v1, String v2) {
    String s1 = normalisedVersion(v1);
    String s2 = normalisedVersion(v2);
    return s1.compareTo(s2);
  }

  private static String normalisedVersion(String version) {
    return normalisedVersion(version, ".", 4);
  }

  private static String normalisedVersion(String version, String sep, int maxWidth) {
    String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
    StringBuilder sb = new StringBuilder();
    for (String s : split) {
      sb.append(String.format("%" + maxWidth + 's', s));
    }
    return sb.toString();
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
      if (file.exists() && file.length() > 10000 && file.length() < 50000) {
        File cur = new File("tinyMediaManager.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update tmm!");
        }
      }
      file = new File("tinyMediaManagerUpd.new");
      if (file.exists() && file.length() > 10000 && file.length() < 50000) {
        File cur = new File("tinyMediaManagerUpd.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update the updater!");
        }
      }
      file = new File("tinyMediaManagerCMD.new");
      if (file.exists() && file.length() > 10000 && file.length() < 50000) {
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
  }
}
