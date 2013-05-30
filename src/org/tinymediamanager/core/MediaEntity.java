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

/**
 * The Class MediaEntity.
 * 
 * @author Manuel Laggner
 */
@MappedSuperclass
public abstract class MediaEntity extends AbstractModelObject {

  /** The id for the database. */
  @GeneratedValue
  protected int                       id;

  /** The ids to store the ID from several metadataproviders. */
  protected HashMap<String, Object>   ids                  = new HashMap<String, Object>();

  /** The title. */
  protected String                    title                = "";

  /** The original title. */
  protected String                    originalTitle        = "";

  /** The year. */
  protected String                    year                 = "";

  /** The overview. */
  protected String                    plot                 = "";

  /** The rating. */
  protected float                     rating               = 0f;

  /** The path. */
  protected String                    path                 = "";

  /** The fanart url. */
  protected String                    fanartUrl            = "";

  /** The poster url. */
  protected String                    posterUrl            = "";

  /** The banner url. */
  protected String                    bannerUrl            = "";

  /** The thumb url. */
  protected String                    thumbUrl             = "";

  /** The date added. */
  protected Date                      dateAdded            = new Date();

  /** The production company. */
  protected String                    productionCompany    = "";

  /** The scraped. */
  protected boolean                   scraped              = false;

  /** The duplicate flag. */
  @Transient
  protected boolean                   duplicate            = false;

  /** The media files. */
  @OneToMany(cascade = CascadeType.ALL)
  protected List<MediaFile>           mediaFiles           = new ArrayList<MediaFile>();

  /** The media files observable. */
  @Transient
  protected ObservableList<MediaFile> mediaFilesObservable = ObservableCollections.observableList(mediaFiles);

