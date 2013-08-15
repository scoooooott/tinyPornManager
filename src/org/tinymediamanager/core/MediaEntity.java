/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.core;

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

/**
 * The Class MediaEntity.
 * 
 * @author Manuel Laggner
 */
@MappedSuperclass
public abstract class MediaEntity extends AbstractModelObject {

  /** The id for the database. */
  @GeneratedValue
  protected int                                id;

  /** The ids to store the ID from several metadataproviders. */
  protected HashMap<String, Object>            ids                  = new HashMap<String, Object>();

  protected String                             title                = "";
  protected String                             originalTitle        = "";
  protected String                             year                 = "";
  protected String                             plot                 = "";
  protected float                              rating               = 0f;
  protected String                             path                 = "";
  protected String                             fanartUrl            = "";
  protected String                             posterUrl            = "";
  protected String                             bannerUrl            = "";
  protected String                             thumbUrl             = "";
  protected Date                               dateAdded            = new Date();
  protected String                             productionCompany    = "";
  protected boolean                            scraped              = false;

  @Transient
  protected boolean                            duplicate            = false;

  @OneToMany(cascade = CascadeType.ALL)
  protected List<MediaFile>                    mediaFiles           = new ArrayList<MediaFile>();

  @Transient
  protected volatile ObservableList<MediaFile> mediaFilesObservable = ObservableCollections.observableList(mediaFiles);

  /**
   * Initialize after loading from database.
   */
  public void initializeAfterLoading() {
    mediaFilesObservable = ObservableCollections.observableList(mediaFiles);
  }

  /**
   * get the INTERNAL ID of this object. Do not confuse it with the IDs from the metadata provider!
   * 
   * @return internal id
   */
  public int getId() {
    return id;
  }

  public HashMap<String, Object> getIds() {
    return ids;
  }

  public String getFanartUrl() {
    return fanartUrl;
  }

  public String getFanart() {
    List<MediaFile> fanarts = getMediaFiles(MediaFileType.FANART);
    if (fanarts.size() > 0) {
      return fanarts.get(0).getFile().getPath();
    }
    return "";
  }

