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
package org.tinymediamanager.core.movie;

import java.io.File;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ITmmModule;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The class MovieModuleManager. Used to manage the movies module
 * 
 * @author Manuel Laggner
 */
public class MovieModuleManager implements ITmmModule {
  public static final MovieSettings MOVIE_SETTINGS = Globals.settings.getMovieSettings();

  private static final String       MODULE_TITLE   = "Movie management";
  private static final String       MOVIE_DB       = "movies";
  private static MovieModuleManager instance;

  private boolean                   enabled;
  private MVStore                   mvStore;
  private ObjectMapper              objectMapper;
  private ObjectWriter              movieObjectWriter;
  private ObjectWriter              movieSetObjectWriter;

  private MVMap<UUID, String>       movieMap;
  private MVMap<UUID, String>       movieSetMap;

  private MovieModuleManager() {
    enabled = false;
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
  public void startUp() throws Exception {
    // configure database
    mvStore = new MVStore.Builder().fileName(MOVIE_DB).compressHigh().open();
    mvStore.setAutoCommitDelay(2000); // 2 sec
    mvStore.setRetentionTime(0);
    mvStore.setReuseSpace(true);

    // configure JSON
    objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    objectMapper.setSerializationInclusion(Include.NON_DEFAULT);

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
      for (String ds : MOVIE_SETTINGS.getMovieDataSource()) {
        File file = new File(ds + File.separator + Constants.BACKUP_FOLDER);
        FileUtils.deleteQuietly(file);
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  void persistMovie(Movie movie) throws Exception {
    String newValue = movieObjectWriter.writeValueAsString(movie);
    String oldValue = movieMap.get(movie.getDbId());

    if (!StringUtils.equals(newValue, oldValue)) {
      // write movie to DB
      movieMap.put(movie.getDbId(), newValue);

      // write NFO if needed
      if (StringUtils.isNotBlank(oldValue)) {
        movie.writeNFO();
      }
    }
  }

  void removeMovieFromDb(Movie movie) throws Exception {
    movieMap.remove(movie.getDbId());
  }

  void persistMovieSet(MovieSet movieSet) throws Exception {
    String newValue = movieSetObjectWriter.writeValueAsString(movieSet);
    String oldValue = movieMap.get(movieSet.getDbId());
    if (!StringUtils.equals(newValue, oldValue)) {
      movieSetMap.put(movieSet.getDbId(), newValue);
    }
  }

  void removeMovieSetFromDb(MovieSet movieSet) throws Exception {
    movieSetMap.remove(movieSet.getDbId());
  }
}
