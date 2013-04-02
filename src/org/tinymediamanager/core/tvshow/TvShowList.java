/*
 * Copyright 2012-2013 Manuel Laggner
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.tvshow.EpisodeMatching.EpisodeMatchingResult;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.thetvdb.TheTvDbMetadataProvider;

/**
 * The Class TvShowList.
 * 
 * @author Manuel Laggner
 */
public class TvShowList extends AbstractModelObject {

  /** The Constant logger. */
  private static final Logger LOGGER     = Logger.getLogger(TvShowList.class);

  /** The instance. */
  private static TvShowList   instance   = null;

  private List<TvShow>        tvShowList = ObservableCollections.observableList(new ArrayList<TvShow>());

  /**
   * Instantiates a new TvShowList.
   */
  private TvShowList() {
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

  public List<TvShow> getTvShows() {
    return tvShowList;
  }

  public void addTvShow(TvShow newValue) {
    tvShowList.add(newValue);
    firePropertyChange(TV_SHOWS, null, tvShowList);
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
      if (path.equals(tvShow.getDataSource())) {
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
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.remove(tvShow);
    Globals.entityManager.getTransaction().commit();
    firePropertyChange(TV_SHOWS, null, tvShowList);
    firePropertyChange(TV_SHOW_COUNT, oldValue, tvShowList.size());
  }

  /**
   * Load tv shows from database.
   */
  public void loadTvShowsFromDatabase() {
    List<TvShow> tvShows = null;
    try {
      // load tv shows
      TypedQuery<TvShow> query = Globals.entityManager.createQuery("SELECT tvShow FROM TvShow tvShow", TvShow.class);
      tvShows = query.getResultList();
      if (tvShows != null) {
        LOGGER.debug("found " + tvShows.size() + " tv shows in database");
        for (Object obj : tvShows) {
          if (obj instanceof TvShow) {
            TvShow tvShow = (TvShow) obj;
            tvShow.initializeAfterLoading();

            // for performance reasons we add tv shows directly
            tvShowList.add(tvShow);
          }
          else {
            LOGGER.error("retrieved no tv show: " + obj);
          }
        }

      }
      else {
        LOGGER.debug("found no movies in database");
      }
    }
    catch (PersistenceException e) {
      LOGGER.error("loadTvShowsFromDatabase", e);
    }
    catch (Exception e) {
      LOGGER.error("loadTvShowsFromDatabase", e);
    }
  }

  /**
   * Gets the metadata provider.
   * 
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider() {
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
  public IMediaMetadataProvider getMetadataProvider(TvShowScrapers scraper) {
    IMediaMetadataProvider metadataProvider = null;
    switch (scraper) {
      case TVDB:
      default:
        LOGGER.debug("get instance of TheTvDbMetadataProvider");
        metadataProvider = new TheTvDbMetadataProvider();
        break;

    }

    return metadataProvider;
  }

  /**
   * Gets the artwork provider.
   * 
   * @return the artwork provider
   */
  public List<IMediaArtworkProvider> getArtworkProviders() {
    List<TvShowArtworkScrapers> scrapers = new ArrayList<TvShowArtworkScrapers>();
    scrapers.add(TvShowArtworkScrapers.TVDB);

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
        LOGGER.debug("get instance of TheTvDbMetadataProvider");
        artworkProvider = new TheTvDbMetadataProvider();
        artworkProviders.add(artworkProvider);

      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TheTvDbMetadataProvider", e);
      }
    }

    return artworkProviders;
  }

  /**
   * Search tv show.
   * 
   * @param searchTerm
   *          the search term
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  public List<MediaSearchResult> searchTvShow(String searchTerm, IMediaMetadataProvider metadataProvider) {
    // format searchstring
    searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    List<MediaSearchResult> searchResult = null;
    try {
      IMediaMetadataProvider provider = metadataProvider;
      // get a new metadataprovider if nothing is set
      if (provider == null) {
        provider = getMetadataProvider();
      }
      searchResult = provider.search(new MediaSearchOptions(MediaType.TV_SHOW, MediaSearchOptions.SearchParam.QUERY, searchTerm));
    }
    catch (Exception e) {
      LOGGER.error("searchMovie", e);
    }

    return searchResult;
  }

  /************************************************************************
   * 
   ************************************************************************/
  public void udpateDatasources() {
    for (String datasource : Globals.settings.getTvShowSettings().getTvShowDataSource()) {
      findTvShowsInPath(datasource);
    }
  }

  public void findTvShowsInPath(String path) {
    LOGGER.debug("find tv shows in path " + path);
    File filePath = new File(path);
    for (File subdir : filePath.listFiles()) {
      // // cancel task
      // if (cancel) {
      // return;
      // }

      if (subdir.isDirectory()) {
        // get the TV show from this subdir
        findTvShowInDirectory(subdir);
      }
    }
  }

  private void findTvShowInDirectory(File dir) {
    // search for this tvshow folder in database
    TvShow tvShow = getTvShowByPath(dir.getPath());
    if (tvShow == null) {
      // create new one
      tvShow = new TvShow();
      tvShow.setPath(dir.getPath());
      tvShow.setTitle(dir.getName());
      tvShow.saveToDb();
      addTvShow(tvShow);
    }

    // find episodes in this tv show folder
    if (tvShow != null) {
      findTvEpisodes(tvShow, dir);
    }
  }

  private void findTvEpisodes(TvShow tvShow, File dir) {
    LOGGER.debug("parsing " + dir.getPath());
    // crawl this folder and try to find every episode in it
    File[] content = dir.listFiles();
    for (File file : content) {
      if (file.isFile()) {
        // check filetype
        if (!Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()))) {
          continue;
        }

        TvShowEpisode episode = getTvEpisodeByFile(file);
        if (episode == null) {
          // try to check what episode//season
          EpisodeMatchingResult result = EpisodeMatching.detectEpisode(file);
          if (result.episodes.size() > 0) {
            // // episode(s) found; check if there was also a season found
            // int season = 0;
            // if (result.season == 0) {
            // // try to get the result from the parent folder
            // Pattern pattern = Pattern.compile("{1,2}[0-9]$");
            // Matcher matcher = pattern.matcher(dir.getPath());
            // if (matcher.find()) {
            // season = Integer.parseInt(matcher.group());
            // }
            // }

            // add it
            for (int ep : result.episodes) {
              episode = new TvShowEpisode();
              episode.setEpisode(ep);
              episode.setSeason(result.season);
              episode.setTvShow(tvShow);
              episode.addToMediaFiles(new MediaFile(file.getPath(), file.getName()));
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
        }
      }
      if (file.isDirectory()) {
        // dig deeper
        findTvEpisodes(tvShow, file);
      }
    }
  }

  /**
   * Gets the movie by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   */
  public synchronized TvShow getTvShowByPath(String path) {
    // iterate over all tv shows and check whether this path is being owned by one
    for (TvShow tvShow : getTvShows()) {
      if (tvShow.getPath().compareTo(path) == 0) {
        return tvShow;
      }
    }

    return null;
  }

  public synchronized TvShowEpisode getTvEpisodeByFile(File file) {
    // validy check
    if (file == null || !file.exists()) {
      return null;
    }

    // check if that file is in any tv show/episode
    for (TvShow show : getTvShows()) {
      for (TvShowEpisode episode : show.getEpisodes()) {
        for (MediaFile mediaFile : episode.getMediaFiles()) {
          if (file.equals(mediaFile.getFile())) {
            return episode;
          }
        }
      }
    }
    return null;
  }
}
