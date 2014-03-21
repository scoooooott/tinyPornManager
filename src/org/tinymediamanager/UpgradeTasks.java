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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaTrailer;

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
        if (movie.getId("tmdbId") != null && movie.getId("tmdbId") instanceof String) {
          try {
            Integer tmdbid = Integer.parseInt((String) movie.getId("tmdbId"));
            movie.setId("tmdbId", tmdbid);
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
        for (MediaTrailer trailer : movie.getTrailers()) {
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
          for (String ds : Globals.settings.getMovieSettings().getMovieDataSource()) {
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
   * Convert the database from the old combined format (tmm.odb) to the module specific parts
   */
  public static void convertDatabase() {
    File oldDb = new File("tmm.odb");
    if (oldDb.exists()) {
      try {
        // convert old Database into the new one
        // a) start the database connections
        EntityManager oldEntityManager = startUpOldDatabase();
        MovieModuleManager.getInstance().startUp();
        TvShowModuleManager.getInstance().startUp();

        // b) load entities
        MovieList.getInstance().loadMoviesFromDatabase(oldEntityManager);
        TvShowList.getInstance().loadTvShowsFromDatabase(oldEntityManager);

        // c) persist entities to the new databases
        EntityManager entityManager = MovieModuleManager.getInstance().getEntityManager();
        entityManager.getTransaction().begin();
        for (Movie movie : MovieList.getInstance().getMovies()) {
          oldEntityManager.detach(movie);
          movie.saveToDb();
        }
        for (MovieSet movieSet : MovieList.getInstance().getMovieSetList()) {
          oldEntityManager.detach(movieSet);
          movieSet.saveToDb();
        }
        entityManager.getTransaction().commit();

        entityManager = TvShowModuleManager.getInstance().getEntityManager();

        for (TvShow show : TvShowList.getInstance().getTvShows()) {
          oldEntityManager.detach(show);
          entityManager.getTransaction().begin();
          show.saveToDb();
          entityManager.getTransaction().commit();
        }

        // close database connections
        MovieModuleManager.getInstance().shutDown();
        TvShowModuleManager.getInstance().shutDown();

        closeOldDatabase(oldEntityManager);
      }
      catch (Exception e) {
        LOGGER.error("could not convert database: ", e);
      }

      // delete the old database
      oldDb.deleteOnExit();
    }
  }

  private static EntityManager startUpOldDatabase() {
    if (System.getProperty("tmmenhancer") != null) {
      com.objectdb.Enhancer.enhance("org.tinymediamanager.core.entities.*");
      com.objectdb.Enhancer.enhance("org.tinymediamanager.core.movie.entities.*");
      com.objectdb.Enhancer.enhance("org.tinymediamanager.core.tvshow.entities.*");
    }
    // get a connection to the database
    EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("tmm.odb");
    EntityManager entityManager;
    try {
      entityManager = entityManagerFactory.createEntityManager();
    }
    catch (PersistenceException e) {
      if (e.getCause().getMessage().contains("does not match db file")) {
        // happens when there's a recovery file which does not match (cannot be recovered) - just delete and try again
        FileUtils.deleteQuietly(new File("tmm.odb$"));
        entityManager = entityManagerFactory.createEntityManager();
      }
      else {
        // unknown
        throw (e);
      }
    }
    return entityManager;
  }

  private static void closeOldDatabase(EntityManager em) {
    EntityManagerFactory emf = em.getEntityManagerFactory();
    em.close();
    emf.close();
  }
}
