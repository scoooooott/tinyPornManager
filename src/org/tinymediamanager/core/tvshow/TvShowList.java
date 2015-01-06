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
package org.tinymediamanager.core.tvshow;

import static org.tinymediamanager.core.Constants.*;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.anidb.AniDBMetadataProvider;
import org.tinymediamanager.scraper.fanarttv.FanartTvMetadataProvider;
import org.tinymediamanager.scraper.thetvdb.TheTvDbMetadataProvider;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class TvShowList.
 * 
 * @author Manuel Laggner
 */
public class TvShowList extends AbstractModelObject {
  private static final Logger         LOGGER                = LoggerFactory.getLogger(TvShowList.class);
  private static final ResourceBundle BUNDLE                = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static TvShowList           instance              = null;

  private List<TvShow>                tvShowList            = ObservableCollections.observableList(Collections
                                                                .synchronizedList(new ArrayList<TvShow>()));
  private List<String>                tvShowTagsObservable  = ObservableCollections.observableList(Collections
                                                                .synchronizedList(new ArrayList<String>()));
  private List<String>                episodeTagsObservable = ObservableCollections.observableList(Collections
                                                                .synchronizedList(new ArrayList<String>()));
  private List<String>                videoCodecsObservable = ObservableCollections.observableList(Collections
                                                                .synchronizedList(new ArrayList<String>()));
  private List<String>                audioCodecsObservable = ObservableCollections.observableList(Collections
                                                                .synchronizedList(new ArrayList<String>()));

  private PropertyChangeListener      propertyChangeListener;

