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
package org.tinymediamanager.core.movie;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.CustomNullStringSerializerProvider;
import org.tinymediamanager.core.ITmmModule;
import org.tinymediamanager.core.NullKeySerializer;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The class MovieModuleManager. Used to manage the movies module
 * 
 * @author Manuel Laggner
 */
public class MovieModuleManager implements ITmmModule {
  private static final ResourceBundle BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control());
  public static final MovieSettings   SETTINGS     = MovieSettings.getInstance();

  private static final String         MODULE_TITLE = "Movie management";
  private static final String         MOVIE_DB     = "movies.db";
  private static final Logger         LOGGER       = LoggerFactory.getLogger(MovieModuleManager.class);
  private static MovieModuleManager   instance;

  private boolean                     enabled;
  private MVStore                     mvStore;
  private ObjectWriter                movieObjectWriter;
  private ObjectWriter                movieSetObjectWriter;

  private MVMap<UUID, String>         movieMap;
  private MVMap<UUID, String>         movieSetMap;

  private List<String>                startupMessages;

  private MovieModuleManager() {
    enabled = false;
    startupMessages = new ArrayList<>();
  }

  public static MovieModuleManager getInstance() {
    if (instance == null) {
      instance = new MovieModuleManager();
    }
    return instance;
  }

  @Override
  public String getModuleTitle() {
    return MODULE_TITLE;
  }

  @Override
  public void startUp() {
    // configure database
    Path databaseFile = Paths.get(Globals.settings.getSettingsFolder(), MOVIE_DB);
    try {
      mvStore = new MVStore.Builder().fileName(databaseFile.toString()).compressHigh().autoCommitBufferSize(4096).open();
    }
    catch (Exception e) {
      // look if the file is locked by another process (rethrow rather than delete the db file)
      if (e instanceof IllegalStateException && e.getMessage().contains("file is locked")) {
        throw e;
      }

      LOGGER.error("Could not open database file: {}", e.getMessage());
      LOGGER.info("starting over with an empty database file");

      try {
        Utils.deleteFileSafely(Paths.get(MOVIE_DB + ".corrupted"));
        Utils.moveFileSafe(databaseFile, Paths.get(MOVIE_DB + ".corrupted"));
        mvStore = new MVStore.Builder().fileName(databaseFile.toString()).compressHigh().autoCommitBufferSize(4096).open();

        // inform user that the DB could not be loaded
        startupMessages.add(BUNDLE.getString("movie.loaddb.failed"));
      }
      catch (Exception e1) {
        LOGGER.error("could not move old database file and create a new one: {}", e1.getMessage());
      }
    }
    mvStore.setAutoCommitDelay(2000); // 2 sec
    mvStore.setRetentionTime(0);
    mvStore.setReuseSpace(true);
    mvStore.setCacheSize(8);

    // configure JSON
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setTimeZone(TimeZone.getDefault());
    objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
    objectMapper.setSerializerProvider(new CustomNullStringSerializerProvider());
    objectMapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());

    movieObjectWriter = objectMapper.writerFor(Movie.class);
    movieSetObjectWriter = objectMapper.writerFor(MovieSet.class);

    movieMap = mvStore.openMap("movies");
    movieSetMap = mvStore.openMap("movieSets");

    MovieList.getInstance().loadMoviesFromDatabase(movieMap, objectMapper);
    MovieList.getInstance().loadMovieSetsFromDatabase(movieSetMap, objectMapper);
    MovieList.getInstance().initDataAfterLoading();
    enabled = true;
  }

  @Override
  public void shutDown() throws Exception {
    mvStore.compactMoveChunks();
    mvStore.close();

    enabled = false;

    if (Globals.settings.isDeleteTrashOnExit()) {
      for (String ds : SETTINGS.getMovieDataSource()) {
        Path file = Paths.get(ds, Constants.BACKUP_FOLDER);
        Utils.deleteDirectoryRecursive(file);
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * dumps a whole movie to logfile
   * 
   * @param movie
   *          the movie to make the dump for
   */
  public void dump(Movie movie) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Object json = mapper.readValue(movieMap.get(movie.getDbId()), Object.class);
      String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
      LOGGER.info("Dumping Movie: {}\n{}", movie.getDbId(), s);
    }
    catch (Exception e) {
      LOGGER.error("Cannot parse JSON!", e);
    }
  }

  /**
   * dumps a whole movieset to logfile
   * 
   * @param movieSet
   *          the movieset to make the dump for
   */
  public void dump(MovieSet movieSet) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Object json = mapper.readValue(movieSetMap.get(movieSet.getDbId()), Object.class);
      String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
      LOGGER.info("Dumping MovieSet: {}\n{}", movieSet.getDbId(), s);
    }
    catch (Exception e) {
      LOGGER.error("Cannot parse JSON!", e);
    }
  }

  void persistMovie(Movie movie) throws Exception {
    String newValue = movieObjectWriter.writeValueAsString(movie);
    String oldValue = movieMap.get(movie.getDbId());

    if (!StringUtils.equals(newValue, oldValue)) {
      // write movie to DB
      movieMap.put(movie.getDbId(), newValue);
    }
  }

  void removeMovieFromDb(Movie movie) {
    movieMap.remove(movie.getDbId());
  }

  void persistMovieSet(MovieSet movieSet) throws Exception {
    String newValue = movieSetObjectWriter.writeValueAsString(movieSet);
    String oldValue = movieSetMap.get(movieSet.getDbId());
    if (!StringUtils.equals(newValue, oldValue)) {
      movieSetMap.put(movieSet.getDbId(), newValue);
    }
  }

  void removeMovieSetFromDb(MovieSet movieSet) {
    movieSetMap.remove(movieSet.getDbId());
  }

  @Override
  public void initializeDatabase() {
    Utils.deleteFileSafely(Paths.get(Settings.getInstance().getSettingsFolder(), MOVIE_DB));
  }

  @Override
  public void saveSettings() {
    SETTINGS.saveSettings();
  }

  @Override
  public List<String> getStartupMessages() {
    return startupMessages;
  }
}
