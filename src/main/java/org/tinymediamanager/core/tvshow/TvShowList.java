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

import static org.tinymediamanager.core.Constants.ADDED_TV_SHOW;
import static org.tinymediamanager.core.Constants.AUDIO_CODEC;
import static org.tinymediamanager.core.Constants.EPISODE_COUNT;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.REMOVED_TV_SHOW;
import static org.tinymediamanager.core.Constants.TV_SHOWS;
import static org.tinymediamanager.core.Constants.TV_SHOW_COUNT;
import static org.tinymediamanager.core.Constants.VIDEO_CODEC;

import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.ObservableCopyOnWriteArrayList;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

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
  private final List<String>     videoContainersObservable;
  private final List<String>     audioCodecsObservable;
  private final List<Double>     frameRateObservable;

  private PropertyChangeListener propertyChangeListener;

  /**
   * Instantiates a new TvShowList.
   */
  private TvShowList() {
    // create the lists
    tvShowList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(TvShow.class));
    tvShowTagsObservable = new ObservableCopyOnWriteArrayList<>();
    episodeTagsObservable = new ObservableCopyOnWriteArrayList<>();
    videoCodecsObservable = new ObservableCopyOnWriteArrayList<>();
    videoContainersObservable = new ObservableCopyOnWriteArrayList<>();
    audioCodecsObservable = new ObservableCopyOnWriteArrayList<>();
    frameRateObservable = new ObservableCopyOnWriteArrayList<>();

    // the tag listener: its used to always have a full list of all tags used in tmm
    propertyChangeListener = evt -> {
      // listen to changes of tags
      if (Constants.TAG.equals(evt.getPropertyName()) && evt.getSource() instanceof TvShow) {
        TvShow tvShow = (TvShow) evt.getSource();
        updateTvShowTags(tvShow);
      }
      if (Constants.TAG.equals(evt.getPropertyName()) && evt.getSource() instanceof TvShowEpisode) {
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
    };
  }

  /**
   * Gets the single instance of TvShowList.
   * 
   * @return single instance of TvShowList
   */
  public synchronized static TvShowList getInstance() {
    if (TvShowList.instance == null) {
      TvShowList.instance = new TvShowList();
    }

    return TvShowList.instance;
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
   * get all specified trailer scrapers.
   *
   * @param providerIds
   *          the scrapers
   * @return the trailer providers
   */
  public List<MediaScraper> getTrailerScrapers(List<String> providerIds) {
    List<MediaScraper> trailerScrapers = new ArrayList<>();

    for (String providerId : providerIds) {
      if (StringUtils.isBlank(providerId)) {
        continue;
      }
      MediaScraper trailerScraper = MediaScraper.getMediaScraperById(providerId, ScraperType.TVSHOW_TRAILER);
      if (trailerScraper != null) {
        trailerScrapers.add(trailerScraper);
      }
    }

    return trailerScrapers;
  }

  /**
   * all available trailer scrapers.
   *
   * @return the trailer scrapers
   */
  public List<MediaScraper> getAvailableTrailerScrapers() {
    return MediaScraper.getMediaScrapers(ScraperType.TVSHOW_TRAILER);
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
      if (Paths.get(path).equals(Paths.get(tvShow.getDataSource()))) {
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
      LOGGER.error("problem removing TV show from DB: {}", e.getMessage());
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
      LOGGER.error("problem removing TV show from DB: {}", e.getMessage());
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
    for (TvShow tvShow : tvShowList) {
      count += tvShow.getEpisodeCount();
    }

    return count;
  }

  /**
   * Gets the dummy episode count
   *
   * @return the dummy episode count
   */
  public int getDummyEpisodeCount() {
    int count = 0;
    for (TvShow tvShow : tvShowList) {
      count += tvShow.getDummyEpisodeCount();
    }

    return count;
  }

  /**
   * are there any dummy episodes?
   * 
   * @return true/false
   */
  public boolean hasDummyEpisodes() {
    for (TvShow tvShow : tvShowList) {
      if (tvShow.getDummyEpisodeCount() > 0) {
        return true;
      }
    }
    return false;
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
      String json = "";
      try {
        json = tvShowMap.get(uuid);
        TvShow tvShow = tvShowObjectReader.readValue(json);
        tvShow.setDbId(uuid);

        // for performance reasons we add tv shows directly
        tvShowList.add(tvShow);
      }
      catch (Exception e) {
        LOGGER.warn("problem decoding TV show json string: {}", e.getMessage());
        LOGGER.info("dropping corrupt TV show: {}", json);
        tvShowMap.remove(uuid);
      }
    }
    LOGGER.info("found {} TV shows in database", tvShowList.size());
  }

  /**
   * Load episodes from database.
   */
  void loadEpisodesFromDatabase(MVMap<UUID, String> episodesMap, ObjectMapper objectMapper) {
    List<UUID> orphanedEpisodes = new ArrayList<>();

    // load all episodes from the database
    ObjectReader episodeObjectReader = objectMapper.readerFor(TvShowEpisode.class);
    int episodeCount = 0;

    for (UUID uuid : new ArrayList<>(episodesMap.keyList())) {
      String json = "";
      try {
        json = episodesMap.get(uuid);
        TvShowEpisode episode = episodeObjectReader.readValue(json);
        episode.setDbId(uuid);

        // sanity check: only episodes with a video file are valid
        if (episode.getMediaFiles(MediaFileType.VIDEO).isEmpty()) {
          // no video file? drop it
          LOGGER.info("episode \"S{}E{}\" without video file - dropping", episode.getSeason(), episode.getEpisode());
          episodesMap.remove(uuid);
        }

        // check for orphaned episodes
        boolean found = false;

        // and assign it the the right TV show
        for (TvShow tvShow : tvShowList) {
          if (tvShow.getDbId().equals(episode.getTvShowDbId())) {
            episodeCount++;
            episode.setTvShow(tvShow);
            tvShow.addEpisode(episode);
            found = true;
            break;
          }
        }

        if (!found) {
          orphanedEpisodes.add(uuid);
        }
      }
      catch (Exception e) {
        LOGGER.warn("problem decoding episode json string: {}", e.getMessage());
        LOGGER.info("dropping corrupt episode: {}", json);
        episodesMap.remove(uuid);
      }
    }

    // remove orphaned episodes
    for (UUID uuid : orphanedEpisodes) {
      episodesMap.remove(uuid);
    }

    LOGGER.info("found {} episodes in database", episodeCount);
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
      LOGGER.error("failed to persist episode: {}  {}", tvShow.getTitle(), e.getMessage());
    }
  }

  public void removeTvShowFromDb(TvShow tvShow) {
    // delete this TV show from the database
    try {
      TvShowModuleManager.getInstance().removeTvShowFromDb(tvShow);
    }
    catch (Exception e) {
      LOGGER.error("failed to remove episode: {} - {}", tvShow.getTitle(), e.getMessage());
    }
  }

  public void persistEpisode(TvShowEpisode episode) {
    // update/insert this episode to the database
    try {
      TvShowModuleManager.getInstance().persistEpisode(episode);
    }
    catch (Exception e) {
      LOGGER.error("failed to persist episode: {} - S{}E{} - {} : {}", episode.getTvShow().getTitle(), episode.getSeason(), episode.getEpisode(),
          episode.getTitle(), e.getMessage());
    }
  }

  public void removeEpisodeFromDb(TvShowEpisode episode) {
    // delete this episode from the database
    try {
      TvShowModuleManager.getInstance().removeEpisodeFromDb(episode);
    }
    catch (Exception e) {
      LOGGER.error("failed to remove episode: {} - S{}E{} - {} : {}", episode.getTvShow().getTitle(), episode.getSeason(), episode.getEpisode(),
          episode.getTitle(), e.getMessage());
    }
  }

  public MediaScraper getDefaultMediaScraper() {
    MediaScraper scraper = MediaScraper.getMediaScraperById(TvShowModuleManager.SETTINGS.getScraper(), ScraperType.TV_SHOW);
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
    availableScrapers.sort(new TvShowMediaScraperComparator());
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
    availableScrapers.sort(new TvShowMediaScraperComparator());
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
    return getArtworkScrapers(TvShowModuleManager.SETTINGS.getArtworkScrapers());
  }

  /**
   * Search tv show with the default language.
   * 
   * @param searchTerm
   *          the search term
   * @param year
   *          the year of the movie (if available, otherwise <= 0)
   * @param ids
   *          a map of all available ids of the movie or null if no id based search is requested
   * @param mediaScraper
   *          the media scraper
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, int year, Map<String, Object> ids, MediaScraper mediaScraper) {
    return searchTvShow(searchTerm, year, ids, mediaScraper, TvShowModuleManager.SETTINGS.getScraperLanguage());
  }

  /**
   * Search tv show with the chosen language.
   * 
   * @param searchTerm
   *          the search term
   * @param year
   *          the year of the movie (if available, otherwise <= 0)
   * @param ids
   *          a map of all available ids of the movie or null if no id based search is requested
   * @param mediaScraper
   *          the media scraper
   * @param language
   *          the language to search with
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, int year, Map<String, Object> ids, MediaScraper mediaScraper,
      MediaLanguages language) {
    Set<MediaSearchResult> results = new TreeSet<>();
    try {
      ITvShowMetadataProvider provider;

      if (mediaScraper == null) {
        provider = (ITvShowMetadataProvider) getDefaultMediaScraper().getMediaProvider();
      }
      else {
        provider = (ITvShowMetadataProvider) mediaScraper.getMediaProvider();
      }

      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.setSearchQuery(searchTerm);
      options.setLanguage(language);

      if (ids != null) {
        options.setIds(ids);
      }

      if (!searchTerm.isEmpty()) {
        if (Utils.isValidImdbId(searchTerm)) {
          options.setImdbId(searchTerm);
        }
        options.setSearchQuery(searchTerm);
      }

      if (year > 0) {
        options.setSearchYear(year);
      }

      LOGGER.info("=====================================================");
      LOGGER.info("Searching with scraper: {}", provider.getProviderInfo().getId());
      LOGGER.info(options.toString());
      LOGGER.info("=====================================================");
      results.addAll(provider.search(options));

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
    catch (ScrapeException e) {
      LOGGER.error("searchTvShow", e);
      MessageManager.instance
          .pushMessage(new Message(MessageLevel.ERROR, this, "message.tvshow.searcherror", new String[] { ":", e.getLocalizedMessage() }));
    }

    return new ArrayList<>(results);
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

    firePropertyChange(Constants.TAG, null, tvShowTagsObservable);
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

    firePropertyChange(Constants.TAG, null, episodeTagsObservable);
  }

  public List<String> getTagsInEpisodes() {
    return episodeTagsObservable;
  }

  private void updateMediaInformationLists(TvShowEpisode episode) {
    for (MediaFile mf : episode.getMediaFiles(MediaFileType.VIDEO)) {
      // video codec
      String codec = mf.getVideoCodec();
      if (StringUtils.isNotBlank(codec) && !videoCodecsObservable.contains(codec)) {
        addVideoCodec(codec);
      }

      // frame rate
      Double frameRate = mf.getFrameRate();
      if (frameRate > 0 && !frameRateObservable.contains(frameRate)) {
        addFrameRate(frameRate);
      }

      // video container
      String container = mf.getContainerFormat();
      if (StringUtils.isNotBlank(container) && !videoContainersObservable.contains(container.toLowerCase(Locale.ROOT))) {
        addVideoContainer(container.toLowerCase(Locale.ROOT));
      }

      // audio codec
      for (MediaFileAudioStream audio : mf.getAudioStreams()) {
        String audioCodec = audio.getCodec();
        if (StringUtils.isNotBlank(audioCodec) && !audioCodecsObservable.contains(audioCodec)) {
          addAudioCodec(audioCodec);
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

    firePropertyChange(VIDEO_CODEC, null, videoCodecsObservable);
  }

  private void addVideoContainer(String newContainer) {
    if (StringUtils.isBlank(newContainer)) {
      return;
    }

    synchronized (videoContainersObservable) {
      if (videoContainersObservable.contains(newContainer)) {
        return;
      }
      videoContainersObservable.add(newContainer);
    }

    firePropertyChange(Constants.VIDEO_CONTAINER, null, videoContainersObservable);
  }

  private void addFrameRate(Double newFrameRate) {
    if (newFrameRate == 0) {
      return;
    }

    synchronized (frameRateObservable) {
      if (frameRateObservable.contains(newFrameRate)) {
        return;
      }
      frameRateObservable.add(newFrameRate);
    }

    firePropertyChange(Constants.FRAME_RATE, null, frameRateObservable);
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

    firePropertyChange(AUDIO_CODEC, null, audioCodecsObservable);
  }

  public List<String> getVideoCodecsInEpisodes() {
    return videoCodecsObservable;
  }

  public List<String> getVideoContainersInEpisodes() {
    return videoContainersObservable;
  }

  public List<Double> getFrameRatesInEpisodes() {
    return frameRateObservable;
  }

  public List<String> getAudioCodecsInEpisodes() {
    return audioCodecsObservable;
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
  public static List<TvShowEpisode> getTvEpisodesByFile(TvShow tvShow, Path file) {
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

    return episodes;
  }

  /**
   * invalidate the title sortable upon changes to the sortable prefixes
   */
  public void invalidateTitleSortable() {
    for (TvShow tvShow : new ArrayList<>(tvShowList)) {
      tvShow.clearTitleSortable();
      for (TvShowEpisode episode : tvShow.getEpisodes()) {
        episode.clearTitleSortable();
      }
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
   * check if there are episodes without (at least) one VIDEO mf
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
      Thread thread = new Thread(() -> {
        try {
          Thread.sleep(15000);
        }
        catch (Exception ignored) {
        }
        Message message = new Message(MessageLevel.SEVERE, "tmm.tvshows", "message.database.corrupteddata");
        MessageManager.instance.pushMessage(message);
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
    availableScrapers.sort(new TvShowMediaScraperComparator());
    return availableScrapers;
  }

  /**
   * get all default (specified via settings) subtitle scrapers
   *
   * @return the specified subtitle scrapers
   */
  public List<MediaScraper> getDefaultSubtitleScrapers() {
    return getSubtitleScrapers(TvShowModuleManager.SETTINGS.getSubtitleScrapers());
  }

  /**
   * get all default (specified via settings) trailer scrapers
   *
   * @return the specified trailer scrapers
   */
  public List<MediaScraper> getDefaultTrailerScrapers() {
    return getTrailerScrapers(TvShowModuleManager.SETTINGS.getTrailerScrapers());
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

  /**
   * search all episodes of all TV shows for duplicates (duplicate S/E)
   */
  public void searchDuplicateEpisodes() {
    for (TvShow tvShow : getTvShows()) {
      Map<String, TvShowEpisode> episodeMap = new HashMap<>();

      for (TvShowEpisode episode : tvShow.getEpisodes()) {
        String se = "S" + episode.getSeason() + "E" + episode.getEpisode();

        TvShowEpisode duplicate = episodeMap.get(se);
        if (duplicate != null) {
          duplicate.setDuplicate();
          episode.setDuplicate();
        }
        else {
          episodeMap.put(se, episode);
        }
      }
    }
  }

  private class TvShowMediaScraperComparator implements Comparator<MediaScraper> {
    @Override
    public int compare(MediaScraper o1, MediaScraper o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }
}
