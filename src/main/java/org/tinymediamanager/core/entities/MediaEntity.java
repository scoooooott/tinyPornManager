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
package org.tinymediamanager.core.entities;

import static org.tinymediamanager.core.Constants.BANNER;
import static org.tinymediamanager.core.Constants.DATE_ADDED;
import static org.tinymediamanager.core.Constants.DATE_ADDED_AS_STRING;
import static org.tinymediamanager.core.Constants.FANART;
import static org.tinymediamanager.core.Constants.HAS_IMAGES;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.NEWLY_ADDED;
import static org.tinymediamanager.core.Constants.ORIGINAL_TITLE;
import static org.tinymediamanager.core.Constants.PATH;
import static org.tinymediamanager.core.Constants.PLOT;
import static org.tinymediamanager.core.Constants.POSTER;
import static org.tinymediamanager.core.Constants.PRODUCTION_COMPANY;
import static org.tinymediamanager.core.Constants.RATING;
import static org.tinymediamanager.core.Constants.SCRAPED;
import static org.tinymediamanager.core.Constants.THUMB;
import static org.tinymediamanager.core.Constants.TITLE;
import static org.tinymediamanager.core.Constants.VOTES;
import static org.tinymediamanager.core.Constants.YEAR;

import java.awt.Dimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
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
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class MediaEntity. The base class for all entities
 * 
 * @author Manuel Laggner
 */
public abstract class MediaEntity extends AbstractModelObject {
  /** The id for the database. */
  protected UUID                       dbId              = UUID.randomUUID();

  /** The ids to store the ID from several metadataproviders. */
  @JsonProperty
  protected HashMap<String, Object>    ids               = new HashMap<>(0);

  @JsonProperty
  protected String                     title             = "";
  @JsonProperty
  protected String                     originalTitle     = "";
  @JsonProperty
  protected String                     year              = "";
  @JsonProperty
  protected String                     plot              = "";
  @JsonProperty
  protected float                      rating            = 0f;
  @JsonProperty
  protected int                        votes             = 0;
  @JsonProperty
  protected String                     path              = "";
  @JsonProperty
  protected Date                       dateAdded         = new Date();
  @JsonProperty
  protected String                     productionCompany = "";
  @JsonProperty
  protected boolean                    scraped           = false;

  @JsonProperty
  private List<MediaFile>              mediaFiles        = new ArrayList<>();
  @JsonProperty
  protected Map<MediaFileType, String> artworkUrlMap     = new HashMap<>();

  protected boolean                    newlyAdded        = false;
  protected boolean                    duplicate         = false;
  protected ReadWriteLock              readWriteLock     = new ReentrantReadWriteLock();

  public MediaEntity() {
  }

  /**
   * Overwrites all null/empty elements with "other" value (but might be empty also)<br>
   * For lists, check with 'contains' and add.<br>
   * Do NOT merge path, dateAdded, scraped, mediaFiles and other crucial properties!
   * 
   * @param other
   */
  public void merge(MediaEntity other) {
    if (other == null) {
      return;
    }

    this.title = StringUtils.isEmpty(this.title) ? other.getTitle() : this.title;
    this.originalTitle = StringUtils.isEmpty(this.originalTitle) ? other.getOriginalTitle() : this.originalTitle;
    this.year = StringUtils.isEmpty(this.year) ? other.getYear() : this.year;
    this.plot = StringUtils.isEmpty(this.plot) ? other.getPlot() : this.plot;
    this.productionCompany = StringUtils.isEmpty(this.productionCompany) ? other.getProductionCompany() : this.productionCompany;

    this.votes = this.votes == 0 ? other.getVotes() : this.votes;
    this.rating = Float.compare(this.rating, 0f) == 0 ? other.getRating() : this.rating;

    for (String key : other.getIds().keySet()) {
      if (!this.ids.containsKey(key)) {
        this.ids.put(key, other.getId(key));
      }
    }
    for (MediaFileType key : other.getArtworkUrls().keySet()) {
      if (!this.artworkUrlMap.containsKey(key)) {
        this.artworkUrlMap.put(key, other.getArtworkUrl(key));
      }
    }
  }

  /**
   * Initialize after loading from database.
   */
  public void initializeAfterLoading() {
    sortMediaFiles();
  }