  public String getTitle() {
    return title;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public String getPlot() {
    return plot;
  }

  public String getPath() {
    return path;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public String getPoster() {
    List<MediaFile> poster = getMediaFiles(MediaFileType.POSTER);
    if (poster.size() > 0) {
      return poster.get(0).getFile().getPath();
    }
    return "";
  }

  public String getBannerUrl() {
    return bannerUrl;
  }

  public String getBanner() {
    List<MediaFile> banner = getMediaFiles(MediaFileType.BANNER);
    if (banner.size() > 0) {
      return banner.get(0).getFile().getPath();
    }
    return "";
  }

  public String getThumb() {
    List<MediaFile> thumbs = getMediaFiles(MediaFileType.THUMB);
    if (thumbs.size() > 0) {
      return thumbs.get(0).getFile().getPath();
    }
    return "";
  }

  public float getRating() {
    return rating;
  }

  public String getYear() {
    return year;
  }

  public void setIds(HashMap<String, Object> ids) {
    for (Entry<String, Object> entry : ids.entrySet()) {
      setId((String) entry.getKey(), entry.getValue().toString());
      firePropertyChange(entry.getKey(), null, entry.getValue());
    }
  }

  public void setTitle(String newValue) {
    String oldValue = title;
    title = newValue.trim();
    firePropertyChange(TITLE, oldValue, newValue);
  }

  public void setOriginalTitle(String newValue) {
    String oldValue = originalTitle;
    originalTitle = newValue.trim();
    firePropertyChange(ORIGINAL_TITLE, oldValue, newValue);
  }

  public void setPlot(String newValue) {
    String oldValue = plot;
    plot = newValue.trim();
    firePropertyChange(PLOT, oldValue, newValue);
  }

  public void setPath(String newValue) {
    String oldValue = path;
    path = newValue;
    firePropertyChange(PATH, oldValue, newValue);
  }

  public void setRating(float newValue) {
    float oldValue = rating;
    rating = newValue;
    firePropertyChange(RATING, oldValue, newValue);
  }

  public void setYear(String newValue) {
    String oldValue = year;
    year = newValue.trim();
    firePropertyChange(YEAR, oldValue, newValue);
  }

  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange(POSTER_URL, oldValue, newValue);
  }

  public void setPoster(File poster) {
    setImage(poster, MediaFileType.POSTER);
  }

  public void clearPoster() {
    removeAllMediaFiles(MediaFileType.POSTER);
  }

  public void setBannerUrl(String newValue) {
    String oldValue = bannerUrl;
    bannerUrl = newValue;
    firePropertyChange(BANNER_URL, oldValue, newValue);
  }

  public void setBanner(File banner) {
    setImage(banner, MediaFileType.BANNER);
  }

  public void clearBanner() {
    removeAllMediaFiles(MediaFileType.BANNER);
  }

  public void setThumb(File thumb) {
    setImage(thumb, MediaFileType.THUMB);
  }

  public void clearThumb() {
    removeAllMediaFiles(MediaFileType.THUMB);
  }

  public void setFanartUrl(String newValue) {
    String oldValue = fanartUrl;
    fanartUrl = newValue;
    firePropertyChange(FANART_URL, oldValue, newValue);
  }

  public void setThumbUrl(String newValue) {
    String oldValue = thumbUrl;
    thumbUrl = newValue;
    firePropertyChange(THUMB_URL, oldValue, newValue);
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setFanart(File fanart) {
    setImage(fanart, MediaFileType.FANART);
  }

  public void clearFanart() {
    removeAllMediaFiles(MediaFileType.FANART);
  }

  private void setImage(File file, MediaFileType type) {
    List<MediaFile> images = getMediaFiles(type);
    MediaFile mediaFile = null;
    if (images.size() > 0) {
      mediaFile = images.get(0);
      mediaFile.setFile(file);
      mediaFile.gatherMediaInformation(true);
    }
    else {
      mediaFile = new MediaFile(file, type);
      mediaFile.gatherMediaInformation();
      addToMediaFiles(mediaFile);
    }

    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  public Date getDateAdded() {
    return dateAdded;
  }

  public String getDateAddedAsString() {
    if (dateAdded == null) {
      return "";
    }

    return SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()).format(dateAdded);
  }

  public void setDateAdded(Date newValue) {
    Date oldValue = this.dateAdded;
    this.dateAdded = newValue;
    firePropertyChange(DATE_ADDED, oldValue, newValue);
    firePropertyChange(DATE_ADDED_AS_STRING, oldValue, newValue);
  }

  public String getProductionCompany() {
    return productionCompany;
  }

  public void setProductionCompany(String newValue) {
    String oldValue = this.productionCompany;
    this.productionCompany = newValue;
    firePropertyChange(PRODUCTION_COMPANY, oldValue, newValue);
  }

  protected void setScraped(boolean newValue) {
    this.scraped = newValue;
    firePropertyChange(SCRAPED, false, newValue);
  }

  public boolean isScraped() {
    return scraped;
  }

  public void setDuplicate() {
    this.duplicate = true;
  }

  public void clearDuplicate() {
    this.duplicate = false;
  }

  public boolean isDuplicate() {
    return this.duplicate;
  }

  public void setId(String key, Object value) {
    ids.put(key, value);
    firePropertyChange(key, null, value);
  }

  public Object getId(String key) {
    return ids.get(key);
  }

  public void addToMediaFiles(MediaFile mediaFile) {
    synchronized (mediaFilesObservable) {
      if (!mediaFilesObservable.contains(mediaFile)) {
        mediaFilesObservable.add(mediaFile);
        Collections.sort(mediaFilesObservable);
      }
    }

    firePropertyChange(MEDIA_FILES, null, mediaFilesObservable);
    fireAddedEventForMediaFile(mediaFile);
  }

  public void addToMediaFiles(List<MediaFile> mediaFiles) {
    synchronized (mediaFilesObservable) {
      mediaFilesObservable.addAll(mediaFiles);
      Collections.sort(mediaFilesObservable);
    }

    // fire the right events
    for (MediaFile mediaFile : mediaFiles) {
      fireAddedEventForMediaFile(mediaFile);
    }

    firePropertyChange(MEDIA_FILES, null, mediaFilesObservable);
  }

  private void fireAddedEventForMediaFile(MediaFile mediaFile) {
    switch (mediaFile.getType()) {
      case FANART:
        firePropertyChange(FANART, null, getFanart());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case POSTER:
        firePropertyChange(POSTER, null, getPoster());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case BANNER:
        firePropertyChange(BANNER, null, getBanner());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case THUMB:
        firePropertyChange(THUMB, null, getThumb());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      default:
        break;
    }
  }

  private void fireRemoveEventForMediaFile(MediaFile mediaFile) {
    switch (mediaFile.getType()) {
      case FANART:
        firePropertyChange(FANART, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case POSTER:
        firePropertyChange(POSTER, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case BANNER:
        firePropertyChange(BANNER, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case THUMB:
        firePropertyChange(THUMB, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      default:
        break;
    }
  }

  public List<MediaFile> getMediaFiles() {
    return mediaFilesObservable;
  }

  public List<MediaFile> getMediaFiles(MediaFileType type) {
    List<MediaFile> mf = new ArrayList<MediaFile>();
    // synchronized (mediaFilesObservable) {
    for (MediaFile mediaFile : new ArrayList<MediaFile>(mediaFilesObservable)) {
      if (mediaFile.getType().equals(type)) {
        mf.add(mediaFile);
      }
      // }
    }
    return mf;
  }

  public void removeAllMediaFiles() {
    List<MediaFile> changedMediafiles = new ArrayList<MediaFile>(mediaFilesObservable);
    synchronized (mediaFilesObservable) {
      for (int i = mediaFilesObservable.size() - 1; i >= 0; i--) {
        mediaFilesObservable.remove(i);
      }
    }
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void removeFromMediaFiles(MediaFile mediaFile) {
    synchronized (mediaFilesObservable) {
      mediaFilesObservable.remove(mediaFile);
    }

    firePropertyChange(MEDIA_FILES, null, mediaFilesObservable);
    fireRemoveEventForMediaFile(mediaFile);
  }

  public void removeAllMediaFilesExceptType(MediaFileType type) {
    List<MediaFile> changedMediafiles = new ArrayList<MediaFile>();
    synchronized (mediaFilesObservable) {
      for (int i = mediaFilesObservable.size() - 1; i >= 0; i--) {
        MediaFile mediaFile = mediaFilesObservable.get(i);
        if (!mediaFile.getType().equals(type)) {
          mediaFilesObservable.remove(i);
          changedMediafiles.add(mediaFile);
        }
      }
    }
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void removeAllMediaFiles(MediaFileType type) {
    List<MediaFile> changedMediafiles = new ArrayList<MediaFile>();
    synchronized (mediaFilesObservable) {
      for (int i = mediaFilesObservable.size() - 1; i >= 0; i--) {
        MediaFile mediaFile = mediaFilesObservable.get(i);
        if (mediaFile.getType().equals(type)) {
          mediaFilesObservable.remove(i);
          changedMediafiles.add(mediaFile);
        }
      }
    }
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void updateMediaFilePath(File oldPath, File newPath) {
    for (MediaFile mf : new ArrayList<MediaFile>(mediaFilesObservable)) {
      mf.fixPathForRenamedFolder(oldPath, newPath);
    }
  }

  public void gatherMediaFileInformation(boolean force) {
    List<MediaFile> mediaFiles = new ArrayList<MediaFile>(mediaFilesObservable);
    for (MediaFile mediaFile : mediaFiles) {
      mediaFile.gatherMediaInformation(force);
    }

    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  public synchronized void saveToDb() {
    // update DB
    synchronized (Globals.entityManager) {
      Globals.entityManager.getTransaction().begin();
      Globals.entityManager.persist(this);
      Globals.entityManager.getTransaction().commit();
    }
  }

  abstract public void callbackForWrittenArtwork(MediaArtworkType type);
}