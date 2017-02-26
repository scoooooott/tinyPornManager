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
package org.tinymediamanager.core.tvshow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.CustomNullStringSerializerProvider;
import org.tinymediamanager.core.ITmmModule;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The class TvShowModuleManager. Used to manage the tv show module
 * 
 * @author Manuel Laggner
 */
public class TvShowModuleManager implements ITmmModule {
  public static final TvShowSettings SETTINGS     = Globals.settings.getTvShowSettings();

  private static final String        MODULE_TITLE = "TV show management";
  private static final String        TV_SHOW_DB   = "tvshows.db";
  private static final Logger        LOGGER       = LoggerFactory.getLogger(TvShowModuleManager.class);
  private static TvShowModuleManager instance;

  private boolean                    enabled;
  private MVStore                    mvStore;
  private ObjectMapper               objectMapper;
  private ObjectWriter               tvShowObjectWriter;
  private ObjectWriter               episodeObjectWriter;

  private MVMap<UUID, String>        tvShowMap;
  private MVMap<UUID, String>        episodeMap;

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
    // do a DB backup, and keep last 15 copies
    Path db = Paths.get(Settings.getInstance().getSettingsFolder(), TV_SHOW_DB);
    Utils.createBackupFile(db);
    Utils.deleteOldBackupFile(db, 15);

    // configure database
    mvStore = new MVStore.Builder().fileName(Paths.get(Settings.getInstance().getSettingsFolder(), TV_SHOW_DB).toString()).compressHigh()
        .backgroundExceptionHandler(new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread t, Throwable e) {
            LOGGER.error("Error in the background thread of the persistent cache", e);
          }
        }).autoCommitBufferSize(4096).open();
    mvStore.setAutoCommitDelay(2000); // 2 sec
    mvStore.setRetentionTime(0);
    mvStore.setReuseSpace(true);

    // configure JSON
    objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    objectMapper.setTimeZone(TimeZone.getDefault());
    objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
    objectMapper.setSerializerProvider(new CustomNullStringSerializerProvider());

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
      for (String ds : SETTINGS.getTvShowDataSource()) {
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
   * dumps a whole tvshow to logfile
   * 
   * @param tvshow
   *          the TV show to dump the data for
   */
  public void dump(TvShow tvshow) {
    try {
      JSONObject show = new JSONObject(tvShowObjectWriter.writeValueAsString(tvshow));
      JSONArray episodes = new JSONArray();
      for (TvShowEpisode ep : tvshow.getEpisodes()) {
        JSONObject epJson = new JSONObject(episodeObjectWriter.writeValueAsString(ep));
        episodes.put(epJson);
      }
      show.put("episodes", episodes);
      LOGGER.info("Dumping TvShow:\n" + show.toString(4));
    }
    catch (JsonProcessingException e) {
      LOGGER.error("Cannot parse JSON!", e);
    }
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
    Utils.deleteFileSafely(Paths.get(Settings.getInstance().getSettingsFolder(), TV_SHOW_DB));
  }
}
