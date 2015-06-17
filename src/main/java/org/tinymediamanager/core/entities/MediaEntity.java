/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.core.entities;

import static org.tinymediamanager.core.Constants.*;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class MediaEntity. The base class for all entities
 * 
 * @author Manuel Laggner
 */
public abstract class MediaEntity extends AbstractModelObject {
  protected static Comparator<MediaFile>  mediaFileComparator    = null;
  // dirty flag listener
  protected static PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
                                                                   @Override
                                                                   public void propertyChange(PropertyChangeEvent evt) {
                                                                     if (evt.getSource() instanceof MediaEntity) {
                                                                       ((MediaEntity) evt.getSource()).dirty = true;
                                                                     }
                                                                   }
                                                                 };

  /** The id for the database. */
  protected UUID                          dbId                   = UUID.randomUUID();

  /** The ids to store the ID from several metadataproviders. */
  @JsonProperty
  protected HashMap<String, Object>       ids                    = new HashMap<String, Object>(0);

  @JsonProperty
  protected String                        title                  = "";
  @JsonProperty
  protected String                        originalTitle          = "";
  @JsonProperty
  protected String                        year                   = "";
  @JsonProperty
  protected String                        plot                   = "";
  @JsonProperty
  protected float                         rating                 = 0f;
  @JsonProperty
  protected String                        path                   = "";
  @JsonProperty
  protected Date                          dateAdded              = new Date();
  @JsonProperty
  protected String                        productionCompany      = "";
  @JsonProperty
  protected boolean                       scraped                = false;

  @JsonProperty
  private List<MediaFile>                 mediaFiles             = new ArrayList<MediaFile>();
  @JsonProperty
  protected Map<MediaFileType, String>    artworkUrlMap          = new HashMap<MediaFileType, String>();

  protected boolean                       duplicate              = false;
  public boolean                          justAdded              = false;
  protected boolean                       dirty                  = false;
  protected ReadWriteLock                 readWriteLock          = new ReentrantReadWriteLock();

  public MediaEntity() {
    // add this ME to the dirty listener
    addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Initialize after loading from database.
   */
  public void initializeAfterLoading() {
    sortMediaFiles();
  }

  protected void sortMediaFiles() {
    if (mediaFileComparator != null) {
      Collections.sort(mediaFiles, mediaFileComparator);
    }
    else {
      Collections.sort(mediaFiles);
    }
  }

  /**
   * get the INTERNAL ID of this object. Do not confuse it with the IDs from the metadata provider!
   * 
   * @return internal id
   */
  public UUID getDbId() {
    return dbId;
  }

  public void setDbId(UUID id) {
    this.dbId = id;
  }

  public HashMap<String, Object> getIds() {
    return ids;
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

  /**
   * Gets the dimension of the (first) artwork of the given type
   * 
   * @param type
   *          the artwork type
   * @return the dimension of the artwork or a zero dimension if no artwork has been found
   */
  public Dimension getArtworkDimension(MediaFileType type) {
    List<MediaFile> mediaFiles = getMediaFiles(type);
    if (mediaFiles.size() > 0) {
      MediaFile mediaFile = mediaFiles.get(0);
      return new Dimension(mediaFile.getVideoWidth(), mediaFile.getVideoHeight());
    }
    return new Dimension(0, 0);
  }

  /**
   * Gets the file name of the (first) artwork of the given type
   * 
   * @param type
   *          the artwork type
   * @return the file name of the artwork or an empty string if nothing has been found
   */
  public String getArtworkFilename(MediaFileType type) {
    List<MediaFile> thumbs = getMediaFiles(type);
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

  public void setIds(Map<String, Object> ids) {
    for (Entry<String, Object> entry : ids.entrySet()) {
      if (StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null) {
        setId(entry.getKey(), entry.getValue());
        firePropertyChange(entry.getKey(), null, entry.getValue());
      }
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

  public void setArtworkUrl(String url, MediaFileType type) {
    String oldValue = getArtworkFilename(type);

    switch (type) {
      case POSTER:
      case FANART:
      case BANNER:
      case THUMB:
      case CLEARART:
      case DISCART:
      case LOGO:
        artworkUrlMap.put(type, url);
        break;

      default:
        return;
    }

    firePropertyChange(type.name().toLowerCase() + "Url", oldValue, url);
  }

  public String getArtworkUrl(MediaFileType type) {
    String url = artworkUrlMap.get(type);
    return url == null ? "" : url;
  }

  public void setArtwork(File file, MediaFileType type) {
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

  public void removeId(String key) {
    Object obj = ids.remove(key);
    if (obj != null) {
      firePropertyChange(key, obj, null);
    }
  }

  public Object getId(String key) {
    return ids.get(key);
  }

  /**
   * any ID as String or empty
   */
  public String getIdAsString(String key) {
    Object obj = ids.get(key);
    if (obj == null) {
      return "";
    }
    return String.valueOf(obj);
  }

  /**
   * any ID as int or 0
   */
  public int getIdAsInt(String key) {
    int id = 0;
    try {
      id = Integer.valueOf(String.valueOf(ids.get(key)));
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  public void addToMediaFiles(MediaFile mediaFile) {
    readWriteLock.writeLock().lock();
    // only store the MF if it is not in the list or if the type has been changed
    if (mediaFiles.contains(mediaFile)) {
      int i = mediaFiles.indexOf(mediaFile);
      if (i >= 0) {
        MediaFile oldMf = mediaFiles.get(i);
        if (oldMf.getType() != mediaFile.getType()) {
          mediaFiles.remove(i);
        }
      }
    }
    if (!mediaFiles.contains(mediaFile)) {
      mediaFiles.add(mediaFile);
      sortMediaFiles();
    }

    readWriteLock.writeLock().unlock();

    firePropertyChange(MEDIA_FILES, null, mediaFiles);
    fireAddedEventForMediaFile(mediaFile);
  }

  public void addToMediaFiles(List<MediaFile> mediaFiles) {
    readWriteLock.writeLock().lock();
    this.mediaFiles.addAll(mediaFiles);
    sortMediaFiles();
    readWriteLock.writeLock().unlock();

    // fire the right events
    for (MediaFile mediaFile : mediaFiles) {
      fireAddedEventForMediaFile(mediaFile);
    }

    firePropertyChange(MEDIA_FILES, null, mediaFiles);
  }

  private void fireAddedEventForMediaFile(MediaFile mediaFile) {
    switch (mediaFile.getType()) {
      case FANART:
        firePropertyChange(FANART, null, mediaFile.getFile().getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case POSTER:
        firePropertyChange(POSTER, null, mediaFile.getFile().getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case BANNER:
        firePropertyChange(BANNER, null, mediaFile.getFile().getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case THUMB:
        firePropertyChange(THUMB, null, mediaFile.getFile().getPath());
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
    List<MediaFile> mf = new ArrayList<MediaFile>();
    readWriteLock.readLock().lock();
    try {
      mf.addAll(mediaFiles);
    }
    finally {
      readWriteLock.readLock().unlock();
    }
    return mf;
  }

  /**
   * gets all MediaFiles from specific type<br>
   * <b>Can be one or multiple types!</b>
   * 
   * @param types
   *          1-N types
   * @return list of MF (may be empty, but never null)
   */
  public List<MediaFile> getMediaFiles(MediaFileType... types) {
    List<MediaFile> mf = new ArrayList<MediaFile>();
    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (match) {
        mf.add(mediaFile);
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  /**
   * From all MediaFiles of specified type, get the newest one (according to MI filedate)
   * 
   * @param type
   * @return NULL or MF
   */
  public MediaFile getNewestMediaFilesOfType(MediaFileType type) {
    MediaFile mf = null;
    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      if (mediaFile.getType().equals(type)) {
        if (mf == null || mediaFile.getFiledate() >= mf.getFiledate()) {
          // get the latter one
          mf = new MediaFile(mediaFile);
        }
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  /**
   * gets all MediaFiles EXCEPT from specific type<br>
   * <b>Can be one or multiple types!</b>
   * 
   * @param types
   *          1-N types
   * @return list of MF (may be empty, but never null)
   */
  public List<MediaFile> getMediaFilesExceptType(MediaFileType... types) {
    List<MediaFile> mf = new ArrayList<MediaFile>();
    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (!match) {
        mf.add(mediaFile);
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  public void removeAllMediaFiles() {
    List<MediaFile> changedMediafiles = new ArrayList<MediaFile>(mediaFiles);
    readWriteLock.writeLock().lock();
    for (int i = mediaFiles.size() - 1; i >= 0; i--) {
      mediaFiles.remove(i);
    }
    readWriteLock.writeLock().unlock();
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void removeFromMediaFiles(MediaFile mediaFile) {
    readWriteLock.writeLock().lock();
    try {
      mediaFiles.remove(mediaFile);
    }
    finally {
      readWriteLock.writeLock().unlock();
    }

    firePropertyChange(MEDIA_FILES, null, mediaFiles);
    fireRemoveEventForMediaFile(mediaFile);
  }

  public void removeAllMediaFilesExceptType(MediaFileType type) {
    List<MediaFile> changedMediafiles = new ArrayList<MediaFile>();

    readWriteLock.writeLock().lock();
    for (int i = mediaFiles.size() - 1; i >= 0; i--) {
      MediaFile mediaFile = mediaFiles.get(i);
      if (!mediaFile.getType().equals(type)) {
        mediaFiles.remove(i);
        changedMediafiles.add(mediaFile);
      }
    }
    readWriteLock.writeLock().unlock();
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void removeAllMediaFiles(MediaFileType type) {
    List<MediaFile> changedMediafiles = new ArrayList<MediaFile>();

    readWriteLock.writeLock().lock();
    for (int i = mediaFiles.size() - 1; i >= 0; i--) {
      MediaFile mediaFile = mediaFiles.get(i);
      if (mediaFile.getType().equals(type)) {
        mediaFiles.remove(i);
        changedMediafiles.add(mediaFile);
      }
    }
    readWriteLock.writeLock().unlock();
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void updateMediaFilePath(File oldPath, File newPath) {
    readWriteLock.readLock().lock();
    List<MediaFile> mfs = new ArrayList<MediaFile>(this.mediaFiles);
    readWriteLock.readLock().unlock();
    for (MediaFile mf : mfs) {
      mf.replacePathForRenamedFolder(oldPath, newPath);
    }
  }

  public void gatherMediaFileInformation(boolean force) {
    readWriteLock.readLock().lock();
    List<MediaFile> mediaFiles = new ArrayList<MediaFile>(this.mediaFiles);
    readWriteLock.readLock().unlock();
    for (MediaFile mediaFile : mediaFiles) {
      mediaFile.gatherMediaInformation(force);
    }

    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  abstract public void saveToDb();

  abstract public void deleteFromDb();

  abstract public void callbackForWrittenArtwork(MediaArtworkType type);
}