  protected void sortMediaFiles() {
    Comparator<MediaFile> mediaFileComparator = getMediaFileComparator();
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

  /**
   * @deprecated use getPathNIO()
   */
  @Deprecated
  public String getPath() {
    return path;
  }

  public Path getPathNIO() {
    if (StringUtils.isBlank(path)) {
      return null;
    }
    return Paths.get(path).toAbsolutePath();
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
    title = newValue == null ? "" : newValue.trim();
    firePropertyChange(TITLE, oldValue, newValue);
  }

  public void setOriginalTitle(String newValue) {
    String oldValue = originalTitle;
    originalTitle = newValue == null ? "" : newValue.trim();
    firePropertyChange(ORIGINAL_TITLE, oldValue, newValue);
  }

  public void setPlot(String newValue) {
    String oldValue = plot;
    plot = newValue == null ? "" : newValue.trim();
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

  public int getVotes() {
    return votes;
  }

  public void setVotes(int newValue) {
    int oldValue = this.votes;
    this.votes = newValue;
    firePropertyChange(VOTES, oldValue, newValue);
  }

  public void setYear(String newValue) {
    String oldValue = year;
    year = newValue == null ? "" : newValue.trim();
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
      case CLEARLOGO:
        artworkUrlMap.put(type, url);
        break;
      default:
        return;
    }

    firePropertyChange(type.name().toLowerCase(Locale.ROOT) + "Url", oldValue, url);
  }

  /**
   * get the artwork url for the desired type
   * 
   * @param type
   *          the artwork type
   * @return the url to the artwork type or an empty string
   */
  public String getArtworkUrl(MediaFileType type) {
    String url = artworkUrlMap.get(type);
    return url == null ? "" : url;
  }

  /**
   * get all artwork urls
   * 
   * @return a map containing all urls
   */
  public Map<MediaFileType, String> getArtworkUrls() {
    return artworkUrlMap;
  }

  public void setArtwork(Path file, MediaFileType type) {
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
   * Get a map of all primary artworks. If there are multiple media files for one artwork type, only the first is returned in the map
   * 
   * @return a map of all found artworks
   */
  public Map<MediaFileType, MediaFile> getArtworkMap() {
    Map<MediaFileType, MediaFile> artworkMap = new HashMap<>();
    List<MediaFile> mediaFiles = getMediaFiles();
    for (MediaFile mf : mediaFiles) {
      if (!mf.isGraphic()) {
        continue;
      }
      if (!artworkMap.containsKey(mf.getType())) {
        artworkMap.put(mf.getType(), mf);
      }
    }
    return artworkMap;
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

  public void setDateAddedFromMediaFile(MediaFile mf) {
    try {
      BasicFileAttributes view = Files.readAttributes(mf.getFileAsPath(), BasicFileAttributes.class);
      Date creDat = new Date(view.creationTime().toMillis());
      Date modDat = new Date(view.lastModifiedTime().toMillis());
      if (creDat.compareTo(dateAdded) < 0) {
        setDateAdded(creDat);
      }
      if (modDat.compareTo(dateAdded) < 0) {
        setDateAdded(modDat);
      }
    }
    catch (Exception e) {
    }
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
    // remove ID, if empty/0/null
    // if we only skipped it, the existing entry will stay although someone changed it to empty.
    String v = String.valueOf(value);
    if ("".equals(v) || "0".equals(v) || "null".equals(v)) {
      ids.remove(key);
    }
    else {
      ids.put(key, value);
    }
    firePropertyChange(key, null, value);

    // fire special events for our well known IDs
    if (Constants.TMDB.equals(key) || Constants.IMDB.equals(key) || Constants.TVDB.equals(key) || Constants.TRAKT.equals(key)) {
      firePropertyChange(key + "Id", null, value);
    }
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
      id = Integer.parseInt(String.valueOf(ids.get(key)));
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
    for (MediaFile mediaFile : mediaFiles) {
      addToMediaFiles(mediaFile);
    }
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
    List<MediaFile> mf = new ArrayList<>();
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
    List<MediaFile> mf = new ArrayList<>();
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
   * gets the BIGGEST MediaFile of type(s)<br>
   * useful for getting the right MF for displaying mediaInformation
   * 
   * @return biggest MF
   */
  public MediaFile getBiggestMediaFile(MediaFileType... types) {
    MediaFile mf = null;

    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          if (mf == null || mediaFile.getFilesize() >= mf.getFilesize()) {
            mf = mediaFile;
          }
        }
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  /**
   * From all MediaFiles of specified type, get the newest one (according to MI filedate)
   * 
   * @param type
   *          the MediaFileType to get the MediaFile for
   * @return NULL or MF
   */
  public MediaFile getNewestMediaFilesOfType(MediaFileType... types) {
    MediaFile mf = null;
    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          if (mf == null || mediaFile.getFiledate() >= mf.getFiledate()) {
            // get the latter one
            mf = new MediaFile(mediaFile);
          }
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
    List<MediaFile> mf = new ArrayList<>();
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
    List<MediaFile> changedMediafiles = new ArrayList<>(mediaFiles);
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
    List<MediaFile> changedMediafiles = new ArrayList<>();

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
    List<MediaFile> changedMediafiles = new ArrayList<>();

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

  public void updateMediaFilePath(Path oldPath, Path newPath) {
    readWriteLock.readLock().lock();
    List<MediaFile> mfs = new ArrayList<>(this.mediaFiles);
    readWriteLock.readLock().unlock();
    for (MediaFile mf : mfs) {
      mf.replacePathForRenamedFolder(oldPath, newPath);
    }
  }

  public void gatherMediaFileInformation(boolean force) {
    readWriteLock.readLock().lock();
    List<MediaFile> mediaFiles = new ArrayList<>(this.mediaFiles);
    readWriteLock.readLock().unlock();
    for (MediaFile mediaFile : mediaFiles) {
      mediaFile.gatherMediaInformation(force);
    }

    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  public void fireEventForChangedMediaInformation() {
    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  public boolean isNewlyAdded() {
    return this.newlyAdded;
  }

  public void setNewlyAdded(boolean newValue) {
    boolean oldValue = this.newlyAdded;
    this.newlyAdded = newValue;
    firePropertyChange(NEWLY_ADDED, oldValue, newValue);
  }

  abstract public void saveToDb();

  abstract public void deleteFromDb();

  abstract public void callbackForWrittenArtwork(MediaArtworkType type);

  abstract protected Comparator<MediaFile> getMediaFileComparator();
}