  /**
   * Initialize after loading from database.
   */
  public void initializeAfterLoading() {
    mediaFilesObservable = ObservableCollections.observableList(mediaFiles);
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public HashMap<String, Object> getIds() {
    return ids;
  }

  /**
   * Gets the fanart url.
   * 
   * @return the fanart url
   */
  public String getFanartUrl() {
    return fanartUrl;
  }

  /**
   * Gets the path to the fanart.
   * 
   * @return the fanart
   */
  public String getFanart() {
    List<MediaFile> fanarts = getMediaFiles(MediaFileType.FANART);
    if (fanarts.size() > 0) {
      return fanarts.get(0).getFile().getPath();
    }
    return "";
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the original title.
   * 
   * @return the original title
   */
  public String getOriginalTitle() {
    return originalTitle;
  }

  /**
   * Gets the plot.
   * 
   * @return the plot
   */
  public String getPlot() {
    return plot;
  }

  /**
   * Gets the path.
   * 
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets the poster url.
   * 
   * @return the poster url
   */
  public String getPosterUrl() {
    return posterUrl;
  }

  /**
   * Gets the path to the poster.
   * 
   * @return the poster
   */
  public String getPoster() {
    List<MediaFile> poster = getMediaFiles(MediaFileType.POSTER);
    if (poster.size() > 0) {
      return poster.get(0).getFile().getPath();
    }
    return "";
  }

  /**
   * Gets the banner url.
   * 
   * @return the banner url
   */
  public String getBannerUrl() {
    return bannerUrl;
  }

  /**
   * Gets the banner.
   * 
   * @return the banner
   */
  public String getBanner() {
    List<MediaFile> banner = getMediaFiles(MediaFileType.BANNER);
    if (banner.size() > 0) {
      return banner.get(0).getFile().getPath();
    }
    return "";
  }

  /**
   * Gets the thumb.
   * 
   * @return the thumb
   */
  public String getThumb() {
    List<MediaFile> thumbs = getMediaFiles(MediaFileType.THUMB);
    if (thumbs.size() > 0) {
      return thumbs.get(0).getFile().getPath();
    }
    return "";
  }

  /**
   * Gets the rating.
   * 
   * @return the rating
   */
  public float getRating() {
    return rating;
  }

  /**
   * Gets the year.
   * 
   * @return the year
   */
  public String getYear() {
    return year;
  }

  /**
   * Sets the id.
   * 
   * @param ids
   *          the ids
   */
  public void setIds(HashMap<String, Object> ids) {
    // this.ids = ids;
    for (Entry<String, Object> entry : ids.entrySet()) {
      setId((String) entry.getKey(), entry.getValue().toString());
      firePropertyChange(entry.getKey(), null, entry.getValue());
    }
  }

  /**
   * Sets the title.
   * 
   * @param newValue
   *          the new title
   */
  public void setTitle(String newValue) {
    String oldValue = title;
    title = newValue.trim();
    firePropertyChange(TITLE, oldValue, newValue);
  }

  /**
   * Sets the original title.
   * 
   * @param newValue
   *          the new original title
   */
  public void setOriginalTitle(String newValue) {
    String oldValue = originalTitle;
    originalTitle = newValue.trim();
    firePropertyChange(ORIGINAL_TITLE, oldValue, newValue);
  }

  /**
   * Sets the plot.
   * 
   * @param newValue
   *          the new plot
   */
  public void setPlot(String newValue) {
    String oldValue = plot;
    plot = newValue.trim();
    firePropertyChange(PLOT, oldValue, newValue);
  }

  /**
   * Sets the path.
   * 
   * @param newValue
   *          the new path
   */
  public void setPath(String newValue) {
    String oldValue = path;
    path = newValue;
    firePropertyChange(PATH, oldValue, newValue);
  }

  /**
   * Sets the rating.
   * 
   * @param newValue
   *          the new rating
   */
  public void setRating(float newValue) {
    float oldValue = rating;
    rating = newValue;
    firePropertyChange(RATING, oldValue, newValue);
  }

  /**
   * Sets the year.
   * 
   * @param newValue
   *          the new year
   */
  public void setYear(String newValue) {
    String oldValue = year;
    year = newValue.trim();
    firePropertyChange(YEAR, oldValue, newValue);
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange(POSTER_URL, oldValue, newValue);
  }

  /**
   * Sets the poster.
   * 
   * @param poster
   *          the new poster
   */
  public void setPoster(File poster) {
    setImage(poster, MediaFileType.POSTER);
  }

  /**
   * Clear poster.
   */
  public void clearPoster() {
    removeAllMediaFiles(MediaFileType.POSTER);
  }

  /**
   * Sets the banner url.
   * 
   * @param newValue
   *          the new banner url
   */
  public void setBannerUrl(String newValue) {
    String oldValue = bannerUrl;
    bannerUrl = newValue;
    firePropertyChange(BANNER_URL, oldValue, newValue);
  }

  /**
   * Sets the banner.
   * 
   * @param banner
   *          the new banner
   */
  public void setBanner(File banner) {
    setImage(banner, MediaFileType.BANNER);
  }

  /**
   * Clear banner.
   */
  public void clearBanner() {
    removeAllMediaFiles(MediaFileType.BANNER);
  }

  /**
   * Sets the thumb.
   * 
   * @param thumb
   *          the new thumb
   */
  public void setThumb(File thumb) {
    setImage(thumb, MediaFileType.THUMB);
  }

  /**
   * Clear thumb.
   */
  public void clearThumb() {
    removeAllMediaFiles(MediaFileType.THUMB);
  }

  /**
   * Sets the fanart url.
   * 
   * @param newValue
   *          the new fanart url
   */
  public void setFanartUrl(String newValue) {
    String oldValue = fanartUrl;
    fanartUrl = newValue;
    firePropertyChange(FANART_URL, oldValue, newValue);
  }

  /**
   * Sets the thumb url.
   * 
   * @param newValue
   *          the new thumb url
   */
  public void setThumbUrl(String newValue) {
    String oldValue = thumbUrl;
    thumbUrl = newValue;
    firePropertyChange(THUMB_URL, oldValue, newValue);
  }

  /**
   * Gets the thumb url.
   * 
   * @return the thumb url
   */
  public String getThumbUrl() {
    return thumbUrl;
  }

  /**
   * Sets the fanart.
   * 
   * @param fanart
   *          the new fanart
   */
  public void setFanart(File fanart) {
    setImage(fanart, MediaFileType.FANART);
  }

  /**
   * Clear fanart.
   */
  public void clearFanart() {
    removeAllMediaFiles(MediaFileType.FANART);
  }

  /**
   * Sets the image.
   * 
   * @param file
   *          the file
   * @param type
   *          the type
   */
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

  /**
   * Gets the date added.
   * 
   * @return the date added
   */
  public Date getDateAdded() {
    return dateAdded;
  }

  /**
   * Gets the date added as string.
   * 
   * @return the date added as string
   */
  public String getDateAddedAsString() {
    if (dateAdded == null) {
      return "";
    }

    return SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()).format(dateAdded);
  }

  /**
   * Sets the date added.
   * 
   * @param newValue
   *          the new date added
   */
  public void setDateAdded(Date newValue) {
    Date oldValue = this.dateAdded;
    this.dateAdded = newValue;
    firePropertyChange(DATE_ADDED, oldValue, newValue);
    firePropertyChange(DATE_ADDED_AS_STRING, oldValue, newValue);
  }

  /**
   * Gets the production company.
   * 
   * @return the production company
   */
  public String getProductionCompany() {
    return productionCompany;
  }

  /**
   * Sets the production company.
   * 
   * @param newValue
   *          the new production company
   */
  public void setProductionCompany(String newValue) {
    String oldValue = this.productionCompany;
    this.productionCompany = newValue;
    firePropertyChange(PRODUCTION_COMPANY, oldValue, newValue);
  }

  /**
   * Sets the scraped.
   * 
   * @param newValue
   *          the new scraped
   */
  protected void setScraped(boolean newValue) {
    this.scraped = newValue;
    firePropertyChange(SCRAPED, false, newValue);
  }

  /**
   * checks if this movie has been scraped.
   * 
   * @return isScraped
   */
  public boolean isScraped() {
    return scraped;
  }

  /**
   * Sets the duplicate.
   */
  public void setDuplicate() {
    this.duplicate = true;
  }

  /**
   * Clear duplicate.
   */
  public void clearDuplicate() {
    this.duplicate = false;
  }

  /**
   * Checks if is duplicate.
   * 
   * @return true, if is duplicate
   */
  public boolean isDuplicate() {
    return this.duplicate;
  }

  /**
   * Sets the id.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void setId(String key, Object value) {
    ids.put(key, value);
  }

  /**
   * Gets the id.
   * 
   * @param key
   *          the key
   * @return the id
   */
  public Object getId(String key) {
    return ids.get(key);
  }

  /**
   * Adds a single MediaFile to this mediaentity.
   * 
   * @param mediaFile
   *          the mediafile
   */
  public void addToMediaFiles(MediaFile mediaFile) {
    synchronized (mediaFilesObservable) {
      mediaFilesObservable.add(mediaFile);
      Collections.sort(mediaFilesObservable);
    }

    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());
    fireAddedEventForMediaFile(mediaFile);
  }

