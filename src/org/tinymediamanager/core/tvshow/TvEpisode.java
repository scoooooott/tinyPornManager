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

import static org.tinymediamanager.core.Constants.EPISODE;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.SEASON;
import static org.tinymediamanager.core.Constants.TV_SHOW;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.MediaFile;

/**
 * The Class TvEpisode.
 * 
 * @author Manuel Laggner
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public class TvEpisode extends MediaEntity {

  private TvShow          tvShow               = null;
  private int             episode              = 0;
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
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getPoster()
   */
  @Override
  public String getPoster() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setPoster(java.lang.String)
   */
  @Override
  public void setPoster(String poster) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setFanart(java.lang.String)
   */
  @Override
  public void setFanart(String fanart) {
    // TODO Auto-generated method stub

  }

  public TvShow getTvShow() {
    return tvShow;
  }

  public void setTvShow(TvShow newValue) {
    TvShow oldValue = this.tvShow;
    this.tvShow = newValue;
    firePropertyChange(TV_SHOW, oldValue, newValue);
  }

  public int getEpisode() {
    return episode;
  }

  public int getSeason() {
    return season;
  }

  public void setEpisode(int newValue) {
    int oldValue = this.episode;
    this.episode = newValue;
    firePropertyChange(EPISODE, oldValue, newValue);
  }

  public void setSeason(int newValue) {
    int oldValue = this.season;
    this.season = newValue;
    firePropertyChange(SEASON, oldValue, newValue);
  }

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

}