  /**
   * Instantiates a new TvShowList.
   */
  private TvShowList() {
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

    boolean newTransaction = false;
    if (!TvShowModuleManager.getInstance().getEntityManager().getTransaction().isActive()) {
      TvShowModuleManager.getInstance().getEntityManager().getTransaction().begin();
      newTransaction = true;
    }

    TvShowModuleManager.getInstance().getEntityManager().remove(tvShow);

    if (newTransaction) {
      TvShowModuleManager.getInstance().getEntityManager().getTransaction().commit();
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

    boolean newTransaction = false;
    if (!TvShowModuleManager.getInstance().getEntityManager().getTransaction().isActive()) {
      TvShowModuleManager.getInstance().getEntityManager().getTransaction().begin();
      newTransaction = true;
    }

    TvShowModuleManager.getInstance().getEntityManager().remove(tvShow);

    if (newTransaction) {
      TvShowModuleManager.getInstance().getEntityManager().getTransaction().commit();
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

  /**
   * Load tv shows from database.
   */
  public void loadTvShowsFromDatabase(EntityManager entityManager) {
    List<TvShow> tvShows = null;
    try {
      // load tv shows
      TypedQuery<TvShow> query = entityManager.createQuery("SELECT tvShow FROM TvShow tvShow", TvShow.class);
      tvShows = query.getResultList();
      if (tvShows != null) {
        LOGGER.info("found " + tvShows.size() + " tv shows in database");
        for (Object obj : tvShows) {
          if (obj instanceof TvShow) {
            TvShow tvShow = (TvShow) obj;
            tvShow.initializeAfterLoading();
            for (TvShowEpisode episode : tvShow.getEpisodes()) {
              episode.initializeAfterLoading();
              updateEpisodeTags(episode);
              updateMediaInformationLists(episode);
            }

            // for performance reasons we add tv shows directly
            tvShowList.add(tvShow);
            updateTvShowTags(tvShow);
            tvShow.addPropertyChangeListener(propertyChangeListener);
          }
          else {
            LOGGER.error("retrieved no tv show: " + obj);
          }
        }

        // check for corrupted media entities
        checkAndCleanupMediaFiles();
      }
      else {
        LOGGER.debug("found no movies in database");
      }
    }
    catch (Exception e) {
      LOGGER.error("loadTvShowsFromDatabase", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "", "message.database.loadtvshows"));
    }
  }

  /**
   * Gets the metadata provider.
   * 
   * @return the metadata provider
   */
  public ITvShowMetadataProvider getMetadataProvider() {
    TvShowScrapers scraper = Globals.settings.getTvShowSettings().getTvShowScraper();
    return getMetadataProvider(scraper);
  }

  /**
   * Gets the metadata provider.
   * 
   * @param scraper
   *          the scraper
   * @return the metadata provider
   */
  public ITvShowMetadataProvider getMetadataProvider(TvShowScrapers scraper) {
    ITvShowMetadataProvider metadataProvider = null;
    switch (scraper) {
      case ANIDB:
        LOGGER.debug("get instance of AniDbMetadataProvider");
        metadataProvider = new AniDBMetadataProvider();
        break;
      case TVDB:
      default:
        LOGGER.debug("get instance of TheTvDbMetadataProvider");
        try {
          metadataProvider = new TheTvDbMetadataProvider();
        }
        catch (Exception e) {
          LOGGER.warn("failed to get instance of TheTvDbMetadataProvider", e);
        }
        break;

    }

    return metadataProvider;
  }

  /**
   * Gets the metadata provider from a searchresult's providerId.
   * 
   * @param providerId
   *          the scraper
   * @return the metadata provider
   */
  public ITvShowMetadataProvider getMetadataProvider(String providerId) {
    // FIXME: rework scrapers/providerInfo to contain Movie(Tv)Scrapers enums
    if (providerId == null || providerId.isEmpty()) {
      // default
      return getMetadataProvider(TvShowScrapers.TVDB);
    }
    if (providerId.equals(ANIDBID)) {
      return getMetadataProvider(TvShowScrapers.ANIDB);
    }
    else if (providerId.equals(TVDBID) || providerId.equals("tvdb")) {
      return getMetadataProvider(TvShowScrapers.TVDB);
    }
    else {
      // default
      return getMetadataProvider(TvShowScrapers.TVDB);
    }
  }

  /**
   * Gets the artwork provider.
   * 
   * @return the artwork provider
   */
  public List<IMediaArtworkProvider> getArtworkProviders() {
    List<TvShowArtworkScrapers> scrapers = new ArrayList<TvShowArtworkScrapers>();
    scrapers.add(TvShowArtworkScrapers.TVDB);
    scrapers.add(TvShowArtworkScrapers.ANIDB);
    scrapers.add(TvShowArtworkScrapers.FANART_TV);
    return getArtworkProviders(scrapers);
  }

  /**
   * Gets the artwork providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the artwork providers
   */
  public List<IMediaArtworkProvider> getArtworkProviders(List<TvShowArtworkScrapers> scrapers) {
    List<IMediaArtworkProvider> artworkProviders = new ArrayList<IMediaArtworkProvider>();

    IMediaArtworkProvider artworkProvider = null;

    // the tv db
    if (scrapers.contains(TvShowArtworkScrapers.TVDB)) {
      try {
        if (Globals.settings.getTvShowSettings().isImageScraperTvdb()) {
          LOGGER.debug("get instance of TheTvDbMetadataProvider");
          artworkProvider = new TheTvDbMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TheTvDbMetadataProvider", e);
      }
    }

    // anidb
    if (scrapers.contains(TvShowArtworkScrapers.ANIDB)) {
      artworkProviders.add(new AniDBMetadataProvider());
    }

    // fanart.tv
    if (scrapers.contains(TvShowArtworkScrapers.FANART_TV)) {
      try {
        if (Globals.settings.getTvShowSettings().isImageScraperFanartTv()) {
          LOGGER.debug("get instance of FanartTvMetadataProvider");
          artworkProvider = new FanartTvMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of FanartTvMetadataProvider", e);
      }
    }

    return artworkProviders;
  }

  /**
   * Search tv show with the default language.
   * 
   * @param searchTerm
   *          the search term
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, ITvShowMetadataProvider metadataProvider) {
    return searchTvShow(searchTerm, metadataProvider, Globals.settings.getTvShowSettings().getScraperLanguage());
  }

  /**
   * Search tv show with the chosen language.
   * 
   * @param searchTerm
   *          the search term
   * @param metadataProvider
   *          the metadata provider
   * @param language
   *          the language to search with
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, ITvShowMetadataProvider metadataProvider, MediaLanguages language) {
    // format searchstring
    // searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    List<MediaSearchResult> searchResult = null;
    try {
      ITvShowMetadataProvider provider = metadataProvider;
      // get a new metadataprovider if nothing is set
      if (provider == null) {
        provider = getMetadataProvider();
      }
      MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW, MediaSearchOptions.SearchParam.QUERY, searchTerm);
      options.set(SearchParam.LANGUAGE, language.name());
      options.set(SearchParam.COUNTRY, Globals.settings.getTvShowSettings().getCertificationCountry().getAlpha2());
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
      LOGGER.error("searchMovie", e);
    }

    return searchResult;
  }

  private void updateTvShowTags(TvShow tvShow) {
    for (String tagInTvShow : tvShow.getTags()) {
      boolean tagFound = false;
      for (String tag : tvShowTagsObservable) {
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
    for (String tag : tvShowTagsObservable) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    tvShowTagsObservable.add(newTag);
    firePropertyChange("tag", null, tvShowTagsObservable);
  }

  public List<String> getTagsInTvShows() {
    return tvShowTagsObservable;
  }

  private void updateEpisodeTags(TvShowEpisode episode) {
    for (String tagEpisode : episode.getTags()) {
      boolean tagFound = false;
      for (String tag : episodeTagsObservable) {
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
    for (String tag : episodeTagsObservable) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    episodeTagsObservable.add(newTag);
    firePropertyChange("tag", null, episodeTagsObservable);
  }

  public List<String> getTagsInEpisodes() {
    return episodeTagsObservable;
  }

  private void updateMediaInformationLists(TvShowEpisode episode) {
    // video codec
    for (MediaFile mf : episode.getMediaFiles(MediaFileType.VIDEO)) {
      String codec = mf.getVideoCodec();
      boolean codecFound = false;

      for (String mfCodec : videoCodecsObservable) {
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
    for (MediaFile mf : episode.getMediaFiles(MediaFileType.VIDEO)) {
      for (MediaFileAudioStream audio : mf.getAudioStreams()) {
        String codec = audio.getCodec();
        boolean codecFound = false;
        for (String mfCodec : audioCodecsObservable) {
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

    for (String codec : videoCodecsObservable) {
      if (codec.equals(newCodec)) {
        return;
      }
    }

    videoCodecsObservable.add(newCodec);
    firePropertyChange("videoCodec", null, videoCodecsObservable);
  }

  private void addAudioCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    for (String codec : audioCodecsObservable) {
      if (codec.equals(newCodec)) {
        return;
      }
    }

    audioCodecsObservable.add(newCodec);
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
   * @return the movie by path
   */
  public TvShow getTvShowByPath(File path) {
    ArrayList<TvShow> tvShows = new ArrayList<TvShow>(tvShowList);
    // iterate over all tv shows and check whether this path is being owned by one
    for (TvShow tvShow : tvShows) {
      if (new File(tvShow.getPath()).compareTo(path) == 0) {
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
    List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>(1);
    // validy check
    if (file == null) {
      return episodes;
    }

    // check if that file is in this tv show/episode (iterating thread safe)
    for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(tvShow.getEpisodes())) {
      for (MediaFile mediaFile : new ArrayList<MediaFile>(episode.getMediaFiles())) {
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
    for (TvShow tvShow : new ArrayList<TvShow>(tvShowList)) {
      tvShow.clearTitleSortable();
    }
  }

  /**
   * Gets the new TvShows or TvShows with new episodes
   * 
   * @return the new TvShows
   */
  public List<TvShow> getNewTvShows() {
    List<TvShow> newShows = new ArrayList<TvShow>();
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
    List<TvShowEpisode> newEp = new ArrayList<TvShowEpisode>();
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
   * check if there are movies without (at least) one VIDEO mf
   */
  private void checkAndCleanupMediaFiles() {
    boolean problemsDetected = false;
    for (TvShow tvShow : tvShowList) {
      for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(tvShow.getEpisodes())) {
        List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
        if (mfs.isEmpty()) {
          tvShow.removeEpisode(episode);
          problemsDetected = true;
        }
      }
    }

    if (problemsDetected) {
      LOGGER.warn("episodes without VIDEOs detected");
      // since we have no active UI yet, push a popup message in an own window
      if (!GraphicsEnvironment.isHeadless()) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            JOptionPane.showMessageDialog(null, BUNDLE.getString("message.database.corrupteddata"));
          }
        });
      }
    }
  }
}