  /**
   * Adds a list of MediaFiles to this mediaentity.
   * 
   * @param mediaFiles
   *          the media files
   */
  public void addToMediaFiles(ArrayList<MediaFile> mediaFiles) {
    synchronized (mediaFilesObservable) {
      mediaFilesObservable.addAll(mediaFiles);
      Collections.sort(mediaFilesObservable);
    }

    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());

    // fire the right events
    for (MediaFile mediaFile : mediaFiles) {
      fireAddedEventForMediaFile(mediaFile);
    }
  }

  /**
   * Fire added event for media file.
   * 
   * @param mediaFile
   *          the media file
   */
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

  /**
   * Fire remove event for media file.
   * 
   * @param mediaFile
   *          the media file
   */
  private void fireRemoveEventForMediaFile(MediaFile mediaFile) {
    switch (mediaFile.getType()) {
      case FANART:
        firePropertyChange(FANART, " ", "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case POSTER:
        firePropertyChange(POSTER, " ", "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case BANNER:
        firePropertyChange(BANNER, " ", "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case THUMB:
        firePropertyChange(THUMB, " ", "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      default:
        break;
    }
  }

  /**
   * Gets the media files.
   * 
   * @return the media files
   */
  public List<MediaFile> getMediaFiles() {
    return this.mediaFilesObservable;
  }

  /**
   * Gets the media files of a specific MediaFile type.
   * 
   * @param type
   *          the type
   * @return the media files
   */
  public List<MediaFile> getMediaFiles(MediaFileType type) {
    List<MediaFile> mf = new ArrayList<MediaFile>();
    synchronized (mediaFilesObservable) {
      for (MediaFile mediaFile : this.mediaFilesObservable) {
        if (mediaFile.getType().equals(type)) {
          mf.add(mediaFile);
        }
      }
    }
    return mf;
  }

  /**
   * Clears all the media files.
   */
  public void removeAllMediaFiles() {
    synchronized (mediaFilesObservable) {
      for (int i = mediaFilesObservable.size() - 1; i >= 0; i--) {
        MediaFile mediaFile = mediaFilesObservable.get(i);
        mediaFilesObservable.remove(i);
        fireRemoveEventForMediaFile(mediaFile);
      }
    }
    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());
  }

  /**
   * Removes the from media files.
   * 
   * @param mediaFile
   *          the media file
   */
  public void removeFromMediaFiles(MediaFile mediaFile) {
    synchronized (mediaFilesObservable) {
      mediaFilesObservable.remove(mediaFile);
    }

    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());
    fireRemoveEventForMediaFile(mediaFile);
  }

  /**
   * Removes specific type the from media files.
   * 
   * @param type
   *          the MediaFileType
   */
  public void removeAllMediaFilesExceptType(MediaFileType type) {
    synchronized (mediaFilesObservable) {
      for (int i = mediaFilesObservable.size() - 1; i >= 0; i--) {
        MediaFile mediaFile = mediaFilesObservable.get(i);
        if (!mediaFile.getType().equals(type)) {
          mediaFilesObservable.remove(i);
          fireRemoveEventForMediaFile(mediaFile);
        }
      }
    }
    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());
  }

  /**
   * Removes the all media files.
   * 
   * @param type
   *          the type
   */
  public void removeAllMediaFiles(MediaFileType type) {
    synchronized (mediaFilesObservable) {
      for (int i = mediaFilesObservable.size() - 1; i >= 0; i--) {
        MediaFile mediaFile = mediaFilesObservable.get(i);
        if (mediaFile.getType().equals(type)) {
          mediaFilesObservable.remove(i);
          fireRemoveEventForMediaFile(mediaFile);
        }
      }
    }
    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());
  }

  /**
   * Update media file path.
   * 
   * @param oldPath
   *          the old path
   * @param newPath
   *          the new path
   */
  public void updateMediaFilePath(File oldPath, File newPath) {
    synchronized (mediaFilesObservable) {
      for (MediaFile mf : mediaFilesObservable) {
        mf.fixPathForRenamedFolder(oldPath, newPath);
      }
    }
  }

  public void gatherMediaFileInformation(boolean force) {
    for (MediaFile mediaFile : mediaFiles) {
      mediaFile.gatherMediaInformation(force);
    }

    firePropertyChange(MEDIA_INFORMATION, false, true);
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
