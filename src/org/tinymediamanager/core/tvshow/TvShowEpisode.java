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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.MediaEntityImageFetcher;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

/**
 * The Class TvEpisode.
 * 
 * @author Manuel Laggner
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public class TvShowEpisode extends MediaEntity {

  /** The tv show. */
  private TvShow          tvShow               = null;

  /** The episode. */
  private int             episode              = 0;

  /** The season. */
  private int             season               = -1;

  /** The media files. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MediaFile> mediaFiles           = new ArrayList<MediaFile>();

  /** The media files observable. */
  @Transient
  private List<MediaFile> mediaFilesObservable = ObservableCollections.observableList(mediaFiles);

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
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setPoster(java.lang.String)
   */
  @Override
  public void setPoster(String poster) {
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
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setBanner(java.lang.String)
   */
  @Override
  public void setBanner(String banner) {
  }

  /**
   * Gets the tv show.
   * 
   * @return the tv show
   */
  public TvShow getTvShow() {
    return tvShow;
  }

  /**
   * Sets the tv show.
   * 
   * @param newValue
   *          the new tv show
   */
  public void setTvShow(TvShow newValue) {
    TvShow oldValue = this.tvShow;
    this.tvShow = newValue;
    firePropertyChange(TV_SHOW, oldValue, newValue);
  }

  /**
   * Gets the episode.
   * 
   * @return the episode
   */
  public int getEpisode() {
    return episode;
  }

  /**
   * Gets the season.
   * 
   * @return the season
   */
  public int getSeason() {
    return season;
  }

  /**
   * Sets the episode.
   * 
   * @param newValue
   *          the new episode
   */
  public void setEpisode(int newValue) {
    int oldValue = this.episode;
    this.episode = newValue;
    firePropertyChange(EPISODE, oldValue, newValue);
    firePropertyChange(TITLE_FOR_UI, "", newValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setTitle(java.lang.String)
   */
  @Override
  public void setTitle(String newValue) {
    super.setTitle(newValue);
    firePropertyChange(TITLE_FOR_UI, "", newValue);
  }

  /**
   * Sets the season.
   * 
   * @param newValue
   *          the new season
   */
  public void setSeason(int newValue) {
    int oldValue = this.season;
    this.season = newValue;
    firePropertyChange(SEASON, oldValue, newValue);
    firePropertyChange(TITLE_FOR_UI, "", newValue);
  }

  /**
   * Gets the title for ui.
   * 
   * @return the title for ui
   */
  public String getTitleForUi() {
    StringBuffer titleForUi = new StringBuffer();
    if (episode > 0 && season > 0) {
      titleForUi.append(String.format("S%02dE%02d - ", season, episode));
    }
    titleForUi.append(title);
    return titleForUi.toString();
  }

  /**
   * Initialize after loading.
   */
  public void initializeAfterLoading() {
    mediaFilesObservable = ObservableCollections.observableList(mediaFiles);
  }

  /**
   * Adds the to media files.
   * 
   * @param obj
   *          the obj
   */
  public void addToMediaFiles(MediaFile obj) {
    mediaFilesObservable.add(obj);
    firePropertyChange(MEDIA_FILES, null, mediaFilesObservable);
  }

  /**
   * Gets the media files.
   * 
   * @return the media files
   */
  public List<MediaFile> getMediaFiles() {
    return mediaFilesObservable;
  }

  /**
   * Removes the from media files.
   * 
   * @param obj
   *          the obj
   */
  public void removeFromMediaFiles(MediaFile obj) {
    mediaFilesObservable.remove(obj);
    firePropertyChange(MEDIA_FILES, null, mediaFilesObservable);
  }

  /**
   * Save to db.
   */
  public synchronized void saveToDb() {
    // update DB
    synchronized (Globals.entityManager) {
      Globals.entityManager.getTransaction().begin();
      Globals.entityManager.persist(this);
      Globals.entityManager.getTransaction().commit();
    }
  }

  /**
   * Write fanart image.
   */
  public void writeFanartImage() {
    if (StringUtils.isNotEmpty(getFanartUrl())) {
      boolean firstImage = true;
      // create correct filename
      MediaFile mf = getMediaFiles().get(0);
      String filename = path + File.separator + FilenameUtils.getBaseName(mf.getFilename()) + "-fanart." + FilenameUtils.getExtension(getFanartUrl());
      // get image in thread
      MediaEntityImageFetcher task = new MediaEntityImageFetcher(this, getFanartUrl(), MediaArtworkType.BACKGROUND, filename, firstImage);
      Globals.executor.execute(task);
    }
  }
}
