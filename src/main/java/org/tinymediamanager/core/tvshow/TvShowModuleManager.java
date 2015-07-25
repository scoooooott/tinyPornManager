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
package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ITmmModule;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The class TvShowModuleManager. Used to manage the tv show module
 * 
 * @author Manuel Laggner
 */
public class TvShowModuleManager implements ITmmModule {
  public static final TvShowSettings TV_SHOW_SETTINGS = Globals.settings.getTvShowSettings();

  private static final String        MODULE_TITLE = "TV show management";
  private static final String        TV_SHOW_DB   = "tvshows.db";
  private static TvShowModuleManager instance;

  private boolean      enabled;
  private MVStore      mvStore;
  private ObjectMapper objectMapper;
  private ObjectWriter tvShowObjectWriter;
  private ObjectWriter episodeObjectWriter;

  private MVMap<UUID, String> tvShowMap;
  private MVMap<UUID, String> episodeMap;

  private TvShowModuleManager() {
    enabled = false;
  }

  public static TvShowModuleManager getInstance() {
    if (instance == null) {
      instance = new TvShowModuleManager();
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
    mvStore = new MVStore.Builder().fileName(TV_SHOW_DB).compressHigh().open();
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

    tvShowObjectWriter = objectMapper.writerFor(TvShow.class);
    episodeObjectWriter = objectMapper.writerFor(TvShowEpisode.class);

    tvShowMap = mvStore.openMap("tvshows");
    episodeMap = mvStore.openMap("episodes");

    TvShowList.getInstance().loadTvShowsFromDatabase(tvShowMap, objectMapper);
    TvShowList.getInstance().loadEpisodesFromDatabase(episodeMap, objectMapper);
    TvShowList.getInstance().initDataAfterLoading();
    enabled = true;
  }

  @Override
  public void shutDown() throws Exception {
    mvStore.compactMoveChunks();
    mvStore.close();

    enabled = false;

    if (Globals.settings.isDeleteTrashOnExit()) {
      for (String ds : Globals.settings.getTvShowSettings().getTvShowDataSource()) {
        File file = new File(ds + File.separator + Constants.BACKUP_FOLDER);
        FileUtils.deleteQuietly(file);
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  void persistTvShow(TvShow tvShow) throws Exception {
    String newValue = tvShowObjectWriter.writeValueAsString(tvShow);
    String oldValue = tvShowMap.get(tvShow.getDbId());

    if (!StringUtils.equals(newValue, oldValue)) {
      // write to DB
      tvShowMap.put(tvShow.getDbId(), newValue);
    }
  }

  void removeTvShowFromDb(TvShow tvShow) throws Exception {
    tvShowMap.remove(tvShow.getDbId());
  }

  void persistEpisode(TvShowEpisode episode) throws Exception {
    String newValue = episodeObjectWriter.writeValueAsString(episode);
    String oldValue = episodeMap.get(episode.getDbId());

    if (!StringUtils.equals(newValue, oldValue)) {
      episodeMap.put(episode.getDbId(), newValue);
    }
  }

  void removeEpisodeFromDb(TvShowEpisode episode) throws Exception {
    episodeMap.remove(episode.getDbId());
  }

  @Override
  public void initializeDatabase() throws Exception {
    FileUtils.deleteQuietly(new File(TV_SHOW_DB));
  }
}
