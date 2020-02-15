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
package org.tinymediamanager.core.tvshow;

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
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The class TvShowModuleManager. Used to manage the tv show module
 * 
 * @author Manuel Laggner
 */
public class TvShowModuleManager implements ITmmModule {
  private static final ResourceBundle BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control());
  public static final TvShowSettings  SETTINGS     = TvShowSettings.getInstance();

  private static final String         MODULE_TITLE = "TV show management";
  private static final String         TV_SHOW_DB   = "tvshows.db";
  private static final Logger         LOGGER       = LoggerFactory.getLogger(TvShowModuleManager.class);
  private static TvShowModuleManager  instance;

  private boolean                     enabled;
  private MVStore                     mvStore;
  private ObjectWriter                tvShowObjectWriter;
  private ObjectWriter                episodeObjectWriter;

  private MVMap<UUID, String>         tvShowMap;
  private MVMap<UUID, String>         episodeMap;

  private List<String>                startupMessages;

  private TvShowModuleManager() {
    enabled = false;
    startupMessages = new ArrayList<>();
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
  public void startUp() {
    // configure database
    Path databaseFile = Paths.get(Globals.settings.getSettingsFolder(), TV_SHOW_DB);
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
        Utils.deleteFileSafely(Paths.get(TV_SHOW_DB + ".corrupted"));
        Utils.moveFileSafe(databaseFile, Paths.get(TV_SHOW_DB + ".corrupted"));
        mvStore = new MVStore.Builder().fileName(databaseFile.toString()).compressHigh().autoCommitBufferSize(4096).open();

        // inform user that the DB could not be loaded
        startupMessages.add(BUNDLE.getString("tvshow.loaddb.failed"));
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
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode node = mapper.readValue(tvShowMap.get(tvshow.getDbId()), ObjectNode.class);

      ArrayNode episodes = JsonNodeFactory.instance.arrayNode();
      for (TvShowEpisode ep : tvshow.getEpisodes()) {
        ObjectNode epNode = mapper.readValue(episodeMap.get(ep.getDbId()), ObjectNode.class);
        episodes.add(epNode);
        // TODO: dump EP IDs !!!
      }
      node.set("episodes", episodes);

      String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
      LOGGER.info("Dumping TvShow: {}\n{}", tvshow.getDbId(), s);
    }
    catch (Exception e) {
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

  void removeTvShowFromDb(TvShow tvShow) {
    tvShowMap.remove(tvShow.getDbId());
  }

  void persistEpisode(TvShowEpisode episode) throws Exception {
    String newValue = episodeObjectWriter.writeValueAsString(episode);
    String oldValue = episodeMap.get(episode.getDbId());

    if (!StringUtils.equals(newValue, oldValue)) {
      episodeMap.put(episode.getDbId(), newValue);
    }
  }

  void removeEpisodeFromDb(TvShowEpisode episode) {
    episodeMap.remove(episode.getDbId());
  }

  @Override
  public void initializeDatabase() {
    Utils.deleteFileSafely(Paths.get(Settings.getInstance().getSettingsFolder(), TV_SHOW_DB));
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
