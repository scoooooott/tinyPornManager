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

import static org.tinymediamanager.core.Constants.ADDED_TV_SHOW;
import static org.tinymediamanager.core.Constants.EPISODE_COUNT;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.REMOVED_TV_SHOW;
import static org.tinymediamanager.core.Constants.TV_SHOWS;
import static org.tinymediamanager.core.Constants.TV_SHOW_COUNT;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * The Class TvShowList.
 * 
 * @author Manuel Laggner
 */
public class TvShowList extends AbstractModelObject {
  private static final Logger    LOGGER   = LoggerFactory.getLogger(TvShowList.class);
  private static TvShowList      instance = null;

  private final List<TvShow>     tvShowList;
  private final List<String>     tvShowTagsObservable;
  private final List<String>     episodeTagsObservable;
  private final List<String>     videoCodecsObservable;
  private final List<String>     audioCodecsObservable;

  private PropertyChangeListener propertyChangeListener;

  /**
   * Instantiates a new TvShowList.
   */
  private TvShowList() {
    // create the lists
    tvShowList = ObservableCollections.observableList(Collections.synchronizedList(new ArrayList<TvShow>()));
    tvShowTagsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<String>());
    episodeTagsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<String>());
    videoCodecsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<String>());
    audioCodecsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<String>());

    // the tag listener: its used to always have a full list of all tags used in tmm
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // listen to changes of tags
        if ("tag".equals(evt.getPropertyName()) && evt.getSource() instanceof TvShow) {
          TvShow tvShow = (TvShow) evt.getSource();
          updateTvShowTags(tvShow);
        }
        if ("tag".equals(evt.getPropertyName()) && evt.getSource() instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) evt.getSource();
          updateEpisodeTags(episode);
        }
        if ((MEDIA_FILES.equals(evt.getPropertyName()) || MEDIA_INFORMATION.equals(evt.getPropertyName()))
            && evt.getSource() instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) evt.getSource();
          updateMediaInformationLists(episode);
        }
        if (EPISODE_COUNT.equals(evt.getPropertyName())) {
          firePropertyChange(EPISODE_COUNT, 0, 1);
        }
      }
    };
  }

  /**
   * Gets the single instance of TvShowList.
   * 
   * @return single instance of TvShowList
   */
  public static TvShowList getInstance() {
    if (instance == null) {
      instance = new TvShowList();
    }

    return instance;
  }

  /**
   * Gets the tv shows.
   * 
   * @return the tv shows
   */
  public List<TvShow> getTvShows() {
    return tvShowList;
  }

  /**
   * Gets the unscraped TvShows
   * 
   * @return the unscraped TvShows
   */
  public List<TvShow> getUnscrapedTvShows() {
    List<TvShow> unscrapedShows = new ArrayList<>();
    for (TvShow show : tvShowList) {
      if (!show.isScraped()) {
        unscrapedShows.add(show);
      }
    }
    return unscrapedShows;
  }

  /**
   * Adds the tv show.
   * 
   * @param newValue
   *          the new value
   */
  public void addTvShow(TvShow newValue) {
    int oldValue = tvShowList.size();

    tvShowList.add(newValue);
    newValue.addPropertyChangeListener(propertyChangeListener);
    firePropertyChange(TV_SHOWS, null, tvShowList);
    firePropertyChange(ADDED_TV_SHOW, null, newValue);
    firePropertyChange(TV_SHOW_COUNT, oldValue, tvShowList.size());
  }

  /**
   * Removes the datasource.
   * 
   * @param path
   *          the path
   */
  public void removeDatasource(String path) {
    if (StringUtils.isEmpty(path)) {
      return;
    }

    for (int i = tvShowList.size() - 1; i >= 0; i--) {
      TvShow tvShow = tvShowList.get(i);
      if (new File(path).equals(new File(tvShow.getDataSource()))) {
        removeTvShow(tvShow);
      }
    }
  }

  /**
   * Removes the tv show.
   * 
   * @param tvShow
   *          the tvShow
   */
  public void removeTvShow(TvShow tvShow) {
    int oldValue = tvShowList.size();
    tvShow.removeAllEpisodes();
    tvShowList.remove(tvShow);

    try {
      TvShowModuleManager.getInstance().removeTvShowFromDb(tvShow);
    }
    catch (Exception e) {
      LOGGER.error("problem removing TV show from DB: " + e.getMessage());
    }

    firePropertyChange(TV_SHOWS, null, tvShowList);
    firePropertyChange(REMOVED_TV_SHOW, null, tvShow);
    firePropertyChange(TV_SHOW_COUNT, oldValue, tvShowList.size());
  }

  /**
   * Removes the tv show from tmm and deletes all files from the data source
   * 
   * @param tvShow
   *          the tvShow
   */
  public void deleteTvShow(TvShow tvShow) {
    int oldValue = tvShowList.size();

    tvShow.deleteFilesSafely();
    tvShow.removeAllEpisodes();
    tvShowList.remove(tvShow);

    try {
      TvShowModuleManager.getInstance().removeTvShowFromDb(tvShow);
    }
    catch (Exception e) {
      LOGGER.error("problem removing TV show from DB: " + e.getMessage());
    }

    firePropertyChange(TV_SHOWS, null, tvShowList);
    firePropertyChange(REMOVED_TV_SHOW, null, tvShow);
    firePropertyChange(TV_SHOW_COUNT, oldValue, tvShowList.size());
  }

  /**
   * Gets the tv show count.
   * 
   * @return the tv show count
   */
  public int getTvShowCount() {
    return tvShowList.size();
  }

  /**
   * Gets the episode count.
   * 
   * @return the episode count
   */
  public int getEpisodeCount() {
    int count = 0;
    for (int i = 0; i < tvShowList.size(); i++) {
      TvShow tvShow = tvShowList.get(i);
      count += tvShow.getEpisodeCount();
    }

    return count;
  }

  public TvShow lookupTvShow(UUID uuid) {
    for (TvShow tvShow : tvShowList) {
      if (tvShow.getDbId().equals(uuid)) {
        return tvShow;
      }
    }
    return null;
  }

  /**
   * Load tv shows from database.
   */
  void loadTvShowsFromDatabase(MVMap<UUID, String> tvShowMap, ObjectMapper objectMapper) {
    // load all TV shows from the database
    ObjectReader tvShowObjectReader = objectMapper.readerFor(TvShow.class);

    for (UUID uuid : new ArrayList<>(tvShowMap.keyList())) {
      try {
        TvShow tvShow = tvShowObjectReader.readValue(tvShowMap.get(uuid));
        tvShow.setDbId(uuid);

        // for performance reasons we add tv shows directly
        tvShowList.add(tvShow);
      }
      catch (Exception e) {
        LOGGER.warn("problem decoding TV show json string: " + e.getMessage());
        LOGGER.info("dropping corrupt TV show");
        tvShowMap.remove(uuid);
      }
    }
    LOGGER.info("found " + tvShowList.size() + " TV shows in database");
  }

  /**
   * Load episodes from database.
   */
  void loadEpisodesFromDatabase(MVMap<UUID, String> episodesMap, ObjectMapper objectMapper) {
    // load all episodes from the database
    ObjectReader episodeObjectReader = objectMapper.readerFor(TvShowEpisode.class);
    int episodeCount = 0;

    for (UUID uuid : new ArrayList<>(episodesMap.keyList())) {
      try {
        episodeCount++;
        TvShowEpisode episode = episodeObjectReader.readValue(episodesMap.get(uuid));
        episode.setDbId(uuid);

        // and assign it the the right TV show
        for (TvShow tvShow : tvShowList) {
          if (tvShow.getDbId().equals(episode.getTvShowDbId())) {
            episode.setTvShow(tvShow);
            tvShow.addEpisode(episode);
            break;
          }
        }
      }
      catch (Exception e) {
        LOGGER.warn("problem decoding episode json string: " + e.getMessage());
        LOGGER.info("dropping corrupt episode");
        episodesMap.remove(uuid);
      }
    }
    LOGGER.info("found " + episodeCount + " episodes in database");
  }

  void initDataAfterLoading() {
    // check for corrupted media entities
    checkAndCleanupMediaFiles();

    // init everything after loading
    for (TvShow tvShow : tvShowList) {
      tvShow.initializeAfterLoading();
      updateTvShowTags(tvShow);

      for (TvShowEpisode episode : tvShow.getEpisodes()) {
        episode.initializeAfterLoading();
        updateEpisodeTags(episode);
        updateMediaInformationLists(episode);
      }

      tvShow.addPropertyChangeListener(propertyChangeListener);
    }
  }

  public void persistTvShow(TvShow tvShow) {
    // update/insert this TV show to the database
    try {
      TvShowModuleManager.getInstance().persistTvShow(tvShow);
    }
    catch (Exception e) {
      LOGGER.error("failed to persist episode: " + tvShow.getTitle() + "; " + e.getMessage());
    }
  }

  public void removeTvShowFromDb(TvShow tvShow) {
    // delete this TV show from the database
    try {
      TvShowModuleManager.getInstance().removeTvShowFromDb(tvShow);
    }
    catch (Exception e) {
      LOGGER.error("failed to remove episode: " + tvShow.getTitle() + "; " + e.getMessage());
    }
  }

  public void persistEpisode(TvShowEpisode episode) {
    // update/insert this episode to the database
    try {
      TvShowModuleManager.getInstance().persistEpisode(episode);
    }
    catch (Exception e) {
      LOGGER.error("failed to persist episode: " + episode.getTvShow().getTitle() + " - S" + episode.getSeason() + "E" + episode.getEpisode() + " - "
          + episode.getTitle() + "; " + e.getMessage());
    }
  }

  public void removeEpisodeFromDb(TvShowEpisode episode) {
    // delete this episode from the database
    try {
      TvShowModuleManager.getInstance().removeEpisodeFromDb(episode);
    }
    catch (Exception e) {
      LOGGER.error("failed to remove episode: " + episode.getTvShow().getTitle() + " - S" + episode.getSeason() + "E" + episode.getEpisode() + " - "
          + episode.getTitle() + "; " + e.getMessage());
    }
  }

  public MediaScraper getDefaultMediaScraper() {
    MediaScraper scraper = MediaScraper.getMediaScraperById(TvShowModuleManager.SETTINGS.getTvShowScraper(), ScraperType.TV_SHOW);
    if (scraper == null) {
      scraper = MediaScraper.getMediaScraperById(Constants.TVDB, ScraperType.TV_SHOW);
    }
    return scraper;
  }

  public MediaScraper getMediaScraperById(String providerId) {
    return MediaScraper.getMediaScraperById(providerId, ScraperType.TV_SHOW);
  }

  public List<MediaScraper> getAvailableMediaScrapers() {
    List<MediaScraper> availableScrapers = MediaScraper.getMediaScrapers(ScraperType.TV_SHOW);
    Collections.sort(availableScrapers, new TvShowMediaScraperComparator());
    return availableScrapers;
  }

  /**
   * Gets all available artwork scrapers.
   * 
   * @return the artwork scrapers
   */
  public List<MediaScraper> getAvailableArtworkScrapers() {
    List<MediaScraper> availableScrapers = MediaScraper.getMediaScrapers(ScraperType.TV_SHOW_ARTWORK);
    // we can use the TvShowMediaScraperComparator here too, since TheTvDb should also be first
    Collections.sort(availableScrapers, new TvShowMediaScraperComparator());
    return availableScrapers;
  }

  /**
   * get all specified artwork scrapers
   * 
   * @return the specified artwork scrapers
   */
  public List<MediaScraper> getArtworkScrapers(List<String> providerIds) {
    List<MediaScraper> artworkScrapers = new ArrayList<>();

    for (String providerId : providerIds) {
      if (StringUtils.isBlank(providerId)) {
        continue;
      }
      MediaScraper artworkScraper = MediaScraper.getMediaScraperById(providerId, ScraperType.TV_SHOW_ARTWORK);
      if (artworkScraper != null) {
        artworkScrapers.add(artworkScraper);
      }
    }

    return artworkScrapers;
  }

  /**
   * get all default (specified via settings) artwork scrapers
   * 
   * @return the specified artwork scrapers
   */
  public List<MediaScraper> getDefaultArtworkScrapers() {
    return getArtworkScrapers(TvShowModuleManager.SETTINGS.getTvShowArtworkScrapers());
  }

  /**
   * Search tv show with the default language.
   * 
   * @param searchTerm
   *          the search term
   * @param mediaScraper
   *          the media scraper
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, MediaScraper mediaScraper) {
    return searchTvShow(searchTerm, mediaScraper, TvShowModuleManager.SETTINGS.getScraperLanguage());
  }

  /**
   * Search tv show with the chosen language.
   * 
   * @param searchTerm
   *          the search term
   * @param mediaScraper
   *          the media scraper
   * @param language
   *          the language to search with
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, MediaScraper mediaScraper, MediaLanguages language) {
    // format searchstring
    // searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    List<MediaSearchResult> searchResult = null;
    try {
      ITvShowMetadataProvider provider;

      if (mediaScraper == null) {
        provider = (ITvShowMetadataProvider) getDefaultMediaScraper().getMediaProvider();
      }
      else {
        provider = (ITvShowMetadataProvider) mediaScraper.getMediaProvider();
      }

      MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW, searchTerm);
      options.setLanguage(LocaleUtils.toLocale(language.name()));
      options.setCountry(TvShowModuleManager.SETTINGS.getCertificationCountry());
      LOGGER.info("=====================================================");
      LOGGER.info("Searching with scraper: " + provider.getProviderInfo().getId() + ", " + provider.getProviderInfo().getVersion());
      LOGGER.info(options.toString());
      LOGGER.info("=====================================================");
      searchResult = provider.search(options);

      // if result is empty, try all scrapers
      // FIXME only needed if we have more "true" scrapers
      // if (searchResult.isEmpty()) {
      // LOGGER.debug("no result yet - trying alternate scrapers");
      // for (TvShowScrapers ts : TvShowScrapers.values()) {
      // ITvShowMetadataProvider provider2 = getMetadataProvider(ts);
      // if (provider.getProviderInfo().equals(provider2.getProviderInfo())) {
      // continue;
      // }
      // searchResult = provider2.search(options);
      // if (!searchResult.isEmpty()) {
      // break;
      // }
      // }
      // }
    }
    catch (Exception e) {
      LOGGER.error("searchTvShow", e);
    }

    return searchResult;
  }

  private void updateTvShowTags(TvShow tvShow) {
    List<String> availableTags = new ArrayList<>(tvShowTagsObservable);

    for (String tagInTvShow : new ArrayList<>(tvShow.getTags())) {
      boolean tagFound = false;
      for (String tag : availableTags) {
        if (tagInTvShow.equals(tag)) {
          tagFound = true;
          break;
        }
      }
      if (!tagFound) {
        addTvShowTag(tagInTvShow);
      }
    }
  }

  private void addTvShowTag(String newTag) {
    if (StringUtils.isBlank(newTag)) {
      return;
    }

    synchronized (tvShowTagsObservable) {
      if (tvShowTagsObservable.contains(newTag)) {
        return;
      }
      tvShowTagsObservable.add(newTag);
    }

    firePropertyChange("tag", null, tvShowTagsObservable);
  }

  public List<String> getTagsInTvShows() {
    return tvShowTagsObservable;
  }

  private void updateEpisodeTags(TvShowEpisode episode) {
    List<String> availableTags = new ArrayList<>(episodeTagsObservable);
    for (String tagEpisode : new ArrayList<>(episode.getTags())) {
      boolean tagFound = false;
      for (String tag : availableTags) {
        if (tagEpisode.equals(tag)) {
          tagFound = true;
          break;
        }
      }
      if (!tagFound) {
        addEpisodeTag(tagEpisode);
      }
    }
  }

  private void addEpisodeTag(String newTag) {
    if (StringUtils.isBlank(newTag)) {
      return;
    }

    synchronized (episodeTagsObservable) {
      if (episodeTagsObservable.contains(newTag)) {
        return;
      }
      episodeTagsObservable.add(newTag);
    }

    firePropertyChange("tag", null, episodeTagsObservable);
  }

  public List<String> getTagsInEpisodes() {
    return episodeTagsObservable;
  }

  private void updateMediaInformationLists(TvShowEpisode episode) {
    // video codec
    List<String> availableCodecs = new ArrayList<>(videoCodecsObservable);
    for (MediaFile mf : episode.getMediaFiles(MediaFileType.VIDEO)) {
      String codec = mf.getVideoCodec();
      boolean codecFound = false;

      for (String mfCodec : availableCodecs) {
        if (mfCodec.equals(codec)) {
          codecFound = true;
          break;
        }
      }

      if (!codecFound) {
        addVideoCodec(codec);
      }
    }

    // audio codec
    availableCodecs = new ArrayList<>(audioCodecsObservable);
    for (MediaFile mf : episode.getMediaFiles(MediaFileType.VIDEO)) {
      for (MediaFileAudioStream audio : mf.getAudioStreams()) {
        String codec = audio.getCodec();
        boolean codecFound = false;
        for (String mfCodec : availableCodecs) {
          if (mfCodec.equals(codec)) {
            codecFound = true;
            break;
          }
        }

        if (!codecFound) {
          addAudioCodec(codec);
        }
      }
    }
  }

  private void addVideoCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    synchronized (videoCodecsObservable) {
      if (videoCodecsObservable.contains(newCodec)) {
        return;
      }
      videoCodecsObservable.add(newCodec);
    }

    firePropertyChange("videoCodec", null, videoCodecsObservable);
  }

  private void addAudioCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    synchronized (audioCodecsObservable) {
      if (audioCodecsObservable.contains(newCodec)) {
        return;
      }
      audioCodecsObservable.add(newCodec);
    }

    firePropertyChange("audioCodec", null, audioCodecsObservable);
  }

  public List<String> getVideoCodecsInEpisodes() {
    return videoCodecsObservable;
  }

  public List<String> getAudioCodecsInEpisodes() {
    return audioCodecsObservable;
  }

  /**
   * Gets the TV show by path.
   * 
   * @param path
   *          the path
   * @return the TV show by path
   * @deprecated use getTvShowByPath(Path path)
   */
  @Deprecated
  public TvShow getTvShowByPath(File path) {
    return getTvShowByPath(path.toPath());
  }

  /**
   * Gets the TV show by path.
   * 
   * @param path
   *          path
   * @return the TV show by path
   */
  public TvShow getTvShowByPath(Path path) {
    ArrayList<TvShow> tvShows = new ArrayList<>(tvShowList);
    // iterate over all tv shows and check whether this path is being owned by one
    for (TvShow tvShow : tvShows) {
      if (tvShow.getPathNIO().compareTo(path.toAbsolutePath()) == 0) {
        return tvShow;
      }
    }

    return null;
  }

  /**
   * Gets the episodes by file. Filter out all episodes from the Database which are part of this file
   * 
   * @param file
   *          the file
   * @return the tv episodes by file
   */
  public List<TvShowEpisode> getTvEpisodesByFile(TvShow tvShow, File file) {
    List<TvShowEpisode> episodes = new ArrayList<>(1);
    // validy check
    if (file == null) {
      return episodes;
    }

    // check if that file is in this tv show/episode (iterating thread safe)
    for (TvShowEpisode episode : new ArrayList<>(tvShow.getEpisodes())) {
      for (MediaFile mediaFile : new ArrayList<>(episode.getMediaFiles())) {
        if (file.equals(mediaFile.getFile())) {
          episodes.add(episode);
        }
      }
    }
    // for (int j = 0; j < tvShow.getEpisodes().size(); j++) {
    // TvShowEpisode episode = tvShow.getEpisodes().get(j);
    // for (int k = 0; k < episode.getMediaFiles().size(); k++) {
    // MediaFile mediaFile = episode.getMediaFiles().get(k);
    // if (file.equals(mediaFile.getFile())) {
    // episodes.add(episode);
    // }
    // }
    // }
    return episodes;
  }

  /**
   * invalidate the title sortable upon changes to the sortable prefixes
   */
  public void invalidateTitleSortable() {
    for (TvShow tvShow : new ArrayList<>(tvShowList)) {
      tvShow.clearTitleSortable();
    }
  }

  /**
   * Gets the new TvShows or TvShows with new episodes
   * 
   * @return the new TvShows
   */
  public List<TvShow> getNewTvShows() {
    List<TvShow> newShows = new ArrayList<>();
    for (TvShow show : tvShowList) {
      if (show.isNewlyAdded()) {
        newShows.add(show);
      }
    }
    return newShows;
  }

  /**
   * Gets the new episodes
   * 
   * @return the new episodes
   */
  public List<TvShowEpisode> getNewEpisodes() {
    List<TvShowEpisode> newEp = new ArrayList<>();
    for (TvShow show : tvShowList) {
      for (TvShowEpisode ep : show.getEpisodes()) {
        if (ep.isNewlyAdded()) {
          newEp.add(ep);
        }
      }
    }
    return newEp;
  }

  /**
   * Gets the unscraped episodes
   * 
   * @return the unscraped episodes
   */
  public List<TvShowEpisode> getUnscrapedEpisodes() {
    List<TvShowEpisode> newEp = new ArrayList<>();
    for (TvShow show : tvShowList) {
      for (TvShowEpisode ep : show.getEpisodes()) {
        if (!ep.isScraped()) {
          newEp.add(ep);
        }
      }
    }
    return newEp;
  }

  /**
   * check if there are movies without (at least) one VIDEO mf
   */
  private void checkAndCleanupMediaFiles() {
    boolean problemsDetected = false;
    for (TvShow tvShow : tvShowList) {
      for (TvShowEpisode episode : new ArrayList<>(tvShow.getEpisodes())) {
        List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
        if (mfs.isEmpty()) {
          tvShow.removeEpisode(episode);
          problemsDetected = true;
        }
      }
    }

    if (problemsDetected) {
      LOGGER.warn("episodes without VIDEOs detected");

      // and push a message
      // also delay it so that the UI has time to start up
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(15000);
          }
          catch (Exception ignored) {
          }
          Message message = new Message(MessageLevel.SEVERE, "tmm.tvshows", "message.database.corrupteddata");
          MessageManager.instance.pushMessage(message);
        }
      });
      thread.start();
    }
  }

  /**
   * all available subtitle scrapers.
   *
   * @return the subtitle scrapers
   */
  public List<MediaScraper> getAvailableSubtitleScrapers() {
    List<MediaScraper> availableScrapers = MediaScraper.getMediaScrapers(ScraperType.SUBTITLE);
    Collections.sort(availableScrapers, new TvShowMediaScraperComparator());
    return availableScrapers;
  }

  /**
   * get all default (specified via settings) subtitle scrapers
   *
   * @return the specified subtitle scrapers
   */
  public List<MediaScraper> getDefaultSubtitleScrapers() {
    return getSubtitleScrapers(MovieModuleManager.MOVIE_SETTINGS.getMovieSubtitleScrapers());
  }

  /**
   * get all specified subtitle scrapers.
   *
   * @param providerIds
   *          the scrapers
   * @return the subtitle scrapers
   */
  public List<MediaScraper> getSubtitleScrapers(List<String> providerIds) {
    List<MediaScraper> subtitleScrapers = new ArrayList<>();

    for (String providerId : providerIds) {
      if (StringUtils.isBlank(providerId)) {
        continue;
      }
      MediaScraper subtitleScraper = MediaScraper.getMediaScraperById(providerId, ScraperType.SUBTITLE);
      if (subtitleScraper != null) {
        subtitleScrapers.add(subtitleScraper);
      }
    }

    return subtitleScrapers;
  }

  private class TvShowMediaScraperComparator implements Comparator<MediaScraper> {
    @Override
    public int compare(MediaScraper o1, MediaScraper o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }
}
