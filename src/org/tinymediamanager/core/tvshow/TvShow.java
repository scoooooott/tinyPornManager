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

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.MediaEntityImageFetcher;
import org.tinymediamanager.core.tvshow.connector.TvShowToXbmcNfoConnector;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;

/**
 * The Class TvShow.
 * 
 * @author Manuel Laggner
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public class TvShow extends MediaEntity {

  /** The Constant LOGGER. */
  private static final Logger LOGGER             = Logger.getLogger(TvShow.class);

  /** The episodes. */
  private List<TvShowEpisode> episodes           = new ArrayList<TvShowEpisode>();

  /** The movies observable. */
  @Transient
  private List<TvShowEpisode> episodesObservable = ObservableCollections.observableList(episodes);

  /** The seasons. */
  @Transient
  private List<TvShowSeason>  seasons            = ObservableCollections.observableList(new ArrayList<TvShowSeason>());

  /** The new genres based on an enum like class. */
  private List<String>        genres             = new ArrayList<String>();

  /** The genres2 for access. */
  @Transient
  private List<MediaGenres>   genresForAccess    = new ArrayList<MediaGenres>();

  /** The data source. */
  private String              dataSource         = "";

  /** The imdb id. */
  private String              imdbId             = "";

  /** The nfo filename. */
  private String              nfoFilename        = "";

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getFanart()
   */
  @Override
  public String getFanart() {
    if (!StringUtils.isEmpty(fanart)) {
      return path + File.separator + fanart;
    }
    else {
      return fanart;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getPoster()
   */
  @Override
  public String getPoster() {
    if (!StringUtils.isEmpty(poster)) {
      return path + File.separator + poster;
    }
    else {
      return poster;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setPoster(java.lang.String)
   */
  @Override
  public void setPoster(String newValue) {
    String oldValue = this.poster;
    this.poster = newValue;
    firePropertyChange(POSTER, oldValue, newValue);
    firePropertyChange(HAS_IMAGES, false, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setFanart(java.lang.String)
   */
  @Override
  public void setFanart(String newValue) {
    String oldValue = this.fanart;
    this.fanart = newValue;
    firePropertyChange(FANART, oldValue, newValue);
    firePropertyChange(HAS_IMAGES, false, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getBanner()
   */
  @Override
  public String getBanner() {
    if (!StringUtils.isEmpty(banner)) {
      return path + File.separator + banner;
    }
    else {
      return banner;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setBanner(java.lang.String)
   */
  @Override
  public void setBanner(String newValue) {
    String oldValue = this.banner;
    this.banner = newValue;
    firePropertyChange(BANNER, oldValue, newValue);
    firePropertyChange(HAS_IMAGES, false, true);
  }

  /**
   * Gets the episodes.
   * 
   * @return the episodes
   */
  public List<TvShowEpisode> getEpisodes() {
    return episodesObservable;
  }

  /**
   * Adds the episode.
   * 
   * @param episode
   *          the episode
   */
  public void addEpisode(TvShowEpisode episode) {
    episodesObservable.add(episode);
    addToSeason(episode);

    firePropertyChange(EPISODES, null, episodesObservable);
  }

  /**
   * Adds the to season.
   * 
   * @param episode
   *          the episode
   */
  private void addToSeason(TvShowEpisode episode) {
    TvShowSeason season = getSeasonForEpisode(episode);
    season.addEpisode(episode);
  }

  /**
   * Gets the season for episode.
   * 
   * @param episode
   *          the episode
   * @return the season for episode
   */
  public TvShowSeason getSeasonForEpisode(TvShowEpisode episode) {
    TvShowSeason season = null;

    // search for an existing season
    for (TvShowSeason s : seasons) {
      if (s.getSeason() == episode.getSeason()) {
        season = s;
        break;
      }
    }

    // no one found - create one
    if (season == null) {
      season = new TvShowSeason(episode.getSeason(), this);
      seasons.add(season);
      firePropertyChange(SEASONS, null, seasons);
    }

    return season;
  }

  /**
   * Initialize after loading.
   */
  public void initializeAfterLoading() {
    episodesObservable = ObservableCollections.observableList(episodes);

    // load genres
    for (String genre : genres) {
      addGenre(MediaGenres.getGenre(genre));
    }

    // create the seasons structure
    for (TvShowEpisode episode : episodes) {
      addToSeason(episode);
    }
  }

  /**
   * Gets the data source.
   * 
   * @return the data source
   */
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Sets the data source.
   * 
   * @param newValue
   *          the new data source
   */
  public void setDataSource(String newValue) {
    String oldValue = this.dataSource;
    this.dataSource = newValue;
    firePropertyChange(DATA_SOURCE, oldValue, newValue);
  }

  /**
   * remove all episodes from this tv show.
   */
  public void removeAllEpisodes() {
    if (episodesObservable.size() > 0) {
      Globals.entityManager.getTransaction().begin();
      for (int i = episodesObservable.size() - 1; i >= 0; i--) {
        TvShowEpisode episode = episodesObservable.get(i);
        episodesObservable.remove(episode);
        Globals.entityManager.remove(episode);
      }
      Globals.entityManager.getTransaction().commit();
    }
  }

  /**
   * Gets the seasons.
   * 
   * @return the seasons
   */
  public List<TvShowSeason> getSeasons() {
    return seasons;
  }

  /**
   * Gets the genres.
   * 
   * @return the genres
   */
  public List<MediaGenres> getGenres() {
    return genresForAccess;
  }

  /**
   * Adds the genre.
   * 
   * @param newValue
   *          the new value
   */
  public void addGenre(MediaGenres newValue) {
    if (!genresForAccess.contains(newValue)) {
      genresForAccess.add(newValue);
      if (!genres.contains(newValue.name())) {
        genres.add(newValue.name());
      }
      firePropertyChange(GENRE, null, newValue);
      firePropertyChange(GENRES_AS_STRING, null, newValue);
    }
  }

  /**
   * Sets the genres.
   * 
   * @param genres
   *          the new genres
   */
  public void setGenres(List<MediaGenres> genres) {
    // two way sync of genres

    // first, add new ones
    for (MediaGenres genre : genres) {
      if (!this.genresForAccess.contains(genre)) {
        this.genresForAccess.add(genre);
        if (!genres.contains(genre.name())) {
          this.genres.add(genre.name());
        }
      }
    }

    // second remove old ones
    for (int i = this.genresForAccess.size() - 1; i >= 0; i--) {
      MediaGenres genre = this.genresForAccess.get(i);
      if (!genres.contains(genre)) {
        this.genresForAccess.remove(genre);
        this.genres.remove(genre.name());
      }
    }

    firePropertyChange(GENRE, null, genres);
    firePropertyChange(GENRES_AS_STRING, null, genres);
  }

  /**
   * Removes the genre.
   * 
   * @param genre
   *          the genre
   */
  public void removeGenre(MediaGenres genre) {
    if (genresForAccess.contains(genre)) {
      genresForAccess.remove(genre);
      genres.remove(genre.name());
      firePropertyChange(GENRE, null, genre);
      firePropertyChange(GENRES_AS_STRING, null, genre);
    }
  }

  /**
   * Gets the genres as string.
   * 
   * @return the genres as string
   */
  public String getGenresAsString() {
    StringBuilder sb = new StringBuilder();
    for (MediaGenres genre : genresForAccess) {
      if (!StringUtils.isEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(genre != null ? genre.toString() : "null");
    }
    return sb.toString();
  }

  /**
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   */
  public void setMetadata(MediaMetadata metadata) {
    // check if metadata has at least a name
    if (StringUtils.isEmpty(metadata.getTitle())) {
      LOGGER.warn("wanted to save empty metadata for " + getTitle());
      return;
    }

    setImdbId(metadata.getImdbId());
    setTitle(metadata.getTitle());
    setRating((float) metadata.getRating());
    setPlot(metadata.getPlot());

    // set scraped
    setScraped(true);

    // write NFO
    writeNFO();

    // update DB
    saveToDb();
  }

  /**
   * Sets the artwork.
   * 
   * @param artwork
   *          the new artwork
   */
  public void setArtwork(List<MediaArtwork> artwork) {
    setArtwork(artwork, Globals.settings.getTvShowScraperMetadataConfig());
  }

  /**
   * Sets the artwork.
   * 
   * @param artwork
   *          the artwork
   * @param config
   *          the config
   */
  public void setArtwork(List<MediaArtwork> artwork, TvShowScraperMetadataConfig config) {
    if (config.isArtwork()) {
      // poster
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.POSTER) {
          // set url
          setPosterUrl(art.getDefaultUrl());
          // and download it
          writePosterImage();
          break;
        }
      }

      // fanart
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BACKGROUND) {
          // set url
          setFanartUrl(art.getDefaultUrl());
          // and download it
          writeFanartImage();
          break;
        }
      }

      // banner
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BANNER) {
          // set url
          setBannerUrl(art.getDefaultUrl());
          // and download it
          writeBannerImage();
          break;
        }
      }

      // update DB
      saveToDb();
    }
  }

  /**
   * Write poster image.
   */
  public void writePosterImage() {
    if (StringUtils.isNotEmpty(getPosterUrl())) {
      boolean firstImage = true;
      // create correct filename
      String filename = path + File.separator + "poster." + FilenameUtils.getExtension(getPosterUrl());
      // get image in thread
      MediaEntityImageFetcher task = new MediaEntityImageFetcher(this, getPosterUrl(), MediaArtworkType.POSTER, filename, firstImage);
      Globals.executor.execute(task);
    }
  }

  /**
   * Write fanart image.
   */
  public void writeFanartImage() {
    if (StringUtils.isNotEmpty(getFanartUrl())) {
      boolean firstImage = true;
      // create correct filename
      String filename = path + File.separator + "fanart." + FilenameUtils.getExtension(getFanartUrl());
      // get image in thread
      MediaEntityImageFetcher task = new MediaEntityImageFetcher(this, getFanartUrl(), MediaArtworkType.BACKGROUND, filename, firstImage);
      Globals.executor.execute(task);
    }
  }

  /**
   * Write banner image.
   */
  public void writeBannerImage() {
    if (StringUtils.isNotEmpty(getBannerUrl())) {
      boolean firstImage = true;
      // create correct filename
      String filename = path + File.separator + "banner." + FilenameUtils.getExtension(getBannerUrl());
      // get image in thread
      MediaEntityImageFetcher task = new MediaEntityImageFetcher(this, getBannerUrl(), MediaArtworkType.BANNER, filename, firstImage);
      Globals.executor.execute(task);
    }
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    setNfoFilename(TvShowToXbmcNfoConnector.setData(this));
  }

  /**
   * Sets the nfo filename.
   * 
   * @param newValue
   *          the new nfo filename
   */
  public void setNfoFilename(String newValue) {
    String oldValue = this.nfoFilename;
    this.nfoFilename = newValue;
    firePropertyChange(NFO_FILENAME, oldValue, newValue);
    firePropertyChange(HAS_NFO_FILE, false, true);
  }

  /**
   * Gets the nfo filename.
   * 
   * @return the nfo filename
   */
  public String getNfoFilename() {
    if (!StringUtils.isEmpty(nfoFilename)) {
      return path + File.separator + nfoFilename;
    }
    else {
      return nfoFilename;
    }
  }

  /**
   * Gets the checks for nfo file.
   * 
   * @return the checks for nfo file
   */
  public Boolean getHasNfoFile() {
    if (!StringUtils.isEmpty(nfoFilename)) {
      return true;
    }
    return false;
  }

  /**
   * Gets the imdb id.
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    return imdbId;
  }

  /**
   * Sets the imdb id.
   * 
   * @param newValue
   *          the new imdb id
   */
  public void setImdbId(String newValue) {
    String oldValue = imdbId;
    imdbId = newValue;
    firePropertyChange("imdbId", oldValue, newValue);
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
